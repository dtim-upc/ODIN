import {defineStore} from 'pinia'
import {useNotify} from 'src/use/useNotify.js'
import intentsAPI from "src/api/intentsAPI.js";

export const useIntentsStore = defineStore('intents', {

  state: () => ({
    intentID: "",
    queryUri: "",
    problems: [],
    selectedQuery: [],
    abstractPlans: [],
    logicalPlans: [],
    selectedPlans: [],
  }),

  getters: {},
  actions: {
    async init() {

    },

    async postIntent(projectID, data) {
      const notify = useNotify();
      console.log("Creating intent")

      await intentsAPI.postIntent(projectID, data).then((response) => {
        if (response.status === 200) {
          notify.positive("Intent created")
          console.log(response.data)
          this.intentID = response.data
        } else {
          notify.negative("Intent could not be created")
        }
      }).catch((error) => {
        console.log("error is: " + error)
        if (error.response) {
          notify.negative("Something went wrong when creating an intent.")
        }
      });
    },

    async annotateDataset(data) {
      const notify = useNotify();
      console.log("Annotating dataset")

      await intentsAPI.annotateDataset(data).then((response) => {
        console.log(response)
        if (response.status === 200) {
          notify.positive(`Dataset annotated`)
          this.queryUri = Object.values(response.data)[0]
        } else {
          notify.negative("Dataset could not be annotated")
        }
      }).catch((error) => {
        console.log("error is: " + error)
        if (error.response) {
          notify.negative("Something went wrong in the server when annotating a dataset.")
        }
      });
    },

    getProblems() {
      intentsAPI.getProblems()
        .then(response => {
          console.log("Intent problems received")

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
      console.log("Running abstract planner")

      await intentsAPI.setAbstractPlans(data).then((response) => {
        if (response.status === 200) {
          notify.positive(`Abstract plans created`)
          // Formatting the plans to be displayed in the UI
          for (let plan in response.data) {
            const newObject = {
              name: plan.split('#').at(-1),
              id: plan,
              selected: false,
              plan: response.data[plan]
            }
            this.abstractPlans.push(newObject)
          }
          this.logicalPlans = []
          this.selectedPlans = []
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

    /*
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
    },*/

    async setLogicalPlans(data, successCallback) {
      const notify = useNotify();
      console.log("Running logical planner with data ", data)

      await intentsAPI.setLogicalPlans(data).then((response) => {
        console.log(response)
        if (response.status === 200) {
          notify.positive(`Logical plans created`)
          // Formatting the plans to be displayed in the UI
          const keys = Object.keys(response.data);
          this.logicalPlans = [];
        
          for (let key of keys) {
            let found = false
            const plan = {
              id: key,
              selected: false,
              plan: response.data[key]
            }
            this.logicalPlans.map(logPlan => {
              if (logPlan.id === this.removeLastPart(key)) {
                logPlan.plans.push(plan)
                found = true
              }
            })
            if (!found) {
              this.logicalPlans.push({
                id: this.removeLastPart(key),
                selected: false,
                plans: [plan]
              })
            }
          }
          this.selectedPlans = []
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

    removeLastPart(inputString) {
      const parts = inputString.split(' ');
      if (parts.length > 1) {
          parts.pop(); // Remove the last part
          return parts.join(' ');
      } else {
          return inputString; // Return the original string if there's only one part
      }
    },

    /*
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
    },*/

    /*
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
    },*/    

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

    async downloadAllRDF(selectedPlanIds) {
      console.log("Downloading all RDF files")
      const notify = useNotify();

      await intentsAPI.downloadAllRDF(selectedPlanIds).then((response) => {
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

    async downloadAllKNIME(selectedPlanIds) {
      console.log("Downloading All KNIME files")
      const notify = useNotify();

      await intentsAPI.downloadAllKNIME(selectedPlanIds).then((response) => {
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
    },

    async storeWorkflow(projectID, data) {
      const notify = useNotify();

      await intentsAPI.storeWorkflow(projectID, this.intentID, data).then((response) => {
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
