import { defineStore } from 'pinia'
import {useNotify} from 'src/use/useNotify.js'
import api from "src/api/dataSourcesAPI.js";
import integrationAPI from "src/api/integration.api.js";
import { useAuthStore } from 'stores/auth.store.js'
import { useRoute } from "vue-router";
import projectAPI from 'src/api/projectAPI';
import download from 'downloadjs'
// const notify = useNotify()

// let notify;
// let authStore;
// let route;


export const useIntegrationStore = defineStore('integration',{

    state: () => ({
        project : {},
        projectTemporal: {},
        datasources: [], // these ds are in temporal landing
        selectedDS: [], //selected ds. It is an array because of table requirement input but it will always contain only one element
        alignments: [],
        joinAlignments: []
          // {"domainLabelA":"person", "domainLabelB":"country", "rightArrow":"true" ,"iriA": "A", "iriB": "B", "labelA": "lA", "labelB": "lB", "l": "i2", "type":"property" }]
    }),

    getters : {
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
        getDatasourcesNumber(state){
          return state.datasources.length
        },
        getSourceB(state) {
            if(state.selectedDS.length == 1)
                return state.selectedDS[0]
            else
                return null
        },
        getGraphicalA(state){
          //todo see if works
            return state.project.integratedGraph.graphicalSchema
        },
        getGraphicalB(state){
          if(state.selectedDS.length == 1){
            console.log("******************"+state.selectedDS[0])
              return state.selectedDS[0].localGraph.graphicalSchema
          }
          else
              return ""
        },
        getGlobalSchema(state){

          if(state.projectTemporal.integratedGraph.globalGraph.graphicalSchema)
            return state.projectTemporal.integratedGraph.globalGraph.graphicalSchema
          return ""
        },
        getGraphicalSchemaIntegration(state){
          if(state.projectTemporal.integratedGraph.graphicalSchema)
            return state.projectTemporal.integratedGraph.graphicalSchema
           return ""
        },
        isDSEmpty(state) {
            return state.datasources.length == 0
        },
        isJoinAlignmentsRelationshipsComplete(state){

          if(state.joinAlignments.length == 0)
            return true;

          if(state.joinAlignments.filter(a => a.relationship == "").length == 0){
            return true;
          }
          return false;

        }
    },
    actions: {

      async init(){

        // console.log("integration stores init...")
        // // const notify  = useNotify()
        // const authStore = useAuthStore()
        // const route = useRoute()
        // if(authStore.user.accessToken && !this.project.name){
        //   const response = await projectAPI.getProjectByID(route.params.id, authStore.user.accessToken)

        //     if(response.status == 200) {
        //       this.project = response.data
        //       // console.log("project assigned ", this.project)
        //     } else {
        //       console.log("something wrong with response: ", response)
        //     }
        // }
        // if(authStore.user.accessToken && this.datasources.length === 0) {
        //    this.getTemporalDatasources()
        //  }
      },
        async setProject(p){
          const route = useRoute()
          const authStore = useAuthStore()
          console.log("setting project to integration store..")

          if(p) {
           this.project = p

          } else if (!this.project.projectName) {

            const response = await projectAPI.getProjectByID(route.params.id, authStore.user.accessToken)

            if(response.status == 200){
              this.project = response.data
            }


          }
          // console.log("Ds: ",this.datasources.length)
          // && this.datasources.length === 0
          if(authStore.user.accessToken ) {
            console.log("retrieving temporal data sources...")
            this.getTemporalDatasources()
            this.alignments = []
          }

        },
        deleteTemporalDS(ds){
          const notify  = useNotify()
          const authStore = useAuthStore()
            api.deleteTemporal(this.project.projectId, ds.id, authStore.user.accessToken).then( response => {

              console.log("delete ds temporal")
              console.log(response.data)

              if(response.status == 204) {
                let index = this.datasources.indexOf(ds)
                if(index > -1) {
                    console.log("dele index")
                    this.datasources.splice(index,1)
                } else {
                    console.log("something wrong, could not find data source in array to delete it")
                    // this.selectedDS =
                }

                if(this.selectedDS.length > 0) {

                  if(this.selectedDS[0].id === ds.id){
                    console.log("data source deleter from selection")
                    this.selectedDS = []
                  }


                }



              } else {
                console.log("check status!!! something wrong: ",response)
              }


            }).catch(err => {
              console.log("error deleting data sources")
              // check how to get err status e.g., 401
              console.log(err)
              notify.negative("Error deleting data source")
            })


        },

        async getTemporalDatasources() {
          const notify  = useNotify()
          const authStore = useAuthStore()

            console.log("Pinia getting temporal data sources...")
            const res = await api.getAll(this.project.projectId, authStore.user.accessToken).then(response => {

              console.log("ds temporal received", response)

              if(response.data === "") { // when no datasources, api answer ""
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
              } else if(err.response && err.response.status === 404){
                this.datasources = []
                notify.positive("There are no data sources yet. Add sources to see them.")
              }
              else {
                notify.negative("Cannot connect to the server.")
              }
            });

        },

        // this will upload the data source
        addDataSource(projectID, data, success){
          const notify  = useNotify()
          const authStore = useAuthStore()
            console.log("adding data source...", data)
            api.bootstrap(projectID, authStore.user.accessToken, data)
              .then((response) => {
                console.log("dataset created ",response)
                if (response.status == 200) {

                  // this should be in temporal landing
                  //this.datasources.push(response.data)
                  this.project.datasets.push(response.data)

                  // this.temporalDatasources.push(response.data)

                  success(response.data);
                  // storeDS.addDataSource(response.data)

                } else {
                  // console.log("error")
                  notify.negative("Cannot create datasource. Something went wrong in the server.")
                }
              }).catch( (error) => {
              console.log("error addding ds: ", error)
              notify.negative("Something went wrong in the server.")
            });
        },
        addSelectedDatasource(ds){
        console.log(
          "METODO ADDSELECTEDDATASOURCE**************************",ds
        )
          // we can only have one selected ds
          this.selectedDS = []
          this.selectedDS.push(ds)
        },
        SelectOneDatasource(){

          if(this.datasources.length > 0) {
            this.selectedDS = []
            this.selectedDS.push(this.datasources[0])
          }

        },

        deleteSelectedDatasource(ds){
          console.log("deleteselect: ", ds)
          console.log("sources ; ",this.selectedDS)
            let index = this.selectedDS.indexOf(ds)
            if(index > -1) {
                console.log("dele index")
                this.selectedDS.splice(index,1)
            } else {

              this.selectedDS = this.selectedDS.filter(x => x.id != ds.id)
                // console.log("check!!! something wrong else delete selected ds")
                // this.selectedDS =
            }
        },
        finishIntegration(ds){
          this.deleteSelectedDatasource(ds);

          let index = this.datasources.indexOf(ds)
                if(index > -1) {
                    console.log("dele index")
                    this.datasources.splice(index,1)
                } else {
                    console.log("something wrong, could not find data source in array to delete it")
                    // this.selectedDS =
                }

        },




        addAligment(aligment, refactor) {
            console.log("alignment store: ", aligment)

            let a = {}
            if(refactor){

                a.iriA = aligment.resourceA.iri
                a.labelA = aligment.resourceA.label
                a.iriB = aligment.resourceB.iri
                a.labelB = aligment.resourceB.label
                a.l = aligment.integratedLabel
                a.type = aligment.type

            } else {
                a = aligment
            }

            this.alignments.push(a)
        },
        integrateTemporal( callback){
          const authStore = useAuthStore()
          const notify = useNotify()

          var data = {
              dsB: this.selectedDS[0],
              alignments: this.alignments
            }

          integrationAPI.integrate( this.project.projectId, data, authStore.user.accessToken ).then((response) => {
              console.log("integration response...", response)
            //   console.log(response)
              if (response.status == 201 || response.status) {
                notify.positive("Integration succeeded")

                this.projectTemporal = response.data.project
                this.joinAlignments = response.data.joins
                  if(callback)
                    callback()
              } else {
                notify.negative("There was an error for the integration task")
              }
            }).catch( (error) => {
              console.log("error integrating ds")
            notify.negative("Something went wrong in the server. No possible to integrate it")
          });
        },
        integrateJoins(callback ) {
          const authStore = useAuthStore()
          const notify = useNotify()

          if(this.joinAlignments.length > 0 ){

            integrationAPI.integrateJoins(this.project.projectId, this.joinAlignments, authStore.user.accessToken).then((response) => {
              console.log("join integration response...", response)

              if (response.status == 201 || response.status) {
                notify.positive("Integration succeeded")

                this.projectTemporal = response.data
                if(callback)
                    callback()

              }else{
                notify.negative("There was an error for the integration task")
               }

            }).catch( (error) => {
              console.log("error integrating ds")
            notify.negative("Something went wrong in the server. No possible to integrate it")
          });


          }



        },
        saveIntegration(callback){

          const authStore = useAuthStore()
          const notify = useNotify()


          console.log("save intregration store...",authStore.user.accessToken )
          console.log("project id ", this.project.projectId )
          // acceptIntegration
          integrationAPI.finishIntegration( this.project.projectId, authStore.user.accessToken ).then((response) => {
            console.log("integration response...", response)

            if (response.status == 200) {

              // this.projectTemporal = response.data
                if(callback)
                  callback(this.selectedDS[0])
                notify.positive("Integration saved successfully")
            } else {
              notify.negative("There was an error to save the integration")
            }
          }).catch( (error) => {
            console.log("error saving integration", error)
          notify.negative("Something went wrong in the server. No possible to save integration")
        });


        },
        deleteAligment(aligment) {

          console.log("aligment is ", aligment)
          let index = this.alignments.indexOf(aligment)
          console.log("index is ", index)
          this.alignments.splice(index,1)

        },
        deleteJoinAlignment(alignment) {

          console.log("join alignment is ", alignment)
          let index = this.joinAlignments.indexOf(alignment)
          console.log("index is ", index)
          this.joinAlignments.splice(index,1)

        },
        getAlignmentsSurvey(){
          console.log("getting alignments survey....", this.selectedDS[0].id)
          const authStore = useAuthStore()
          const notify = useNotify()


          integrationAPI.surveyAlignments(this.project.projectId, this.selectedDS[0].id ,authStore.user.accessToken).then((response) => {
            console.log("survey alignments response...", response)

            if (response.status == 200) {
                this.alignments = response.data
            }
          }).catch( (error) => {
            console.log("error alignments survye ", error)
          notify.negative("Something went wrong in the server.")
        });

        },
        async downloadSourceTemporal(dsID){
          console.log("download....",dsID)



          const authStore = useAuthStore()
          const notify  = useNotify()
          const response = await integrationAPI.downloadSourceGraph(this.project.projectId,dsID,authStore.user.accessToken);

          const content = response.headers['content-type'];
          download(response.data, "source_graph.ttl", content)

        },





    }



})
