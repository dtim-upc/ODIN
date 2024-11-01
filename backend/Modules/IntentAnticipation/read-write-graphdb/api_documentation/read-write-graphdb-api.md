# API Documentation

## Base Route

**GET /**

Returns information about the available routes of the server.

### Response

```json
{
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
      "users": "array"
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
      "user": "string"
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
```

## Endpoint Documentation

### /get_intent

**GET /get_intent**

Get the most used intent associated with a user and dataset.

#### Parameters

- `user`: User identifier
- `dataset`: Dataset name

#### Response

```json
{
  "intent": "string"
}
```

#### Example Usage

```
http://localhost:8002/get_intent?user=<user>&dataset=<dataset>
```

#### Errors

- **400 Bad Request**: If `user` or `dataset` parameters are missing.

  ```json
  {
    "error": "Missing user or dataset parameter"
  }
  ```

- **500 Internal Server Error**: If there is an issue processing the request.

  ```json
  {
    "error": "Error message describing the issue"
  }
  ```

### /get_metric

**GET /get_metric**

Get the most used metric associated with a user, dataset, and intent.

#### Parameters

- `user`: User identifier
- `dataset`: Dataset name
- `intent`: Intent name

#### Response

```json
{
  "metric": "string"
}
```

#### Example Usage

```
http://localhost:8002/get_metric?user=<user>&dataset=<dataset>&intent=<intent>
```

#### Errors

- **400 Bad Request**: If `user`, `dataset`, or `intent` parameters are missing.

  ```json
  {
    "error": "Missing user, dataset, or intent parameter"
  }
  ```

- **500 Internal Server Error**: If there is an issue processing the request.

  ```json
  {
    "error": "Error message describing the issue"
  }
  ```

### /get_preprocessing

**GET /get_preprocessing**

Check if data preprocessing is required given a user, dataset, and intent.

#### Parameters

- `user`: User identifier
- `dataset`: Dataset name
- `intent`: Intent name

#### Response

```json
{
  "preprocessing": "boolean"
}
```

#### Example Usage

```
http://localhost:8002/get_preprocessing?user=<user>&dataset=<dataset>&intent=<intent>
```

#### Errors

- **400 Bad Request**: If `user`, `dataset`, or `intent` parameters are missing.

  ```json
  {
    "error": "Missing user, dataset, or intent parameter"
  }
  ```

- **500 Internal Server Error**: If there is an issue processing the request.

  ```json
  {
    "error": "Error message describing the issue"
  }
  ```

### /get_algorithm

**GET /get_algorithm**

Get the most used algorithm associated with a user, dataset, and intent.

#### Parameters

- `user`: User identifier
- `dataset`: Dataset name
- `intent`: Intent name

#### Response

```json
{
  "algorithm": "string"
}
```

#### Example Usage

```
http://localhost:8002/get_algorithm?user=<user>&dataset=<dataset>&intent=<intent>
```

#### Errors

- **400 Bad Request**: If `user`, `dataset`, or `intent` parameters are missing.

  ```json
  {
    "error": "Missing user, dataset, or intent parameter"
  }
  ```

- **500 Internal Server Error**: If there is an issue processing the request.

  ```json
  {
    "error": "Error message describing the issue"
  }
  ```

### /get_preprocessing_algorithm

**GET /get_preprocessing_algorithm**

Get the most used preprocessing algorithm associated with a user, dataset, and intent.

#### Parameters

- `user`: User identifier
- `dataset`: Dataset name
- `intent`: Intent name

#### Response

```json
{
  "preprocessing_algorithm": "string"
}
```

#### Example Usage

```
http://localhost:8002/get_preprocessing_algorithm?user=<user>&dataset=<dataset>&intent=<intent>
```

#### Errors

- **400 Bad Request**: If `user`, `dataset`, or `intent` parameters are missing.

  ```json
  {
    "error": "Missing user, dataset, or intent parameter"
  }
  ```

- **500 Internal Server Error**: If there is an issue processing the request.

  ```json
  {
    "error": "Error message describing the issue"
  }
  ```

### /get_users

**GET /get_users**

Retrieve a list of users.

#### Response

```json
{
  "last_inserted_user": "string",
  "users": "array"
}
```

#### Example Usage

```
http://localhost:8002/get_users
```

#### Errors

- **500 Internal Server Error**: If there is an issue processing the request.

  ```json
  {
    "error": "Error message describing the issue"
  }
  ```

### /add_user

**POST /add_user**

Add a new user with the given email.

#### Parameters

- `email`: User's email address

#### Request Body

```json
{
  "email": "string"
}
```

#### Response

```json
{
  "status": "string",
  "message": "string"
}
```

#### Example Usage

```
curl -X POST http://localhost:8002/add_user -H "Content-Type: application/json" -d '{"email": "test@example.com"}'
```

#### Errors

- **400 Bad Request**: If `email` parameter is missing.

  ```json
  {
    "status": "error",
    "message": "Email is required"
  }
  ```

- **500 Internal Server Error**: If there is an issue processing the request.

  ```json
  {
    "status": "error",
    "message": "Failed to add new user"
  }
  ```

### /get_user_by_email

**GET /get_user_by_email**

Retrieve user by email.

#### Parameters

- `email`: User's email address

#### Response

```json
{
  "user": "string"
}
```

#### Example Usage

```
http://localhost:8002/get_user_by_email?email=user@example.com
```

#### Errors

- **400 Bad Request**: If `email` parameter is missing.

  ```json
  {
    "error": "Email parameter is required."
  }
  ```

- **404 Not Found**: If the user is not found.

  ```json
  {
    "message": "User not found."
  }
  ```

### /add_dataset

**POST /add_dataset**

Add a new dataset with the given name.

#### Parameters

- `dataset`: Name of the dataset

#### Request Body

```json
{
  "dataset": "string"
}
```

#### Response

```json
{
  "status": "string",
  "message": "string"
}
```

#### Example Usage

```
curl -X POST http://localhost:8002/add_dataset -H "Content-Type: application/json" -d '{"dataset": "new_dataset_name"}'
```

#### Errors

- **400 Bad Request**: If `dataset` parameter is missing.

  ```json
  {
    "status": "error",
    "message": "Dataset is required"
  }
  ```

- **500 Internal Server Error**: If there is an issue processing the request.

  ```json
  {
    "status": "error",
    "message": "Failed to add new dataset"
  }
  ```

### /add_workflow

**POST /add_workflow**

Adds a new workflow to the GraphDB repository using the provided data.

#### Parameters

- `data`: Workflow data

#### Request Body

```json
{
  "data": {
    "user": "string",
    "dataset": "string",
    "intent": "string",
    "algorithm_constraint": "string",
    "hyperparam_constraints": {
    },
    "time": "string",
    "preprocessor_constraint": "string",
    "max_time": "string",
    "pipeline": {
      "preprocs": ["string"],
      "learner": "string"
    },
    "metricName": "string",
    "metric_value": "string"
  }
}
```

#### Response

```json
{
  "status": "string",
  "message": "string"
}
```

#### Example Usage

```
curl -X POST http://localhost:8002/add_workflow -H "Content-Type: application/json" -d '{"data": {"user": "john_doe", "dataset": "iris", "intent": "Classification", "algorithm_constraint": "SVC", "hyperparam_constraints": {}, "time": 100, "max_time": 300, "preprocessor_constraint": "StandardScaler", "pipeline": {"preprocs": ["StandardScaler()"], "learner": "SVC(C=1.0)"}, "metricName": "Accuracy", "metric_value": 0.90}}'
```

#### Errors

- **400 Bad Request**: If `data` parameter is missing.

  ```json
  {
    "status": "error",
    "message": "Pipeline is required"
  }
  ```

- **500 Internal Server Error**: If there is an issue processing the request.

  ```json
  {
    "status": "error",
    "message": "Failed to add workflow"
  }
  ```

