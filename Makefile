# https://www.softwaretestinghelp.com/cpp-makefile-tutorial/

# Makefile for Writing Make Files Example

# *****************************************************
# Variables to control Makefile operation

CC = g++
CFLAGS = -Wall -Wextra -Werror -g

# ****************************************************
# Targets needed to bring the executable up to date
# https://stackoverflow.com/Questions/335928/ld-cannot-find-an-existing-library
# https://stackoverflow.com/questions/6302282/how-do-i-link-libcurl-to-my-c-program-in-linux
# https://stackoverflow.com/questions/12187078/why-cant-gcc-find-my-static-library
main: main.o ./libs/curl/lib/.libs/libcurl.a
	$(CC) $(CFLAGS) -o ProxyBroker main.o -Llibs/curl/lib/.libs/curl.a -lcurl

# https://stackoverflow.com/questions/558803/how-to-add-a-default-include-path-for-gcc-in-linux
# The main.o target can be written more simply
main.o: src/main.cpp 
	$(CC) $(CFLAGS) -I ./include -c src/main.cpp 

# requires libtool, autoconf, libssl
# libcurl.a is located curl/lib/.libs after being built
./libs/curl/lib/.libs/libcurl.a:
	cd libs/curl && \
	autoreconf -fi && \
	./configure --with-openssl && \
	make && \
	cd ../../

clean:
	-rm *.o *.a ProxyBroker
