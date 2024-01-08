import {defineStore} from 'pinia'
import {useNotify} from 'src/use/useNotify.js'
import intentsAPI from "src/api/intentsAPI.js";
import JSZip from 'jszip';

export const useIntentsStore = defineStore('intents', {

  state: () => ({
    datasets: [],
    problems: [],
    abstractPlans: [],
    logicalPlans: [],
    workflowPlans: [],
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

    async setAbstractPlans(data, successCallback) {
      const notify = useNotify();
      console.log("Running abstract planner with data ", data)

      await intentsAPI.setAbstractPlans(data).then((response) => {
        if (response.status === 204) {
          notify.positive(`Abstract plans created`)
          successCallback();
        } else {
          notify.negative("Abstract plans could not be created")
        }
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

    async setLogicalPlans(data, successCallback) {
      const notify = useNotify();
      console.log("Running logical planner with data ", data)

      await intentsAPI.setLogicalPlans(data).then((response) => {
        if (response.status === 204) {
          notify.positive(`Logical plans created`)
          successCallback();
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

    setWorkflowPlans(data, successCallback) {
      const notify = useNotify();
      console.log("Running workflow planner with data ", data)

      intentsAPI.setWorkflowPlans(data).then((response) => {
        if (response.status === 204) {
          notify.positive(`Workflow plans created`)
          successCallback();
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

    async getWorkflowPlans() {
      console.log("Getting workflow plans")
      const notify = useNotify();

      await intentsAPI.getWorkflowPlans().then((response) => {
        if (response.status === 200) {
          if (response.data === "") {
            this.workflowPlans = []
          } else {
            this.workflowPlans = response.data
          }
          notify.positive(`Workflow plans obtained`)
        } else {
          notify.negative("Error getting the workflow plans")
        }
      }).catch((error) => {
        console.log("error is: " + error)
        if (error.response) {
          notify.negative("Something went wrong in the server when getting the workflow plans")
        }
      });
    },

    async downloadRDF(planID) {
      console.log("Downloading RDF file for plan " + planID)
      const notify = useNotify();

      await intentsAPI.downloadRDF(planID).then((response) => {
        if (response.status === 200) {
          this.createDownload(response.data, planID + ".ttl")
          notify.positive(`File downloaded`)
        } else {
          notify.negative("Error getting the RDF file")
        }
      }).catch((error) => {
        console.log("error is: " + error)
        if (error.response) {
          notify.negative("Something went wrong in the server when getting the RDF file")
        }
      });
    },

    async downloadKNIME(planID) {
      console.log("Downloading KNIME file for plan " + planID)
      const notify = useNotify();

      await intentsAPI.downloadKNIME(planID).then((response) => {
        if (response.status === 200) {
          this.createDownload(response.data, planID + ".knwf")
          notify.positive(`File downloaded`)
        } else {
          notify.negative("Error getting the KNIME file")
        }
      }).catch((error) => {
        console.log("error is: " + error)
        if (error.response) {
          notify.negative("Something went wrong in the server when getting the KNIME file")
        }
      });
    },

    async downloadAllRDF() {
      console.log("Downloading all RDF files")
      const notify = useNotify();

      await intentsAPI.downloadAllRDF().then((response) => {
        if (response.status === 200) {
          this.createDownload(response.data, "rdf.zip")
          notify.positive(`Files downloaded`)
        } else {
          notify.negative("Error getting all RDF files")
        }
      }).catch((error) => {
        console.log("error is: " + error)
        if (error.response) {
          notify.negative("Something went wrong in the server when getting all RDF files")
        }
      });
    },

    async downloadAllKNIME(planID) {
      console.log("Downloading All KNIME files")
      const notify = useNotify();

      await intentsAPI.downloadAllKNIME(planID).then((response) => {
        if (response.status === 200) {
          this.createDownload(response.data, "knime.zip")
          notify.positive(`Files downloaded`)
        } else {
          notify.negative("Error getting all the KNIME files")
        }
      }).catch((error) => {
        console.log("error is: " + error)
        if (error.response) {
          notify.negative("Something went wrong in the server when getting all KNIME files")
        }
      });
    },

    createDownload(data, name) {
      const url = window.URL.createObjectURL(new Blob([data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', name);
      document.body.appendChild(link);
      link.click();
    }
  }
})
