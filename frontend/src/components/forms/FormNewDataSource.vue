<template>
  <q-dialog v-model="showS" @hide="props.show=false">
    <q-card style="width: 400px; max-width: 80vw">
      <q-form ref="form" @submit="onSubmit" @reset="onReset" class="q-gutter-md">

        <!-- Sección 1: Título -->
        <q-card-section>
          <div class="text-h5">Create new dataset</div>
        </q-card-section>

        <!-- Resto del contenido con desplazamiento -->
        <div style="overflow-y: auto; max-height: calc(80vh - 140px);">
          <!-- Sección 2: Información del Conjunto de Datos -->
          <q-card-section>
            <div class="text-h6">Dataset information</div>
            <!-- Tipo de origen de datos -->
            <q-select
              v-model="DataSourceType"
              :options="options"
              label="Type"
              class="q-mt-none"
            />

            <!-- Sección 4: Lista de archivos cargados -->
            <q-card-section v-if="uploadedFiles.length > 0">
              <div class="text-h6">Uploaded Files</div>
              <ul>
                <li v-for="(file, index) in uploadedFiles" :key="index">{{ file.name }}</li>
              </ul>
            </q-card-section>

            <!-- File and Folder Upload Section -->
            <div
              class="uploader__empty-state uploader__empty-state--with-display-name uploader__empty-state--with-directories-selector">
              <svg viewBox="0 0 72 72" role="img" aria-label="Upload files" @click="triggerFileUpload">
                <path
                  d="M36.493 72C16.118 72 0 55.883 0 36.493 0 16.118 16.118 0 36.493 0 55.882 0 72 16.118 72 36.493 72 55.882 55.883 72 36.493 72zM34 34h-9c-.553 0-1 .452-1 1.01v1.98A1 1 0 0 0 25 38h9v9c0 .553.452 1 1.01 1h1.98A1 1 0 0 0 38 47v-9h9c.553 0 1-.452 1-1.01v-1.98A1 1 0 0 0 47 34h-9v-9c0-.553-.452-1-1.01-1h-1.98A1 1 0 0 0 34 25v9z"
                  fill="#5268ff" fill-rule="nonzero"></path>
              </svg>
              <div class="uploader__empty-state-text">
                <h2 @click="triggerFileUpload">Upload files</h2>
                <button @click="triggerFolderUpload" class="uploader__sub-title uploader__directories-dialog-trigger">Or
                  select a folder
                </button>
              </div>
            </div>

            <!-- List of Uploaded Files/Folders -->
            <div class="uploaded-items-list">
              <div v-for="(item, index) in uploadedItems" :key="index" class="uploaded-item">
                <template v-if="item.files === undefined">
                  {{ item.name }}
                </template>
                <template v-else>
                  <div>{{ item.name }}</div>
                  <div>{{ item.files.length }} files</div>
                  <div>Total Size: {{ item.totalSize }} bytes</div>
                </template>
              </div>
            </div>

            <!-- Hidden Inputs for File and Folder Upload -->
            <input type="file" ref="fileUpload" multiple @change="handleFileUpload" style="display: none;">
            <input type="file" ref="folderUpload" webkitdirectory directory @change="handleFolderUpload"
                   style="display: none;">


            <!-- Mostrar selector de archivo si se selecciona "Local file/s" -->
            <!--
            <q-file
              type="file"
              v-if="isLocalFileOptionSelected"
              ref="fileds"

              v-model="uploadedFiles"
              auto-expand
              :label="fileInputLabel"
              :headers="{ 'content-type': 'multipart/form-data' }"
              :accept="fileAccept"
              :max-files="maxFilesValue"
              lazy-rules
              :rules="fileRules"
              @update:modelValue="updateUploadedFiles"
              borderless
              multiple
              append

              clearable
              use-chips

              counter
              :counter-label="counterLabelFunction"
            >
              <template v-slot:prepend>
                <q-icon name="attach_files" @click="this.$refs.fileds.pickFiles();"/>
                <q-icon name="folder" class="interactive-icon" @click="openDirectoryPicker"/>
              </template>
              <template v-slot:label>
                <label class="fileLabel">Upload files</label><br>
                <a href="javascript:void(0)" class="richText" @click.prevent="openDirectoryPicker">Or select a
                  folder</a>
              </template>
            </q-file>
            -->

            <!-- Mostrar campos de conexión a la base de datos si se selecciona "SQL Database" -->
            <q-card-section v-if="isRemoteFileOptionSelected">
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

            <!-- Mostrar campos de conexión a la base de datos si se selecciona "SQL Database" -->
            <q-card-section v-if="!isLocalFileOptionSelected && !isRemoteFileOptionSelected">
              <q-input filled v-model="databaseHost" label="Database Host" lazy-rules
                       :rules="[(val) => !!val || 'Please enter the database host']"/>
              <q-input filled v-model="databaseUser" label="Database User" lazy-rules
                       :rules="[(val) => !!val || 'Please enter the database user']"/>
              <q-input filled v-model="databasePassword" label="Database Password" type="password"/>
              <!-- Agregar más campos según sea necesario para la conexión a la base de datos -->
            </q-card-section>

            <!-- Descripción del conjunto de datos (opcional) -->
            <q-input v-model="newDatasource.datasetDescription" filled autogrow label="Description (Optional)"/>
          </q-card-section>

          <!-- Sección 3: Información del Repositorio -->
          <q-card-section>
            <div class="text-h6">Repository information</div>

            <q-card-section>
              <q-checkbox v-model="createNewRepository" label="Create new repository"
                          :disable="storeDS.repositories.length === 0"/>

              <!-- Mostrar el campo de entrada para el nombre del nuevo repositorio si "Create new repository" está seleccionado -->
              <q-input v-if="createNewRepository" filled v-model="newDatasource.repositoryName"
                       label="Name of the new repository" lazy-rules
                       :rules="[(val) => (val && val.length > 0) || 'Please type a name']"/>
              <q-select
                v-else
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
            </q-card-section>
          </q-card-section>
        </div>


        <!-- Botones del formulario -->
        <q-card-section>
          <div v-if="showFormButtons">
            <q-btn label="Submit" type="submit" color="primary"/>
            <q-btn label="Cancel" type="reset" color="primary" flat class="q-ml-sm" v-close-popup/>
          </div>
        </q-card-section>
      </q-form>
    </q-card>
  </q-dialog>
