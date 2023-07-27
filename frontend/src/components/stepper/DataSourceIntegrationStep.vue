<template>
  <q-stepper style="width:100%;margin-top:15px" v-model="step" ref="stepper" color="primary" animated class="no-padding-stepper">

        <q-step :name="1" title="Uploaded data sources" icon="settings" :done="step > 1" style="min-height: 70vh">
          Here are the uploaded data sources that have not yet been integrated into the project
          <TableTemporalDataSources :no_shadow="true" ></TableTemporalDataSources>
        </q-step>

        <!-- v-if="integrationStore.project.numberOfDS == '0'" -->
        <!-- <div> -->
        <q-step :name="2" title="Preview data source" icon="settings" :done="step > 1" style="min-height: 70vh;height: 1px" id="previewSourceStep">
          <!-- For each ad campaign that you create, you can control how much you're willing to -->
          <!-- spend on clicks and conversions, which networks and geographical locations you want -->
          <!-- your ads to show on, and more. -->


          <div class="column items-center " style="height: 100%;">
            <!-- <div class="col-6">
              <div>
               <json-viewer :value="jsonData"></json-viewer>
              </div>

            </div> -->
            <div class="col-8" style="height: 100%;min-width: 75vw;">
              <Graph :graphical="integrationStore.getGraphicalB"></Graph>
            </div>
         </div>




          <!-- <CSVPreview></CSVPreview> -->
        </q-step>
        <!-- </div> -->


        <q-step v-if="integrationStore.project.datasets.length != 0" :name="3" title="Integrate with project" icon="create_new_folder" :done="step > 2" style="min-height: 70vh">
          <!-- <q-input outlined v-model="integratedName" label="Integrated datasource name" placeholder="Type a name for the integrated source" /> -->

          <TableAligments :no_shadow="true" />
          <!-- :alignments.sync="alignments" -->
        </q-step>

        <q-step v-if="integrationStore.project.datasets.length != 0" :name="4" title="Review alignments" icon="create_new_folder" :done="step > 3" style="min-height: 70vh">

          The following alignments cannot be integrated as their entity domains are not integrated. Delete them or indicate the relationships of their entity domains to integrate them.
          <TableJoinAlignments :no_shadow="true"></TableJoinAlignments>
        </q-step>


        <q-step v-if="integrationStore.project.datasets.length != 0" :name="5" title="Preview integration" icon="settings" style="min-height: 70vh;height: 1px" id="previewIntegration">
          <div class="row" style="height: 92%;" >
            <div class="col-12">
             <!-- hola {{integrationStore.getGlobalSchema}} -->
             This is a preview of the global schema generated. Note that green elements are integrated resources. If you would like to see the schema integrated, use the toggle to visualize how source schemas are connected.
             <q-toggle :label="previewGS" false-value="Schema integrated" true-value="Global schema" v-model="previewGS"/>

              <Graph :graphical="previewGS == 'Global schema' ? integrationStore.getGlobalSchema: integrationStore.getGraphicalSchemaIntegration"></Graph>
            </div>
         </div>
        </q-step>

        <template v-slot:navigation>
          <q-stepper-navigation class="">
            <!-- $refs.stepper.previous -->
            <q-btn v-if="step > 1" flat color="primary" @click="previousStep()"   :label="step === 4 ? 'Back' : 'Back'" class="q-ml-sm"/>

            <q-btn class="q-ml-sm" @click="clickOk" :disable="disableStepBtn()" color="primary" :label="stepLabel()"/>
            <!-- <q-btn @click="" color="primary" label="Delete"/> -->
                      </q-stepper-navigation>
        </template>
    </q-stepper>

</template>

<script setup>
import {  ref, onBeforeMount, onMounted } from '@vue/runtime-core'
// import CSVPreview from 'components/previews/CSVPreview.vue';
import TableAligments from 'components/tables/TableAligments.vue';
import TableJoinAlignments from 'components/tables/TableJoinAlignments.vue';
import TableTemporalDataSources from "components/tables/TableTemporalDataSources.vue"
import Graph from 'components/graph/Graph.vue'
import { useDataSourceStore } from 'src/stores/datasources.store.js'
import { useIntegrationStore } from 'src/stores/integration.store.js'


