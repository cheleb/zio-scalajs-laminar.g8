#!/usr/bin/env bash

. ./scripts/env.sh

if [ ! -e \$BUILD_ENV_FILE ]; then
    echo "Waiting for \$BUILD_ENV_FILE to be generated..."
    echo '  Import the project !!!'
    echo '  \'
    echo '   \'
    echo '    _\/'
fi

until [ -e \$BUILD_ENV_FILE ]; do
    echo -n "."
    sleep 4
done

. \$BUILD_ENV_FILE
echo
echo "Starting npm dev server for client"
echo " * SCALA_VERSION=\$SCALA_VERSION"
rm -f modules/client/target/scala-\$SCALA_VERSION/client-fastopt/main.js
touch \$NPM_DEV_STARTED
cd modules/client
npm run dev
