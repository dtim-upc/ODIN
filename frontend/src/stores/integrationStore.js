import { defineStore } from 'pinia'
import { useNotify } from 'src/use/useNotify.js'
import integrationAPI from "src/api/integrationAPI.js";
import { useProjectsStore } from 'src/stores/projectsStore'

const projectsStore = useProjectsStore();
const notify = useNotify();

export const useIntegrationStore = defineStore('integration', {
  state: () => ({
    projectTemporal: {},
    datasetToIntegrate: [],
    alignments: [],
    joinAlignments: [],
  }),

  getters: {
    getSourceB() {
      if (this.datasetToIntegrate.length === 1)
        return this.datasetToIntegrate[0]
      else
        return null
    },
    getGraphicalB() {
      if (this.datasetToIntegrate.length === 1) {
        return this.datasetToIntegrate[0].localGraph.graphicalSchema
      } else
        return ""
    },
    getTemporalGlobalSchema() {
      if (this.projectTemporal.temporalIntegratedGraph.globalGraph.graphicalSchema)
        return this.projectTemporal.temporalIntegratedGraph.globalGraph.graphicalSchema
      return ""
    },
    getTemporalGraphicalSchemaIntegration() {
      if (this.projectTemporal.temporalIntegratedGraph.graphicalSchema)
        return this.projectTemporal.temporalIntegratedGraph.graphicalSchema
      return ""
    },
    isJoinAlignmentsRelationshipsComplete() {
      if (this.joinAlignments.length === 0)
        return true;
      return this.joinAlignments.filter(a => a.relationship === "").length === 0;
    }
  },

  actions: {

    // ------------ Data management

    selectDatasetToIntegrate(ds) {
      this.datasetToIntegrate = [ds];
    },
    
    addAlignment(alignment, refactor) {
      let newAlignment = true
      console.log(this.alignments)
      this.alignments.map(schemaAlignment => {
        if (schemaAlignment.iriA ===  alignment.resourceA.iri && schemaAlignment.iriB ===  alignment.resourceB.iri) {
          newAlignment = false
          notify.negative("Alignment already defined");
        }
      })
      if (newAlignment) {
        const a = refactor ? {
          iriA: alignment.resourceA.iri,
          labelA: alignment.resourceA.label,
          iriB: alignment.resourceB.iri,
          labelB: alignment.resourceB.label,
          l: alignment.integratedLabel,
          type: alignment.type,
          similarity: alignment.similarity
        } : alignment;
      
        this.alignments.push(a);
        notify.positive("Alignment added")
      }
    },

    deleteAlignment(alignment) {
      this.alignments = this.alignments.filter(a => a !== alignment);
    },
    
    deleteJoinAlignment(alignment) {
      this.joinAlignments = this.joinAlignments.filter(a => a !== alignment);
    },

    // ------------ Integration steps
    
    async getAutomaticAlignments() {
      try {
        const response = await integrationAPI.getAutomaticAlignments(projectsStore.currentProject.projectId, this.datasetToIntegrate[0].id);
        this.alignments = response.data;
        notify.positive(`${response.data.length} automatic alignments found`);
      } catch (error) {
        console.error("Error:", error);
        notify.negative("Something went wrong in the server.");
      }
    },

    async integrate(successCallback) {
      const data = {
        dsB: this.datasetToIntegrate[0],
        alignments: this.alignments
      };
    
      try {
        const response = await integrationAPI.integrate(projectsStore.currentProject.projectId, data);
        notify.positive("Integration succeeded");
        this.projectTemporal = response.data.project;
        this.joinAlignments = response.data.joins;
        successCallback()
      } catch (error) {
        console.error("Error:", error);
        notify.negative("There was an error for the integration task " + error);
      }
    },
    
    async reviewAlignments(successCallback) {
      try {
        const response = await integrationAPI.reviewAlignments(projectsStore.currentProject.projectId, this.joinAlignments);
        this.projectTemporal = response.data;
        notify.positive("Integration succeeded");
        successCallback();
      } catch (error) {
        console.error("Error:", error);
        notify.negative("Something went wrong when reviewing the alignmentst " + error);
      }
    },
    
    async persistIntegration() {
      try {
        await integrationAPI.persistIntegration(projectsStore.currentProject.projectId);
        notify.positive("Integration saved successfully");
        projectsStore.updateCurrentProject();
        this.alignments = []
        this.joinAlignments = []
      } catch (error) {
        console.error("Error:", error);
        notify.negative("Something went wrong when persisting the integration " + error);
      }
    },
    

  }

})
