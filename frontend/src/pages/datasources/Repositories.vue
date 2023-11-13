<template>
  <!-- class="flex flex-center" -->
  <q-page>
    <TableRepositories/>
  </q-page>
</template>

<script setup>
import TableRepositories from "../../components/tables/TableRepositories.vue";

import { onBeforeMount } from "vue";

onBeforeMount(() => {
  document.title = "Repositories"; // Título de la pestaña
});
</script>
