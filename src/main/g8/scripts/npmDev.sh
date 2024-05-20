. ./scripts/env.sh

until [ -e \$BUILD_ENV_FILE ]; do
    echo "Waiting for \$BUILD_ENV_FILE to be generated..."
    sleep 2
done

. \$BUILD_ENV_FILE

echo "Starting npm dev server for client"
echo " * SCALA_VERSION=\$SCALA_VERSION"
rm -f modules/client/target/scala-\$SCALA_VERSION/client-fastopt/main.js
touch \$NPM_DEV_STARTED
cd modules/client
npm run dev
