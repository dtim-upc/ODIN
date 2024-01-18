import {odinApi} from 'boot/axios';

export default {
  postRepository(projectID, data) {
    return odinApi.post('/project/' + projectID + '/repository', data)
  },
  getAllRepositories(projectID) {
    return odinApi.get('/project/' + projectID + '/repositories')
  },
  putRepository(repositoryID, projectID, data) {
    return odinApi.put('/project/' + projectID + '/repository/' + repositoryID, data)
  },
  deleteRepository(projectID, repositoryID) {
    return odinApi.delete('/project/' + projectID + '/repository/' + repositoryID)
  }
}
