<template>
    <q-page padding>
        <div class="row q-col-gutter-md text-center justify-center">            
            <div class="col-12">
                <h4> Final workflows </h4>
            </div>
            <div v-if="intentsStore.selectedPlans.length === 0">
              <h6 style="color: red;">No workflows selected</h6>
            </div>
              <div v-else class="col-12 col-lg-8 text-left">
                <q-list bordered separator>
                    <q-item v-for="(group, index) in intentsStore.selectedPlans" :key="index" class="q-my-sm">
                        <q-item-section> 
                          <text-body1 style="font-size: 17px;"> {{ group.id }} </text-body1>
                        </q-item-section>
                        <div >
                          <q-expansion-item label="Individual plans" style="font-size: 17px; background-color: rgb(243, 241, 241);">
                            <q-list bordered separator>
                              <q-item v-for="(plan, indexPlan) in group.plans" :key="indexPlan" class="q-my-sm">
                              <q-item-section> {{ plan.id }}</q-item-section>
                              <!---<q-item-section> 
                                <q-input v-model="plan.id" label="Workflow name"/>
                              </q-item-section>-->
                              <q-item-section avatar>
                                  <q-btn color="primary" icon="mdi-eye-outline" size="10px" @click="openDialog(plan.plan)" style="font-size: 14px;"/>
                              </q-item-section>
                              <q-item-section avatar>
                                  <q-btn color="primary" icon="mdi-database" size="10px" @click="storeWorkflowDialog(plan.plan)" label="Store" style="font-size: 14px;"/>
                              </q-item-section>
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

    <DialogWithVisualizedPlan v-model:dialog="dialog" :visualizedPlan="visualizedPlan"/>

    <q-dialog v-model="storeWorkflowDialogBoolean">
      <q-card>
        <q-card-section>
          <q-form @submit="storeWorkflow" class="text-right">
            <q-input v-model="workflowName" label="Workflow name" :rules="[ val => val && val.length > 0 || 'Insert a name']"/>
            
            <q-btn type="submit" color="primary" label="Store" v-close-popup/>
          </q-form>
        </q-card-section>
      </q-card>
    </q-dialog>
</template>

<script setup>
import {ref} from 'vue'
import {useIntentsStore} from 'stores/intentsStore.js'
import DialogWithVisualizedPlan from "../../components/intents/DialogWithVisualizedPlan.vue";
import {useRoute} from "vue-router";

const route = useRoute()
const intentsStore = useIntentsStore()

const storeWorkflowDialogBoolean = ref(false)
const dialog = ref(false)

const selectedPlan = ref(null)
const workflowName = ref("")
const visualizedPlan = ref(null)

const openDialog = (plan) => {
  visualizedPlan.value = plan
  dialog.value = true
}

const storeWorkflowDialog = (plan) => {
  selectedPlan.value = plan
  storeWorkflowDialogBoolean.value = true
}

const downloadRDF = (plan) => {
  intentsStore.downloadRDF(plan.id)
}

const downloadKNIME = (plan) => {
  intentsStore.downloadKNIME(plan.id)
}

const downloadAllRDF = () => {
  const selectedPlanIds = intentsStore.selectedPlans.map(group => group.plans.map(plan => plan.id));
  intentsStore.downloadAllRDF(selectedPlanIds)
}

const downloadAllKNIME = () => {
  const selectedPlanIds = intentsStore.selectedPlans.map(group => group.plans.map(plan => plan.id));
  intentsStore.downloadAllKNIME(selectedPlanIds)
}

const storeWorkflow = () => {
  console.log("Storing workflow")
  const data = {
    workflowName: workflowName.value,
    visualRepresentation: selectedPlan.value
  };
  const projectID = route.params.id

  intentsStore.storeWorkflow(projectID, data)
}

</script>
