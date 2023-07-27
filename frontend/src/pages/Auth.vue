<template>


<!-- bg-light-green -->
  <div class="animated-bg  fullscreen row justify-center items-center">

    <Particles
                id="tsparticles"
                :particlesInit="particlesInit"
                :particlesLoaded="particlesLoaded"
                :options='{
  particles: {
    number: {
      value: 80,
      density: {
        enable: true,
        area: 800
      }
    },
    color: {
      value: ["#BD10E0", "#B8E986", "#50E3C2", "#FFD300", "#E86363"]
    },
    shape: {
      type: "circle",
      stroke: {
        width: 0,
        color: "#b6b2b2"
      }
    },
    opacity: {
      value: 0.5211089197812949,
      random: false,
      animation: {
        enable: true,
        speed: 1,
        minimumValue: 0.1,
        sync: false
      }
    },
    size: {
      value: 8.017060304327615,
      random: true,
      animation: {
        enable: true,
        speed: 12.181158184520175,
        minimumValue: 0.1,
        sync: false
      }
    },
    lineLinked: {
      enable: false,
      distance: 150,
      color: "#c8c8c8",
      opacity: 0.4,
      width: 1
    },
    move: {
      enable: true,
      speed: 1,
      direction: "none",
      random: false,
      straight: false,
      outMode: "bounce",
      bounce: false,
      attract: {
        enable: false,
        rotateX: 600,
        rotateY: 1200
      }
    }
  },
  interactivity: {
    detectOn: "canvas",
    events: {
      onHover: {
        enable: true,
        mode: "connect"
      },
      onClick: {
        enable: false,
        mode: "push"
      },
      resize: true
    },
    modes: {
      grab: {
        distance: 400,
        lineLinked: {
          opacity: 1
        }
      },
      bubble: {
        distance: 400,
        size: 40,
        duration: 2,
        opacity: 8,
        speed: 3
      },
      connect: {
        distance: 300,
        radius: 140
      },
      repulse: {
        distance: 200,
        duration: 0.4
      },
      push: {
        particles_nb: 4
      },
      remove: {
        particles_nb: 2
      }
    }
  },
  fpsLimit: 30,
  detectRetina: true
}' ></Particles>
    <div class="column">
      <div class="row">
        <h5 class="text-h5 text-white q-my-md"> ODIN 2</h5>
      </div>
      <div class="row">
        <q-card square bordered class="q-pa-lg shadow-1">
            <div v-if="loginView">
                <q-card-section>
                    <q-form class="q-gutter-md">
                    <q-input square filled v-model="credentials.username" type="text" label="username" autocomplete="current-password" />
                     <q-input square filled  v-model="credentials.password" type="password" label="password" autocomplete="current-password" v-on:keyup.enter="login" />
                    <!-- <q-input square filled v-model="credentials.password" type="password" label="password" /> -->
                    </q-form>
                </q-card-section>
                <q-card-actions class="q-px-md">
                    <q-btn unelevated color="light-green-7" size="lg" class="full-width" label="Login" @click="login"/>
                </q-card-actions>
                <q-card-section class="text-center q-pa-none">
                    <p class="text-grey-6 cursor-pointer" @click="loginView=!loginView" >Not registered? Created an Account</p>
                </q-card-section>
                    
            </div>
            <div v-else>

                <q-card-section>
                    <q-form class="q-gutter-md">
                    <q-input square filled v-model="user.firstName" type="text" label="first name" />
                    <q-input square filled v-model="user.lastName" type="text" label="last name" />
                    <q-input square filled v-model="user.username" type="text" label="username"  autocomplete="current-password" :error-message="errorM" :error="errorUserName"/>
                    <q-input square filled v-model="user.password" type="password" label="password" autocomplete="current-password"/>
  
                    <!-- v-on:keyup.enter="props.row.edit = false;" -->
                    </q-form>
                </q-card-section>
                <q-card-actions class="q-px-md">
                    <q-btn unelevated color="light-green-7" size="lg" class="full-width" label="SIGN-UP" @click="signUp()" />
                </q-card-actions>
                <q-card-section class="text-center q-pa-none">
                    <p class="text-grey-6 cursor-pointer" @click="loginView=!loginView">Already an account? sign in</p>
                </q-card-section>

            </div>


          
        </q-card>
      </div>
    </div>
  </div>

</template>



<script setup>
import {ref, reactive, onMounted} from "vue";
// import api from "src/api/dataSourcesAPI.js";
// import {useNotify} from 'src/use/useNotify.js' 
import { useAuthStore } from 'stores/auth.store.js'

// import Particles from "vue3-particles";
import { loadFull } from "tsparticles";


const particlesInit = async (engine) => {
    await loadFull(engine);
}

const particlesLoaded = async (container) => {
    console.log("Particles container loaded", container);
}
// const notify = useNotify()
const authStore = useAuthStore()


const loginView = ref(true)

const credentials = reactive({
    username : "",
    password : ""
})

const user = reactive({
    username : "",
    firstName: "",
    lastName: "",
    password : ""
})

const login = () => {
    authStore.login(credentials)
}

const successSignUp = () => {
  loginView.value = true;
}

const signUp = () => {

    
    authStore.registerUser(user, successSignUp, userNameTaken)


}

const errorM = ref('')
const errorUserName = ref(false)

const userNameTaken = (message) =>{

    errorM.value = message;
    errorUserName.value = true

}

onMounted( () => {
//     authStore.init()


})



</script>



<style>
/* #tsparticles {
  position: absolute;
  width: 100%;
  height: 100%;
  background-color: #000;
  background-image: url("");
  background-repeat: no-repeat;
  background-size: cover;
  background-position: 50% 50%;

} */

.q-card {
  width: 360px;
}


.animated-bg{
    background: linear-gradient(253deg, #0cc898, #1797d2, #864fe1);
    background-size: 300% 300%;
    -webkit-animation: Background 25s ease infinite;
    -moz-animation: Background 25s ease infinite;
    animation: Background 25s ease infinite;
}


@-webkit-keyframes Background {
    0% {
      background-position: 0% 50%
    }
    50% {
      background-position: 100% 50%
    }
    100% {
      background-position: 0% 50%
    }
  }
  
  @-moz-keyframes Background {
    0% {
      background-position: 0% 50%
    }
    50% {
      background-position: 100% 50%
    }
    100% {
      background-position: 0% 50%
    }
  }
  
  @keyframes Background {
    0% {
      background-position: 0% 50%
    }
    50% {
      background-position: 100% 50%
    }
    100% {
      background-position: 0% 50%
    }
  }
  


</style>