cmake_minimum_required(VERSION 3.0.0)
project(ProxyBroker VERSION 0.1.0)

include(CTest)
enable_testing()

add_executable(ProxyBroker src/main.cpp)

# https://stackoverflow.com/questions/26704629/setting-up-curl-library-path-in-cmake
# https://stackoverflow.com/a/45232173
set(CURL_LIBRARY "libs/curl")
set(CURL_INCLUDE_DIR "libs/curl/include")
find_package(CURL REQUIRED)
include_directories(${CURL_INCLUDE_DIR})
target_link_libraries(ProxyBroker ${CURL_LIBRARIES})

set(CPACK_PROJECT_NAME ${PROJECT_NAME})
set(CPACK_PROJECT_VERSION ${PROJECT_VERSION})
include(CPack)
