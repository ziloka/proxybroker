# https://levelup.gitconnected.com/create-an-optimized-rust-alpine-docker-image-1940db638a6c
# https://chemidy.medium.com/create-the-smallest-and-secured-golang-docker-image-based-on-scratch-4752223b7324
# https://github.com/rust-lang/cargo/issues/7563#issuecomment-636248861

# Rust 1.62
FROM rust@sha256:26d3406ed01076d53cf13b95397f2695fbb965e42aa685bca8045dcf11055904 AS build-env

# Set the working directory
WORKDIR /workspace/proxybroker/

# We want dependencies cached, so copy those first.
COPY Cargo.toml Cargo.lock ./

RUN apk add build-base ca-certificates curl g++ gcc libressl-dev make openssl-dev upx binutils

# Now copy in the rest of the sources
COPY src/ ./src/

# This is the actual application build.
RUN rustup target add x86_64-unknown-linux-musl && \
    cargo build --target x86_64-unknown-linux-musl --release && \
    upx target/x86_64-unknown-linux-musl/release/proxybroker

# Alpine 3.16.1
FROM alpine@sha256:7580ece7963bfa863801466c0a488f11c86f85d9988051a9f9c68cb27f6b7872

WORKDIR /app
COPY --from=build-env /workspace/proxybroker/target/x86_64-unknown-linux-musl/release/proxybroker .

# This command runs your application, comment out this line to compile only
CMD ["./proxybroker"]

LABEL Name=ProxyBroker Version=0.0.1
