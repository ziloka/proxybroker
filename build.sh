#!/bin/sh
cargo build --release

# rustup target add x86_64-unknown-linux-musl
# cargo build --target=x86_64-unknown-linux-musl --release
# rustup target add aarch64-apple-darwin
# cargo build --target=aarch64-apple-darwin --release
# rustup target add x86_64-apple-darwin
# cargo build --target=x86_64-apple-darwin --release
# rustup target add x86_64-pc-windows-msvc
# cargo build --target=x86_64-pc-windows-msvc --release

upx --best --lzma target/release/proxybroker
ls -lhsa target/release/proxybroker