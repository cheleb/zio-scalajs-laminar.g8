#!/usr/bin/env bash
set -e
. ./scripts/env.sh -d

. $BUILD_ENV_FILE

echo "Waiting for $MAIN_JS_FILE to be compiled..."
until [ -e $MAIN_JS_FILE ]; do
    sleep 1
    echo -n "."
done

echo "Watching client-fastopt/main.js for changes..."

sleep 3

MOD=dev sbt '~client/fastLinkJS'
