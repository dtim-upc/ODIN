import {defineStore} from 'pinia'
import {useNotify} from 'src/use/useNotify.js'
import intentsAPI from "src/api/intentsAPI.js";

const notify = useNotify();

export const useIntentsStore = defineStore('intents', {

  state: () => ({
    intents: [],
    intentID: "",
    queryUri: "",
    problems: [],
    selectedQuery: [],
    abstractPlans: [],
    logicalPlans: [],
    selectedPlans: [],
  }),

  actions: {

    // ------------ CRUD operations
    async getAllIntents(projectID) {
      try {
        const response = await intentsAPI.getAllIntents(projectID);
        this.intents = response.data || [];
      } catch (error) {
        console.error("Error retrieving intents:", error);
        console.error("Error:", error);
      }
    },

    async postIntent(projectID, data) {
      try {
        const response = await intentsAPI.postIntent(projectID, data);
        notify.positive("Intent created");
        this.intentID = response.data;
      } catch (error) {
        notify.negative("Error creating an intent.");
        console.error("Error:", error);
      }
    },
    
    async putIntent(intentID, projectID, data, successCallback) {
      try {
        await intentsAPI.putIntent(intentID, projectID, data);
        notify.positive(`Intent successfully edited`);
        this.getAllIntents(projectID);
        successCallback();
      } catch (error) {
        notify.negative("Error editing an intent.");
        console.error("Error:", error);
      }
    },

    async deleteIntent(projectID, intentID) {
      try {
        response = await intentsAPI.deleteIntent(projectID, intentID);
        notify.positive(`Intent deleted successfully`);
        this.getAllIntents(projectID);
      } catch (error) {
        notify.negative("Error deleting an intent.");
        console.error("Error:", error);
      }
    },
    
    // ------------ Plan generation operations
    async getProblems() {
      try {
        const response = await intentsAPI.getProblems();
        this.problems = response.data || [];
      } catch (error) {
        console.error("Error:", error);
      }
    },
    
    async annotateDataset(data) {
      try {
        const response = await intentsAPI.annotateDataset(data);
        notify.positive(`Dataset annotated`);
        this.queryUri = Object.values(response.data)[0];
      } catch (error) {
        notify.negative("Error in annotating the dataset.");
        console.error("Error:", error);
      }
    },
    
    async setAbstractPlans(data, successCallback) {
      try {
        const response = await intentsAPI.setAbstractPlans(data);
        notify.positive(`Abstract plans created`);
          this.abstractPlans = Object.entries(response.data).map(([plan, value]) => ({
            name: plan.split('#').at(-1),
            id: plan,
            selected: false,
            plan: value
          }));
          this.logicalPlans = [];
          this.selectedPlans = [];
          successCallback();
      } catch (error) {
        notify.negative("Error creating the abstract plans.");
        console.error("Error:", error);
      }
    },
    
    async setLogicalPlans(data, successCallback) {
      try {
        const response = await intentsAPI.setLogicalPlans(data);
        notify.positive(`Logical plans created`);
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
      } catch (error) {
        notify.negative("Error creating the logical plans.");
        console.error("Error:", error);
      }
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

    // ------------ Download operations
    async downloadRDF(planID) {
      try {
        const response = await intentsAPI.downloadRDF(planID);
        this.createDownload(response.data, `${planID}.ttl`);
        notify.positive(`RDF file downloaded`);
      } catch (error) {
        notify.negative("Error downloading the RDF file");
        console.error("Error:", error);
      }
    },
    
    async downloadKNIME(planID) {
      try {
        const response = await intentsAPI.downloadKNIME(planID);
        this.createDownload(response.data, `${planID}.knwf`);
        notify.positive(`KNIME file downloaded`);
      } catch (error) {
        notify.negative("Error downloading the KNIME file");
        console.error("Error:", error);
      }
    },
    
    async downloadAllRDF(selectedPlanIds) {
      try {
        const response = await intentsAPI.downloadAllRDF(selectedPlanIds);
        this.createDownload(response.data, "rdf.zip");
        notify.positive(`All RDF files downloaded`);
      } catch (error) {
        notify.negative("Error downloading all the RDF files");
        console.error("Error:", error);
      }
    },
    
    async downloadAllKNIME(selectedPlanIds) {
      try {
        const response = await intentsAPI.downloadAllKNIME(selectedPlanIds);
        this.createDownload(response.data, "knime.zip");
        notify.positive(`All RDF files downloaded`);
      } catch (error) {
        notify.negative("Error downloading all the KNIMW files");
        console.error("Error:", error);
      }
    },
    
    createDownload(data, name) {
      const url = window.URL.createObjectURL(new Blob([data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', name);
      document.body.appendChild(link);
      link.click();
    },
    
  }
})
