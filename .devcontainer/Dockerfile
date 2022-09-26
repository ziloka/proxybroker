# bullseye (debian 11) or buster (debian 10)
FROM mcr.microsoft.com/vscode/devcontainers/rust:0.202.5-bullseye

RUN sudo apt-get update && \
    sudo apt-get install -y software-properties-common iputils-ping upx-ucl neofetch

# Resources to generate flamegraphs
# https://github.com/flamegraph-rs/flamegraph
# https://github.com/jonhoo/inferno
# https://gist.github.com/KodrAus/97c92c07a90b1fdd6853654357fd557a

# https://askubuntu.com/questions/50145/how-to-install-perf-monitoring-tool
# https://askubuntu.com/a/753796
RUN apt-get install -y build-essential flex bison && \
    git clone --depth 1 --filter=blob:none --sparse https://github.com/torvalds/linux.git /tmp/linux && \
    cd /tmp/linux && \
    git sparse-checkout set tools && \
    cd /tmp/linux/tools/perf && \ 
    make && \
    sudo cp perf /usr/bin && \
    cd / && \
    rm -rf /tmp/linux

# docker run -it ubuntu:22.04 /bin/bash
# apt-get update && apt-get install -y wget unzip libssl1.1
# wget http://nz2.archive.ubuntu.com/ubuntu/pool/main/o/openssl/libssl1.1_1.1.1f-1ubuntu2.16_amd64.deb

USER vscode

RUN cargo install flamegraph cross
# Usage: flamegraph -o flamegraph.svg -- ./target/release/proxybroker find

# FROM mcr.microsoft.com/vscode/devcontainers/base:ubuntu-22.04
# https://github.com/sfackler/rust-openssl/issues/855#issuecomment-796384141
# RUN sudo apt-get update && \
#     sudo apt-get install -y build-essential pkg-config libssl-dev

# # https://github.com/rust-lang/rustup/issues/1031#issuecomment-354025515
# RUN curl https://sh.rustup.rs -sSf | sh -s -- -y && \
#     source "$HOME/.cargo/env"
