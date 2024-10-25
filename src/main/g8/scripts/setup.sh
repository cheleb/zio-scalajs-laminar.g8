#!/usr/bin/env bash

. ./scripts/env.sh

if [ ! -e \$BUILD_ENV_FILE ]; then
    echo "Waiting for \$BUILD_ENV_FILE to be generated..."
    echo '  Import the project !!!'
    echo

    until [ -e \$BUILD_ENV_FILE ]; do
      echo -n "."
      sleep 4
    done

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
        age=\$(stat -t %s -f %m -- "\$filename")
        age_lock=\$(stat -t %s -f %m -- "\$filename_lock")
        if [ \$age_lock -lt \$age ]; then
            echo "Updating npm dependencies..."
            npm i
        fi
    fi
}

cd modules/client

npmInstall

$if(scalablytyped.truthy)$
#
# Generating scalablytyped
#
cd scalablytyped
npmInstall
cd ../../..
echo "Generating Scala.js bindings..."
sbt -mem 4096 compile
$endif$
