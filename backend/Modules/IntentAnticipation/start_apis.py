import subprocess
import signal
import time

# Function to run a Flask app with the given command
def run_flask_app(command):
    """Run a Flask app in a separate process."""
    process = subprocess.Popen(command, shell=True)
    return process

if __name__ == "__main__":
    # Define the commands to run each Flask app on different ports
    commands = [
        "flask --app ./llm/api_llm_interaction.py run --host 0.0.0.0 --port 8001",
        "flask --app ./read-write-graphdb/api_graphdb_interaction.py run --host 0.0.0.0 --port 8002"
    ]

    # Start each command in a separate process
    processes = [run_flask_app(command) for command in commands]

    try:
        # Keep the main script running while the Flask apps are running
        print("APIs are running. Press Ctrl+C to stop.")
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        # Handle termination gracefully on Ctrl+C
        print("\nShutting down all APIs...")
        for process in processes:
            process.send_signal(signal.SIGINT)
            process.wait()
        print("All APIs have been terminated.")