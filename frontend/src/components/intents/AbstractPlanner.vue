<template>
    <q-page padding>
        <q-form class="row q-col-gutter-md text-center justify-center" @submit.prevent="handleSubmit" @reset="resetForm">            
            <div class="col-12">
                <h4> Abstract planner </h4>
            </div>
            <div class="col-6">
                <q-input label="Intent name" outlined v-model="intentName" class="q-mb-sm"
                    :rules="[ val => val && val.length > 0 || 'Insert a name']"/>

                <q-select label="Query" outlined v-model="query" :options="queriesStore.queries.map(query => query.queryName)" class="q-mb-sm"
                    :rules="[ val => val && val.length > 0 || 'Select a dataset']"/>
                
                <q-select label="Problem" outlined v-model="problem" :options=Object.keys(intentsStore.problems) class="q-mb-sm"
                    :rules="[ val => val && val.length > 0 || 'Select a problem']"/>
                
            </div>
            <div class="col-12">
                <q-btn label="Run abstract planner" color="primary" type="submit"/>
                <q-btn label="Reset" type="reset" class="q-ml-sm"/>
                
            </div>
        </q-form>
    </q-page>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {useIntentsStore} from 'stores/intentsStore.js'
import {useQueriesStore} from 'stores/queriesStore.js'
import {useRoute, useRouter} from "vue-router";
import { useQuasar } from 'quasar'

const router = useRouter()
const route = useRoute()
const $q = useQuasar()

const intentsStore = useIntentsStore()
const queriesStore = useQueriesStore()

const intentName = ref(null)
const query = ref(null)
const problem = ref(null)

const handleSubmit = async() => {
  $q.loading.show({message: 'Annotating query...'}) // First, annotate the dataset and define the new ontology
  const selectedQuery = queriesStore.queries.find(queryStore => queryStore.queryName === query.value);
  intentsStore.selectedQuery = selectedQuery
  let data = {
    'path': selectedQuery.csvpath,
    'label': selectedQuery.label,
  }
  await intentsStore.annotateDataset(data)

  $q.loading.show({message: 'Running abstract planner...'}) // Then, run the planner
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

onMounted(() => {
  queriesStore.getQueries(route.params.id)
  intentsStore.getProblems()
})


</script>
