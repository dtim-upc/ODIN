<template>
  <q-dialog v-model="showS" @hide="props.show=false">
    <q-card style="width: 400px; max-width: 80vw">

      <q-card-section>
        <div style="overflow-y: auto; max-height: calc(80vh - 140px);">
          <div class="text-h5">Create new repository</div>
          <q-card-section>
            <q-input filled autogrow v-model="formData['repositoryName']" label="Repository name"
                     :rules="[(val) => (val && val.length > 0) || 'Mandatory field']"/>
            <q-input v-model="formData['repositoryDescription']" filled autogrow label="Description (Optional)"/>
          </q-card-section>

          <!-- Tipo de origen de datos -->
          <q-card-section>
            <!-- Tipo de origen de datos -->
            <q-select
              v-model="RepositoryType"
              :options="dataRepositoryTypes"
              label="Repository Type"
              class="q-mt-none"
            />
          </q-card-section>

          <q-card-section>
            <div class="text-h6">Access:</div>
            <div style="display: flex; justify-content: center;">
              <q-option-group  :options="[{label: 'Virtualized', value: true},{label: 'Materialized', value: false}]" 
                                v-model="formData.isVirtualized" color="primary" inline
                                :disable="RepositoryType?.value === 'Local_Repository.json'" />
            </div>
          </q-card-section>
          <q-card-section>
            <!-- We iterate over every element of the schema of the selected repository -->
            <div v-for="(field, fieldName) in formSchema.properties" :key="fieldName">
              <!-- "Normal" fields (i.e. for all repositories) -->
              <div v-if="field.type !== undefined && field.dependsOn === undefined">
                <div>
                  <!-- Text fields other than passwords -->
                  <q-input v-if="fieldName.toLowerCase() !== 'password' && field.type === 'string'"
                           filled autogrow v-model="formData[fieldName]" :label="field.label"
                           :rules="[(val) => (val && val.length > 0) || 'Mandatory field']"/>
                  <!-- Passwords -->
                  <q-input v-if="fieldName.toLowerCase() === 'password'"
                           v-model="formData[fieldName]" filled :type="isPwd ? 'password' : 'text'"
                           :label=field.label>
                    <template v-slot:append>
                      <q-icon
                        :name="isPwd ? 'visibility_off' : 'visibility'"
                        class="cursor-pointer"
                        @click="isPwd = !isPwd"
                      />
                    </template>
                  </q-input>

                  <!-- Selects -->
                  <q-select v-if="field.type === 'select'" class="q-mt-none"
                    v-model="formData[fieldName]" :options="field.options" :label="field.label"
                  />

                  <!-- Buttons -->
                  <q-btn v-if="field.type === 'button'" @click="testConnection">{{ field.label }}</q-btn>

                  <!-- Toggles -->
                  <q-btn-toggle v-if="field.type === 'toggle'" v-model="formData[fieldName]" spread class="q-ma-md"
                    no-caps rounded unelevated toggle-color="primary" color="white" text-color="primary"
                    :options="field.options"
                  />

                </div>
              </div>
              <!-- Fields that appear when the repository connects to a database -->
              <div v-else-if="field.dependsOn.field === 'connectBy' && field.dependsOn.value === formData['connectBy']">
                <div v-if="field.type === 'section'" v-for="(field, fieldName) in field.properties" :key="fieldName">
                  <div>
                    <q-input v-if="fieldName.toLowerCase() !== 'password' && field.type === 'string'"
                             filled autogrow v-model="formData[fieldName]" :label="field.label"
                             :rules="[(val) => (val && val.length > 0) || 'Mandatory field']"/>

                    <q-input v-if="fieldName.toLowerCase() === 'password'"
                             v-model="formData[fieldName]" filled :type="isPwd ? 'password' : 'text'" :label=field.label>
                      <template v-slot:append>
                        <q-icon
                          :name="isPwd ? 'visibility_off' : 'visibility'"
                          class="cursor-pointer"
                          @click="isPwd = !isPwd"
                        />
                      </template>
                    </q-input>

                    <q-select v-if="field.type === 'select'" v-model="formData[fieldName]"
                      :options="field.options" :label="field.label" class="q-mt-none"
                    />
                    <q-btn v-if="field.type === 'button'" @click="testConnection">{{ field.label }}</q-btn>

                    <q-btn-toggle v-if="field.type === 'toggle'" v-model="formData[fieldName]" spread
                      class="q-ma-md" no-caps rounded unelevated toggle-color="primary" color="white"
                      text-color="primary" :options="field.options"
                    />

                  </div>
                </div>
              </div>
            </div>
          </q-card-section>
        </div>

      </q-card-section>
      <q-form ref="form" @submit="onSubmit" @reset="onReset" class="q-gutter-md">
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
import {ref, onMounted, watch, computed} from "vue";
import {useRepositoriesStore} from "stores/repositoriesStore.js";
import { useProjectsStore } from "src/stores/projectsStore";

