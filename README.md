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
- Rust does not have [green threads](https://users.rust-lang.org/t/green-threads-vs-async/42159/4) making this depend on operating system threads. This means if you have a good cpu that supports lots of threads, this program will run faster than that compared to running it on a bad cpu that doesn't support as many threads.
