import requests
from pathlib import Path


def register_user_zenoh(username, password, email):
    url = "http://localhost:5000/auth/register"
    headers = {
        "accept": "application/json",
        "Content-Type": "application/x-www-form-urlencoded"
    }
    data = {
        "username": username,
        "password": password,
        "email": email
    }

    response = requests.post(url, headers=headers, data=data)
    if response.status_code == 201:
        print("Successfully registered")
    elif response.status_code == 409:
        print("User already exists")
    else:
        print(f"Failed to register: {response.status_code}")
        print(f"Reason: {response.reason}")


def login_zenoh(username, password):
    url = "http://localhost:5000/auth/login"
    headers = {
        "accept": "application/json",
        "Content-Type": "application/x-www-form-urlencoded"
    }
    data = {
        "username": username,
        "password": password,
    }

    response = requests.post(url, headers=headers, data=data)
    token = ""
    if response.status_code == 200:
        print("Successfully logged in")
        token = response.json()["tokens"]["access"]
    else:
        print(f"Failed to log in: {response.status_code}")
        print(f"Reason: {response.reason}")
    return token


def upload_initial_file_zenoh(use_case, folder, subfolder, dataset_path, token):
    dataset_name = Path(dataset_path).stem
    url = f"http://localhost:5000/file/{use_case}/{folder}/{subfolder}/{dataset_name}"
    headers = {
        "accept": "application/json",
        "Authorization": f"Bearer {token}"
    }
    # Define the file to be uploaded
    files = {
        "file": (dataset_name, open(dataset_path, "rb"), "application/pdf")
    }

    response = requests.post(url, headers=headers, files=files)
    if response.status_code == 200:
        print("Successfully uploaded")
    else:
        print(f"Failed to upload: {response.status_code}")
        print(f"Reason: {response.reason}")


def format_logical_plans(logical_plans_unformatted):
    logical_plans = []

    def remove_last_part(input_string):
        parts = input_string.split(' ')
        if len(parts) > 1:
            parts.pop()  # Remove the last part
            return ' '.join(parts)
        else:
            return input_string  # Return the original string if there's only one part

    keys = list(logical_plans_unformatted.keys())

    for key in keys:
        found = False
        plan = {
            'id': key,
            'selected': False,
            'plan': logical_plans_unformatted[key]['logical_plan'],
            'graph': logical_plans_unformatted[key]['graph']
        }
        for logPlan in logical_plans:
            if logPlan['id'] == remove_last_part(key):
                logPlan['plans'].append(plan)
                found = True
                break
        if not found:
            logical_plans.append({
                'id': remove_last_part(key),
                'selected': False,
                'plans': [plan]
            })

    return logical_plans
