import requests
import urllib.parse
import json
import pandas as pd
import rdflib
from rdflib import Graph, URIRef, XSD, Literal
from rdflib.namespace import RDF, RDFS
import math
import os
from utils import save_workflow

# GraphDB REST API
## https://graphdb.ontotext.com/documentation/10.1/using-the-graphdb-rest-api.html

base_url = os.getenv('GRAPHDB_URL')
repository = "test-repo"
last_inserted_user= None

def execute_sparql_query(base_url, repository, query):
    """
    Executes a SPARQL query using GraphDB's REST API and returns the results.
    
    Args:
    - base_url (str): The base URL of the GraphDB server.
    - repository (str): The name of the GraphDB repository.
    - query (str): The SPARQL query to execute.
    
    Returns:
    - dict: The JSON response from the SPARQL endpoint.
    """
    try:
        # URL encode the query using quote_plus for proper URL encoding
        encoded_query = urllib.parse.quote_plus(query)
        
        url = f"{base_url}/repositories/{repository}?query={encoded_query}"
        
        headers = {
            "Accept": "application/sparql-results+json"
        }
        
        # Make the GET request to the SPARQL endpoint
        response = requests.get(url, headers=headers)
        
        response.raise_for_status()  

        return response.json()
        
    except requests.exceptions.RequestException as e:
        # Log the exception details and re-raise it
        print(f"Failed to execute SPARQL query. Error: {str(e)}")
        raise

def get_intent(user, dataset):
    """
    Retrieves the most used intent associated with a user and dataset.
    
    Args:
    - user (str): The user identifier.
    - dataset (str): The dataset identifier.
    
    Returns:
    - str: The most used intent.
    """
    
    found = False
    intent = None
    
    # Check if the user has used the dataset before
    query = f"""
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    PREFIX ml: <http://localhost/8080/intentOntology#>

    SELECT ?intent (COUNT(?intent) AS ?count)
    WHERE {{
        ml:{user} ml:runs ?workflow.
        ?workflow ml:hasInput ml:{dataset}.
        ?workflow ml:achieves ?task.
        ?task ml:hasIntent ?intent 
    }}
    GROUP BY ?intent
    ORDER BY DESC(?count)
    LIMIT 1
    """
    
    results = execute_sparql_query(base_url, repository, query)
    
    if results["results"]["bindings"]:
        intent = results["results"]["bindings"][0]["intent"]["value"]
        found = True
    
    # If not found, look for the dataset usage by any user
    if not found:
        query = f"""
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX ml: <http://localhost/8080/intentOntology#>

        SELECT ?intent (COUNT(?intent) AS ?count)
        WHERE {{
            ?workflow ml:hasInput ml:{dataset}.
            ?workflow ml:achieves ?task.
            ?task ml:hasIntent ?intent 
        }}
        GROUP BY ?intent
        ORDER BY DESC(?count)
        LIMIT 1
        """
        
        results = execute_sparql_query(base_url, repository, query)
        
        if results["results"]["bindings"]:
            intent = results["results"]["bindings"][0]["intent"]["value"]
            found = True
    
    # If still not found, look for the most used intent by the user
    if not found:
        query = f"""
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX ml: <http://localhost/8080/intentOntology#>

        SELECT ?intent (COUNT(?intent) AS ?count)
        WHERE {{
            ml:{user} ml:runs ?workflow.
            ?workflow ml:achieves ?task.
            ?task ml:hasIntent ?intent 
        }}
        GROUP BY ?intent
        ORDER BY DESC(?count)
        LIMIT 1
        """
        
        results = execute_sparql_query(base_url, repository, query)
        
        if results["results"]["bindings"]:
            intent = results["results"]["bindings"][0]["intent"]["value"]
            found = True
    
    # If still not found, get the most used intent overall
    if not found:
        query = """
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX ml: <http://localhost/8080/intentOntology#>

        SELECT ?intent (COUNT(?intent) AS ?count)
        WHERE {
            ?task ml:hasIntent ?intent 
        }
        GROUP BY ?intent
        ORDER BY DESC(?count)
        LIMIT 1
        """
        
        results = execute_sparql_query(base_url, repository, query)
        
        if results["results"]["bindings"]:
            intent = results["results"]["bindings"][0]["intent"]["value"]
            found = True
    
    return intent.split("#")[-1]

