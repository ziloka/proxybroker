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

## Build from source

1. Clone the repository
```
git clone --recurse-submodules -j8 --single-branch --branch cpp --depth=1 https://github.com/Ziloka/ProxyBroker.git
```
*Note: if you already cloned the repository* and it states that
"The source directory"
...
"does not contain a CMakeLists.txt file."
Run this to download submodules
```
git submodule update --init --recursive --remote --no-fetch --depth=1
```

2. Prerequisites 

### Unix

#### Requirements
- [gcc](https://gcc.gnu.org/install/download.html)
- [cmake](https://cmake.org/download/)
- [make](https://www.gnu.org/software/make/)
- [OpenSSL Development files](https://www.openssl.org/source/)
- [Boost](https://www.boost.org/)
- *Optional: [ninja-build](https://ninja-build.org/)*

Ubuntu
```
sudo apt-get install libcurl4-openssl-dev libboost-all-dev
```

### Windows

These components are usually installed via the visual studio installer
Components that were last compiled with

#### Requirements
- MSVC v143 - VS 2022 C++ x64/x86 build tools
- Windows 11 SDK (10.0.22000.0)
- C++ Cmake tools for windows
- [Boost](https://www.boost.org/)
- *Optional: [ninja-build](https://ninja-build.org/)*

Add environment variables to path
*As well as ninja if you installed it*
```
C:\Program Files\Microsoft Visual Studio\2022\Community\Common7\IDE\CommonExtensions\Microsoft\CMake\CMake\bin\cmake.exe
```

Somehow add boost to path?

3. Compile

Compiling with ninja
```
sudo cmake -Bbuild -H. -GNinja .. \
&& cd build \
&& sudo cmake --build . --target ProxyBroker --config Release
```

Alternative:


## Motiviation
- Inspired by [ProxyBroker](https://github.com/constverum/ProxyBroker) (A more maintained version of that [project](https://github.com/bluet/proxybroker2))
- Web Service inspired by [go-shadowsocks2](https://github.com/shadowsocks/go-shadowsocks2)