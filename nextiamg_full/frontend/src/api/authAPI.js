import {odinApi} from 'boot/axios';

export default {
  registerUser(credentials) {
    return odinApi.post('/signup', credentials)
  },
  login(credentials) {
    return odinApi.post('/login', credentials)
  },
}
