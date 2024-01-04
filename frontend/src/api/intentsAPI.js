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
}
