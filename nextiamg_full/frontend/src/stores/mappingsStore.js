import { defineStore } from 'pinia';
import { useNotify } from 'src/use/useNotify.js';
import mappingAPI from 'src/api/mappingsAPI.js';
import download from 'downloadjs';
import {useProjectsStore} from "./projectsStore";

const projectsStore = useProjectsStore();
const notify = useNotify();

export const useMappingsStore = defineStore('mappings', {
  state: () => ({}),
  actions: {
    async downloadMappings(projectID, type) {
      try {
        const response = await mappingAPI.downloadMappings(projectsStore.currentProject.projectId, type);
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
