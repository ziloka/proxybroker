# https://stackoverflow.com/questions/58593661/slow-gradle-build-in-docker-caching-gradle-build
# https://stackoverflow.com/a/59022743
FROM alpine:3.14.2 as cache

RUN apk add --no-cache gradle && \
  mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME /home/gradle/cache_home
COPY app/build.gradle /home/gradle/java-code/
WORKDIR /home/gradle/java-code

RUN gradle clean build -i --stacktrace

# https://stackoverflow.com/questions/53669151/java-11-application-as-lightweight-docker-image
# https://levelup.gitconnected.com/java-developing-smaller-docker-images-with-jdeps-and-jlink-d4278718c550
FROM alpine:3.14.2 as build

WORKDIR /usr/app/proxybroker
COPY . .
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
ENV JAVA_MINIMAL="/opt/java-minimal"
RUN apk add --no-cache openjdk11-jdk openjdk11-jmods gradle

RUN gradle bootJar -i --stacktrace

# https://nipafx.dev/jdeps-tutorial-analyze-java-project-dependencies/
# find JDK dependencies dynamically from jar
RUN jdeps \
    # dont worry about missing modules
    --ignore-missing-deps \
    # suppress any warnings printed to console
    -q \
    # java release version targeting
    --multi-release 11 \
    # output the dependencies at end of run
    --print-module-deps \
    # specify the the dependencies for the jar
    --class-path build/lib/* \
    # pipe the result of running jdeps on the app jar to file
    app/build/libs/ProxyBroker.jar > jre-deps.info
# https://stackoverflow.com/questions/57955837/java-11-java-beans-propertychangelistener
# https://stackoverflow.com/questions/61727613/unexpected-behaviour-from-gson
# Build minimal JRE
RUN jlink --verbose \
     --compress 2 \
     --no-header-files \
     --no-man-pages \
     --add-modules $(cat jre-deps.info),java.xml,java.sql,java.prefs,java.desktop,java.management,jdk.unsupported \
         # java.desktop - java/beans/PropertyEditorSupport \
         # java.instrument - java/lang/instrument/IllegalClassFormatException
         # java.naming - javax/naming/NamingException
         # java.management - javax/management/MBeanServer
         # java.security.jgss - org/ietf/jgss/GSSException \
     --module-path build/lib/* \
     --release-info="add:IMPLEMENTOR=ziloka:IMPLEMENTOR_VERSION=ziloka_JRE" \
     --output "$JAVA_MINIMAL"

FROM alpine:3.14.2

ENV JAVA_HOME=/opt/java-minimal
ENV PATH "$PATH:$JAVA_HOME/bin"

# https://stackoverflow.com/questions/51307075/docker-and-java-fontconfiguration-issue
# https://stackoverflow.com/a/68515645
# https://github.com/AdoptOpenJDK/openjdk-docker/issues/75#issuecomment-445815730
RUN ln -s /lib/libc.musl-x86_64.so.1 /usr/lib/libc.musl-x86_64.so.1
ENV LD_LIBRARY_PATH /usr/lib

COPY --from=build /usr/app/proxybroker/app/build/libs/ProxyBroker.jar .
COPY --from=build $JAVA_HOME $JAVA_HOME

ENTRYPOINT ["java", "-jar", "ProxyBroker.jar"]
