package services

import (
	"embed"
	"encoding/json"
	"fmt"
	"github.com/Ziloka/ProxyBroker/structs"
	"github.com/Ziloka/ProxyBroker/utils"
	"github.com/oschwald/geoip2-golang"
	"log"
	"net/http"
	"time"
)

func handler(w http.ResponseWriter, r *http.Request) {
	fmt.Fprintf(w, "Hi there, I love %s!", r.URL.Path[1:])
}

func StartWebService(assetFS embed.FS, port string, verbose bool) {

	checkedProxies := []structs.Proxy{}
	checkedProxiesChan := make(chan structs.Proxy, 99999)
	bytes, readFileError := assetFS.ReadFile("assets/GeoLite2-Country.mmdb")

	if readFileError != nil {
		return
	}

	db, dbErr := geoip2.FromBytes(bytes)
	if dbErr != nil {
		return
	}

	ticker := utils.NewTicker(5, time.Minute)
	quit := make(chan string)
	go func() {
		for {
			select {
			case <-ticker.C:

				// Collect proxies
				quit := make(chan bool)
				proxiesChan := make(chan []structs.Proxy, 99999)
				Collect(assetFS, db, quit, proxiesChan, []string{"http", "https", "socks4", "socks5"}, []string{}, []string{}, verbose)
				publicIpAddr, err := GetpublicIpAddr()
				if err != nil {
					return
				}

				// Check current proxies
				proxiesChan <- checkedProxies

			waitForProxies:
				for {
					select {
					case proxiesArr := <-proxiesChan:
						for i := range proxiesArr {
							go Check(checkedProxiesChan, publicIpAddr, proxiesArr[i], verbose)
						}
					case receivedProxy := <-checkedProxiesChan:
						if verbose {
							fmt.Println("finished processing %s proxy", receivedProxy.Proxy)
						}
						convertToArrStr := func () ([]string) {
							arr := []string{}
							for _, proxyStruct := range(checkedProxies) {
								arr = append(arr, proxyStruct.Proxy)
							}
							return arr
						}

						if receivedProxy.IsOnline && !utils.Contains(convertToArrStr(), receivedProxy.Proxy) {
							checkedProxies = append(checkedProxies, receivedProxy)
						}
					case <-quit:
						break waitForProxies
					}
				}
			case <-quit:
				ticker.Stop()
				return
			}
		}
	}()

	http.HandleFunc("/", handler)
	http.HandleFunc("/api", func(w http.ResponseWriter, r *http.Request) {

		jsonResponse, jsonErr := json.Marshal(checkedProxies)
		if jsonErr != nil {
			return
		}

		fmt.Fprint(w, string(jsonResponse))

	})

	// https://stackoverflow.com/a/11124241
	log.Fatal(http.ListenAndServe(fmt.Sprintf(":%v", port), nil))

}