def get_metric(user, dataset, intent):
    """
    Retrieves the most used metric associated with a user, dataset, and intent.
    
    Args:
    - user (str): The user identifier.
    - dataset (str): The dataset identifier.
    - intent (str): The intent identifier.
    
    Returns:
    - str: The most used metric.
    """
    
    found = False
    metric = None
    
    # Check if the user has used the dataset for the intent
    query = f"""
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    PREFIX ml: <http://localhost/8080/intentOntology#>

    SELECT ?metric (COUNT(?metric) AS ?count)
    WHERE {{
        ml:{user} ml:runs ?workflow.
        ?workflow ml:hasInput ml:{dataset}.
        ?workflow ml:achieves ?task.
        ?task ml:hasIntent ml:{intent}.
        ?task ml:hasRequirement ?eval.
        ?eval ml:onMetric ?metric 
    }}
    GROUP BY ?metric
    ORDER BY DESC(?count)
    LIMIT 1
    """
    
    results = execute_sparql_query(base_url, repository, query)
    
    if results["results"]["bindings"]:
        metric = results["results"]["bindings"][0]["metric"]["value"]
        found = True
    
    # If not found, look for the dataset usage by any user for the intent
    if not found:
        query = f"""
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX ml: <http://localhost/8080/intentOntology#>

        SELECT ?metric (COUNT(?metric) AS ?count)
        WHERE {{
            ?workflow ml:hasInput ml:{dataset}.
            ?workflow ml:achieves ?task.
            ?task ml:hasIntent ml:{intent}.
            ?task ml:hasRequirement ?eval.
            ?eval ml:onMetric ?metric 
        }}
        GROUP BY ?metric
        ORDER BY DESC(?count)
        LIMIT 1
        """
        
        results = execute_sparql_query(base_url, repository, query)
        
        if results["results"]["bindings"]:
            metric = results["results"]["bindings"][0]["metric"]["value"]
            found = True
    
    # If still not found, look for the most used metric by the user for the intent
    if not found:
        query = f"""
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX ml: <http://localhost/8080/intentOntology#>

        SELECT ?metric (COUNT(?metric) AS ?count)
        WHERE {{
            ml:{user} ml:runs ?workflow.
            ?workflow ml:achieves ?task.
            ?task ml:hasIntent ml:{intent}.
            ?task ml:hasRequirement ?eval.
            ?eval ml:onMetric ?metric 
        }}
        GROUP BY ?metric
        ORDER BY DESC(?count)
        LIMIT 1
        """
        
        results = execute_sparql_query(base_url, repository, query)
        
        if results["results"]["bindings"]:
            metric = results["results"]["bindings"][0]["metric"]["value"]
            found = True
    
    # If still not found, get the most used metric overall for the intent
    if not found:
        query = f"""
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX ml: <http://localhost/8080/intentOntology#>

        SELECT ?metric (COUNT(?metric) AS ?count)
        WHERE {{
            ?task ml:hasIntent ml:{intent}.
            ?task ml:hasRequirement ?eval.
            ?eval ml:onMetric ?metric 
        }}
        GROUP BY ?metric
        ORDER BY DESC(?count)
        LIMIT 1
        """
        
        results = execute_sparql_query(base_url, repository, query)
        print(results)
        
        if results["results"]["bindings"]:
            metric = results["results"]["bindings"][0]["metric"]["value"]
            found = True
    
    return metric.split("#")[-1]


