# Testing cURL
# MAKEFILE

# C++ Compiler (Default: g++)
CXX = g++
CFLAGS = -Wall -Werror

# Librarys
INCLUDE = -I/usr/local/include
LDFLAGS = -L/usr/local/lib 
LDLIBS = -lcurl

# Details
SOURCES = src/main.cpp
OUT = test

.PHONY: all

all: build

$(OUT): $(patsubst %.cpp,%.o,$(SOURCES))