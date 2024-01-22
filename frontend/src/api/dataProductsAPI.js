import {odinApi} from 'boot/axios';

export default {
  getDataProducts(projectID) {
    return odinApi.get('/project/' + projectID + '/data-products')
  },  
  materializeDataProduct(projectID, dataProductID) {
    return odinApi.post('/project/' + projectID + '/data-products/' + dataProductID + '/materialize')
  },
  postDataProduct(projectID, data) {
    return odinApi.post('/project/' + projectID + '/data-product', data)
  },
}
