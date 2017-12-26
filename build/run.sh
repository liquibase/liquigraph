#!/usr/bin/env bash
set -e
if [ "$WITH_DOCKER" = true ] ; then
    docker pull neo4j:${NEO_VERSION}
    docker run --detach --publish=7474:7474 --volume=$HOME/neo4j/data:/data --env=NEO4J_AUTH=neo4j/j4oen neo4j:${NEO_VERSION}
    # Wait up to 30s for Neo4j to become available
    for i in {1..30}; do
        curl -s localhost:7474/db/data -o /dev/null && break
        sleep 1
    done
fi
mvn -T4 -q -B -V clean verify -Dneo4j.version=${NEO_VERSION} ${EXTRA_PROFILES}
