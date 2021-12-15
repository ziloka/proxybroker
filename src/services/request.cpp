#include <iostream>
#include <string>
#include <proxybroker/services/checker.hpp>

#define CURL_STATICLIB
#include "curl/curl.h"

static size_t my_write(void* buffer, size_t size, size_t nmemb, void* param)
{
  std::string& text  = *static_cast<std::string*>(param);
  size_t totalsize = size * nmemb;
  text.append(static_cast<char*>(buffer), totalsize);
  return totalsize;
}

string check(std::string){

  std::string result;
    CURL* curl;
    CURLcode res;

    curl_global_init(CURL_GLOBAL_DEFAULT);

    curl = curl_easy_init();
    if (curl) {
      curl_easy_setopt(curl, CURLOPT_URL, "https://api.ipify.org");
      curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, my_write);
      curl_easy_setopt(curl, CURLOPT_WRITEDATA, &result);

      curl_easy_setopt(curl, CURLOPT_VERBOSE, 1L);

      res = curl_easy_perform(curl);

      curl_easy_cleanup(curl);

      if(CURLE_OK != res) {
        std::cerr << "CURL error: " << res << std::endl;
      }
    }
    curl_global_cleanup();

    std::cout << result << "\n\n";

}