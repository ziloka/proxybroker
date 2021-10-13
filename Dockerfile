# https://stackoverflow.com/questions/53669151/java-11-application-as-lightweight-docker-image
# https://levelup.gitconnected.com/java-developing-smaller-docker-images-with-jdeps-and-jlink-d4278718c550
FROM gradle:7.2.0-jdk11 as build

WORKDIR /usr/app/proxybroker
COPY . .

RUN gradle wrapper \
    && ./gradlew build \
    && ./gradlew copyDependencies

FROM openjdk:11.0-jre-slim
#FROM bellsoft/liberica-openjdk-alpine-musl:11.0.12-7

COPY --from=build /usr/app/proxybroker/app/build/libs/ProxyBroker.jar .
ENTRYPOINT ["java", "-jar", "ProxyBroker.jar"]

