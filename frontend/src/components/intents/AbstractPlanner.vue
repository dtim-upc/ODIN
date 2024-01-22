<template>
    <q-page padding>
        <q-form class="row q-col-gutter-md text-center justify-center" @submit.prevent="handleSubmit" @reset="resetForm">            
            <div class="col-12">
                <h4> Abstract planner </h4>
            </div>
            <div class="col-6">
                <q-input label="Intent name" outlined v-model="intentName" class="q-mb-sm"
                    :rules="[ val => val && val.length > 0 || 'Insert a name']"/>

                <q-select label="Query" outlined v-model="selectedDataProdutName" :options="dataProductsStore.dataProducts.map(dp => dp.datasetName)" class="q-mb-sm"
                    :rules="[ val => val && val.length > 0 || 'Select a dataset']"/>
                
                <q-select label="Problem" outlined v-model="problem" :options=Object.keys(intentsStore.problems) class="q-mb-sm"
                    :rules="[ val => val && val.length > 0 || 'Select a problem']"/>

                <q-select v-if="selectedDataProdutName && problem ==='Classification'" label="Target variable" outlined v-model="target" :options="getAttributes" class="q-mb-sm"
                    :rules="[ val => val && val.length > 0 || 'Select a target variable']"/>
                
            </div>
            <div class="col-12">
                <q-btn label="Run abstract planner" color="primary" type="submit"/>
                <q-btn label="Reset" type="reset" class="q-ml-sm"/>
                
            </div>
        </q-form>
    </q-page>
</template>


<script setup>
import {ref, onMounted, computed} from 'vue'
import {useIntentsStore} from 'stores/intentsStore.js'
import {useQueriesStore} from 'stores/queriesStore.js'
import {useDataProductsStore} from 'stores/dataProductsStore.js'
import {useRoute, useRouter} from "vue-router";
import { useQuasar } from 'quasar'

const router = useRouter()
const route = useRoute()
const $q = useQuasar()

const intentsStore = useIntentsStore()
const queriesStore = useQueriesStore()
const dataProductsStore = useDataProductsStore()

const intentName = ref(null)
const selectedDataProdutName = ref(null)
const problem = ref(null)
const target = ref(null)

const handleSubmit = async() => {
  const selectedDataProduct = dataProductsStore.dataProducts.find(dp => dp.datasetName === selectedDataProdutName.value);

  $q.loading.show({message: 'Creating intent...'}) // First, create the intent object in the backend
  let data = new FormData();
  data.append("intentName", intentName);
  data.append("problem", problem);
  data.append("dataProductID", selectedDataProduct.id)
  
  await intentsStore.postIntent(route.params.id, data)

  $q.loading.show({message: 'Materializing data product...'}) // Then, create the csv file from the dataProduct
  await dataProductsStore.materializeDataProduct(route.params.id, selectedDataProduct.id)

  $q.loading.show({message: 'Annotating query...'}) // Then, annotate the dataset and define the new ontology
  data = {
    'path': dataProductsStore.selectedDataProductPath,
    'label': target.value,
  }
  console.log(data)
  await intentsStore.annotateDataset(data)

  $q.loading.show({message: 'Running abstract planner...'}) // Finally, run the planner
  data = {
    'intent_name': intentName.value,
    'dataset': intentsStore.queryUri,
    'problem': intentsStore.problems[problem.value],
  }
  console.log(data)

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

const getAttributes = computed(() => {
  const selectedDataProduct = dataProductsStore.dataProducts.find(dp => dp.datasetName === selectedDataProdutName.value);

  if (selectedDataProduct) {
    return selectedDataProduct.attributes.map(att => att.name)
  }
  return []
})

onMounted(async() => {
  await dataProductsStore.getDataProducts(route.params.id)
  intentsStore.getProblems()
})


</script>
