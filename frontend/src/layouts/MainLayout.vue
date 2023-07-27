<template>
  <q-layout view="lHr LpR lFr">
<!-- v-model="drawer" -->
    <q-drawer  show-if-above :mini="miniState" :width="200" :breakpoint="500" bordered >
      <q-scroll-area class="fit">
        <q-list padding>

          <q-item style="max-width: 200px; padding-top: 0px" >
            <q-item-section avatar v-if="miniState" >
          
              <ODIN_short class="logoODIN" style="width:38px"></ODIN_short>
              <!-- <q-icon name="img:/assets/ODIN_short.svg" size="lg" color="primary"/>   -->
            </q-item-section>
            <q-item-section v-else>
              <q-img src="~assets/ODIN.svg" style="max-width: 180px; max-height: 35px; " fit="contain"/>
            </q-item-section>
          </q-item>

          <q-separator/>
          <q-item>
            <q-item-section avatar>
              <q-icon :name="miniState == true ? 'mdi-arrow-collapse-right' : 'mdi-arrow-collapse-left'" @click="miniState = !miniState"/>
            </q-item-section>
          </q-item>
          <q-item exact manual-focus>
            <q-item-section avatar>
              <!-- <q-icon name="o_cottage"/> -->
              <q-btn  flat padding="xs" icon="o_folder" :to="{name: 'projects'}" color="neutral500 ">
                          <q-tooltip anchor="center right" self="center left" :offset="[10, 10]" transition-show="fade" transition-hide="fade">
                            Projects
                          </q-tooltip>
                        </q-btn> 
            </q-item-section>
            <q-item-section>
                  Projects
            </q-item-section>
          </q-item>
          <q-item exact manual-focus>
            <q-item-section avatar>
              <!-- <q-icon name="o_cottage"/> -->
              <q-btn  flat padding="xs" icon="o_cottage" :to="{name: 'home'}" :color="$route.name === 'home' ? 'primary600': 'neutral500' " :class="{ activebg: $route.name === 'home' }">
                          <q-tooltip anchor="center right" self="center left" :offset="[10, 10]" transition-show="fade" transition-hide="fade">
                            Home
                          </q-tooltip>
                        </q-btn> 
            </q-item-section>
            <q-item-section>
                  Home
            </q-item-section>
          </q-item>
                    <!-- <q-item manual-focus>
                      <q-item-section avatar>
                 
                        <q-btn  flat padding="xs" icon="o_hub" to="/viewTripleStore" :color="$route.name === 'viewTripleStore' ? 'primary600': 'neutral500' " :class="{ activebg: $route.name === 'viewTripleStore' }">
                          <q-tooltip anchor="center right" self="center left" :offset="[10, 10]" transition-show="fade" transition-hide="fade">
                            View triple store
                          </q-tooltip>
                        </q-btn> 
                      </q-item-section>
  
                    </q-item> -->

          <!-- <q-item manual-focus>
            <q-item-section avatar>
              <q-btn flat padding="xs" icon="o_bubble_chart" to="/dataSources" :color="$route.name === 'dataSources' ? 'primary600': 'neutral500' " :class="{ activebg: $route.name === 'dataSources' }">
                <q-tooltip anchor="center right" self="center left" :offset="[10, 10]" transition-show="fade" transition-hide="fade">
                  Data sources
                </q-tooltip>
              </q-btn> 
            </q-item-section>
          
          </q-item> -->


          <q-item manual-focus>
            <q-item-section avatar>
              <!--      o_file_copy o_spoke   o_category  workspaces   category spoke-->
              <!-- <q-icon size="sm" name="o_bubble_chart "/> -->
              <q-btn flat padding="xs" icon="o_hub" :to="{name: 'schema'}" :color="$route.name === 'schema' ? 'primary600': 'neutral500' " :class="{ activebg: $route.name === 'schema' }">
                <q-tooltip anchor="center right" self="center left" :offset="[10, 10]" transition-show="fade" transition-hide="fade">
                  Schema
                </q-tooltip>
              </q-btn> 
            </q-item-section>
            <q-item-section>
              Schema
            </q-item-section>
          
          </q-item>


          <q-item manual-focus>
            <q-item-section avatar>
              <!--      o_file_copy o_spoke   o_category  workspaces   category spoke-->
              <!-- <q-icon size="sm" name="o_bubble_chart "/> -->
              <q-btn flat padding="xs" icon="o_layers" :to="{name:'datasources'}" :color=" ['datasources', 'dsIntegration'].includes($route.name) ? 'primary600': 'neutral500' " :class="{ activebg:  ['datasources', 'dsIntegration'].includes($route.name) }">
                <q-tooltip anchor="center right" self="center left" :offset="[10, 10]" transition-show="fade" transition-hide="fade">
                  Data sources
                </q-tooltip>
              </q-btn> 
            </q-item-section>
            <q-item-section>
              Data sources
            </q-item-section>
          </q-item>


          <q-item manual-focus>
            <q-item-section avatar>
              <!-- <q-icon name="mdi-selection-search"/> -->
              <q-btn flat padding="xs" icon="mdi-selection-search" :to="{name:'query'}" :color="$route.name === 'query' ? 'primary600': 'neutral500' " :class="{ activebg: $route.name === 'query' }">
                <q-tooltip anchor="center right" self="center left" :offset="[10, 10]" transition-show="fade" transition-hide="fade">
                  Query
                </q-tooltip>
              </q-btn> 
            </q-item-section>
            <q-item-section>
              Query
            </q-item-section>
          </q-item>

          <div class="fixed-bottom">
            
          <q-item manual-focus>
            <q-item-section avatar>
                <q-btn class="q-mr-xs" flat round @click="authStore.setDark()" :icon="$q.dark.isActive ? 'nights_stay' : 'wb_sunny'"/>
            </q-item-section>
            
          </q-item>

          <q-item manual-focus>
            <q-item-section avatar>
                <q-btn round dense flat :icon="$q.fullscreen.isActive ? 'fullscreen_exit' : 'fullscreen'" @click="$q.fullscreen.toggle()" v-if="$q.screen.gt.sm"/>
            </q-item-section>
          </q-item>


          <q-item manual-focus>
            <q-item-section avatar>
                <q-btn round flat>
                  <q-avatar size="26px">
                    <img src="https://cdn.quasar.dev/img/boy-avatar.png"/>
                  </q-avatar>

                  <q-menu auto-close transition-show="scale" transition-hide="scale" anchor="top end" self="bottom left">
                    <q-list style="min-width: 100px">
                      <q-item clickable @click="authStore.logout()">
                        <q-item-section>Log out</q-item-section>
                      </q-item>
                    </q-list>
                  </q-menu>

                </q-btn>
            </q-item-section>
          </q-item>

          </div>

        </q-list>
      </q-scroll-area>
    </q-drawer>

    <q-page-container>
      <router-view/>
    </q-page-container>
  </q-layout>
