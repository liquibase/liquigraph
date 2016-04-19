#!/usr/bin/env bash
set -e
if [ "$WITH_DOCKER" = true ] ; then
    docker pull neo4j:${NEO_VERSION}
    docker run --detach --publish=7474:7474 --volume=$HOME/neo4j/data:/data --env=NEO4J_AUTH=neo4j/j4oen neo4j
fi
mvn -T4 clean test -Dneo4j.version=${NEO_VERSION} ${EXTRA_PROFILES}