</template>

<script>
export default {
  data() {
    return {
      uploadedItems: [],
    };
  },
  methods: {
    triggerFileUpload() {
      this.$refs.fileUpload.click();
    },
    triggerFolderUpload() {
      this.$refs.folderUpload.click();
    },
    handleFileUpload(event) {
      const files = Array.from(event.target.files);
      this.uploadedItems.push(...files);
    },
    handleFolderUpload(event) {
      const folder = event.target.files[0].webkitRelativePath.substring(0, event.target.files[0].webkitRelativePath.indexOf('/'));
      const files = Array.from(event.target.files);
      // Handle folder selection
      const folderInfo = {
        type: 'folder',
        name: folder,
        files: files, // Array to store files in the folder
        totalSize: 0, // Total size of files in the folder
      };

      files.forEach((file) => {
        folderInfo.totalSize += file.size;
      });

      this.uploadedItems.push(folderInfo);
    }
  }
}
</script>


<script setup>
import {ref, reactive, onMounted, watch, computed} from "vue";
import {useNotify} from 'src/use/useNotify.js'
import {useRoute, useRouter} from "vue-router";
import {useIntegrationStore} from 'src/stores/integration.store.js'
import {useDataSourceStore} from "../../stores/datasources.store";
import axios from "axios";
import {odinApi} from "../../boot/axios";
// Función para abrir el selector de directorios
const openDirectoryPicker = () => {
  const input = document.createElement('input');
  input.type = 'file';
  input.webkitdirectory = 'webkitdirectory'; // Esto permite seleccionar directorios
  input.addEventListener('change', handleDirectorySelection);
  input.click();
}

