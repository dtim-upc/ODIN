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
  downloadRDF(planID) {
    return intentsApi.get('/workflow_plans/rdf/' + planID)
  },
  downloadKNIME(planID) {
    return intentsApi.get('/workflow_plans/knime/' + planID)
  },
  downloadAllRDF(selectedPlanIds) {
    const ids = selectedPlanIds.join(',');
    return intentsApi.get(`/workflow_plans/rdf/all?ids=${ids}`, {responseType: 'blob'})
  },
  downloadAllKNIME(selectedPlanIds) {
    const ids = selectedPlanIds.join(',');
    return intentsApi.get(`/workflow_plans/knime/all?ids=${ids}`, {responseType: 'blob'})
  },
  storeWorkflow(projectID, intentID, data) {
    return odinApi.post('/project/' + projectID + '/intent/' + intentID + '/workflow', data)
  },
}
