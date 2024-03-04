import {intentsApi} from 'boot/axios';
import {odinApi} from 'boot/axios';

export default {
  postIntent(projectID, data) {
    return odinApi.post('/project/' + projectID + '/intent', data)
  },
  getAllIntents(projectID) {
    return odinApi.get('/project/' + projectID + '/intents')
  },
  deleteIntent(projectID, intentID) {
    return odinApi.delete('/project/' + projectID + '/intent/' + intentID)
  },
  putIntent(intentID, projectID, data) {
    return odinApi.put('/project/' + projectID + '/intent/' + intentID, data)
  },
  annotateDataset(data) {
    return intentsApi.post('/annotate_dataset', data)
  },
  getProblems() {
    return intentsApi.get('/problems')
  },
  setAbstractPlans(data) {
    return intentsApi.post('/abstract_planner', data)
  },
  setLogicalPlans(data) {
    return intentsApi.post('/logical_planner', data)
  },
  downloadKNIME(data) {
    return intentsApi.post('/workflow_plans/knime', data, {responseType: 'blob'})
  },
  downloadAllKNIME(data) {
    return intentsApi.post(`/workflow_plans/knime/all`, data, {responseType: 'blob'})
  },
  downloadProactive(data) {
    return intentsApi.post('/workflow_plans/proactive', data, {responseType: 'blob'})
  },
}