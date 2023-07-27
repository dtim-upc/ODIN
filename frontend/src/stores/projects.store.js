import { defineStore } from 'pinia'
import {useNotify} from 'src/use/useNotify.js'
import projectAPI from "src/api/projectAPI.js";
import { useAuthStore } from 'stores/auth.store.js'

export const useProjectsStore = defineStore('projects',{

    state: () => ({
        projects : [],
    }),

    getters : {},
    actions: {
      init() {
        console.log("projects store init")
        // this.initStores();
        const authStore = useAuthStore();
        if (authStore.user.accessToken && this.projects.length === 0) {
          this.getProjects()
        }
      },
      getProjects() {
        const authStore = useAuthStore();
        projectAPI.getAllProjects(authStore.user.accessToken)
          .then(response => {

            console.log("projects received")
            console.log(response.data)

            if (response.data === "") { // when no datasources, api answer ""
              this.projects = []
            } else {
              this.projects = response.data
            }

          }).catch(err => {
          console.log("error retrieving data sources")
          console.log(err)
        })
      },
      createProject(project, successCallback) {
        const authStore = useAuthStore();
        const notify = useNotify();

        console.log("create project store...")
        project.createdBy = "Julio Berne"//authStore.user.username
        console.log("send project: ", project)
        projectAPI.createProject(project, authStore.user.accessToken).then((response) => {
          if (response.status === 201) {
            console.log(response)
            notify.positive(`Project ${project.projectName} successfully created`)
            this.projects.push(response.data)
            successCallback()
          } else {
            notify.negative("Cannot create project. Something went wrong in the server.")
          }
        }).catch((error) => {
          console.log("error is: " + error)
          if (error.response) {
            notify.negative("Something went wrong in the server for creating a project.")
          }
        });
      },
      deleteProjectByID(id, successCallback) {
        const authStore = useAuthStore();
        const notify = useNotify();

        projectAPI.deleteProjectByID(id, authStore.user.accessToken)
          .then((response) => {
            if (response.status === 200) {
              const index = this.projects.findIndex(project => project.projectId === id);
              if (index !== -1) {
                // Remove the project from the projects array using splice
                this.projects.splice(index, 1);
                notify.positive(`Project ${id} successfully deleted`);
                successCallback();
              } else {
                notify.negative("Project not found in the list.");
              }
            } else {
              notify.negative("Cannot delete project. Something went wrong on the server.");
            }
          })
          .catch((error) => {
            console.log("Error is: " + error);
            if (error.response) {
              notify.negative("Something went wrong on the server while deleting a project.");
            }
          });
      }
    }


    })
