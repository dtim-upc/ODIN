import { defineStore } from 'pinia';
import { useNotify } from 'src/use/useNotify.js';
import mappingAPI from 'src/api/mappingsAPI.js';
import download from 'downloadjs';

const notify = useNotify();

export const useMappingsStore = defineStore('mappings', {
  state: () => ({}),
  actions: {
    async downloadMappings(projectID, type, configFile = null) {
      try {
        const formData = new FormData();
        formData.append('projectId', projectID);
        formData.append('mappingType', type);
        if (configFile) {
          formData.append('configFile', configFile);
        }
        const response = await mappingAPI.downloadMappings(projectID, formData);
        const content = response.headers['content-type'];
        const filename = "mappings_and_ontology.zip"; // can be customized from header if needed
        download(response.data, filename, content);
        notify.positive("Mappings downloaded successfully");
      } catch (error) {
        notify.negative("Error downloading mappings");
        console.error("Error downloading mappings:", error);
      }
    }
  }
});
