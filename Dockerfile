# http://blog.gilliard.lol/2018/01/10/Java-in-containers-jdk10.html
# https://jaxenter.com/nobody-puts-java-container-139373.html

FROM gradle:7.2.0-jdk11 as build

WORKDIR /usr/app
COPY . .
RUN ./gradlew build

FROM adoptopenjdk/openjdk11:alpine-jre

WORKDIR /usr/app/
COPY --from=build /usr/app/build/libs/ProxyChecker-*.jar .
CMD ["java", "-jar", "ProxyChcker-*.jar"]

