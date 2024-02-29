<template>
  <div class="q-pa-md">
    <!-- Main table component -->
    <q-table :rows="repositoriesStore.repositories" :columns="columns" :filter="search" row-key="id">
      <template v-slot:top-left="">
        <div class="q-table__title">
          Repositories
          <q-btn unelevated padding="none" color="primary700" icon="add" @click="showAddDataRepository = true"/>
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
          <q-btn dense round flat color="grey" @click="editRow(props)" icon="edit"></q-btn>
          <q-btn dense round flat color="grey" @click="deleteRow(props)" icon="delete"></q-btn>
        </q-td>
      </template>

      <template v-slot:no-data>
        <div class="full-width row flex-center text-accent q-gutter-sm q-pa-xl" style="flex-direction: column">
          <NoDataImage/>
          <span style="color: rgb(102, 102, 135);font-weight: 500;font-size: 1rem;line-height: 1.25;">No data repositories.</span>
        </div>
      </template>

      <template v-slot:body-cell-expand="props">
        <q-td :props="props">
          <q-expansion-item :label="'Show datasets'">
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
                  <td>{{ projectsStore.currentProject.integratedDatasets.some(integratedDataset => integratedDataset.id === dataset.id) ? 'Yes' : 'No' }}</td>
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

    <!-- Additional dialogs that appear to fufill certain actions -->

    <CreateRepositoryForm v-model:show="showAddDataRepository" />

    <EditRepositoryForm v-model:show="showEditRepository" :repositoryData="selectedRepository" />

    <!-- Confirm dialog for deleting a repository-->
    <ConfirmDialog v-model:show="showConfirmDialog" title="Confirm deletion of repository" 
                    body="Do you really want to delete the repository? All associated datasets will be deleted"
                    :onConfirm="confirmDelete"/>
  </div>
</template>

<script setup>
import { onMounted, ref} from "vue";
import {useProjectsStore} from 'src/stores/projectsStore.js'
import CreateRepositoryForm from "components/forms/CreateRepositoryForm.vue";
import {useRepositoriesStore} from "src/stores/repositoriesStore.js";
import ConfirmDialog from "src/components/utils/ConfirmDialog.vue";
import EditRepositoryForm from "src/components/forms/EditRepositoryForm.vue";
import NoDataImage from "src/assets/NoDataImage.vue";
import FullScreenToggle from "./TableUtils/FullScreenToggle.vue";
import { useNotify } from "src/use/useNotify";

const props = defineProps({
  showAddDataRepositoryProp: {type: Boolean, default: false}
});

const repositoriesStore = useRepositoriesStore()
const projectsStore = useProjectsStore()
const projectID = useProjectsStore().currentProject.projectId
const notify = useNotify()

const selectedRepository = ref(null);

const search = ref("")

const showAddDataRepository = ref(props.showAddDataRepositoryProp)
const showConfirmDialog = ref(false)
const showEditRepository = ref(false)

const columns = [
  {name: "id", label: "ID", align: "center", field: "id", sortable: true},
  {name: "repositoryName", label: "Repository Name", align: "center", field: "repositoryName", sortable: true},
  {name: "repositoryType", label: "Repository Type", align: "center", field: "repositoryType", sortable: true},
  {name: "expand", label: "Datasets", align: "center", field: "expand", sortable: false},
  {name: 'actions', label: 'Actions', align: 'center', field: 'actions', sortable: false,},
];

onMounted(() => {
  repositoriesStore.getRepositories(projectID)
})

let confirmDelete = () => {}
const deleteRow = (propsRow) => {
  // Check if some of the datasets of the repository are integrated
  let someDatasetIsIntegrated = false

  selectedRepository.value = propsRow.row
  propsRow.row.datasets.forEach(dataset => {
    if (projectsStore.currentProject.integratedDatasets.some(integratedDataset => integratedDataset.id === dataset.id)) {
      someDatasetIsIntegrated = true
    }
  })

  if (someDatasetIsIntegrated) {
    notify.negative("The repository can not be deleted because some of its datasets are integrated")
  }
  else {
    showConfirmDialog.value = true
    confirmDelete = () => {
      repositoriesStore.deleteRepository(projectID, propsRow.row.id)
    }
  }
}

const editRow = (propsRow) => {
  showEditRepository.value = true
  selectedRepository.value = propsRow.row
}
</script>
