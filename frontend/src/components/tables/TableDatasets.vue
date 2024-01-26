<template>
  <div class="q-pa-md">
    <div>
      <!-- Table for non-integrated datasets -->
      <h5> New Datasets </h5>
      <q-table ref="tableRef" :rows="rows.filter(dataset => !isDatasetIntegrated(dataset))" 
              :columns="columns" :filter="search" row-key="id">

        <template v-slot:top-left="">
          <div class="q-table__title">
            Datasets
            <q-btn unelevated padding="none" color="primary700" icon="add" @click="showSelectRepository = true"/>
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

        <template v-slot:no-data>
          <div class="full-width row flex-center text-accent q-gutter-sm q-pa-xl" style="flex-direction: column">
            <NoDataImage/>
            <span style="color: rgb(102, 102, 135);font-weight: 500;font-size: 1rem;line-height: 1.25;">No datasets.</span>
          </div>
        </template>

        <template v-slot:body-cell-timestamp="props">
          <q-td :props="props">
            {{ formatTimestamp(props.row.created_at) }}
          </q-td>
        </template>

        <template v-slot:body-cell-actions="props">
          <q-td :props="props">
            <q-btn dense round flat color="grey" icon="more_vert" label="">
              <q-menu auto-close>
                <q-list style="min-width: 100px">

                  <ActionItem iconName="remove_red_eye" actionLabel="See schema" @click="showGraph(props)" />
                  <ActionItem iconName="bookmark" actionLabel="Set base schema" @click="setProjectSchema(props)" />
                  <ActionItem iconName="join_full" actionLabel="Integrate schema" :isDisabled="isDatasetIntegrated(props.row)" @click="integrateRow(props)" />

                  <q-separator />

                  <ActionItem iconName="edit" actionLabel="Edit dataset" @click="editRow(props)" />
                  <ActionItem iconName="delete" actionLabel="Delete dataset" :isDisabled="isDatasetIntegrated(props.row)" @click="deleteRow(props)" />

                  <q-separator />

                  <ActionItem iconName="download" actionLabel="Download schema"  @click="datasetsStore.downloadDatasetSchema(route.params.id, props.row.id)" />

                </q-list>
              </q-menu>
            </q-btn>
          </q-td>
        </template>
      </q-table>
    </div>

    <div>
      <!-- Table for integrated datasets -->
      <h5>Integrated Datasets</h5>
      <q-table ref="tableRef" :rows="rows.filter(dataset => isDatasetIntegrated(dataset))" 
               :columns="columns" :filter="search" row-key="id">

      <template v-slot:top-left="">
        <div class="q-table__title">
          <q-btn label="Integrated schema" dense color="primary" icon="download" @click="projectsStore.downloadProjectSchema(route.params.id)"
                 style="margin-right:10px"></q-btn>
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

      <template v-slot:no-data>
        <div class="full-width row flex-center text-accent q-gutter-sm q-pa-xl" style="flex-direction: column">
          <NoDataImage/>
          <span style="color: rgb(102, 102, 135);font-weight: 500;font-size: 1rem;line-height: 1.25;">There are no datasets integrated yet.</span>
          <span style="color: rgb(102, 102, 135);font-weight: 500;font-size: 1rem;line-height: 1.25;">To integrate datasets, set one of them as the base schema of project, please add at least two sources.</span>
        </div>
      </template>

      <template v-slot:body-cell-timestamp="props">
        <q-td :props="props">
          {{ formatTimestamp(props.row.created_at) }}
        </q-td>
      </template>

        <template v-slot:body-cell-actions="props">
          <q-td :props="props">
            <q-btn dense round flat color="grey" icon="more_vert" label="">
              <q-menu auto-close>
                <q-list style="min-width: 100px">

                  <ActionItem iconName="remove_red_eye" actionLabel="See schema" @click="showGraph(props)" />
                  <ActionItem iconName="bookmark" actionLabel="Set base schema" @click="setProjectSchema(props)" />

                  <q-separator />

                  <ActionItem iconName="edit" actionLabel="Edit dataset" @click="editRow(props)" />
                  
                  <q-separator />

                  <ActionItem iconName="download" actionLabel="Download schema"  @click="datasetsStore.downloadDatasetSchema(route.params.id, props.row.id)" />

                </q-list>
              </q-menu>
            </q-btn>

          </q-td>
        </template>
    </q-table>
    </div>

    <!-- Additional dialogs that appear to fufill certain actions -->

    <FormSelectRepository v-model:show="showSelectRepository" @repository-selected="handleRepositorySelected"></FormSelectRepository>

    <FormNewDataSource v-model:show="showPostDataset"></FormNewDataSource>
    <EditDatasetForm v-model:show="showEditDialog" :datasetData="selectedDataset" />

    <!-- Confirm dialog for deleting a dataset-->
    <ConfirmDialog v-model:show="showDeleteDialog" title="Confirm deletion of dataset" 
                    body="Do you really want to delete the dataset?"
                    :onConfirm="confirmDelete"/>
                    
    <!-- Confirm dialog to confirm setting a new base schema for the project -->
    <ConfirmDialog v-model:show="showSetProjectSchema" :title="projectsStore.getGlobalSchema !== null ? 'Overwrite Project Schema' : 'Set Dataset Schema as Project Base Schema'" 
                    :body="projectsStore.getGlobalSchema !== null ? 'Do you want to overwrite the existing project schema?' : 'Do you want to set the dataset schema as the base integration schema for the project?'"
                    :onConfirm="confirmSetProjectSchema"/>

    <!-- Dialog showcasing the graph schema -->
    <q-dialog v-model="showGraphDialog">
      <q-card style="max-width: 400px; margin: 20px auto;">
        <q-card-section class="q-pt-md q-pb-md">
          <div class="text-h6">Graphical Data</div>
        </q-card-section>
        <q-card-section class="q-pt-none">
          <div class="q-dialog__content">
            <Graph v-if="selectedGraphical" :graphical="selectedGraphical"></Graph>
          </div>
        </q-card-section>
        <q-separator />
        <q-card-actions align="center">
          <q-btn label="Cerrar" color="primary" @click="showGraphDialog = false" />
        </q-card-actions>
      </q-card>
    </q-dialog>

  </div>
