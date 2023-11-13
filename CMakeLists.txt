cmake_minimum_required(VERSION 3.21.0)

option(X_VCPKG_APPLOCAL_DEPS_INSTALL ON)
set(CMAKE_TOOLCHAIN_FILE ${CMAKE_CURRENT_SOURCE_DIR}/vcpkg/scripts/buildsystems/vcpkg.cmake CACHE STRING "Vcpkg toolchain file")

project(ProxyBroker VERSION 1.0.0)
set(CMAKE_CXX_STANDARD 17)

include(CTest)
enable_testing()

include_directories("include")

find_package(CURL REQUIRED)

find_path(BoostLockfree_INCLUDE_DIR boost/lockfree)
if(BoostLockfree_INCLUDE_DIR STREQUAL "BoostLockfree_INCLUDE_DIR-NOTFOUND")
  message(FATAL_ERROR "Could not find Boost Lockfree header Library.")
endif()
include_directories(${BoostLockfree_INCLUDE_DIR})

find_package(CLI11 CONFIG REQUIRED)

find_package(nlohmann_json CONFIG REQUIRED)

add_executable(
  ProxyBroker 
  src/main.cpp
  src/services/collector.cpp
  src/cmds/find.cpp
)

target_link_libraries(ProxyBroker PRIVATE CURL::libcurl nlohmann_json::nlohmann_json CLI11::CLI11)

# add_custom_command(
#         TARGET ProxyBroker POST_BUILD
#         COMMAND ${CMAKE_COMMAND} -E copy
#                 $<TARGET_RUNTIME_DLLS:ProxyBroker>
#                 ${CMAKE_CURRENT_BINARY_DIR})

install(
  TARGETS ProxyBroker
  # RUNTIME DESTINATION bin
  # LIBRARY DESTINATION lib
  # ARCHIVE DESTINATION lib
)

set(CPACK_PROJECT_NAME ${PROJECT_NAME})
set(CPACK_PROJECT_VERSION ${PROJECT_VERSION})
include(CPack)