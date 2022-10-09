If the the system has Openssl v1, it will outperform with a system that compiled with openssl <v3.0.5

These two articles are what I found
- https://github.com/openssl/openssl/issues/17064#issuecomment-973444945
- https://developers.redhat.com/articles/2021/12/17/why-glibc-234-removed-libpthread

The following three had similar performance results (~5s)

Had `glibc 2.31`, and `1.1.1n`
```bash
docker run -it debian:11 /bin/bash
apt-get update && \
apt-get install -y curl git build-essential pkg-config libssl-dev && \
curl https://sh.rustup.rs -sSf | sh -s -- -y && \
source "$HOME/.cargo/env" && \
git clone -b rust --single-branch https://github.com/ziloka/proxybroker && \
cd proxybroker && \
time cargo build --release && \
ldd target/release/proxybroker && \
# Print information relating to system
time cargo run --release find && \
ldd --version ldd && \
openssl version && \
ldconfig -p | grep libssl
```

Had `glibc 2.31`, and `1.1.1f`
```bash
docker run -it ubuntu:20.04 /bin/bash
apt-get update && \
apt-get install -y curl git build-essential pkg-config libssl-dev && \
curl https://sh.rustup.rs -sSf | sh -s -- -y && \
source "$HOME/.cargo/env" && \
git clone -b rust --single-branch https://github.com/ziloka/proxybroker && \
cd proxybroker && \
time cargo build --release && \
ldd target/release/proxybroker && \
time cargo run --release find && \
# Print information relating to system
ldd --version ldd && \
openssl version && \
ldconfig -p | grep libssl
```

Had `glibc 2.28`, and `openssl 1.1.1k`
```bash
docker run -it redhat/ubi8-minimal:8.6-941 
microdnf install -y gcc git openssl-devel procps-ng
curl https://sh.rustup.rs -sSf | sh -s -- -y && \
source "$HOME/.cargo/env" && \
git clone -b rust --single-branch https://github.com/ziloka/proxybroker && \
cd proxybroker && \
time cargo build --release && \
ldd target/release/proxybroker && \
time cargo run --release find && \
# Print information relating to system
ldd --version ldd && \
openssl version && \
ldconfig -p | grep libssl
```

However with this configuration it took about a minute.


Had `glibc 2.35` and `openssl 3.0.2`
```bash
docker run -it ubuntu:22.04 /bin/bash
apt-get update && \
apt-get install -y curl git build-essential pkg-config libssl-dev && \
curl https://sh.rustup.rs -sSf | sh -s -- -y && \
source "$HOME/.cargo/env" && \
git clone -b rust --single-branch https://github.com/ziloka/proxybroker && \
cd proxybroker && \
time cargo build --release && \
ldd target/release/proxybroker && \
time cargo run --release find && \
# Print information relating to system
ldd --version ldd && \
openssl version && \
ldconfig -p | grep libssl
```

Try compiling it with glibc 2.34+ and using openssl 1.0

Had `glibc 2.35` and `openssl 1.0.2o`
```bash
docker run -it ubuntu:22.04 /bin/bash
apt-get update && \
apt-get install -y curl wget git build-essential checkinstall zlib1g-dev pkg-config && \
# Build openssl from source 
cd /usr/local/src/ && \
wget https://www.openssl.org/source/openssl-1.0.2o.tar.gz && \
tar -xf openssl-1.0.2o.tar.gz && \
cd openssl-1.0.2o && \
./config --prefix=/usr/local/ssl --openssldir=/usr/local/ssl shared zlib && \
make && \
make test && \
make install && \
cd /etc/ld.so.conf.d/ && \
echo "/usr/local/ssl/lib" >> openssl-1.0.2o.conf && \
ldconfig -v && \
mv /usr/bin/c_rehash /usr/bin/c_rehash.BEKUP && \
mv /usr/bin/openssl /usr/bin/openssl.BEKUP && \
rm /etc/environment && \
echo "PATH=\"/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games:/usr/local/ssl/bin\"" >> /etc/environment && \
source /etc/environment && \
export PKG_CONFIG_PATH=/usr/local/ssl/lib/pkgconfig && \
# Start building rust application
curl https://sh.rustup.rs -sSf | sh -s -- -y && \
source "$HOME/.cargo/env" && \
git clone -b rust --single-branch https://github.com/ziloka/proxybroker /proxybroker && \
cd /proxybroker && \
time cargo build --release && \
ldd target/release/proxybroker && \
time cargo run --release find && \
# Print information relating to system
ldd --version ldd && \
openssl version && \
ldconfig -p | grep libssl
```

Compiling with a system which has openssl 3.0 appears to degrade the performance by a signficiant amount.
You should compile with a system which has openssl 1.0.

The glibc version on the system does not neccessarily matter.

Just Note openssl 3.0.5+ seems to fix this performance issue