def get_preprocessing(user, dataset, intent):
    """
    Determines if preprocessing is required based on user-specific or general task constraints for a given dataset and intent.

    Args:
    - user (str): The user identifier.
    - dataset (str): The dataset identifier.
    - intent (str): The intent identifier.

    Returns:
    - bool: True if preprocessing is required, False otherwise.
    """
    found = False
    preprocessing = True

    # Check if the user has used the dataset for the intent with ConstraintNoPreprocessing
    query = f"""
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    PREFIX ml: <http://localhost/8080/intentOntology#>

    SELECT (COUNT(DISTINCT ?task) AS ?constraintTaskCount)
    WHERE {{
        ml:{user} ml:runs ?workflow.
        ?workflow ml:hasInput ml:{dataset}.
        ?workflow ml:achieves ?task.
        ?task ml:hasIntent ml:{intent}.
        ?task ml:hasConstraint ml:ConstraintNoPreprocessing
    }}
    """
    results = execute_sparql_query(base_url, repository, query)

    if results["results"]["bindings"]:
        constraint_task = int(results["results"]["bindings"][0]["constraintTaskCount"]["value"])
        found = True

    if found:
        # Count total tasks achieved by the user with the dataset
        query_aux = f"""
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX ml: <http://localhost/8080/intentOntology#>

        SELECT (COUNT(DISTINCT ?task) AS ?taskCount)
        WHERE {{
            ml:{user} ml:runs ?workflow.
            ?workflow ml:hasInput ml:{dataset}.
            ?workflow ml:achieves ?task.
        }}
        """
        results_aux = execute_sparql_query(base_url, repository, query_aux)

        if results_aux["results"]["bindings"]:
            total_tasks = int(results_aux["results"]["bindings"][0]["taskCount"]["value"])

            if total_tasks > 0:
                if constraint_task / total_tasks < 0.5:
                    preprocessing = True
                else:
                    preprocessing = False

    if not found:
        # Count total tasks achieved by any user with the dataset
        query = f"""
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX ml: <http://localhost/8080/intentOntology#>

        SELECT (COUNT(DISTINCT ?task) AS ?constraintTaskCount)
        WHERE {{
            ?workflow ml:hasInput ml:{dataset}.
            ?workflow ml:achieves ?task.
            ?task ml:hasConstraint ml:ConstraintNoPreprocessing
        }}
        """
        results = execute_sparql_query(base_url, repository, query)

        if results["results"]["bindings"]:
            constraint_task = int(results["results"]["bindings"][0]["constraintTaskCount"]["value"])
            found = True

        if found:
            query_aux = f"""
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX ml: <http://localhost/8080/intentOntology#>

            SELECT (COUNT(DISTINCT ?task) AS ?taskCount)
            WHERE {{
                ?workflow ml:hasInput ml:{dataset}.
                ?workflow ml:achieves ?task.
            }}
            """
            results_aux = execute_sparql_query(base_url, repository, query_aux)

            if results_aux["results"]["bindings"]:
                total_tasks = int(results_aux["results"]["bindings"][0]["taskCount"]["value"])

                # Check if total_tasks is not zero to avoid division by zero
                if total_tasks > 0:
                    if constraint_task / total_tasks < 0.5:
                        preprocessing = True
                    else:
                        preprocessing = False

    if not found:
        # Count tasks achieved by the user with the intent and ConstraintNoPreprocessing
        query = f"""
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX ml: <http://localhost/8080/intentOntology#>

        SELECT (COUNT(DISTINCT ?task) AS ?constraintTaskCount)
        WHERE {{
            ml:{user} ml:runs ?workflow.
            ?workflow ml:achieves ?task.
            ?task ml:hasIntent ml:{intent}.
            ?task ml:hasConstraint ml:ConstraintNoPreprocessing
        }}
        """
        results = execute_sparql_query(base_url, repository, query)

        if results["results"]["bindings"]:
            constraint_task = int(results["results"]["bindings"][0]["constraintTaskCount"]["value"])
            found = True

        if found:
            query_aux = f"""
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX ml: <http://localhost/8080/intentOntology#>

            SELECT (COUNT(DISTINCT ?task) AS ?taskCount)
            WHERE {{
                ml:{user} ml:runs ?workflow.
                ?workflow ml:achieves ?task.
                ?task ml:hasIntent ml:{intent}
            }}
            """
            results_aux = execute_sparql_query(base_url, repository, query_aux)

            if results_aux["results"]["bindings"]:
                total_tasks = int(results_aux["results"]["bindings"][0]["taskCount"]["value"])

                # Check if total_tasks is not zero to avoid division by zero
                if total_tasks > 0:
                    if constraint_task / total_tasks < 0.5:
                        preprocessing = True
                    else:
                        preprocessing = False

    if not found:
        # Count tasks achieved by any user with the intent and ConstraintNoPreprocessing
        query = f"""
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX ml: <http://localhost/8080/intentOntology#>

        SELECT (COUNT(DISTINCT ?task) AS ?constraintTaskCount)
        WHERE {{
            ?task ml:hasIntent ml:{intent}.
            ?task ml:hasConstraint ml:ConstraintNoPreprocessing
        }}
        """
        results = execute_sparql_query(base_url, repository, query)

        if results["results"]["bindings"]:
            constraint_task = int(results["results"]["bindings"][0]["constraintTaskCount"]["value"])
            found = True

        if found:
            query_aux = f"""
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX ml: <http://localhost/8080/intentOntology#>

            SELECT (COUNT(DISTINCT ?task) AS ?taskCount)
            WHERE {{
                ?task ml:hasIntent ml:{intent}
            }}
            """
            results_aux = execute_sparql_query(base_url, repository, query_aux)

            if results_aux["results"]["bindings"]:
                total_tasks = int(results_aux["results"]["bindings"][0]["taskCount"]["value"])

                # Check if total_tasks is not zero to avoid division by zero
                if total_tasks > 0:
                    if constraint_task / total_tasks < 0.5:
                        preprocessing = True
                    else:
                        preprocessing = False
    if preprocessing:
        return "Yes"
    else:
        return "No"


