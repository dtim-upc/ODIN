<template>
  <q-stepper style="width:100%;margin-top:15px" v-model="step" ref="stepper" color="primary" animated
             class="no-padding-stepper">
    <q-step v-for="(stepItem, index) in steps" :key="index" :name="stepItem.name" :title="stepItem.title" :icon="stepItem.icon" :done="step > stepItem.name" :style="stepItem.style" :id="stepItem.id">
      <div v-if="stepItem.id === 'previewSourceStep' || stepItem.id === 'previewIntegration'" :class="stepItem.divClass" :style="stepItem.divStyle">
        <div v-if="stepItem.id === 'previewSourceStep'" class="col-8" style="height: 100%; min-width: 75vw;">
          <Graph :graphical="integrationStore.getGraphicalB"></Graph>
        </div>
        <div v-else class="col-12">
          This is a preview of the global schema generated. Note that green elements are integrated resources. If you
          would like to see the schema integrated, use the toggle to visualize how source schemas are connected.
          <q-toggle :label="previewGS" false-value="Schema integrated" true-value="Global schema" v-model="previewGS"/>
          <Graph :graphical="previewGS === 'Global schema' ? integrationStore.getTemporalGlobalSchema: 
                  integrationStore.getTemporalGraphicalSchemaIntegration" />
        </div>
      </div>
      <TableAlignments v-if="stepItem.id === 'integrateWithProject'"/>
      <div v-if="stepItem.id === 'reviewAlignments'">
        The following alignments cannot be integrated as their entity domains are not integrated. Delete them or indicate
        the relationships of their entity domains to integrate them.
        <TableJoinAlignments :no_shadow="true"></TableJoinAlignments>
      </div>
    </q-step>

    <template v-slot:navigation>
      <q-stepper-navigation class="">
        <q-btn flat color="primary" @click="previousStep()" :label="step === 4 ? 'Back' : 'Back'" class="q-ml-sm"/>
        <q-btn class="q-ml-sm" @click="clickOk" :disable="disableStepBtn()" color="primary" :label="stepLabel()"/>
      </q-stepper-navigation>
    </template>
  </q-stepper>
</template>

<script setup>
import {ref} from '@vue/runtime-core'
import TableAlignments from 'components/tables/TableAlignments.vue';
import TableJoinAlignments from 'components/tables/TableJoinAlignments.vue';
import Graph from 'components/graph/Graph.vue'
import {useIntegrationStore} from 'src/stores/integrationStore.js'
import {useRouter} from "vue-router";

const integrationStore = useIntegrationStore();
const router = useRouter()

const step = ref(1)
const previewGS = ref('Global schema')

const steps = [
  { name: 1, title: "Preview dataset", icon: "settings", style: "min-height: 70vh;height: 1px", id: "previewSourceStep", divClass: "column items-center", divStyle: "height: 100%;" },
  { name: 2, title: "Integrate with project", icon: "mdi-graph-outline", style: "min-height: 70vh", id: "integrateWithProject" },
  { name: 3, title: "Review alignments", icon: "mdi-table-headers-eye", style: "min-height: 70vh", id: "reviewAlignments" },
  { name: 4, title: "Preview integration", icon: "mdi-eye", style: "min-height: 70vh;height: 1px", id: "previewIntegration", divClass: "row", divStyle: "height: 92%;" }
]

const disableStepBtn = () => {
  switch (step.value) {
    case 2:
      return integrationStore.alignments.length === 0;
    case 3:
      return !integrationStore.isJoinAlignmentsRelationshipsComplete;
    default: 
      return false;
  }
}

const stepLabel = () => {
  switch (step.value) {
    case 3:
      return "Integrate"
    case 4:
      return "Finish"
    default:
      return "Continue"
  }
}

const previousStep = () => {
  if (integrationStore.joinAlignments.length === 0 && step.value === 4) {
    step.value = 2
  } else if (step.value === 1) {
    router.push({ name: 'datasets' });
  } else {
    step.value--
  }
}

const clickOk = () => {
  switch (step.value) {
    case 1:
      step.value++
      integrationStore.alignments = [] // reset the alignments left from other integrations
      break;
    case 2:
      integrationStore.integrate(function () {
        if (integrationStore.joinAlignments.length === 0) {
          step.value = 4
        } else {
          step.value++
        }
      })
      break;
    case 3:
      integrationStore.reviewAlignments(function () {
        step.value++
      })
      break;
    default: //last step
      integrationStore.persistIntegration()
      router.push({ name: 'datasets' });
  }
}

</script>

<style lang="scss">

#previewIntegration > .q-stepper__step-content, #previewSourceStep > .q-stepper__step-content {
  height: 100%;

  .q-stepper__step-inner {
    height: 100%;
    // height: 90%;
  }
}

</style>
