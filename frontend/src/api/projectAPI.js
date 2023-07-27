import {odinApi} from 'boot/axios';


const headers = (token) => {
      return {headers: { Authorization: `Bearer ${token}` }}
}

export default {
    getAllProjects(token) { return odinApi.get('/projects'/*, headers(token)*/) },
    createProject(data, token) { return odinApi.post('/projects', data /*headers(token)*/) },
    getProjectByID(id, token) { return odinApi.get('/projects/'+id /*, headers(token) */) },
    deleteProjectByID(id, token) { return odinApi.delete('/deleteProject/'+id /*, headers(token) */) },
}
