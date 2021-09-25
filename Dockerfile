FROM alpine:3.14.2 as build

WORKDIR /usr/app/proxybroker
COPY app/build.gradle .

RUN apk add --no-cache openjdk11 gradle \
    && gradle wrapper \
    && ./gradlew build
RUN rm -rf /var/cache/apk/*

COPY . .

# https://github.com/GoogleContainerTools/distroless
# May switch to a distroless image
FROM adoptopenjdk/openjdk11:alpine-jre
COPY --from=build /usr/app/proxybroker/app/build/libs/ProxyBroker.jar .
ENTRYPOINT ["java", "-jar", "ProxyBroker.jar"]