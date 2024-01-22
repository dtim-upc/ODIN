import {defineStore} from 'pinia'
import {useNotify} from 'src/use/useNotify.js'
import queryAPI from "src/api/queryAPI.js";

export const useQueriesStore = defineStore('queries', {

  state: () => ({
    queries: [],
    selectedDataProductPath: null,
  }),

  getters: {},
  actions: {
    async init() {

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
  }
})
