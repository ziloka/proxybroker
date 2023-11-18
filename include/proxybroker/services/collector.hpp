#include <nlohmann/json.hpp>

class Collector {

  public:
    static std::vector<std::vector<std::string>> getSources();
    static void getProxies(std::vector<std::vector<std::string>> proxySources);

};