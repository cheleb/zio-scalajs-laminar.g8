#!/usr/bin/env bash

. ./scripts/env.sh

. \$BUILD_ENV_FILE

echo
echo "Starting npm dev server for client"
echo " * SCALA_VERSION=\$SCALA_VERSION"
sleep 3
cd modules/client
#DEBUG="vite:sourcemap" npm run dev
npm run dev
