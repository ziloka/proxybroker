package services

import (
	"crypto/tls"
	"encoding/json"
	"github.com/Ziloka/ProxyBroker/utils"
	"io"
	"net"
	"net/http"
	"net/url"
	"strings"
	"time"
)

type HttpResponse struct {
	Origin string
}

// https://golangbyexample.com/return-value-goroutine-go/
func Check(proxies *[]string, myRemoteAddr string, proxy string) {

  // Do not push duplicates back into the array
	if !utils.Contains(*proxies, proxy) {

		// https://stackoverflow.com/questions/14661511/setting-up-proxy-for-http-client
		// https://stackoverflow.com/a/14663620
		proxyUrl, _ := url.Parse("http://" + proxy)
		// https://medium.com/@nate510/don-t-use-go-s-default-http-client-4804cb19f779
		httpClient := &http.Client{
			Timeout: 10 * time.Second,
			Transport: &http.Transport{
				MaxIdleConns:       100,
				IdleConnTimeout:    10 * time.Second,
				DisableCompression: true,
				Proxy:              http.ProxyURL(proxyUrl),
				TLSClientConfig:    &tls.Config{InsecureSkipVerify: true},
				Dial: (&net.Dialer{
					Timeout: 10 * time.Second,
				}).Dial,
				DialContext: (&net.Dialer{
					Timeout:   10 * time.Second,
					KeepAlive: 10 * time.Second,
				}).DialContext,
				ForceAttemptHTTP2:   true,
				TLSHandshakeTimeout: 10 * time.Second,
			},
			Jar: nil,
			CheckRedirect: func(req *http.Request, via []*http.Request) error {
				return http.ErrUseLastResponse
			},
		}

		// https://stackoverflow.com/questions/17156371/how-to-get-json-response-from-http-get
		res, httpGetErr := httpClient.Get("https://httpbin.org/ip?json")
		if httpGetErr != nil {
			return
		}
		defer res.Body.Close()
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
					// Proxy is High
					*proxies = append(*proxies, proxy)
				} else {
					// Proxy is either transparent or anonymous
					// *proxies = append(*proxies, proxy)
				}
			}
		}

	}

}