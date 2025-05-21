import {odinApi} from 'boot/axios';

export default {
  postDataset(projectID, data) {
    return odinApi.post('/project/' + projectID + '/dataset', data)
  },
  getAllDatasets(projectID) {
    return odinApi.get('/project/' + projectID + '/datasets')
  },
  deleteDataset(projectID, datasetID) {
    return odinApi.delete('/project/' + projectID + '/dataset/' + datasetID)
  },
  putDataset(projectID, datasetID, data) {
    return odinApi.put('/project/' + projectID + '/dataset/' + datasetID, data)
  },
  downloadDatasetSchema(projectID, datasetID) {
    return odinApi.get('/project/' + projectID + '/dataset/' + datasetID + '/schema', {responseType: 'blob'})
  },
  setDatasetSchemaAsProjectSchema(projectID, datasetID) {
    return odinApi.post('/project/' + projectID + '/dataset/' + datasetID + '/set-project-schema')
  },
  downloadFile(url) {
    return odinApi.get('/download-file?url=' + encodeURIComponent(url), {responseType: 'arraybuffer'})
  },
  makeAPIRequest(url) {
    return odinApi.get('/make-request?url=' + encodeURIComponent(url), {responseType: 'arraybuffer',})
  }
}
