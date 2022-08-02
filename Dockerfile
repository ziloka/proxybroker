# GCC support can be specified at major, minor, or micro version
# (e.g. 8, 8.2 or 8.2.0).
# See https://hub.docker.com/r/library/gcc/ for all supported GCC
# tags from Docker Hub.
# See https://docs.docker.com/samples/library/gcc/ for more on how to use this image
FROM alpine:3.14.2

# These commands copy your files into the specified directory in the image
# and set that as the working location
COPY . /usr/src/ProxyBroker
WORKDIR /usr/src/ProxyBroker

# This command compiles your app using GCC, adjust for your source code
RUN	apk add gcc g++ musl-dev make curl-dev && \
  g++ -I ./include -o main src/main.cpp -lcurl

# This command runs your application, comment out this line to compile only
CMD ["./main"]

LABEL Name=proxybroker Version=0.0.1
