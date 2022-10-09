# ProxyBroker

Proxy is an open source tool that asynchronously finds public proxies from multiple sources and concurrently checks them.

## Features

- Supported protocols: HTTP(S), SOCKS4/5.
- Proxies may be filtered by type, anonymity level, response time, country and status
- Work as a proxy server that distributes incoming requests to external proxies. With automatic proxy rotation.
- Automatically removes duplicate proxies
- Is asynchronous 

## Requirements
- Operating system must be Windows, MacOS, or Linux

## Good things to know
- [Why I rewrote it in golang](https://www.baeldung.com/concurrency-principles-patterns#1-goroutines-in-go)
- Use Https instead of http, http might leak your ip address if you are using a high level proxy

## Building from source
- [git](https://gcc.gnu.org/install/download.html)
- [rust](https://cmake.org/download/)
Run

```
git clone https://github.com/Ziloka/ProxyBroker.git && \
../build.sh
```

The executable can be found in target/release

# Coding Notes

- Rust program was significantly harder to write compared to golang, or java.
- ~~Have openssl v1 on the system (or with openssl v3.0.5+, otherwise don't make a [github issue on performance if you compile with openssl v3](https://github.com/sysown/proxysql/pull/3937))~~
- ~~The runtime environment doesn't matter as long as it doesn't use musl and uses glibc~~
- ~~https://github.com/openssl/openssl/issues/17064#issuecomment-973444945~~
- ~~https://developers.redhat.com/articles/2021/12/17/why-glibc-234-removed-libpthread~~
- statically link rustls instead of dynamically linking openssl library to prevent performance differences on different platforms