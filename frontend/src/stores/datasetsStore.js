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

        if (contentDisposition) {
          const match = contentDisposition.match(/filename="?([^"]+)"?/)
          if (match) {
            filename = match[1]
          } else {
            // Fallback if format is not standard
            filename = 'downloaded_file'
          }
        } else {
          const urlParts = url.split('/')
          filename = urlParts[urlParts.length - 1]
        }

        const contentType = response.headers['content-type'] || 'application/octet-stream'
        const blob = new Blob([response.data], { type: contentType })
        const file = new File([blob], filename, { type: contentType })

        return file
      } catch (error) {
        console.error("Error:", error)
        notify.negative("Error when downloading the file")
        return null
      }
    },

    async makeAPIRequest(url) {
      try {
        const response = await datasetsAPI.makeAPIRequest(url) // Should return blob + headers

        let filename
        const contentDisposition = response.headers['content-disposition']

        if (contentDisposition) {
          const match = contentDisposition.match(/filename="?([^"]+)"?/)
          if (match) {
            filename = match[1]
          }
        } else {
          const urlParts = url.split('/')
          const baseName = urlParts[urlParts.length - 1]
          const contentType = response.headers['content-type'] || ''

          if (contentType.includes('application/json')) {
            filename = baseName.toLowerCase().endsWith('.json') ? baseName : baseName + '.json'
          } else if (contentType.includes('text/csv')) {
            filename = baseName.toLowerCase().endsWith('.csv') ? baseName : baseName + '.csv'
          } else {
            filename = baseName // Fallback: use as-is
          }
        }

        const contentType = response.headers['content-type'] || 'application/octet-stream'
        const blob = new Blob([response.data], { type: contentType })
        const file = new File([blob], filename, { type: contentType })
        return file

      } catch (error) {
        console.error("Error:", error)
        notify.negative("Error when requesting the file to the API")
        return null
      }
    }
  }
})
