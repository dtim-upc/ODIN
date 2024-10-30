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

# Example usage:
if __name__ == "__main__":
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

    # Specify GraphDB base URL
    graphdb_base_url = 'http://localhost:7200'

    # Create repository in GraphDB
    create_repository_from_template('./read-write-graphdb/utils/repository_template.ttl', repository_values, graphdb_base_url)
