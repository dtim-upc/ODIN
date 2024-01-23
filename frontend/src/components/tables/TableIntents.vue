<template>
    <div class="q-pa-md">
      <q-table :rows="rows" :columns="columns" :filter="search" row-key="id"
               no-data-label="No intents in the system. Consider creating a new intent."
               no-results-label="The filter didn't uncover any results" :visible-columns="visibleColumns">
                
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
  
          <q-btn flat round dense :icon="props.inFullscreen ? 'fullscreen_exit' : 'fullscreen'"
                 @click="props.toggleFullscreen">
            <q-tooltip :disable="$q.platform.is.mobile" v-close-popup>
              {{ props.inFullscreen ? "Exit Fullscreen" : "Toggle Fullscreen" }}
            </q-tooltip>
          </q-btn>
        </template>
  
        <template v-slot:body-cell-actions="props">
          <q-td :props="props">
            <q-btn dense round flat color="grey" @click="editIntent(props)" icon="edit"></q-btn>
            <q-btn dense round flat color="grey" @click="deleteIntent(props)" icon="delete"></q-btn>
          </q-td>
        </template>
 
  
        <!-- New slot for expandable content -->
        <template v-slot:body-cell-expand="props">
  
          <q-td :props="props">
          <!-- Use q-expansion-item to make rows expandable with datasets -->
          <q-expansion-item :label="'Show workflows'">
            <!-- Content to be displayed when the row is expanded -->
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
                    <q-btn @click="visualizeWorkflow(workflow.visualRepresentation)" color="primary" icon="visibility" size="sm"></q-btn>
                  </td>
                  <td>
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
      
      <DialogWithVisualizedPlan v-model:dialog="visualizePlan" :visualizedPlan="visualizedPlan"/>
  
      <q-dialog v-model="showDialogEditIntent">
        <q-card flat bordered style="min-width: 30vw;">
          <q-card-section class="q-pt-none">
            <EditIntentForm
              @submit-success="showDialogEditIntent=false"
              @cancel-form="showDialogEditIntent=false"
              :intentData="selectedIntent"
            ></EditIntentForm>
          </q-card-section>
        </q-card>
      </q-dialog>

      <q-dialog v-model="showDialogEditWorkflow">
        <q-card flat bordered style="min-width: 30vw;">
          <q-card-section class="q-pt-none">
            <EditWorkflowForm
              @submit-success="showDialogEditWorkflow=false"
              @cancel-form="showDialogEditWorkflow=false"
              :workflowData="selectedWorkflow"
              :intentData="selectedIntent"
            ></EditWorkflowForm>
          </q-card-section>
        </q-card>
      </q-dialog>
  
      <ConfirmDialog v-model:show="showDialogDeleteIntent" title="Confirm deletion of intent" 
                      body="Do you really want to delete the intent? All associated workflows will be deleted"
                      :onConfirm="confirmDelete"/>
      <ConfirmDialog v-model:show="showDialogDeleteWorkflow" title="Confirm deletion of workflow" 
                      body="Do you really want to delete the workflow?"
                      :onConfirm="confirmDelete"/>
    </div>
  </template>
  
  <script setup>
  import {computed, onBeforeMount, onMounted, ref} from "vue";
  import {useDataSourceStore} from 'src/stores/datasourcesStore.js'
  import {useIntegrationStore} from 'src/stores/integrationStore.js'
  import {useIntentsStore} from "src/stores/intentsStore.js";
  import {useWorkflowsStore} from "src/stores/workflowStore.js";
  import {useRoute, useRouter} from "vue-router";
  import ConfirmDialog from "src/components/ConfirmDialog.vue";
  import EditIntentForm from "src/components/forms/EditIntentForm.vue";
  import EditWorkflowForm from "src/components/forms/EditWorkflowForm.vue";
  import DialogWithVisualizedPlan from "../../components/intents/DialogWithVisualizedPlan.vue";
  
  const route = useRoute()
  const router = useRouter()
  
  /*
    store
  */
  const storeDS = useDataSourceStore();
  const integrationStore = useIntegrationStore();
  const intentsStore = useIntentsStore()
  const workflowsStore = useWorkflowsStore()
  
  onBeforeMount(() => {
    storeDS.setProject()
    integrationStore.init()
  })

  const visualizePlan = ref(false)
  const visualizedPlan = ref(null)
  
  const selectedIntent = ref(null);
  const selectedWorkflow = ref(null);
  const search = ref("")
  const visibleColumns = ["intentID", "intentName", "dataProductName", "problem", "expand", "actions"]; // Columns to be displayed
  
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
    await intentsStore.getAllIntents(route.params.id)
  })

  const visualizeWorkflow = (visualRepresentation) => {
    visualizedPlan.value = visualRepresentation
    visualizePlan.value = true
  }
  
  let confirmDelete = () => {}

  const deleteIntent = (propsRow) => {
    showDialogDeleteIntent.value = true
    confirmDelete = () => {
      intentsStore.deleteIntent(route.params.id, propsRow.row.intentID)
    }
  }

  const deleteWorkflow = (intent, workflow) => {
    showDialogDeleteWorkflow.value = true
    confirmDelete = () => {
      workflowsStore.deleteWorkflow(route.params.id, intent.intentID, workflow.workflowID)
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
  
  <style lang="css" scoped>
  .centered-table {
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  .centered-table table {
    width: 100%;
  }
  </style>
  