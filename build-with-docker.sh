#!/bin/bash

function cleanup {
	if [[ "$docker_status" != "0" ]] && [[ -n "$container_id" ]]; then
		docker --log-level=fatal rm -f ${container_id}
	fi
	unset image_tag dependency_version container_id docker_status
}

function usage {
	echo -e "\n$0 -t <docker_tag> -v <neo4j_dependency>\n"
	echo -e "\t-t Neo4j docker tag (see neo4j/neo4j on Docker Hub, e.g.: 'latest' or '2.3.0')"
	echo -e "\t-v Neo4j Maven dependency version (e.g.: 2.3.0)"
	echo -e "\t-h this help\n"
}

trap cleanup EXIT

while getopts "t:v:h" option
do
	case ${option} in
		t) image_tag=$OPTARG
		;;
		v) dependency_version=$OPTARG
		;;
		h) usage; exit 0
		;;
	esac
done

if [[ -z "$image_tag" ]] || [[ -z "$dependency_version" ]]; then
	>&2 usage
	exit 42
fi

container_id=`docker run --detach --publish=7474:7474 --volume=$HOME/neo4j-data:/tmp --env=NEO4J_AUTH=neo4j/foobar neo4j/neo4j:${image_tag}`
docker_status=$?
if [[ "$docker_status" != "0" ]]; then
	>&2 echo "Could not start Neo4j docker image at version: $image_tag. Aborting..."
	exit 42 
fi
sleep 10
mvn clean package -Pwith-neo4j-io,with-docker -Dneo4j.version=${dependency_version}


