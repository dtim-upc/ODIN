<template>


    <q-card flat bordered>
      <q-item class="q-py-none">

        <q-item-section>
          <q-item-label class="dark_title">Select alignments</q-item-label>
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
                Project: {{ integrationStore.project.projectName }}
              </div>
              <!-- <q-responsive :ratio="4/3" style="max-height: 53vh">
                <Webvowl :view="'bdi_manual_alignments'" :id="dsA.id" :minimal-i="dsA.type == 'INTEGRATED'? true: false"/>
              </q-responsive> -->
              <div class="row" style="min-height:50vh ;border: 1px #e4eaec;border-style: solid;" >
                <div class="col">
                <!-- integrationStore.selectedDS -->
                    <Graph :graphical="integrationStore.getGraphicalA" :alignment="alignment" :enableClickR="true"  @elementClick="setAlignmentA"></Graph>
                </div>
                <!-- <div class="col-6">
                    <Graph :graphical="graphical" ></Graph>
                </div> -->
                </div>
              <div class="q-pt-md">

                <div class="q-gutter-md">

                  <q-input outlined v-model="alignment.type" prefix="Type: " disable dense/>
                  <q-input outlined v-model="alignment.resourceA.label" prefix="Label: " disable dense/>
                </div>

              </div>
            </q-card-section>

            <q-card-section class="col q-pl-none">
              <div class="row bg-primary justify-center text-white q-pa-xs"> DataSource:  {{ integrationStore.getSourceB.name  }}</div>
              <!-- <q-responsive :ratio="4/3" style="max-height: 53vh">
                <Webvowl :view="'bdi_manual_alignments'" :id="dsB.id"   :minimal-i="dsB.type == 'INTEGRATED'? true: false"/>
              </q-responsive> -->
               <div class="row" style="min-height:50vh ;">
                <div class="col">
                    <Graph :graphical="integrationStore.getGraphicalB" :alignment="alignment"  :enableClickR="true" @elementClick="setAlignmentB"></Graph>
                </div>
                <!-- <div class="col-6">
                    <Graph :graphical="graphical" ></Graph>
                </div> -->
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
            <!--                  <div>-->
            <div class="col-6">
              <q-input outlined v-model="alignment.integratedLabel" prefix="Integrated label: " dense />
              <!-- <q-checkbox v-model="identifier" label="Make it identifier" color="teal" /> -->
            </div>
            <div class="col-3">
              <q-btn outline color="primary" label="Close" @click="close"/>
              <q-btn class="q-ml-sm" color="primary" label="Add alignment" @click="addAlignment" :disable="alignment.integratedLabel == ''"/>

            </div>

          </q-card-section>
        </q-card-section>
      </q-card-section>
    </q-card>

</template>

<script setup>
import {ref, reactive, onMounted} from "vue";
import Graph from 'components/graph/Graph.vue'
import { useIntegrationStore } from 'src/stores/integration.store.js'

const props = defineProps({
    dsA: {type: Object, default: {id: "", name: "", type: "", graphicalGraph: "", iri: "", path: ""}},
    dsB: {type: Object, default: {id: "", name: "", type: "", graphicalGraph: "", iri: "", path: ""}},
    show_dialog: {type: Boolean, default: false}
});

const integrationStore = useIntegrationStore()

// onMounted(() => {
//   integrationStore.setProject()
// })

const alignment = reactive({

    type : '', //both resource must be same type
    trueType: '',
    shortType: '',
    integratedLabel : '',
    resourceA : {
        name:'',
        label: '',
        iri: '',
    },
    resourceB: {
        name:'',
        label: '',
        iri: '',
    }

})



// const resourceA =  reactive({
//     dsID: '',
//     name:'',
//     label: '',
//     iri: '',
//     type: ''
// })

// const resourceB =  reactive({
//     dsID: '',
//     name:'',
//     label: '',
//     iri: '',
//     type: ''
// })


const alertAlignmentType = ref(false)
const fullscreen = ref(false)
const disableAdd = ref(true)
const identifier = ref(false)

