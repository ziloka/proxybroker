# bullseye (debian 11) or buster (debian 10)
FROM mcr.microsoft.com/vscode/devcontainers/rust:0.202.5-bullseye

RUN apt-get update && \
    apt-get install -y git build-essential curl pkg-config software-properties-common iputils-ping upx-ucl neofetch

# Ubuntu 22.04 setup

# FROM ubuntu:22.04

# RUN apt-get update && \
#     apt-get install -y git build-essential curl pkg-config software-properties-common iputils-ping upx-ucl neofetch

# # Install rust
# RUN ["/bin/bash", "-c", "curl https://sh.rustup.rs -sSf | sh -s -- -y", "source \"$HOME/.cargo/env\""]

# # https://github.com/sysown/proxysql/pull/3937
# # Install Latest openssl development version (highest than 3.0.5+)
# # https://fedingo.com/how-to-install-openssl-in-ubuntu/
# # https://www.howtoforge.com/tutorial/how-to-install-openssl-from-source-on-linux/
# # https://github.com/openssl/openssl/issues/8170
# RUN apt install checkinstall zlib1g-dev -y && \
#     git clone --depth 1 --branch master https://github.com/openssl/openssl /usr/local/src/openssl && \
#     cd /usr/local/src/openssl && \
#     ./config --prefix=/usr/local/ssl --openssldir=/usr/local/ssl shared zlib && \
#     make -j 8 && \
#     make install_sw && \
#     cp libssl.so.3 /lib/x86_64-linux-gnu/libssl.so.3 && \
#     cp libcrypto.so.3 /lib/x86_64-linux-gnu/libcrypto.so.3 && \
#     cd /etc/ld.so.conf.d/ && \
#     echo "/usr/local/ssl/lib" >> openssl-1.0.2o.conf && \
#     ldconfig -v && \
#     mv /usr/bin/c_rehash /usr/bin/c_rehash.BEKUP && \
#     mv /usr/bin/openssl /usr/bin/openssl.BEKUP && \
#     rm /etc/environment && \
#     echo "PATH=\"/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games:/usr/local/ssl/bin/\"" >> /etc/environment && \
#     echo "export PKG_CONFIG_PATH=/usr/local/ssl/lib64/pkgconfig" >> /etc/profile

# # Resources to generate flamegraphs
# # https://github.com/flamegraph-rs/flamegraph

# # https://askubuntu.com/questions/50145/how-to-install-perf-monitoring-tool
# # https://askubuntu.com/a/753796
# RUN apt-get install -y flex bison && \
#     git clone --depth 1 --filter=blob:none --sparse https://github.com/torvalds/linux.git /tmp/linux && \
#     cd /tmp/linux && \
#     git sparse-checkout set tools && \
#     cd /tmp/linux/tools/perf && \ 
#     make && \
#     cp perf /usr/bin && \
#     cd / && \
#     rm -rf /tmp/linux

# # flamegraph -o flamegraph.svg -- target/release/proxybroker find