def get_algorithm(user, dataset, intent):

    """
    Retrieves the most frequently used algorithm for a given user, dataset, and intent.

    Args:
    - user (str): The user identifier.
    - dataset (str): The dataset identifier.
    - intent (str): The intent identifier.

    Returns:
    - str: The most frequently used algorithm for the specified criteria, or None if no algorithm is found.
    """

    found = False
    algorithm = None
    
    # Check if the user has used the dataset with a specific algorithm constraint
    query = f"""
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    PREFIX ml: <http://localhost/8080/intentOntology#>

    SELECT ?algorithm (COUNT(?algorithm) AS ?count)
    WHERE {{
        ml:{user} ml:runs ?workflow.
        ?workflow ml:hasInput ml:{dataset}.
        ?workflow ml:achieves ?task.
        ?task ml:hasConstraint ?constraint.
        ?constraint rdf:type ml:ConstraintAlgorithm.
        ?constraint ml:on ?algorithm 
    }}
    GROUP BY ?algorithm
    ORDER BY DESC(?count)
    LIMIT 1
    """
    
    results = execute_sparql_query(base_url, repository, query)
    
    if results["results"]["bindings"]:
        algorithm = results["results"]["bindings"][0]["algorithm"]["value"]
        found = True
    
    # If not found, look for other users' usage of the dataset
    if not found:
        query = f"""
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX ml: <http://localhost/8080/intentOntology#>

        SELECT ?algorithm (COUNT(?algorithm) AS ?count)
        WHERE {{
            ?workflow ml:hasInput ml:{dataset}.
            ?workflow ml:achieves ?task.
            ?task ml:hasConstraint ?constraint.
            ?constraint rdf:type ml:ConstraintAlgorithm.
            ?constraint ml:on ?algorithm 
        }}
        GROUP BY ?algorithm
        ORDER BY DESC(?count)
        LIMIT 1
        """
        
        results = execute_sparql_query(base_url, repository, query)
        
        if results["results"]["bindings"]:
            algorithm = results["results"]["bindings"][0]["algorithm"]["value"]
            found = True
    
    # If still not found, look for the user's usage of the same intent
    if not found:
        query = f"""
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX ml: <http://localhost/8080/intentOntology#>

        SELECT ?algorithm (COUNT(?algorithm) AS ?count)
        WHERE {{
            ml:{user} ml:runs ?workflow.
            ?workflow ml:achieves ?task.
            ?task ml:hasIntent ml:{intent}.
            ?task ml:hasConstraint ?constraint.
            ?constraint rdf:type ml:ConstraintAlgorithm.
            ?constraint ml:on ?algorithm 
        }}
        GROUP BY ?algorithm
        ORDER BY DESC(?count)
        LIMIT 1
        """
        
        results = execute_sparql_query(base_url, repository, query)
        
        if results["results"]["bindings"]:
            algorithm = results["results"]["bindings"][0]["algorithm"]["value"]
            found = True
    
    # If still not found, get the most used algorithm for the intent overall
    if not found:
        query = f"""
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX ml: <http://localhost/8080/intentOntology#>

        SELECT ?algorithm (COUNT(?algorithm) AS ?count)
        WHERE {{
            ?task ml:hasIntent ml:{intent}.
            ?task ml:hasConstraint ?constraint.
            ?constraint rdf:type ml:ConstraintAlgorithm.
            ?constraint ml:on ?algorithm 
        }}
        GROUP BY ?algorithm
        ORDER BY DESC(?count)
        LIMIT 1
        """
        
        results = execute_sparql_query(base_url, repository, query)
        
        if results["results"]["bindings"]:
            algorithm = results["results"]["bindings"][0]["algorithm"]["value"]
            found = True
    
    return algorithm.split("#")[-1] if algorithm else None


