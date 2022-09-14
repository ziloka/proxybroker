npm run build
npm run compileNative
# Packaging executables makes pkg unable to read from file
# upx --best --lzma build/proxybroker*
ls -lhsa build