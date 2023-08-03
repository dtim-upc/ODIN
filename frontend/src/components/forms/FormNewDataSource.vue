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


          <q-input filled v-model="newDatasource.datasetName" label="Introduce a dataset name" lazy-rules
                   :rules="[(val) => (val && val.length > 0) || 'Please type a name', ]"/>

          <q-input v-model="newDatasource.datasetDescription" filled autogrow label="Description (Optional)"/>

          <q-select
            v-model="DataSourceType"
            :options="options"
            label="Type"
            class="q-mt-none"
          />

          <!-- Show file selector if "Local file/s" is selected -->
          <q-file
            v-if="isLocalFileOptionSelected"
            ref="fileds"
            outlined
            v-model="uploadedFiles"
            auto-expand
            :label="fileInputLabel"
            :headers="{ 'content-type': 'multipart/form-data' }"
            :accept="fileAccept"
            :max-files="maxFilesValue"
            lazy-rules
            :rules="fileRules"
            @update:modelValue="updateUploadedFiles"
            multiple
          >
            <template v-slot:prepend>
              <q-icon name="attach_files" @click="this.$refs.fileds.pickFiles();"/>
            </template>
          </q-file>

          <!-- Show database connection fields if "SQL Database" is selected -->
          <div v-else>
            <q-input filled v-model="databaseHost" label="Database Host" lazy-rules :rules="[(val) => !!val || 'Please enter the database host']" />
            <q-input filled v-model="databaseUser" label="Database User" lazy-rules :rules="[(val) => !!val || 'Please enter the database user']" />
            <q-input filled v-model="databasePassword" label="Database Password" type="password" />
            <!-- Add more fields as needed for database connection -->
          </div>



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
  if (createNewRepository.value) {
    // User selected "Create new repository"
    newDatasource.repositoryId = null;
  } else {
    // User selected an existing repository
    const selectedRepo = storeDS.repositories.find(repo => repo.id === newDatasource.repositoryId);
    if (selectedRepo) {
      newDatasource.repositoryId = selectedRepo.id;
    } else {
      newDatasource.repositoryId = null; // Handle the case when the selected repository is not found
    }
  }
}

const integrationStore = useIntegrationStore()

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
    console.log(projectId+"+++++++++++++++++++++++1 id del proyecto cogido"); // Output: 1
    projectID.value = projectId;
    await storeDS.getRepositories(projectID.value)
    if(storeDS.repositories.length===0)createNewRepository.value=true;
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
  "Local file/s",
  "SQL Database",
];

const newDatasource = reactive({
  repositoryId: ref(null),
  repositoryName: '',
  datasetName: '',
  datasetDescription: '',
})


const uploadedFiles = ref([]);
const DataSourceType = ref(options[0]);
const onReset = () => {
  uploadedFiles.value = null;
}

const onSubmit = () => {
  const data = new FormData();

  data.append("datasetName", newDatasource.datasetName);
  data.append("datasetDescription", newDatasource.datasetDescription);
  data.append("repositoryName", newDatasource.repositoryName);
  data.append("repositoryId", newDatasource.repositoryId === null || createNewRepository.value ? '' : newDatasource.repositoryId); // Set as empty string if repositoryId is null

  // Append all files as an array under the key 'attach_files'
  uploadedFiles.value.forEach((file) => {
    data.append('attach_files', file);
  });

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


// Computed property to determine the rules for the q-file component based on the selected DataSourceType
const fileRules = computed(() => {
  if (DataSourceType.value === 'Single file') {
    return [(val) => (val && val.name !== '') || 'Please upload a file'];
  } else if (DataSourceType.value === 'Local file/s') {
    return [(val) => val && val.length > 0 || 'Please upload at least one file'];
  } else if (DataSourceType.value === 'Directory') {
    return [(val) => (val && val.length > 0) || 'Please select a directory'];
  } else {
    // For other DataSourceType values, no specific rules are required
    return [];
  }
});

// Computed property to determine the label for the q-file component based on the selected DataSourceType
const fileInputLabel = computed(() => {
  if (DataSourceType.value === 'Local file/s') {
    return 'Select one or more files to import.';
  } else {
    // For other DataSourceType values, no specific label is required
    return 'Select files to import.';
  }
});

// Computed property to determine the accept attribute for the q-file component based on the selected DataSourceType
const fileAccept = computed(() => {
  if (DataSourceType.value === 'Local file/s') {
    return '.csv, application/json';
  } else {
    // For other DataSourceType values, any file type is accepted
    return '';
  }
});

const maxFilesValue = ref(undefined);

// Watcher to update maxFilesValue whenever the DataSourceType changes
watch(() => DataSourceType.value, () => {
  if (DataSourceType.value === 'Single file') {
    maxFilesValue.value = 1;
  } else if (DataSourceType.value === 'Local file/s' || DataSourceType.value === 'Directory') {
    maxFilesValue.value = undefined; // Allow selecting any number of files
  } else {
    maxFilesValue.value = 1; // For other DataSourceType values, allow only one file to be uploaded
  }
});


// Add this method to update the uploadedFile value when the q-file component emits the update:modelValue event
const updateUploadedFiles = (value) => {
  uploadedFiles.value = value;
}

const databaseHost = ref('');
const databaseUser = ref('');
const databasePassword = ref('');

// Computed property to determine if "Local file/s" is selected
const isLocalFileOptionSelected = computed(() => DataSourceType.value === 'Local file/s');

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
