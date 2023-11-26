#include <iostream>
#include <vector>
#include <regex>
#include <nlohmann/json.hpp>
#include <proxybroker/services/collector.hpp>
#include "curl/curl.h"

std::vector<std::vector<std::string>> Collector::getSources() {

  // vector of proxySources
  // first index is url
  // second index is protocol

  std::vector<std::vector<std::string>> array = {
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

  return array;

};

// https://everything.curl.dev/libcurl/drive/multi-socket
// use rxcpp to implement additional stuff to go back to find.cpp file
// https://kirkshoop.github.io/introductionToRxcpp/#68
static size_t my_write(void* buffer, size_t size, size_t nmemb, void* param)
{
  std::string& text = *static_cast<std::string*>(param);
  size_t totalsize = size * nmemb;
  text.append(static_cast<char*>(buffer), totalsize);

  std::vector<std::string> proxies;
  std::regex proxyRegex("\\d+\\.\\d+\\.\\d+\\.\\d+:\\d+", std::regex_constants::ECMAScript | std::regex_constants::icase);
  auto words_begin = std::sregex_iterator(text.begin(), text.end(), proxyRegex);
  auto words_end = std::sregex_iterator();
  for(std::sregex_iterator i = words_begin; i != words_end; ++i) {
    std::smatch match = *i;
    std::string match_str = match.str();
    proxies.push_back(match_str);
  }

  std::cout << "Found " << proxies.size() << " proxies" << std::endl;

  return totalsize;
}

// https://curl.se/libcurl/c/curl_multi_poll.html
// https://curl.se/libcurl/c/multi-app.html
void Collector::getProxies(std::vector<std::vector<std::string>> proxySources) {
  curl_global_init(CURL_GLOBAL_DEFAULT);
  CURLM* multi_handle = curl_multi_init();

  // https://json.nlohmann.me/features/iterators/
  for(int i = 0; i < proxySources.size(); i++) {
    std::string result;

    std::string url = proxySources[i][0];
    std::string protocol = proxySources[i][1];
    
    CURL* easy_handle = curl_easy_init();
    if (easy_handle) {
      curl_easy_setopt(easy_handle, CURLOPT_URL, url.c_str());
      curl_easy_setopt(easy_handle, CURLOPT_WRITEFUNCTION, my_write);
      curl_easy_setopt(easy_handle, CURLOPT_WRITEDATA, &result);
      curl_multi_add_handle(multi_handle, easy_handle);
    }
  }

  int still_running = 1;  
  while(still_running) {
    CURLMcode mc = curl_multi_perform(multi_handle, &still_running);
 
    if(still_running)
      /* wait for activity, timeout or "nothing" */
      mc = curl_multi_poll(multi_handle, NULL, 0, 1000, NULL);
 
    if(mc)
      break;
  }

  // CURLMsg *msg;
  // int msgs_left;
  // while((msg = curl_multi_info_read(multi_handle, &msgs_left))) {
  // if(msg->msg == CURLMSG_DONE) {
  //   std::cout << "result: " << msg->data.result << std::endl;
  //   }
  // }
}