<template>
    <q-page>
      <TableIntents/>
    </q-page>
  </template>
  
  <script setup>
  import TableIntents from "../../components/tables/TableIntents.vue";
  
  import { onBeforeMount } from "vue";
  
  onBeforeMount(() => {
    document.title = "Intents";
  });
  </script>
  