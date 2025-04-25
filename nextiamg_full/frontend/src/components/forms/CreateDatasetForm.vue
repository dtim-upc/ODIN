<template>
  <q-dialog v-model="showS" @hide="props.show=false" :key="showS">

    <q-card  style="width: 400px; max-width: 80vw">
      <q-card-section>
        <!-- Resto del contenido con desplazamiento -->
        <div style="overflow-y: auto; max-height: calc(80vh - 140px);">
          <!-- Sección 1: Título form -->
          <div class="text-h4">Create new dataset</div>
          <div class="text-h5">Parent Repository: {{ repositoriesStore.selectedRepository.repositoryName }}</div>
          <div class="text-h6">Repository Type: {{ repositoriesStore.selectedRepository.repositoryType }}</div>

          <!-- Sección 2: Información del Conjunto de Datos -->
          <q-card-section v-if="uploadedItems.length > 0">
            <div class="text-h6">Datasets information</div>

            <!-- Sección 3: Lista de archivos cargados -->
            <!-- List of Uploaded Files/Folders -->
            <div class="uploaded-items-list">
              <div v-for="(item, index) in uploadedItems" :key="index" class="uploaded-item">
                <div class="special-button">
                  <q-btn @click="removeUploadedItem(index)" flat round>
                    <q-icon name="close" size="1.25em"/>
                  </q-btn>
                </div>

                <template
                  v-if="item.files === undefined && item.name !== undefined && item.otherInfo === undefined && isLocalRepository || isAPIRepository">
                  <div v-if="item.name !== undefined">{{ item.name }}</div>
                  <div class="file-system-entry__details">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="13" viewBox="0 0 24 24">
                      <path
                        d="M13.744 8s1.522-8-3.335-8h-8.409v24h20v-13c0-3.419-5.247-3.745-8.256-3zm4.256 11h-12v-1h12v1zm0-3h-12v-1h12v1zm0-3h-12v-1h12v1zm-3.432-12.925c2.202 1.174 5.938 4.883 7.432 6.881-1.286-.9-4.044-1.657-6.091-1.179.222-1.468-.185-4.534-1.341-5.702z"
                        fill="#6a6d70">
                      </path>
                    </svg>
                    <span class="file-system-entry__detail">
                      {{ formatFileSize(item.size) }} ·
                    </span>
                    <span class="file-system-entry__detail">
                      {{ item.type }}
                    </span>
                  </div>
                </template>

                <template v-if="item.files !== undefined && isLocalRepository">
                  <div>{{ item.name }}</div>
                  <div class="file-system-entry__details">
                    <span class="file-system-entry__detail">
                      <svg viewBox="0 0 9 9" width="10" height="10" xmlns="http://www.w3.org/2000/svg">
                        <path
                          d="M0 6.14285714V.85714286C0 .38375593.38375593 0 .85714286 0h2.26447876c1.33783784 0 .74324324 1.23673511 2.08108108 1.23673511h2.94015444C8.61624407 1.23673511 9 1.62049104 9 2.09387797v4.04897917C9 6.61624407 8.61624407 7 8.14285714 7H.85714286C.38375593 7 0 6.61624407 0 6.14285714z"
                          fill="#6a6d70">
                        </path>
                      </svg>
                      <span class="directory__type-detail">
                        Folder ·
                      </span>
                    </span>
                    <span class="file-system-entry__detail">{{ item.files.length }} elements</span>
                  </div>
                </template>

                <template v-else-if="isJDBCRepository">
                  <div>{{ item.name }}</div>
                  <div class="file-system-entry__details">
                    <span class="file-system-entry__detail">
                      <svg xmlns="http://www.w3.org/2000/svg" width="10" height="10" viewBox="0 0 24 24">
                        <path
                          d="M22 18.055v2.458c0 1.925-4.655 3.487-10 3.487-5.344 0-10-1.562-10-3.487v-2.458c2.418 1.738 7.005 2.256 10 2.256 3.006 0 7.588-.523 10-2.256zm-10-3.409c-3.006 0-7.588-.523-10-2.256v2.434c0 1.926 4.656 3.487 10 3.487 5.345 0 10-1.562 10-3.487v-2.434c-2.418 1.738-7.005 2.256-10 2.256zm0-14.646c-5.344 0-10 1.562-10 3.488s4.656 3.487 10 3.487c5.345 0 10-1.562 10-3.487 0-1.926-4.655-3.488-10-3.488zm0 8.975c-3.006 0-7.588-.523-10-2.256v2.44c0 1.926 4.656 3.487 10 3.487 5.345 0 10-1.562 10-3.487v-2.44c-2.418 1.738-7.005 2.256-10 2.256z"
                          fill="#6a6d70">
                        </path>
                      </svg>
                      <span class="directory__type-detail">
                        SQL table ·
                      </span>
                    </span>
                    <span class="file-system-entry__detail">{{ item.otherInfo }} lines · </span>
                    <span class="file-system-entry__detail">({{ item.size }}) </span>
                  </div>
                </template>
              </div>
            </div>
          </q-card-section>

          <!-- File and Folder Upload Section -->
          <q-card-section v-if="isLocalRepository && !isRemoteFileOptionSelected">
            <div
              class="hoverDiv uploader__empty-state uploader__empty-state--with-display-name uploader__empty-state--with-directories-selector">
              <svg viewBox="0 0 72 72" role="img" aria-label="Upload files" @click="triggerFileUpload">
                <path
                  d="M36.493 72C16.118 72 0 55.883 0 36.493 0 16.118 16.118 0 36.493 0 55.882 0 72 16.118 72 36.493 72 55.882 55.883 72 36.493 72zM34 34h-9c-.553 0-1 .452-1 1.01v1.98A1 1 0 0 0 25 38h9v9c0 .553.452 1 1.01 1h1.98A1 1 0 0 0 38 47v-9h9c.553 0 1-.452 1-1.01v-1.98A1 1 0 0 0 47 34h-9v-9c0-.553-.452-1-1.01-1h-1.98A1 1 0 0 0 34 25v9z"
                  fill="#5268ff" fill-rule="nonzero"></path>
              </svg>
              <div class="uploader__empty-state-text">
                <h2 @click="triggerFileUpload">Upload files</h2>
                <button @click="triggerFolderUpload" class="uploader__sub-title uploader__directories-dialog-trigger">
                  Or select a folder
                </button>
              </div>
            </div>

            <!-- Hidden Inputs for File and Folder Upload -->
            <input type="file" ref="fileUpload" @change="handleFileUpload" style="display: none;" multiple>
            <input type="file" ref="folderUpload" directory @change="handleFolderUpload"
                   style="display: none;">
          </q-card-section>

          <!-- Mostrar campos de input URL si se selecciona "Remote file" -->
          <q-card-section v-if="isLocalRepository && isRemoteFileOptionSelected">
            <!-- Mostrar campo de entrada para la URL del archivo remoto si "Remote file/s" está seleccionado -->
            <q-input
              v-if="isRemoteFileOptionSelected"
              filled
              v-model="remoteFileUrl"
              label="Remote File URL"
              lazy-rules
              :rules="[(val) => (val && val.length > 0) || 'Please enter a URL']"
            />
            <q-btn
              v-if="isRemoteFileOptionSelected"
              label="Download Remote File"
              color="primary"
              @click="downloadFile"
            />
          </q-card-section>

          <q-card-section v-if="isAPIRepository">
            <q-badge color="secondary" multi-line>
              GET
              {{ }}
            </q-badge>
            <q-input
              filled
              v-model="remoteFileUrl"
              label="API request endpoint"
              lazy-rules
              :rules="[(val) => (val && val.length > 0) || 'Please enter a URL']"
            />
            <q-btn
              label="Make request"
              color="primary"
              @click="makeRequest"
              class="q-mb-sm"
            />
            <q-input
              filled
              v-model="apiDatasetName"
              label="Dataset name"
              lazy-rules
              :rules="[(val) => (val && val.length > 0) || 'Please enter a URL']"
            />
          </q-card-section>

          <!-- Tipo de origen de datos -->
          <q-card-section v-if="isLocalRepository">
            <!-- Tipo de origen de datos -->
            <q-select
              v-model="DatasetType"
              :options="options"
              label="Type"
              class="q-mt-none"
            />
          </q-card-section>

          <!-- Descripción del conjunto de datos (opcional) -->
          <q-card-section>
            <!-- Descripción del conjunto de datos (opcional) -->
            <q-input v-model="newDataset.datasetDescription" filled autogrow label="Description (Optional)"/>
          </q-card-section>
        </div>
      </q-card-section>

      <q-form ref="form" @submit="onSubmit" @reset="onReset" class="q-gutter-md">
        <!-- Botones del formulario -->
        <q-card-section>
          <div>
            <q-btn label="Submit" type="submit" color="primary"/>
            <q-btn label="Cancel" type="reset" color="primary" flat class="q-ml-sm" v-close-popup/>
          </div>
        </q-card-section>
      </q-form>
    </q-card>
  </q-dialog>
