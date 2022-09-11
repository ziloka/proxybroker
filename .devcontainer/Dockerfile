# https://github.com/graalvm/container/pkgs/container/graalvm-ce/29791026?tag=ol8-java17-22.2.0-b1
FROM ghcr.io/graalvm/graalvm-ce:ol8-java17-22.2.0-b1

# Setup vscode user
ARG USERNAME=vscode
ARG USER_UID=1000
ARG USER_GID=$USER_UID
RUN microdnf install sudo && \
  groupadd $USERNAME && \
  adduser -u $USER_UID -g $USERNAME -s /bin/bash $USERNAME && \
  echo $USERNAME ALL=\(root\) NOPASSWD:ALL > /etc/sudoers.d/$USERNAME && \
  chmod 0440 /etc/sudoers.d/$USERNAME

# https://github.com/oracle/container-images/issues/16#issuecomment-543459881
RUN microdnf update \
  && microdnf install git unzip zip wget curl \
  && gu install native-image