// const graphical = "{\"nodes\":[{\"id\":\"Class1\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks.title\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"type\":\"objectProperty\",\"label\":\"title\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"linkId\":\"Link1\"},{\"id\":\"Class2\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks.createdAt\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"type\":\"objectProperty\",\"label\":\"createdAt\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"linkId\":\"Link2\"},{\"id\":\"Class3\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums\",\"iriType\":\"http://www.w3.org/2000/01/rdf-schema#Class\",\"type\":\"class\",\"label\":\"ds1_museums\"},{\"id\":\"Class4\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks.idObject\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"type\":\"objectProperty\",\"label\":\"idObject\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"linkId\":\"Link3\"},{\"id\":\"Class5\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks.domain\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"type\":\"objectProperty\",\"label\":\"domain\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"linkId\":\"Link4\"},{\"id\":\"Class6\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.location\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"type\":\"objectProperty\",\"label\":\"location\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"linkId\":\"Link5\"},{\"id\":\"Class8\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks\",\"iriType\":\"http://www.w3.org/2000/01/rdf-schema#Class\",\"type\":\"class\",\"label\":\"artworks\"},{\"id\":\"Class9\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.ContainerMembershipProperty1\",\"iriType\":\"http://www.w3.org/2000/01/rdf-schema#ContainerMembershipProperty\",\"type\":\"objectProperty\",\"label\":\"ContainerMembershipProperty1\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1\",\"range\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks\",\"linkId\":\"Link6\"},{\"id\":\"Class10\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.museum\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"type\":\"objectProperty\",\"label\":\"museum\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"linkId\":\"Link7\"},{\"id\":\"Class11\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.has_artworks\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"type\":\"objectProperty\",\"label\":\"has_artworks\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums\",\"range\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1\",\"linkId\":\"Link8\"},{\"id\":\"Class12\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.category\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"type\":\"objectProperty\",\"label\":\"category\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"linkId\":\"Link9\"},{\"id\":\"Class13\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq\",\"type\":\"class\",\"label\":\"Seq1\"},{\"id\":\"Class14\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks.madeBy\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"type\":\"objectProperty\",\"label\":\"madeBy\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"linkId\":\"Link10\"},{\"id\":\"Datatype15\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"type\":\"xsdType\",\"label\":\"string\"},{\"id\":\"Datatype16\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"type\":\"xsdType\",\"label\":\"string\"},{\"id\":\"Datatype17\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"type\":\"xsdType\",\"label\":\"string\"},{\"id\":\"Datatype18\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"type\":\"xsdType\",\"label\":\"string\"},{\"id\":\"Datatype19\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"type\":\"xsdType\",\"label\":\"string\"},{\"id\":\"Datatype20\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"type\":\"xsdType\",\"label\":\"string\"},{\"id\":\"Datatype21\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"type\":\"xsdType\",\"label\":\"string\"},{\"id\":\"Datatype22\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"type\":\"xsdType\",\"label\":\"string\"}],\"links\":[{\"id\":\"Link1\",\"source\":\"Class8\",\"target\":\"Datatype15\",\"label\":\"title\"},{\"id\":\"Link2\",\"source\":\"Class8\",\"target\":\"Datatype16\",\"label\":\"createdAt\"},{\"id\":\"Link3\",\"source\":\"Class8\",\"target\":\"Datatype17\",\"label\":\"idObject\"},{\"id\":\"Link4\",\"source\":\"Class8\",\"target\":\"Datatype18\",\"label\":\"domain\"},{\"id\":\"Link5\",\"source\":\"Class3\",\"target\":\"Datatype19\",\"label\":\"location\"},{\"id\":\"Link6\",\"source\":\"Class13\",\"target\":\"Class8\",\"label\":\"ContainerMembershipProperty1\"},{\"id\":\"Link7\",\"source\":\"Class3\",\"target\":\"Datatype20\",\"label\":\"museum\"},{\"id\":\"Link8\",\"source\":\"Class3\",\"target\":\"Class13\",\"label\":\"has_artworks\"},{\"id\":\"Link9\",\"source\":\"Class3\",\"target\":\"Datatype21\",\"label\":\"category\"},{\"id\":\"Link10\",\"source\":\"Class8\",\"target\":\"Datatype22\",\"label\":\"madeBy\"}]}"


const emit = defineEmits(["close-dialog","add-alignment"])

const close = () => {
    emit('close-dialog')
    resetRA()
    resetRB()
    //   this.resetLabelsA()
    //   this.resetLabelsB()
    }

const resetRA = () => {

  alignment.resourceA.name = ''
  alignment.resourceA.label = ''
  alignment.resourceA.iri = ''

  if( alignment.resourceB.iri == '' ) {
    alignment.type=''
    alignment.trueType= '',
    alignment.shortType= '',
    alignment.integratedLabel = ''
  }
 }

const resetRB = () => {

  alignment.resourceB.name = ''
  alignment.resourceB.label = ''
  alignment.resourceB.iri = ''

  if( alignment.resourceA.iri == '' ) {
    alignment.type=''
    alignment.trueType= '',
    alignment.shortType= '',
     alignment.integratedLabel = ''
  }
 }


const addAlignment = () => {

    integrationStore.addAligment(alignment, true)
    resetRA()
    resetRB()

    //   this.$emit("add-alignment", {
    //     row: {iriA: this.selectedA_iri, iriB: this.selectedB_iri, l: this.integratedLabel, type: this.selectedA_type, identifier:this.identifier}
    //   })
    //   this.resetLabelsA()
    //   this.resetLabelsB()

    }


const setAlignmentA = (resource) => {

  if(resource.event == 'unfocused') {
    resetRA()
  } else if(alignment.type == '' || alignment.type == resource.type ) {

    alignment.resourceA.label = resource.label
    alignment.resourceA.iri = resource.iri
        if(alignment.type != '') {
      alignment.integratedLabel = alignment.resourceA.label + "_" + alignment.resourceB.label
    }
    alignment.type = resource.type
    alignment.shortType = resource.shortType
    alignment.trueType = resource.trueType


  } else {

    // show some error. we can only select same type

  }




}


const setAlignmentB = (resource) => {

    // if(resource.id == resourceA.id) {
      // console.log("alig")
  if(resource.event == 'unfocused') {
    resetRB()
  } else if(alignment.type == '' || alignment.type == resource.type ) {
    alignment.resourceB.label = resource.label
    alignment.resourceB.iri = resource.iri
    if(alignment.type != '') {
      alignment.integratedLabel = alignment.resourceA.label + "_" + alignment.resourceB.label
    }
    alignment.type = resource.type
    alignment.shortType = resource.shortType
    alignment.trueType = resource.trueType

  } else {
    // show some error. we can only select same type
  }

}



</script>

<style>


</style>
