FROM mcr.microsoft.com/vscode/devcontainers/rust:1-bullseye

RUN sudo apt-get update && \
    sudo apt-get install -y iputils-ping upx && \
    rustup default stable 

# RUN apk add --no-cache gcc g++ musl-dev cmake make automake autoconf openssl-dev
# RUN sudo chown -R vscode /workspaces/proxybroker

#RUN rm /etc/alpine-release \
#&&	wget -q -O /etc/apk/keys/sgerrand.rsa.pub https://alpine-pkgs.sgerrand.com/sgerrand.rsa.pub \
#&&	wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.33-r0/glibc-2.33-r0.apk \
#&&	apk add glibc-2.33-r0.apk
#&&  apk add --no-cache -X http://dl-cdn.alpinelinux.org/alpine/edge/testing mono gdb

#ENV LD_LIBRARY_PATH=/usr/glibc-compat/lib:/usr/lib:/lib

# RUN apk add --no-cache coreutils wget file \
# 	bash icu-libs krb5-libs libgcc libintl libssl1.1 libstdc++ zlib \
# &&	wget https://dot.net/v1/dotnet-install.sh \
# &&	chmod +x dotnet-install.sh \
# &&	./dotnet-install.sh -c 5.0

# mono OpenDebugAD7.dll --help
# ~/.dotnet/dotnet OpenDebugAD7.dll --help