import argparse
import shutil
import os
import requests
import json

# GraphDB REST API
## https://graphdb.ontotext.com/documentation/10.1/using-the-graphdb-rest-api.html

def import_server_files(source_file, destination_directory, base_url, repo_id):
    """
    Copies a file to a specified server directory and imports it to a GraphDB repository via a POST request.
    """
    os.makedirs(destination_directory, exist_ok=True)

    filename = os.path.basename(source_file)
    destination_path = os.path.join(destination_directory, filename)
    shutil.copy(source_file, destination_path)

    print(f"File '{filename}' copied to server directory '{destination_directory}'.")

    data_url = filename
    payload = {
        "fileNames": [data_url]
    }
    headers = {
        "Content-Type": "application/json"
    }

    url = f"{base_url}/rest/repositories/{repo_id}/import/server"
    response = requests.post(url, headers=headers, data=json.dumps(payload))

    if response.status_code == 202:
        print(f"File '{filename}' imported to repository {repo_id} successfully.")
    else:
        print(f"Failed to import files to repository {repo_id}. Status Code: {response.status_code}, Error: {response.text}")

# Example usage:
if __name__ == "__main__":

    # Set up argument parsing
    parser = argparse.ArgumentParser(description="Import server files to a specified directory.")
    parser.add_argument("destination_directory", type=str, help="Local path to GraphDB server directory where files will be imported.")
    args = parser.parse_args()

    destination_directory = args.destination_directory
    
    if not os.path.exists(destination_directory):
        os.makedirs(destination_directory)
    
    # Parse the arguments
    args = parser.parse_args()
    source_file = "./read-write-graphdb/graphdb-import/KnowledgeBase.nt"
    base_url = os.getenv('GRAPHDB_URL')
    repo_id = "test-repo" # specify your repository name
    
    import_server_files(source_file, "data/", base_url, repo_id)
