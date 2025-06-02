#!/bin/sh

echo "Running in $BUILD_MODE mode"

if [ "$BUILD_MODE" = "build" ]; then
  echo "Building and serving Quasar SPA..."
  quasar build && npx quasar serve dist/spa --port 9000 --hostname 0.0.0.0
else
  echo "Starting Quasar in dev mode..."
  quasar dev
fi
