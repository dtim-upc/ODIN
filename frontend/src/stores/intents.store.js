import {defineStore} from 'pinia'
import {useNotify} from 'src/use/useNotify.js'
import intentsAPI from "src/api/intentsAPI.js";

export const useIntentsStore = defineStore('intents', {

  state: () => ({
    datasets: [],
    problems: [],
    abstractPlans: [],
    logicalPlans: [],
  }),

  getters: {},
  actions: {
    async init() {

    },

    getDatasets() {
      intentsAPI.getDatasets()
        .then(response => {

          console.log("Intent datasets received")
          console.log(response.data)

          if (response.data === "") { // when no datasources, api answer ""
            this.datasets = []
          } else {
            this.datasets = response.data
          }

        }).catch(err => {
        console.log("error retrieving Intent datasets")
        console.log(err)
      })
    },

    getProblems() {
      intentsAPI.getProblems()
        .then(response => {

          console.log("Intent problems received")
          console.log(response.data)

          if (response.data === "") {
            this.problems = []
          } else {
            this.problems = response.data
          }

        }).catch(err => {
        console.log("error retrieving problems")
        console.log(err)
      })
    },

    setAbstractPlans(data, successCallback) {
      const notify = useNotify();
      console.log("Running abstract planner with data ", data)

      intentsAPI.setAbstractPlans(data).then((response) => {
        if (response.status === 204) {
          notify.positive(`Abstract plans created`)
        } else {
          notify.negative("Abstract plans could not be created")
        }
        successCallback();
      }).catch((error) => {
        console.log("error is: " + error)
        if (error.response) {
          notify.negative("Something went wrong in the server for creating a project.")
        }
      });
    },

    async getAbstractPlans() {
      console.log("Getting abstract plans")
      const notify = useNotify();

      await intentsAPI.getAbstractPlans().then((response) => {
        if (response.status === 200) {
          if (response.data === "") {
            this.abstractPlans = []
          } else {
            this.abstractPlans = response.data
          }
          notify.positive(`Abstract plans obtained`)
        } else {
          notify.negative("Error getting the abstract plans")
        }
      }).catch((error) => {
        console.log("error is: " + error)
        if (error.response) {
          notify.negative("Something went wrong in the server when getting the abstract plans")
        }
      });
    },

    setLogicalPlans(data) {
      const notify = useNotify();
      console.log("Running logical planner with data ", data)

      intentsAPI.setLogicalPlans(data).then((response) => {
        if (response.status === 204) {
          notify.positive(`Logical plans created`)
        } else {
          notify.negative("Logical plans could not be created")
        }
      }).catch((error) => {
        console.log("error is: " + error)
        if (error.response) {
          notify.negative("Something went wrong in the server when creating the logical plans.")
        }
      });
    },

    async getLogicalPlans() {
      console.log("Getting logical plans")
      const notify = useNotify();

      await intentsAPI.getLogicalPlans().then((response) => {
        if (response.status === 200) {
          if (response.data === "") {
            this.logicalPlans = []
          } else {
            this.logicalPlans = response.data
          }
          notify.positive(`Logical plans obtained`)
        } else {
          notify.negative("Error getting the logical plans")
        }
      }).catch((error) => {
        console.log("error is: " + error)
        if (error.response) {
          notify.negative("Something went wrong in the server when getting the logical plans")
        }
      });
    },

    setWorkflowPlans(data) {
      const notify = useNotify();
      console.log("Running workflow planner with data ", data)

      intentsAPI.setWorkflowPlans(data).then((response) => {
        if (response.status === 204) {
          notify.positive(`Workflow plans created`)
        } else {
          notify.negative("Workflow plans could not be created")
        }
      }).catch((error) => {
        console.log("error is: " + error)
        if (error.response) {
          notify.negative("Something went wrong in the server when creating the workflow plans.")
        }
      });
    },
  }
})
