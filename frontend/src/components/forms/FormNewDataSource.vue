<template>
  <q-dialog v-model="showS" @hide="props.show=false" :key="showS">
    <q-spinner-pie v-if="loading" color="light-blue" size="5em" align="center"/>

    <q-card v-else style="width: 400px; max-width: 80vw">
      <q-card-section>
        <!-- Resto del contenido con desplazamiento -->
        <div style="overflow-y: auto; max-height: calc(80vh - 140px);">
          <!-- Sección 1: Título form -->
          <div class="text-h4">Create new dataset</div>
          <div class="text-h5">Parent Repository: {{ storeDS.selectedRepositoryName }}</div>
          <div class="text-h6">Repository Type: {{ storeDS.selectedRepositoryType }}</div>

          <!-- Sección 2: Información del Conjunto de Datos -->
          <q-card-section v-if="uploadedItems.length > 0">
            <div class="text-h6">Datasets information</div>

            <!-- Sección 3: Lista de archivos cargados -->
            <!-- List of Uploaded Files/Folders -->
            <div class="uploaded-items-list">
              <div v-for="(item, index) in uploadedItems" :key="index" class="uploaded-item"
                   @mouseover="showSpecialButton(index)" @mouseleave="hideSpecialButton(index)">

                <div class="special-button special-button-hidden">
                  <q-button @click="removeUploadedItem(index)" flat round>
                    <q-icon name="close" size="1.25em"/>
                  </q-button>
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
            <!-- Mostrar campo de entrada para la URL del archivo remoto si "Remote file/s" está seleccionado -->
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
            />
          </q-card-section>

          <!-- Tipo de origen de datos -->
          <q-card-section v-if="isLocalRepository">
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
import {ref, reactive, onMounted, watch, computed, onBeforeMount} from "vue";
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

async function makeRequest() {
  let endpoint = remoteFileUrl.value; // Reemplaza con la URL que deseas descargar

  let id_buscar = storeDS.getSelectedRepositoryId; // Define el ID que deseas buscar

  let repositorio_encontrado = null; // Inicializa con null, no con None

  // Supongamos que storeDS.repositories es una lista de objetos con propiedades "id" y "url"
  for (const repo of storeDS.repositories) {
    if (repo.id === id_buscar) {
      repositorio_encontrado = repo;
      break;
    }
  }

  if (repositorio_encontrado) {
    notify.positive("REQUEST MADE: " + repositorio_encontrado.url + endpoint);

    try {
      const response = await odinApi.get(`/makeRequest?url=${encodeURIComponent(repositorio_encontrado.url + endpoint)}`, {
        responseType: 'arraybuffer',
      });

      const contentDisposition = response.headers['content-disposition'];
      let filename;

      if (contentDisposition) {
        const match = contentDisposition.match(/filename="(.+)"/);
        if (match) {
          filename = match[1];
        }
      } else {
        // Si no se encuentra Content-Disposition, intenta obtener el nombre del archivo del URL
        const urlParts = repositorio_encontrado.url.split('/');
        filename = urlParts[urlParts.length - 1] + ".json";
      }

      // Crea un nuevo objeto File a partir de la respuesta
      const blob = new Blob([response.data], {type: 'application/json'});
      const file = new File([blob], filename, {type: 'application/json'});

      uploadedItems.value.push(file);
    } catch (error) {
      console.error('Error al descargar el archivo:', error);
    }

  } else {
    notify.negative("REPOSITORY NOT FOUND");
  }
}

