# NextiaMG - ODIN (Cyclops Integration)

This repository contains the **NextiaMG** (Mappings Generation) component, a part of the larger ODIN project. It focuses on generating mappings between local data sources and a global schema, specifically tailored for integration with Cyclops. This version is optimized for integration into a larger project and requires some components from the **ODIN** project, but we've omitted the unrelated parts for simplicity.

ODIN's backend is developed in **Java** and utilizes the **Spring framework**. As the backend for ODIN, its primary role is to **orchestrate** calls to various Nextia libraries within the department. **Each Nextia** library serves a distinct **functionality**, and ODIN also manages application persistence.

ODIN's **persistence** is bifurcated into two components: graph persistence, currently using Jena, and relational database persistence using ORM with the embedded H2 database.


These are the main modules from ODIN that are necessary for the **NextiaMG** component:
- **NextiaCore:** Contains domain class definitions for the system and serves as a cross-cutting library. It lacks business logic, focusing solely on Plain Old Java Object (POJO) classes.

- **NextiaDataLayer:** Manages an intermediate persistence layer for accessing data from uploaded datasets.

- **NextiaBS:** Extracts schemas from datasets loaded into ODIN and generates wrappers, which are SQL queries demonstrating how to access the original dataset's data.

- **NextiaJD:** Join discovery. Responsible for automatically discovering attribute alignments from datasets.

- **NextiaDI:** Handles data integration. Given two schemas and alignments, it integrates them and generates the corresponding integrated graph.

The core purpose of this repository is to provide the necessary logic and configurations for integrating **NextiaMG** into your existing systems through Docker.

## NextiaMG Component

The **NextiaMG** component is responsible for:
- **Mapping generation**: Creating mappings between local data sources and the global schema using the integrated graph. In this initial version the supported mappings language is **R2RML**.


### Integration with Docker

We’ve streamlined the setup process by using **Docker** for easy deployment. The following instructions guide you through integrating the NextiaMG component into your project, using only the necessary components and Docker configurations.

---

## Deployment Requirements

### Environment Variables
The following environment variables are required for the NextiaMG component to function correctly:
- `LTS_ENDPOINT`: The URL of the Long Term Storage service (e.g., `http://minio:9000`).
- `LTS_ACCESKEY`: The access key for the Long Term Storage service.
- `LTS_SECRETKEY`: The secret key for the Long Term Storage service.
- `LTS_BUCKET`: The name of the bucket in the Long Term Storage service where files will be stored (e.g., long-term-storage).
- `LTS_CYCLOPSLTS`: The LTS technology used by Cyclops (e.g., `MinIO`).

### Volumes & Persistent Storage

The NextiaMG component collects data either through the local file system via a Finder interface or through external API calls. It processes the collected data and generates output as a downloadable ZIP file containing the generated mappings and the global schema.

#### Input Data
- The input data is collected from the local file system or external API calls, depending on the integration used.
- The data is stored temporarily in the container's internal storage during processing.

#### Output Data
- After processing, the generated mappings and global schema are packaged into a downloadable ZIP file.
- The ZIP file will be saved in a specific output directory within the container, which is later made available for download by the user.
- The file is stored temporarily until it is accessed by the user or external system.

#### Database Storage
- No persistent database storage is required for this component. The data is processed in-memory, and the results are stored as files (ZIP file containing mappings and the schema).




### Network Configuration

The NextiaMG component consists of two services: the **frontend** and the **backend**, which are deployed as Docker containers.

#### Exposed Ports
- **Frontend (Application)**:
    - Accessible at `localhost:9000` (host → container).
- **Backend**:
    - Accessible at `localhost:3000` (host → container).

#### Communication Between Services
- The frontend communicates with the backend using Docker's internal network. The frontend connects to the backend at the internal service name `backend`.

#### Docker Networking
- The frontend and backend containers are on the same Docker network, so they can communicate directly.
- Docker Compose handles the network setup and service discovery automatically.

#### Firewall Rules
- No firewall rules are needed locally. If running in a cloud or production environment, ensure that ports `8080` (backend) and `9000` (frontend) are open.

By default, the backend is only reachable from within the Docker network, while the frontend is accessible externally via port `9000`.


---

## Infrastructure Setup & Resource Allocation

### CPU & Memory Requirements
This component can be resource-intensive depending on the size of the datasets. You may need to adjust the CPU and memory allocation in the Docker configuration.

### Storage Considerations
- The component requires enough disk space for input files and generated output files.

---

## How to Run NextiaMG Using Docker

### Step 1: Clone the Repository