</template>

<script setup>
import {computed, onMounted, ref} from "vue";
import {useDatasetsStore} from 'src/stores/datasetsStore.js';
import {useIntegrationStore} from 'src/stores/integrationStore.js';
import {useProjectsStore} from 'src/stores/projectsStore.js';
import {useNotify} from 'src/use/useNotify.js';
import FormNewDataSource from "components/forms/FormNewDataSource.vue";
import {useRouter, useRoute} from "vue-router";
import ConfirmDialog from "src/components/ConfirmDialog.vue";
import EditDatasetForm from "components/forms/EditDatasetForm.vue";
import Graph from "../graph/Graph.vue";
import { QBtn, QMenu } from 'quasar';
import FormSelectRepository from "../forms/FormSelectRepository.vue";
import NoDataImage from "src/assets/NoDataImage.vue";
import ActionItem from "./TableUtils/ActionItem.vue";
import FullScreenToggle from "./TableUtils/FullScreenToggle.vue";

const datasetsStore = useDatasetsStore()
const integrationStore = useIntegrationStore()
const projectsStore = useProjectsStore()
const notify = useNotify()
const router = useRouter()
const route = useRoute()

const selectedGraphical = ref(null)
const selectedDataset = ref(false)

const search = ref("")

const showEditDialog = ref(false)
const showDeleteDialog = ref(false)
const showSetProjectSchema = ref(false)
const showGraphDialog = ref(false)
const showSelectRepository = ref(false);
const showPostDataset = ref(false);

const rows = computed(() => {
  return datasetsStore.datasets.map((dataset) => {
    return {
      ...dataset,
      repositoryName: dataset.repository.repositoryName,
    }
  })
})

const columns = [
  {name: "id", label: "ID", align: "center", field: "id", sortable: true,},
  {name: "Name", label: "Name", align: "center", field: "datasetName", sortable: true,},
  {name: 'repository', label: 'Repository', align: 'center', field: 'repositoryName', sortable: true,},
  {name: "timestamp", label: "Upload date", align: "center", field: "created_at", sortable: true,},
  {name: 'actions', label: 'Actions', align: 'center', field: 'actions', sortable: false,},
]

onMounted(() => {
  datasetsStore.getDatasets(route.params.id);
})

const showGraph = (props) => {
  selectedGraphical.value = props.row.localGraph.graphicalSchema;
  showGraphDialog.value = true;
}

let confirmSetProjectSchema = () => {}
const setProjectSchema = (propsRow) => {
  showSetProjectSchema.value = true;
  confirmSetProjectSchema = () => {
    datasetsStore.setDatasetSchemaAsProjectSchema(route.params.id, propsRow.row.id);
  }
}

const isDatasetIntegrated = (row) => {
  const integratedDatasets = projectsStore.currentProject.integratedDatasets;
  return integratedDatasets.some(dataset => dataset.id === row.id);
}

const formatTimestamp = (timestamp) => {
  const date = new Date(timestamp)
  const formattedDate = `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}:${date.getSeconds().toString().padStart(2, '0')}`;
  return formattedDate;
}

const handleRepositorySelected = () => {
  showSelectRepository.value = false;
  showPostDataset.value = true;
}

let confirmDelete = () => {}
const deleteRow = (propsRow) => {
  showDeleteDialog.value = true
  confirmDelete = () => {
    datasetsStore.deleteDataset(route.params.id, propsRow.row.id)
  }
}

const editRow = (props) => {
  selectedDataset.value = props.row; 
  showEditDialog.value = true;
}

const integrateRow = (props) => {
  integrationStore.selectDatasetToIntegrate(props.row)
  if (projectsStore.getGlobalSchema === null) {
    notify.negative("There's no base schema set. Define one to start the integration.")
  }
  else {
    router.push({name: 'dsIntegration'})
  }
}

</script>