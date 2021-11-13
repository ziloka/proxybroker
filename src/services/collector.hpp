#include <curlpp/cURLpp.hpp>
#include <curlpp/Options.hpp>

// https://stackoverflow.com/questions/1011339/how-do-you-make-a-http-request-with-c
// https://stackoverflow.com/a/1112084

namespace collector {
  enum ProxyType {http, https, socks4, socks5, all}
  
}