<template>
  <div class="q-pa-md">
    <q-table :grid="gridEnable" ref="tableRef" :rows="rows" :columns="columns" :filter="search"
             :class="{ 'no-shadow': no_shadow }" row-key="id"
             no-data-label="I didn't find anything for you. Consider creating a new data source."
             no-results-label="The filter didn't uncover any results" :visible-columns="visibleColumns">

      <template v-slot:top-left="">
        <div class="q-table__title">
          {{ title }}
          <q-btn unelevated v-if="view === 'repositories'" padding="none" color="primary700" icon="add"
                 @click="addDataRepository = true"/>
        </div>
      </template>

      <template v-slot:top-right="props">
        <q-btn v-if="!integrationStore.isDSEmpty" outline
               color="primary" label="Finish pending sources" class="q-mr-xs"
               :to="{ name: 'dsIntegration' }">
          <q-badge color="orange" floating>{{ integrationStore.datasources.length }}</q-badge>
        </q-btn>

        <q-btn label="Integrated schema" dense color="primary" icon="download" @click="projectsStore.downloadProjectSchema(route.params.id)"
               style="margin-right:10px"></q-btn>

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

      <template v-if="view === 'repositories'" v-slot:body-cell-actions="props">
        <q-td :props="props">
          <!-- <q-btn dense round flat color="grey" :to="'/dataSources/view/' + props.row.id" -->
          <!-- icon="remove_red_eye"></q-btn> -->
          <q-btn dense round flat color="grey" @click="editRow(props)" icon="edit"></q-btn>
          <q-btn dense round flat color="grey" @click="deleteRow(props)" icon="delete"></q-btn>
        </q-td>
      </template>

      <template v-slot:no-data="{ icon, message, filter }">
        <div class="full-width row flex-center text-accent q-gutter-sm q-pa-xl" style="flex-direction: column">
          <svg width="160px" height="88px" viewBox="0 0 216 120" fill="none" xmlns="http://www.w3.org/2000/svg"
               class="sc-jIkXHa sc-ZOtfp fXAzWm jPTZgW">
            <g opacity="0.84" clip-path="url(#EmptyDocuments_svg__clip0_1142_57509)">
              <path fill-rule="evenodd" clip-rule="evenodd"
                    d="M189.25 19.646a7.583 7.583 0 010 15.166h-43.333a7.583 7.583 0 010 15.167h23.833a7.583 7.583 0 010 15.167h-11.022c-5.28 0-9.561 3.395-9.561 7.583 0 1.956 1.063 3.782 3.19 5.48 2.017 1.608 4.824 1.817 7.064 3.096a7.583 7.583 0 01-3.754 14.174H65.75a7.583 7.583 0 010-15.166H23.5a7.583 7.583 0 110-15.167h43.333a7.583 7.583 0 100-15.167H39.75a7.583 7.583 0 110-15.166h43.333a7.583 7.583 0 010-15.167H189.25zm0 30.333a7.583 7.583 0 110 15.166 7.583 7.583 0 010-15.166z"
                    fill="#D9D8FF" fill-opacity="0.8"></path>
              <path fill-rule="evenodd" clip-rule="evenodd"
                    d="M132.561 19.646l10.077 73.496.906 7.374a4.334 4.334 0 01-3.773 4.829l-63.44 7.789a4.333 4.333 0 01-4.83-3.772l-9.767-79.547a2.166 2.166 0 011.91-2.417l5.262-.59 63.655-7.162zM73.162 26.33l4.97-.557-4.97.557z"
                    fill="#fff"></path>
              <path
                d="M73.162 26.33l4.97-.557m54.429-6.127l10.077 73.496.906 7.374a4.334 4.334 0 01-3.773 4.829l-63.44 7.789a4.333 4.333 0 01-4.83-3.772l-9.767-79.547a2.166 2.166 0 011.91-2.417l5.262-.59 63.655-7.162z"
                stroke="#7B79FF" stroke-width="2.5"></path>
              <path fill-rule="evenodd" clip-rule="evenodd"
                    d="M129.818 24.27l9.122 66.608.82 6.682c.264 2.153-1.246 4.11-3.373 4.371l-56.812 6.976c-2.127.261-4.066-1.272-4.33-3.425l-8.83-71.908a2.167 2.167 0 011.887-2.415l7.028-.863"
                    fill="#F0F0FF"></path>
              <path fill-rule="evenodd" clip-rule="evenodd"
                    d="M135.331 5.833H85.978a2.97 2.97 0 00-2.107.873A2.97 2.97 0 0083 8.813v82.333c0 .823.333 1.567.872 2.106a2.97 2.97 0 002.107.873h63.917a2.97 2.97 0 002.106-.873 2.97 2.97 0 00.873-2.106V23.367a2.98 2.98 0 00-.873-2.107L137.437 6.705a2.98 2.98 0 00-2.106-.872z"
                    fill="#fff" stroke="#7B79FF" stroke-width="2.5"></path>
              <path
                d="M135.811 7.082v12.564a3.25 3.25 0 003.25 3.25h8.595M94.644 78.146h28.167m-28.167-55.25h28.167-28.167zm0 13h46.584-46.584zm0 14.083h46.584-46.584zm0 14.084h46.584-46.584z"
                stroke="#7B79FF" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"></path>
            </g>
            <defs>
              <clipPath id="EmptyDocuments_svg__clip0_1142_57509">
                <path fill="#fff" d="M0 0h216v120H0z"></path>
              </clipPath>
            </defs>
          </svg>
          <span style="color: rgb(102, 102, 135);font-weight: 500;font-size: 1rem;line-height: 1.25;">Data sources not found.</span>
          <span style="color: rgb(102, 102, 135);font-weight: 500;font-size: 1rem;line-height: 1.25;">To integrate data sources with the project, please add at least two sources.</span>
        </div>
      </template>

      <!-- New slot for expandable content -->
      <template v-slot:body-cell-expand="props">

        <q-td :props="props">
          <!-- Use q-expansion-item to make rows expandable with datasets -->
          <q-expansion-item :label="'Show datasets'">
            <!-- Content to be displayed when the row is expanded -->
            <div class="centered-table">
              <table>
                <thead>
                <tr>
                  <th><b>Dataset Id</b></th>
                  <th><b>Dataset Name</b></th>
                  <th><b>Is integrated</b></th>
                </tr>
                </thead>
                <tbody>
                <tr v-if="props.row.datasets && props.row.datasets.length > 0" v-for="dataset in props.row.datasets" :key="dataset.id">
                  <td>{{ dataset.id }}</td>
                  <td>{{ dataset.datasetName }}</td>
                  <td>{{ storeDS.project.integratedDatasets.some(integratedDataset => integratedDataset.id === dataset.id) ? 'Yes' : 'No' }}</td>
                </tr>
                <tr v-else>
                  <td colspan="3">There are no datasets in this repository</td>
                </tr>
                </tbody>
              </table>
            </div>
          </q-expansion-item>
        </q-td>

      </template>

    </q-table>

    <FormNewRepository v-model:show="addDataRepository"></FormNewRepository>

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
import {computed, defineComponent, onBeforeMount, onMounted, defineProps, ref} from "vue";
import {useDataSourceStore} from 'src/stores/datasourcesStore.js'
import {useIntegrationStore} from 'src/stores/integrationStore.js'
import {useProjectsStore} from 'src/stores/projectsStore.js'
import {useNotify} from 'src/use/useNotify.js'
import FormNewRepository from "components/forms/FormNewRepository.vue";
import {useRepositoriesStore} from "src/stores/repositoriesStore.js";
import {useRoute} from "vue-router";
import ConfirmDialog from "src/components/ConfirmDialog.vue";
import EditRepositoryForm from "src/components/forms/EditRepositoryForm.vue";

