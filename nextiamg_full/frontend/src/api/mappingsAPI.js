import { odinApi } from 'boot/axios';

export default {
  downloadMappings(projectID, formData) {
    return odinApi.post('/project/' + projectID + '/mappings/download', formData, { responseType: 'blob' });
  }
}
