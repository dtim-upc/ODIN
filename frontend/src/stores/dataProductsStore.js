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
      
      },

      putDataProduct(dataProductID, projectID, data) {
        const notify = useNotify();
  
        dataProductAPI.putDataProduct(projectID, dataProductID, data)
          .then((response) => {
            if (response.status === 200) {
              notify.positive(`Data product successfully edited`);
              this.getDataProducts(projectID)
            } else {
              notify.negative("Cannot edit data. Something went wrong on the server.");
            }
          })
          .catch((error) => {
            console.log("Error is: " + error);
            if (error.response) {
              notify.negative("Something went wrong on the server while editing the data.");
            }
          });
      },

      deleteDataProduct(projectID, dataProductID) {
        const notify = useNotify();
        console.log("Deleting data product with ID ", dataProductID)

        dataProductAPI.deleteDataProduct(projectID, dataProductID).then((response) => {
            if (response.status === 200) {
              notify.positive(`Data product deleted successfully`)
              this.getDataProducts(projectID)
            } else {
              notify.negative("Data product could not be deleted.")
            }
        }).catch((error) => {
            console.log("error is: " + error)
            if (error.response) {
                notify.negative("Something went wrong in the server when deleting a data product.")
            }
        });
    }

  }
})
