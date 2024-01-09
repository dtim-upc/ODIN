import {odinApi} from 'boot/axios';

export default {
  getRepositories(projectID) {
    return odinApi.get('/project/' + projectID + '/repositories')
  },
  editRepository(data) {
    return odinApi.post('/editRepository', data)
  },
  deleteRepository(projectID, repositoryID) {
    return odinApi.delete('/project/' + projectID + '/repository/' + repositoryID)
  }
}
