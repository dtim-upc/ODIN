import { defineStore } from 'pinia';
import { useNotify } from 'src/use/useNotify.js';
import dataProductAPI from "src/api/dataProductsAPI";

const notify = useNotify();

export const useDataProductsStore = defineStore('dataProducts', {
  state: () => ({
    dataProducts: [],
    selectedDataProductPath: "",
  }),

  actions: {

    // ------------ CRUD operations
    async getDataProducts(projectID) {
      try {
        const response = await dataProductAPI.getDataProducts(projectID);
        this.dataProducts = response.data === "" ? [] : response.data;
      } catch (error) {
        notify.negative("Error retrieving data products");
        console.error("Error:", error);
      }
    },
    
    async postDataProduct(projectID, data) {
      try {
        await dataProductAPI.postDataProduct(projectID, data);
        notify.positive("Data product stored successfully");
      } catch (error) {
        notify.negative("Error storing the data product");
        console.error("Error:", error);
      }
    },
    
    async putDataProduct(dataProductID, projectID, data) {
      try {
        await dataProductAPI.putDataProduct(projectID, dataProductID, data);
        notify.positive(`Data product successfully edited`);
        this.getDataProducts(projectID);
      } catch (error) {
        notify.negative("Something went wrong in the server while editing the data.");
        console.error("Error:", error);
      }
    },
    
    async deleteDataProduct(projectID, dataProductID) {
      try {
        await dataProductAPI.deleteDataProduct(projectID, dataProductID);
        notify.positive(`Data product deleted successfully`);
        this.getDataProducts(projectID);
      } catch (error) {
        notify.negative("Something went wrong in the server when deleting a data product.");
        console.error("Error:", error);
      }
    },

    // ------------ Other operations
    async materializeDataProduct(projectID, dataProductID) {
      try {
        const response = await dataProductAPI.materializeDataProduct(projectID, dataProductID);
        this.selectedDataProductPath = response.data;
      } catch (error) {
        notify.negative("Error materializing the data");
        console.error("Error:", error);
      }
    }
  }
});