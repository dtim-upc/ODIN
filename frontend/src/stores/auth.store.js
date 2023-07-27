import { defineStore } from 'pinia'
import {useNotify} from 'src/use/useNotify.js'
import { Dark } from 'quasar'
import { useProjectsStore } from 'stores/projects.store.js'
import api from "src/api/auth.api.js";
import { LocalStorage, SessionStorage } from 'quasar'

// let notify;
// let projectStore; 

export const useAuthStore = defineStore('auth',{

    state: () => ({
         // initialize state from local storage to enable user to stay logged in
         user:{},
         darkMode: (LocalStorage.getItem('odinDarkMode') || false ),
        //  localStorage.getItem('odinDarkMode') !== null? localStorage.getItem('odinDarkMode'): false,
        //  user: localStorage.getItem('user') !== null? JSON.parse(localStorage.getItem('user')):{},
    }),

    getters : {

        getUserName(state){
            if(state.user.firstName) {
                return state.user.firstName.charAt(0).toUpperCase() + state.user.firstName.slice(1);
              
            } 
            return ""
        }
    },
    actions: {

        // it is initialized in router creation. File src/router/index.js
        init(){
            // console.log("auth init....")
            if(localStorage.getItem('user') !== null)
                this.user = JSON.parse(localStorage.getItem('user'))

                Dark.set(this.darkMode)
            if(localStorage.getItem('odinDarkMode') === null) {
                LocalStorage.set('odinDarkMode', this.darkMode)
                
            } 

            // projectStore = useProjectsStore()
            // notify = useNotify()
            // console.log(projectStore)

        },
        async login(credentials) {
            // todo: i THINK NEEDS A CATCH...LOOK INTO THIS https://jasonwatmore.com/post/2022/05/26/vue-3-pinia-jwt-authentication-tutorial-example#login-view-vue
            // console.log(credentials)
            api.login(credentials).then( response => {

                console.log("login....", response)   
                if(response.status == 200){

                    this.user = response.data   
                     // store user details and jwt in local storage to keep user logged in between page refreshes
                    localStorage.setItem('user', JSON.stringify(this.user));   
                    // redirect to previous url or default to home page
                    // console.log(projectStore)
                    // const projectStore = useProjectsStore()
                    // projectStore.init()
                    
                    this.router.push('/projects');
                }
                    

            }).catch( error => {
                const notify = useNotify()
                    console.log("error login", error)

                    if(error.response) {

                        let r = error.response
                        if( r.data.message == 'User not found' )
                            notify.negative("Username does not exist")
                        else 
                        notify.negative("password incorrect")

                    }
                    
                    

                  });
            // const user = await fetchWrapper.post(`${baseUrl}/authenticate`, { username, password });

            // update pinia state
            // this.user = user;

           

            // redirect to previous url or default to home page
            // router.push(this.returnUrl || '/');
         },
         setDark(flag){
            Dark.toggle()
            this.darkMode = Dark.isActive
            LocalStorage.set('odinDarkMode', this.darkMode)
         }
         ,
         logout() {
            console.log("logout")
            this.user = {};
            localStorage.removeItem('user');
            localStorage.removeItem('odinDarkMode')
            this.router.push('/auth');
        },
        registerUser(credentials, successC, callback) {
            console.log('registerUser action', credentials)

            api.registerUser(credentials).then(response => {
        
                const notify = useNotify()
                     console.log("data received ")
                     console.log(response)
                     if(response.status == 201) {
                        if(successC)
                            successC()
                        notify.positive("User successfully registered")
                     }
                    
                   }).catch(function (error) {

                    let r = error.response

                    if( r.status == 409) {
                        callback(r.data)
                    } else{
                        notify.negative("error registering user")
                    }

                  });
        }
        

    }



})    



// import { defineStore } from 'pinia'
// import {useNotify} from 'src/use/useNotify.js'
// import { useProjectsStore } from 'stores/projects.store.js'
// import api from "src/api/auth.api.js";

// let notify;
// let projectStore; 

// export const useAuthStore = defineStore('auth',{

//     state: () => ({
//          // initialize state from local storage to enable user to stay logged in
//          user: {},
//     }),

//     getters : {},
//     actions: {

//         init(){
//             console.log("auth init....")
//             if(localStorage.getItem('user') !== null)
//                 this.user = JSON.parse(localStorage.getItem('user')),
//             projectStore = useProjectsStore()
//             notify = useNotify()
//             console.log(projectStore)

//         },
//         async login(credentials) {
//             // todo: i THINK NEEDS A CATCH...LOOK INTO THIS https://jasonwatmore.com/post/2022/05/26/vue-3-pinia-jwt-authentication-tutorial-example#login-view-vue
//             // console.log(credentials)
//             api.login(credentials).then( response => {

//                 console.log("login....", response)   
//                 if(response.status == 200){

//                     this.user = response.data   
//                      // store user details and jwt in local storage to keep user logged in between page refreshes
//                     localStorage.setItem('user', JSON.stringify(this.user));   
//                     // redirect to previous url or default to home page
//                     console.log(projectStore)
//                     projectStore.init()
                    
//                     this.router.push('/projects');
//                 }
                    

//             }).catch( error => {

//                     console.log("error login", error)

//                     if(error.response) {

//                         let r = error.response
//                         if( r.data.message == 'User not found' )
//                             notify.negative("Username does not exist")
//                         else 
//                         notify.negative("password incorrect")

//                     }
                    
                    

//                   });
//             // const user = await fetchWrapper.post(`${baseUrl}/authenticate`, { username, password });

//             // update pinia state
//             // this.user = user;

           

//             // redirect to previous url or default to home page
//             // router.push(this.returnUrl || '/');
//          },
//          logout() {
//             console.log("logout")
//             this.user = {};
//             localStorage.removeItem('user');
//             this.router.push('/auth');
//         },
//         registerUser(credentials, callback) {
//             console.log('registerUser action', credentials)

//             api.registerUser(credentials).then(response => {
        
//                      console.log("data received ")
//                      console.log(response)
                    
//                    }).catch(function (error) {

//                     let r = error.response

//                     if( r.status == 409) {
//                         callback(r.data)
//                     } else{
//                         notify.negative("error registering user")
//                     }

//                   });
//         }
        

//     }



// })    