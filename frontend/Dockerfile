# Use the official Node.js image as the base
FROM node:20

# Set the working directory
WORKDIR /app

ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh


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
CMD ["/wait-for-it.sh", "backend:3000", "--", "quasar", "dev"]