// -------------------------------------------------------------
//                         PROPS & EMITS
// -------------------------------------------------------------

// -------------------------------------------------------------
//                         STORES & GLOBALS
// -------------------------------------------------------------
const dataSourceStore = useDataSourceStore();
const integrationStore = useIntegrationStore();

onMounted( () => {
  dataSourceStore.setProject()
    integrationStore.setProject()
})


const step = ref(1)
if(integrationStore.selectedDS.length > 0) {
  step.value = 2
}
// -------------------------------------------------------------
//                         Others
// -------------------------------------------------------------
const previewGS = ref('Global schema')

// import JsonViewer from 'vue-json-viewer'
  // const props = defineProps({
  //     step : { default:ref(1)}
  // });

// const jsonData = {hola:"123", jsd:"23"}
// const graphical = "{\"nodes\":[{\"id\":\"Class1\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks.title\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"type\":\"objectProperty\",\"label\":\"title\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"linkId\":\"Link1\"},{\"id\":\"Class2\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks.createdAt\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"type\":\"objectProperty\",\"label\":\"createdAt\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"linkId\":\"Link2\"},{\"id\":\"Class3\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums\",\"iriType\":\"http://www.w3.org/2000/01/rdf-schema#Class\",\"type\":\"class\",\"label\":\"ds1_museums\"},{\"id\":\"Class4\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks.idObject\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"type\":\"objectProperty\",\"label\":\"idObject\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"linkId\":\"Link3\"},{\"id\":\"Class5\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks.domain\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"type\":\"objectProperty\",\"label\":\"domain\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"linkId\":\"Link4\"},{\"id\":\"Class6\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.location\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"type\":\"objectProperty\",\"label\":\"location\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"linkId\":\"Link5\"},{\"id\":\"Class8\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks\",\"iriType\":\"http://www.w3.org/2000/01/rdf-schema#Class\",\"type\":\"class\",\"label\":\"artworks\"},{\"id\":\"Class9\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.ContainerMembershipProperty1\",\"iriType\":\"http://www.w3.org/2000/01/rdf-schema#ContainerMembershipProperty\",\"type\":\"objectProperty\",\"label\":\"ContainerMembershipProperty1\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1\",\"range\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks\",\"linkId\":\"Link6\"},{\"id\":\"Class10\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.museum\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"type\":\"objectProperty\",\"label\":\"museum\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"linkId\":\"Link7\"},{\"id\":\"Class11\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.has_artworks\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"type\":\"objectProperty\",\"label\":\"has_artworks\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums\",\"range\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1\",\"linkId\":\"Link8\"},{\"id\":\"Class12\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.category\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"type\":\"objectProperty\",\"label\":\"category\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"linkId\":\"Link9\"},{\"id\":\"Class13\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq\",\"type\":\"class\",\"label\":\"Seq1\"},{\"id\":\"Class14\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks.madeBy\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"type\":\"objectProperty\",\"label\":\"madeBy\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/598177e0e52c44299b9017f5f65604b8/ds1_museums.Seq1.artworks\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"linkId\":\"Link10\"},{\"id\":\"Datatype15\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"type\":\"xsdType\",\"label\":\"string\"},{\"id\":\"Datatype16\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"type\":\"xsdType\",\"label\":\"string\"},{\"id\":\"Datatype17\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"type\":\"xsdType\",\"label\":\"string\"},{\"id\":\"Datatype18\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"type\":\"xsdType\",\"label\":\"string\"},{\"id\":\"Datatype19\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"type\":\"xsdType\",\"label\":\"string\"},{\"id\":\"Datatype20\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"type\":\"xsdType\",\"label\":\"string\"},{\"id\":\"Datatype21\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"type\":\"xsdType\",\"label\":\"string\"},{\"id\":\"Datatype22\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"type\":\"xsdType\",\"label\":\"string\"}],\"links\":[{\"id\":\"Link1\",\"source\":\"Class8\",\"target\":\"Datatype15\",\"label\":\"title\"},{\"id\":\"Link2\",\"source\":\"Class8\",\"target\":\"Datatype16\",\"label\":\"createdAt\"},{\"id\":\"Link3\",\"source\":\"Class8\",\"target\":\"Datatype17\",\"label\":\"idObject\"},{\"id\":\"Link4\",\"source\":\"Class8\",\"target\":\"Datatype18\",\"label\":\"domain\"},{\"id\":\"Link5\",\"source\":\"Class3\",\"target\":\"Datatype19\",\"label\":\"location\"},{\"id\":\"Link6\",\"source\":\"Class13\",\"target\":\"Class8\",\"label\":\"ContainerMembershipProperty1\"},{\"id\":\"Link7\",\"source\":\"Class3\",\"target\":\"Datatype20\",\"label\":\"museum\"},{\"id\":\"Link8\",\"source\":\"Class3\",\"target\":\"Class13\",\"label\":\"has_artworks\"},{\"id\":\"Link9\",\"source\":\"Class3\",\"target\":\"Datatype21\",\"label\":\"category\"},{\"id\":\"Link10\",\"source\":\"Class8\",\"target\":\"Datatype22\",\"label\":\"madeBy\"}]}"


  const disableStepBtn = () =>{
    console.log("step val: ", step.value)
      switch (step.value){
        case 1: // list uploaded data sources
          console.log("return true")
          return integrationStore.selectedDS.length != 1;
          // return integrationStore.selectedDS.length != 2
          // return selectedDS.value.filter(v => (v.graphicalGraph  || v.graphicalMinimalIntegration  ) ).length != 2
          return false;
          break;
        case 2: // preview bootstrapped graph
          // return alignments.value.length == 0
          break;
        case 3: // selection of alignments
          if(integrationStore.alignments.length == 0)
            return true;
          return false;
          break;
        case 4: // review of alignments

          return !integrationStore.isJoinAlignmentsRelationshipsComplete;
          break;
        default: //present final integration
          return false;
      }
    }

 const stepLabel = () =>{
      switch (step.value) {
        case 2:
          if(integrationStore.project.datasets.length <= 1)
            return "Finish"
          return "Continue"
        case 4:
          return "Integrate"
        case 5:
          return "Finish"
        default:
          return "Continue"
          break;
      }
    }

    const previousStep = () => {

      if(integrationStore.joinAlignments.length == 0 && step.value == 5) {
        step.value = 3
      } else {
        step.value--
      }



    }

   const clickOk = () => {
      switch (step.value){
        case 1:
            step.value++
          break;
        case 2:
          console.log(integrationStore.project.datasets.length+" +++++++++++++++++++++++++++++++++ numero de datasets")
          if(integrationStore.project.datasets.length == 1  ){
            // we persist data source
            console.log("finish preview...")
           dataSourceStore.finishPreview()

          } else {
            console.log("moving to integrate with project")
            step.value++
          }
          break;
        case 3:

        console.log("integrate with project. Step value", step.value)
        integrationStore.integrateTemporal( function () {

          if( integrationStore.joinAlignments.length == 0 ) {
            step.value = 5
          } else {
            step.value++
          }
        } )
        // step.value++
        //
          // emit("finished")
          // return false;
          break;

        case 4:

          console.log("step 4 review alignments")
          integrationStore.integrateJoins( function () { step.value++ } )

          break;
        default:
          //last step
          console.log("step 4 save integration")
            integrationStore.saveIntegration(dataSourceStore.persistDataSource)
      }
   }


</script>

<style lang="scss">

#previewIntegration  > .q-stepper__step-content, #previewSourceStep > .q-stepper__step-content{
  height: 100%;


  .q-stepper__step-inner{
    height: 100%;
    // height: 90%;
  }
}

</style>
