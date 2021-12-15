#include <nlohmann/json.hpp>

class Collector {

  public:
    static nlohmann::json getSources();
    static nlohmann::json getProxies(nlohmann::json proxieSources);

};