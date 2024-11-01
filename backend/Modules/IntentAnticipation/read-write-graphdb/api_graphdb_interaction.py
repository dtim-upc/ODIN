from flask import Flask, request, jsonify
from flask_cors import CORS

from utils.query_graphdb import get_intent, get_metric, get_preprocessing, get_algorithm, get_preprocessing_algorithm, \
    get_users, add_new_user, find_user_by_email, add_new_dataset, add_new_workflow, get_all_metrics, get_all_algorithms, \
    get_all_preprocessing_algorithms

app = Flask(__name__)
CORS(app)

# Dictionary route information
routes_info = {
    "/get_intent": {
        "parameters": ["user", "dataset"],
        "description": "Get the most used intent associated with a user and dataset.",
        "response": {
            "intent": "string"
        },
        "example_usage": "http://localhost:8002/get_intent?user=<user>&dataset=<dataset>"
    },
    "/get_metric": {
        "parameters": ["user", "dataset", "intent"],
        "description": "Get the most used metric associated with a user, dataset, and intent.",
        "response": {
            "metric": "string"
        },
        "example_usage": "http://localhost:8002/get_metric?user=<user>&dataset=<dataset>&intent=<intent>"
    },
    "/get_preprocessing": {
        "parameters": ["user", "dataset", "intent"],
        "description": "Check if data preprocessing is required given a user, dataset and intent.",
        "response": {
            "preprocessing": "boolean"
        },
        "example_usage": "http://localhost:8002/get_preprocessing?user=<user>&dataset=<dataset>&intent=<intent>"
    },
    "/get_algorithm": {
        "parameters": ["user", "dataset", "intent"],
        "description": "Get the most used algorithm associated with a user, dataset, and intent.",
        "response": {
            "algorithm": "string"
        },
        "example_usage": "http://localhost:8002/get_algorithm?user=<user>&dataset=<dataset>&intent=<intent>"
    },
    "/get_preprocessing_algorithm": {
        "parameters": ["user", "dataset", "intent"],
        "description": "Get the most used preprocessing algorithm associated with a user, dataset, and intent.",
        "response": {
            "preprocessing_algorithm": "string"
        },
        "example_usage": "http://localhost:8002/get_preprocessing_algorithm?user=<user>&dataset=<dataset>&intent=<intent>"
    },
    "/get_users": {
        "parameters": [],
        "description": "Retrieve a list of users",
        "response": {
            "last_inserted_user": "string",
            "users": "array",
        },
        "example_usage": "http://localhost:8002/get_users"
    },
    "/add_user": {
        "parameters": ["email"],
        "description": "Add a new user with the given email.",
        "response": {
            "status": "string",
            "message": "string"
        },
        "example_usage": "curl -X POST http://localhost:8002/add_user -H \"Content-Type: application/json\" -d '{\"email\": \"test@example.com\"}'"
    },
    "/get_user_by_email": {
        "parameters": ["email"],
        "description": "Retrieve user by email",
        "response": {
            "user": "string",
        },
        "example_usage": "http://localhost:8002/get_user_by_email?email=user@example.com"
    },
    "/add_dataset": {
        "parameters": ["dataset"],
        "description": "Add a new dataset with the given name.",
        "response": {
            "status": "string",
            "message": "string"
        },
        "example_usage": "curl -X POST http://localhost:8002/add_dataset -H \"Content-Type: application/json\" -d '{\"dataset\": \"new_dataset_name\"}'"
    },
    "/add_workflow": {
    "parameters": ["data"],
    "description": "Adds a new workflow to the GraphDB repository using the provided data.",
    "response": {
        "workflow_name": "string"
    },
    "example_usage": "curl -X POST http://localhost:8002/add_workflow -H \"Content-Type: application/json\" -d '{\"data\": {\"user\": \"example_user\", \"dataset\": \"dataset_name\", \"intent\": \"intent_class\", \"algorithm_constraint\": \"ExampleAlgorithm\", \"hyperparam_constraints\": {\"param1\": \"value1\", \"param2\": \"value2\"}, \"time\":  \"time_value\", \"preprocessor_constraint\": \"ExamplePreprocessor\", \"max_time\": \"max_time_value\", \"pipeline\": {\"preprocs\": [\"ExamplePreprocessor()\"], \"learner\": \"ExampleLearner()\"}, \"metricName\": \"example_metric\", \"metric_value\": \"example metric_value\"}}'"
}

}

@app.route('/', methods=['GET'])
def base_route():
    return jsonify(routes_info), 200


