cmake_minimum_required(VERSION 3.0.0)
project(ProxyBroker VERSION 0.1.0)

add_subdirectory(libs/curl)

include(CTest)
enable_testing()

# https://stackoverflow.com/questions/26704629/setting-up-curl-library-path-in-cmake
# https://stackoverflow.com/a/45232173
set(CURL_LIBRARY "-lcurl")
set(CURL_INCLUDE_DIR "libs/curl/include")
find_package(CURL REQUIRED)

include_directories("./include")

add_executable(ProxyBroker src/main.cpp src/services/collector.cpp)

include_directories(${CURL_INCLUDE_DIR})
target_link_libraries(ProxyBroker ${CURL_LIBRARIES})

set(CPACK_PROJECT_NAME ${PROJECT_NAME})
set(CPACK_PROJECT_VERSION ${PROJECT_VERSION})
include(CPack)
