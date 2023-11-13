#include <CLI/CLI.hpp>

struct SubcommandFindOptions {
  bool verbose;
  std::string filename;
};

void find(SubcommandFindOptions const &opt);