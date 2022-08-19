FROM golang:1.17.5-alpine3.15

RUN apk add --no-cache --update musl-dev gcc make git bash \
  && apk add --no-cache --update bind-tools binutils util-linux file curl bash upx

ARG USERNAME=vscode
ARG USER_UID=1000
ARG USER_GID=$USER_UID
RUN apk add sudo && \
  addgroup $USERNAME && \
  adduser -u $USER_UID -G $USERNAME -s /bin/bash -D $USERNAME && \
  echo $USERNAME ALL=\(root\) NOPASSWD:ALL > /etc/sudoers.d/$USERNAME && \
  chmod 0440 /etc/sudoers.d/$USERNAME

# https://pkg.go.dev/net/http
# ENV GODEBUG http2debug=1
# https://syslog.ravelin.com/a-profiling-adventure-to-speed-up-go-5cfa198759db
ENV GOTRACE 1

USER vscode

RUN go get -v github.com/ramya-rao-a/go-outline && \
  go get -v golang.org/x/tools/gopls && \
  go get github.com/uudashr/gopkgs/v2/cmd/gopkgs && \
  go install honnef.co/go/tools/cmd/staticcheck@latest

# See here for image contents: https://github.com/microsoft/vscode-dev-containers/tree/v0.238.1/containers/go/.devcontainer/base.Dockerfile

# [Choice] Go version (use -bullseye variants on local arm64/Apple Silicon): 1, 1.16, 1.17, 1-bullseye, 1.16-bullseye, 1.17-bullseye, 1-buster, 1.16-buster, 1.17-buster
# ARG VARIANT="1.18-bullseye"
# FROM mcr.microsoft.com/vscode/devcontainers/go:0-${VARIANT}

# [Choice] Node.js version: none, lts/*, 16, 14, 12, 10
# ARG NODE_VERSION="none"
# RUN if [ "${NODE_VERSION}" != "none" ]; then su vscode -c "umask 0002 && . /usr/local/share/nvm/nvm.sh && nvm install ${NODE_VERSION} 2>&1"; fi

# [Optional] Uncomment this section to install additional OS packages.
# RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
#     && apt-get -y install --no-install-recommends <your-package-list-here>

# [Optional] Uncomment the next lines to use go get to install anything else you need
# USER vscode
# RUN go get -x <your-dependency-or-tool>

# [Optional] Uncomment this line to install global node packages.
# RUN su vscode -c "source /usr/local/share/nvm/nvm.sh && npm install -g <your-package-here>" 2>&1