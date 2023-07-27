import {odinApi} from 'boot/axios';



export default {

    integrateJoins(projectID, joins,token) { return odinApi.post('/project/'+projectID+'/integration/join', joins, {headers: { Authorization: `Bearer ${token}` }})  },

    integrate(projectID,data, token) { return odinApi.post('/project/'+projectID+'/integration', data, {headers: { Authorization: `Bearer ${token}` }})  },

    finishIntegration(projectID, token){ return odinApi.post('/project/'+projectID+'/integration/persist', null,{headers: { Authorization: `Bearer ${token}` }}) },

    surveyAlignments(projectID, data, token){ return odinApi.post('/project/'+projectID+'/integration/survey', data,{headers: { 'Content-Type': 'text/plain', Authorization: `Bearer ${token}` }}) },

    downloadSourceGraph(projectID, datasourceID, token) { return odinApi.get('/project/'+projectID+'/integration/download/sourcegraph',  {headers: { Authorization: `Bearer ${token}` }, params: { dsID: datasourceID }, responseType: 'blob'  }    ) },
}
