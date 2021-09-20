# http://blog.gilliard.lol/2018/01/10/Java-in-containers-jdk10.html
# https://jaxenter.com/nobody-puts-java-container-139373.html

FROM frolvlad/alpine-gcc as build

WORKDIR /usr/app
COPY . .

ADD "https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-21.2.0/graalvm-ce-java11-linux-amd64-21.2.0.tar.gz" /usr/java/graalvm-ce-java11-linux-amd64-21.2.0.tar.gz
RUN apk add gradle \
   && tar xvf /usr/java/graalvm-ce-java11-linux-amd64-21.2.0.tar.gz \
   && export PATH=/usr/java/graalvm-ce-java11-21.2.0/bin:$PATH \
   && export JAVA_HOME=/usr/java/graalvm-ce-java11-21.2.0/bin

# Build Executable
RUN ./gradlew nativeBuild

FROM springci/graalvm-ce:stable-java11-0.11.x

WORKDIR /usr/app/
COPY --from=build /usr/app/ProxyChecker.exe .
CMD ["java", "-jar", "/usr/app/ProxyChecker.jar"]
