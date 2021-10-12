# https://stackoverflow.com/questions/53669151/java-11-application-as-lightweight-docker-image
# https://levelup.gitconnected.com/java-developing-smaller-docker-images-with-jdeps-and-jlink-d4278718c550
FROM alpine:3.14.2 as build

WORKDIR /usr/app/proxybroker

COPY . .

RUN apk add --no-cache openjdk11 gradle \
    && gradle wrapper \
    && ./gradlew run

# https://github.com/GoogleContainerTools/distroless
# May switch to a distroless image
FROM adoptopenjdk/openjdk11:alpine-jre
COPY --from=build /usr/app/proxybroker/app/build/libs/ProxyBroker.jar .
ENTRYPOINT ["java", "-jar", "ProxyBroker.jar"]
