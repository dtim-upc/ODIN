<template>
  <q-card flat bordered>
    <q-item class="q-py-none">

      <q-item-section>
        <q-item-label class="dark_title" style="font-size: 20px;">Select alignments</q-item-label>
      </q-item-section>

      <q-card-section class="row items-center q-pb-none q-pt-none">
        <q-space/>
        <q-btn icon="close" flat round dense v-close-popup @click="close" class="dark_title"/>
      </q-card-section>
    </q-item>

    <q-separator/>

    <q-card-section class="row no-padding">
      <q-card-section class="col-12 no-padding">

        <div class=" row q-pb-none">

          <q-banner dense inline-actions class="text-white bg-red col-12" :class="{ hidden: !alertAlignmentType }">
            Incompatible types. Please select another element with the same type.
          </q-banner>

          <q-card-section class="col">
            <div class="row bg-primary justify-center text-white q-pa-xs">
              Project: {{ projectsStore.currentProject.projectName }}
            </div>
            <div class="row" style="min-height:50vh ;border: 1px #e4eaec;border-style: solid;">
              <div class="col">
                <Graph :graphical="projectsStore.getGlobalSchema" :alignment="alignment" :enableClickR="true"
                       @elementClick="setAlignmentA"></Graph>
              </div>
            </div>
            <div class="q-pt-md">
              <div class="q-gutter-md">
                <q-input outlined v-model="alignment.type" prefix="Type: " disable dense/>
                <q-input outlined v-model="alignment.resourceA.label" prefix="Label: " disable dense/>
              </div>
            </div>
          </q-card-section>

          <q-card-section class="col q-pl-none">
            <div class="row bg-primary justify-center text-white q-pa-xs"> Dataset:
              {{ integrationStore.getSourceB.datasetName }}
            </div>
            <div class="row" style="min-height:50vh ;">
              <div class="col">
                <Graph :graphical="integrationStore.getGraphicalB" :alignment="alignment" :enableClickR="true"
                       @elementClick="setAlignmentB"></Graph>
              </div>
            </div>
            <div class="q-pt-md">
              <div class="q-gutter-md">
                <q-input outlined v-model="alignment.type" prefix="Type: " disable dense/>
                <q-input outlined v-model="alignment.resourceB.label" prefix="Label:" disable dense/>
              </div>
            </div>
          </q-card-section>

        </div>
        <q-card-section class="row justify-between q-pt-none">
          <div class="col-6">
            <q-input outlined v-model="alignment.integratedLabel" prefix="Integrated label: " dense/>
          </div>
        </q-card-section>
        <q-card-section class="row justify-between q-pt-none">
          <div class="col-12" align="right">
            <q-btn outline color="primary" label="Close" @click="close"/>
            <q-btn class="q-ml-sm" color="primary" label="Add alignment" @click="addAlignment" :disable="alignment.integratedLabel == ''"/>
          </div>
        </q-card-section>
      </q-card-section>
    </q-card-section>
  </q-card>

</template>

<script setup>
import {ref, reactive} from "vue";
import Graph from 'components/graph/Graph.vue'
import {useIntegrationStore} from 'src/stores/integrationStore.js'
import {useProjectsStore} from 'src/stores/projectsStore.js'

const integrationStore = useIntegrationStore()
const projectsStore = useProjectsStore()

const props = defineProps({
  dsA: {type: Object, default: {id: "", name: "", type: "", graphicalGraph: "", iri: "", path: ""}},
  dsB: {type: Object, default: {id: "", name: "", type: "", graphicalGraph: "", iri: "", path: ""}},
  show_dialog: {type: Boolean, default: false}
});

const alignment = reactive({
  type: '', //both resource must be of the same type
  trueType: '',
  shortType: '',
  integratedLabel: '',
  similarity:1.0,
  resourceA: {
    name: '',
    label: '',
    iri: '',
  },
  resourceB: {
    name: '',
    label: '',
    iri: '',
  }
})

const alertAlignmentType = ref(false)

const emit = defineEmits(["close-dialog", "add-alignment"])

const close = () => {
  emit('close-dialog')
  resetRA()
  resetRB()
}

const resetRA = () => {
  alignment.resourceA.name = ''
  alignment.resourceA.label = ''
  alignment.resourceA.iri = ''

  if (alignment.resourceB.iri == '') {
    alignment.type = ''
    alignment.trueType = '',
    alignment.shortType = '',
    alignment.integratedLabel = ''
  }
}

const resetRB = () => {
  alignment.resourceB.name = ''
  alignment.resourceB.label = ''
  alignment.resourceB.iri = ''

  if (alignment.resourceA.iri == '') {
    alignment.type = ''
    alignment.trueType = '',
    alignment.shortType = '',
    alignment.integratedLabel = ''
  }
}

const addAlignment = () => {
  integrationStore.addAlignment(alignment, true)
  resetRA()
  resetRB()
}

const setAlignmentA = (resource) => {
  if (resource.event == 'unfocused') { // we want to unselect an element
    resetRA()
  } else if (alignment.type == '' || alignment.type == resource.type) {
    alignment.resourceA.label = resource.label
    alignment.resourceA.iri = resource.iri
    if (alignment.type != '') {
      alignment.integratedLabel = alignment.resourceA.label + "_" + alignment.resourceB.label
    }
    alignment.type = resource.type
    alignment.shortType = resource.shortType
    alignment.trueType = resource.trueType
  } // if the types are not compatible, the Graph component already shows an alert
}

const setAlignmentB = (resource) => {
  if (resource.event == 'unfocused') { // we want to unselect an element
    resetRB()
  } else if (alignment.type == '' || alignment.type == resource.type) {
    alignment.resourceB.label = resource.label
    alignment.resourceB.iri = resource.iri
    if (alignment.type != '') {
      alignment.integratedLabel = alignment.resourceA.label + "_" + alignment.resourceB.label
    }
    alignment.type = resource.type
    alignment.shortType = resource.shortType
    alignment.trueType = resource.trueType
  } // if the types are not compatible, the Graph component already shows an alert
}

</script>