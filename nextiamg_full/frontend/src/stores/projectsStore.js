import { defineStore } from 'pinia';
import { useNotify } from 'src/use/useNotify.js';
import projectAPI from "src/api/projectAPI.js";
import download from 'downloadjs';

const notify = useNotify();

export const useProjectsStore = defineStore('projects', {
  state: () => ({
    // We persist the current project in the local storage to not lose the data when refreshing the page
    currentProject: JSON.parse(localStorage.getItem('currentProject')) || {},
    projects: [],
  }),

  getters: {
    getGlobalSchema() {
      if (this.currentProject.integratedGraph !== null) {
        return this.currentProject.integratedGraph.globalGraph.graphicalSchema || "";
      }
      else return null
    },
  },

  actions: {

    // ------------ CRUD operations

    // i.e. get project
    async setCurrentProject(project) { 
      try {
        const response = await projectAPI.getProject(project.projectId);
        this.currentProject = response.data;
        localStorage.setItem('currentProject', JSON.stringify(this.currentProject));
      } catch (error) {
        notify.negative("Error retrieving the project");
        console.error("Error:", error);
      }
    },

    // i.e. get the same project from the server (with the latest data) and set it as the current project
    updateCurrentProject() {
      this.setCurrentProject(this.currentProject)
    },

    async getProjects() {
      try {
        const response = await projectAPI.getAllProjects();
        this.projects = response.data === "" ? [] : response.data;
      } catch (error) {
        notify.negative("Error retrieving the projects");
        console.error("Error:", error);
      }
    },

    async postProject(project, successCallback) {
      project.createdBy = "Admin"; //authStore.user.username, when users are implemented
      try {
        const response = await projectAPI.postProject(project);
        notify.positive(`Project ${project.projectName} successfully created`);
        this.getProjects();
        successCallback();
      } catch (error) {
        notify.negative("Error creating the project");
        console.error("Error:", error);

      }
    },

    async putProject(project, successCallback) {
      try {
        await projectAPI.putProject(project.projectId, project);
        notify.positive(`Project ${project.projectId} successfully edited`);
        this.getProjects();
        successCallback();
      } catch (error) {
        notify.negative("Error editing the project");
        console.error("Error:", error);
      }
    },

    async deleteProject(projectID) {
      try {
        await projectAPI.deleteProject(projectID);
        notify.positive(`Project deleted`);
        this.getProjects();
      } catch (error) {
        notify.negative("Error deleting the project");
        console.error("Error:", error);
      }
    },

    // ---------------- Other operations

    async cloneProject(id, successCallback) {
      try {
        await projectAPI.cloneProject(id);
        notify.positive(`Project successfully cloned`);
        this.getProjects();
        successCallback();
      } catch (error) {
        notify.negative("Error cloning the project");
        console.error("Error:", error);
      }
    },

    async downloadProjectSchema(projectID) {
      try {
        const response = await projectAPI.downloadProjectGraph(projectID);
        const content = response.headers['content-type'];
        download(response.data, "source_graph.ttl", content);
      } catch (error) {
        notify.negative("Error downloading the project schema");
        console.error("Error:", error);
      }
    },

    async resetProjectSchema(projectID) {
      try {
        await projectAPI.resetProjectSchema(projectID);
        this.updateCurrentProject();
      } catch (error) {
        notify.negative("Error reseting the project schema");
        console.error("Error:", error);
      }
    },
  }
});