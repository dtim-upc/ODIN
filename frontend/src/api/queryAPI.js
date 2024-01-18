import {odinApi} from 'boot/axios';

export default {
  getQueries(projectID) {
    return odinApi.get('/project/' + projectID + '/queries')
  },
  queryGraph(projectID, data) {
    return odinApi.post('/project/' + projectID + '/query-graph', data)
  },
  postQuery(projectID, data) {
    return odinApi.post('/project/' + projectID + '/query', data)
  },
}
