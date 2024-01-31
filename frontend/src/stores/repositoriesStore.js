import { defineStore } from 'pinia';
import {useNotify} from 'src/use/useNotify.js'
import repositoryAPI from "src/api/repositoryAPI.js";

const notify = useNotify();

export const useRepositoriesStore = defineStore('repositories', {
  state: () => ({
    repositories: [],
    selectedRepository: {},
  }),

  actions: {
    
    setSelectedRepository(repositoryId) {
      this.selectedRepository = this.repositories.find(repository => repository.id === repositoryId);
    },

    // ------------ CRUD operations
    async getRepositories(projectId) {
      try {
        const response = await repositoryAPI.getRepositories(projectId);
        if (response.data === "") {
          this.repositories = [];
        } else {
          this.repositories = response.data;
        }
      } catch (error) {
        notify.negative("Error getting the repositories");
        console.error("Error:", error);
      }
    },
    
    async postRepository(projectID, data, successCallback) {
      try {
        await repositoryAPI.postRepository(projectID, data);
        notify.positive(`Repository successfully created`);
        this.getRepositories(projectID);
        successCallback()
      } catch (error) {
        notify.negative("Error creating the repository");
        console.error("Error:", error);
      }
    },

    async putRepository(repositoryID, projectID, data, successCallback) {
      try {
        await repositoryAPI.putRepository(repositoryID, projectID, data);
        notify.positive(`Repository successfully edited`);
        this.getRepositories(projectID)
        successCallback();
      } catch (error) {
        notify.negative("Error editing the repository");
        console.error("Error:", error);
      }
    },

    async deleteRepository(projectID, repositoryID) {
      try {
        await repositoryAPI.deleteRepository(projectID, repositoryID);
        notify.positive(`Repository deleted successfully`);
        this.getRepositories(projectID);
      } catch (error) {
        notify.negative("Error deleting the repository");
        console.error("Error:", error);
      }
    },

    // ------------ Operations related to JDBC repositories
    async testConnection(data) {
      try { 
        const response = await repositoryAPI.testConnection(data);
        if (response.data === true) {
          notify.positive('Connection established successfully.');
          return true;
        } else {
          notify.negative("Error connecting with the provided database.");
          return false;
        }
      } catch (error) {
        notify.error('Error while trying to connect with the database');
        console.error("Error:", error);
        return false;
      }
    },

    async retrieveDBTables(projectID, repositoryID) {
      try {
        const response = await repositoryAPI.retrieveDBTables(projectID, repositoryID);
        return response.data;
      } catch (error) {
        notify.error('Error while trying to get the tables of the database.');
        console.error("Error:", error);
      }
    },

    // ------------ Get the schemas when creating a new repository

    async getRepositoryTypes() {
      try {
        const response = await repositoryAPI.getRepositoryTypes();
        return response.data
      } catch (error) {
        notify.error('Error while getting the types of repositories.');
        console.error("Error:", error);
      }
    },

    async getRepositorySchema(repositoryType) {
      try {
        const response = await repositoryAPI.getRepositorySchema(repositoryType);
        return response.data;
      } catch (error) {
        notify.error('Error while getting the repository schema');
        console.error("Error:", error);
      }
    }
  },
});