const openFilePicker = () => {
  const input = document.createElement('input');
  input.type = 'file';
  input.addEventListener('change', updateUploadedFiles);
  input.click();
}

// Función para manejar la selección de directorios
const handleDirectorySelection = async (event) => {
  const selectedFiles = event.target.files;

  for (let i = 0; i < selectedFiles.length; i++) {
    const file = selectedFiles[i];

    if (file) { // Comprobar si el archivo no es nulo
      if (file.isDirectory) {
        //await processDirectory(file);
      } else {
        if (uploadedFiles.value == null) uploadedFiles.value = ref([]);
        // Es un archivo, agrégalo a la lista
        uploadedFiles.value.push(file);
        // Establece el nombre de la carpeta como el nombre del repositorio
        createNewRepository.value = true;
        newDatasource.repositoryName = event.target.files[0].webkitRelativePath.substring(0, event.target.files[0].webkitRelativePath.indexOf('/'));
      }
    }
  }
};

const remoteFileUrl = ref(""); // Variable para almacenar la URL del archivo remoto

async function downloadFile() {
  let url = remoteFileUrl.value; // Reemplaza con la URL que deseas descargar
  try {
    const response = await odinApi.get(`/download?url=${encodeURIComponent(url)}`, {
      responseType: 'arraybuffer', // Cambia el tipo de respuesta a 'arraybuffer'
    });

    const contentDisposition = response.headers['content-disposition'];
    let filename;

    if (contentDisposition) {
      // Si el encabezado content-disposition existe, obtén el nombre del archivo
      filename = contentDisposition.split(';')[1].trim().split('=')[1];
    } else {
      // Si el encabezado no existe, intenta obtener el nombre del archivo de la URL
      const urlParts = url.split('/');
      filename = urlParts[urlParts.length - 1];
    }

    // Crea un nuevo objeto File a partir de la respuesta
    const blob = new Blob([response.data], {type: 'application/octet-stream'});
    const file = new File([blob], filename, {type: 'application/octet-stream'});

    // Agrega el archivo a la lista uploadedFiles
    uploadedFiles.value.push(file);
  } catch (error) {
    console.error('Error al descargar el archivo:', error);
  }
}

function counterLabelFunction({filesNumber, maxFiles, totalSize}) {
  return `${filesNumber} files of ${totalSize}`
}

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
    console.log(projectId + "+++++++++++++++++++++++1 id del proyecto cogido"); // Output: 1
    projectID.value = projectId;
    await storeDS.getRepositories(projectID.value)
    if (storeDS.repositories.length === 0) createNewRepository.value = true;
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
  "Remote file/s",
  "SQL Database",
];

const newDatasource = reactive({
  repositoryId: ref(null),
  repositoryName: '',
  datasetName: '',
  datasetDescription: '',
});


const uploadedFiles = ref([]);
const DataSourceType = ref(options[0]);
const onReset = () => {// Restablece los valores de los campos a su estado inicial
  newDatasource.repositoryId = null;
  newDatasource.repositoryName = '';
  newDatasource.datasetName = '';
  newDatasource.datasetDescription = '';
  uploadedFiles.value = [];
  DataSourceType.value = options[0];
  databaseHost.value = '';
  databaseUser.value = '';
  databasePassword.value = '';
  remoteFileUrl.value = '';
  createNewRepository.value = false;

  uploadedFiles.value = ref([]);
  DataSourceType.value = options[0];
}

