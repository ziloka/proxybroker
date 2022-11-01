#!/bin/sh

# https://github.com/johnthagen/min-sized-rust#optimize-libstd-with-build-std

host=$(rustc -Vv | grep -oP "(?<=host: )[\w_-]+")

# Setup dependencies
rustup toolchain install nightly
rustup component add rust-src --toolchain nightly

# Compile
cargo +nightly build -Z build-std=std,panic_abort -Z build-std-features=panic_immediate_abort --target $host --release

# Run post build tasks
upx --best --lzma target/$host/release/proxybroker
ls -lhsa target/$host/release/proxybroker