import {odinApi} from 'boot/axios';

export default {
  getAllProjects() {
    return odinApi.get('/projects')
  },
  postProject(data) {
    return odinApi.post('/project', data)
  },
  getProject(projectID) {
    return odinApi.get('/project/' + projectID)
  },
  deleteProject(projectID) {
    return odinApi.delete('/project/' + projectID)
  },
  putProject(projectID, data) {
    return odinApi.put('/project/' + projectID, data)
  },
  cloneProject(projectID) {
    return odinApi.post('/project/' + projectID + '/clone')
  },
  downloadProjectGraph(projectID) {
    return odinApi.get('/project/' + projectID + '/schema', {responseType: 'blob'})
  },
  resetProjectSchema(projectID) {
    return odinApi.post('/project/' + projectID + '/reset-schema')
  },
}
