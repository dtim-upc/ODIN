<h1 align="center">
  <a href="https://www.essi.upc.edu/dtim/odin/"><img src="https://github.com/dtim-upc/newODIN/blob/master/logos/ODIN.svg" alt="NextiaDI" width="300">
  </a>
</h1>

<h4 align="center">A dataspace management system<a href="https://www.essi.upc.edu/dtim/tools/odin"></a></h4>

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=dtim-upc_newODIN&metric=alert_status)](https://sonarcloud.io/dashboard?id=dtim-upc_newODIN)

# Project Onboarding Documentation

## Table of Contents

1. [Introduction](#introduction)
2. [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
    - [Configuration](#configuration)
3. [Project Structure](#project-structure)
4. [Backend](#backend)
    - [Architecture](#backend-architecture)
    - [Dependencies](#backend-dependencies)
5. [Frontend](#frontend)
    - [Architecture](#frontend-architecture)
    - [Dependencies](#frontend-dependencies)
6. [Usage](#usage)
7. [Troubleshooting](#troubleshooting)
8. [Contributing](#contributing)
9. [License](#license)

## Introduction <a name="introduction"></a>

Welcome to ODIN! This documentation is designed to help new developers quickly get started with the project and understand its architecture, dependencies, and configuration.

## Getting Started <a name="getting-started"></a>

### Prerequisites <a name="prerequisites"></a>

Before you begin, ensure that you have the following prerequisites installed:

- [Node.js](https://nodejs.org/) (version >=12.22.1)
- [NPM](https://docs.npmjs.com/cli/v8/commands/npm-install) (version >=6.14.12)
- [Yarn](https://classic.yarnpkg.com/lang/en/docs/install/#windows-stable) You can install it using `npm install -g yarn`
- [Quasar](https://quasar.dev/) (CLI >= 2.0) You can install it using `npm install -g @quasar/cli`
- [Gradle](https://gradle.org/) (version >=6.8)
- [Java](https://www.oracle.com/es/java/technologies/javase/jdk11-archive-downloads.html) (version 11)
- [Spark](https://spark.apache.org/downloads.html)

### Installation <a name="installation"></a>

1. Clone the repository:

   ```bash
   git clone https://github.com/dtim-upc/ODIN.git
   cd ODIN
   ```
   
2. Clone the others repositories used by ODIN:


   - [NextiaCore](https://github.com/dtim-upc/NextiaCore)
   ```bash
   git clone https://github.com/dtim-upc/NextiaCore.git
   ```


   - [NextiaDataLayer](https://github.com/dtim-upc/NextiaJD2)
   ```bash
   git clone https://github.com/dtim-upc/NextiaJD2.git
   ```


   - [NextiaBS](https://github.com/dtim-upc/NextiaBS)
   ```bash
   git clone https://github.com/dtim-upc/NextiaBS.git
   ```


   - [NextiaDI](https://github.com/dtim-upc/NextiaDI)
   ```bash
   git clone https://github.com/dtim-upc/NextiaDI.git
   ```


   - [NextiaJD2](https://github.com/dtim-upc/NextiaJD2) (ask for permissions)
   ```bash
   git clone https://github.com/dtim-upc/NextiaJD2.git
   ```


### Configuration <a name="configuration"></a>

1. Generate the JAR folders of the Nextia projects:

   Execute for each Nextia project the Gradle task `uberJar`. This task will generate a zipped folder containing the compiled and optimized JAR files necessary for the seamless integration of Nextia functionalities within the ODIN system. The JAR generated will be located in the `build/libs` path of each project. 

   First, you should generate NextiaCore.jar because this JAR will be used by the other Nextia projects as a fundamental component, serving as a core library that provides essential functionalities and serves as a foundation for the remaining Nextia modules.

   Then, generate NextiaDataLayer.jar, which serves as a critical component utilized by both NextiaJD and ODIN. This JAR encapsulates data layer functionalities, ensuring seamless communication between NextiaJD and ODIN, and enabling efficient data management within the integrated system.

2. Execute `importExternalJar` Gradle task of ODIN:

   This task will import automatically all the `NextiaXX.jar` needed by ODIN.
   Ensure that all Nextia projects are located under the same folder as ODIN. This organizational structure is essential for the importExternalJar Gradle task to effectively locate and import the generated JAR files from Nextia projects.

   Check that the JAR libraries have been imported into lib directory in ODIN/api.

3. Finally execute `gradle bootRun` to start the application or open the project in Intellij IDE and run the main class `OdinApplication.java`.


#### Set up

The first time running the frontend, dependencies must be installed using the command `yarn install` under the frontend folder directory. Once dependencies are installed, the frontend server can be started using the command `quasar dev` which creates a server at 8080 or the close available port.

If it's not working, try this:
llega a la carpeta del frontend: cd .../frontend
mira si está el fichero .json: dir package.json
instalalo: npm init -y
ejecuta: quasar dev

IF STILL NOT WORKING CHECK THE package.json and ensure to have installed the Node.js, etc.


## Project Structure <a name="project-structure"></a>
## Backend <a name="backend"></a>
### Architecture <a name="backend-architecture"></a>
### Dependencies <a name="backend-dependencies"></a>
## Frontend <a name="frontend"></a>
### Architecture <a name="frontend-architecture"></a>
### Dependencies <a name="frontend-dependencies"></a>
## Usage <a name="usage"></a>
## Troubleshooting <a name="troubleshooting"></a>
## Contributing <a name="contributing"></a>
## License <a name="license"></a>

   This project is licensed under the <a href="https://opensource.org/license/mit/">MIT License</a>.

