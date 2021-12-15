cmake_minimum_required(VERSION 3.0.0)
project(ProxyBroker VERSION 1.0.0)

add_subdirectory(libs/curl)

include(CTest)
enable_testing()

# Look for required libraries
set(CURL_LIBRARY "-lcurl") 
SET(CURL_INCLUDE_DIR "include/curl/include")

SET(requiredlibs)

FIND_PACKAGE(CURL)
IF(CURL_FOUND)
  include_directories(${CURL_INCLUDE_DIR})
  SET(requiredlibs ${requiredlibs} ${CURL_LIBRARIES} )
ELSE(CURL_FOUND)
  find_package(CURL REQUIRED) 
  include_directories(${CURL_INCLUDE_DIR})
  target_link_libraries(ProxyBroker ${CURL_LIBRARIES})
ENDIF(CURL_FOUND)

include_directories("include")

add_executable(
  ProxyBroker 
  src/main.cpp
  src/services/collector.cpp
  src/cmds/find.cpp
)

set(CPACK_PROJECT_NAME ${PROJECT_NAME})
set(CPACK_PROJECT_VERSION ${PROJECT_VERSION})
include(CPack)