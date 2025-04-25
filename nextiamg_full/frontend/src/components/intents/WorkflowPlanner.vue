<template>
  <q-page padding>
    <q-form class="row q-col-gutter-md text-center justify-center" @submit.prevent="handleSubmit">            
      <div class="col-12">
        <h4> Workflow planner </h4>
        <h6> Select the Logical Plans to send to the Workflow Planner: </h6>
      </div>
      <div v-if="intentsStore.logicalPlans.length === 0">
        <h6 style="color: red;">No logical plans generated</h6>
      </div>
      <div v-else class="col-12 col-lg-8 text-left">
        <q-list bordered separator>
          <q-item v-for="(group, index) in intentsStore.logicalPlans" :key="index" class="q-my-sm">
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

        <DialogWithVisualizedPlan v-model:dialog="dialog" :visualizedPlan="visualizedPlan"/>
      
      </div>
      <div class="col-12">
        <h6>{{ countSelectedPlans }} selected plan(s)</h6>
        <q-btn label="Select plans" color="primary" type="submit" size="17px"/>
      </div>
      <div class="col-12">
        <q-btn label="Select all" @click="selectAll()" size="14px"/>
        <q-btn label="Select none" @click="selectNone()" class="q-ml-sm" size="14px"/>
      </div>
    </q-form>
  </q-page>
</template>

<script setup>
import {ref} from 'vue'
import {useIntentsStore} from 'stores/intentsStore.js'
import DialogWithVisualizedPlan from "../../components/intents/visualize_plan/DialogWithVisualizedPlan.vue";
import {useRoute, useRouter} from "vue-router";

const router = useRouter()
const route = useRoute()

const intentsStore = useIntentsStore()

const visualizedPlan = ref(null)
const dialog = ref(false)
const countSelectedPlans = ref(intentsStore.countSelectedPlans)

const openDialog = (plan) => {
  visualizedPlan.value = plan
  dialog.value = true
}

const checkboxIndividualPlan = (group, value) => {
  if (value) countSelectedPlans.value++
  else countSelectedPlans.value--

  let countSelectedPlansOfGroup = 0
  group.plans.map(plan => {
    if (plan.selected) countSelectedPlansOfGroup++
  })
  if (countSelectedPlansOfGroup === group.plans.length) {
    group.selected = true
  }
  else if (countSelectedPlansOfGroup > 0) {
    group.selected = null // indeterminated state
  }
  else {
    group.selected = false
  }
}

const checkboxGroup = (group, value) => {
  group.plans.map(plan => {
    if (plan.selected !== value) {
      if (value) countSelectedPlans.value++
      else countSelectedPlans.value--
    }
    plan.selected = value
  })
}

const handleSubmit = () => {
  intentsStore.selectedPlans = intentsStore.logicalPlans.map(group => {
    const filteredPlans = group.plans.filter(plan => plan.selected)
    return {
      ...group,
      plans: filteredPlans
    }
  }).filter(group => group.plans.length > 0) // get only the groups with at least one selected plan
  intentsStore.countSelectedPlans = countSelectedPlans.value

  router.push({ path: route.path.substring(0, route.path.lastIndexOf("/")) + "/intent-workflows" })
}

const selectedPlansOfGroup = (group) => {
  const totalNumberOfPlansOfGroup = group.plans.length
  let countSelectedPlansOfGroup = 0
  group.plans.map(plan => {
    if (plan.selected) countSelectedPlansOfGroup++
  })
  return "(" + countSelectedPlansOfGroup + "/" + totalNumberOfPlansOfGroup + ")"
}

const selectAll = () => {
  intentsStore.logicalPlans.forEach(group => {
    checkboxGroup(group, true)
    group.selected = true
  });
}

const selectNone = () => {
  intentsStore.logicalPlans.forEach(group => {
    checkboxGroup(group, false)
    group.selected = false
  });
}

</script>
