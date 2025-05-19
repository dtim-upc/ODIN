
# NextiaMG Component 

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
No mandatory environment variables are required to run the NextiaMG component at this time.

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
    docker compose build -f ./nextiamg_full/docker-compose.yml --no-cache frontend backend
    ```

2. **Start the containers**:

    ```bash
    docker-compose -f ./nextiamg_full/docker-compose.yml up frontend backend
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

---



