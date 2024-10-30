import hashlib
from flask import Blueprint, render_template, jsonify, request, abort
from flask_login import login_required, current_user
import requests
import logging
from . import db
from .models import Dataset
from .models import Intent
import os
from werkzeug.utils import secure_filename

logging.basicConfig(level=logging.DEBUG)

main = Blueprint('main', __name__)


UPLOAD_FOLDER = './uploads'
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)

def compute_file_hash(file_path):
    """Compute SHA-256 hash of the file at the given path."""
    sha256 = hashlib.sha256()
    with open(file_path, 'rb') as f:
        for byte_block in iter(lambda: f.read(4096), b""):
            sha256.update(byte_block)
    return sha256.hexdigest()


@main.route('/')
def index():
    return render_template('index.html')

@main.route('/dashboard', methods=['GET', 'POST'])
def dashboard():
    step = request.args.get('step', 'dashboard')

    if request.method == 'GET':
            
        if step == 'workflow/tpot':
            try:
                response = requests.get('http://localhost:8003/tpot')
                response.raise_for_status()
                return jsonify(response.json()), 200
            except requests.RequestException as e:
                return jsonify({'error': str(e)}), 500

        elif step == 'workflow/hyperopt':
            try:
                response = requests.get('http://localhost:8003/hyperopt')
                response.raise_for_status()
                return jsonify(response.json()), 200
            except requests.RequestException as e:
                return jsonify({'error': str(e)}), 500
    
        else:
                return render_template('dashboard.html', step=step)
            
    elif request.method == 'POST':
        # Handle POST request
        
        if step == 'upload/fileName':
            file = request.files.get('file')
            file_name = request.form.get('fileName')
            if file and file_name:
                print(current_user.email)
                print('Received file name:', file_name)

                # Secure the file name and save the file
                filename = secure_filename(file_name)
                file_path = os.path.join(UPLOAD_FOLDER, filename)
                file.save(file_path)

                # Compute hash of the saved file
                file_hash = compute_file_hash(file_path)
                
                # Check for existing entry with the same hash
                existing_entry = Dataset.query.filter_by(email=current_user.email, file_hash=file_hash).first()

                if not existing_entry:
                    new_dataset = Dataset(email=current_user.email, dataset_name=file_name, file_hash=file_hash)
                    db.session.add(new_dataset)
                    db.session.commit()
                    try:
                        response = requests.post('http://localhost:8002/add_dataset', json={'dataset': file_name.rsplit('.', 1)[0]})
                        response.raise_for_status()  # Raises an HTTPError for bad responses
                        print(f'Dataset {file_name} created successfully and added to GraphDB. Saved in {file_path}')
                    except requests.exceptions.RequestException as e:
                        print(f'Dataset created but failed to add to GraphDB: {str(e)}')

                return jsonify({'message': 'File name received', 'fileName': file_name, 'local_file_path': file_path}), 200
            else:
                return jsonify({'error': 'No file name or file provided'}), 400


        
        elif step == 'capture/predictIntent':

            data = request.get_json()
            problem_desc = data.get('text')
            file_name = data.get('fileName')
            if problem_desc and file_name:
                try:
                    response = requests.post('http://localhost:8001/predictIntent', json={'text': problem_desc})
                    response.raise_for_status()
                    response_data = response.json()

                    intent=response_data.get('intent')
                    model=response_data.get('model')

                    intent_instance = Intent(
                        email=current_user.email,
                        dataset_name=file_name,  # Use the file name as the dataset name
                        problem_description=problem_desc,
                        model = model,
                        intent=intent
                    )
                    db.session.add(intent_instance)
                    db.session.commit()

                    return jsonify(response_data), 200                
                except requests.exceptions.RequestException as e:
                    return jsonify({'error': f'Intent Prediction Failed: {str(e)}'}), 500
            else:
                return jsonify({'error': 'File name or analytical problem description is not defined'}), 400
        
        elif step == 'actions/get_intent':
            data = request.get_json()
            file_name = data.get('fileName')

            response_data = {
                'intents': [], 
            }

            try:
                response_user = requests.get(f'http://localhost:8002/get_user_by_email?email={current_user.email}')
                response_user.raise_for_status()  # Raises an HTTPError for bad responses
                user_data = response_user.json()
                user = user_data.get('user')
                print(user)
                dataset = file_name.rsplit('.', 1)[0]
                
                try:
                    response = requests.get(f'http://localhost:8002/get_intent?user={user}&dataset={dataset}')
                    response_user.raise_for_status()  # Raises an HTTPError for bad responses
                    intent_response= response.json()
                    intent = intent_response.get('intent')
                    response_data['intents'] = [intent]
                    print(response_data)

                except requests.exceptions.RequestException as e:
                    print(f'Error: {str(e)}')

                
            except requests.exceptions.RequestException as e:
                print(f'Error: {str(e)}')

            return jsonify(response_data)
        
        elif step == 'actions/get_intent_metrics':
            data = request.get_json()
            intent = data.get('intent')
            file_name = data.get('fileName')
            dataset = file_name.rsplit('.', 1)[0]

            try:
                server_url = "http://localhost:8002"
                response_user = requests.get(f'{server_url}/get_user_by_email', params={'email': current_user.email})
                response_user.raise_for_status()
                user_data = response_user.json()
                user = user_data.get('user')

                if not user:
                    return jsonify({"error": "User not found"}), 404
            except requests.RequestException as e:
                return jsonify({"error": f"Error fetching user: {str(e)}"}), 500

            # Create a dictionary to store results
            results = {}

            # Define the endpoints
            endpoints = {
                'algorithm': '/get_algorithm',
                'metric': '/get_metric',
                'preprocessing': '/get_preprocessing',
                'preprocessing_algorithm': '/get_preprocessing_algorithm'
            }

            # Make GET requests to each endpoint
            for key, endpoint in endpoints.items():
                try:
                    response = requests.get(f"{server_url}{endpoint}", params={
                        'user': user,
                        'dataset': dataset,  # Assuming dataset is the same as file_name
                        'intent': intent
                    })
                    response.raise_for_status()
                    results[key] = response.json().get(key)
                except requests.RequestException as e:
                    results[key] = f""

            # Return the results as a JSON response
            return jsonify(results)  
            
        elif step == 'actions/send_data':
            data = request.json
            print('Received data:', data)
            
            # Extract the local file path from the data
            local_file_path = data.get('local_file_path')
            
            # Ensure the file exists
            if not local_file_path or not os.path.exists(local_file_path):
                return jsonify({"message": "File not found"}), 400
            
            # URL of the preprocessing server
            automl_server_url = 'http://localhost:8003/send_and_preprocess'
            
            # Prepare the files and data for the POST request
            files = {'file': open(local_file_path, 'rb')}
            payload = {
                'dataset': data.get('dataset'),
                'intent': data.get('intent'),
                'metric': data.get('metric'),
                'preprocessing': data.get('preprocessing'),
                'hyperparameter': data.get('hyperparameter'),
                'hyperparameterValue': data.get('hyperparameterValue'),
                'algorithm': data.get('algorithm'),
                'preprocessingAlgorithm': data.get('preprocessingAlgorithm'),
                'timeLimit': data.get('timeLimit')
            }
            
            try:
                # Send the POST request to the preprocessing server
                response = requests.post(automl_server_url, files=files, data=payload)
                
                # Check the response from the other server
                if response.status_code == 200:
                    return jsonify({"message": "Workflow data and file sent successfully"}), 200
                else:
                    return jsonify({"message": "Failed to send data to preprocessing server", "error": response.text}), response.status_code
            except requests.RequestException as e:
                return jsonify({"message": "Request to preprocessing server failed", "error": str(e)}), 500
            finally:
                files['file'].close()

        elif step == 'workflow/save':
            try:
                # Get the current user's email
                response_user = requests.get(f'http://localhost:8002/get_user_by_email?email={current_user.email}')
                response_user.raise_for_status()  # Raises an HTTPError for bad responses
                
                # Extract user information
                user_data = response_user.json()
                user = user_data.get('user')
                print(f"User retrieved: {user}")

                # Ensure the 'user' field is added to the data
                data = request.json

                data['user'] = user

                # Prepare the data payload
                pipeline = {
                    "user": data['user'],
                    "dataset": data.get('dataset'),
                    "intent": data.get('intent'),
                    "algorithm_constraint": data.get('algorithm_constraint'),
                    "hyperparam_constraints": data.get('hyperparam_constraints', {}),
                    "time": data.get('time'),
                    "preprocessor_constraint": data.get('preprocessor_constraint'),
                    "max_time": data.get('max_time'),
                    "pipeline": data.get('pipeline', {}),
                    "metricName": data.get('metricName'),
                    "metric_value": data.get('metric_value')
                }

                # Debug: Print payload being sent
                print(f"Payload being sent: {pipeline}")

                # Send POST request to add workflow
                response = requests.post('http://localhost:8002/add_workflow', json=pipeline)
                response.raise_for_status()  # Raises an HTTPError for bad responses
                
                # Success message
                return jsonify({"message": "Workflow data sent successfully"}), 200

            except requests.exceptions.RequestException as e:
                # Error handling
                return jsonify({'error': str(e)}), 500

        else:
            abort(405)  # Method not allowed for other steps

@main.route('/docs')
def docs():
    return render_template('docs.html')

@main.route('/profile')
@login_required
def profile():
    return render_template('profile.html', name=current_user.name)

    