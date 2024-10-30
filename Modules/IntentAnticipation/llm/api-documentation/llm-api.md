# API Documentation

## Base Route

**GET /**

Returns information about the available routes of the server.

### Response

```json
{
  "/predictIntent": {
    "method": "POST",
    "description": "Classify text and return the intent.",
    "request_body": {
      "text": "string"
    },
    "response": {
      "intent": "string",
      "model": "string"
    },
    "example_usage": {
      "curl": "curl -X POST http://localhost:8001/predictIntent -H \"Content-Type: application/json\" -d '{\"text\": \"Your text to classify\"}'"
    }
  }
}
```

## Predict Intent

**POST /predictIntent**

Classifies the provided text and returns the intent.

### Request Body

```json
{
  "text": "Your text to classify"
}
```

### Response

```json
{
  "intent": "string",
  "model": "string"
}
```

### Example Usage

```bash
curl -X POST http://localhost:8001/predictIntent -H "Content-Type: application/json" -d '{"text": "Your text to classify"}'
```

### Errors

- **400 Bad Request**: If the `text` parameter is missing in the request body.

  ```json
  {
    "error": "Text parameter is required."
  }
  ```

- **500 Internal Server Error**: If there is an issue processing the request.

  ```json
  {
    "error": "Error message describing the issue"
  }
  ```
