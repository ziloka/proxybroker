#include <iostream>
#include <vector>
#include <nlohmann/json.hpp>
#include <proxybroker/services/collector.hpp>

#define CURL_STATICLIB
#include "curl/curl.h"

nlohmann::json Collector::getSources() {

  // vector of proxySources
  // first index is url
  // second index is protocol
  return {
    {"https://api.proxyscrape.com/v2/?request=getproxies&protocol=http&timeout=10000&country=all&ssl=all&anonymity=all", "http"},
    {"https://raw.githubusercontent.com/clarketm/proxy-list/master/proxy-list-raw.txt", "http"},
    {"https://www.freeproxylists.net/", "http"},
    {"https://www.proxyscan.io/download?type=http", "http"},
    {"https://www.proxy-list.download/api/v1/get?type=http", "http"},
    {"https://raw.githubusercontent.com/TheSpeedX/PROXY-List/master/http.txt", "http"},
    {"https://proxylist.geonode.com/api/proxy-list?limit=200&page=1&sort_by=lastChecked&sort_type=desc&protocols=http", "http"},
    {"https://www.proxy-list.download/HTTPS", "https"},
    {"https://www.proxyscan.io/download?type=https", "https"},
    {"https://www.proxy-list.download/api/v1/get?type=https", "https"},
    {"https://proxylist.geonode.com/api/proxy-list?limit=200&page=1&sort_by=lastChecked&sort_type=desc&protocols=https", "https"},
    {"https://api.proxyscrape.com/v2/?request=getproxies&protocol=socks4&timeout=10000&country=all", "socks4"},
    {"https://www.socks-proxy.net/", "socks4"},
    {"https://www.proxyscan.io/download?type=socks4", "socks4"},
    {"https://www.proxy-list.download/api/v1/get?type=socks4", "socks4"},
    {"https://raw.githubusercontent.com/TheSpeedX/PROXY-List/master/socks4.txt", "socks4"},
    {"https://proxylist.geonode.com/api/proxy-list?limit=200&page=1&sort_by=lastChecked&sort_type=desc&protocols=socks4", "socks4"},
    {"https://api.proxyscrape.com/v2/?request=getproxies&protocol=socks5&timeout=10000&country=all", "socks5"},
    {"https://www.proxy-list.download/SOCKS5", "socks5"},
    {"https://www.proxyscan.io/download?type=socks5", "socks5"},
    {"https://www.proxy-list.download/api/v1/get?type=socks5", "socks5"},
    {"https://raw.githubusercontent.com/TheSpeedX/PROXY-List/master/socks5.txt", "socks5"},
    {"https://proxylist.geonode.com/api/proxy-list?limit=200&page=1&sort_by=lastChecked&sort_type=desc&protocols=socks5", "socks5"}
  };

};

nlohmann::json Collector::getProxies(nlohmann::json proxySources){
  
};