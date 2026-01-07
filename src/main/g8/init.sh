#!/usr/bin/env bash

if [ ! -e "./scripts-managed/setup.sc" ]; then
  echo "✨ Setting up fullstack environment..."
  sbt fullstackInit
fi

./scripts-managed/setup.sc

