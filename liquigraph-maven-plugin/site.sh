#!/bin/zsh
set -ex

CURRENT_VERSION=$(mvn -N -q org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=project.version -DforceStdout=true)
VERSION=$(echo $CURRENT_VERSION | sed 's/-SNAPSHOT.*//')
mvn -B -q site 2> /dev/null
for file in `ls **/*.html`; do sed -i .bak "s/$CURRENT_VERSION/$VERSION/" $file; done
