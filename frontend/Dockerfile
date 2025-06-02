FROM node:20

WORKDIR /app

# Accept build-time argument (default: dev)
ARG BUILD_MODE=dev

# Set runtime environment variable from build arg
ENV BUILD_MODE=$BUILD_MODE

# Install Quasar CLI globally
RUN npm install -g @quasar/cli

# Copy project files
COPY . .

# Install dependencies
RUN npm install --force && yarn install

EXPOSE 9000

# Set default environment variable
ENV BUILD_MODE=dev

# Entry point that decides whether to build or serve
CMD if [ "$BUILD_MODE" = "build" ]; then quasar build; else quasar dev; fi
