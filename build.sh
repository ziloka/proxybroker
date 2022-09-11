#!/bin/sh

# https://github.com/oracle/graal/issues/4782
# https://www.graalvm.org/22.0/reference-manual/native-image/Agent/
# native-image -classpath app/src/resources/ --no-fallback --enable-http --enable-https -jar app/build/libs/ProxyBroker.jar

# https://stackoverflow.com/questions/67987247/graalvm-native-image-reflection-doesnt-work
# https://stackoverflow.com/a/68007563
# ./gradlew -Dagentlib:native-image-agent=config-output-dir=native-image.conf run --args="find"
# native-image -H:+ReportExceptionStackTraces -jar app/build/libs/ProxyBroker.jar
./gradlew -Pagent run --args="find"

./gradlew nativeCompile