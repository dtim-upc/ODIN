<template>
  <q-dialog v-model="showS" @hide="props.show=false">
    <q-card style="width: 400px; max-width: 80vw">

      <q-card-section>
        <!-- Resto del contenido con desplazamiento -->
        <div style="overflow-y: auto; max-height: calc(80vh - 140px);">
          <!-- Sección 1: Título form -->
          <div class="text-h5">Create new repository</div>

          <!-- File and Folder Upload Section -->
          <q-card-section v-if="isLocalFileOptionSelected">
            <q-input
              filled
              v-model="localRepository"
              label="Repository name"
              lazy-rules
              :rules="[(val) => (val && val.length > 0) || 'Please enter a name']"
            />
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
const localRepository = ref(""); // Variable para almacenar la URL del archivo remoto

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

const projectID = ref(null)

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
  repositoryName: '',
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
  const data = new FormData();
  console.log("Contenido de uploadedItems:", uploadedItems.value);

  data.append("datasetDescription", newDatasource.datasetDescription);
  data.append("repositoryName", localRepository.value);

  integrationStore.addDataRepository(route.params.id, data, successCallback);

  onReset();
}

const successCallback = (datasource) => {

  console.log("success callback")

  notify.positive(`Data Source ${datasource.id} successfully uploaded`)
  onReset()
  form.value.resetValidation()

  showS.value = false;

  storeDS.getRepositories(route.params.id);
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
