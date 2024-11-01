# Use the official Node.js image as the base
FROM node:20

# Set the working directory
WORKDIR /app

# Install Yarn globally
#RUN npm install -g yarn

# Install Quasar CLI globally
RUN npm install -g @quasar/cli

# Copy the project files
COPY . .

# Install project dependencies
RUN npm install  --force
RUN yarn install

# Expose the port your application runs on
EXPOSE 9000

# Run the application
CMD ["quasar", "dev"]