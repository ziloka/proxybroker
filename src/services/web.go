package services

import (
	"embed"
	"fmt"
	"github.com/Ziloka/ProxyBroker/structs"
	"github.com/Ziloka/ProxyBroker/utils"
	"github.com/gin-gonic/gin"
	"github.com/oschwald/geoip2-golang"
	"time"
)

func StartService(assetFS embed.FS, port int, verbose bool, isRestService bool){
	if(isRestService){
		startWebService(assetFS, port, verbose)
	} else {

	}
}

func startWebService(assetFS embed.FS, port int, verbose bool) {

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
				Collect(assetFS, db, quit, proxiesChan, []string{"http", "https", "socks4", "socks5"}, []string{}, []int{}, verbose)
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
							go Check(checkedProxiesChan, &checkedProxies, publicIpAddr, proxiesArr[i], verbose)
						}
					case receivedProxy := <-checkedProxiesChan:
						if verbose {
							fmt.Printf("finished processing %s proxy\n", receivedProxy.Proxy)
						}
						convertToArrStr := func() []string {
							arr := []string{}
							for _, proxyStruct := range checkedProxies {
								arr = append(arr, proxyStruct.Proxy)
							}
							return arr
						}

						if receivedProxy.IsOnline && !utils.StringContains(convertToArrStr(), receivedProxy.Proxy) {
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

	r := gin.Default()

	r.GET("/api", func(c *gin.Context) {
		c.JSON(200, checkedProxies)
	})
	r.Run()

}
