

hello:
	echo "Hello"
build:
	go build -o ProxyBroker main.go
run:
	go run main.go
compile:
	# Run go tool dist list to see supported platforms
	echo "Compiling for every OS and Platform"
	GOOS=aix GOARCH=ppc64 go build -o dist/ProxyBroker-aix-ppc64 main.go
	GOOS=android GOARCH=386 go build -o dist/ProxyBroker-android-386 main.go
	GOOS=android GOARCH=amd64 go build -o dist/ProxyBroker-android-amd64 main.go
	GOOS=android GOARCH=arm go build -o dist/ProxyBroker-android-arm main.go
	GOOS=android GOARCH=arm64 go build -o dist/ProxyBroker-android-arm64 main.go
	GOOS=darwin GOARCH=amd64 go build -o dist/ProxyBroker-darwin-amd64 main.go
	GOOS=darwin GOARCH=arm64 go build -o dist/ProxyBroker-darwin-arm64 main.go
	GOOS=dragonfly GOARCH=amd64 go build -o dist/ProxyBroker-dragonfly-amd64 main.go
	GOOS=freebsd GOARCH=386 go build -o dist/ProxyBroker-freebsd-386 main.go
	GOOS=freebsd GOARCH=amd64 go build -o dist/ProxyBroker-freebsd-amd64 main.go
	GOOS=freebsd GOARCH=arm go build -o dist/ProxyBroker-freebsd-arm main.go
	GOOS=freebsd GOARCH=arm64 go build -o dist/ProxyBroker-freebsd-arm64 main.go
	GOOS=illumos GOARCH=amd64 go build -o dist/ProxyBroker-illumos-amd64 main.go
	GOOS=ios GOARCH=amd64 go build -o dist/ProxyBroker-ios-amd64 main.go
	GOOS=ios GOARCH=arm64 go build -o dist/ProxyBroker-ios-arm64 main.go
	GOOS=js GOARCH=wasm go build -o dist/ProxyBroker-js-wasm main.go
	GOOS=linux GOARCH=386 go build -o dist/ProxyBroker-linux-386 main.go
	GOOS=linux GOARCH=amd64 go build -o dist/ProxyBroker-linux-amd64 main.go
	GOOS=linux GOARCH=arm go build -o dist/ProxyBroker-linux-arm main.go
	GOOS=linux GOARCH=arm64 go build -o dist/ProxyBroker-linux-arm64 main.go
	GOOS=linux GOARCH=mips go build -o dist/ProxyBroker-linux-mips main.go
	GOOS=linux GOARCH=mips64 go build -o dist/ProxyBroker-linux-mips64 main.go
	GOOS=linux GOARCH=mips64le go build -o dist/ProxyBroker-linux-mips64le main.go
	GOOS=linux GOARCH=mipsle go build -o dist/ProxyBroker-linux-mipsle main.go
	GOOS=linux GOARCH=ppc64 go build -o dist/ProxyBroker-linux-ppc64 main.go
	GOOS=linux GOARCH=ppc64le go build -o dist/ProxyBroker-linux-ppc64le main.go
	GOOS=linux GOARCH=riscv64 go build -o dist/ProxyBroker-linux-riscv64 main.go
	GOOS=linux GOARCH=s390x go build -o dist/ProxyBroker-linux-s390x main.go
	GOOS=netbsd GOARCH=386 go build -o dist/ProxyBroker-netbsd-386 main.go
	GOOS=netbsd GOARCH=amd64 go build -o dist/ProxyBroker-netbsd-amd64 main.go
	GOOS=netbsd GOARCH=arm go build -o dist/ProxyBroker-netbsd-arm main.go
	GOOS=netbsd GOARCH=arm64 go build -o dist/ProxyBroker-netbsd-arm64 main.go
	GOOS=openbsd GOARCH=386 go build -o dist/ProxyBroker-openbsd-386 main.go
	GOOS=openbsd GOARCH=amd64 go build -o dist/ProxyBroker-openbsd-amd64 main.go
	GOOS=openbsd GOARCH=arm go build -o dist/ProxyBroker-openbsd-arm main.go
	GOOS=openbsd GOARCH=arm64 go build -o dist/ProxyBroker-openbsd-arm64 main.go
	GOOS=openbsd GOARCH=mips64 go build -o dist/ProxyBroker-openbsd-mips64 main.go
	GOOS=plan9 GOARCH=386 go build -o dist/ProxyBroker-plan9-386 main.go
	GOOS=plan9 GOARCH=amd64 go build -o dist/ProxyBroker-plan9-amd64 main.go
	GOOS=plan9 GOARCH=arm go build -o dist/ProxyBroker-plan9-arm main.go
	GOOS=solaris GOARCH=amd64 go build -o dist/ProxyBroker-solaris-amd64 main.go
	GOOS=windows GOARCH=386 go build -o dist/ProxyBroker-windows-386 main.go
	GOOS=windows GOARCH=amd64 go build -o dist/ProxyBroker-windows-amd64 main.go
	GOOS=windows GOARCH=arm go build -o dis0.t/ProxyBroker-windows-arm main.go
	GOOS=windows GOARCH=amd64 go build -o dist/ProxyBroker-windows-amd64 main.go
