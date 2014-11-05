#!/bin/sh
FOLDER=$(git rev-parse --show-toplevel || echo ".");
FOLDER="$FOLDER/liquigraph-cli"
(cd $FOLDER; cat target/classes/liquigraph target/liquigraph.jar > target/liquigraph.run && chmod +x target/liquigraph.run)

