<template>
  <div class="q-pa-md">
      <!-- Main table component -->
    <q-table :rows="rows" :columns="columns" :filter="search" row-key="id" 
              no-results-label="The filter didn't uncover any results">
      <template v-slot:top-left="">
        <div class="q-table__title">
          Intents
          <q-btn unelevated padding="none" color="primary700" icon="add" @click="router.push({name: 'abstract-planner'})"/>
        </div>
      </template> 

      <template v-slot:top-right="props">
        <q-input outlined dense debounce="400" color="primary" v-model="search">
          <template v-slot:append>
            <q-icon name="search"/>
          </template>
        </q-input>

        <FullScreenToggle :props="props" @toggle="props.toggleFullscreen"/>
      </template>

      <template v-slot:body-cell-actions="props">
        <q-td :props="props">
          <q-btn dense round flat color="grey" @click="editIntent(props)" icon="edit"></q-btn>
          <q-btn dense round flat color="grey" @click="deleteIntent(props)" icon="delete"></q-btn>
        </q-td>
      </template>

      <template v-slot:no-data>
          <div class="full-width row flex-center text-accent q-gutter-sm q-pa-xl" style="flex-direction: column">
            <NoDataImage/>
            <span style="color: rgb(102, 102, 135);font-weight: 500;font-size: 1rem;line-height: 1.25;">No intents.</span>
          </div>
        </template>

      <template v-slot:body-cell-expand="props">

        <q-td :props="props">
        <q-expansion-item :label="'Show workflows'">
          <div class="centered-table">
            <table>
              <thead>
              <tr>
                <th><b>Workflow Id</b></th>
                <th><b>Workflow Name</b></th>
                <th><b>Visualize</b></th>
                <th><b>Actions</b></th>
              </tr>
              </thead>
              <tbody>
              <tr v-if="props.row.workflows && props.row.workflows.length > 0" v-for="workflow in props.row.workflows" :key="workflow.workflowID">
                <td>{{ workflow.workflowID }}</td>
                <td>{{ workflow.workflowName }}</td>
                <td>
                  <q-btn @click="visualizeWorkflow(workflow.workflowGraph.workflowRepresentation)" color="primary" icon="visibility" size="sm"></q-btn>
                </td>
                <td>
                  <q-btn dense round flat color="grey" @click="workflowsStore.downloadWorkflowSchema(projectID, props.row.intentID, workflow)" icon="download" label="RDF"></q-btn>
                  <!--<q-btn dense round flat color="grey" @click="intentsStore.downloadKNIME(plan)" icon="download" label="KNIME"/>-->
                  <q-btn dense round flat color="grey" @click="editWorkflow(props.row, workflow)" icon="edit"></q-btn>
                  <q-btn dense round flat color="grey" @click="deleteWorkflow(props.row, workflow)" icon="delete"></q-btn>
                </td>
              </tr> 
              <tr v-else>
                <td colspan="3">There are no workflows for this query</td>
              </tr> 
              </tbody>
            </table>
          </div>
        </q-expansion-item>
      </q-td>

      </template>

    </q-table>
    
    <!-- Additional dialogs that appear to fufill certain actions -->
    
    <DialogWithVisualizedPlan v-model:dialog="showVisualizePlan" :visualizedPlan="visualizedPlan"/>

    <EditIntentForm v-model:show="showDialogEditIntent" :intentData="selectedIntent" />
    <EditWorkflowForm v-model:show="showDialogEditWorkflow" :intentData="selectedIntent" :workflowData="selectedWorkflow"/>

    <!-- Confirm dialog for deleting an intent-->
    <ConfirmDialog v-model:show="showDialogDeleteIntent" title="Confirm deletion of intent" 
                    body="Do you really want to delete the intent? All associated workflows will be deleted"
                    :onConfirm="confirmDelete"/>
    <!-- Confirm dialog for deleting a workflow-->
    <ConfirmDialog v-model:show="showDialogDeleteWorkflow" title="Confirm deletion of workflow" 
                    body="Do you really want to delete the workflow?"
                    :onConfirm="confirmDelete"/>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from "vue";
import {useIntentsStore} from "src/stores/intentsStore.js";
import {useWorkflowsStore} from "src/stores/workflowsStore.js";
import {useProjectsStore} from "src/stores/projectsStore.js";
import {useRouter} from "vue-router";
import ConfirmDialog from "src/components/utils/ConfirmDialog.vue";
import NoDataImage from "src/assets/NoDataImage.vue";
import EditIntentForm from "src/components/forms/EditIntentForm.vue";
import EditWorkflowForm from "src/components/forms/EditWorkflowForm.vue";
import DialogWithVisualizedPlan from "../../components/intents/visualize_plan/DialogWithVisualizedPlan.vue";
import FullScreenToggle from "./TableUtils/FullScreenToggle.vue";

const intentsStore = useIntentsStore()
const workflowsStore = useWorkflowsStore()
const projectID = useProjectsStore().currentProject.projectId
const router = useRouter()

const visualizedPlan = ref(null)
const selectedIntent = ref(null);
const selectedWorkflow = ref(null);

const search = ref("")

const showVisualizePlan = ref(false)
const showDialogDeleteIntent = ref(false)
const showDialogDeleteWorkflow= ref(false)
const showDialogEditIntent = ref(false)
const showDialogEditWorkflow = ref(false)

const rows = computed(() => {
return intentsStore.intents
  .filter((intent) => intent.workflows && intent.workflows.length > 0) // We only display intents with workflows
  .map((intent) => {
    return {
      ...intent,
      dataProductName: intent.dataProduct.datasetName, // Get data product name from data product
      workflows: intent.workflows, // Get workflows from intent
      problem: intent.problem.substring(intent.problem.lastIndexOf('#') + 1), // Get substring from last # till the end
    };
  });
});

const columns = [
  {name: "intentID", label: "ID", align: "center", field: "intentID", sortable: true},
  {name: "intentName", label: "Intent Name", align: "center", field: "intentName", sortable: true},
  {name: "dataProductName", label: "Associated Data product", align: "center", field: "dataProductName", sortable: true},
  {name: "problem", label: "Problem", align: "center", field: "problem", sortable: true},
  {name: "expand", label: "Workflows", align: "center", field: "expand", sortable: false},
  {name: 'actions', label: 'Actions', align: 'center', field: 'actions', sortable: false,},
];

onMounted(async() => {
  await intentsStore.getAllIntents(projectID)
})

const visualizeWorkflow = (visualRepresentation) => {
  visualizedPlan.value = visualRepresentation
  showVisualizePlan.value = true
}

let confirmDelete = () => {}

const deleteIntent = (propsRow) => {
  showDialogDeleteIntent.value = true
  confirmDelete = () => {
    intentsStore.deleteIntent(projectID, propsRow.row.intentID)
  }
}

const deleteWorkflow = (intent, workflow) => {
  showDialogDeleteWorkflow.value = true
  confirmDelete = () => {
    workflowsStore.deleteWorkflow(projectID, intent.intentID, workflow.workflowID)
  }
}

const editIntent = (propsRow) => {
  showDialogEditIntent.value = true
  selectedIntent.value = propsRow.row
}

const editWorkflow = (intent, workflow) => {
  showDialogEditWorkflow.value = true
  selectedIntent.value = intent
  selectedWorkflow.value = workflow
}

</script>
