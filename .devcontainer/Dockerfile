# [Choice] Node.js version (use -bullseye variants on local arm64/Apple Silicon): 18, 16, 14, 18-bullseye, 16-bullseye, 14-bullseye, 18-buster, 16-buster, 14-buster
ARG VARIANT=16-bullseye
FROM mcr.microsoft.com/vscode/devcontainers/javascript-node:0-${VARIANT}

# [Optional] Uncomment this section to install additional OS packages.
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends cmake upx-ucl

# [Optional] Uncomment if you want to install an additional version of node using nvm
# ARG EXTRA_NODE_VERSION=10
# RUN su node -c "source /usr/local/share/nvm/nvm.sh && nvm install ${EXTRA_NODE_VERSION}"

# [Optional] Uncomment if you want to install more global node modules
RUN su node -c "yarn global add -g nexe pkg"

# Install UPX from source
# RUN cd /tmp && \
#   git clone --depth 1 https://github.com/upx/upx && \
#   cd upx && \
#   git submodule update --init && \
#   make && \
#   cp build/release/upx /usr/bin/upx


# RUN cd /tmp && \
#   wget https://github.com/upx/upx/suites/8272200487/artifacts/362142752 && \
#   unzip upx-devel4-518525f-ubuntu-22.04.zip && \
#   tar xzf upx-devel4-518525f-ubuntu-22.04.tar.gz && \
#   cd upx-devel4-518525f-ubuntu-22.04/release-clang && \
#   cp upx /usr/bin/upx