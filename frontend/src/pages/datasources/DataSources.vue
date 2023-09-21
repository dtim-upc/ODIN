<template>
  <!-- class="flex flex-center" -->
  <q-page>
    <TableDataSources/>
  </q-page>
</template>

<script setup>
import TableDataSources from 'components/tables/TableDataSources.vue';

import { onBeforeMount } from "vue";

onBeforeMount(() => {
  document.title = "Datasets"; // Título de la pestaña
});


</script>
