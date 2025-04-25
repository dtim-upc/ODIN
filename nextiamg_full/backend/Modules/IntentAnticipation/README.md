# extremexp-prototype
## System Architecture

The prototype is designed as a multi-server communication system, where users interact with the main application, which is hosted and executed by the **web_app server**.

Currently, **SQLite** functions as the database linked to the web_app server, handling user login information, datasets uploaded by users, and intent predictions received from the LLM server.

The **llm server** is accessed by the main app, which sends user-inputted text for processing. This text is classified by a specific LLM into an analytical intent, which is then presented to the user within the main app.

The **read-write-graphdb server** manages storage and retrieval of information from the **GraphDB**, which maintains the knowledge base.

Finally, the **automl server** processes requests from the main app, including the working dataset, intent predictions, and ML pipeline constraints. Based on this information, it generates ML pipelines using tools like TPOT or Hyperopt and returns the results to the main app.

![System Components](static/system-components.png)

## Project Setup

### Set up your virtual environment
 ```bash
  pip install virtualenv
  python3.11 -m venv prototype
  source prototype/bin/activate
```
### Install and Configure GraphDB
- Refer to https://graphdb.ontotext.com/documentation/10.7/how-to-install-graphdb.html for installation instructions.
- Once you install GraphDB, set the port to **7200** for the GraphDB instance.
- Start GraphDB server.

### Populate GraphDB repository with the KnowledgeBase.nt
- Install requests library :
```bash
pip install requests
```
- Create GraphDB repository (Default name is test-repo. Refer to the create_graphdb_repository.py to change settings):
```bash
python read-write-graphdb/utils/create_graphdb_repository.py
```
- Load data into created repository (local file path to GraphDB server directory is required):
```bash
python read-write-graphdb/utils/import_file_to_graphdb_repository.py <$user.home/graphdb-import/>
```

### Store your API keys
Make sure you have ```.env``` file in the **llm folder** with your API keys stored.
  ```
  OPENAI_API_KEY=<YOUR OPENAI_API_KEY>
  LlamaAPI_KEY=<LlamaAPI_KEY>
  ```
For more information on how to obtain API Keys, refer to : [OpenAI](https://platform.openai.com/docs/quickstart) and [LLama AI](https://docs.llama-api.com/api-token).


### Install OpenMP Library
  ```
brew install libomp
  ```

### Start servers
```bash
chmod +x start_servers.sh
./start_servers.sh
```
The **start_servers.sh** script will automatically install the necessary requirements and start the following servers:
- web_app
- llm
- read-write-graphdb
- automl

For detailed information about the API routes and functionality for each server, please refer to the specific **Markdown documentation** files located in their respective folders.

(**optional**) You can also **start one server at a time**, installing the necessary packages and running the main script.

As an example, the following commands will start the *llm server*.
```bash
python3.11 -m pip install -r llm/requirements.txt
python3.11 llm/api_llm_interaction.py
```
### Navigate to the Main Application (web_app)
   In order to engage with application you should navigate your web browser to
`http://localhost:8000` or you can use `curl`.

```bash
curl -X GET http://localhost:8000
```

## Usage Guide
[[Watch Video]](https://drive.google.com/file/d/1hEKr7KGFvUbbweNEbMF8r9jD_QV_9tU4/view?usp=sharing)

For between server communication, refer to the diagram  ```static/diagram-drawio.png```.

## Enhancing the Server Implementation

The current implementation can be further improved by adjusting or adding new functionalities to the existing servers. Additionally, more servers can be linked to the prototype.

### Setting Up and Interacting with a New Server

1. Create a folder named `new_server` (or another name of your choice).

2. Inside the `new_server` folder, create the following files:
-  `requirements.txt` – This file will list the dependencies required for the server.
- `api_interaction.py` – This script will contain the main server code.

3. If you have specific functions you'd like to define and use within `api_interaction.py`, you can store these in a `utils` folder within `new_server`.
4. Inside the `utils` folder, create Python files for your utility functions. For example, `helpers.py`.
   
```python
def greet(name):
    return f"Hello, {name}!"
```
5. Import and use the functions defined in the utils folder in your `api_interaction.py`
      
```python
from flask import Flask
from utils.helpers import greet

app = Flask(__name__)

@app.route('/')
def hello_world():
    return greet('World')

if __name__ == '__main__':
    app.run(port=8004, debug=True) # specify the port
```
In this example, the Flask application is configured to run on port 8004 and will display "Hello, World!" when accessed at the root URL.

6. **Make requests to the new server**

Now you can define route in the main application make requests to the new server:

```python
@main.route('/call-server')
def call_server():
    try:
        response = requests.get('http://localhost:8004')
        return jsonify({
            'status': response.status_code,
            'data': response.text
        })
    except requests.RequestException as e:
        return jsonify({'error': str(e)}), 500
```
You can test this route my starting main app and the new server and accessing the url http://localhost:8000/call-server in your browser.

### Adding New Functionalities to the Existing Servers

Adding new functionalities can be achieved in the same manner: 

1. **Add a New Function**: Create a new function in the `utils` module.

2. **Import the Function**: Import this function into the API script of the server.

3. **Define a New Route**: In the API script, define a new route that uses the imported function.

4. **Update the Main App**: In the main application, add a new route that makes requests to the server using the new endpoint you defined.
