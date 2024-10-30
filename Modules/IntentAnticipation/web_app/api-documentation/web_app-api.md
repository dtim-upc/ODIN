# API Documentation

## Authentication API (auth.py)

### Login

**Endpoint:** `/login`

**Method:** `GET`

**Description:** Displays the login page.

**Responses:**
- **200 OK**
  - Content-Type: `text/html`
  - Description: Renders the login page template.

---

**Endpoint:** `/login`

**Method:** `POST`

**Description:** Authenticates a user and logs them in.

**Parameters:**
- **email** (form data, required): The user's email address.
- **password** (form data, required): The user's password.
- **remember** (form data, optional): Boolean flag to remember the user on subsequent visits.

**Responses:**
- **302 Found**
  - Location: `/main/dashboard`
  - Description: Redirects to the dashboard on successful login.
- **302 Found**
  - Location: `/login`
  - Description: Redirects back to the login page with an error message if credentials are incorrect.
---

### Signup

**Endpoint:** `/signup`

**Method:** `GET`

**Description:** Displays the signup page.

**Responses:**
- **200 OK**
  - Content-Type: `text/html`
  - Description: Renders the signup page template.

---

**Endpoint:** `/signup`

**Method:** `POST`

**Description:** Registers a new user.

**Parameters:**
- **email** (form data, required): The user's email address.
- **name** (form data, required): The user's full name.
- **password** (form data, required): The user's password.

**Responses:**
- **302 Found**
  - Location: `/login`
  - Description: Redirects to the login page after successful signup.
- **302 Found**
  - Location: `/signup`
  - Description: Redirects back to the signup page with an error message if the email already exists.
---

### Logout

**Endpoint:** `/logout`

**Method:** `GET`

**Description:** Logs out the current user.

**Responses:**
- **302 Found**
  - Location: `/main/index`
  - Description: Redirects to the main index page after logout.

**Security:**
- Requires authentication (protected by `@login_required` decorator).

---

## Main API (main.py)

### Index

**Endpoint:** `/`

**Method:** `GET`

**Description:** Displays the homepage.

**Responses:**
- **200 OK**
  - Content-Type: `text/html`
  - Description: Renders the index.html template.

---

### Dashboard

**Endpoint:** `/dashboard`

**Method:** `GET`

**Description:** Handles GET requests for various steps of the workflow.

**Parameters:**
- **step** (query parameter, optional): The current step of the workflow (e.g., `workflow/tpot`, `workflow/hyperopt`).

**Responses:**
- **200 OK**
  - Content-Type: `application/json` (for `workflow/tpot` and `workflow/hyperopt`)
  - Description: Returns JSON data from the specified workflow endpoint.
- **200 OK**
  - Content-Type: `text/html`
  - Description: Renders the dashboard page template for other steps.

**Error Responses:**
- **500 Internal Server Error**
  - Description: Returned if there's an error with the external service request.

---

**Method:** `POST`

**Description:** Handles POST requests for various steps of the workflow.

**Parameters:**
- **step** (form data, required): The current step of the workflow (e.g., `upload/fileName`, `capture/predictIntent`).

**Body:**
- **upload/fileName**
  - **file** (file, required): The file to upload.
  - **fileName** (form data, required): The name of the file.

- **capture/predictIntent**
  - **text** (JSON, required): The problem description.
  - **fileName** (JSON, required): The file name.

- **actions/get_intent**
  - **fileName** (JSON, required): The file name.

- **actions/get_intent_metrics**
  - **intent** (JSON, required): The intent to get metrics for.
  - **fileName** (JSON, required): The file name.

- **actions/send_data**
  - **local_file_path** (JSON, required): The local path of the file.
  - **dataset** (JSON, optional): The dataset name.
  - **intent** (JSON, optional): The intent.
  - **metric** (JSON, optional): The metric.
  - **preprocessing** (JSON, optional): The preprocessing type.
  - **hyperparameter** (JSON, optional): The hyperparameter.
  - **hyperparameterValue** (JSON, optional): The hyperparameter value.
  - **algorithm** (JSON, optional): The algorithm.
  - **preprocessingAlgorithm** (JSON, optional): The preprocessing algorithm.
  - **timeLimit** (JSON, optional): The time limit.

- **workflow/save**
  - **pipeline** (JSON, required): The workflow data to save.

**Responses:**
- **200 OK**
  - Description: Various responses based on the step (e.g., file upload confirmation, prediction results).
- **400 Bad Request**
  - Description: Returned if required parameters are missing or invalid.
- **500 Internal Server Error**
  - Description: Returned if there's an error with the external service request.

---

### Documentation

**Endpoint:** `/docs`

**Method:** `GET`

**Description:** Displays the docs.html page.

**Responses:**
- **200 OK**
  - Content-Type: `text/html`
  - Description: Renders the documentation page template.

---

### Profile

**Endpoint:** `/profile`

**Method:** `GET`

**Description:** Displays the user's profile page.

**Responses:**
- **200 OK**
  - Content-Type: `text/html`
  - Description: Renders the profile page template.
- **401 Unauthorized**
  - Description: Returned if the user is not logged in (protected by `@login_required` decorator).
