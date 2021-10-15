package services

import (
	"encoding/json"
	"time"
	"net/http"
	"net/url"
)

type HttpResponse struct {
	// https://github.com/nsqio/nsq/issues/906#issuecomment-410726215
	Origin string `json:"origin"`
}

// https://golangbyexample.com/return-value-goroutine-go/
func Check(proxy string) (bool, error) {
	isOnline := false
	// https://stackoverflow.com/questions/14661511/setting-up-proxy-for-http-client
	// https://stackoverflow.com/a/14663620
	proxyUrl, _ := url.Parse(proxy)
	httpClient := &http.Client{ Timeout: 10 * time.Second, Transport: &http.Transport{Proxy: http.ProxyURL(proxyUrl)}}
	// https://stackoverflow.com/questions/17156371/how-to-get-json-response-from-http-get
	// https://stackoverflow.com/a/31129967
	res, httpgetErr := httpClient.Get("http://httpbin.org/ip?json")
	if httpgetErr != nil {
		return isOnline, httpgetErr
	}
	defer res.Body.Close()
	obj := &HttpResponse{}
	json.NewDecoder(res.Body).Decode(obj)
	return isOnline, nil
}