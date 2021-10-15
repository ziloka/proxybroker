package services

import (
	"crypto/tls"
	"encoding/json"
	"net"
	"net/http"
	"net/url"
	"strings"
	"time"
)

type HttpResponse struct {
	// https://github.com/nsqio/nsq/issues/906#issuecomment-410726215
	Origin string `json:"origin"`
}

// https://golangbyexample.com/return-value-goroutine-go/
func Check(proxies *[]string, myRemoteAddr string, proxy string) {
	// https://stackoverflow.com/questions/14661511/setting-up-proxy-for-http-client
	// https://stackoverflow.com/a/14663620
	proxyUrl, _ := url.Parse("http://"+proxy)
	// https://medium.com/@nate510/don-t-use-go-s-default-http-client-4804cb19f779
	httpClient := &http.Client{
		Timeout: 1 * time.Second, 
		Transport: &http.Transport{
			Proxy: http.ProxyURL(proxyUrl), 
			TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
			Dial:(&net.Dialer{
				Timeout: 1 * time.Second,
			}).Dial,
			TLSHandshakeTimeout: 1 * time.Second,
		},
	}
	// https://stackoverflow.com/questions/17156371/how-to-get-json-response-from-http-get
	// https://stackoverflow.com/a/31129967
	res, httpgetErr := httpClient.Get("http://httpbin.org/ip?json")
	if httpgetErr != nil {
		return
	}
	defer res.Body.Close()
	if res.StatusCode == 200 {
		obj := &HttpResponse{}
		json.NewDecoder(res.Body).Decode(obj)
		if obj.Origin != myRemoteAddr {
			if !strings.Contains(obj.Origin, myRemoteAddr) {
				// Proxy is High
				*proxies = append(*proxies, proxy)
			} else {
				// Proxy is either transparent or anonymous
				*proxies = append(*proxies, proxy)
			}
		}
	}
}