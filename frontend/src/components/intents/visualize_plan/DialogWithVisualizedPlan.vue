<template>
  <div>
    <q-dialog v-model="dialogVisible" persistent :maximized="maximizedToggle" transition-show="slide-up" transition-hide="slide-down">
      <q-card class="text-black">
        <q-bar>
          <q-space />
          <q-btn dense flat icon="minimize" @click="maximizedToggle = false" :disable="!maximizedToggle">
            <q-tooltip v-if="maximizedToggle" class="bg-white text-primary">Minimize</q-tooltip>
          </q-btn>
          <q-btn dense flat icon="crop_square" @click="maximizedToggle = true" :disable="maximizedToggle">
            <q-tooltip v-if="!maximizedToggle" class="bg-white text-primary">Maximize</q-tooltip>
          </q-btn>
          <q-btn dense flat icon="close" @click="close">
            <q-tooltip class="bg-white text-primary">Close</q-tooltip>
          </q-btn>
        </q-bar>

        <q-card-section>
          <VisualizePlan :plan="visualizedPlan"/> 
        </q-card-section>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup>

import { ref, defineEmits, watch } from 'vue';
import VisualizePlan from './VisualizePlan.vue';

const props = defineProps({
  dialog: {type: Boolean, required: true},
  visualizedPlan: {type: Object, required: true}
});

const dialogVisible = ref(props.dialog);
const maximizedToggle = ref(true)

const emits = defineEmits(['update:dialog'])

watch(() => props.dialog, (newVal) => {
  dialogVisible.value = newVal
})

const close = () => {
  dialogVisible.value = false
  emits('update:dialog', false)
}
</script>
