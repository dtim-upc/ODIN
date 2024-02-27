import {odinApi} from 'boot/axios';

export default {
  getDataProducts(projectID) {
    return odinApi.get('/project/' + projectID + '/data-products')
  },  
  materializeDataProduct(projectID, dataProductID) {
    return odinApi.post('/project/' + projectID + '/data-product/' + dataProductID + '/materialize')
  },
  postDataProduct(projectID, data) {
    return odinApi.post('/project/' + projectID + '/data-product', data)
  },
  deleteDataProduct(projectID, dataProductID) {
    return odinApi.delete('/project/' + projectID + '/data-product/' +  dataProductID)
  },
  putDataProduct(projectID, dataProductID, data) {
    return odinApi.put('/project/' + projectID + '/data-product/' +  dataProductID, data)
  },
  downloadTemporalDataProduct(projectID, dataProductUUID) {
    return odinApi.post('/project/' + projectID + '/download-temporal-data-product/' +  dataProductUUID)
  },
  downloadDataProduct(projectID, dataProductID) {
    return odinApi.post('/project/' + projectID + '/data-product/' +  dataProductID + '/download')
  },
}
