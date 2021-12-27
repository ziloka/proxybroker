# https://github.com/microsoft/vscode-remote-try-go/blob/main/.devcontainer/Dockerfile
# https://github.com/microsoft/vscode-dev-containers/tree/v0.195.0/containers/go/.devcontainer/base.Dockerfile

# See here for image contents: https://github.com/microsoft/vscode-dev-containers/tree/v0.195.0/containers/go/.devcontainer/base.Dockerfile
# [Choice] Go version (use -bullseye variants on local arm64/Apple Silicon): 1, 1.16, 1.17, 1-bullseye, 1.16-bullseye, 1.17-bullseye, 1-buster, 1.16-buster, 1.17-buster

FROM golang:1.17.5-alpine3.15

# https://pkg.go.dev/net/http
# ENV GODEBUG http2debug=1
# https://syslog.ravelin.com/a-profiling-adventure-to-speed-up-go-5cfa198759db
ENV GOTRACE 1

RUN apk add --no-cache --update musl-dev gcc make git \
  && apk add --no-cache --update bind-tools binutils util-linux file curl

RUN go get -v github.com/ramya-rao-a/go-outline && \
  go get -v golang.org/x/tools/gopls && \
  go get github.com/uudashr/gopkgs/v2/cmd/gopkgs && \
  go install honnef.co/go/tools/cmd/staticcheck@latest

# Add ndk-build to environment variables
# ENV PATH="/${NDK_VERSION}/:${PATH}"

# ARG VARIANT=1-bullseye
# FROM mcr.microsoft.com/vscode/devcontainers/go:0-${VARIANT}

# [Optional] Uncomment this section to install additional OS packages.
# RUN apt-get update && export DEBIAN_FRONTEND=noninteractive  \
#     && apt-get -y install --no-install-recommends <your-package-list-here>

# [Optional] Uncomment the next line to use go get to install anything else you need
# RUN go get -x <your-dependency-or-tool>

# [Optional] Uncomment this line to install global node packages.
# RUN su vscode -c "source /usr/local/share/nvm/nvm.sh && npm install -g <your-package-here>" 2>&1