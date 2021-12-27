#include <nlohmann/json.hpp>

class Collector {

  public:
    static nlohmann::json getSources();
    static std::vector<std::string> getProxies(nlohmann::json proxieSources);

};