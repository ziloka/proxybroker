package socks // import "h12.io/socks"

import (
	"fmt"
	"net"
)

// Constants to choose which version of SOCKS protocol to use.
const (
	SOCKS4 = iota
	SOCKS4A
	SOCKS5
)

// Dial returns the dial function to be used in http.Transport object.
// Argument proxyURI should be in the format: "socks5://user:password@127.0.0.1:1080?timeout=5s".
// The protocol could be socks5, socks4 and socks4a.
func Dial(proxyURI string) func(string, string) (net.Conn, error) {
	cfg, err := parse(proxyURI)
	if err != nil {
		return dialError(err)
	}
	return cfg.dialFunc()
}

// DialSocksProxy returns the dial function to be used in http.Transport object.
// Argument socksType should be one of SOCKS4, SOCKS4A and SOCKS5.
// Argument proxy should be in this format "127.0.0.1:1080".
func DialSocksProxy(socksType int, proxy string) func(string, string) (net.Conn, error) {
	return (&config{Proto: socksType, Host: proxy}).dialFunc()
}

func (c *config) dialFunc() func(string, string) (net.Conn, error) {
	switch c.Proto {
	case SOCKS5:
		return func(_, targetAddr string) (conn net.Conn, err error) {
			return c.dialSocks5(targetAddr)
		}
	case SOCKS4, SOCKS4A:
		return func(_, targetAddr string) (conn net.Conn, err error) {
			return c.dialSocks4(targetAddr)
		}
	}
	return dialError(fmt.Errorf("unknown SOCKS protocol %v", c.Proto))
}

func dialError(err error) func(string, string) (net.Conn, error) {
	return func(_, _ string) (net.Conn, error) {
		return nil, err
	}
}
