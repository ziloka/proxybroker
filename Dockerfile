FROM alpine:3.14.2 as build

RUN apk add --no-cache openjdk11 gradle

WORKDIR /usr/app/proxybroker
COPY . .

RUN gradle wrapper \
    && ./gradlew build

# https://github.com/GoogleContainerTools/distroless
# May switch to a distroless image
FROM alpine:3.14.2
COPY --from=build /usr/app/proxybroker/build/libs/ProxyBroker.jar .
RUN apk add --no-cache openjdk11
ENTRYPOINT ["java", "-jar", "ProxyBroker.jar"]