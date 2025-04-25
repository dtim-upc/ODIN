<template>
  <div class="q-pa-md">
    <q-table :rows="rows" :columns="columns" row-key="name"
             no-results-label="The filter didn't uncover any results" :class="{ 'no-shadow': true }">
      <template v-slot:top-left="">
        <div class="q-table__title">
          Query result
        </div>
      </template>

      <template v-slot:top-right="props">
        <FullScreenToggle :props="props" @toggle="props.toggleFullscreen"/>
      </template>

    </q-table>
  </div>
</template>

<script setup>
import FullScreenToggle from "./TableUtils/FullScreenToggle.vue";

const props = defineProps({
  columns: {type: Array},
  rows: {type: Array},
});

</script>

