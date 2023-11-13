#include <nlohmann/json.hpp>

class Collector {

  public:
    static std::vector<std::vector<char*>> getSources();
    static std::vector<std::string> getProxies(std::vector<std::vector<char*>> proxySources);

};