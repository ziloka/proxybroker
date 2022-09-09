FROM mcr.microsoft.com/vscode/devcontainers/rust:0.202.5-bullseye

RUN sudo apt-get update && \
    sudo apt-get install -y iputils-ping upx-ucl neofetch

# Resources to generate flamegraphs
# https://github.com/jonhoo/inferno
# https://github.com/flamegraph-rs/flamegraph
# https://gist.github.com/KodrAus/97c92c07a90b1fdd6853654357fd557a

# https://askubuntu.com/questions/50145/how-to-install-perf-monitoring-tool
# https://askubuntu.com/a/753796
RUN sudo apt-get install -y build-essential flex bison && \
    git clone --depth 1 --filter=blob:none --sparse https://github.com/torvalds/linux.git /tmp/linux && \
    cd /tmp/linux && \
    git sparse-checkout set tools && \
    cd /tmp/linux/tools/perf && \ 
    make && \
    sudo cp perf /usr/bin && \
    cargo install flamegraph
# Usage: flamegraph -o flamegraph.svg -- ./target/release/proxybroker find