Clone this repository to your local machine:

```bash
git clone https://gitlab.com/cyclops4100006/nextiamg.git
cd nextiamg
```

### Step 2: Configure Docker Compose

We provide a `docker-compose.yml` file for easy configuration and integration with the system. To build and run the component using Docker Compose, follow these steps:

1. **Build the containers**:

    ```bash
    docker compose -f ./docker-compose.yml build --no-cache frontend backend
    ```

2. **Start the containers**:

    ```bash
    docker compose -f ./docker-compose.yml up frontend backend
    ```

This will set up the ODIN tool with the NextiaMG component along with all required dependencies for the project.

---

## Repository Structure

The repository structure follows the original layout for compatibility with the larger project, but with only the necessary components included:

```bash
nextiamg_full/              # Full project code, including NextiaMG
├── backend/                # Java-based backend implementation
├── frontend/               # Frontend components (Vue.js)
├── Dockerfile              # Dockerfile for building the component
├── docker-compose.yml      # Docker Compose configuration
├── README.md               # This README file
└── requirements.txt        # Dependencies for Python parts (if any)
README.md 
```

---
## Questions / Contact
Please reach out (aniol.bisquert@upc.edu) if you have any questions or need assistance with the NextiaMG tool.

---

## Additional Instructions

For further customization or specific integrations, refer to the original ODIN project documentation. However, this repository provides all the necessary components for integrating the **NextiaMG** component in its standalone form.



# Project Onboarding Documentation

## Table of Contents

