package services

import (
	"encoding/json"
	"io"
	"net/http"
	"strings"
	"github.com/Ziloka/ProxyBroker/structs"
	"github.com/Ziloka/ProxyBroker/utils"
)

type HttpResponse struct {
	Origin string
}

// https://golangbyexample.com/return-value-goroutine-go/
func Check(proxies chan structs.Proxy, myRemoteAddr string, proxy structs.Proxy) {
	// test proxy with provided protocol
	if proxy.Protocol != "" {
		trp := structs.NewTransport(proxy.Protocol, proxy.Proxy)
		httpClient := &http.Client{Transport: trp}
		res, httpGetErr := httpClient.Get("https://httpbin.org/ip?json")
		if httpGetErr != nil {
			return
		}
		defer res.Body.Close()
		filterProxies(proxies, myRemoteAddr, trp, res, proxy)
	} else {
		// test proxy with all protocols
		protocols := []string{"http", "socks4", "socks5"}
		trp := &structs.CustomTransport{}
		res := &http.Response{}
		for _, protocol := range protocols {
			trp = structs.NewTransport(protocol, proxy.Proxy)
			httpClient := &http.Client{Transport: trp}
			httpbinRes, httpGetErr := httpClient.Get("https://httpbin.org/ip?json")
			if httpGetErr != nil {
				continue
			}
				defer httpbinRes.Body.Close()
				res = httpbinRes
		}
		if res != nil {
			filterProxies(proxies, myRemoteAddr, trp, res, proxy)
		}
	}

}

func filterProxies(proxies chan structs.Proxy, myRemoteAddr string, tp *structs.CustomTransport, res *http.Response, proxy structs.Proxy) {
	if res.StatusCode == 200 {
		// Check if the response is valid JSON
		// May be HTML stating 500 server error
		b, err := io.ReadAll(res.Body)
		if err != nil {
			panic(err)
		}
		if utils.IsJSON(string(b)) {
			// https://stackoverflow.com/questions/21197239/decoding-json-using-json-unmarshal-vs-json-newdecoder-decode
			// https://stackoverflow.com/a/21198571
			obj := &HttpResponse{}
			json.Unmarshal(b, &obj)
			if !strings.Contains(obj.Origin, myRemoteAddr) {
				// fmt.Printf("D: %v, RD: %v, CD: %v\n", tp.Duration(), tp.ReqDuration(), tp.ConnDuration())
				// Proxy is High
				proxyStruct := structs.Proxy{
					Proxy:        proxy.Proxy,
					AvgRespTime:  tp.Duration(),
					ConnDuration: tp.ConnDuration(),
					ReqDuration:  tp.ReqDuration(),
				}
				proxies <- proxyStruct
			}
		}
	}
}
