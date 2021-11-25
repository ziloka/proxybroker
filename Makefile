# Variables to control Makefile operation
CC = g++
CFLAGS = -Wall -Wextra -g 

# Targets needed to bring the executable up to date
# https://stackoverflow.com/Questions/335928/ld-cannot-find-an-existing-library
# https://stackoverflow.com/questions/6302282/how-do-i-link-libcurl-to-my-c-program-in-linux
# https://stackoverflow.com/questions/12187078/why-cant-gcc-find-my-static-library
ProxyBroker: build/main.o build/collector.o build/checker.o build/find.o libs/curl/lib/.libs/libcurl.a
	$(CC) $(CFLAGS) -o build/ProxyBroker build/main.o build/collector.o build/checker.o build/find.o -Llibs/curl/lib/.libs/libcurl.a -lcurl
	make clean

# https://stackoverflow.com/questions/558803/how-to-add-a-default-include-path-for-gcc-in-linux
build/main.o: src/main.cpp
	mkdir -p build
	$(CC) $(CFLAGS) -o build/main.o -I ./include -c src/main.cpp
	
build/collector.o: src/services/collector.cpp
	$(CC) $(CFLAGS) -o build/collector.o -I ./include -c src/services/collector.cpp

build/checker.o: src/services/checker.cpp
	$(CC) $(CFLAGS) -o build/checker.o -I ./include -c src/services/checker.cpp

build/find.o: src/cmds/find.cpp
	mkdir -p build/cmds
	$(CC) $(CFLAGS) -o build/find.o -I ./include -c src/cmds/find.cpp

# requires libtool, autoconf, libssl
# libcurl.a is located curl/lib/.libs after being built
libs/curl/lib/.libs/libcurl.a:
	cd libs/curl && \
	autoreconf -fi && \
	./configure --with-openssl && \
	make && \
	cd ../../

clean:
	rm -f build/*.o