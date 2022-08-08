# https://stackoverflow.com/questions/11354518/application-auto-build-versioning
# https://stackoverflow.com/a/39378759

# This how we want to name the binary output
BINARY=ProxyBroker

# These are the values we want to pass for VERSION and BUILD
# git tag 1.0.1
# git commit -am "One more change after the tags"
VERSION=`git describe --tags`
BUILD=`date +%FT%T%z`

# Setup the -ldflags option for go build here, interpolate the variable values
LDFLAGS=-ldflags "-w -s -X main.Version=${VERSION} -X main.Build=${BUILD}"

# Builds the project
build:
		cd src; \
		go build ${LDFLAGS} -o ${BINARY}

# Installs our project: copies binaries
install:
		go install ${LDFLAGS}

# Cleans our project: deletes binaries
clean:
		if [ -f ${BINARY} ] ; then rm ${BINARY} ; fi

.PHONY: clean install