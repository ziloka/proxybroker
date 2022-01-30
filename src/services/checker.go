package services

import (
	"encoding/json"
	"io"
	"net/http"
	"strings"
	"time"
	"github.com/Ziloka/ProxyBroker/structs"
	"github.com/Ziloka/ProxyBroker/utils"
)

type HttpResponse struct {
	Origin string
}

// https://golangbyexample.com/return-value-goroutine-go/
func Check(proxiesChan chan structs.Proxy, proxies *[]structs.Proxy, myRemoteAddr string, proxy structs.Proxy, verbose bool) {

	includesProxy := func () bool {
		for _, a := range *proxies {
			if a.Proxy == proxy.Proxy {
				return true
			}
		}
		return false
	}

	if(includesProxy()) {
		return;
	}

	// test proxy with provided protocol
	if proxy.Protocol != "" {
		trp := structs.NewTransport(proxy.Protocol, proxy.Proxy)
		// https://medium.com/@nate510/don-t-use-go-s-default-http-client-4804cb19f779
		httpClient := &http.Client{
			Transport: trp,
			Timeout: time.Second * 5,
		}
		res, httpGetErr := httpClient.Get("https://httpbin.org/ip?json")
		if httpGetErr != nil {
			return
		}
		defer res.Body.Close()
		filterProxies(proxiesChan, proxies, myRemoteAddr, trp, res, proxy)
	} else {
		// test proxy with all protocols
		protocols := []string{"http", "socks4", "socks5"}
		trp := &structs.CustomTransport{}
		res := &http.Response{}
		for _, protocol := range protocols {
			trp = structs.NewTransport(protocol, proxy.Proxy)
			// https://medium.com/@nate510/don-t-use-go-s-default-http-client-4804cb19f779
			httpClient := &http.Client{
				Transport: trp,
				Timeout: time.Second * 5,
			}
			httpbinRes, httpGetErr := httpClient.Get("https://httpbin.org/ip?json")
			if httpGetErr != nil {
				continue
			}
			defer httpbinRes.Body.Close()
			res = httpbinRes
		}
		if res != nil {
			filterProxies(proxiesChan, proxies, myRemoteAddr, trp, res, proxy)
		}
	}
}

func filterProxies(proxiesChan chan structs.Proxy, proxies *[]structs.Proxy, myRemoteAddr string, tp *structs.CustomTransport, res *http.Response, proxy structs.Proxy) {
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
			// Don't add duplicates of proxies or proxies that origin from my ip address
			if !strings.Contains(obj.Origin, myRemoteAddr) {
				// Proxy is High
				proxy.IsOnline = true
				proxy.AvgRespTime = tp.Duration()
				proxy.ConnDuration = tp.ConnDuration()
				proxy.ReqDuration = tp.ReqDuration()
				*proxies = append(*proxies, proxy)
				proxiesChan <- proxy
			}
		}
	}
}
