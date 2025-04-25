import { defineStore } from 'pinia'
import { useNotify } from 'src/use/useNotify.js'
import datasetsAPI from "src/api/datasetsAPI.js"
import download from 'downloadjs'
import { useProjectsStore } from './projectsStore'

const notify = useNotify()
const projectsStore = useProjectsStore()

export const useDatasetsStore = defineStore('datasets', {
  state: () => ({
    datasets: [],
  }),

  getters: {
    getDatasetsNumber() {
      return this.datasets.length
    },
  },

  actions: {

    // ------------ CRUD operations
    async getDatasets(projectId) {
      try {
        const response = await datasetsAPI.getAllDatasets(projectId)
        this.datasets = response.data || []
      } catch (error) {
        notify.negative("Error retrieving datasets")
        console.error("Error:", error)
      }
    },

    async postDataset(projectID, data, success) {
      try {
        const response = await datasetsAPI.postDataset(projectID, data)
        this.getDatasets(projectID)
        success(response.data)
      } catch (error) {
        notify.negative("Error creating datasets")
        console.error("Error:", error)
      }
    },

    async putDataset(projectID, datasetID, data, successCallback) {
      try {
        await datasetsAPI.putDataset(projectID, datasetID, data)
        notify.positive(`Dataset successfully edited`)
        this.getDatasets(projectID)
        successCallback()
      } catch (error) {
        notify.negative("Error when editing the dataset")
        console.error("Error:", error)
      }
    },

    async deleteDataset(projectID, datasetID) {
      try {
        await datasetsAPI.deleteDataset(projectID, datasetID)
        notify.positive("Dataset successfully deleted")
        this.getDatasets(projectID)
      } catch (error) {
        notify.negative("Error when deleting the dataset")
        console.error("Error:", error)
      }
    },

    // ---------------- Graph/Integration related operations

    async setDatasetSchemaAsProjectSchema(projectID, datasetID) {
      try {
        await datasetsAPI.setDatasetSchemaAsProjectSchema(projectID, datasetID)
        projectsStore.updateCurrentProject()
        notify.positive("Schema successfully set")
      } catch (error) {
        console.error("Error:", error)
        notify.negative("Error setting the project schema")
      }
    },

    async downloadDatasetSchema(projectID, datasetID) {
      try {
        const response = await datasetsAPI.downloadDatasetSchema(projectID, datasetID)
        const content = response.headers['content-type']
        const datasetName = this.datasets.find(dataset => dataset.id === datasetID).datasetName
        download(response.data, datasetName + ".ttl", content)
      } catch (error) {
        console.error("Error:", error)
        notify.negative("Error downloading the schema")
      }
    },

    // ---------------- Operations to get data from sources (which later will become datasets)

    async downloadFile(url) {
      try {
        const response = await datasetsAPI.downloadFile(url)
    
        let filename
        const contentDisposition = response.headers['content-disposition']
        // If the header in content-disposition exists, get the file name, if not, try to get the file name from the URL
        if (contentDisposition) {
          filename = contentDisposition.split('')[1].trim().split('=')[1]
        } else {
          const urlParts = url.split('/')
          filename = urlParts[urlParts.length - 1]
        }
        // Create a new file object from the response
        const blob = new Blob([response.data], {type: 'application/octet-stream'})
        const file = new File([blob], filename, {type: 'application/octet-stream'})
        return file
      } catch (error) {
        console.error("Error:", error)
        notify.negative("Error when downloading the file")
        return null
      }
    },

    async makeAPIRequest(url) {
      try {
        const response = await datasetsAPI.makeAPIRequest(url)
  
        let filename
        const contentDisposition = response.headers['content-disposition']
        // If the header in content-disposition exists, get the file name, if not, try to get the file name from the URL in the repository
        if (contentDisposition) {
          const match = contentDisposition.match(/filename="(.+)"/)
          if (match) {
            filename = match[1]
          }
        } else {
          const urlParts = url.split('/')
          filename = urlParts[urlParts.length - 1] + ".json"
        }
  
        // Create a new file object from the response
        const blob = new Blob([response.data], {type: 'application/json'})
        const file = new File([blob], filename, {type: 'application/json'})
        return file
      } catch (error) {
        console.error("Error:", error)
        notify.negative("Error when requesting the file to the API")
        return null
      }
    }
  }
})