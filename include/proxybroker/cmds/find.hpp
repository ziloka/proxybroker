#include <CLI11/CLI11.hpp>

struct SubcommandFindOptions {
  bool verbose;
  std::string filename;
};

void find(SubcommandFindOptions const &opt);