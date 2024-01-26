<template>
  <q-page class="q-pa-md">
    <TableProjects></TableProjects>
  </q-page>
</template>

<script setup>
import TableProjects from 'components/tables/TableProjects.vue';

import { onBeforeMount } from "vue";

onBeforeMount(() => {
  document.title = "Projects"; // Título de la pestaña
});

</script>
