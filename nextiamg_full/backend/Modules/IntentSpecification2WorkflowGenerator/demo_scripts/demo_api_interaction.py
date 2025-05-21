import os
import sys

import requests
from rdflib import Graph

from demo_scripts.generic_functions import format_logical_plans

# Data info
script_dir = os.path.dirname(os.path.abspath(sys.argv[0]))
dataset_path = os.path.join(script_dir, "countries.csv")  # ABSOLUTE Path of the dataset to use

# Base intent info
problem = "https://extremexp.eu/ontology/cbox#Classification"  # As of now, only Classification
label = "IncomeGroup"  # Label of the attribute to be predicted. Has to be one attribute from the dataset
intent_name = "Intent_1"  # Can be anything

# Algorithm(s) to implement the abstract plans with
plan_ids = ["https://extremexp.eu/ontology/cbox#DecisionTree"]  # DecisionTree, SVM or NN (more than one are allowed)

# These plans are a subset of the resulting logical plans, which will be stored as RDF files
# Can be Decision Tree <0-3>, Hypertangent/Polynomial/Rbf Svm <0-24>, Convolutional/Feedforward/Recurrent/Lstm Nn <0-24>,
# depending on what was selected as plan_ids
selected_plans = ["Decision Tree 0", "Decision Tree 2"]


########### GET LIST OF PROBLEMS ###########
url = "http://localhost:5001/problems"
response = requests.get(url)
problems = []
if response.status_code == 200:
    print("Successfully obtained the available problems")
    problems = response.json()
    print(f"List of problems:{problems}")
else:
    print(f"Failed to create abstract plans: {response.status_code}")
    print(f"Reason: {response.reason}")
    print(f"Reason: {response.text}")

########### ANNOTATE DATASET ###########
url = "http://localhost:5001/annotate_dataset"
headers = {
    "Content-Type": "application/json",
}
data = {
    "path": dataset_path,
    "label": label,
}

response = requests.post(url, headers=headers, json=data)
ontology = ""
data_product_uri = ""
if response.status_code == 200:
    print("Successfully annotated data")
    ontology = response.json()["ontology"]
    data_product_uri = response.json()["data_product_uri"]
else:
    print(f"Failed to annotate data: {response.status_code}")
    print(f"Reason: {response.reason}")
    print(f"Reason: {response.text}")

########### CREATE ABSTRACT PLANS ###########
url = "http://localhost:5001/abstract_planner"
headers = {
    "accept": "application/json",
    "Content-Type": "application/json",
}
data = {
    "intent_name": intent_name,
    "dataset": data_product_uri,
    "problem": problem,
    "ontology": ontology
}

response = requests.post(url, headers=headers, json=data)
abstract_plans = []
intent = []
algorithm_implementations = []
if response.status_code == 200:
    print("Successfully created abstract plans")
    abstract_plans = response.json()["abstract_plans"]
    intent = response.json()["intent"]
    algorithm_implementations = response.json()["algorithm_implementations"]
    print("List of possible implementations for the defined problem:")
    for abstract_plan in abstract_plans:
        print("\t" + abstract_plan)
else:
    print(f"Failed to create abstract plans: {response.status_code}")
    print(f"Reason: {response.reason}")
    print(f"Reason: {response.text}")

########### LOGICAL PLANS ###########
url = "http://localhost:5001/logical_planner"
headers = {
    "accept": "application/json",
    "Content-Type": "application/json",
}
data = {
    "plan_ids": plan_ids,
    "intent_graph": intent,
    "ontology": ontology,
    "algorithm_implementations": algorithm_implementations
}

response = requests.post(url, headers=headers, json=data)
logical_plans = []
logical_plans_unformatted = []
if response.status_code == 200:
    print("Successfully created logical plans")
    logical_plans_unformatted = response.json()
else:
    print(f"Failed to create logical plans: {response.status_code}")
    print(f"Reason: {response.reason}")
    print(f"Reason: {response.text}")

logical_plans = format_logical_plans(logical_plans_unformatted)
print("List of logical plans:")
for logical_plan in logical_plans:
    for logical_plan_implementation in logical_plan["plans"]:
        print("\t" + logical_plan_implementation["id"])


########### STORE RDF FILE ###########
rdf_file = Graph()
rdf_path = ""
for logical_plan in logical_plans:
    for logical_plan_implementation in logical_plan["plans"]:
        if logical_plan_implementation["id"] in selected_plans:
            rdf_file.parse(data=logical_plan_implementation["graph"], format="turtle")
            path_to_store_rdf_file = os.path.join(rdf_path, f"{logical_plan_implementation["id"]}.rdf")
            rdf_file.serialize(destination=path_to_store_rdf_file, format='turtle')


########### GENERATE KNIME REPRESENTATION ###########
url = "http://localhost:5001/workflow_plans/knime"
headers = {
    "accept": "application/json",
    "Content-Type": "application/json",
}

for logical_plan in logical_plans:
    for logical_plan_implementation in logical_plan["plans"]:
        if logical_plan_implementation["id"] in selected_plans:
            data = {
                "graph": logical_plan_implementation["graph"],
                "ontology": ontology,
            }
            response = requests.post(url, headers=headers, json=data)