def get_preprocessing_algorithm(user, dataset, intent):
    """
    Retrieves the most used preprocessing algorithm associated with a user, dataset, and intent.
    
    Args:
    - user (str): The user identifier.
    - dataset (str): The dataset identifier.
    - intent (str): The intent identifier.
    
    Returns:
    - str: The most used preprocessing algorithm.
    """
    
    found = False
    algorithm = None
    
    # Check if the user has used the dataset with a preprocessing algorithm
    query = f"""
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    PREFIX ml: <http://localhost/8080/intentOntology#>

    SELECT ?algorithm (COUNT(?algorithm) AS ?count)
    WHERE {{
        ml:{user} ml:runs ?workflow.
        ?workflow ml:hasInput ml:{dataset}.
        ?workflow ml:achieves ?task.
        ?task ml:hasConstraint ?constraint.
        ?constraint rdf:type ml:ConstraintPreprocessingAlgorithm.
        ?constraint ml:on ?algorithm 
    }}
    GROUP BY ?algorithm
    ORDER BY DESC(?count)
    LIMIT 1
    """
    
    results = execute_sparql_query(base_url, repository, query)
    
    if results["results"]["bindings"]:
        algorithm = results["results"]["bindings"][0]["algorithm"]["value"]
        found = True
    
    # If not found, look for the dataset usage by any user with a preprocessing algorithm
    if not found:
        query = f"""
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX ml: <http://localhost/8080/intentOntology#>

        SELECT ?algorithm (COUNT(?algorithm) AS ?count)
        WHERE {{
            ?workflow ml:hasInput ml:{dataset}.
            ?workflow ml:achieves ?task.
            ?task ml:hasConstraint ?constraint.
            ?constraint rdf:type ml:ConstraintPreprocessingAlgorithm.
            ?constraint ml:on ?algorithm 
        }}
        GROUP BY ?algorithm
        ORDER BY DESC(?count)
        LIMIT 1
        """
        
        results = execute_sparql_query(base_url, repository, query)
        
        if results["results"]["bindings"]:
            algorithm = results["results"]["bindings"][0]["algorithm"]["value"]
            found = True
    
    # If still not found, look for the user's usages of the same intent with a preprocessing algorithm
    if not found:
        query = f"""
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX ml: <http://localhost/8080/intentOntology#>

        SELECT ?algorithm (COUNT(?algorithm) AS ?count)
        WHERE {{
            ml:{user} ml:runs ?workflow.
            ?workflow ml:achieves ?task.
            ?task ml:hasIntent ml:{intent}.
            ?task ml:hasConstraint ?constraint.
            ?constraint rdf:type ml:ConstraintPreprocessingAlgorithm.
            ?constraint ml:on ?algorithm 
        }}
        GROUP BY ?algorithm
        ORDER BY DESC(?count)
        LIMIT 1
        """
        
        results = execute_sparql_query(base_url, repository, query)
        
        if results["results"]["bindings"]:
            algorithm = results["results"]["bindings"][0]["algorithm"]["value"]
            found = True
    
    # If still not found, get the most used preprocessing algorithm overall for the intent
    if not found:
        query = f"""
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX ml: <http://localhost/8080/intentOntology#>

        SELECT ?algorithm (COUNT(?algorithm) AS ?count)
        WHERE {{
            ?task ml:hasIntent ml:{intent}.
            ?task ml:hasConstraint ?constraint.
            ?constraint rdf:type ml:ConstraintPreprocessingAlgorithm.
            ?constraint ml:on ?algorithm 
        }}
        GROUP BY ?algorithm
        ORDER BY DESC(?count)
        LIMIT 1
        """
        
        results = execute_sparql_query(base_url, repository, query)
        
        if results["results"]["bindings"]:
            algorithm = results["results"]["bindings"][0]["algorithm"]["value"]
            found = True
    
    return algorithm.split("#")[-1]