</template>

<script setup>
import {ref, reactive, onMounted, watch, computed} from "vue";
import {useNotify} from 'src/use/useNotify.js'
import {useDatasetsStore} from "../../stores/datasetsStore";
import {useRepositoriesStore} from "src/stores/repositoriesStore.js";
import { useProjectsStore } from "src/stores/projectsStore";
import { useQuasar } from "quasar";

const datasetsStore = useDatasetsStore();
const repositoriesStore = useRepositoriesStore()
const projectID = useProjectsStore().currentProject.projectId

const remoteFileUrl = ref(""); 
const apiDatasetName = ref(""); 
const isLocalRepository = ref(false);
const isJDBCRepository = ref(false);
const isAPIRepository = ref(false);

const $q = useQuasar()
const notify = useNotify()

const form = ref(null)
const options = ["Local file/s", "Remote file/s"];

const props = defineProps({
  show: {type: Boolean, default: false, required: true},
  afterSubmitShowGraph: {type: Boolean, default: true},
  repositoryId: String,
  repositoryName: String,
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

async function downloadFile() {
  let url = remoteFileUrl.value;
  const file = await datasetsStore.downloadFile(url);
  uploadedItems.value.push(file)
}

async function makeRequest() {
  let endpoint = remoteFileUrl.value; // Reemplaza con la URL que deseas descargar

  newDataset.endpoint = endpoint;
  newDataset.apiDatasetName = apiDatasetName

  const file = await datasetsStore.makeAPIRequest(repositoriesStore.selectedRepository.url + endpoint)
  uploadedItems.value.push(file)
  apiDatasetName.value = file.name.substring(0, file.name.indexOf('.'))
}

async function initializeComponent() {
  await repositoriesStore.getRepositories(projectID)
    
  isLocalRepository.value = repositoriesStore.selectedRepository.repositoryType === "LocalRepository";
  isJDBCRepository.value = repositoriesStore.selectedRepository.repositoryType === "RelationalJDBCRepository";
  isAPIRepository.value = repositoriesStore.selectedRepository.repositoryType === "APIRepository";

  if (isJDBCRepository.value) {
    $q.loading.show({message: 'Retrieving tables...'})
    uploadedItems.value = await repositoriesStore.retrieveDBTables(projectID, repositoriesStore.selectedRepository.id);
    $q.loading.hide()
  }
}

watch(() => showS.value, (newValue) => {
  if (newValue) {
    initializeComponent();
  }
});

onMounted(() => {
  initializeComponent();
});

defineExpose({
  form
})

const newDataset = reactive({
  repositoryId: repositoriesStore.selectedRepository.id,
  repositoryName: repositoriesStore.selectedRepository.repositoryName,
  datasetDescription: '',
});

const uploadedItems = ref([]);
const DatasetType = ref(options[0]);
const onReset = () => {// Restablece los valores de los campos a su estado inicial
  newDataset.repositoryId = null;
  repositoriesStore.selectedRepository = {};
  newDataset.repositoryName = '';
  newDataset.datasetDescription = '';
  newDataset.endpoint = '';
  newDataset.apiDatasetName = '';
  DatasetType.value = options[0];
  databaseHost.value = '';
  databaseUser.value = '';
  databasePassword.value = '';
  remoteFileUrl.value = '';

  uploadedItems.value = []; // Vacía la lista de archivos cargados

  DatasetType.value = options[0];
}

const onSubmit = async() => {
  if (uploadedItems.value.length === 0) { // If there are no files we ntify it as an error
    notify.negative('Please add at least one file before submitting.');
    return;
  }
  
  $q.loading.show({message: 'Creating dataset...'})
  const data = new FormData()

  data.append("datasetDescription", newDataset.datasetDescription);
  data.append("repositoryName", newDataset.repositoryName);
  data.append("repositoryId", repositoriesStore.selectedRepository.id); // Set as empty string if repositoryId is null

  const attachTables = [];

  // Append all files as an array under the key 'attach_files'
  uploadedItems.value.forEach((item) => {
    if (isLocalRepository.value) {
      if (item.files === undefined) { // Only one file
        data.append('attachFiles', item)
      }
      else { // Multiple files
        item.files.forEach((file) => {
          data.append('attachFiles', file)
        })
      }
    }
    else if (isJDBCRepository.value) {
      attachTables.push(item.name.toString())
    }
    else if (isAPIRepository.value) {
      data.append('attachFiles', item);
      data.append('endpoint', newDataset.endpoint)
      data.append('apiDatasetName', newDataset.apiDatasetName)
    }
  })

  data.append('attachTables', attachTables);
  
  const successCallback = () => {
    onReset()
    showS.value = false;
  }

  await datasetsStore.postDataset(projectID, data, successCallback);
  $q.loading.hide()
}


// Open file explorer
const triggerFileUpload = () => {
  const fileUploadInput = document.createElement('input');
  fileUploadInput.type = 'file';
  fileUploadInput.accept = '.csv, .json, .xml'; // Specify accepted file extensions
  fileUploadInput.multiple = true; // Enable multiple file selection
  fileUploadInput.addEventListener('change', handleFileUpload);
  fileUploadInput.click();
};

// Open file explorer (only for directories)
const triggerFolderUpload = () => {
  const folderUploadInput = document.createElement('input');
  folderUploadInput.type = 'file';
  folderUploadInput.webkitdirectory = 'webkitdirectory'; // Permite seleccionar directorios
  folderUploadInput.addEventListener('change', handleFolderUpload);
  folderUploadInput.click();
};

// Método para manejar la carga de archivos
const handleFileUpload = (event) => {
  const files = Array.from(event.target.files);
  uploadedItems.value.push(...files);
};

// Método para manejar la carga de carpetas
const handleFolderUpload = (event) => {
  const folder = event.target.files[0].webkitRelativePath.substring(0, event.target.files[0].webkitRelativePath.indexOf('/'));
  const files = Array.from(event.target.files);

  // Manejar la selección de carpeta
  const folderInfo = {
    type: 'folder',
    name: folder,
    files: files, // Array para almacenar archivos en la carpeta
    totalSize: 0, // Tamaño total de los archivos en la carpeta
  };

  files.forEach((file) => {
    folderInfo.totalSize += file.size;
  });

  uploadedItems.value.push(folderInfo);
};

// Función para dar formato al tamaño del archivo con unidades
const formatFileSize = (size) => {
  if (size < 1024) {
    return `${size} Bytes`;
  } else if (size < 1024 * 1024) {
    return `${Math.round(size / 1024)} KB`;
  } else {
    return `${Math.round(size / (1024 * 1024))} MB`;
  }
};

const removeUploadedItem = (index) => {
  uploadedItems.value.splice(index, 1); // Elimina el elemento en el índice dado
}

const maxFilesValue = ref(undefined);

// Watcher to update maxFilesValue whenever the DatasetType changes
watch(() => DatasetType.value, () => {
  if (DatasetType.value === 'Single file') {
    maxFilesValue.value = 1;
  } else if (DatasetType.value === 'Local file/s' || DatasetType.value === 'Directory') {
    maxFilesValue.value = undefined; // Allow selecting any number of files
  } else {
    maxFilesValue.value = 1; // For other DatasetType values, allow only one file to be uploaded
  }
});

const databaseHost = ref('');
const databaseUser = ref('');
const databasePassword = ref('');

// Computed property to determine if "Local file/s" is selected
const isLocalFileOptionSelected = computed(() => DatasetType.value === options[0]);
const isRemoteFileOptionSelected = computed(() => DatasetType.value === options[1]);

</script>


<style lang="scss" scoped>
.uploader__empty-state {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.uploader__empty-state svg {
  width: 50px;
  height: 50px;
  margin-top: 15px;
  margin-right: 15px;
}

.uploader__empty-state-text {
  display: flex;
  flex-direction: column;
  align-items: start;
}

.uploader__empty-state-text h2 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 400; /* Agregar negrita (bold) */
}

.uploader__empty-state-text button {
  background: none;
  border: none;
  font-size: 1rem;
  color: grey;
  cursor: pointer;
  padding: 0;
  text-decoration: underline; /* Agregar subrayado */
  transition: color 0.3s ease, text-decoration 0.3s ease; /* Transiciones de estilo */
  margin-top: -15px; /* Ajusta el espacio vertical entre button y h2 */
}

.uploader__empty-state-text button:hover {
  color: blue; /* Cambia el color al pasar el cursor */
  //text-decoration: none; /* Elimina el subrayado al pasar el cursor */
}

.hoverDiv h2:hover {
  color: blue; /* Cambia el color al pasar el cursor */
}

/* Styles for the List of Uploaded Items */
.uploaded-items-list {
  margin-top: 20px;
}

.uploaded-item {
  border: 1px solid #ccc;
  padding: 10px;
  margin-bottom: 10px;
  position: relative;
}

.special-button {
  position: absolute;
  top: 18%; /* Coloca el botón en el centro vertical */
  left: 85%; /* Coloca el botón a la derecha del contenido */
  color: red;
  cursor: pointer;
}
</style>
