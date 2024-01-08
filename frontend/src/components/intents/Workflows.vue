<template>
    <q-page padding>
        <div class="row q-col-gutter-md text-center justify-center">            
            <div class="col-12">
                <h4> Final workflows </h4>
            </div>
              <div class="col-12 col-lg-8 text-left">
                <q-list bordered separator>
                    <q-item v-for="(group, index) in workflowGroups" :key="index" class="q-my-sm">
                        <q-item-section> 
                          <text-body1 style="font-size: 17px;"> {{ group.id }} </text-body1>
                        </q-item-section>
                        <div class="col-6">
                          <q-expansion-item label="Individual plans" style="font-size: 17px; background-color: rgb(243, 241, 241);">
                            <q-list bordered separator>
                              <q-item v-for="(plan, indexPlan) in group.plans" :key="indexPlan" class="q-my-sm">
                              <q-item-section> {{ plan }}</q-item-section>
                              <q-item-section avatar>
                                  <q-btn color="primary" icon="mdi-download" size="10px" @click="downloadRDF(plan)" label="RDF" style="font-size: 14px;"/>
                              </q-item-section>
                              <q-item-section avatar>
                                  <q-btn color="primary" icon="mdi-download" size="10px" @click="downloadKNIME(plan)" label="KNIME" style="font-size: 14px;">
                                  </q-btn>
                              </q-item-section>
                              </q-item>
                            </q-list>
                          </q-expansion-item>
                        </div>
                    </q-item>
                </q-list>
                
            </div>
            <div class="col-12">
                <q-btn label="Download all RDF representations" @click="downloadAllRDF()"/>
                <q-btn label="Download all KNIME representations" @click="downloadAllKNIME()" class="q-ml-sm"/>
            </div>
        </div>
    </q-page>
</template>

<script setup>
import {ref, onBeforeMount} from 'vue'
import {useIntentsStore} from 'stores/intents.store.js'

const intentsStore = useIntentsStore()
const workflowGroups = ref([])

const downloadRDF = (plan) => {
  intentsStore.downloadRDF(plan)
}

const downloadKNIME = (plan) => {
  intentsStore.downloadKNIME(plan)
}

const downloadAllRDF = () => {
  intentsStore.downloadAllRDF()
}

const downloadAllKNIME = () => {
  intentsStore.downloadAllKNIME()
}

function removeLastPart(inputString) {
    const parts = inputString.split(' ');
    if (parts.length > 1) {
        parts.pop(); // Remove the last part
        return parts.join(' ');
    } else {
        return inputString; // Return the original string if there's only one part
    }
}

onBeforeMount(async() => {
  await intentsStore.getWorkflowPlans()
  console.log(intentsStore.workflowPlans)
  let groups = []

  intentsStore.workflowPlans.map(plan => {
    const method = removeLastPart(plan)
    let found = false
    groups.map(group => {
      if (group.id = method) {
        group.plans.push(plan)
        found = true
      }
    })
    if (!found) {
      groups.push({
        id: method,
        plans: [plan]
      })
    }
  })
  workflowGroups.value = groups
})

</script>
