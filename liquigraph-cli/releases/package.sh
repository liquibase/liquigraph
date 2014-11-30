#!/bin/sh
FOLDER=$(git rev-parse --show-toplevel || echo ".");
FOLDER="$FOLDER/liquigraph-cli"
(cd $FOLDER; zip -j target/liquigraph.zip target/classes/liquigraph target/liquigraph.jar)

