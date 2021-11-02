# https://github.com/microsoft/vscode-remote-try-go/blob/main/.devcontainer/Dockerfile

# See here for image contents: https://github.com/microsoft/vscode-dev-containers/tree/v0.195.0/containers/go/.devcontainer/base.Dockerfile
# [Choice] Go version (use -bullseye variants on local arm64/Apple Silicon): 1, 1.16, 1.17, 1-bullseye, 1.16-bullseye, 1.17-bullseye, 1-buster, 1.16-buster, 1.17-buster

FROM golang:1.17.2-alpine3.14

# https://pkg.go.dev/net/http
ENV GODEBUG http2debug=1

RUN apk add musl-dev gcc sudo git

RUN go get -v github.com/ramya-rao-a/go-outline \
  && go get -v golang.org/x/tools/gopls \
  && go get github.com/uudashr/gopkgs/v2/cmd/gopkgs \
  && go get honnef.co/go/tools/cmd/staticcheck@latest

# ARG VARIANT=1-bullseye
# FROM mcr.microsoft.com/vscode/devcontainers/go:0-${VARIANT}

# [Optional] Uncomment this section to install additional OS packages.
# RUN apt-get update && export DEBIAN_FRONTEND=noninteractive  \
#     && apt-get -y install --no-install-recommends <your-package-list-here>

# [Optional] Uncomment the next line to use go get to install anything else you need
# RUN go get -x <your-dependency-or-tool>

# [Optional] Uncomment this line to install global node packages.
# RUN su vscode -c "source /usr/local/share/nvm/nvm.sh && npm install -g <your-package-here>" 2>&1