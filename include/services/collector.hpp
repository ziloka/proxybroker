#include <nlohmann/json.hpp>

struct proxySource {
  std::string proxyUrl
  std::string type
}

class Collector {

  public:
    nlohmann::json getSources();

};