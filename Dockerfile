FROM golang:1.17.2-alpine3.14 as build

WORKDIR /usr/app/proxybroker
COPY . .

ENV GOOS=linux

# https://blog.filippo.io/shrink-your-go-binaries-with-this-one-weird-trick/
RUN go build -ldflags="-s -w" -o ProxyBroker main.go \
	&& apk add binutils upx \
	&& strip --strip-all ProxyBroker \
	&& upx -9 ProxyBroker

FROM scratch

COPY --from=build /usr/app/proxybroker/ProxyBroker main
CMD ["./ProxyBroker"]