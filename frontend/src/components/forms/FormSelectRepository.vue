<template>
  <q-dialog v-model="showS" @hide="props.show=false">
    <q-card style="width: 400px; max-width: 80vw">

      <q-card-section>
        <!-- Resto del contenido con desplazamiento -->
        <div style="overflow-y: auto; max-height: calc(80vh - 140px);">
          <!-- Sección 1: Título form -->
          <div class="text-h5">Select your repository</div>

          <!-- Sección 4: Información del Repositorio -->
          <q-card-section>
            <template v-if="storeDS.repositories.length === 0">
              <!-- Mostrar el mensaje si no hay repositorios -->
              <div class="text-h6 text-warning">
                No repositories available. Please create a new repository.
              </div>
            </template>
            <template v-else>
              <!-- Mostrar el campo de selección de repositorio si hay repositorios disponibles -->
              <q-select
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
            </template>
          </q-card-section>

        </div>
      </q-card-section>

      <q-form ref="form" @submit="onSubmit" @reset="onReset" class="q-gutter-md">
        <!-- Botones del formulario -->
        <q-card-section>
          <div v-if="showFormButtons" align="right">
            <q-btn label="Cancel" type="reset" color="primary" flat class="q-ml-sm" v-close-popup/>
            <q-btn label="Next" type="submit" color="primary"/>
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

const newDatasource = reactive({
  repositoryId: ref(null),
  repositoryName: '',
});


const onReset = () => {// Restablece los valores de los campos a su estado inicial
  newDatasource.repositoryId = null;
  newDatasource.repositoryName = '';
}

const onSubmit = () => {
  const data = new FormData();

  data.append("repositoryId", newDatasource.repositoryId === null || createNewRepository.value ? '' : newDatasource.repositoryId); // Set as empty string if repositoryId is null

  onReset();
}

const successCallback = (datasource) => {

  console.log("success callback")

  notify.positive(`Data Source ${datasource.id} successfully uploaded`)
  onReset()
  form.value.resetValidation()

  showS.value = false;

  integrationStore.addSelectedDatasource(datasource)
  storeDS.getDatasources(route.params.id)
}

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
