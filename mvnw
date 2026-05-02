#!/bin/sh
# Maven Wrapper — downloads Maven if not present
MAVEN_VERSION=3.9.6
MAVEN_HOME="$HOME/.m2/wrapper/dists/apache-maven-${MAVEN_VERSION}"
MVN_CMD="$MAVEN_HOME/bin/mvn"

if [ ! -f "$MVN_CMD" ]; then
  echo "Downloading Maven ${MAVEN_VERSION}..."
  mkdir -p "$MAVEN_HOME"
  curl -fsSL "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
    | tar -xz -C "$HOME/.m2/wrapper/dists/"
  mv "$HOME/.m2/wrapper/dists/apache-maven-${MAVEN_VERSION}" "$MAVEN_HOME" 2>/dev/null || true
fi

exec "$MVN_CMD" "$@"
