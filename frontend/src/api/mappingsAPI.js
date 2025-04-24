import { odinApi } from 'boot/axios';

export default {
  downloadMappings(projectID, type) {
    return odinApi.get('/project/' + projectID + '/mappings/' + type + '/download', {
      responseType: 'blob'
    });
  }
}
