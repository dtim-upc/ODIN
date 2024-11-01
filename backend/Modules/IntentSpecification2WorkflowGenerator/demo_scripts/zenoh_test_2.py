import os
import sys
from pathlib import Path

import requests

from demo_scripts.generic_functions import register_user_zenoh, login_zenoh, upload_initial_file_zenoh, \
    format_logical_plans

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

########### ABSTRACT PLANS ###########
dataset_name = Path(original_dataset_path).stem
url = "http://localhost:5001/abstract_planner_zenoh"
headers = {
    "accept": "application/json",
    "Content-Type": "application/json",
}
data = {
    "intent_name": intent_name,
    "problem": problem,
    "original_dataset_path": original_dataset_path,
    "downloaded_dataset_path": downloaded_dataset_path,
    "label": label,
    "zenoh_storage_path": f"{use_case}/{folder}/{subfolder}/{dataset_name}",
    "token": token
}

response = requests.post(url, headers=headers, json=data)
abstract_plans = []
intent = []
algorithm_implementations = []
ontology = ""
if response.status_code == 200:
    print("Successfully created abstract plans")
    abstract_plans = response.json()["abstract_plans"]
    intent = response.json()["intent"]
    algorithm_implementations = response.json()["algorithm_implementations"]
    ontology = response.json()["ontology"]
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

########### SELECT & STORE (LOGICAL) PLANS (i.e. RDF files) ###########
url = "http://localhost:5001/store_rdf_zenoh"
headers = {
    "accept": "application/json",
    "Content-Type": "application/json",
}
data = {
    "logical_plans": logical_plans,
    "selected_ids": selected_plans,
    "zenoh_store_path": f"{use_case}/{folder}/{subfolder}/",
    "token": token
}

response = requests.post(url, headers=headers, json=data)
if response.status_code == 200:
    print("Successfully uploaded RDF files to Zenoh")
else:
    print(f"Failed to load RDF files: {response.status_code}")
    print(f"Reason: {response.reason}")
    print(f"Reason: {response.text}")
