import { defineStore } from 'pinia';
import { useNotify } from 'src/use/useNotify.js';
import workflowAPI from "src/api/workflowAPI.js";
import { useIntentsStore } from "src/stores/intentsStore.js";

const notify = useNotify();

export const useWorkflowsStore = defineStore('store', {
  state: () => ({}),

  actions: {

    // ---------------- CRUD Operations
    async postWorkflow(projectID, intentID, data) {
      try {
        await workflowAPI.postWorkflow(projectID, intentID, data);
        notify.positive(`Workflow stored`);
      } catch (error) {
        notify.negative("Error storing the workflow");
        console.error("Error:", error);
      }
    },

    async putWorkflow(intentID, projectID, workflowID, data) {
      try {
        await workflowAPI.putWorkflow(intentID, projectID, workflowID, data);
        notify.positive(`Workflow successfully edited`);
        useIntentsStore().getAllIntents(projectID); // Refresh the intents and, as such, the workflows
      } catch (error) {
        notify.negative("Error editing the workflow");
        console.error("Error:", error);
      }
    },

    async deleteWorkflow(projectID, intentID, workflowID) {
      const notify = useNotify();

      try {
        await workflowAPI.deleteWorkflow(projectID, intentID, workflowID);
        notify.positive(`Workflow deleted successfully`);
        useIntentsStore().getAllIntents(projectID); // Refresh the intents and, as such, the workflows
      } catch (error) {
        notify.negative("Error deleting the workflow.");
        console.error("Error:", error);
      }
    },
  }
});