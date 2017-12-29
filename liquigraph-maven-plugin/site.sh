#!/bin/zsh
set -ex

CURRENT_VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate \
			-Dexpression=project.version 2> /dev/null \
			| grep '^\d')
VERSION=$(echo $CURRENT_VERSION | sed 's/-SNAPSHOT//')
mvn -B -q site 2> /dev/null
for file in `ls **/*.html`; do sed -i .bak "s/$CURRENT_VERSION/$VERSION/" $file; done
