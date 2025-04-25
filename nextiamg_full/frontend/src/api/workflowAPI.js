import {odinApi} from 'boot/axios';

export default {
  postWorkflow(projectID, intentID, data) {
    return odinApi.post('/project/' + projectID + '/intent/' + intentID + '/workflow', data)
  },
  putWorkflow(projectID, intentID, workflowID, data) {
    return odinApi.put('/project/' + projectID + '/intent/' + intentID + '/workflow/' + workflowID, data)
  },
  deleteWorkflow(projectID, intentID, workflowID) {
    return odinApi.delete('/project/' + projectID + '/intent/' + intentID + '/workflow/' + workflowID)
  },
  downloadWorkflowSchema(projectID, intentID, workflowID) {
    return odinApi.get('/project/' + projectID + '/intent/' + intentID + '/workflow/' + workflowID + '/schema', {responseType: 'blob'})
  },
}
