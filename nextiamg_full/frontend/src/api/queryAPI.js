import {odinApi} from 'boot/axios';

export default {
  queryGraph(projectID, data) {
    return odinApi.post('/project/' + projectID + '/query-graph', data)
  },
}
