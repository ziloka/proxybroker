# https://levelup.gitconnected.com/create-an-optimized-rust-alpine-docker-image-1940db638a6c
# https://chemidy.medium.com/create-the-smallest-and-secured-golang-docker-image-based-on-scratch-4752223b7324
# https://github.com/rust-lang/cargo/issues/7563#issuecomment-636248861

# Rust 1.64 Alpine 3.16
FROM rust@sha256:3963de6b13ca26a8ca3e8964fe82810b9e84814e97feb8a7319578d1fcd045fd AS build-env

# Set the working directory
WORKDIR /workspaces/proxybroker/

# We want dependencies cached, so copy those first.
COPY Cargo.toml Cargo.lock ./

RUN apk add build-base ca-certificates curl g++ gcc libressl-dev make openssl-dev upx binutils

# Now copy in the rest of the sources
COPY src/ ./src/

# This is the actual application build.
RUN cargo build --release && \
    upx target/release/proxybroker

# FROM gcr.io/distroless/cc-debian11
# Alpine 3.16.1
FROM alpine@sha256:7580ece7963bfa863801466c0a488f11c86f85d9988051a9f9c68cb27f6b7872
# FROM scratch

WORKDIR /app
COPY --from=build-env /workspace/proxybroker/target/release/proxybroker .

# This command runs your application, comment out this line to compile only
ENTRYPOINT ["./proxybroker"]

LABEL Name=ProxyBroker Version=0.0.1
