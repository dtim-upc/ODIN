<template>
  <q-dialog v-model="showS" @hide="props.show=false">
    <!--  -->
    <q-card style="width: 400px; max-width: 80vw">
      <q-card-section>
        <div class="text-h6">Create new dataset</div>
      </q-card-section>

      <q-card-section>

        <q-form ref="form" @submit="onSubmit" @reset="onReset" class="q-gutter-md">
          <q-checkbox v-model="createNewRepository" label="Create new repository" :disable="storeDS.repositories.length === 0"/>

          <!-- Show the input field for the name of the new repository if "Nuevo repositorio" is selected -->
          <q-input v-if="createNewRepository" filled v-model="newDatasource.repositoryName" label="Name of the new repository" lazy-rules
                   :rules="[(val) => (val && val.length > 0) || 'Please type a name']"/>
          <q-select v-else
                    filled
                    v-model="selectedRepositoryName"
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

          <q-input filled v-model="newDatasource.datasetName" label="Introduce a dataset name" lazy-rules
                   :rules="[(val) => (val && val.length > 0) || 'Please type a name', ]"/>
          <q-select v-model="DataSourceType" :options="options" label="Type" class="q-mt-none"/>

          <q-input v-model="newDatasource.datasetDescription" filled autogrow label="Description (Optional)"/>

          <q-file ref="fileds" outlined v-model="uploadedFile" auto-expand
                  label="Select the file you would like to import."
                  :headers="{ 'content-type': 'multipart/form-data' }" accept=".csv, application/json" :max-files="1"
                  lazy-rules :rules="[(val) => (val && val.name !== '') || 'Please upload a file' ]">
            <template v-slot:prepend>
              <q-icon name="attach_file" @click="this.$refs.fileds.pickFiles();"/>
            </template>
          </q-file>

          <div v-if="showFormButtons">
            <q-btn label="Submit" type="submit" color="primary"/>
            <q-btn label="Cancel" type="reset" color="primary" flat class="q-ml-sm" v-close-popup/>
          </div>
        </q-form>
      </q-card-section>
    </q-card>
  </q-dialog>

</template>

<script setup>
import {ref, reactive, onMounted, watch, computed} from "vue";
import {useNotify} from 'src/use/useNotify.js'
import {useRoute, useRouter} from "vue-router";
import {useIntegrationStore} from 'src/stores/integration.store.js'
import {useDataSourceStore} from "../../stores/datasources.store";

// -------------------------------------------------------------
//                         PROPS & EMITS
// -------------------------------------------------------------

const props = defineProps({
  show: {type: Boolean, default: false, required: true},
  showFormButtons: {type: Boolean, default: true},
  afterSubmitShowGraph: {type: Boolean, default: true},
});


const emit = defineEmits(["update:show"])
const showS = computed({
  get() {
    return props.show
  },
  set(newValue) {
    emit('update:show', newValue)
  }
})

const storeDS = useDataSourceStore();

// -------------------------------------------------------------
//                         STORES & GLOBALS
// -------------------------------------------------------------
const onRepositoryChange = () => {
  const selectedRepo = repositories.find(repo => repo.id === newDatasource.repositoryId);
  if (selectedRepo) {
    selectedRepositoryName.value = selectedRepo.name;
  } else {
    selectedRepositoryName.value = null;
  }
}

const integrationStore = useIntegrationStore()

const projectID = ref(null)
const selectedRepositoryName = ref(null);
const createNewRepository = ref(false); // Variable para determinar si se va a crear un nuevo repositorio


// When the component is mounted, fetch the repositories for the current project.
onMounted(async () => {
  const url = window.location.href; // Get the current URL
  const regex = /project\/(\d+)\//;
  const match = url.match(regex);
  let projectId;
  if (match) {
    projectId = match[1];
    console.log(projectId+"+++++++++++++++++++++++1 id del proyecto cogido"); // Output: 1
    projectID.value = projectId;
    await storeDS.getRepositories(projectID.value)
  }
});

const route = useRoute()
const router = useRouter()
// -------------------------------------------------------------
//                         Others
// -------------------------------------------------------------

const form = ref(null)
const notify = useNotify()

defineExpose({
  form
})

const options = [
  "SQLDatabase", "Upload file"
];

const repositories = [];

const newDatasource = reactive({
  repositoryId: null,
  repositoryName: '',
  datasetName: '',
  datasetDescription: '',
})


const uploadedFile = ref(null);
const DataSourceType = ref("Upload file");
const onReset = () => {
  uploadedFile.value = null;
}

const onSubmit = () => {
  const data = new FormData();
  data.append("attach_file", uploadedFile.value);
  data.append("datasetName", newDatasource.datasetName);
  data.append("datasetDescription", newDatasource.datasetDescription);
  data.append("repositoryName", selectedRepositoryName.value);
  data.append("repositoryId", newDatasource.repositoryId);

  integrationStore.addDataSource(route.params.id, data, successCallback)
}

const successCallback = (datasource) => {

  console.log("success callback")

  notify.positive(`Data Source ${datasource.datasetName} successfully uploaded`)
  onReset()
  form.value.resetValidation()

  showS.value = false;

  integrationStore.addSelectedDatasource(datasource)

  if (props.afterSubmitShowGraph)
    router.push({name: 'dsIntegration'})

}


</script>

<style lang="scss">
.fileBoxLabel {

  margin: 0px;
  padding: 0px;
  border: 0px;
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
