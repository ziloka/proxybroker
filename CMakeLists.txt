cmake_minimum_required(VERSION 3.0.0)
project(ProxyBroker VERSION 1.0.0)
set(CMAKE_CXX_STANDARD 17)

add_subdirectory(libs/curl)

include(CTest)
enable_testing()

include_directories("include")

add_executable(
  ProxyBroker 
  src/main.cpp
  src/services/collector.cpp
  src/cmds/find.cpp
)

# https://stackoverflow.com/questions/6141147/how-do-i-include-a-path-to-libraries-in-g
# https://stackoverflow.com/a/6141166
set(CURL_LIBRARY "-lcurl") 
find_package(CURL REQUIRED)
include_directories(${CURL_INCLUDE_DIR})
target_link_libraries(ProxyBroker ${CURL_LIBRARIES})

set(CPACK_PROJECT_NAME ${PROJECT_NAME})
set(CPACK_PROJECT_VERSION ${PROJECT_VERSION})
include(CPack)