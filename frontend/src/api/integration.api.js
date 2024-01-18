import {odinApi} from 'boot/axios';

export default {
  integrate(projectID, data) {
    return odinApi.post('/project/' + projectID + '/integration', data)
  },
  reviewAlignments(projectID, joins) {
    return odinApi.post('/project/' + projectID + '/integration/review-alignments', joins)
  },
  persistIntegration(projectID) {
    return odinApi.post('/project/' + projectID + '/integration/persist')
  },
  getAutomaticAlignments(projectID, datasetToIntegrateID) {
    return odinApi.post('/project/' + projectID + '/integration/compute-automatic-alignments/' + datasetToIntegrateID)
  },

  
  downloadSourceGraph(projectID, datasourceID, token) {
    return odinApi.get('/project/' + projectID + '/integration/download/sourcegraph', {
      headers: {Authorization: `Bearer ${token}`},
      params: {dsID: datasourceID},
      responseType: 'blob'
    })
  },
}
