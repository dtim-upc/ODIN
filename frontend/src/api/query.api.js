import {odinApi} from 'boot/axios';


const headers = (token) => {
      return {headers: { Authorization: `Bearer ${token}` }}
}

export default {

    queryGraph(data, projectID, token) { return odinApi.post('/query/'+projectID+'/graphical', data, headers(token)) },

}