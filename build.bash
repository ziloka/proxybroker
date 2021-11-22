#!/usr/bin/env bash

# https://www.digitalocean.com/community/tutorials/how-to-build-go-executables-for-multiple-platforms-on-ubuntu-16-04

# Run to compile for the following platforms: MacOS, Windows, Linux (The most commonly used operating systems)
# bash build.bash github.com/Ziloka/ProxyBroker

# https://blog.kowalczyk.info/article/vEja/embedding-build-number-in-go-executable.html
# notice how we avoid spaces in $now to avoid quotation hell in go build command
BUILD_TIME=$(date +'%Y-%m-%d_%T')
BUILD_VERSION=$2

package=$1
if [[ -z "$package" ]]; then
  echo "usage: $0 <package-name>"
  exit 1
elif [[ -z "$BUILD_VERSION" ]]; then
  echo "usage: $0 <package-name> <package-version>"
  exit 1
fi

package_split=(${package//\// })
package_name=${package_split[-1]}

platforms=("windows/amd64" "linux/amd64" "darwin/amd64")

for platform in "${platforms[@]}"
do
  platform_split=(${platform//\// })
  GOOS=${platform_split[0]}
  GOARCH=${platform_split[1]}
  output_name=$package_name'-'$GOOS'-'$GOARCH
  if [ $GOOS = "windows" ]; then
    output_name+='.exe'
  fi
    
  env GOOS=$GOOS GOARCH=$GOARCH go build -ldflags="-s -w -X main.SHA_HASH=$(git rev-parse HEAD) -X main.BUILD_TIME=$BUILD_TIME -X main.BUILD_VERSION=$BUILD_VERSION" -o $output_name $package
  if [ $? -ne 0 ]; then
    echo 'An error has occurred! Aborting the script execution...'
    exit 1
  fi
done