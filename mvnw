#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF)
# Maven Wrapper startup script for Linux/macOS
# ----------------------------------------------------------------------------

if [ -z "$JAVA_HOME" ]; then
  JAVACMD=$(which java)
else
  JAVACMD="$JAVA_HOME/bin/java"
fi

MAVEN_WRAPPER_JAR=".mvn/wrapper/maven-wrapper.jar"
MAVEN_WRAPPER_PROPERTIES=".mvn/wrapper/maven-wrapper.properties"

DISTRIBUTION_URL=$(grep distributionUrl "$MAVEN_WRAPPER_PROPERTIES" | sed 's/distributionUrl=//')
MAVEN_HOME="$HOME/.m2/wrapper/dists/$(basename $DISTRIBUTION_URL .zip)"

if [ ! -d "$MAVEN_HOME" ]; then
  echo "Downloading Maven..."
  mkdir -p "$MAVEN_HOME"
  TEMP_ZIP="$MAVEN_HOME/apache-maven.zip"
  curl -sL "$DISTRIBUTION_URL" -o "$TEMP_ZIP"
  unzip -q "$TEMP_ZIP" -d "$MAVEN_HOME"
  rm "$TEMP_ZIP"
fi

MAVEN_BIN=$(find "$MAVEN_HOME" -name "mvn" -type f | head -1)
exec "$MAVEN_BIN" "$@"
