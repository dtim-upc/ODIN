import {odinApi} from 'boot/axios';

// export function dataSourcesAPI(data) {

//     const createDSTemp = data =>  odinApi.post('/dataSource', data, {headers: {'Content-Type': 'multipart/form-data'},})


//     return {
//         createDSTemp,
//     }


// }

// var dataSourcesAPI = {
//     createDSTemp( data) {  odinApi.post('/dataSource', data, {headers: {'Content-Type': 'multipart/form-data'},}) }
// }


// export default dataSourcesAPI

// export function createDSTemp(data) {

//     return odinApi.post('/dataSource', data, {headers: {'Content-Type': 'multipart/form-data'},})

// }


export default {
  bootstrap(projectID, token, data) { return odinApi.post('/project/'+projectID, data) },
  getAll (projectID, token) { return odinApi.get('/project/'+projectID+'/datasources'/*, {headers: { Authorization: `Bearer ${token}` }} */  ) },
  deleteDatasource(projectID, id, token) {return odinApi.delete('/project/'+projectID+'/datasource/'+ id/*, {headers: { Authorization: `Bearer ${token}` }}*/ )},


  createDSPersistent (projectID, datasource, token) { return odinApi.post('/project/'+projectID+'/datasources/persist', datasource,  {headers: { Authorization: `Bearer ${token}` }})},
  getTriples(projectID, datasourceID, token) {return odinApi.get('/project/'+projectID+'/datasources/triples/'+datasourceID, {headers: { Authorization: `Bearer ${token}` }}   )},

  getAllTemporal (projectID, token) { return odinApi.get('/project/'+projectID+'/temp/ds', {headers: { Authorization: `Bearer ${token}` }}   ) },
  deleteTemporal(projectID, id, token) {return odinApi.delete('/project/'+ projectID + '/temp/ds/'+id, {headers: { Authorization: `Bearer ${token}` }} )},

  downloadSourceGraph(projectID, datasourceID, token) { return odinApi.get('/project/'+projectID+'/datasources/download/sourcegraph',  {headers: { Authorization: `Bearer ${token}` }, params: { dsID: datasourceID }, responseType: 'blob'  }    ) },
    downloadProjectGraph(projectID, token) { return odinApi.get('/project/'+projectID+'/integration/download/projectschema',  {headers: { Authorization: `Bearer ${token}` }, responseType: 'blob'  }    ) },
}
