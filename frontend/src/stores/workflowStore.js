import {defineStore} from 'pinia'
import {useNotify} from 'src/use/useNotify.js'
import workflowAPI from "src/api/workflowAPI.js";
import {useIntentsStore} from "src/stores/intentsStore.js";

export const useWorkflowsStore = defineStore('store', {

  state: () => ({

  }),

  getters: {},
  actions: {
    async init() {

    },

    async postWorkflow(projectID, intentID, data) {
      const notify = useNotify();

      await workflowAPI.postWorkflow(projectID, intentID, data).then((response) => {
        if (response.status === 200) {
          notify.positive(`Workflow stored`)
        } else {
          notify.negative("Error storing the workflow")
        }
      }).catch((error) => {
        console.log("error is: " + error)
        if (error.response) {
          notify.negative("Something went wrong in the server when storing the workflow")
        }
      });
    },

    async putWorkflow(intentID, projectID, workflowID, data) {
      const notify = useNotify();

      await workflowAPI.putWorkflow(intentID, projectID, workflowID, data)
        .then((response) => {
          if (response.status === 200) {
            notify.positive(`Workflow successfully edited`);
            useIntentsStore().getAllIntents(projectID) // Refresh the intents and, as such, the workflows
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

    async deleteWorkflow(projectID, intentID, workflowID) {
      const notify = useNotify();
      console.log("Deleting workflow with ID ", workflowID)

      await workflowAPI.deleteWorkflow(projectID, intentID, workflowID).then((response) => {
          if (response.status === 200) {
            notify.positive(`Workflow deleted successfully`)
            useIntentsStore().getAllIntents(projectID) // Refresh the intents and, as such, the workflows
          } else {
            notify.negative("Workflow could not be deleted.")
          }
      }).catch((error) => {
          console.log("error is: " + error)
          if (error.response) {
              notify.negative("Something went wrong in the server when deleting a workflow.")
          }
      });
    },
  }
})
