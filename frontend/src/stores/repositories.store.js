import { defineStore } from 'pinia';
import {useNotify} from 'src/use/useNotify.js'
import repositoryAPI from "src/api/repositoryAPI.js";

export const useRepositoriesStore = defineStore('repositories', {
    state: () => ({
        repositories: [],
        selectedRepositoryId: null,
        selectedRepositoryType: null,
        selectedRepositoryName: null,
    }),

    actions: {
        async init() {

        },

        setSelectedRepositoryId(repositoryId) {
          this.selectedRepositoryId = repositoryId;
          this.setSelectedRepositoryName(repositoryId);
          this.selectedRepositoryType = this.repositories.some(repository => repository.id === this.selectedRepositoryId) ? this.repositories.find(repository => repository.id === this.selectedRepositoryId).repositoryType : "ERROR 404 No type";
        },
    
        setSelectedRepositoryName(repositoryId) {
          this.selectedRepositoryName = this.repositories.some(repository => repository.id === repositoryId) ? this.repositories.find(repository => repository.id === repositoryId).repositoryName : "ERROR 404 No name";
        },

        async getRepositories(projectId) {
            const notify = useNotify()
            console.log("Getting repositories...")

            await repositoryAPI.getRepositories(projectId).then(response => {
              console.log("repos received", response.data)
              if (response.data === "") { // when no datasources, api answer ""
                this.repositories = []
                notify.positive("There are no repositories yet. Add sources to see them.")
              } else if (response.status === 204) {
                this.repositories = []
                notify.positive("There are no repositories yet. Add sources to see them.")
              } else {
                this.repositories = response.data
              }
            }).catch(err => {
              console.log("error retrieving data sources")
              console.log(err)
              if (err.response && err.response.status === 401) {
                // Handle unauthorized error
                // Notify the user or perform any other necessary actions
                notify.negative("Unauthorized access.")
              } else if (err.response && err.response.status === 404) {
                this.repositories = []
                notify.negative("Repositories not found.")
              } else {
                notify.negative("Cannot connect to the server.")
              }
            });
          },

          editRepository(data, successCallback) {
            const notify = useNotify();
      
            repositoryAPI.editRepository(data)
              .then((response) => {
                if (response.status === 200) {
                  notify.positive(`Repository successfully edited`);
                  successCallback()
                } else {
                  notify.negative("Cannot edit data. Something went wrong on the server.");
                }
              })
              .catch((error) => {
                console.log("Error is: " + error);
                if (error.response) {
                  notify.negative("Something went wrong on the server while editing the data.");
                }
              });
          },

        deleteRepository(projectID, repositoryID) {
            const notify = useNotify();
            console.log("Deleting repository with ID ", repositoryID)

            repositoryAPI.deleteRepository(projectID, repositoryID).then((response) => {
                if (response.status === 200) {
                  notify.positive(`Repository deleted successfully`)
                  var index = this.repositories.map(function(repo) { return repo.id; }).indexOf(repositoryID);
                  if (index > -1) {
                    this.repositories.splice(index, 1) // remove from local store
                  }
                } else {
                  notify.negative("Repository could not be deleted.")
                }
            }).catch((error) => {
                console.log("error is: " + error)
                if (error.response) {
                    notify.negative("Something went wrong in the server when deleting a repository.")
                }
            });
        }
    },
});
