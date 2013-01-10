#!/bin/bash
source "$(dirname $0)"/readProjectProperties.sh
JAR_NAME=$name"-"$version".jar"
[ -f "$JAR_NAME" ] && rm "$JAR_NAME"
"`dirname $0`"/buildAndTest.sh
jar cvf "$JAR_NAME" -C classes . && echo "Successfully built jar '$JAR_NAME'" || echo "Failed to built '$JAR_NAME'"
