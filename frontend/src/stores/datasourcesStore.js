import { defineStore } from 'pinia';
import { useNotify } from 'src/use/useNotify.js';
import api from "src/api/datasetsAPI.js";
import { useAuthStore } from 'src/stores/authStore.js';
import { useRoute } from "vue-router";
import projectAPI from 'src/api/projectAPI';
import { useIntegrationStore } from 'src/stores/integrationStore.js';
import download from 'downloadjs';

export const useDataSourceStore = defineStore('datasource', {
  state: () => ({
    project: {},
    datasources: [],
  }),

  getters: {
    getDatasourcesNumber(state) {
      return state.datasources.length;
    },
    getGlobalSchema(state) {
      return state.project.integratedGraph.globalGraph.graphicalSchema || "";
    },
    getGraphicalSchemaIntegration(state) {
      return state.project.integratedGraph.graphicalSchema || "";
    },
    getTemporalGlobalSchema(state) {
      return state.project.temporalIntegratedGraph.globalGraph.graphicalSchema || "";
    },
    getTemporalGraphicalSchemaIntegration(state) {
      return state.project.temporalIntegratedGraph.graphicalSchema || "";
    },
  },

  actions: {
    
    async setProject(proj) {
      const route = useRoute();
      const authStore = useAuthStore();
      const integrationStore = useIntegrationStore();

      if (proj) {
        this.project = proj;
        integrationStore.setProject(proj);
      } else if (!this.project.projectName || this.project.projectId != route.params.id) {
        const response = await projectAPI.getProject(route.params.id, authStore.user.accessToken);

        if (response.status == 200) {
          this.project = response.data;
          integrationStore.setProject(this.project);
        }
      }

      if (authStore.user.accessToken) {
        this.getDatasources(proj.id);
      }
      return this.project;
    },

    async updateProjectInfo() {
      const authStore = useAuthStore();
      const integrationStore = useIntegrationStore();
      const response = await projectAPI.getProject(this.project.projectId, authStore.user.accessToken);

      if (response.status == 200) {
        this.project = response.data;
        await integrationStore.setProject(this.project);
      }
    },

    async getDatasources(projectId) {
      const notify = useNotify();
      const authStore = useAuthStore();

      try {
        const response = await api.getAllDatasets(projectId, authStore.user.accessToken);

        if (response.data === "" || response.status === 204) {
          this.datasources = [];
          notify.positive("There are no data sources yet. Add sources to see them.");
        } else {
          this.datasources = response.data;
        }
      } catch (err) {
        if (err.response && err.response.status === 401) {
          notify.negative("Unauthorized access.");
        } else if (err.response && err.response.status === 404) {
          this.datasources = [];
          notify.negative("Datasources not found.");
        } else {
          notify.negative("Cannot connect to the server.");
        }
      }
    },

    async editDatasource(projectID, datasetID, data, successCallback) {
      const notify = useNotify();

      try {
        const response = await api.putDataset(projectID, datasetID, data);

        if (response.status === 200) {
          notify.positive(`Dataset successfully edited`);
          successCallback();
        } else {
          notify.negative("Cannot edit data. Something went wrong on the server.");
        }
      } catch (error) {
        notify.negative("Something went wrong on the server while editing the data.");
      }
    },

    async finishPreview() {
      await this.updateProjectInfo();
      this.router.go(-1);
    },

    async setDatasetSchemaAsProjectSchema(ds) {
      const authStore = useAuthStore();
      const notify = useNotify();

      try {
        const response = await api.setDatasetSchemaAsProjectSchema(this.project.projectId, ds.id, authStore.user.accessToken);

        if (response.status == 200) {
          notify.positive("Schema successfully set");
          await this.updateProjectInfo();
        } else {
          notify.negative("Something went wrong setting the schema of the project.");
        }
      } catch (err) {
        notify.negative("Something went wrong setting the schema of the project.");
      }
    },

    async deleteDataSource(ds) {
      const authStore = useAuthStore();
      const notify = useNotify();

      try {
        const response = await api.deleteDataset(this.project.projectId, ds.id, authStore.user.accessToken);

        if (response.status == 200) {
          notify.positive("Successfully deleted");

          let index = this.datasources.indexOf(ds);
          if (index > -1) {
            this.datasources.splice(index, 1);
          }
          await this.updateProjectInfo();
        } else {
          notify.negative("Something went wrong in the server.");
        }
      } catch (err) {
        notify.negative("Cannot delete data source. Error in the server.");
      }
    },

    async downloadSource(dsID) {
      const response = await api.downloadDatasetGraph(this.project.projectId, dsID);
      const content = response.headers['content-type'];
      download(response.data, "prueba.ttl", content);
    },
  }
});