<template>
  <q-page padding>
    <div>     
      <q-tabs v-model="currentTab" class="bg-white">
        <q-route-tab label="Intent Definition" name = "intent-definition" icon="mdi-comment-text-outline" to="intent-definition"/>
        <q-route-tab label="Abstract Planner" name = "abstract-planner" icon="mdi-arrow-expand-right" to="abstract-planner"/>
        <q-route-tab label="Logical Planner" name = "logical-planner" icon="mdi-call-split" to="logical-planner"/>
        <q-route-tab label="Workflow Planner" name = "workflow-planner" icon="mdi-cog" to="workflow-planner"/>
        <q-route-tab label="Workflows" name = "intent-workflows" icon="mdi-share-variant" to="intent-workflows"/>
      </q-tabs>

      <q-tab-panels v-model="currentTab">
        <q-tab-panel name = "intent-definition">
          <IntentDefinition/>
        </q-tab-panel>
        <q-tab-panel name = "abstract-planner">
          <AbstractPlanner/>
        </q-tab-panel>
        <q-tab-panel name = "logical-planner">
          <LogicalPlanner/>
        </q-tab-panel>
        <q-tab-panel name = "workflow-planner">
          <WorkflowPlanner/>
        </q-tab-panel>
        <q-tab-panel name = "intent-workflows">
          <SelectedPlans/>
        </q-tab-panel>
      </q-tab-panels>
    </div>
    <div class="bg-white q-pa-md">
        <q-btn @click="router.push({name: 'intents'})" > Back to list </q-btn>
    </div>
  </q-page>
</template>

<script setup>
import { ref } from 'vue'
import LogicalPlanner from "../components/intents/LogicalPlanner.vue";
import WorkflowPlanner from "../components/intents/WorkflowPlanner.vue";
import SelectedPlans from "../components/intents/SelectedPlans.vue";
import { useRouter } from 'vue-router';
import IntentDefinition from '../components/intents/IntentDefinition.vue';
import AbstractPlanner from '../components/intents/AbstractPlanner.vue';

const router = useRouter()
const currentTab = ref('aintent-definition')

</script>

