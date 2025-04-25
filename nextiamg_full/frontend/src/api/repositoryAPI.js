import {odinApi} from 'boot/axios';

export default {
  postRepository(projectID, data) {
    return odinApi.post('/project/' + projectID + '/repository', data)
  },
  getRepositories(projectID) {
    return odinApi.get('/project/' + projectID + '/repositories')
  },
  putRepository(repositoryID, projectID, data) {
    return odinApi.put('/project/' + projectID + '/repository/' + repositoryID, data)
  },
  deleteRepository(projectID, repositoryID) {
    return odinApi.delete('/project/' + projectID + '/repository/' + repositoryID)
  },
  testConnection(data) {
    return odinApi.post('/test-connection', data)
  },
  retrieveDBTables(projectID, repositoryId) {
    return odinApi.get('/project/' + projectID + '/repository/' + repositoryId + '/get-tables')
  },
  getRepositoryTypes() {
    return odinApi.get("/data-repository-schemas")
  },
  getRepositorySchema(fileName) {
    return odinApi.get(`/form-schema/` + fileName)
  }
}
