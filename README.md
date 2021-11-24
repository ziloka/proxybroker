# ProxyBroker

Proxy is an open source tool that asynchronously finds public proxies from multiple sources and concurrently checks them.

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
- [gcc](https://gcc.gnu.org/install/download.html)
- [cmake](https://cmake.org/download/)
- [make](https://www.gnu.org/software/make/)
Run
```
mkdir build && \
cd build && \
cmake ../
```



## Motiviation
- Inspired by [ProxyBroker](https://github.com/constverum/ProxyBroker) (A more maintained version of that [project](https://github.com/bluet/proxybroker2))
- Web Service inspired by [go-shadowsocks2](https://github.com/shadowsocks/go-shadowsocks2)