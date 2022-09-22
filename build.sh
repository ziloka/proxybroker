#!/bin/sh
cargo build --release

upx --best --lzma target/release/proxybroker
ls -lhsa target/release/proxybroker