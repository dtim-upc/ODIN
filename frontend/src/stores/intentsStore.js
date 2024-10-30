import {defineStore} from 'pinia'
import {useNotify} from 'src/use/useNotify.js'
import intentsAPI from "src/api/intentsAPI.js";
import JSZip from 'jszip';
import FileSaver from 'file-saver';

const notify = useNotify();

export const useIntentsStore = defineStore('intents', {

  state: () => ({
    intents: [], // List of intents in the system (ODIN objects)
    problems: [], // List of problems available for the user to select when creating an intent
    intentID: "", // ID of the current intent (ODIN object), used to associate the workflows that are stored to it
    
    selectedProblem: "", // Problem selected by the user, either manually or via inference
    intentName: "", 
    selectedDataProdutName:"",
    intentDescription:"", // To infer the problem based on a description

    target:"", // Only for classification tasks

    selectedMetric: "",
    allMetrics: [],
    selectedPreprocessing: "",
    selectedPreprocessingAlgorithm: "",
    allPreprocessingAlgorithms: [],
    selectedAlgorithm: "",
    allAlgorithms: [],

    dataProductURI: "", // URI of the selected data product. This is required given that when working with graphs we need URIs
    intent_graph: {}, // Graph definition of the current intent
    ontology: "", // Ontology of the system (graph)
    algorithmImplementations: [], // List of algorithms defined in the ontology
    labelColumn: "", // Column over which a classification model will operate. ONLY FOR INTEGRATION WITH PROACTIVE. SHOULD BE REMOVED WHEN INTEGRATION DEPENDS ON THE GRAPH
    
    abstractPlans: [], // List of abstract plans (displayed in Logical Planner)
    logicalPlans: [], // List of logical plans (displayed in Workflow Planner)
    selectedPlans: [], // List of selected plans by the user (Displayed in Workflows)
    countSelectedPlans: 0 // Number of selected plans by the user
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
      this.labelColumn = data["label"] // SHOULD BE REMOVED EVENTUALLY
      try {
        const response = await intentsAPI.annotateDataset(data);
        notify.positive(`Dataset annotated`)
        this.ontology = response.data.ontology
        this.dataProductURI = response.data.data_product_uri;
      } catch (error) {
        notify.negative("Error in annotating the dataset.")
        console.error("Error:", error);
      }
    },
    
    async setAbstractPlans(data, successCallback) {
      try {
        const response = await intentsAPI.setAbstractPlans(data);
        notify.positive(`Abstract plans created`)
        this.intent_graph = response.data.intent
        this.algorithmImplementations = response.data.algorithm_implementations
        this.abstractPlans = Object.entries(response.data.abstract_plans).map(([plan, value]) => ({
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
            plan: response.data[key].logical_plan,
            graph:  response.data[key].graph,
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
    async downloadRDF(plan) {
      try {
        FileSaver.saveAs(new Blob([plan.graph]), `${plan.id}.ttl`);
        notify.positive(`RDF file downloaded`);
      } catch (error) {
        notify.negative("Error downloading the RDF file");
        console.error("Error:", error);
      }
    },
    
    async downloadKNIME(plan) {
      const data = {"graph": plan.graph, "ontology": this.ontology}
      try {
        const response = await intentsAPI.downloadKNIME(data);
        FileSaver.saveAs(new Blob([response.data]), `${plan.id}.knwf`);
        notify.positive(`KNIME file downloaded`);
      } catch (error) {
        notify.negative("Error downloading the KNIME file");
        console.error("Error:", error);
      }
    },

    async downloadProactive(plan) {
      const currentIntent = this.intents.find(intent => intent.intentID === String(this.intentID)) // SHOULD BE REMOVED EVENTUALLY
      const dataProductName = currentIntent.dataProduct.datasetName // SHOULD BE REMOVED EVENTUALLY
      // At some point, the translation to the Proactive ontology should be done, and the API should only require the graph to make it
      const data = {"graph": plan.graph, "ontology": this.ontology, "layout": plan.plan, "label_column": this.labelColumn, "data_product_name": dataProductName}
      try {
        const response = await intentsAPI.downloadProactive(data);
        FileSaver.saveAs(new Blob([response.data]), `${plan.id}.xml`);
        notify.positive(`Proactive file downloaded`);
      } catch (error) {
        notify.negative("Error downloading the KNIME file");
        console.error("Error:", error);
      }
    },

    getSelectedGraphs() {
      const graphs = {}
      for (const [key, value] of Object.entries(this.selectedPlans)) {
        for (const plan of value.plans) {
          const { graph, id } = plan
          graphs[id] = graph
        }
      }
      return graphs
    },
    
    async downloadAllRDF() {
      const zip = new JSZip();
      try {
        for (const [key, value] of Object.entries(this.getSelectedGraphs())) {
          zip.file(key + ".ttl", value);
        }
        zip.generateAsync({ type: 'blob' }).then(function (content) {
          FileSaver.saveAs(content, 'rdf-files.zip');
        });
        notify.positive(`All RDF files downloaded`);
      } catch (error) {
        notify.negative("Error downloading all the RDF files");
        console.error("Error:", error);
      }
    },
    
    async downloadAllKNIME() {
      const data = {"graphs": this.getSelectedGraphs(), "ontology": this.ontology}
      try {
        const response = await intentsAPI.downloadAllKNIME(data);
        FileSaver.saveAs(new Blob([response.data]), `knime.zip`);
        notify.positive(`All RDF files downloaded`);
      } catch (error) {
        notify.negative("Error downloading all the KNIMW files");
        console.error("Error:", error);
      }
    },

    // ------------ Intent anticipation
  
    async predictIntentType(data) {
      try {
        const response = await intentsAPI.predictIntentType(data);
        notify.positive(`Type of intent predicted`);
        this.selectedProblem = response.data.intent
      } catch (error) {
        notify.negative("Error deleting an intent.");
        console.error("Error:", error);
      }
    },

    async addUser() {
      let data = {
        "email": "test@user.com"
      }
      await intentsAPI.addUser(data);
    },

    async addDataset() {
      let data = {
        "dataset": this.selectedDataProdutName
      }
      await intentsAPI.addDataset(data);
    },

    async predictParameters() {
      const user = "testuser"
      const dataset = this.selectedDataProdutName 
      const intent = this.selectedProblem 

      let response = await intentsAPI.getMetric(user, dataset, intent);
      this.selectedMetric = response.data.metric
      response = await intentsAPI.getAlgorithm(user, dataset, intent);
      this.selectedAlgorithm = response.data.algorithm
      response = await intentsAPI.getPreprocessing(user, dataset, intent);
      this.selectedPreprocessing = response.data.preprocessing
      response = await intentsAPI.getPreprocessingAlgorithm(user, dataset, intent);
      this.selectedPreprocessingAlgorithm= response.data.preprocessing_algorithm

      this.selectedAlgorithm = "DecisionTree"
    },

    async getAllInfo() {
      const response = await intentsAPI.getAllInfo();
      this.allMetrics = response.data.metrics
      this.allAlgorithms = response.data.algorithms
      this.allPreprocessingAlgorithms= response.data.preprocessing_algorithms

      this.allAlgorithms = ["DecisionTree", "NN", "SVM"]
    },
  }
})
