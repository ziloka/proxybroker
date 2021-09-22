FROM alpine:3.14.2 as build

RUN apk add --no-cache gradle gcc zlib-dev gcompat

ADD https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-21.2.0/graalvm-ce-java11-linux-amd64-21.2.0.tar.gz graalvm.tar.gz

# create user gradle
RUN adduser -D -s /bin/sh gradle \
    && mkdir -p /usr/app/proxybroker/.gradle \
    && chmod -R 777 /usr/app/proxybroker/.gradle

# Install Java
RUN mkdir -p /usr/java \
 && tar -xzf graalvm.tar.gz -C /usr/java \
 && rm graalvm.tar.gz

# Add java to environment variables
ENV PATH /usr/java/graalvm-ce-java11-21.2.0/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
# LD_LIBRARY_PATH is default library path used for available dynamic and shared libraries
# Add shared library to environment variables to prevent libjvm.so error
ENV LD_LIBRARY_PATH /usr/java/graalvm-ce-java11-21.2.0/lib/server

WORKDIR /usr/app/proxybroker
COPY . .

USER gradle

ENV GRADLE_USER_HOME /home/gradle/.gradle

RUN export GRADLE_USER_HOME=/home/gradle/.gradle \
    && gradle wrapper --stacktrace \
    && ./gradlew nativeImage

FROM alpine:3.14.2
WORKDIR /graalvm-demo
COPY --from=build /usr/app/proxybroker .
CMD ./app