<template>
    <q-page padding>

        <q-tabs v-model="currentTab" class="bg-white">
            <q-route-tab label="Abstract Planner" name = "abstract-planner" icon="mdi-arrow-expand-right" to="abstract-planner"/>
            <q-route-tab label="Logical Planner" name = "logical-planner" icon="mdi-call-split" to="logical-planner"/>
            <q-route-tab label="Workflow Planner" name = "workflow-planner" icon="mdi-cog" to="workflow-planner"/>
            <q-route-tab label="Workflows" name = "intent-workflows" icon="mdi-share-variant" to="intent-workflows"/>
        </q-tabs>

        <q-tab-panels v-model="currentTab" class="bg-white">
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
                <IntentWorkflows/>
            </q-tab-panel>
        </q-tab-panels>
        
    </q-page>
</template>

<script setup>
import { ref } from 'vue'
import AbstractPlanner from "../../components/intents/AbstractPlanner.vue";
import LogicalPlanner from "../../components/intents/LogicalPlanner.vue";
import WorkflowPlanner from "../../components/intents/WorkflowPlanner.vue";
import IntentWorkflows from "../../components/intents/IntentWorkflows.vue";
    
const currentTab = ref('abs_pln')
</script>