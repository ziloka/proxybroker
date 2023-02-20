#include <iostream>
#include <CLI11/CLI11.hpp>
#include <nlohmann/json.hpp>
#include <proxybroker/services/collector.hpp>
#include <proxybroker/cmds/find.hpp>
#include <boost/lockfree/queue.hpp>

// https://stackoverflow.com/a/64984673
boost::lockfree::queue<char *> queue(128);

void find(SubcommandFindOptions const& opt){
  nlohmann::json proxies = Collector::getProxies(Collector::getSources());
  for (std::string proxies : proxies) {
    std::cout << proxies << std::endl;
  }
}