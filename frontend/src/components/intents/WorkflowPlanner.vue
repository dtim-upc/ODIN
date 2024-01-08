<template>
    <q-page padding>
        <q-form class="row q-col-gutter-md text-center justify-center" @submit.prevent="handleSubmit">            
            <div class="col-12">
                <h4> Workflow planner </h4>
                <h6> Select the Logical Plans to send to the Workflow Planner: </h6>
            </div>
                <div class="col-12 col-lg-8 text-left">
                <q-list bordered separator>
                    <q-item v-for="(group, index) in logicalPlansGroups" :key="index" class="q-my-sm">
                        <q-item-section avatar>
                            <q-checkbox v-model="group.selected" @update:model-value="checkboxGroup(group, $event)"/>
                        </q-item-section>
                        <q-item-section> 
                          <text-body1 style="font-size: 17px;"> {{ group.id }}  {{ selectedPlansOfGroup(group) }} </text-body1>
                        </q-item-section>
                        <div class="col-6">
                          <q-expansion-item label="Individual plans" style="font-size: 17px; background-color: rgb(243, 241, 241);">
                            <q-list bordered separator>
                              <q-item v-for="(plan, indexPlan) in group.plans" :key="indexPlan" class="q-my-sm">
                                <q-item-section avatar>
                                  <q-checkbox v-model="plan.selected" @update:model-value="checkboxIndividualPlan(group, $event)"/>
                              </q-item-section>
                              <q-item-section> {{ plan.id }}</q-item-section>
                              <q-item-section avatar>
                                  <q-btn color="primary" icon="mdi-eye-outline" size="10px" @click="openDialog(plan.plan)">
                                  </q-btn>
                              </q-item-section>
                              </q-item>
                            </q-list>
                          </q-expansion-item>
                        </div>
                    </q-item>
                </q-list>

                <q-dialog v-model="dialog" persistent :maximized="maximizedToggle" transition-show="slide-up" transition-hide="slide-down">
                    <q-card class="text-black">
                      <q-bar>
                        <q-space />
                        <q-btn dense flat icon="minimize" @click="maximizedToggle = false" :disable="!maximizedToggle">
                          <q-tooltip v-if="maximizedToggle" class="bg-white text-primary">Minimize</q-tooltip>
                        </q-btn>
                        <q-btn dense flat icon="crop_square" @click="maximizedToggle = true" :disable="maximizedToggle">
                          <q-tooltip v-if="!maximizedToggle" class="bg-white text-primary">Maximize</q-tooltip>
                        </q-btn>
                        <q-btn dense flat icon="close" v-close-popup>
                          <q-tooltip class="bg-white text-primary">Close</q-tooltip>
                        </q-btn>
                      </q-bar>

                      <q-card-section>
                        <VisualizePlan :plan="visualizedPlan"/> 
                      </q-card-section>
                    </q-card>
                  </q-dialog>
                
            </div>
            <div class="col-12">
                <h6>{{ selectedPlans }} selected plan(s)</h6>
                <q-btn label="Run workflow planner" color="primary" type="submit" size="17px"/>
            </div>
            <div class="col-12">
                <q-btn label="Select all" @click="selectAll()" size="14px"/>
                <q-btn label="Select none" @click="selectNone()" class="q-ml-sm" size="14px"/>
            </div>
        </q-form>
    </q-page>
</template>

<script setup>
import {ref, onBeforeMount} from 'vue'
import {useIntentsStore} from 'stores/intents.store.js'
import VisualizePlan from "../../components/intents/VisualizePlan.vue";
import {useRoute, useRouter} from "vue-router";
import { useQuasar } from 'quasar'

const router = useRouter()
const route = useRoute()
const $q = useQuasar()

const intentsStore = useIntentsStore()

const logicalPlansGroups = ref([])
const visualizedPlan = ref(null)
const dialog = ref(false)
const maximizedToggle = ref(true)
const selectedPlans = ref(0)

const openDialog = (plan) => {
  visualizedPlan.value = plan
  dialog.value = true
}

const checkboxIndividualPlan = (group, value) => {
  if (value) selectedPlans.value++
  else selectedPlans.value--

  let countSelectedPlans = 0
  group.plans.map(plan => {
    if (plan.selected) countSelectedPlans++
  })
  if (countSelectedPlans === group.plans.length) {
    group.selected = true
  }
  else if (countSelectedPlans > 0) {
    group.selected = null // indeterminated state
  }
  else {
    group.selected = false
  }
}

const checkboxGroup = (group, value) => {
  group.plans.map(plan => {
    if (plan.selected !== value) {
      if (value) selectedPlans.value++
      else selectedPlans.value--
    }
    plan.selected = value
  })
}

const handleSubmit = () => {
  $q.loading.show({message: 'Running workflow planner'})
  const successCallback = () => {
    router.push({ path: route.path.substring(0, route.path.lastIndexOf("/")) + "/workflows" })
  }

  let selectedIDs = []
  logicalPlansGroups.value.map(group => {
    group.plans.map(plan => {
      if (plan.selected) selectedIDs.push(plan.id)
    })
  })

  intentsStore.setWorkflowPlans(selectedIDs, successCallback)
  $q.loading.hide()
}

const selectedPlansOfGroup = (group) => {
  const totalNumberOfPlans = group.plans.length
  let numberOfSelectedPlans = 0
  group.plans.map(plan => {
    if (plan.selected) numberOfSelectedPlans++
  })
  return "(" + numberOfSelectedPlans + "/" + totalNumberOfPlans + ")"
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

const selectAll = () => {
  logicalPlansGroups.value.forEach(group => {
    checkboxGroup(group, true)
    group.selected = true
  });
}

const selectNone = () => {
  logicalPlansGroups.value.forEach(group => {
    checkboxGroup(group, false)
    group.selected = false
  });
}


onBeforeMount(async() => {
  await intentsStore.getLogicalPlans()
  console.log(intentsStore.logicalPlans)
  const keys = Object.keys(intentsStore.logicalPlans);
  let groups = [];

  for (let key of keys) {
    let found = false
    const plan = {
      id: key,
      selected: false,
      plan: intentsStore.logicalPlans[key]
    }
    groups.map(group => {
      if (group.id === removeLastPart(key)) {
        group.plans.push(plan)
        found = true
      }
    })
    if (!found) {
      groups.push({
        id: removeLastPart(key),
        selected: false,
        plans: [plan]
      })
    }
  }
  logicalPlansGroups.value = groups
})

</script>
