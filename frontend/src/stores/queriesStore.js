import { defineStore } from 'pinia';
import { useNotify } from 'src/use/useNotify.js';
import queryAPI from "src/api/queryAPI.js";

const notify = useNotify();

export const useQueriesStore = defineStore('queries', {
  state: () => ({
    queries: [],
    selectedDataProductPath: null,
  }),

  actions: {
    async queryGraph(projectID, data, successCallback) {
      try {
        const response = await queryAPI.queryGraph(projectID, data);
        if (response.data === '') {
          notify.positive("Query result is empty");
        } else {
          successCallback(response.data);
        }
      } catch (error) {
        notify.negative("Error querying graph");
      }
    },
  }
});