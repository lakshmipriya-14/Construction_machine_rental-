#!/bin/bash
echo "================================================"
echo " BuildRight Equipment Rental System"
echo "================================================"
echo ""

if command -v mvn &> /dev/null; then
    echo "Found Maven. Starting..."
    mvn spring-boot:run
elif [ -f "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
    source "$HOME/.sdkman/bin/sdkman-init.sh"
    mvn spring-boot:run
else
    echo "Maven not found. Trying to download..."
    MAVEN_VERSION=3.9.6
    MAVEN_DIR="$HOME/.buildright-maven/apache-maven-${MAVEN_VERSION}"
    if [ ! -f "$MAVEN_DIR/bin/mvn" ]; then
        mkdir -p "$HOME/.buildright-maven"
        curl -fsSL "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
          | tar -xz -C "$HOME/.buildright-maven/"
    fi
    "$MAVEN_DIR/bin/mvn" spring-boot:run
fi
