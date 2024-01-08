<template>
    <q-page padding>
        <q-form class="row q-col-gutter-md text-center justify-center" @submit.prevent="handleSubmit">            
            <div class="col-12">
                <h4> Logical planner </h4>
                <h6> Select the Abstract Plans to send to the Logical Planner: </h6>
            </div>
                <div class="col-6 text-left">
                <q-list bordered separator>
                    <q-item v-for="(method, index) in methods" :key="method.id" class="q-my-sm">
                        <q-item-section avatar>
                            <q-checkbox v-model="method.selected"/>
                        </q-item-section>
                        <q-item-section> 
                          <text-body1 style="font-size: 17px;"> {{ method.name }} </text-body1>
                        </q-item-section>
                        <q-item-section avatar>
                            <q-btn color="primary" icon="mdi-eye-outline" @click="openDialog(method.plan)">
                            </q-btn>
                        </q-item-section>
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
                <q-btn label="Run logical planner" color="primary" type="submit"/>
            </div>
            <div class="col-12">
                <q-btn label="Select all" @click="selectAll()"/>
                <q-btn label="Select none" @click="selectNone()" class="q-ml-sm"/>
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
const intentsStore = useIntentsStore()
const $q = useQuasar()

const methods = ref([])
const dialog = ref(false)
const maximizedToggle = ref(true)
const visualizedPlan = ref(null)
        
const handleSubmit = async() => {
  $q.loading.show({message: 'Running logical planner'})
  const successCallback = () => {
    router.push({ path: route.path.substring(0, route.path.lastIndexOf("/")) + "/workflow-planner" })
  }

  let data = []
  methods.value.map(method => {
    if (method.selected) {
      data.push(method.id)
    }
  })

  await intentsStore.setLogicalPlans(data, successCallback)
  $q.loading.hide()
}

const selectAll = () => {
  methods.value.forEach(method => {
    method.selected = true
  });
}

const selectNone = () => {
  methods.value.forEach(method => {
    method.selected = false
  });
}

const openDialog = (plan) => {
  visualizedPlan.value = plan
  dialog.value = true
}

onBeforeMount(async() => {
  await intentsStore.getAbstractPlans()
  for (let plan in intentsStore.abstractPlans) {
    const newObject = {
      name: plan.split('#').at(-1),
      id: plan,
      selected: false,
      plan: intentsStore.abstractPlans[plan]
    }
    console.log(methods.value)
    methods.value.push(newObject)
  }
})

</script>