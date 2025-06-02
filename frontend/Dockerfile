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

# Entry point that decides whether to build or serve
ENTRYPOINT ["/entrypoint.sh"]

