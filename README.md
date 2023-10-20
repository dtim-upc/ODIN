<h1 align="center">
  <a href="https://www.essi.upc.edu/dtim/odin/"><img src="https://github.com/dtim-upc/newODIN/blob/master/logos/ODIN.svg" alt="NextiaDI" width="300">
  </a>
</h1>

<h4 align="center">A dataspace management system</a></h4>
<p align="center">
  • <a href="#about">About</a> •
  <a href="#development">Development</a> •
</p>


[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=dtim-upc_newODIN&metric=alert_status)](https://sonarcloud.io/dashboard?id=dtim-upc_newODIN)

## About

## Development

### Backend

#### Requirements

* Gradle >= v6.8
* NextiaCore.jar
* NextiaDI.jar
* NextiaBS.jar

#### Set up

Before running the backend project, it is necessary to create your own profile which specifies the paths needed for the application. Properties files must be created under api/src/main/resources using the following name syntax: “application-<Profile name>.properties”. To enable a profile properties you need to modify the property  profiles.active: <Profile name> in the file application.properties in the same directory 

You will need also to generate or download the Nextia's libs and paste them into lib directory in ODIN/api. Or you can also download the Nextia's dependencies, configure them and generate their JARs using the task "uberJar" and finally, from ODIN project execute the task "importExternalJar" of the build.gradle file.

Then, for running the project you can use the terminal command `gradle bootRun` to start the application or open the project in Intellij IDE and run the main class "OdinApplication.java".

#### Useful links

https://www.arquitecturajava.com/spring-autowired-y-la-inyeccion-de-dependencias/

### Frontend

#### Requirements

* Node >=12.22.1  Note that uneven versions of Node i.e. 13, 15, etc. do not work
* NPM >=6.14.12
* Yarn. You can install it using `npm install -g yarn`
* Quasar CLI >= 2.0. You can install it using `npm install -g @quasar/cli`

#### Set up

The first time running the frontend, dependencies must be installed using the command `yarn install` under the frontend folder directory. Once dependencies are installed, the frontend server can be started using the command `quasar dev` which creates a server at 8080 or the close available port.

If it's not working, try this:
llega a la carpeta del frontend: cd .../frontend
mira si está el fichero .json: dir package.json
instalalo: npm init -y
ejecuta: quasar dev

IF STILL NOT WORKING CHECK THE package.json and ensure to have installed the Node.js, etc.