const route = useRoute()

/*
  props
*/
const props = defineProps({
  no_shadow: {type: Boolean, default: false},
  view: {type: String, default: "repositories"},
});
const gridEnable = ref(false)

/*
  store
*/
const notify = useNotify()
const storeDS = useDataSourceStore();
const integrationStore = useIntegrationStore();
const repositoriesStore = useRepositoriesStore()
const projectsStore = useProjectsStore()

onBeforeMount(() => {
  storeDS.setProject()
  integrationStore.init()
})

const selectedRepository = ref(null);
const title = "Repositories";
const search = ref("")
const visibleColumns = ["id", "repositoryName", "repositoryType", "expand", "actions"]; // Columns to be displayed

const addDataRepository = ref(false)
const showConfirmDialog = ref(false)
const editRepository = ref(false)

const rows = computed(() => {
  return repositoriesStore.repositories.map((repo) => {
    return {
      ...repo,
      datasets: repo.datasets, // Obtener la lista de Datasets asociados
    };
  });
});

const columns = [
  {name: "id", label: "ID", align: "center", field: "id", sortable: true},
  {name: "repositoryName", label: "Repository Name", align: "center", field: "repositoryName", sortable: true},
  {name: "repositoryType", label: "Repository Type", align: "center", field: "repositoryType", sortable: true},
  {name: "expand", label: "Datasets", align: "center", field: "expand", sortable: false},
  {name: 'actions', label: 'Actions', align: 'center', field: 'actions', sortable: false,},
];

onMounted(() => {
  repositoriesStore.getAllRepositories(route.params.id,)
})

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
