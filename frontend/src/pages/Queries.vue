<template>
  <div class="q-pa-md">
    <q-table :grid="gridEnable" :rows="rows" :columns="columns" :filter="search"
             row-key="id"
             no-data-label="No queries created yet"
             no-results-label="The filter didn't uncover any results" :visible-columns="visibleColumns">

      <template v-slot:top-left="">
        <div class="q-table__title">
          Queries
        </div>
      </template>

      <template v-slot:top-right="props">

        <q-input outlined dense debounce="400" color="primary" v-model="search">
          <template v-slot:append>
            <q-icon name="search"/>
          </template>
        </q-input>

        <q-btn flat round dense size="xl" :icon="props.inFullscreen ? 'fullscreen_exit' : 'fullscreen'"
               @click="props.toggleFullscreen">
          <q-tooltip :disable="$q.platform.is.mobile" v-close-popup>
            {{ props.inFullscreen ? "Exit Fullscreen" : "Toggle Fullscreen" }}
          </q-tooltip>
        </q-btn>
      </template>

      <template v-slot:body-cell-actions="props">
        <q-td :props="props">
          <q-btn dense round flat color="grey" @click="editRow(props)" icon="edit"></q-btn>
          <q-btn dense round flat color="grey" @click="deleteRow(props)" icon="delete"></q-btn>
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
                </tr>
                </thead>
                <tbody>
                <tr v-if="props.row.workflows && props.row.workflows.length > 0" v-for="workflow in props.row.workflows" :key="workflow.workflowID">
                  <td>{{ workflow.workflowID }}</td>
                  <td>{{ workflow.workflowName }}</td>
                  <td>
                    <q-btn @click="visualizeWorkflow(workflow.visualRepresentation)" color="primary" icon="visibility" size="sm"></q-btn>
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

    <q-dialog v-model="editRepository">
      <q-card flat bordered class="my-card" style="min-width: 30vw;">
        <q-card-section class="q-pt-none">
          <EditRepositoryForm
            @submit-success="editRepository=false"
            @cancel-form="editRepository=false"
            :repositoryData="selectedRepository"
          ></EditRepositoryForm>
        </q-card-section>
      </q-card>
    </q-dialog>

    <ConfirmDialog v-model:show="showConfirmDialog" title="Confirm deletion of repository" 
                    body="Do you really want to delete the repository? All associated datasets will be deleted"
                    :onConfirm="confirmDelete"/>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from "vue";
import {useDataSourceStore} from 'src/stores/datasources.store.js'
import {useQueriesStore} from 'src/stores/queriesStore.js'
import {useNotify} from 'src/use/useNotify.js'
import {useRepositoriesStore} from "src/stores/repositories.store.js";
import {useRoute} from "vue-router";
import ConfirmDialog from "src/components/ConfirmDialog.vue";
import EditRepositoryForm from "src/components/forms/EditRepositoryForm.vue";
import DialogWithVisualizedPlan from "../components/intents/DialogWithVisualizedPlan.vue";

const route = useRoute()

const gridEnable = ref(false)
const visualizePlan = ref(false)
const visualizedPlan = ref(null)

/*
  store
*/
const notify = useNotify()
const storeDS = useDataSourceStore();
const queriesStore = useQueriesStore();
const repositoriesStore = useRepositoriesStore()

onMounted(async() => {
  storeDS.setProject()
  await queriesStore.getQueries(route.params.id)
  console.log("queries", queriesStore.queries)
})

const selectedRepository = ref(null);
const search = ref("")
const visibleColumns = ["queryID", "queryName", "expand", "actions"]; // Columns to be displayed

const showConfirmDialog = ref(false)
const editRepository = ref(false)

const rows = computed(() => {
  return queriesStore.queries.map((query) => {
    return {
      ...query,
      workflows: query.workflows,
    };
  });
});

const columns = [
  {name: "queryID", label: "ID", align: "center", field: "queryID", sortable: true},
  {name: "queryName", label: "Query Name", align: "center", field: "queryName", sortable: true},
  {name: "expand", label: "Workflows", align: "center", field: "expand", sortable: false},
  {name: 'actions', label: 'Actions', align: 'center', field: 'actions', sortable: false,},
];

const visualizeWorkflow = (visualRepresentation) => {
  visualizedPlan.value = visualRepresentation
  visualizePlan.value = true
}

let confirmDelete = () => {}
const deleteRow = (propsRow) => {
  showConfirmDialog.value = true
  confirmDelete = () => {
    repositoriesStore.deleteRepository(route.params.id, propsRow.row.id)
  }
}

const editRow = (propsRow) => {
  editRepository.value = true
  selectedRepository.value = propsRow.row
}
</script>

<style lang="css" scoped>
.centered-table {
  display: flex;
  justify-content: center; /* Centra horizontalmente el contenido de la tabla */
  align-items: center; /* Centra verticalmente el contenido de la tabla */
}

.centered-table table {
  width: 100%; /* Asegura que la tabla ocupe todo el ancho disponible */
}
</style>
