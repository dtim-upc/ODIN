<template>
  <q-dialog v-model="showS" @hide="props.show=false">
    <q-card style="width: 400px; max-width: 80vw">

        <q-card-section>
        <!-- Resto del contenido con desplazamiento -->
        <div style="overflow-y: auto; max-height: calc(80vh - 140px);">
          <!-- Sección 1: Título form -->
          <div class="text-h5">Create new dataset</div>

          <!-- Sección 2: Información del Conjunto de Datos -->
          <q-card-section v-if="uploadedItems.length > 0">
            <div class="text-h6">Datasets information</div>

            <!-- Sección 3: Lista de archivos cargados -->
            <!-- List of Uploaded Files/Folders -->
            <div class="uploaded-items-list">
              <div v-for="(item, index) in uploadedItems" :key="index" class="uploaded-item">
                <template v-if="item.files === undefined">
                  <div>{{ item.name }}</div>
                  <div class="file-system-entry__details">
                    <span class="file-system-entry__detail">
                      {{ item.size }} ·
                    </span>
                    <span class="file-system-entry__detail">
                      {{ item.type }}
                    </span>
                  </div>
                </template>

                <template v-else>
                  <div>{{ item.name }}</div>
                  <div class="file-system-entry__details">
                    <span class="file-system-entry__detail">
                      <svg viewBox="0 0 9 7" width="9" height="7" xmlns="http://www.w3.org/2000/svg">
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
              </div>
            </div>
          </q-card-section>

          <!-- File and Folder Upload Section -->
          <q-card-section v-if="isLocalFileOptionSelected">
            <div
              class="uploader__empty-state uploader__empty-state--with-display-name uploader__empty-state--with-directories-selector">
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

            <!-- Mostrar selector de archivo si se selecciona "Local file/s" -->
            <!-- file picker comentado para replicar entrada de wetransfer
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
          </q-card-section>

          <!-- Mostrar campos de input URL si se selecciona "Remote file" -->
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

          <!-- Tipo de origen de datos -->
          <q-card-section>
            <!-- Tipo de origen de datos -->
            <q-select
              v-model="DataSourceType"
              :options="options"
              label="Type"
              class="q-mt-none"
            />
          </q-card-section>

          <!-- Descripción del conjunto de datos (opcional) -->
          <q-card-section>
            <!-- Descripción del conjunto de datos (opcional) -->
            <q-input v-model="newDatasource.datasetDescription" filled autogrow label="Description (Optional)"/>
          </q-card-section>

          <!-- Sección 4: Información del Repositorio -->
          <q-card-section>
            <!-- <div class="text-h6">Repository information</div> -->
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
        </div>
        </q-card-section>

      <q-form ref="form" @submit="onSubmit" @reset="onReset" class="q-gutter-md">
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

<script setup>
import {ref, reactive, onMounted, watch, computed} from "vue";
import {useNotify} from 'src/use/useNotify.js'
import {useRoute, useRouter} from "vue-router";
import {useIntegrationStore} from 'src/stores/integration.store.js'
import {useDataSourceStore} from "../../stores/datasources.store";
import {odinApi} from "../../boot/axios";


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

    // Agrega el archivo a la lista uploadedItems
    uploadedItems.value.push(file);
  } catch (error) {
    console.error('Error al descargar el archivo:', error);
  }
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

const uploadedItems = ref([]);
const DataSourceType = ref(options[0]);
const onReset = () => {// Restablece los valores de los campos a su estado inicial
  newDatasource.repositoryId = null;
  newDatasource.repositoryName = '';
  newDatasource.datasetName = '';
  newDatasource.datasetDescription = '';
  DataSourceType.value = options[0];
  databaseHost.value = '';
  databaseUser.value = '';
  databasePassword.value = '';
  remoteFileUrl.value = '';
  createNewRepository.value = false;

  uploadedItems.value = []; // Vacía la lista de archivos cargados

  DataSourceType.value = options[0];
}

const onSubmit = () => {
  const data = new FormData();
  console.log("Contenido de uploadedItems:", uploadedItems.value);


  data.append("datasetName", newDatasource.datasetName);
  data.append("datasetDescription", newDatasource.datasetDescription);
  data.append("repositoryName", newDatasource.repositoryName);
  data.append("repositoryId", newDatasource.repositoryId === null || createNewRepository.value ? '' : newDatasource.repositoryId); // Set as empty string if repositoryId is null

  // Append all files as an array under the key 'attach_files'
  uploadedItems.value.forEach((item) => {
    console.log("Archivo que se va a agregar:", item);

    //si item.files === undefined es un fichero individual
    if(item.files === undefined) data.append('attach_files', item);

    //si item.files !== undefined es una carpeta, accedemos a item.files
    else{
      item.files.forEach((file) => {
        data.append('attach_files', file);
      });
    }
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

// Método para abrir el selector de archivos
const triggerFileUpload = () => {
  const fileUploadInput = document.createElement('input');
  fileUploadInput.type = 'file';
  fileUploadInput.accept = '.csv, .json'; // Specify accepted file extensions
  fileUploadInput.multiple = true; // Enable multiple file selection
  fileUploadInput.addEventListener('change', handleFileUpload);
  fileUploadInput.click();
};

// Método para abrir el selector de carpetas
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
  autoSelectRepository();
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
  autoSelectRepository();
};

const autoSelectRepository = () => {
  if (uploadedItems.value.length > 0) {
    const fileName = ref("");

    if (uploadedItems.value[0] && uploadedItems.value[0].files !== undefined) {
      fileName.value = uploadedItems.value[0].files[0].webkitRelativePath.substring(0, uploadedItems.value[0].files[0].webkitRelativePath.indexOf('/'));
    } else {
      fileName.value = uploadedItems.value[0].name;
      //fileName.value = uploadedItems.value[0].webkitRelativePath.substring(0, uploadedItems.value[0].webkitRelativePath.indexOf('/'))
      console.log(uploadedItems.value[0])
    }


    if (storeDS.repositories) { // Verifica si storeDS.repositories está definido
      const matchingRepository = storeDS.repositories.find(repo => repo.repositoryName === fileName.value);
      if (matchingRepository) {
        createNewRepository.value = false;
        newDatasource.repositoryId = matchingRepository.id;
      } else {
        createNewRepository.value = true;
        newDatasource.repositoryName = fileName.value;
      }
    }
  }
};

// Agrega un watcher para ejecutar la función autoSelectRepository cada vez que se actualice uploadedItems
watch(uploadedItems, () => {
  autoSelectRepository();
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
  margin-top: 5px;
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
