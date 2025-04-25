# Use the official Python 3.11 image from the Docker Hub
FROM python:3.11-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the requirements file first to leverage Docker cache
COPY . .

# Install dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Expose the application's port (default for FastAPI is 8000)
EXPOSE 8000

# Run API
CMD ["flask", "--app", "./api/api_main.py", "run", "--host", "0.0.0.0", "--port", "8000" ]