</template>

<script setup>
import {ref} from "vue";
import ODIN_short from "components/icons/ODIN_short.vue";
import { useAuthStore } from 'stores/auth.store.js'
// import {outlinedHub as hubi}  from "@quasar/extras/material-icons-outlined"
const miniState = ref(true)
const authStore = useAuthStore()
</script>

<style lang="scss">
.q-drawer--left.q-drawer--bordered {
  border-right: 1px solid rgb(234, 234, 239);
}
.body--light {
  .activebg{
    background-color: $primary100;
  }
}
.body--dark {
 
  .activebg{
    background-color: $neutral100d;
  }
 
}
/* a.router-link-exact-active{
  background-color: #f0f0ff;
} */
/* .bg-color {
   background-color: #f4f8fb;
  background-color: #ffffff;
} */
.bg-white {
  background-color: #ffffff;
}
.bg-active {
  /* color: #8f81bd; */
  /* color:#5e429e; */
  /* background: #f0edfd; */
  /* background: #e7e2f3; */
}
/* .bg-active .q-btn {
  background: #e7e2f3;
} */
/*@import url("https://fonts.googleapis.com/css?family=Source+Sans+Pro");*/
/*body {*/
/*  background-color: #fff;*/
/*  color: #171717;*/
/*  transition: background-color 0.2s ease, color 0.2s ease;*/
/*}*/
/*body.dark-mode {*/
/*  background-color: #242424;*/
/*}*/
/*body.dark-mode .flex h1 {*/
/*  color: #fff;*/
/*}*/
.mode-toggle {
  position: relative;
  padding: 0;
  width: 44px;
  height: 24px;
  min-width: 36px;
  min-height: 20px;
  background-color: #262626;
  border: 0;
  border-radius: 24px;
  outline: 0;
  overflow: hidden;
  cursor: pointer;
  z-index: 2;
  -webkit-tap-highlight-color: rgba(0, 0, 0, 0);
  -webkit-touch-callout: none;
  appearance: none;
  transition: background-color 0.5s ease;
}
.mode-toggle .toggle {
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  margin: auto;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 3px solid transparent;
  box-shadow: inset 0 0 0 2px #a5abba;
  overflow: hidden;
  transition: transform 0.5s ease;
}
.mode-toggle .toggle #dark-mode {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  border-radius: 50%;
}
.mode-toggle .toggle #dark-mode:before {
  content: "";
  position: relative;
  width: 100%;
  height: 100%;
  left: 50%;
  float: left;
  background-color: #a5abba;
  transition: border-radius 0.5s ease, width 0.5s ease, height 0.5s ease, left 0.5s ease, transform 0.5s ease;
}
.mode-toggle {
  background-color: #333333;
}
body.dark-mode .mode-toggle .toggle {
  transform: translateX(19px);
}
body.dark-mode .mode-toggle .toggle #dark-mode:before {
  border-radius: 50%;
  width: 150%;
  height: 85%;
  left: 40%;
  transform: translate(-10%, -40%), rotate(-35deg);
}
</style>