import { defineStore } from 'pinia';
import { useNotify } from 'src/use/useNotify.js';
import projectAPI from "src/api/projectAPI.js";
import download from 'downloadjs';

const notify = useNotify();

export const useProjectsStore = defineStore('projects', {
  state: () => ({
    currentProject: {},
    projects: [],
  }),

  actions: {
    async init() {
      if (this.projects.length === 0) {
        await this.getProjects();
      }
    },

    async getProjects() {
      try {
        const response = await projectAPI.getAllProjects();
        this.projects = response.data === "" ? [] : response.data;
      } catch (error) {
        notify.negative("Could not retrieve projects");
        console.error("Error:", error);
      }
    },

    async postProject(project, successCallback) {
      project.createdBy = "Admin"; //authStore.user.username, when users are implemented
      try {
        const response = await projectAPI.postProject(project);
        notify.positive(`Project ${project.projectName} successfully created`);
        this.projects.push(response.data);
        successCallback();
      } catch (error) {
        notify.negative("Something went wrong in the server for creating a project.");
        console.error("Error:", error);

      }
    },

    async deleteProject(projectID) {
      try {
        await projectAPI.deleteProject(projectID);
        notify.positive(`Project deleted`);
        this.getProjects();
      } catch (error) {
        notify.negative("Something went wrong on the server while deleting a project.");
        console.error("Error:", error);
      }
    },

    async putProject(project, successCallback) {
      try {
        await projectAPI.putProject(project.projectID, project);
        notify.positive(`Project ${project.projectId} successfully edited`);
        this.getProjects();
        successCallback();
      } catch (error) {
        notify.negative("Something went wrong on the server while editing a project.");
        console.error("Error:", error);
      }
    },

    async cloneProject(id, successCallback) {
      try {
        await projectAPI.cloneProject(id);
        notify.positive(`Project successfully cloned`);
        this.getProjects();
        successCallback();
      } catch (error) {
        notify.negative("Something went wrong on the server while cloning the project.");
        console.error("Error:", error);
      }
    },

    async downloadProjectSchema(projectID) {
      try {
        const response = await projectAPI.downloadProjectGraph(projectID);
        const content = response.headers['content-type'];
        download(response.data, "source_graph.ttl", content);
      } catch (error) {
        notify.negative("Something went wrong on the server while downloading the project schema.");
      }
    }
  }
});