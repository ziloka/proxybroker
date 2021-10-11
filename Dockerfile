# https://stackoverflow.com/questions/53669151/java-11-application-as-lightweight-docker-image
FROM alpine:3.14.2 as build

WORKDIR /usr/app/proxybroker
COPY app/build.gradle .

RUN apk add --no-cache openjdk11 gradle \
    && gradle wrapper \
    && ./gradlew -Pagent run \
    && ./gradlew -Pagent nativeBuild
RUN rm -rf /var/cache/apk/*

COPY . .

# https://github.com/GoogleContainerTools/distroless
# May switch to a distroless image
FROM adoptopenjdk/openjdk11:alpine-jre
COPY --from=build /usr/app/proxybroker/app/build/libs/ProxyBroker.jar .
ENTRYPOINT ["java", "-jar", "ProxyBroker.jar"]
