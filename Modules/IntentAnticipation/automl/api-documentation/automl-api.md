# API Documentation

## Base Route

**GET /**

Returns information about the available routes of the server.

### Response

```json
{
  "/send_and_preprocess": "POST - Upload a file and preprocess the data.",
  "/hyperopt": "GET - Run the Hyperopt pipeline and get results.",
  "/tpot": "GET - Run the TPOT pipeline and get results.",
  "/<path:filename>": "GET - Download a file from the server."
}
```

## Endpoint Documentation

### /send_and_preprocess

**POST /send_and_preprocess**

Upload a file and preprocess the data.

#### Parameters

- `file`: The file to be uploaded.

#### Request Body

Form-data with file.

#### Response

```json
{
  "message": "File and data received successfully",
  "file_path": "string"
}
```

#### Errors

- **400 Bad Request**: If no file is part of the request or no file is selected.

  ```json
  {
    "message": "No file part in the request"
  }
  ```

  ```json
  {
    "message": "No file selected"
  }
  ```

- **500 Internal Server Error**: If there is an issue with preprocessing the data.

  ```json
  {
    "error": "Error message describing the issue"
  }
  ```

### /hyperopt

**GET /hyperopt**

Run the Hyperopt pipeline and get results.

#### Response

```json
{
  "results": "string",
  "image": "string",
  "graph": "string"
}
```

#### Example Usage

```
http://localhost:8003/hyperopt
```

#### Errors

- **400 Bad Request**: If the intent in the configuration is invalid.

  ```json
  {
    "error": "Invalid intent. Please use 'classification' or 'regression'."
  }
  ```

- **500 Internal Server Error**: If there is an issue processing the request or reading the configuration.

  ```json
  {
    "error": "Error message describing the issue"
  }
  ```

### /tpot

**GET /tpot**

Run the TPOT pipeline and get results.

#### Response

```json
{
  "image": "string",
  "graph": "string",
  "metric_name": "string",
  "metric_value": "string"
}
```

#### Example Usage

```
http://localhost:8003/tpot
```

#### Errors

- **400 Bad Request**: If the intent in the configuration is invalid.

  ```json
  {
    "error": "Invalid intent. Please use 'classification' or 'regression'."
  }
  ```

- **500 Internal Server Error**: If there is an issue processing the request or reading the configuration.

  ```json
  {
    "error": "Error message describing the issue"
  }
  ```

### /<path:filename>

**GET /<path:filename>**

Download a file from the server.

#### Parameters

- `filename`: Name of the file to be downloaded.

#### Response

- The file requested.

#### Example Usage

```
http://localhost:8003/your_file_name.json
```

#### Errors

- **404 Not Found**: If the file does not exist.

  ```json
  {
    "error": "File not found."
  }
  ```

## Additional Information

### Preprocessing

The `preprocess_data` module is used for preprocessing the uploaded data.

### ML Pipeline

The `generate_ml_pipeline` module is responsible for running Hyperopt and TPOT pipelines. It also generates the necessary images and graphs for results.
### File Storage

Files are stored in the `./data/uploads` directory. Preprocessed data and workflow requests are saved in `./data/preprocessed`.

