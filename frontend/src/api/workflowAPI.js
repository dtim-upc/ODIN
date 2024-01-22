import {odinApi} from 'boot/axios';

export default {
  postWorkflow(projectID, intentID, data) {
    return odinApi.post('/project/' + projectID + '/intent/' + intentID + '/workflow', data)
  },
}
