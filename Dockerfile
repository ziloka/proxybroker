FROM golang:1.17.5-alpine3.15 as build

# https://medium.com/geekculture/golang-app-build-version-in-containers-3d4833a55094
ARG BUILD_VERSION

WORKDIR /usr/app/proxybroker
COPY . .

ENV GOOS=linux
ENV CGO_ENABLED=0

RUN apk add --no-cache ca-certificates upx binutils

# https://dev.to/akshaybharambe14/guide-to-compress-golang-binaries-2d95
# https://prog.world/optimizing-the-size-of-the-go-binary/
# https://blog.filippo.io/shrink-your-go-binaries-with-this-one-weird-trick/
RUN cd src \
  go install -ldflags '-extldflags "-static"' && \
  go build -ldflags="-s -w -X main.sha1ver=$(git rev-parse HEAD) -X main.buildTime=$(date +'%Y-%m-%d_%T') -X main.BuildVersion=${BUILD_VERSION}" -o ProxyBroker main.go && \
  strip --strip-all ProxyBroker && \
  upx --best --lzma ProxyBroker

FROM scratch

# https://stackoverflow.com/questions/61752074/http-request-in-docker-container-fails-with-https-endpoint
# https://stackoverflow.com/a/61752498
COPY --from=build /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/
COPY --from=build /usr/app/proxybroker/src/ProxyBroker ProxyBroker

ENTRYPOINT ["./ProxyBroker"]