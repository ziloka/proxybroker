FROM mcr.microsoft.com/vscode/devcontainers/rust:0.202.5-bullseye

RUN sudo apt-get update && \
    sudo apt-get install -y iputils-ping upx-ucl neofetch

# Rename current perf version to kernel version (5.4) can be found out by uname -r
# This solution does not work at all
# https://stackoverflow.com/questions/56259373/perf-version-not-matching-with-my-kernel-version

# Resources to generate flamegraphs
# https://github.com/jonhoo/inferno
# https://github.com/flamegraph-rs/flamegraph
# https://gist.github.com/KodrAus/97c92c07a90b1fdd6853654357fd557a

# https://askubuntu.com/questions/50145/how-to-install-perf-monitoring-tool
# https://askubuntu.com/a/753796
RUN sudo apt-get install -y build-essential flex bison && \
    git clone --depth 1 https://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git /tmp/linux && \
    cd /tmp/linux/tools/perf && \ 
    make && \
    sudo cp perf /usr/bin && \
    cargo install flamegraph
# Usage: flamegraph -o flamegraph.svg -- ./target/release/proxybroker find