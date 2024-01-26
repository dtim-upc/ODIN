<template>
  <q-layout view="hHh lpR fFf">

    <q-header bordered class="header_bg text-black">
      <q-toolbar>
        <q-toolbar-title>
          <q-img src="~assets/ODIN.svg" style="max-width: 180px; max-height: 35px; " fit="contain"/>

        </q-toolbar-title>
        <q-space/>
        <div class="q-gutter-sm row items-center no-wrap">
          <q-btn round dense flat color="black" :icon="$q.fullscreen.isActive ? 'fullscreen_exit' : 'fullscreen'"
                 @click="$q.fullscreen.toggle()" v-if="$q.screen.gt.sm">
          </q-btn>
          <q-btn round flat>
            <q-avatar size="26px">
              <img src="https://cdn.quasar.dev/img/boy-avatar.png"/>
            </q-avatar>

            <q-menu auto-close transition-show="scale" transition-hide="scale" anchor="bottom left" self="top left">
              <q-list style="min-width: 100px">
                <q-item clickable @click="authStore.logout()">
                  <q-item-section>Log out</q-item-section>
                </q-item>
              </q-list>
            </q-menu>

          </q-btn>
        </div>

      </q-toolbar>
    </q-header>

    <q-page-container>
      <router-view/>
    </q-page-container>

  </q-layout>
</template>

<script setup>
import {useAuthStore} from 'stores/authStore.js'

const authStore = useAuthStore()
</script>

<style lang="scss">

.body--light {
  .header_bg {
    background: $neutral0 !important;
  }
}

.body--dark {
  .header_bg {
    background: $neutral500d !important;
  }
}

</style>
