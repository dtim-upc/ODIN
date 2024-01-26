<template>
  <!-- class="flex flex-center" -->
  <q-page>
    <TableDatasets/>
  </q-page>
</template>

<script setup>
import TableDatasets from 'components/tables/TableDatasets.vue';

import { onBeforeMount } from "vue";

onBeforeMount(() => {
  document.title = "Datasets"; // Título de la pestaña
});


</script>