def get_users_with_workflows():

    """
    Retrieves a list of distinct users who have associated workflows.

    Returns:
    - list of str: User identifiers (names) who have at least one workflow.
    """
    
    query = """
    PREFIX ml: <http://localhost/8080/intentOntology#>
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

    SELECT DISTINCT ?user
    WHERE {
      ?user ml:runs ?workflow .
    }
    """
    results = execute_sparql_query(base_url, repository, query)
    users = []
    if results["results"]["bindings"]:
        users = [binding["user"]["value"].split('#')[-1] for binding in results["results"]["bindings"]]
    
    return users


def get_users():
    """
    Retrieves a list of distinct users from the repository.

    Returns:
    - list of str: User identifiers (names) for all users of type `ml:User`.
    """
    global last_inserted_user  # Declare the use of the global variable
    
    query = """
    PREFIX ml: <http://localhost/8080/intentOntology#>
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

    SELECT DISTINCT ?user
    WHERE {
    ?user rdf:type ml:User .
    }
    """
    results = execute_sparql_query(base_url, repository, query)
    users = []
    if results["results"]["bindings"]:
        users = [binding["user"]["value"].split('#')[-1] for binding in results["results"]["bindings"]]


    # Extract numeric part and find the highest number
    user_numbers = []
    for user in users:
        if user.startswith("User"):
            num_part = user[4:] 
            user_numbers.append(int(num_part))
    
    next_user_number = max(user_numbers) if user_numbers else 0
    new_user = f"User{next_user_number}"

    last_inserted_user = new_user
    
    return {
        "users": users,
        "last_inserted_user": last_inserted_user
    }

def add_new_user(email):
    """
    Adds a new user with a unique ID and specified email to the GraphDB repository and updates the last inserted user record.

    Args:
    - email (str): The email address of the new user.

    Returns:
    - str: The ID of the newly added user if successful, or None if there was an error.
    """
    result = get_users()  # Call the updated get_users function
    last_inserted_user = result["last_inserted_user"]   
    repository_id = repository
    url = f"{base_url}/repositories/{repository_id}/statements"

    headers = {
        "Content-Type": "application/sparql-update"
    }

    # print(last_inserted_user)
    numeric_part = ''.join(filter(str.isdigit, last_inserted_user))
    new_user_id = f"User{int(numeric_part) + 1}"

    query = f"""
    PREFIX ml: <http://localhost/8080/intentOntology#>
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

    INSERT DATA {{
        ml:{new_user_id} rdf:type ml:User ;
                ml:email "{email}".    
    }}
    """

    response = requests.post(url, headers=headers, data=query)

    if response.status_code == 204:
        # Update the last inserted user
        last_inserted_user = new_user_id
        print(f"Added new user: {new_user_id}")
        return new_user_id

    else:
        print(f"Error {response.status_code}: {response.text}")
        return None