const onSubmit = () => {
  const data = new FormData();
  console.log("Contenido de uploadedFiles:", uploadedFiles.value);


  data.append("datasetName", newDatasource.datasetName);
  data.append("datasetDescription", newDatasource.datasetDescription);
  data.append("repositoryName", newDatasource.repositoryName);
  data.append("repositoryId", newDatasource.repositoryId === null || createNewRepository.value ? '' : newDatasource.repositoryId); // Set as empty string if repositoryId is null

  // Append all files as an array under the key 'attach_files'
  uploadedFiles.value.forEach((file) => {
    console.log("Archivo que se va a agregar:", file);
    data.append('attach_files', file);
  });

  integrationStore.addDataSource(route.params.id, data, successCallback)
  // Limpia la lista de archivos cargados después de enviar el formulario

  onReset();
}

const successCallback = (datasource) => {

  console.log("success callback")

  notify.positive(`Data Source ${datasource.datasetId} successfully uploaded`)
  onReset()
  form.value.resetValidation()

  showS.value = false;

  integrationStore.addSelectedDatasource(datasource)
  storeDS.getDatasources(route.params.id)
}

// Computed property para determinar las reglas para el componente <q-file> -->
const fileRules = computed(() => {
  return [(val) => (val && val.length > 0) || 'Please upload at least one file or folder'];
});

// Agrega este método en tu bloque <script setup>

const autoSelectRepository = () => {
  if (!createNewRepository.value && uploadedFiles.value.length > 0) {
    const fileName = uploadedFiles.value[0].name; // Supongo que solo verificas el primer archivo
    if (storeDS.repositories) { // Verifica si storeDS.repositories está definido
      const matchingRepository = storeDS.repositories.find(repo => repo.repositoryName === fileName);
      if (matchingRepository) {
        createNewRepository.value = false;
        newDatasource.repositoryId = matchingRepository.id;
      } else {
        createNewRepository.value = true;
        newDatasource.repositoryName = fileName;
      }
    }
  }
};

// Agrega un watcher para ejecutar la función autoSelectRepository cada vez que se actualice uploadedFiles
watch(uploadedFiles, () => {
  autoSelectRepository();
});


// Computed property para determinar la etiqueta del componente <q-file> -->
const fileInputLabel = computed(() => {
  return 'Select files or folders to import.';
});

const folderInputLabel = computed(() => {
  return 'Or select a folder';
});

// Computed property to determine the accept attribute for the q-file component based on the selected DataSourceType
const fileAccept = computed(() => {

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

// Agrega un watcher para limpiar uploadedFiles cuando el formulario se muestra
watch(showS, (show) => {
  if (!show) {
    // Si el formulario se cierra, limpia uploadedFiles
    uploadedFiles.value = [];
  }
});

// Add this method to update the uploadedFile value when the q-file component emits the update:modelValue event
const updateUploadedFiles = (value) => {
  uploadedFiles.value = value;

  if (value == null) uploadedFiles.value = [];
}

const databaseHost = ref('');
const databaseUser = ref('');
const databasePassword = ref('');

// Computed property to determine if "Local file/s" is selected
const isLocalFileOptionSelected = computed(() => DataSourceType.value === options[0]);

const isRemoteFileOptionSelected = computed(() => DataSourceType.value === options[1]);

</script>


<style lang="scss" scoped>
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

/* Styles for the Upload Section */
.uploader__empty-state {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.uploader__empty-state svg {
  width: 50px;
  height: 50px;
  margin-right: 20px;
}

.uploader__empty-state-text {
  display: flex;
  flex-direction: column;
  align-items: start;
}

.uploader__empty-state-text h2 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: bold; /* Agregar negrita (bold) */
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
}

.uploader__empty-state-text button:hover {
  color: blue; /* Cambia el color al pasar el cursor */
  //text-decoration: none; /* Elimina el subrayado al pasar el cursor */
}

/* Styles for the List of Uploaded Items */
.uploaded-items-list {
  margin-top: 20px;
}

.uploaded-item {
  border: 1px solid #ccc;
  padding: 10px;
  margin-bottom: 10px;
}
</style>
