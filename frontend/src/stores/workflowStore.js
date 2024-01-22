import {defineStore} from 'pinia'
import {useNotify} from 'src/use/useNotify.js'
import workflowAPI from "src/api/workflowAPI.js";

export const useWorkflowStore = defineStore('store', {

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
  }
})
