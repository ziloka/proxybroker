FROM gradle:7.2.0-jdk11 AS build

WORKDIR /usr/app/proxybroker
COPY . .

RUN gradle wrapper \
    && ./gradlew build

# https://github.com/GoogleContainerTools/distroless
# May switch to a distroless image
FROM adoptopenjdk/openjdk11:alpine-jre

WORKDIR /usr/app/
COPY --from=build /usr/app/proxybroker/build/libs/ProxyBroker.jar .
CMD ["java", "-jar", "ProxyBroker.jar"]