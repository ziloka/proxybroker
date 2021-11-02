package services

import (
	"encoding/json"
	"io"
	"net"
	"net/http"
	"net/url"
	"strings"
	"time"
	"github.com/Ziloka/ProxyBroker/structs"
	"github.com/Ziloka/ProxyBroker/utils"
)

type HttpResponse struct {
	Origin string
}

// https://golangbyexample.com/return-value-goroutine-go/
func Check(proxies chan structs.Proxy, myRemoteAddr string, proxy string) {
	// https://stackoverflow.com/questions/14661511/setting-up-proxy-for-http-client
	// https://stackoverflow.com/a/14663620
	proxyUrl, _ := url.Parse("http://" + proxy)
	tp := newTransport(proxyUrl)
	// https://medium.com/@nate510/don-t-use-go-s-default-http-client-4804cb19f779
	httpClient := &http.Client{
		Transport: tp,
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
				// fmt.Printf("D: %v, RD: %v, CD: %v\n", tp.Duration(), tp.ReqDuration(), tp.ConnDuration())
				// Proxy is High
				proxyStruct := structs.Proxy{
					Proxy: proxy,
					AvgRespTime: tp.Duration(),
				}
				proxies <- proxyStruct
			} else {
				// Proxy is either transparent or anonymous
				// *proxies = append(*proxies, proxy)
			}
		}
	}
}

// https://stackoverflow.com/questions/30526946/time-http-response-in-go
// https://github.com/skyec/go-instrumented-roundtripper/blob/master/main.go
// go run file.go http://google.com
type customTransport struct {
	rtp       http.RoundTripper
	dialer    *net.Dialer
	connStart time.Time
	connEnd   time.Time
	reqStart  time.Time
	reqEnd    time.Time
}

func newTransport(proxy *url.URL) *customTransport {

	tr := &customTransport{
		dialer: &net.Dialer{
			Timeout:   10 * time.Second,
			KeepAlive: 10 * time.Second,
		},
	}
	tr.rtp = &http.Transport{
		Proxy:               http.ProxyURL(proxy),
		Dial:                tr.dial,
		TLSHandshakeTimeout: 10 * time.Second,
	}
	return tr
}

func (tr *customTransport) RoundTrip(r *http.Request) (*http.Response, error) {
	tr.reqStart = time.Now()
	resp, err := tr.rtp.RoundTrip(r)
	tr.reqEnd = time.Now()
	return resp, err
}

func (tr *customTransport) dial(network, addr string) (net.Conn, error) {
	tr.connStart = time.Now()
	cn, err := tr.dialer.Dial(network, addr)
	tr.connEnd = time.Now()
	return cn, err
}

func (tr *customTransport) ReqDuration() time.Duration {
	return tr.Duration() - tr.ConnDuration()
}

func (tr *customTransport) ConnDuration() time.Duration {
	return tr.connEnd.Sub(tr.connStart)
}

func (tr *customTransport) Duration() time.Duration {
	return tr.reqEnd.Sub(tr.reqStart)
}