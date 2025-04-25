import json
import os
import shutil
import time

import requests

# GraphDB REST API
## https://graphdb.ontotext.com/documentation/10.1/using-the-graphdb-rest-api.html

def read_ttl_template(template_file):
    """Reads TTL template file and returns its content."""
    with open(template_file, 'r') as f:
        return f.read()

def replace_placeholders(ttl_content, values):
    """Replaces placeholders in TTL content with corresponding values."""
    for key, value in values.items():
        placeholder = f'<{key}>'
        ttl_content = ttl_content.replace(placeholder, value)
    return ttl_content

def create_repository_from_template(template_file, values, base_url):
    """Creates a repository in GraphDB using provided TTL template and values."""
    # Read TTL template
    ttl_content = read_ttl_template(template_file)
    
    # Replace placeholders
    ttl_content = replace_placeholders(ttl_content, values)
    
    repositories_endpoint = f'{base_url}/rest/repositories'
    files = {'config': ('repository_config.ttl', ttl_content.encode('utf-8'), 'text/turtle')}
    
    # Send POST request to create repository
    response = requests.post(repositories_endpoint, files=files)
    
    if response.status_code == 201:
        repository_id = values['REPOSITORY_ID']
        print(f"Repository {repository_id} created successfully.")
    else:
        print(f"Failed to create repository. Status code: {response.status_code}")
        print(response.text)

def repository_exists(repository_id, base_url):
    """Checks if a repository exists in GraphDB."""
    repositories_endpoint = f'{base_url}/rest/repositories'

    try:
        response = requests.get(repositories_endpoint)
        if response.status_code == 200:
            repositories = response.json()  # Assuming response is in JSON format
            if not repositories:
                return False
            else:
                return True
        else:
            print(f"Failed to retrieve repositories. Status code: {response.status_code}")
            print(response.text)
    except requests.ConnectionError:
        print("Error connecting to GraphDB while checking repository existence.")

    return False

def import_server_files(source_file, destination_directory, base_url, repo_id):
    """
    Copies a file to a specified server directory and imports it to a GraphDB repository via a POST request.
    """

    filename = os.path.basename(source_file)
    destination_path = os.path.join(destination_directory, filename)
    shutil.copy(source_file, destination_path)

    for file_name in os.listdir('read-write-graphdb/graphdb-import/'):
        print(file_name)
    print(f"File '{filename}' copied to server directory '{destination_directory}'.")

    data_url = filename
    payload = {
        "fileNames": ["read-write-graphdb/graphdb-import/KnowledgeBase.nt"]
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
    # Specify GraphDB base URL
    graphdb_base_url = os.getenv('GRAPHDB_URL')
    time.sleep(15) # Wait for GraphDB to fully initialize
    # Specify repository configuration values
    repository_values = {
        'REPOSITORY_ID': 'test-repo',  # Replace with a valid repository ID
        'READ_ONLY': 'false',
        'RULESET': 'rdfsplus-optimized',
        'DISABLE_SAMEAS': 'true',
        'CHECK_INCONSISTENCIES': 'false',
        'ENTITY_ID_SIZE': '32',
        'ENABLE_CONTEXT_INDEX': 'false',
        'ENABLE_PREDICATE_LIST': 'true',
        'ENABLE_FTS_INDEX': 'false',
        'FTS_INDEXES': '"default" "iri"',
        'FTS_STRING_LITERALS_INDEX': 'default',
        'FTS_IRIS_INDEX': 'none',
        'QUERY_TIMEOUT': '0',
        'THROW_QUERY_EVAL_EXCEPTION': 'false',
        'QUERY_LIMIT_RESULTS': '0',
        'BASE_URL': 'http://example.org/owlim#',
        'DEFAULT_NS': '',
        'IMPORTS': '',
        'REPOSITORY_TYPE': 'file-repository',
        'STORAGE_FOLDER': 'storage',
        'ENTITY_INDEX_SIZE': '10000000',
        'IN_MEMORY_LITERAL_PROPERTIES': 'true',
        'ENABLE_LITERAL_INDEX': 'true'
    }

    # Check if repository exists before creating it
    # if not repository_exists(repository_values['REPOSITORY_ID'], graphdb_base_url):
    # Create repository in GraphDB
    create_repository_from_template('./read-write-graphdb/utils/repository_template.ttl', repository_values,
                                    graphdb_base_url)
    source_file = "./read-write-graphdb/graphdb-import/KnowledgeBase.nt"
    repo_id = "test-repo"  # specify your repository name

    import_server_files(source_file, "data/", graphdb_base_url, repo_id)
