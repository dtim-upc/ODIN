<template>
  <q-card-section>
    <div class="text-h6">Edit dataset</div>
  </q-card-section>
  <q-card-section>

    <q-form ref="form" @submit="onSubmit" @reset="onReset" class="q-gutter-md">
      <!--
      <q-checkbox v-model="createNewRepository" label="Create new repository"
                  :disable="storeDS.repositories.length === 0"/>
      -->

      <!-- Show the input field for the name of the new repository if "Nuevo repositorio" is selected -->
      <!--
      <q-input v-if="createNewRepository" filled v-model="newDatasource.repositoryName"
               label="Name of the new repository" lazy-rules
               :rules="[(val) => (val && val.length > 0) || 'Please type a name']"/>
      <q-select v-else
                filled
                v-model="newDatasource.repositoryId"
                :options="storeDS.repositories"
                label="Repository"
                class="q-mt-none"
                emit-value
                map-options
                option-value="id"
                option-label="repositoryName"
                :rules="[(val) => !!val || 'Please select a repository']"
                @input="onRepositoryChange"
      />
      -->

      <q-input filled v-model="newDatasource.datasetName" label="Introduce a dataset name" lazy-rules
               :rules="[(val) => (val && val.length > 0) || 'Please type a name', ]"/>

      <q-input v-model="newDatasource.datasetDescription" filled autogrow label="Description (Optional)"/>

      <div v-if="showFormButtons">
        <q-btn label="Update" type="submit" color="primary" v-close-popup/>
        <q-btn label="Cancel" type="reset" color="primary" flat class="q-ml-sm" v-close-popup/>
      </div>
    </q-form>
  </q-card-section>
</template>

<script setup>
import {ref, reactive, onMounted} from "vue";
import {useNotify} from 'src/use/useNotify.js'
import {useRoute, useRouter} from "vue-router";
import {useDataSourceStore} from "stores/datasourcesStore";
import {useRepositoriesStore} from "stores/repositoriesStore.js";

// -------------------------------------------------------------
//                         PROPS & EMITS
// -------------------------------------------------------------

const props = defineProps({
  show: {type: Boolean, default: false, required: true},
  showFormButtons: {type: Boolean, default: true},
  afterSubmitShowGraph: {type: Boolean, default: true},
  datasetData:{type: Object, default: null},
});


const emit = defineEmits(["update:show"])

const storeDS = useDataSourceStore();
const repositoriesStore = useRepositoriesStore();

// -------------------------------------------------------------
//                         STORES & GLOBALS
// -------------------------------------------------------------
// Function to set the initial value of repositoryId and repositoryName based on the datasetData
const setInitialValues = () => {
  if (props.datasetData) {
    // Get the repositoryId of the datasetData
    const datasetRepositoryId = getDatasetRepositoryId(props.datasetData.id);
    newDatasource.repositoryId = datasetRepositoryId || null;
    // If a repositoryId is found, get the repository name
    newDatasource.repositoryName = datasetRepositoryId ? getRepositoryName(datasetRepositoryId) : "";
  }
};

// Function to find the repositoryId based on the datasetId
const getDatasetRepositoryId = (datasetId) => {
  let repositoryId = null;
  repositoriesStore.repositories.forEach((repo) => {
    const foundDataset = repo.datasets.find((dataset) => dataset.id === datasetId);
    if (foundDataset) {
      repositoryId = repo.id;
    }
  });
  return repositoryId;
};

const getRepositoryName = (repositoryId) => {
  let repositoryName = null;
  repositoriesStore.repositories.forEach((repo) => {
    if (repo.id === repositoryId) {
      repositoryName = repo.name;
    }
  });
  return repositoryName;
};

const onRepositoryChange = () => {
  if (createNewRepository.value) {
    // User selected "Create new repository"
    newDatasource.repositoryId = null;
  } else {
    // User selected an existing repository
    const selectedRepo = repositoriesStore.repositories.find(repo => repo.id === newDatasource.repositoryId);
    if (selectedRepo) {
      newDatasource.repositoryId = selectedRepo.id;
    } else {
      newDatasource.repositoryId = null; // Handle the case when the selected repository is not found
    }
  }
}

const projectID = ref(null)
const createNewRepository = ref(false); // Variable para determinar si se va a crear un nuevo repositorio


// When the component is mounted, fetch the repositories for the current project.
onMounted(async () => {
  const url = window.location.href; // Get the current URL
  const regex = /project\/(\d+)\//;
  const match = url.match(regex);
  let projectId;
  if (match) {
    projectId = match[1];
    console.log(projectId + "+++++++++++++++++++++++1 id del proyecto cogido"); // Output: 1
    projectID.value = projectId;
    await repositoriesStore.getAllRepositories(projectID.value)
    if (repositoriesStore.repositories.length === 0) createNewRepository.value = true;
  }
  setInitialValues();

});

const route = useRoute()
// -------------------------------------------------------------
//                         Others
// -------------------------------------------------------------

const form = ref(null)
const notify = useNotify()

defineExpose({
  form
})

const options = [
  "Local file/s",
  "SQL Database",
];

const newDatasource = reactive({
  //repositoryId: props.datasetData ? props.datasetData.repository.id : ref(null),
  repositoryName: props.datasetData ? '':"",
  datasetId: props.datasetData ? props.datasetData.id :ref(null),
  datasetName: props.datasetData ? props.datasetData.datasetName :'',
  datasetDescription: props.datasetData ? props.datasetData.datasetDescription :'',
})


const uploadedFiles = ref([]);
const onReset = () => {
  uploadedFiles.value = null;
}

const onSubmit = () => {
    const data = new FormData();
    data.append("datasetName", newDatasource.datasetName);
    data.append("datasetDescription", newDatasource.datasetDescription);
    storeDS.editDatasource(projectID.value, newDatasource.datasetId, data, successCallback)
}

const successCallback = () => {

  console.log("success callback")

  //form.value.resetValidation()

  //integrationStore.addSelectedDatasource(datasource)
  storeDS.getDatasources(route.params.id)
}
</script>

<style lang="scss">
.fileBoxLabel {

  margin: 0;
  padding: 0;
  border: 0;
  font: inherit;
  vertical-align: baseline;


}

.fileBox {

  .q-field__control {
    height: 150px;
  }

  .q-field__control:before {
    border: 1px dashed #A4A4A4;
  }


}

.fileUploadBox {

  position: absolute;
  z-index: 1;
  box-sizing: border-box;
  display: table;
  table-layout: fixed;
  width: 100px;
  height: 80px;
  top: 86px;
  left: 100px;
  border: 1px dashed #A4A4A4;
  border-radius: 3px;
  text-align: center;
  overflow: hidden;


  .contentFile {

    display: table-cell;
    vertical-align: middle;


  }

  input {

    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    opacity: 0;


  }


}


</style>
