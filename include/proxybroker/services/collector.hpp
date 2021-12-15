#include <nlohmann/json.hpp>

struct proxySource {
  std::string proxyUrl;
  std::string type;
};

class Collector {

  public:
    static nlohmann::json getSources();
    static nlohmann::json getProxies();

};