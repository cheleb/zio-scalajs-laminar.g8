#!/usr/bin/env bash
set -e
#
# This script is used to run the fullstack server
#  See getoptions.sh for the command line options
#    or run `./scripts/fullstackRun.sh -h`
#

# Import the project environment variables
. ./scripts/env.sh

# This is a hack to force the build-env.sh file to be generated
# And setup the project
if [ ! -e $BUILD_ENV_FILE ]; then
    sbt projects
fi

. ./scripts/setup-noninteractive.sh

MOD=$BUILD_MOD sbt -mem 4096 "${BUILD_CLEAN}server/run"
