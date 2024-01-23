<template>
    <q-card-section>
      <div class="text-h6">Edit intent</div>
    </q-card-section>
    <q-card-section>
      <q-form ref="form" @submit="onSubmit" class="q-gutter-md">
        <q-input filled v-model="editedIntent.name" label="Intent name" lazy-rules :rules="nameRules" />
        <div class="text-right">
          <q-btn label="Update" type="submit" color="primary" v-close-popup />
          <q-btn label="Cancel" type="reset" color="primary" flat class="q-ml-sm" v-close-popup />
        </div>
      </q-form>
    </q-card-section>
  </template>
  
  <script setup>
  import { defineProps, reactive } from "vue";
  import { useIntentsStore } from "src/stores/intentsStore.js";
  import { useRoute } from "vue-router";
  
  const intentsStore = useIntentsStore();
  const route = useRoute();
  
  const props = defineProps({
    intentData: { type: Object, default: null },
  });
  
  const editedIntent = reactive({
    id: props.intentData?.intentID || null,
    name: props.intentData?.intentName || '',
  });
  
  const nameRules = [(val) => (val && val.length > 0) || 'Please type a name'];
  
  const onSubmit = () => {
    const data = new FormData();
    data.append("intentID", editedIntent.id);
    data.append("intentName", editedIntent.name);
    intentsStore.putIntent(editedIntent.id, route.params.id, data, successCallback)
  };
  
  const successCallback = () => {
    intentsStore.getAllIntents(route.params.id); // get the repositories again to refresh the list of the store
  };
  </script>