const repositoriesStore = useRepositoriesStore();
const projectID = useProjectsStore().currentProject.projectId

const form = ref(null)
const isPwd = ref(true);

const RepositoryType = ref();
const dataRepositoryTypes = ref([]);
const formSchema = ref("");
const connectBy = ref("");
const formData = ref({
  repositoryDescription: '',
  repositoryName: '',
  isVirtualized: ref(false),
  connectBy: 'connectByUrl'
});

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

watch(() => showS.value, async (newValue) => {
  if (newValue) {
    dataRepositoryTypes.value = await repositoriesStore.getRepositoryTypes();
  }
});

defineExpose({
  form
})

onMounted( async() => {
  dataRepositoryTypes.value = await repositoriesStore.getRepositoryTypes();
  await repositoriesStore.getRepositories(projectID)
});

// When the user selects a repository, we fetch its schema from the API
watch(RepositoryType, async(newType) => {
  const fileName = newType.value;
  formSchema.value = await repositoriesStore.getRepositorySchema(fileName);

  if (newType.value === 'Local_Repository.json') {
    formData.value.isVirtualized = false;
  }
  formData["connectBy"] = connectBy;

  if (formSchema.properties) {
    const properties = responseData.properties;
    for (const attributeName in properties) {
      formData[attributeName] = properties[attributeName]; // Dynamically add the attributes
    }
  }
})

const onReset = () => {
  repositoriesStore.selectedRepository = {};
}

const testConnection = async () => {
  const data = {};
  if (formSchema.value.attributes) {
    const formAttributes = formSchema.value.attributes;
    for (const attributeName in formAttributes) {
      data[attributeName] = formData.value[attributeName]; // Dynamically add the attributes
    }
  }
  return repositoriesStore.testConnection(data);
}

const onSubmit = async () => {
  const data = {}
  data["repositoryName"] = formData.value["repositoryName"];
  data["datasetDescription"] = formData.value["repositoryDescription"];
  data["repositoryType"] = formSchema.value.class;
  data["connectBy"] = formData.value["connectBy"];
  data["isVirtual"] = formData.value["isVirtualized"];

  if (formSchema.value.attributes) {
    const formAttributes = formSchema.value.attributes;
    for (const attributeName in formAttributes) {
      data[attributeName] = formData.value[attributeName] // Dynamically add the attributes of the schemas
    }
  }

  const successCallback = () => {
    onReset()
    form.value.resetValidation()
    showS.value = false;
  }
  
  // If the repository is relational, we test the connection before creating it
  if (formSchema.value.class === 'RelationalJDBCRepository') {
    if (await testConnection()) {
      repositoriesStore.postRepository(projectID, data, successCallback)
    } 
  }
  else {
    repositoriesStore.postRepository(projectID, data, successCallback)
  }
}


</script>