<template>
  <q-dialog v-model="showS" @hide="props.show=false">
    <q-card style="width: 400px; max-width: 80vw">
      <!-- Sección 1: Título -->
      <q-card-section>
        <div class="text-h5">Create new dataset</div>
      </q-card-section>

      <!-- Sección 2: Información del Repositorio -->
      <q-card-section>
        <div class="text-h6">Repository information</div>
        <q-form ref="form" @submit="onSubmit" @reset="onReset" class="q-gutter-md">
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

          <!-- Sección 3: Información del Conjunto de Datos -->
          <q-card-section>
            <div class="text-h6">Dataset information</div>
            <!-- Tipo de origen de datos -->
            <q-select
              v-model="DataSourceType"
              :options="options"
              label="Type"
              class="q-mt-none"
            />

            <!-- Mostrar selector de archivo si se selecciona "Local file/s" -->
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

            <!-- Botón personalizado con dos columnas -->
            <q-card-section v-if="isLocalFileOptionSelected">
              <div class="uploader__empty-state uploader__empty-state--with-display-name uploader__empty-state--with-directories-selector">
                <div class="uploader__empty-state-column">
                  <svg viewBox="0 0 72 72" role="img" aria-label="Subir archivos">
                    <path
                      d="M36.493 72C16.118 72 0 55.883 0 36.493 0 16.118 16.118 0 36.493 0 55.882 0 72 16.118 72 36.493 72 55.882 55.883 72 36.493 72zM34 34h-9c-.553 0-1 .452-1 1.01v1.98A1 1 0 0 0 25 38h9v9c0 .553.452 1 1.01 1h1.98A1 1 0 0 0 38 47v-9h9c.553 0 1-.452 1-1.01v-1.98A1 1 0 0 0 47 34h-9v-9c0-.553-.452-1-1.01-1h-1.98A1 1 0 0 0 34 25v9z"
                      fill="#5268ff" fill-rule="nonzero"></path>                  </svg>
                </div>
                <div class="uploader__empty-state-column">
                  <div class="uploader__empty-state-text">
                    <h2>Subir archivos</h2>
                    <button class="uploader__sub-title uploader__directories-dialog-trigger">O selecciona una carpeta</button>
                  </div>
                </div>
              </div>


              <label for="fileInput" class="q-btn q-btn-item q-btn-sm round q-mr-md">
                <q-row justify="left" align="center">
                  <!-- Columna 1: Icono de suma en un círculo -->
                  <q-col cols="auto">
                    <input
                      id="fileInput"
                      type="file"
                      style="display: none"
                      multiple
                      accept="*"
                      @change="handleDirectorySelection"
                    />
                    <q-btn
                      icon="add"
                      round
                      color="primary"
                      size="sm"
                      class="q-mr-md"
                      @click="openFilePicker"
                      @change="updateUploadedFiles"
                    ></q-btn>
                  </q-col>
                  <!-- Columna 2: Texto "Upload files" y botón "Or select a folder" -->
                  <q-col cols="auto">
                    <div class="text-h6">Upload files</div>
                    <q-btn
                      label="Or select a folder"
                      color="primary"
                      @click.prevent="openDirectoryPicker"
                    />
                  </q-col>
                </q-row>
              </label>
            </q-card-section>

            <!-- Mostrar campos de conexión a la base de datos si se selecciona "SQL Database" -->
            <q-card-section v-else>
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

          <!-- Botones del formulario -->
          <q-card-section>
            <div v-if="showFormButtons">
              <q-btn label="Submit" type="submit" color="primary"/>
              <q-btn label="Cancel" type="reset" color="primary" flat class="q-ml-sm" v-close-popup/>
            </div>
          </q-card-section>
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

    if (file.isDirectory) {
      await processDirectory(file);
    } else {
      // Es un archivo, agrégalo a la lista
      uploadedFiles.value.push(file);
    }
  }
};

const processDirectory = async (directory) => {
  const directoryReader = directory.createReader();

  const readEntriesRecursively = async (reader) => {
    const entries = await new Promise((resolve) => reader.readEntries(resolve));

    if (entries.length === 0) {
      return;
    }

    for (const entry of entries) {
      if (entry.isDirectory) {
        await readEntriesRecursively(entry.createReader());
      } else {
        // Es un archivo, agrégalo a la lista
        uploadedFiles.value.push(entry);
      }
    }

    await readEntriesRecursively(reader);
  };

  await readEntriesRecursively(directoryReader);
};


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
}

const successCallback = (datasource) => {

  console.log("success callback")

  notify.positive(`Data Source ${datasource.datasetName} successfully uploaded`)
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

.fileLabel {
  font-size: 22px; /* Cambia el tamaño de la fuente según tus preferencias */
  margin-bottom: 10px; /* Espacio entre la etiqueta y el texto richText */
}

.richText {
  font-size: 16px;
  color: grey; /* Color del texto richText, puedes cambiarlo según tu preferencia */
  cursor: pointer;
  text-decoration: underline;
}

.q-dialog {
  text-size-adjust: 100%;
  -webkit-font-smoothing: antialiased;
  font-weight: 400;
  font-family: Actief Grotesque Normal,-apple-system,\.SFNSText-Regular,San Francisco,Roboto,Segoe UI,Helvetica Neue,Lucida Grande,sans-serif;
  -webkit-tap-highlight-color: rgba(0,0,0,0);
  box-sizing: border-box;
  cursor: pointer;
  outline: 0;
  user-select: none;
  display: flex;
  align-items: center;
  padding: 0 1.25em;
  min-height: 6.875em;
}

.uploader__empty-state {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.uploader__empty-state-column {
  flex: 1; /* Esto asegura que ambas columnas ocupen un espacio igual */
  padding: 10px; /* Añade un espacio entre las columnas si es necesario */
}

/* Estilo adicional para el contenido SVG en la primera columna */
.uploader__empty-state-column svg {
  width: 100%;
  max-width: 50px; /* Ajusta el ancho máximo según tus preferencias */
}
/* Estilo adicional para el botón en la segunda columna */
.uploader__sub-title {
  background-color: #5268ff; /* Cambia el color de fondo según tus preferencias */
  color: white; /* Cambia el color del texto según tus preferencias */
  padding: 5px 10px; /* Ajusta el espaciado del botón según tus preferencias */
  border: none; /* Quita el borde si es necesario */
}

</style>
