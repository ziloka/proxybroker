#!/bin/sh
cargo build --release
# rustup target add x86_64-unknown-linux-musl
# cargo build --target=x86_64-unknown-linux-musl
upx --best --lzma target/release/proxybroker
ls -lhsa target/release/proxybroker