#!/usr/bin/env bash

. ./scripts/env.sh

rm -f \$NPM_DEV_STARTED

filename_lock=node_modules/.package-lock.json

function npmInstall () {
  if [ ! -f "\$filename_lock" ]; then
      echo "First time setup: Installing npm dependencies..."
      npm i
  else
      filename=package.json
      age=\$((\$(date +%s) - \$(stat -t %s -f %m -- "\$filename")))
      age_lock=\$((\$(date +%s) - \$(stat -t %s -f %m -- "\$filename_lock")))
      echo "Reinstalling npm dependencies..."
      if [ \$age_lock -gt \$age ]; then
          echo "(New dependencies)"
          npm i
      else
          npm ci
      fi
  fi
}


cd modules/client

npmInstall

$if(scalablytyped.truthy)$
cd scalablytyped
npmInstall
cd ../../..
sbt -mem 4096 compile
$endif$
