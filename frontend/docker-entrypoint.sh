#!/bin/sh

echo "Running in $BUILD_MODE mode"

if [ "$BUILD_MODE" = "build" ]; then
  echo "Building and serving Quasar SPA..."
  quasar build
  http-server dist/spa -p 9000 -a 0.0.0.0
else
  echo "Starting Quasar in dev mode..."
  quasar dev
fi
