#!/bin/sh
cargo build --release
# cargo build --features maxminddb --release
upx --best --lzma target/release/proxybroker
ls -lhsa target/release/proxybroker