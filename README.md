# ProxyBroker

Proxy is an open source tool that asynchronously finds public proxies from multiple sources and concurrently checks them.

<img src="https://img.shields.io/github/workflow/status/Ziloka/ProxyBroker/Golang%20Build?event=push" alt="Build Status">
<img src="https://gitpod.io/button/open-in-gitpod.svg" alt="Gitpod">

## Features

- Supported protocols: HTTP(S), SOCKS4/5.
- Proxies may be filtered by type, anonymity level, response time, country and status
- Work as a proxy server that distributes incoming requests to external proxies. With automatic proxy rotation.
- Automatically removes duplicate proxies
- Is asynchrous

## Requirements
- Operating system must be Windows, MacOS, or Linux

## Good things to know
- [Why I rewrote it in golang](https://www.baeldung.com/concurrency-principles-patterns#1-goroutines-in-go)
- Use Https instead of http, http might leak your ip address if you are using a high level proxy

## Building from source
- [Golang 1.17](https://golang.org/)
- [UPX 3.96](https://upx.github.io/)
- [Strip] 

Run
```
cd src && \
go build -o ProxyBroker main.go
```
To build for Linux, MacOS, and windows run
```
cd src && \
../build.bash github.com/Ziloka/ProxyBroker main.go
```

## Features to be implemented:
- Web server has api rest service to serve proxies, as well as let users surf the web something like [NodeUnblocker](https://github.com/nfriedly/nodeunblocker.com) [Npm Package](https://github.com/nfriedly/node-unblocker)

## Motiviation
- Inspired by [ProxyBroker](https://github.com/constverum/ProxyBroker) (A more maintained version of that [project](https://github.com/bluet/proxybroker2))
- Web Service inspired by [go-shadowsocks2](https://github.com/shadowsocks/go-shadowsocks2)
