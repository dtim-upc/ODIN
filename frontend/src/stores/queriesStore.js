import {defineStore} from 'pinia'
import {useNotify} from 'src/use/useNotify.js'
import queryAPI from "src/api/queryAPI.js";

export const useQueriesStore = defineStore('queries', {

  state: () => ({
    queries: [],
  }),

  getters: {},
  actions: {
    async init() {

    },

    async getQueries(projectID) {
      await queryAPI.getQueries(projectID)
        .then(response => {
          console.log("Queries received")
          console.log(response.data)

          if (response.data === "") { // when no queries, api answer ""
            this.queries = []
          } else {
            this.queries = response.data
          }

        }).catch(err => {
        console.log("error retrieving queries")
        console.log(err)
      })
    },

    queryGraph(projectID, data, successCallback) {
      const notify = useNotify();

      console.log("dd", data)
      queryAPI.queryGraph(projectID, data).then(response => {
        console.log("query success", response)
        if (response.data == '')
          notify.positive("Query result is empty")
        else {
          successCallback(response.data)
        }
      }).catch(err => {
        console.log("error query graph", err)
      })
    
    },

    postQuery(projectID, data) {
      const notify = useNotify();

      queryAPI.postQuery(projectID, data).then(response => {
    
        console.log("query success", response)
        if (response.status === 200) {
          notify.positive("Query stored successfully")
        }
      }).catch(err => {
        notify.negative("Error storing query")
        console.log("error query graph", err)
      })
    
    }
  }
})