@app.route('/get_intent', methods=['GET'])
def get_intent_route():
    user = request.args.get('user')
    dataset = request.args.get('dataset')
    if not user or not dataset:
        return jsonify({"error": "Missing user or dataset parameter"}), 400

    try:
        intent = get_intent(user, dataset)
        return jsonify({"intent": intent}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/get_metric', methods=['GET'])
def get_metric_route():
    user = request.args.get('user')
    dataset = request.args.get('dataset')
    intent = request.args.get('intent')
    if not user or not dataset or not intent:
        return jsonify({"error": "Missing user, dataset, or intent parameter"}), 400

    try:
        metric = get_metric(user, dataset, intent)
        return jsonify({"metric": metric}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/get_preprocessing', methods=['GET'])
def get_preprocessing_route():
    user = request.args.get('user')
    dataset = request.args.get('dataset')
    intent = request.args.get('intent')
    if not user or not dataset or not intent:
        return jsonify({"error": "Missing user, dataset, or intent parameter"}), 400

    try:
        preprocessing = get_preprocessing(user, dataset, intent)
        return jsonify({"preprocessing": preprocessing}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/get_algorithm', methods=['GET'])
def get_algorithm_route():
    user = request.args.get('user')
    dataset = request.args.get('dataset')
    intent = request.args.get('intent')
    if not user or not dataset or not intent:
        return jsonify({"error": "Missing user, dataset, or intent parameter"}), 400

    try:
        algorithm = get_algorithm(user, dataset, intent)
        return jsonify({"algorithm": algorithm}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/get_preprocessing_algorithm', methods=['GET'])
def get_preprocessing_algorithm_route():
    user = request.args.get('user')
    dataset = request.args.get('dataset')
    intent = request.args.get('intent')
    if not user or not dataset or not intent:
        return jsonify({"error": "Missing user, dataset, or intent parameter"}), 400

    try:
        preprocessing_algorithm = get_preprocessing_algorithm(user, dataset, intent)
        return jsonify({"preprocessing_algorithm": preprocessing_algorithm}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500
            

@app.route('/get_users', methods=['GET'])
def get_users_route():
    try:
        users = get_users()
        return jsonify(users=users), 200
    except Exception as e:
        return jsonify(error=str(e)), 500

@app.route('/add_user', methods=['POST'])
def add_user_route():
    data = request.get_json()
    email = data.get('email')

    if not email:
        return jsonify({"status": "error", "message": "Email is required"}), 400

    new_user_id = add_new_user(email)

    if new_user_id:
        return jsonify({"status": "success", "message": f"Added new user: {new_user_id}"}), 201
    else:
        return jsonify({"status": "error", "message": "Failed to add new user"}), 500

@app.route('/get_user_by_email', methods=['GET'])
def get_user_by_email():
    email = request.args.get('email')
    if not email:
        return jsonify({"error": "Email parameter is required."}), 400

    user = find_user_by_email(email)
    if user:
        return jsonify({"user": user}), 200
    else:
        return jsonify({"message": "User not found."}), 404

@app.route('/add_dataset', methods=['POST'])
def add_dataset_route():
    data = request.get_json()
    dataset_name = data.get('dataset')

    if not dataset_name:
        return jsonify({"status": "error", "message": "Dataset is required"}), 400

    new_dataset = add_new_dataset(dataset_name)

    if new_dataset:
        return jsonify({"status": "success", "message": f"Added new dataset: {new_dataset}"}), 201
    else:
        return jsonify({"status": "error", "message": f"Failed to add {new_dataset} dataset "}), 500


@app.route('/add_workflow', methods=['POST'])
def add_workflow_route():
    pipeline = request.get_json()

    if not pipeline:
        return jsonify({"status": "error", "message": "Pipeline is required"}), 400

    new_workflow = add_new_workflow(pipeline)

    if new_workflow:
        return jsonify({"status": "success", "message": f"Added new workflow: {new_workflow}"}), 201
    else:
        return jsonify({"status": "error", "message": f"Failed to add workflow: {new_workflow}"}), 500

@app.route('/get_all_info', methods=['GET'])
def get_all_info_route():
    metrics = get_all_metrics()
    algorithms = get_all_algorithms()
    preprocessing_algorithms = get_all_preprocessing_algorithms()
    return jsonify({
        "metrics": metrics,
        "algorithms": algorithms,
        "preprocessing_algorithms": preprocessing_algorithms,
    }), 200

if __name__ == '__main__':
    app.run(debug=True, port=8002)
