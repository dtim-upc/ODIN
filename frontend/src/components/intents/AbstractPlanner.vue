<template>
    <q-page padding>
        <q-form class="row q-col-gutter-md text-center justify-center" @submit.prevent="handleSubmit" @reset="resetForm">            
            <div class="col-12">
                <h4> Abstract planner </h4>
            </div>
            <div class="col-6">
                <q-input label="Intent name" outlined v-model="intentName" class="q-mb-sm"
                    :rules="[ val => val && val.length > 0 || 'Insert a name']"/>

                <q-select label="Dataset" outlined v-model="dataset" :options=Object.keys(intentsStore.datasets) class="q-mb-sm"
                    :rules="[ val => val && val.length > 0 || 'Select a dataset']"/>
                
                <q-select label="Problem" outlined v-model="problem" :options=Object.keys(intentsStore.problems)
                    :rules="[ val => val && val.length > 0 || 'Select a problem']"/>
                
            </div>
            <div class="col-12">
                <q-btn label="Run abstract planner" color="primary" type="submit"/>
                <q-btn label="Reset" type="reset" class="q-ml-sm"/>
                
            </div>
            <pre>{{ intentsStore.datasets }}</pre>
        </q-form>
    </q-page>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {useIntentsStore} from 'stores/intents.store.js'
import {useRoute, useRouter} from "vue-router";

const router = useRouter()
const route = useRoute()

const intentsStore = useIntentsStore()

const intentName = ref(null)
const dataset = ref(null)
const problem = ref(null)

const handleSubmit = () => {
  const data = {
    'intent_name': intentName.value,
    'dataset': intentsStore.datasets[dataset.value],
    'problem': intentsStore.problems[problem.value],
  }

  const successCallback = () => {
    router.push({ path: route.path.substring(0, route.path.lastIndexOf("/")) + "/logical-planner" })
  }

  console.log(data)
  intentsStore.setAbstractPlans(data, successCallback)
}

const resetForm = () => {
  intentName.value = null
  dataset.value = null
  problem.value = null
}

onMounted(() => {
  intentsStore.getDatasets()
  intentsStore.getProblems()
})


</script>
