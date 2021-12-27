#include <iostream>
#include <vector>
#include <regex>
#include <nlohmann/json.hpp>
#include <proxybroker/services/collector.hpp>
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

static size_t my_write(void* buffer, size_t size, size_t nmemb, void* param)
{
  std::string& text = *static_cast<std::string*>(param);
  size_t totalsize = size * nmemb;
  text.append(static_cast<char*>(buffer), totalsize);
  return totalsize;
}

std::vector<std::string> Collector::getProxies(nlohmann::json proxySources){
  std::vector<std::string> proxies;
  // https://json.nlohmann.me/features/iterators/
  for(auto& [key, val] : proxySources.items()){
    std::string result;
    CURL* curl;
    CURLcode res;
    curl_global_init(CURL_GLOBAL_DEFAULT);
    curl = curl_easy_init();
    if (curl) {
      curl_easy_setopt(curl, CURLOPT_URL, key.c_str());
      curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, my_write);
      curl_easy_setopt(curl, CURLOPT_WRITEDATA, &result);
      // curl_easy_setopt(curl, CURLOPT_VERBOSE, 1L);
      res = curl_easy_perform(curl);
      curl_easy_cleanup(curl);
      if (CURLE_OK != res) {
        std::cerr << "CURL error: " << res << '\n';
      }
    }
    curl_global_cleanup();
    
    // std::cout << result << std::endl;

    std::regex proxyRegex("\\d+\\.\\d+\\.\\d+\\.\\d+:\\d+", std::regex_constants::ECMAScript | std::regex_constants::icase);
    auto words_begin = std::sregex_iterator(result.begin(), result.end(), proxyRegex);
    auto words_end = std::sregex_iterator();
    for(std::sregex_iterator i = words_begin; i != words_end; ++i) {
      std::smatch match = *i;
      std::string match_str = match.str();
      proxies.push_back(match_str);
    }
  }

  return proxies;
};