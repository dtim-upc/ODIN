import os
import sys

import requests
from pathlib import Path
from rdflib import Graph

from demo_scripts.generic_functions import register_user_zenoh, login_zenoh, upload_initial_file_zenoh, format_logical_plans

########### Required information ###########
# ZENOH login info, these can be whatever
username = "user"
password = "user"
email = "user"

# ZENOH storage info, these can be whatever
use_case = "UC1"
folder = "F1"
subfolder = "SF1"

# Dataset info
script_dir = os.path.dirname(os.path.abspath(sys.argv[0]))
original_dataset_path = os.path.join(script_dir, "countries.csv")  # ABSOLUTE Path of the file to be uploaded to Zenoh
downloaded_dataset_path = os.path.join(script_dir, "countries_download.csv")  # ABSOLUTE Path to store the file coming from Zenoh

# Intent info
label = "IncomeGroup"  # Has to be one attribute from the dataset
intent_name = "Intent_1"
problem = "https://extremexp.eu/ontology/cbox#Classification"  # Only Classification
plan_ids = ["https://extremexp.eu/ontology/cbox#DecisionTree"]  # DecisionTree, SVM or NN (more than one are allowed)
# Can be Decision Tree <0-3>, Hypertangent/Polynomial/Rbf Svm <0-24>, Convolutional/Feedforward/Recurrent/Lstm Nn <0-24>
selected_plans = ["Decision Tree 0", "Decision Tree 1", "Decision Tree 2"]

########### REGISTER USER ###########
register_user_zenoh(username, password, email)

########### LOGIN ###########
token = login_zenoh(username, password)

########### UPLOADING INITIAL FILE (not necessary unless there is no file in Zenoh) ###########
upload_initial_file_zenoh(use_case, folder, subfolder, original_dataset_path, token)

########### GETTING INITIAL FILE ###########
dataset_name = Path(original_dataset_path).stem
url = f"http://localhost:5000/file/{use_case}/{folder}/{subfolder}/{dataset_name}"
headers = {
    "accept": "application/json",
    "Authorization": f"Bearer {token}"
}

response = requests.get(url, headers=headers)
if response.status_code == 200:
    print("Successfully downloaded")
    with open(downloaded_dataset_path, 'wb') as file:
        file.write(response.content)
else:
    print(f"Failed to download: {response.status_code}")
    print(f"Reason: {response.reason}")

########### ABSTRACT PLANS ###########
#### ANNOTATE DATASET ####
url = "http://localhost:5001/annotate_dataset"
headers = {
    "Content-Type": "application/json",
}
data = {
    "path": downloaded_dataset_path,
    "label": label,
}

response = requests.post(url, headers=headers, json=data)
ontology = []
data_product_uri = ""
if response.status_code == 200:
    print("Successfully annotated data")
    ontology = response.json()["ontology"]
    data_product_uri = response.json()["data_product_uri"]
else:
    print(f"Failed to annotate data: {response.status_code}")
    print(f"Reason: {response.reason}")
    print(f"Reason: {response.text}")

#### CREATE ABSTRACT PLANS ####
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

########### STORE THE RDF REPRESENTATION OF THE PLANS ###########
rdf_file = Graph()
for plan in logical_plans:
    for planplan in plan["plans"]:
        if planplan["id"] in selected_plans:  # select plan
            rdf_file.parse(data=planplan["graph"], format="turtle")
            rdf_file.serialize(destination=f"{planplan["id"]}.rdf", format='turtle')

            url = f"http://localhost:5000/file/{use_case}/{folder}/{subfolder}/{planplan["id"]}"
            headers = {
                "accept": "application/json",
                "Authorization": f"Bearer {token}"
            }
            # Define the file to be uploaded
            files = {
                "file": (planplan["id"], open(f"{planplan["id"]}.rdf", "rb"), "application/pdf")
            }

            response = requests.post(url, headers=headers, files=files)
            if response.status_code == 200:
                print(f"Successfully uploaded RDF file {planplan["id"]}")
            else:
                print(f"Failed to upload: {response.status_code}")
                print(f"Reason: {response.reason}")