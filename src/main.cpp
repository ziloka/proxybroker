#include <iostream>
#include <CLI/CLI.hpp>
#include <proxybroker/cmds/find.hpp>

void setup_subcommands(CLI::App &app){

  // find command
  auto opt = std::make_shared<SubcommandFindOptions>();
  CLI::App* sub = app.add_subcommand("find", "This is a subcommand");
  sub->add_flag("-v,--verbose", opt->verbose);

  // std::string filename;
  sub->add_option("-f,--file", opt->filename, "Write proxies to this file");
  sub->callback([opt]() {
    find(*opt);
  });
  // app->formatter->

}

int main(int argc, char **argv) {
    CLI::App app{"ProxyBroker is a open source tool that asynchrously finds public proxies from multiple sources and concurrencly checks them."};

    // app.require_subcommand(0, 1);
    

    setup_subcommands(app);

    CLI11_PARSE(app, argc, argv);

    return 0;
}