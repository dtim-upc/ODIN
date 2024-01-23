import { defineStore } from 'pinia';
import { useNotify } from 'src/use/useNotify.js';
import { Dark } from 'quasar';
import api from "src/api/authAPI.js";
import { LocalStorage } from 'quasar';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: LocalStorage.getItem('user') ? JSON.parse(LocalStorage.getItem('user')) : {},
    darkMode: LocalStorage.getItem('odinDarkMode') || false,
  }),

  getters: {
    getUserName(state) {
      return state.user.firstName ? state.user.firstName.charAt(0).toUpperCase() + state.user.firstName.slice(1) : "";
    }
  },

  actions: {
    init() {
      Dark.set(this.darkMode);
      if (!LocalStorage.has('odinDarkMode')) {
        LocalStorage.set('odinDarkMode', this.darkMode);
      }
    },

    async login(credentials) {
      try {
        const response = await api.login(credentials);

        if (response.status === 200) {
          this.user = response.data;
          LocalStorage.set('user', JSON.stringify(this.user));
          this.router.push('/projects');
        }
      } catch (error) {
        const notify = useNotify();
        if (error.response && error.response.data.message === 'User not found') {
          notify.negative("Username does not exist");
        } else {
          notify.negative("Password incorrect");
        }
      }
    },

    setDark(flag) {
      Dark.toggle();
      this.darkMode = Dark.isActive;
      LocalStorage.set('odinDarkMode', this.darkMode);
    },

    logout() {
      this.user = {};
      LocalStorage.remove('user');
      LocalStorage.remove('odinDarkMode');
      this.router.push('/auth');
    },

    async registerUser(credentials, successC, callback) {
      try {
        const response = await api.registerUser(credentials);

        if (response.status === 201) {
          if (successC) successC();
          useNotify().positive("User successfully registered");
        }
      } catch (error) {
        if (error.response && error.response.status === 409) {
          callback(error.response.data);
        } else {
          useNotify().negative("Error registering user");
        }
      }
    }
  }
});