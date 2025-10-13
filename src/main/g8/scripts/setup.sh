#!/usr/bin/env bash
set -e
#
# This script is used to setup the project
# - Install npm dependencies
# - Generate Scala.js bindings
#
. ./scripts/env.sh

# Function to get file modification time in a cross-platform way
get_file_mtime() {
    if [[ "\$OSTYPE" == "darwin"* ]]; then
        # macOS
        stat -f %m "\$1"
    else
        # Linux
        stat -c %Y "\$1"
    fi
}

if [ ! -e \$BUILD_ENV_FILE ]; then
    echo "Waiting for \$BUILD_ENV_FILE to be generated..."
    echo '  Import the project !!!'
    echo

    sbt projects # Will generate the BUILD_ENV_FILE file

    echo
    echo
    echo " Good job 🚀"
    echo

fi

. \$BUILD_ENV_FILE

rm -f \$MAIN_JS_FILE

filename_lock=node_modules/.package-lock.json

function npmInstall() {
    if [ ! -f "\$filename_lock" ]; then
        echo "First time setup: Installing npm dependencies..."
        npm i
    else
        filename=package.json
        age=\$(get_file_mtime "\$filename")
        age_lock=\$(get_file_mtime "\$filename_lock")
        if [ \$age_lock -lt \$age ]; then
            echo "Updating npm dependencies..."
            npm i
        fi
    fi
}

pushd() {
    command pushd "\$@" >/dev/null
}

popd() {
    command popd "\$@" >/dev/null
}

pushd modules/client
npmInstall
popd

$if(scalablytyped.truthy)$
#
# Generating scalablytyped
#
pushd modules/client/scalablytyped
npmInstall
popd

echo "Generating Scala.js bindings..."
sbt -mem 8192 compile
$endif$