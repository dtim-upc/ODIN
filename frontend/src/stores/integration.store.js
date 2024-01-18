import {defineStore} from 'pinia'
import {useNotify} from 'src/use/useNotify.js'
import api from "src/api/datasetsAPI";
import integrationAPI from "src/api/integration.api.js";
import {useAuthStore} from 'stores/auth.store.js'
import {useRoute} from "vue-router";
import projectAPI from 'src/api/projectAPI';
import download from 'downloadjs'
// const notify = useNotify()

// let notify;
// let authStore;
// let route;


export const useIntegrationStore = defineStore('integration', {

  state: () => ({
    project: {},
    projectTemporal: {},
    datasources: [], // these ds are in temporal landing
    selectedDS: [], //selected ds. It is an array because of table requirement input, but it will always contain only one element
    alignments: [],
    joinAlignments: [],
    chargingAlignments:false
    // {"domainLabelA":"person", "domainLabelB":"country", "rightArrow":"true" ,"iriA": "A", "iriB": "B", "labelA": "lA", "labelB": "lB", "l": "i2", "type":"property" }]
  }),

  getters: {
    // getSourceA(state){
    // should always be the project schema
    // if(state.project.numberOfDS == '0') {
    //     if(state.selectedDS.length == 2)
    //         return state.selectedDS[0]
    //     else
    //         return null
    // } else {
    //     // return project graphical schema
    //     return state.project.schema.graphical
    // }
    // return null;
    // },
    getDatasourcesNumber(state) {
      return state.datasources.length
    },
    getSourceB(state) {
      if (state.selectedDS.length === 1)
        return state.selectedDS[0]
      else
        return null
    },
    getGraphicalA(state) {
      if (state.project.integratedGraph !== null && state.project.integratedGraph !== undefined) {
        const graphicalSchema = state.project.integratedGraph.globalGraph?.graphicalSchema;
        if (graphicalSchema !== null && graphicalSchema !== undefined) {
          return graphicalSchema;
        }
      }
      return null; // Devuelve null si no se puede acceder a graphicalSchema
    },
    getGraphicalB(state) {
      if (state.selectedDS.length === 1) {
        console.log("******************" + state.selectedDS[0])
        return state.selectedDS[0].localGraph.graphicalSchema
      } else
        return ""
    },
    getGlobalSchema(state) {
      if (state.projectTemporal.temporalIntegratedGraph.globalGraph.graphicalSchema)
        return state.projectTemporal.temporalIntegratedGraph.globalGraph.graphicalSchema
      return ""
    },
    getGraphicalSchemaIntegration(state) {
      if (state.projectTemporal.temporalIntegratedGraph.graphicalSchema)
        return state.projectTemporal.temporalIntegratedGraph.graphicalSchema
      return ""
    },
    isDSEmpty(state) {
      return state.datasources.length === 0
    },
    isJoinAlignmentsRelationshipsComplete(state) {

      if (state.joinAlignments.length === 0)
        return true;

      return state.joinAlignments.filter(a => a.relationship === "").length === 0;


    }
  },
  actions: {

    async init() {
      
    },
    async setProject(p) {
      const route = useRoute()
      const authStore = useAuthStore()
      console.log("setting project to integration store..")

      if (p) {
        this.project = p

      } else if (!this.project.projectName) {

        const response = await projectAPI.getProject(route.params.id, authStore.user.accessToken)

        if (response.status === 200) {
          this.project = response.data
        }


      }
      // console.log("Ds: ",this.datasources.length)
      // && this.datasources.length === 0
      if (authStore.user.accessToken) {
        console.log("retrieving temporal data sources...")
        this.getTemporalDatasources()
        this.alignments = []
      }

    },

    async getTemporalDatasources() {
      const notify = useNotify()
      const authStore = useAuthStore()

      console.log("Pinia getting temporal data sources...")
      await api.getAll(this.project.projectId, authStore.user.accessToken).then(response => {

        console.log("ds temporal received", response)

        if (response.data === "") { // when no datasources, api answer ""
          this.datasources = []
          notify.positive("There are no data sources yet. Add sources to see them.")
        } else {
          this.datasources = response.data
        }
      }).catch(err => {
        console.log("error retrieving data sources")
        console.log(err)
        if (err.response && err.response.status === 401) {
          // Handle unauthorized error
          // Notify the user or perform any other necessary actions
          notify.negative("Unauthorized access.")
        } else if (err.response && err.response.status === 404) {
          this.datasources = []
          notify.positive("There are no data sources yet. Add sources to see them.")
        } else {
          notify.negative("Cannot connect to the server.")
        }
      });
    },

    // this will upload the data source
    addDataSource(projectID, data, success) {
      const notify = useNotify()
      api.postDataset(projectID, data)
        .then((response) => {
          console.log("dataset created ", response)
          if (response.status === 200) {

            // this should be in temporal landing
            //this.project.datasets.push(response.data)

            // this.temporalDatasources.push(response.data)

            success(response.data);
            // storeDS.addDataSource(response.data)

          } else {
            // console.log("error")
            notify.negative("Cannot create datasource. Something went wrong in the server.")
          }
        }).catch((error) => {
        console.log("error addding ds: ", error)
        notify.negative("Something went wrong in the server. Dataset not created")
      });
    },

    addSelectedDatasource(ds) {
      console.log(
        "METODO ADDSELECTEDDATASOURCE**************************", ds
      )
      // we can only have one selected ds
      this.selectedDS = []
      this.selectedDS.push(ds)
    },
    SelectOneDatasource() {

      if (this.datasources.length > 0) {
        this.selectedDS = []
        this.selectedDS.push(this.datasources[0])
      }

    },

    deleteSelectedDatasource(ds) {
      console.log("deleteselect: ", ds)
      console.log("sources ; ", this.selectedDS)
      let index = this.selectedDS.indexOf(ds)
      if (index > -1) {
        console.log("dele index")
        this.selectedDS.splice(index, 1)
      } else {

        this.selectedDS = this.selectedDS.filter(x => x.id !== ds.id)
        // console.log("check!!! something wrong else delete selected ds")
        // this.selectedDS =
      }
    },

    addAligment(aligment, refactor) {
      console.log("alignment store: ", aligment)

      let a = {}
      if (refactor) {

        a.iriA = aligment.resourceA.iri
        a.labelA = aligment.resourceA.label
        a.iriB = aligment.resourceB.iri
        a.labelB = aligment.resourceB.label
        a.l = aligment.integratedLabel
        a.type = aligment.type
        a.similarity = aligment.similarity

      } else {
        a = aligment
      }

      this.alignments.push(a)
    },
    integrate(callback) {
      const notify = useNotify()

      var data = {
        dsB: this.selectedDS[0],
        alignments: this.alignments
      }

      integrationAPI.integrate(this.project.projectId, data).then((response) => {
        console.log("integration response...", response)
        //   console.log(response)
        if (response.status === 201 || response.status) {
          notify.positive("Integration succeeded")

          this.projectTemporal = response.data.project
          this.joinAlignments = response.data.joins
          if (callback)
            callback()
        } else {
          notify.negative("There was an error for the integration task")
        }
      }).catch((error) => {
        console.log("error integrating ds")
        notify.negative("Something went wrong in the server. No possible to integrate it " + error)
      });
    },
    reviewAlignments(callback) {
      const notify = useNotify()

      if (this.joinAlignments.length > 0) {

        integrationAPI.reviewAlignments(this.project.projectId, this.joinAlignments).then((response) => {
          console.log("join integration response...", response)

          if (response.status === 201 || response.status) {
            notify.positive("Integration succeeded")

            this.projectTemporal = response.data
            console.log("***",this.projectTemporal)
            console.log(response.data)


            if (callback)
              callback()

          } else {
            notify.negative("There was an error for the integration task")
          }

        }).catch((error) => {
          console.log("error integrating ds")
          notify.negative("Something went wrong in the server. No possible to integrate it " + error)
        });


      }


    },
    persistIntegration() {
      const notify = useNotify()

      console.log("project id ", this.project.projectId)
      // acceptIntegration
      integrationAPI.persistIntegration(this.project.projectId).then((response) => {
        console.log("integration response...", response)
        if (response.status === 200) {
          notify.positive("Integration saved successfully")
        } else {
          notify.negative("There was an error to save the integration")
        }
      }).catch((error) => {
        console.log("error saving integration", error)
        notify.negative("Something went wrong in the server. No possible to save integration")
      });


    },
    deleteAligment(aligment) {

      console.log("aligment is ", aligment)
      let index = this.alignments.indexOf(aligment)
      console.log("index is ", index)
      this.alignments.splice(index, 1)

    },
    deleteJoinAlignment(alignment) {

      console.log("join alignment is ", alignment)
      let index = this.joinAlignments.indexOf(alignment)
      console.log("index is ", index)
      this.joinAlignments.splice(index, 1)

    },
    getAutomaticAlignments() {
      console.log("getting alignments survey....", this.selectedDS[0].id)
      const notify = useNotify()

      integrationAPI.getAutomaticAlignments(this.project.projectId, this.selectedDS[0].id).then((response) => {
        console.log("survey alignments response...", response)

        if (response.status === 200) {
          this.alignments = response.data
          notify.positive(response.data.length + " automatic alignments found")
          this.chargingAlignments = false
        } else if (response.status === 204){
          this.chargingAlignments = false
          notify.negative("No automatic alignments found")
        }
      }).catch((error) => {
        console.log("error alignments survye ", error)
        notify.negative("Something went wrong in the server.")
      });

    },
    async downloadSourceTemporal(dsID) {
      console.log("download....", dsID)


      const authStore = useAuthStore()
      const notify = useNotify()
      const response = await integrationAPI.downloadSourceGraph(this.project.projectId, dsID, authStore.user.accessToken);

      const content = response.headers['content-type'];
      download(response.data, "source_graph.ttl", content)

    },


  }


})
