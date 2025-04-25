<template>
    <q-item exact manual-focus>
      <q-item-section v-if="miniState" avatar>
        <q-btn flat
               no-caps
               padding="xs"
               :icon="icon"
               :to="{name: url}"
               :color=" activeRoutes.includes($route.name) ? 'primary600': 'neutral500' "
               :class="{ activebg:  activeRoutes.includes($route.name) }">
          <q-item-section>
            <div class="icon-label-wrapper">
              <span class="label-style">{{ label }}</span>
            </div>
          </q-item-section>
          <q-tooltip anchor="center right"
                     self="center left"
                     :offset="[10, 10]"
                     transition-show="fade"
                     transition-hide="fade">
            {{ label }}
          </q-tooltip>
        </q-btn>
      </q-item-section>
      <q-item-section v-else>
        <q-btn flat
               no-caps
               :icon="icon"
               :to="{name: url}"
               :color=" activeRoutes.includes($route.name) ? 'primary600': 'neutral500' "
               :class="{ activebg:  activeRoutes.includes($route.name) }">
          <q-item-section>
            <div class="icon-label-wrapper">
              <span class="label-style">{{ label }}</span>
            </div>
          </q-item-section>
          <q-tooltip anchor="center right" self="center left" :offset="[10, 10]" transition-show="fade" transition-hide="fade">
            {{ label }}
          </q-tooltip>
        </q-btn>
      </q-item-section>
    </q-item>
  </template>
  
  <script setup>
    const props = defineProps({
      icon: {
        type: String,
        required: true
      },
      url: {
        type: String,
        required: true
      },
      activeRoutes: {
        type: Array,
        required: true
      },
      label: {
        type: String,
        required: true
      },
      miniState: {
        type: Boolean,
        default: false
      }
    });
  </script>