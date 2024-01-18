import {odinApi} from 'boot/axios';

export default {
  getAllProjects(token) {
    return odinApi.get('/projects'/*, headers(token)*/)
  },
  postProject(data, token) {
    return odinApi.post('/project', data /*headers(token)*/)
  },
  getProject(projectID, token) {
    return odinApi.get('/project/' + projectID /*, headers(token) */)
  },
  deleteProject(projectID, token) {
    return odinApi.delete('/project/' + projectID /*, headers(token) */)
  },
  putProject(projectID, data) {
    return odinApi.put('/project/' + projectID, data)
  },
  cloneProject(projectID) {
    return odinApi.post('/project/' + projectID + '/clone')
  },
  downloadProjectGraph(projectID) {
    return odinApi.get('/project/' + projectID + '/schema', {responseType: 'blob'})
  }
}
