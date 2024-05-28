#!/usr/bin/env bash

. ./scripts/env.sh

rm -f \$NPM_DEV_STARTED

filename_lock=node_modules/.package-lock.json


function npmInstall () {
  if [ ! -f "\$filename_lock" ]; then
      echo "First time setup: Installing npm dependencies..."
      npm i
  else
      npmRefresh=3600
      filename=package.json
      age=\$((\$(date +%s) - \$(stat -t %s -f %m -- "\$filename")))
      age_lock=\$((\$(date +%s) - \$(stat -t %s -f %m -- "\$filename_lock")))
      if [ \$age_lock -gt \$npmRefresh ] || [ \$age_lock -gt \$age ]; then
          echo "Reinstalling npm dependencies..."
          if [ \$age_lock -gt \$age ]; then
              echo "(New dependencies)"
          else
              echo "(Refreshing dependencies: package-lock.json is older than \$npmRefresh seconds (\$age_lock).)"
          fi
  
          npm i
      else
          echo "Skipping npm install... \$filename_lock is less than an \$npmRefresh seconds old (\$age)."
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
