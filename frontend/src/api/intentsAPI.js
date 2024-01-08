import {intentsApi} from 'boot/axios';

export default {
  getDatasets() {
    return intentsApi.get('/datasets')
  },

  getProblems() {
    return intentsApi.get('/problems')
  },

  setAbstractPlans(data) {
    return intentsApi.post('/abstract_planner', data)
  },

  getAbstractPlans() {
    return intentsApi.get('/abstract_plans')
  },

  setLogicalPlans(data) {
    return intentsApi.post('/logical_planner', data)
  },

  getLogicalPlans() {
    return intentsApi.get('/logical_plans')
  },

  setWorkflowPlans(data) {
    return intentsApi.post('/workflow_planner', data)
  },

  getWorkflowPlans() {
    return intentsApi.get('/workflow_plans')
  },

  downloadRDF(planID) {
    return intentsApi.get('/workflow_plans/rdf/' + planID)
  },

  downloadKNIME(planID) {
    return intentsApi.get('/workflow_plans/knime/' + planID)
  },

  downloadAllRDF() {
    return intentsApi.get('/workflow_plans/rdf/all', {responseType: 'blob'})
  },

  downloadAllKNIME() {
    return intentsApi.get('/workflow_plans/knime/all', {responseType: 'blob'})
  },
}
