# Collect proxies
# gradlew run --args="find --types http HTTPS --coutries US --limit 10 --outfile ./proxies.txt"
# Activate GraalVM native-image build environment
cmd /c "C:\Program Files (x86)\Microsoft Visual Studio\2019\Community\VC\Auxiliary\Build\vcvarsx86_amd64.bat"
native-image -jar build/libs/ProxyChecker.jar