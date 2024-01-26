<template>
  <q-layout view="lHr LpR lFr">
    <q-drawer show-if-above :mini="miniState" :width="200" :breakpoint="500" bordered>
      <q-scroll-area class="fit">
        <q-list padding>

          <q-item style="max-width: 200px; padding-top: 0px">
            <q-item-section avatar v-if="miniState">
              <ODIN_short class="logoODIN" style="width:38px"></ODIN_short>
            </q-item-section>

            <q-item-section v-else>
              <q-img src="~assets/ODIN.svg" style="max-width: 180px; max-height: 35px; " fit="contain"/>
            </q-item-section>
          </q-item>

          <q-separator/>

          <q-item>
            <q-item-section avatar>
              <q-icon :name="miniState == true ? 'mdi-arrow-collapse-right' : 'mdi-arrow-collapse-left'"
                      @click="miniState = !miniState"/>
            </q-item-section>
          </q-item>

          <div class="top-buttons-wrapper">
            <SideBarElement :icon="'o_folder'" :url="'projects'" :active-routes="['projects']" :label="'Projects'" :mini-state="miniState"/>
            <SideBarElement :icon="'o_cottage'" :url="'home'" :active-routes="['home']" :label="'Home'" :mini-state="miniState"/>
            <SideBarElement :icon="'mdi-database'" :url="'repositories'" :active-routes="['repositories']" :label="'Repositories'" :mini-state="miniState"/>
            <SideBarElement :icon="'o_layers'" :url="'datasources'" :active-routes="['datasources', 'dsIntegration']" :label="'Datasets'" :mini-state="miniState"/>
            <SideBarElement :icon="'o_hub'" :url="'schema'" :active-routes="['schema']" :label="'Schema'" :mini-state="miniState"/>
            <SideBarElement :icon="'mdi-chat-question'" :url="'query'" :active-routes="['query']" :label="'Query'" :mini-state="miniState"/>
            <SideBarElement :icon="'mdi-selection-search'" :url="'data-products'" :active-routes="['data-products']" :label="'Data products'" :mini-state="miniState"/>
            <SideBarElement :icon="'mdi-thought-bubble'" :url="'intents-list'" :active-routes="['intents-list', 'abstract-planner', 'logical-planner', 'workflow-planner', 'intent-workflows']" :label="'Intents'" :mini-state="miniState"/>
          </div>

          <div class="fixed-bottom">

            <q-item manual-focus>
              <q-item-section avatar>
                <q-btn class="q-mr-xs" flat round @click="authStore.setDark()" :icon="$q.dark.isActive ? 'nights_stay' : 'wb_sunny'"/>
              </q-item-section>
            </q-item>

            <q-item manual-focus>
              <q-item-section avatar>
                <q-btn round dense flat :icon="$q.fullscreen.isActive ? 'fullscreen_exit' : 'fullscreen'"
                       @click="$q.fullscreen.toggle()" v-if="$q.screen.gt.sm"/>
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
import {useAuthStore} from 'stores/authStore.js'
import SideBarElement from "./SideBarElement.vue";
const miniState = ref(false)
const authStore = useAuthStore()

</script>

<style lang="scss">

.q-drawer--left.q-drawer--bordered {
  border-right: 1px solid rgb(234, 234, 239);
}

.body--light {
  .activebg {
    background-color: $primary100;
  }
}

.body--dark {
  .activebg {
    background-color: $neutral100d;
  }
}

.bg-white {
  background-color: #ffffff;
}

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

.top-buttons-wrapper {
  max-height: calc(100vh - 275px);
  overflow-y: auto
}

.btn-fixed-width {
  width: 100px
}
</style>
