<template>
    <q-card-section>
      <div class="text-h6">Edit workflow</div>
    </q-card-section>
    <q-card-section>
      <q-form ref="form" @submit="onSubmit" class="q-gutter-md">
        <q-input filled v-model="editedWorkflow.workflowName" label="Workflow name" lazy-rules :rules="nameRules" />
        <div class="text-right">
          <q-btn label="Update" type="submit" color="primary" v-close-popup />
          <q-btn label="Cancel" type="reset" color="primary" flat class="q-ml-sm" v-close-popup />
        </div>
      </q-form>
    </q-card-section>
  </template>
  
  <script setup>
  import { defineProps, reactive } from "vue";
  import { useWorkflowsStore } from "src/stores/workflowStore.js";
  import { useRoute } from "vue-router";
  
  const workflowStore = useWorkflowsStore();
  const route = useRoute();
  
  const props = defineProps({
    intentData: { type: Object, default: null },
    workflowData: { type: Object, default: null },
  });
  
  const editedWorkflow = reactive({
    workflowID: props.workflowData?.workflowID || null,
    workflowName: props.workflowData?.workflowName || '',
  });
  
  const nameRules = [(val) => (val && val.length > 0) || 'Please type a name'];
  
  const onSubmit = () => {
    const data = new FormData();
    data.append("workflowName", editedWorkflow.workflowName);
    workflowStore.putWorkflow(props.intentData.intentID, route.params.id, editedWorkflow.workflowID, data)
  };

  </script>