<template>
    <q-page>
        <q-form class="row text-center justify-center" @submit.prevent="handleSubmit" @reset="resetForm">            
            <div class="col-12">
                <h4> Abstract Planner </h4>
            </div>
            <div class="col-12" style="padding-left: 12px;">
                <div class="text-body1 text-left"> General intent information </div>
            </div>
            <div class="col-4" style="padding: 10px;">
              <q-input label="Intent name" outlined v-model="intentName" class="q-mb-sm" disable
                    :rules="[ val => val && val.length > 0 || 'Insert a name']"/>
            </div>
            <div class="col-4" style="padding: 10px;">
              <q-select label="Data product" outlined v-model="selectedDataProdutName" disable class="q-mb-sm"
                :rules="[ val => val && val.length > 0 || 'Select a dataset']"/>
            </div>
            <div class="col-4" style="padding: 10px;">
              <q-select label="Problem" outlined v-model="problem" disable class="q-mb-sm"
                    :rules="[ val => val && val.length > 0 || 'Select a problem']"/>
            </div>
            <div class="col-12" style="padding-left: 12px;">
                <div class="text-body1 text-left"> Problem-specific information </div>
            </div>
            <div class="col-4" style="padding: 10px;">
              <q-select v-if="selectedDataProdutName && problem ==='Classification'" label="Target variable" outlined v-model="target" disable class="q-mb-sm"
                    :rules="[ val => val && val.length > 0 || 'Select a target variable']"/>
            </div>
            <div class="col-4"></div>
            <div class="col-4"></div>
            <div class="col-12" style="padding-left: 12px;">
                <div class="text-body1 text-left"> Configuration </div>
            </div>
            <!-- <div class="col-3" style="padding: 10px;">
              <q-select label="Metric to optimize" outlined v-model="selectedMetric" :options=intentsStore.allMetrics class="q-mb-sm"
                    :rules="[ val => val && val.length > 0 || 'Select a metric']"/>
            </div> -->
            <div class="col-3" style="padding: 10px;">
              <q-select label="Algorithm" outlined v-model="selectedAlgorithm" :options=intentsStore.allAlgorithms class="q-mb-sm"
              :rules="[ val => val && val.length > 0 || 'Select an algorithm']"/>
            </div>
            <!-- <div class="col-3" style="padding: 10px;">
              <q-select label="Is preprocessing needed?" outlined v-model="selectedPreprocessing" :options="['Yes', 'No']" class="q-mb-sm"
              :rules="[ val => val && val.length > 0 || 'Select an option']"/>
            </div>
            <div class="col-3" style="padding: 10px;">
              <q-select label="Preprocessing algorithm" outlined v-model="selectedPreprocessingAlgorithm" :options=intentsStore.allPreprocessingAlgorithms class="q-mb-sm"
              :rules="[ val => val && val.length > 0 || 'Select an algorithm']"
              v-if="selectedPreprocessing ==='Yes'"/>
            </div> -->

            <div class="col-12">
                <q-btn label="Run Abstract Planner" color="primary" type="submit"/>
                <q-btn label="Reset" type="reset" class="q-ml-sm"/>
                
            </div>
        </q-form>
    </q-page>
</template>

<script setup>
import {ref, onMounted, computed} from 'vue'
import {useIntentsStore} from 'stores/intentsStore.js'
import {useDataProductsStore} from 'stores/dataProductsStore.js'
import {useProjectsStore} from 'stores/projectsStore.js'
import {useRoute, useRouter} from "vue-router";
import {useQuasar} from 'quasar'

const router = useRouter()
const route = useRoute()
const $q = useQuasar()

const intentsStore = useIntentsStore()
const dataProductsStore = useDataProductsStore()
const projectID = useProjectsStore().currentProject.projectId

const intentName = computed({ get: () => intentsStore.intentName})
const selectedDataProdutName = computed({ get: () => intentsStore.selectedDataProdutName})
const problem = computed({ get: () => intentsStore.selectedProblem})
const target = computed({ get: () => intentsStore.target})

const selectedMetric = computed({
  get: () => intentsStore.selectedMetric,
  set: (value) => intentsStore.selectedMetric = value
})
const selectedAlgorithm = computed({
  get: () => intentsStore.selectedAlgorithm,
  set: (value) => intentsStore.selectedAlgorithm = value
})
const selectedPreprocessing = computed({
  get: () => intentsStore.selectedPreprocessing,
  set: (value) => intentsStore.selectedPreprocessing = value
})
const selectedPreprocessingAlgorithm = computed({
  get: () => intentsStore.selectedPreprocessingAlgorithm,
  set: (value) => intentsStore.selectedPreprocessingAlgorithm = value
})

const handleSubmit = async() => {
  const selectedDataProduct = dataProductsStore.dataProducts.find(dp => dp.datasetName === selectedDataProdutName.value);

  $q.loading.show({message: 'Creating intent...'}) // First, create the intent object in the backend
  let data = new FormData();
  data.append("intentName", intentName.value);
  data.append("problem", intentsStore.problems[problem.value]);
  data.append("dataProductID", selectedDataProduct.id)
  
  await intentsStore.postIntent(projectID, data)
  await intentsStore.getAllIntents(projectID, data) // Refresh the list of intents

  $q.loading.show({message: 'Materializing data product...'}) // Then, create the csv file from the dataProduct
  await dataProductsStore.materializeDataProduct(projectID, selectedDataProduct.id)

  $q.loading.show({message: 'Annotating query...'}) // Then, annotate the dataset and define the new ontology
  data = {
    'path': dataProductsStore.selectedDataProductPath,
    'label': target.value,
  }
  await intentsStore.annotateDataset(data)

  $q.loading.show({message: 'Running abstract planner...'}) // Finally, run the planner
  data = {
    'intent_name': intentName.value,
    'dataset': intentsStore.dataProductURI,
    'problem': intentsStore.problems[problem.value],
    'ontology': intentsStore.ontology,
  }

  const successCallback = () => {
    router.push({ path: route.path.substring(0, route.path.lastIndexOf("/")) + "/logical-planner" })
  }

  await intentsStore.setAbstractPlans(data, successCallback)
  $q.loading.hide()
}

const resetForm = () => {
  intentName.value = null
  query.value = null
  problem.value = null
}

onMounted(async() => {
  await dataProductsStore.getDataProducts(projectID)
  intentsStore.getProblems()
})

</script>