// -------------------------------------------------------------
//                         PROPS & EMITS
// -------------------------------------------------------------
const props = defineProps({
  show: {type: Boolean, default: false, required: true},
  showFormButtons: {type: Boolean, default: true},
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

const storeDS = useDataSourceStore();

// -------------------------------------------------------------
//                         STORES & GLOBALS
// -------------------------------------------------------------
const showSpecialButton = (index) => {
  const specialButton = document.querySelectorAll('.special-button')[index];
  specialButton.classList.remove('special-button-hidden');
}

const hideSpecialButton = (index) => {
  const specialButton = document.querySelectorAll('.special-button')[index];
  specialButton.classList.add('special-button-hidden');
}


const integrationStore = useIntegrationStore()

const projectID = ref(null);
const isLocalRepository = ref(false);
const isJDBCRepository = ref(false);
const isAPIRepository = ref(false);

async function initializeComponent() {
  const url = window.location.href; // Get the current URL
  const regex = /project\/(\d+)\//;
  const match = url.match(regex);
  let projectId;
  if (match) {
    projectId = match[1];
    console.log(projectId + "+++++++++++++++++++++++1 id del proyecto cogido"); // Output: 1
    projectID.value = projectId;
    await storeDS.getRepositories(projectID.value);

    console.log("LLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL");

    //qué tipo de repositorio es?
    //storeDS.repositories.some(repository => repository.id === storeDS.selectedRepositoryId) ? console.log(storeDS.repositories.find(repository => repository.id === storeDS.selectedRepositoryId)) : "NADA";

    isLocalRepository.value = storeDS.selectedRepositoryType === "LocalRepository";
    isJDBCRepository.value = storeDS.selectedRepositoryType === "RelationalJDBCRepository";
    isAPIRepository.value = storeDS.selectedRepositoryType === "ApiRepository";
    console.log(isLocalRepository);

    if (isJDBCRepository.value) {
      console.log("no es local repository");

      //call backend end point for retrieving db information and adding the response to uploadedItems
      try {
        const repositoryId = storeDS.selectedRepositoryId;

        // Realiza la solicitud GET al punto final del backend con el repositoryId como parámetro
        const response = await odinApi.get(`/` + repositoryId + `/tables`);

        // Verifica si la solicitud se realizó con éxito
        if (response.status === 200) {
          const tablesData = response.data; // Esto debería contener la información de las tablas

          for (const table of tablesData) {
            uploadedItems.value.push(table); // Agregar cada objeto individual a uploadedItems
          }

          console.log(tablesData);
        } else {
          // Maneja el caso en el que la solicitud no se realizó con éxito (por ejemplo, un código de estado no 200)
          console.error('Error en la solicitud al obtener información de tablas');
        }
      } catch (error) {
        // Maneja cualquier error que pueda ocurrir durante la solicitud
        console.error('Error al obtener información de tablas:', error);
      }
    }

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

const route = useRoute()
const router = useRouter()
// -------------------------------------------------------------
//                         Others
// -------------------------------------------------------------

const form = ref(null)
const notify = useNotify()
const loading = ref(false); // Variable para controlar la carga

defineExpose({
  form
})

const options = [
  "Local file/s",
  "Remote file/s",
];

const newDatasource = reactive({
  repositoryId: storeDS.selectedRepositoryId,
  repositoryName: storeDS.selectedRepositoryName,
  datasetName: '',
  datasetDescription: '',
});

const uploadedItems = ref([]);
const DataSourceType = ref(options[0]);
const onReset = () => {// Restablece los valores de los campos a su estado inicial
  newDatasource.repositoryId = null;
  storeDS.selectedRepositoryId = null;
  newDatasource.repositoryName = '';
  newDatasource.datasetName = '';
  newDatasource.datasetDescription = '';
  DataSourceType.value = options[0];
  databaseHost.value = '';
  databaseUser.value = '';
  databasePassword.value = '';
  remoteFileUrl.value = '';

  uploadedItems.value = []; // Vacía la lista de archivos cargados

  DataSourceType.value = options[0];
}

const onSubmit = () => {
  loading.value = true; // Activa la carga

  // Check if uploadedItems is empty
  if (uploadedItems.value.length === 0) {
    // Display a notification indicating that at least one file should be added
    notify.negative('Please add at least one file before submitting.');
    return; // Abort form submission
  }

  const data = new FormData();
  console.log("Contenido de uploadedItems:", uploadedItems.value);


  data.append("datasetName", newDatasource.datasetName);
  data.append("datasetDescription", newDatasource.datasetDescription);
  data.append("repositoryName", newDatasource.repositoryName);
  data.append("repositoryId", storeDS.selectedRepositoryId); // Set as empty string if repositoryId is null
  console.log(newDatasource.repositoryId, "++++++++++++++++++++++++++");

  const attachTables = [];

  // Append all files as an array under the key 'attach_files'
  uploadedItems.value.forEach((item) => {
    console.log("Archivo que se va a agregar:", item);

    if (item.files === undefined && isLocalRepository.value) {
      data.append('attachFiles', item);
    } else if (item.files !== undefined && isLocalRepository.value) {
      item.files.forEach((file) => {
        data.append('attachFiles', file);
      });
    } else if (item.files === undefined && !isLocalRepository.value) {
      //data.append('attachFiles', item);
      attachTables.push(item.name.toString()); // Agregar el nombre al array attachTables
    } else if (item.files === undefined && isAPIRepository.value) {
      data.append('attachFiles', item);
    } else {
      console.log("attachTables: " + item.name);
      attachTables.push(item.name.toString()); // Agregar el nombre al array attachTables
    }
    console.log(attachTables + " --------------");
  });

  data.append('attachTables', attachTables);

  integrationStore.addDataSource(route.params.id, data, successCallback);

  onReset();
}

const successCallback = (datasource) => {
  loading.value = false; // Desactiva la carga después de la operación

  console.log("success callback")

  notify.positive(`Dataset/s successfully uploaded`)
  onReset()

  showS.value = false;

  integrationStore.addSelectedDatasource(datasource)
  storeDS.getDatasources(route.params.id)
  storeDS.getRepositories(route.params.id)
}

// Método para abrir el selector de archivos
const triggerFileUpload = () => {
  const fileUploadInput = document.createElement('input');
  fileUploadInput.type = 'file';
  fileUploadInput.accept = '.csv, .json, .xml'; // Specify accepted file extensions
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
  top: 30%; /* Coloca el botón en el centro vertical */
  left: 91%; /* Coloca el botón a la derecha del contenido */
  color: blue;
  cursor: pointer;
}

.special-button q-button:hover {
  color: red; /* Cambia el color al pasar el cursor */
  //text-decoration: none; /* Elimina el subrayado al pasar el cursor */
}

.special-button-hidden {
  display: none;
}
</style>