1. [Introduction](#introduction)
2. [Executing ODIN](#executing)
    - [Docker](#docker)
    - [Creating the environment](#environment)
      - [Requirements](#requirements)
      - [Backend](#backend-configuration)
      - [Intents modules](#intents-configuration)
      - [Frontend](#frontend-configuration)
3. [Project Structure](#project-structure)
   - [Backend](#backend)
     - [Database division](#database-division)
     - [Architecture](#backend-architecture)
     - [Code style](#code-style)
   - [Frontend](#frontend)
       - [Architecture](#frontend-architecture)
       - [Dependencies](#frontend-dependencies)
4. [Demo](#demo)
5. [People](#people)
6. [Related links](#related-links)
7. [License](#license)

## Introduction <a name="introduction"></a>

Welcome to ODIN! This documentation is designed to help new developers quickly get started with the project and understand its architecture, dependencies, and configuration.

## Executing ODIN <a name="executing"></a>

### Docker <a name="docker"></a>

The easiest way to execute ODIN is via the Docker environment provided in the repository. To do so, make sure that [Docker](https://www.docker.com/) is installed in your machine and running. Once this is done, go to the root folder of the repository and run the following command:

   ```bash
   docker-compose build
   ```
This will build the containers and the environment. Once it finishes, to launch ODIN run the next line, also in the root folder:

  ```bash
   docker-compose up
   ```
After some seconds all the services will have deployed and will be ready to be used.

**Note:** to use the intent anticipation/generation module it is necessary to populate a GraphDB instance with an initial set of triples. To do so, go to the deployed GraphDB URL (by default, [localhost:7200](http://localhost:7200/)), select _import_ on the top left and then _upload RDF files_. Upload the following file: [KnowledgeBase.nt](backend/Modules/IntentAnticipation/read-write-graphdb/graphdb-import/KnowledgeBase.nt)

### Creating the environment <a name="environment"></a>

This section details how to set up ODIN outside of the Docker environment, which requires downloading all the necessary dependencies and running the corresponding services.

#### Requirements <a name="requirements"></a>

Ensure that you have the following prerequisites installed:

- [Node.js](https://nodejs.org/) (version >=20.0.0)
- [NPM](https://docs.npmjs.com/cli/v8/commands/npm-install)
- [Yarn](https://classic.yarnpkg.com/lang/en/docs/install/#windows-stable) You can install it using `npm install -g yarn` (or on a macOS install it using Homebrew using `brew install yarn`)
- [Quasar](https://quasar.dev/) You can install it using `npm install -g @quasar/cli`
- [Gradle](https://gradle.org/) (version >=8.5)
- [Java](https://www.oracle.com/es/java/technologies/javase/jdk11-archive-downloads.html) (version 11)
- [Python](https://www.python.org/downloads/) (version >=3.12)
- [Hadoop](https://spark.apache.org/downloads.html): **only for Windows**. Create a folder, e.g. _C:/Work/Hadoop/bin_, and place there the files _hadoop.dl_ and _winutils.exe_ from the following [link](https://github.com/cdarlint/winutils/tree/master/hadoop-3.3.5/bin). Finally, add the path (e.g. _C:/Work/Hadoop/bin_) to the path environment variable.

Once everything is installed, clone the repository:

   ```bash
   git clone https://github.com/dtim-upc/ODIN.git
   cd ODIN
   ```
#### Backend <a name="backend-configuration"></a>

1. In the root folder execute the following command to compile all the different modules and run their respective tests. For Windows:

    ```bash
   gradlew build
   ```
   For Unix:
    ```bash
   ./gradle build
   ```

2. In the same directory, run the following command to launch the backend API. For Windows:
   ```bash
   gradlew bootRun
   ```
   For Unix:
    ```bash
   ./gradle bootRun
   ```

##### Intents modules <a name="intents-configuration"></a>
The intent-generation functionalities are separated into two different modules, which can be found in the backend folder. Both are built with Python and, as such, can not be natively executed by ODIN. Instead, they offers APIs to which ODIN connects to and sends requests. 
1. Go to `ODIN/Modules/IntentSpecification2WorkflowGenerator`. First install all the required libraries with the following command:
   
   ```bash
   pip install -r requirements.txt    
   ```
   Then, launch the API with the following line
   ```bash
   flask --app api\api_main.py run    
   ```
2. Go to `ODIN/Modules/IntentAnticipation`. First install all the required libraries with the following command:
   
   ```bash
   pip install -r requirements.txt    
   ```
   Then, launch the APIs (there are two) with the following line
   ```bash
   python .\start_apis.py  
   ```

#### Frontend <a name="frontend-configuration"></a>

1. Open in the terminal the `ODIN/frontend` folder.

2. Execute `npm install`.

3. Then, execute `yarn install` (on macOS it is possible you need to run `yarn install --ignore-engines`).

4. Finally, execute `quasar dev` (or on a macOS do it from the `node_modules` directory using `node_modules/@quasar/app-vite/bin/quasar dev`. This will open your browser with the URL http://localhost:9000/#/projects.

_Note: that you must have Quasar CLI as it's mentioned in the Prerequisites section. If there's an error like `Global Quasar CLI • ⚠️   Error  Unknown command "dev"`, it's because you are not in the correct path, or you don't have Quasar CLI installed._ 


## Project Structure <a name="project-structure"></a>

   The project structure is primarily divided into two code sections (backend and frontend) and a third section for documentation, where the UML diagram is located. To access the UML diagram, VisualParadigm is required.

   The code sections consist of two IntelliJ projects. Please note that access to IntelliJ Ultimate is available with a UPC email account.

### Backend <a name="backend"></a>

   ODIN's backend is developed in **Java** and utilizes the **Spring framework**. As the backend for ODIN, its primary role is to **orchestrate** calls to various Nextia libraries within the department. **Each Nextia** library serves a distinct **functionality**, and ODIN also manages application persistence.

   ODIN's **persistence** is bifurcated into two components: graph persistence, currently using Jena, and relational database persistence using ORM with the embedded H2 database.

   The different Nextia modules are presented next:

   - **NextiaCore:** Contains domain class definitions for the system and serves as a cross-cutting library. It lacks business logic, focusing solely on Plain Old Java Object (POJO) classes. 

   - **NextiaDataLayer:** Manages an intermediate persistence layer for accessing data from uploaded datasets.

   - **NextiaBS:** Extracts schemas from datasets loaded into ODIN and generates wrappers, which are SQL queries demonstrating how to access the original dataset's data.

   - **NextiaJD:** Join discovery. Responsible for automatically discovering attribute alignments from datasets.

   - **NextiaDI:** Handles data integration. Given two schemas and alignments, it integrates them and generates the corresponding integrated graph.

   - **NextiaMG:** Creating mappings between local data sources and the global schema using the integrated graph. In this initial version the supported mappings language is R2RML.

   Internally within ODIN, there are NextiaStore and NextiaGraphy. The former manages persistence, while the latter generates a string representation for visually rendering graphs generated by NextiaBS in the frontend.

   The diagram described above would look like this:

```mermaid
graph TD;

subgraph ODIN
  NextiaStore[fa:fa-database <a href='https://github.com/dtim-upc/ODIN/tree/main/api/src/main/java/edu/upc/essi/dtim/odin/NextiaStore' >NextiaStore</a>]
  NextiaGraphy[fa:fa-paint-brush <a href='https://github.com/dtim-upc/ODIN/tree/main/api/src/main/java/edu/upc/essi/dtim/odin/nextiaInterfaces/NextiaGraphy' >NextiaGraphy</a>]
end

subgraph MODULES
  NextiaBS[fa:fa-file-code <a href='https://github.com/dtim-upc/NextiaBS' >NextiaBS</a>]
  NextiaJD[fa:fa-copy <a href='https://github.com/dtim-upc/NextiaJD2' >NextiaJD</a>]
  NextiaDI[fa:fa-object-group <a href='https://github.com/dtim-upc/NextiaDI' >NextiaDI</a>]
  NextiaQR[fa:fa-search <a href='https://github.com/dtim-upc/NextiaQR' >NextiaQR</a>]
end

subgraph TRANSVERSAL
  NextiaCore[fa:fa-book <a href='https://github.com/dtim-upc/NextiaCore' >NextiaCore</a>]
  NextiaDataLayer[fa:fa-universal-access <a href='https://github.com/dtim-upc/NextiaDataLayer' >NextiaDataLayer</a>]
end

ODIN-->NextiaCore;
ODIN-->NextiaDataLayer-->NextiaCore;
ODIN-->NextiaBS-->NextiaCore;
ODIN-->NextiaJD-->NextiaCore;
ODIN-->NextiaDI-->NextiaCore;
ODIN-->NextiaQR-->NextiaCore;
NextiaJD-->NextiaDataLayer;
NextiaQR-->NextiaDataLayer;
```

#### Summary modules structure
#### Transversal Modules

- **NextiaCore:** Contains domain class definitions and acts as a cross-cutting library.
- **NextiaDataLayer:** Manages an intermediate persistence layer to access data from loaded datasets.

#### Nextia Modules

- **NextiaBS:** Extracts schemas from loaded datasets in ODIN and generates wrappers.
- **NextiaJD:** Join discovery. Automatically discovers attribute alignments.
- **NextiaDI:** Handles data integration. Integrates two schemas and generates the corresponding integrated graph.
- **NextiaQR:** (Optional) Performs query-based searches on datasets.

#### Internal NextiaStore and NextiaGraphy

- **NextiaStore:** Manages persistence.
- **NextiaGraphy:** Generates a string representation for rendering graphs in the frontend.



#### Database Division <a name="database-division"></a>

NextiaStore is divided into two types of databases:

1. **Relational Database (H2):**
    - This is used to store relational data as could be some part of a project, datasets, user information...
    - If you must change something, make the necessary changes in the [orm.xml](api/src/main/resources/META-INF/orm.xml) in the `api/src/main/resources/META-INF/orm.xml` path. This file contains the mapping one-to-one with our domain objects persisted ([ORM syntax](https://www.datanucleus.org/products/accessplatform_5_1/jpa/metadata_xml.html)).

2. **Graph Database (Jena):**
    - This one is used to store the schemas of our graphs. This graphs now are implemented under an interface as Model Jena's object.

A usage example of the two databases are available in [getProjectById(String projectId)](https://github.com/dtim-upc/ODIN/blob/6ef41b22c57a7f763a91a7d0ae843918d0be1666/api/src/main/java/edu/upc/essi/dtim/odin/project/ProjectService.java#L167) or also [saveProject(Project project)](https://github.com/dtim-upc/ODIN/blob/6ef41b22c57a7f763a91a7d0ae843918d0be1666/api/src/main/java/edu/upc/essi/dtim/odin/project/ProjectService.java#L113) where we must get from the RelationalDB some information and the other related with its schemas from the GraphDB.

Both types are structured by the same pattern (see [NextiaStore](https://github.com/dtim-upc/ODIN/tree/main/api/src/main/java/edu/upc/essi/dtim/odin/NextiaStore) folder): Factory, Interface, Implementation. See the examples to know how to use them.

#### UML Diagram <a name="uml-diagram"></a>

The UML diagram provides an overview of the project structure. The key sections are described below:

#### Architecture <a name="backend-architecture"></a>

To see the UML class diagram, download [Visual Paradigm Community Edition](https://www.visual-paradigm.com/download/community.jsp). It will ask you an email by the end of the installation or the first time you open the program just to send you the key activation code.

Then open the [UML conceptual ODIN.vpp](https://github.com/dtim-upc/ODIN/blob/integration-dataset-bug/doc/Diagrama%20UML/UML%20conceptual%20ODIN.vpp)

It should look like this:
<p align="center">
  <img src="doc\Diagrama UML\ODIN.jpg">
</p>


Simple interaction with a Nextia module

```mermaid
sequenceDiagram
    participant Frontend
    participant Controller
    participant Service
    participant Interface
    participant InterfaceImpl
    participant Nextia

    Frontend->>Controller: petition
    Controller->>Service: ask service
    Service->>Interface: request template
    Interface->>InterfaceImpl: d
    InterfaceImpl->>Nextia: info
    Nextia-->>InterfaceImpl: answer
    InterfaceImpl-->>Interface: answer
    Interface-->>Service: html & javascript
    Service-->>Controller: iframe ready
    Controller->>Service: set mermaid data on iframe
    Service->>Service: render mermaid
    Controller-->>Frontend: answer
```
   
#### Code Style <a name="code-style"></a>

##### Overview
This project follows a set of coding standards and practices to ensure consistency, readability, and maintainability of the codebase. Adhering to these guidelines will help streamline collaboration and make the codebase more accessible to developers.

##### Branching Strategy
We follow a feature-based branching strategy, where each new feature or task is developed on a dedicated branch. Once a feature is complete and tested, it is merged into the main branch. Branches should be named descriptively, reflecting the nature of the feature or task they represent.

Example git graph:
```mermaid
gitGraph:
commit "Ashish"
branch newbranch
checkout newbranch
commit id:"1111"
commit tag:"test"
checkout main
commit type: HIGHLIGHT
commit
merge newbranch
commit
branch b2
commit
```

After a successful merge, the feature branch should be deleted to keep the repository clean and avoid unnecessary noise.

##### Code Organization
###### Main Branch
The main branch must always contain a working version of the project. Developers should avoid pushing directly to the main branch and instead use feature branches for development.

###### Naming Conventions
* CamelCase: Variable and method names should use CamelCase. For example: myVariable, calculateTotalAmount().

* Class Naming:
  * Interfaces: Named as ConcreteNameInterface.java (e.g., GraphStoreInterface.java).
  * Implementations: Named as ConcreteNameTechnologyUsedImpl (e.g., GraphStoreJenaImpl.java).
  Controller and Service Naming: Controllers should be named with a descriptive term followed by "Controller" (e.g., IntegrationController). The corresponding service should be named with a similar descriptive term followed by "Service" (e.g., IntegrationService).

* Communication between Components: Controllers only communicate with their associated services. Services can interact with other services but should not communicate directly with controllers. Interfaces are used for communication with other modules.

###### Dependency Management
* Interfaces for Communication: Use interfaces to define contracts for communication between modules. This enhances modularity and allows for easier maintenance and testing.

### Frontend <a name="frontend"></a>

#### Architecture <a name="frontend-architecture"></a>
The frontend is organized by components: forms, tables, etc. 

There's an special form: [FormNewRepository.vue](frontend/src/components/forms/FormNewRepository.vue). It has a static part that represents de global repository information and then the dynamic part that charges from the backend all the jsons files located in the backend in this path [`api/src/main/resources/frontend-schemas/RepositoryForms`](api/src/main/resources/frontend-schemas/RepositoryForms). By using this json structures it builds the hierarchy part of the repository specialitation.

##### Component Overview:
1. Global Repository Information:

There is a static part of the form that represents global repository information. This part likely includes fields and input elements for information that is applicable to the entire repository.

2. Dynamic Part - Repository Specialization:

The dynamic part of the form is responsible for loading data from the backend. Specifically, it loads JSON files located in the backend at the path: api/src/main/resources/frontend-schemas/RepositoryForms.

2.1. Dynamic Part Details:
- JSON Files in Backend:

In the backend, there is a directory path specified as `api/src/main/resources/frontend-schemas/RepositoryForms`. This path contains JSON files.
- Purpose of JSON Files:

These JSON files likely contain schema or structure information for various forms related to repository specialization.
- Runtime Loading:

At runtime, the FormNewRepository.vue component fetches and loads these JSON files from the backend.
- Building Hierarchy:

The loaded JSON structures are used to dynamically construct or build a hierarchy within the form. This hierarchy is likely related to the specialization of the repository and is represented in the form.

#### Dependencies <a name="frontend-dependencies"></a>
Dependencies are located in the [package.json](frontend/package.json) file.
## Demo <a name="demo"></a>

https://github.com/dtim-upc/ODIN/assets/75260498/1a3a2642-7061-47e0-94b0-84f081b603bb


## People <a name="people"></a>

These researchers have significantly contributed to shaping ODIN into a more robust and feature-rich platform.
- Javier Flores
- Cristina Gómez
- Sergi Nadal
- Oscar Romero
- Marc Maynou
- Juan Emilio Olivan
- Víctor Asenjo
   

## Related links <a name="related-links"></a>



## License <a name="license"></a>

   This project is licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.html).

