import { defineStore } from 'pinia';
import { useNotify } from 'src/use/useNotify.js';
import dataProductAPI from "src/api/dataProductsAPI";
import download from 'downloadjs';

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
        notify.negative("Error editing the data product");
        console.error("Error:", error);
      }
    },
    
    async deleteDataProduct(projectID, dataProductID) {
      try {
        await dataProductAPI.deleteDataProduct(projectID, dataProductID);
        notify.positive(`Data product deleted successfully`);
        this.getDataProducts(projectID);
      } catch (error) {
        notify.negative("Error deleting the data product.");
        console.error("Error:", error);
      }
    },

    // ------------ Download/materialize operations
    async materializeDataProduct(projectID, dataProductID) { // To be ingested by the intent API
      try {
        const response = await dataProductAPI.materializeDataProduct(projectID, dataProductID);
        this.selectedDataProductPath = response.data;
      } catch (error) {
        notify.negative("Error materializing the data");
        console.error("Error:", error);
      }
    },

    async downloadTemporalDataProduct(projectID, dataProductUUID) { // The user downloads it from the frontend
      try {
        const response = await dataProductAPI.downloadTemporalDataProduct(projectID, dataProductUUID);
        const content = response.headers['content-type'];
        download(response.data, "result.csv", content);
      } catch (error) {
        notify.negative("Error downloading the data");
        console.error("Error:", error);
      }
    },

    async downloadDataProduct(projectID, dataProduct) { // The user downloads it from the frontend
      try {
        const response = await dataProductAPI.downloadDataProduct(projectID, dataProduct.id);
        const content = response.headers['content-type'];
        download(response.data, dataProduct.datasetName + ".csv", content);
      } catch (error) {
        notify.negative("Error downloading the data");
        console.error("Error:", error);
      }
    }
  }
});