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

Install dependencies
```sh
.\vcpkg\bootstrap-vcpkg.bat
# .\vcpkg\vcpkg integrate install
# .\vcpkg\vcpkg add port boost-lockfree curl
.\vcpkg\vcpkg install
# .\vcpkg\vcpkg install --target-triplet x64-windows boost-lockfree curl
```

build packages
```sh
cmake --preset=default
cmake --build build
.\build\Debug\ProxyBroker.exe --help

# mkdir build
# cd build
# cmake .. -G "Unix Makefiles" 
# make
# make install prefix=artifacts

# cmake ..
# cmake --build .
# cmake --install . --prefix artifacts
```
- From [MSFT vcpkg with cmake tutorial](https://learn.microsoft.com/en-us/vcpkg/get_started/get-started?pivots=shell-cmd)

*If you are using powershell, and the application does not provide any output, [You may not be able to see the missing libraries error](https://github.com/PowerShell/PowerShell/issues/16468) when executed the application*
- On [Command Prompt it is fixed]((https://github.com/microsoft/terminal/issues/9788)

## Motiviation
- Inspired by [ProxyBroker](https://github.com/constverum/ProxyBroker) (A more maintained version of that [project](https://github.com/bluet/proxybroker2))
- Web Service inspired by [go-shadowsocks2](https://github.com/shadowsocks/go-shadowsocks2)