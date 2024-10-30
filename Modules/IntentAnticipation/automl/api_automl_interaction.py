import os
import matplotlib
import threading
from flask import Flask, request, jsonify, send_from_directory, url_for
from utils import preprocess_data, generate_ml_pipeline
import json

app = Flask(__name__)

# Ensure the uploads directory exists
UPLOAD_FOLDER = './data/uploads'
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

# Lock for controlling access to shared resources
lock = threading.Lock()

# Set the Matplotlib backend
matplotlib.use('Agg')  # Use 'Agg' for non-interactive backend

def read_working_request():
    """
    Reads and returns the configuration data from the 'working_request.json' file located in the preprocessed data directory.
    """
    working_request = './data/preprocessed/working_request.json'
    if not os.path.exists(working_request):
        raise FileNotFoundError(f"{working_request} does not exist.")
    with open(working_request, 'r') as f:
        config = json.load(f)
    return config

@app.route('/', methods=['GET'])
def base_route():
    return jsonify({
            "/send_and_preprocess": "POST - Upload a file and preprocess the data.",
            "/hyperopt": "GET - Run the Hyperopt pipeline and get results.",
            "/tpot": "GET - Run the TPOT pipeline and get results.",
            "/<path:filename>": "GET - Download a file from the server."
    })

@app.route('/send_and_preprocess', methods=['POST'])
def preprocess():
    if 'file' not in request.files:
        return jsonify({"message": "No file part in the request"}), 400
    
    file = request.files['file']
    if file.filename == '':
        return jsonify({"message": "No file selected"}), 400
    
    if file:
        data_file_path = os.path.join(UPLOAD_FOLDER, file.filename)
        file_name = os.path.basename(file.filename).rsplit('.', 1)[0]

        workflow_request_filename = f'{file_name}_workflow_request.json'
        workflow_request_path = os.path.join(UPLOAD_FOLDER, workflow_request_filename)

        file.save(data_file_path)
        data = request.form.to_dict()

        with open(workflow_request_path, 'w') as json_file:
            json.dump(data, json_file, indent=4)

        # Ensure preprocessing functions are thread-safe
        with lock:
            preprocess_data.preprocess_dataset(data_file_path)
            preprocess_data.preprocess_json(workflow_request_path)

        print('Received data:', data)
        print('File saved to:', data_file_path)
        print('Workflow request saved to:', workflow_request_path)

        return jsonify({"message": "File and data received successfully", "file_path": data_file_path}), 200
    else:
        return jsonify({"message": "File not received"}), 400

@app.route('/hyperopt', methods=['GET'])
def hyperopt_route():
    try:
        config = read_working_request()
       
        intent = config.get('intent')
        
        if intent not in ['classification', 'regression']:
            return jsonify({"error": "Invalid intent. Please use 'classification' or 'regression'."}), 400
        
        # Ensure ML pipeline functions are thread-safe
        with lock:
            img_filename, graph_filename, metric_name, metric_value, results_json = generate_ml_pipeline.hyperopt_pipeline_generator(config)
        
        # Construct file URLs
        img_url = url_for('download_file', filename=img_filename, _external=True)
        graph_url = url_for('download_file', filename=graph_filename, _external=True)
        
        return jsonify({
            "results": results_json,
            "image": img_url,
            "graph": graph_url
        }), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/tpot', methods=['GET'])
def tpot_route():
    try:
        config = read_working_request()
       
        intent = config.get('intent')
        
        if intent not in ['classification', 'regression']:
            return jsonify({"error": "Invalid intent. Please use 'classification' or 'regression'."}), 400
        
        
        # Ensure ML pipeline functions are thread-safe
        with lock:
            img_filename, graph_filename, metric_name, metric_value = generate_ml_pipeline.tpot_pipeline_generator(config)
        
        print(img_filename)
        # Construct file URLs
        img_url = url_for('download_file', filename=img_filename, _external=True)
        graph_url = url_for('download_file', filename=graph_filename, _external=True)
        
        return jsonify({
            "image": img_url,
            "graph": graph_url,
            "metric_name": metric_name,
            "metric_value": metric_value
        }),200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/<path:filename>', methods=['GET'])
def download_file(filename):
    directory = os.path.dirname(filename)
    return send_from_directory(directory, os.path.basename(filename))

if __name__ == '__main__':
    app.run(port=8003, debug=True, threaded=True)  # Enable threading for Flask's built-in server
