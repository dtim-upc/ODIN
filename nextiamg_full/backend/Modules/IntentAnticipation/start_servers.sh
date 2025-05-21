#!/bin/bash

# Array to keep track of PIDs
declare -a PIDS

# Function to start a server and check if it starts successfully
start_server() {
    local server_dir=$1
    local port=$2
    local entry_point=$3
    local requirements_file=$4

    echo "Starting server in $server_dir on port $port with entry point $entry_point..."

    if ! cd "$server_dir"; then
        echo "Failed to cd into $server_dir"
        exit 1
    fi

    # Install requirements if the requirements file exists
    if [ -n "$requirements_file" ] && [ -f "$requirements_file" ]; then
        echo "Installing dependencies from $requirements_file..."
        python3.11 -m pip install -r "$requirements_file"
    else
        echo "No requirements file found or not specified in $server_dir"
    fi

    # Set up the environment variables if any (optional)
    if [ -f .env ]; then
        export $(cat .env | xargs)
    fi

    # Run the Flask server
    if [ -z "$entry_point" ]; then
        echo "No entry point specified for $server_dir"
        return 1
    fi
    FLASK_APP=$entry_point python3.11 -m flask run --port="$port" &

    # Get the PID of the last background process (the Flask server)
    local pid=$!

    # Add PID to array
    PIDS+=($pid)

    echo "Server started in $server_dir with PID $pid"

    # Return to the original directory
    cd ..
}

# Set up a trap to kill all processes on script exit or failure
cleanup() {
    echo "Cleaning up..."
    for pid in "${PIDS[@]}"; do
        if ps -p $pid > /dev/null; then
            echo "Killing process $pid"
            kill $pid
        fi
    done
}

trap cleanup EXIT

start_server "web_app" 8000 "__init__.py" "requirements.txt"
start_server "llm" 8001 "api_llm_interaction.py" "requirements.txt"
start_server "read-write-graphdb" 8002 "api_graphdb_interaction.py" "requirements.txt"
start_server "automl" 8003 "api_automl_interaction.py" "requirements.txt"

# Wait for all background processes to finish
wait