def find_user_by_email(email):
    """
    Retrieves the user ID associated with the specified email address.

    Args:
    - email (str): The email address of the user to find.

    Returns:
    - str: The user ID if a user with the specified email is found, or None if no such user exists.
    """
    query = f"""
    PREFIX ml: <http://localhost/8080/intentOntology#>
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

    SELECT DISTINCT ?user
    WHERE {{
        ?user rdf:type ml:User .
        ?user ml:email "{email}" .
    }}
    """
    results = execute_sparql_query(base_url, repository, query)
    if results["results"]["bindings"]:
        return results["results"]["bindings"][0]["user"]["value"].split('#')[-1]  # Return the first user directly
    
    return None


def add_new_dataset(dataset_name):
    """
    Adds a new dataset with the specified name to the repository.

    Args:
    - dataset_name (str): The name of the dataset to be added.

    Returns:
    - str: The name of the added dataset if successful, or None if there was an error.
    """
    repository_id = repository
    url = f"{base_url}/repositories/{repository_id}/statements"

    headers = {
        "Content-Type": "application/sparql-update"
    }

    dataset_uri = f"http://localhost/8080/intentOntology#{dataset_name}"

    query = f"""
    PREFIX ns_dmop: <http://www.e-lico.eu/ontologies/dmo/DMOP/DMOP.owl#>
    PREFIX RDF: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

    INSERT DATA {{
        <{dataset_uri}> RDF:type ns_dmop:DataSet .
    }}
    """

    response = requests.post(url, headers=headers, data=query)

    if response.status_code == 204:
        print(f"Added new dataset: {dataset_name}")
        return dataset_name
    else:
        print(f"Error {response.status_code}: {response.text}")
        return None


def add_new_workflow(data):
    """
    Adds a new workflow to the GraphDB repository using the provided data and returns the workflow's name.

    Args:
    - data (dict): The data required to create and add the new workflow.

    Returns:
    - str: The name of the added workflow if successful, or None if there was an error.
    """
    repository_id = repository
    url = f"http://localhost:8080/repositories/{repository_id}/statements"

    headers = {
        "Content-Type": "application/sparql-update"
    }

    insert_query, workflow_uri, user_uri, workflow_name = save_workflow.generate_sparql_insert_query(data)

    response = requests.post(url, headers=headers, data=insert_query)

    if response.status_code == 204:
        print(f"Added new workflow: {workflow_uri} for the user {user_uri}")
        return workflow_name
    else:
        print(f"Error {response.status_code}: {response.text}")
        return None


def get_all_metrics():
    query = f"""
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    PREFIX ml: <http://localhost/8080/intentOntology#>
    
    SELECT DISTINCT ?object
    WHERE {{
      ?subject ml:specifies ?object .
    }}
    ORDER BY ASC(?object)
    """

    results = execute_sparql_query(base_url, repository, query)
    metrics = [binding['object']['value'].split('#')[-1] for binding in results['results']['bindings']]

    return metrics


def get_all_algorithms():
    query = f"""
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    PREFIX ml: <http://localhost/8080/intentOntology#>
    
    
    SELECT DISTINCT ?algorithm
    WHERE {{
            ?algorithm a <http://www.e-lico.eu/ontologies/dmo/DMOP/DMOP.owl#ClassificationModelingAlgorithm>
        }}
    ORDER BY ASC(?algorithm)
    """

    results = execute_sparql_query(base_url, repository, query)
    algorithms = [binding['algorithm']['value'].split('#')[-1] for binding in results['results']['bindings']]

    return algorithms


def get_all_preprocessing_algorithms():
    query = f"""
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    PREFIX ml: <http://localhost/8080/intentOntology#>
    
    
    SELECT DISTINCT ?algorithm
    WHERE {{
            ?algorithm a <http://www.e-lico.eu/ontologies/dmo/DMOP/DMOP.owl#DataProcessingAlgorithm>
        }}
    ORDER BY ASC(?algorithm)
    """

    results = execute_sparql_query(base_url, repository, query)
    preprocessing_algorithms = [binding['algorithm']['value'].split('#')[-1] for binding in results['results']['bindings']]

    return preprocessing_algorithms