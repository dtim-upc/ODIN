<template>
  <q-btn flat round dense :icon="props.inFullscreen ? 'fullscreen_exit' : 'fullscreen'"
         @click="toggleFullscreen">
    <q-tooltip :disable="$q.platform.is.mobile" v-close-popup>
      {{ props.inFullscreen ? "Exit Fullscreen" : "Toggle Fullscreen" }}
    </q-tooltip>
  </q-btn>
</template>

<script setup>
import { defineEmits } from 'vue';

const props = defineProps({
  props: { type: Object, required: true }
});

const emit = defineEmits(['toggle']);

const toggleFullscreen = () => {
  emit('toggle');
};
</script>