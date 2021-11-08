FROM golang:1.17.3-alpine3.14 as build

WORKDIR /usr/app/proxybroker
COPY . .

ENV GOOS=linux
ENV CGO_ENABLED=0

RUN apk add --no-cache ca-certificates

# https://blog.filippo.io/shrink-your-go-binaries-with-this-one-weird-trick/
RUN go install -ldflags '-extldflags "-static"' && \
  go build -ldflags="-s -w" -o ProxyBroker main.go && \
  apk add binutils upx && \
  strip --strip-all ProxyBroker && \
  upx -9 ProxyBroker

FROM scratch

# https://stackoverflow.com/a/61752498
COPY --from=build /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/
COPY --from=build /usr/app/proxybroker/ProxyBroker ProxyBroker

ENTRYPOINT ["./ProxyBroker"]