package services

import (
	"encoding/json"
	"github.com/Ziloka/ProxyBroker/structs"
	"github.com/Ziloka/ProxyBroker/utils"
	"io"
	"net"
	"net/http"
	"strings"
)

type HttpResponse struct {
	Origin string
}

// https://golangbyexample.com/return-value-goroutine-go/
func Check(proxies chan structs.Proxy, myRemoteAddr string, proxy structs.Proxy) {
	trp := structs.NewTransport(proxy.Protocol, proxy.Proxy)
	httpClient := &http.Client{Transport: trp}
	res, httpGetErr := httpClient.Get("https://httpbin.org/ip?json")
	if httpGetErr != nil {
		return
	}
	defer res.Body.Close()
	filterProxies(proxies, myRemoteAddr, trp, res, proxy)

}

func filterProxies(proxies chan structs.Proxy, myRemoteAddr string, tp *structs.CustomTransport, res *http.Response, proxy structs.Proxy) {
	if res.StatusCode == 200 {
		// Check if the response is valid JSON
		// May be HTML stating 500 server error
		b, err := io.ReadAll(res.Body)
		if err != nil {
			return
		}
		if utils.IsJSON(string(b)) {
			// https://stackoverflow.com/questions/21197239/decoding-json-using-json-unmarshal-vs-json-newdecoder-decode
			// https://stackoverflow.com/a/21198571
			obj := &HttpResponse{}
			json.Unmarshal(b, &obj)
			if !strings.Contains(obj.Origin, myRemoteAddr) {
				// fmt.Printf("D: %v, RD: %v, CD: %v\n", tp.Duration(), tp.ReqDuration(), tp.ConnDuration())
				// Proxy is High
				addrs, err := net.LookupAddr(proxy.Proxy)
				if err != nil {
					addrs = []string{}
				}
				proxyStruct := structs.Proxy{
					Proxy:        proxy.Proxy,
					AvgRespTime:  tp.Duration(),
					ConnDuration: tp.ConnDuration(),
					ReqDuration:  tp.ReqDuration(),
					HostName:     addrs,
				}
				proxies <- proxyStruct
			} else {
				// Proxy is either transparent or anonymous

			}
		}
	}
}
