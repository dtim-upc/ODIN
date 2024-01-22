import {defineStore} from 'pinia'
import {useNotify} from 'src/use/useNotify.js'
import dataProductAPI from "src/api/dataProductsAPI";

export const useDataProductsStore= defineStore('dataProducts', {

  state: () => ({
    dataProducts: [],
    selectedDataProductPath: "",
  }),

  getters: {},
  actions: {
    init() {
      
    },

    async getDataProducts(projectID) {
        await dataProductAPI.getDataProducts(projectID)
          .then(response => {
            console.log("Data products received")
            console.log(response.data)
            if (response.data === "") { // when no queries, api answer ""
              this.dataProducts = []
            } else {
              this.dataProducts = response.data
            }
  
          }).catch(err => {
          console.log("error retrieving data products")
          console.log(err)
        })
      },
  
      async materializeDataProduct(projectID, dataProductID) {
        await dataProductAPI.materializeDataProduct(projectID, dataProductID)
          .then(response => {
            console.log("Data product materialized")
            this.selectedDataProductPath = response.data
          }).catch(err => {
          console.log("error retrieving queries")
          console.log(err)
        })
      },
  
      postDataProduct(projectID, data) {
        const notify = useNotify();
  
        dataProductAPI.postDataProduct(projectID, data).then(response => {
      
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
