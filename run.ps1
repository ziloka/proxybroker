# Collect proxies
# gradlew run --args="find --types http HTTPS --coutries US --limit 10 --outfile ./proxies.txt"

# Execute Commands in x64 Native Tools Command Prompt (last built on vs2019)
# Activate GraalVM native-image build environment
# https://www.graalvm.org/docs/getting-started/windows/#prerequisites-for-using-native-image-on-windows
cmd /c "C:\Program Files (x86)\Microsoft Visual Studio\2019\Community\VC\Auxiliary\Build\vcvarsx86_amd64.bat"
# With --no-fallback throws error
native-image -jar build/libs/ProxyChecker.jar