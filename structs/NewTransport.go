package structs

import (
	"h12.io/socks"
	proxyLib "golang.org/x/net/proxy"
	"net"
	"net/http"
	"net/url"
	"time"
)

// https://stackoverflow.com/questions/30526946/time-http-response-in-go
// https://stackoverflow.com/a/30547965
// https://github.com/skyec/go-instrumented-roundtripper/blob/master/main.go
// go run file.go http://google.com
type CustomTransport struct {
	rtp       http.RoundTripper
	dialer    *net.Dialer
	connStart time.Time
	connEnd   time.Time
	reqStart  time.Time
	reqEnd    time.Time
}

func NewTransport(protocol string, proxy string) *CustomTransport {

	tr := &CustomTransport{
		dialer: &net.Dialer{
			Timeout:   10 * time.Second,
			KeepAlive: 10 * time.Second,
		},
	}

	if protocol == "http" {
		proxyUrl, _ := url.Parse("http://" + proxy)
		tr.rtp = &http.Transport{
			Proxy:               http.ProxyURL(proxyUrl),
			Dial:                tr.dial,
			TLSHandshakeTimeout: 10 * time.Second,
		}
	} else if protocol == "socks4" {
		dialSocksProxy := socks.DialSocksProxy(socks.SOCKS4, proxy)
		tr.rtp = &http.Transport{
			Dial: dialSocksProxy,
		}
	} else if protocol == "socks5" {
		dialer, _ := proxyLib.SOCKS5("tcp", proxy, nil, proxyLib.Direct)
		tr.rtp = &http.Transport{
			Dial: dialer.Dial,
		}
	}

	return tr
}

func (tr *CustomTransport) RoundTrip(r *http.Request) (*http.Response, error) {
	tr.reqStart = time.Now()
	resp, err := tr.rtp.RoundTrip(r)
	tr.reqEnd = time.Now()
	return resp, err
}

func (tr *CustomTransport) dial(network, addr string) (c net.Conn, err error) {
	tr.connStart = time.Now()
	cn, err := tr.dialer.Dial(network, addr)
	tr.connEnd = time.Now()
	return cn, err
}

func (tr *CustomTransport) ReqDuration() time.Duration {
	return tr.Duration() - tr.ConnDuration()
}

func (tr *CustomTransport) ConnDuration() time.Duration {
	return tr.connEnd.Sub(tr.connStart)
}

func (tr *CustomTransport) Duration() time.Duration {
	return tr.reqEnd.Sub(tr.reqStart)
}
