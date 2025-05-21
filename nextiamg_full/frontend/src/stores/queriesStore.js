import { defineStore } from 'pinia';
import { useNotify } from 'src/use/useNotify.js';
import queryAPI from "src/api/queryAPI.js";

const notify = useNotify();

export const useQueriesStore = defineStore('queries', {
  state: () => ({}),

  actions: {
    async queryGraph(projectID, data, successCallback) {
      try {
        const response = await queryAPI.queryGraph(projectID, data);
        if (response.data === '') {
          notify.positive("Query result is empty");
        } else {
          notify.positive("Query result obtained");
          successCallback(response.data);
        }
      } catch (error) {
        notify.negative("Error querying graph");
        console.error("Error:", error);
      }
    },
  }
});