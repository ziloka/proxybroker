FROM gradle:7.2.0-jdk11 as build

RUN apt-get update \
    && apt-get install -y gcc

ADD https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-21.2.0/graalvm-ce-java11-linux-amd64-21.2.0.tar.gz graalvm.tar.gz

RUN mkdir -p /usr/java \
    && tar -xzf graalvm.tar.gz -C /usr/java \
    && rm graalvm.tar.gz

ENV PATH /usr/java/graalvm-ce-java11-21.2.0/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
# LD_LIBRARY_PATH is default library path used for available dynamic and shared libraries
# Add shared library to environment variables to prevent libjvm.so error
ENV LD_LIBRARY_PATH /usr/java/graalvm-ce-java11-21.2.0/lib/server

WORKDIR /usr/app/proxybroker
COPY . .

RUN gu install native-image \
  && gradle wrapper \
  && ./gradlew build \
  && native-image --no-server -J-Xmx4G -jar build/libs/ProxyBroker.jar

FROM alpine:3.14.2
WORKDIR /graalvm-demo
COPY --from=build /usr/app/ProxyBroker .
CMD ["proxybroker"]