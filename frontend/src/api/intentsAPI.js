import {intentsApi} from 'boot/axios';
import {odinApi} from 'boot/axios';

export default {
  postIntent(projectID, data) {
    return odinApi.post('/project/' + projectID + '/intent', data)
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
  /*
  getAbstractPlans() {
    return intentsApi.get('/abstract_plans')
  },*/

  setLogicalPlans(data) {
    return intentsApi.post('/logical_planner', data)
  },
  /*
  getLogicalPlans() {
    return intentsApi.get('/logical_plans')
  },*/
  /*
  setWorkflowPlans(data) {
    return intentsApi.post('/workflow_planner', data)
  },

  getWorkflowPlans() {
    return intentsApi.get('/workflow_plans')
  },*/

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
