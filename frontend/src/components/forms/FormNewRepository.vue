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

          <!-- Formulario generado dinámicamente -->
          <q-card-section v-for="field in formFields" :key="field.name">
            <q-input v-model="field.value" :label="field.label" :rules="field.rules" />
          </q-card-section>


          <!-- Tipo de origen de datos -->
          <q-card-section>
            <!-- Tipo de origen de datos -->
            <q-select
              v-model="DataSourceType"
              :options="dataRepositoryTypes"
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

const localRepository = ref(""); // Variable para almacenar la URL del archivo remoto
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
const integrationStore = useIntegrationStore()

const projectID = ref(null)

// When the component is mounted, fetch the repositories for the current project.
onMounted(async () => {
  const url = window.location.href; // Get the current URL
  const regex = /project\/(\d+)\//;
  const match = url.match(regex);
  let projectId;
  await fetchDataRepositoryTypes();
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

// Variable reactiva para almacenar los tipos de DataRepository
const dataRepositoryTypes = ref([]);
const DataSourceType = ref();
// Función para cargar los tipos de DataRepository desde el endpoint
const fetchDataRepositoryTypes = async () => {
  try {
    const response = await odinApi.get("/api/data-repository-types");
    dataRepositoryTypes.value = response.data; // Asigna la respuesta a la variable reactiva
    console.log(dataRepositoryTypes,"-----------------------------------------------------------------");
    console.log(dataRepositoryTypes.value[0].fields,"-----------------------------------------------------------------");
    formFields.value = dataRepositoryTypes.value[0].fields;
    DataSourceType.value = dataRepositoryTypes.value[0].name;
  } catch (error) {
    console.error("Error al obtener los tipos de DataRepository:", error);
  }
};

// Observa los cambios en DataSourceType y actualiza los campos del formulario
watch(DataSourceType, (newType) => {
  const selectedType = dataRepositoryTypes.value.find((type) => type.name === newType.name);
  if (selectedType) {
    formFields.value = selectedType.fields;
  }
});

defineExpose({
  form
})

const formFields = ref([]);

const newDatasource = reactive({
  repositoryName: '',
  datasetName: '',
  datasetDescription: '',
});

const onReset = () => {// Restablece los valores de los campos a su estado inicial
  newDatasource.repositoryId = null;
  storeDS.selectedRepositoryId = null;
  newDatasource.repositoryName = '';
  newDatasource.datasetName = '';
  newDatasource.datasetDescription = '';
  databaseHost.value = '';
  databaseUser.value = '';
  databasePassword.value = '';
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

const databaseHost = ref('');
const databaseUser = ref('');
const databasePassword = ref('');

</script>


<style lang="scss" scoped>
</style>
