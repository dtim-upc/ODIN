<template>
  <q-dialog v-model="showS" @hide="props.show=false">
    <q-card style="width: 400px; max-width: 80vw">

      <q-card-section>
        <!-- Resto del contenido con desplazamiento -->
        <div style="overflow-y: auto; max-height: calc(80vh - 140px);">
          <!-- Sección 1: Título form -->
          <div class="text-h5">{{ formSchema.formTitle }}</div>

          <!-- JSON schemas automation -->
          <q-card-section>
            <div v-for="(field, fieldName) in formSchema.properties" :key="fieldName">
              <q-input v-if="field.name !== 'password'
                          && field.type !== 'button'
                          && field.type !== 'boolean'"
                       filled
                       autogrow
                       v-model="field.value"
                       :label="fieldName"
                       :rules="[(val) => (val && val.length > 0) || 'Mandatory field']"/>

              <q-btn v-if="field.type === 'button'">{{ field.text }}</q-btn>

              <q-btn-toggle
                v-if="field.type === 'boolean'"
                v-model="secondModel"
                spread
                class="my-custom-toggle"
                no-caps
                rounded
                unelevated
                toggle-color="primary"
                color="white"
                text-color="primary"
                :options="[
          {label: 'Option 1', value: 'one'},
          {label: 'Option 2', value: 'two'}
        ]"
              />
            </div>

          </q-card-section>
        </div>


          <div style="overflow-y: auto; max-height: calc(80vh - 140px);">
            <div class="text-h5">Create new repository</div>
          <q-card-section>
            <q-input filled autogrow v-model="newDatasource.repositoryName" label="Repository name" :rules="[(val) => (val && val.length > 0) || 'Mandatory field']" />
            <!--Descripción del conjunto de datos (opcional) -->
            <q-input v-model="newDatasource.datasetDescription" filled autogrow label="Description (Optional)"/>
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

            <!-- Formulario generado dinámicamente -->
            <q-card-section v-for="field in formFields" :key="field.name">

              <q-input v-if="field.name !== 'password'" filled autogrow v-model="field.value" :label="field.label" :rules="[(val) => (val && val.length > 0) || 'Mandatory field']" />
              <q-input v-if="field.name.toLowerCase() === 'password'" v-model="field.value" filled :type="isPwd ? 'password' : 'text'" :label="field.label">
                <template v-slot:append>
                  <q-icon
                    :name="isPwd ? 'visibility_off' : 'visibility'"
                    class="cursor-pointer"
                    @click="isPwd = !isPwd"
                  />
                </template>
              </q-input>
            </q-card-section>


          <q-card-section v-if="!isLocalRepository">
            <!-- Contenido del botón -->
            <q-btn label="Test connection" @click="testConnection"/>
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

const isPwd = ref(true);
const isLocalRepository = ref(false);
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

const projectID = ref(null);
const formSchema = ref("");

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
    console.log(dataRepositoryTypes, "-----------------------------------------------------------------");
    console.log(dataRepositoryTypes.value[0].fields, "-----------------------------------------------------------------");
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
    console.log(newType.name + " COMPARAAAAAAAAAAAAAAAAAAAAAA");
    DataSourceType.value = newType.name.toString();
    isLocalRepository.value = 'LocalRepository' === newType.name.toString() ? true : false;
  }
});

watch(() => showS.value, (newValue) => {
  if (newValue) {
    fetchDataRepositoryTypes();
    isLocalRepository.value = DataSourceType.value === 'LocalRepository';
  }

  // Realiza la solicitud GET al punto final del backend
  const response = odinApi.get(`/formSchema`).then(response => {
    formSchema.value = response.data;
  })
    .catch(error => {
      console.error('Error al obtener el JSON Schema', error);
    });
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

const testConnection = async () => {
  const data = {};

  // Add specific fields from formFields to the data object
  formFields.value.forEach((field) => {
    if (field.value !== null && field.value !== undefined) {
      data[field.name.toString()] = field.value.toString();
      //notify.positive(field.name.toString() + " has been added as: " + field.value.toString());
    }
  });

  try {
    // Make a POST request to the backend endpoint
    const response = await odinApi.post('/test-connection', data);

    if (response.data === true) {
      notify.positive('Connection established successfully.');
      return true;
    } else {
      notify.negative("Error connecting with the provided database.");
      return false;
    }
  } catch (error) {
    console.error('Error al intentar establecer conexión con la base de datos:', error);
    notify.negative('Error al intentar establecer conexión con la base de datos:', error);
    return false;
  }
}

const onSubmit = async () => {
  const data = {};

  data["datasetDescription"] = newDatasource.datasetDescription;
  data["repositoryName"] = newDatasource.repositoryName;
  console.log(DataSourceType.value, "++++++++++++++++++++++++++++++++ tipo repo");
  data["repositoryType"] = DataSourceType.value;

  // Add specific fields from formFields to the data object
  formFields.value.forEach((field) => {
    if (field.value !== null && field.value !== undefined) {
      data[field.name.toString()] = field.value.toString();
    }
  });

  if (!isLocalRepository.value) {
    try {
      // Await the result of the testConnection function
      const isConnected = await testConnection();

      if (isConnected) {
        integrationStore.addDataRepository(route.params.id, data, successCallback);
        onReset();
      } else {
      }
    } catch (error) {
      console.error("Error connecting with the database:", error);
      notify.negative("Error connecting with the database:", error);
    }
  } else {
    integrationStore.addDataRepository(route.params.id, data, successCallback);
  }
};

const successCallback = (datasource) => {

  console.log("success callback")

  notify.positive(`Repository successfully created`)
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
