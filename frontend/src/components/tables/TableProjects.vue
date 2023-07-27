<template>

  <q-table :grid="gridEnable" :rows="projectsStore.projects" :columns="columns" :class="{ 'no-shadow': no_shadow }"
          style="height: 600px"
          virtual-scroll
          v-model:pagination="pagination"
          :rows-per-page-options="[0]"
          row-key="id"
           no-data-label="Consider adding some alignments to start the integration process"
           no-results-label="The filter didn't uncover any results" >
    <template v-slot:top-left>
      <div class="q-table__title">
       <span> {{ title }}  </span>
        <q-btn padding="none" color="secondary" icon="add" @click="showDialog = true"/>
      </div>
    </template>

    <template v-slot:top-right="props">

<!--      <q-input outlined dense debounce="400" color="primary" v-model="search">-->
<!--        <template v-slot:append>-->
<!--          <q-icon name="search"/>-->
<!--        </template>-->
<!--      </q-input>-->
<!-- <q-btn flat padding="xs" icon="list" color="primary600 " style="background-color:#F6F6F9"></q-btn> -->
                <q-btn unelevated padding="none" color="primary700" icon="list"
                        @click="gridEnable = !gridEnable" />

      <!-- <q-btn flat round dense :icon="props.inFullscreen ? 'fullscreen_exit' : 'fullscreen'"
             @click="props.toggleFullscreen">
        <q-tooltip :disable="$q.platform.is.mobile" v-close-popup>
          {{ props.inFullscreen ? "Exit Fullscreen" : "Toggle Fullscreen" }}
        </q-tooltip>
      </q-btn> -->
    </template>

    <!-- <template v-slot:body-cell-actions="props">
      <q-td :props="props">
        <q-btn dense round flat color="grey" @click="deleteRow(props)" icon="delete"></q-btn>
      </q-td>
    </template>

    <template v-slot:body-cell-identifier="props">
      <q-td :props="props">
        hola{{props.row.identifier}}
        <q-checkbox v-model="props.row.identifier" color="teal" />
      </q-td>
    </template> -->

    <template v-slot:item="props">
        <div v-if="props.rowIndex==0" class="q-pa-md col-xs-12 col-sm-6 col-md-2 col-lg-2">
            <AddFolder @add-folder="showDialog=!showDialog"></AddFolder>
        </div>
        <div class="q-pa-md col-xs-12 col-sm-6 col-md-2 col-lg-2">


            <Folder :row="props.row" :folderColor="props.row.color"></Folder>
          <!-- <q-card>
            <q-card-section class="text-center">
              Calories for
              <br>
              <strong>{{ props.row.name }}</strong>
            </q-card-section>
            <q-separator />
            <q-card-section class="flex flex-center" :style="{ fontSize: props.row.calories + 'px' }">
              <div>{{ props.row.calories }} g</div>
            </q-card-section>
          </q-card> -->
        </div>
      </template>


    <template v-slot:no-data="{ icon, message, filter }">
      <div class="full-width row flex-center text-accent q-gutter-sm q-pa-xl" style="flex-direction: column">
<!--        <q-icon size="2em" name="sentiment_dissatisfied"/>-->
        <!-- <span> {{ message }} </span> -->
        <svg width="160px" height="88px" viewBox="0 0 216 120" fill="none" xmlns="http://www.w3.org/2000/svg" class="sc-jIkXHa sc-ZOtfp fXAzWm jPTZgW"><g opacity="0.84" clip-path="url(#EmptyDocuments_svg__clip0_1142_57509)"><path fill-rule="evenodd" clip-rule="evenodd" d="M189.25 19.646a7.583 7.583 0 010 15.166h-43.333a7.583 7.583 0 010 15.167h23.833a7.583 7.583 0 010 15.167h-11.022c-5.28 0-9.561 3.395-9.561 7.583 0 1.956 1.063 3.782 3.19 5.48 2.017 1.608 4.824 1.817 7.064 3.096a7.583 7.583 0 01-3.754 14.174H65.75a7.583 7.583 0 010-15.166H23.5a7.583 7.583 0 110-15.167h43.333a7.583 7.583 0 100-15.167H39.75a7.583 7.583 0 110-15.166h43.333a7.583 7.583 0 010-15.167H189.25zm0 30.333a7.583 7.583 0 110 15.166 7.583 7.583 0 010-15.166z" fill="#D9D8FF" fill-opacity="0.8"></path><path fill-rule="evenodd" clip-rule="evenodd" d="M132.561 19.646l10.077 73.496.906 7.374a4.334 4.334 0 01-3.773 4.829l-63.44 7.789a4.333 4.333 0 01-4.83-3.772l-9.767-79.547a2.166 2.166 0 011.91-2.417l5.262-.59 63.655-7.162zM73.162 26.33l4.97-.557-4.97.557z" fill="#fff"></path><path d="M73.162 26.33l4.97-.557m54.429-6.127l10.077 73.496.906 7.374a4.334 4.334 0 01-3.773 4.829l-63.44 7.789a4.333 4.333 0 01-4.83-3.772l-9.767-79.547a2.166 2.166 0 011.91-2.417l5.262-.59 63.655-7.162z" stroke="#7B79FF" stroke-width="2.5"></path><path fill-rule="evenodd" clip-rule="evenodd" d="M129.818 24.27l9.122 66.608.82 6.682c.264 2.153-1.246 4.11-3.373 4.371l-56.812 6.976c-2.127.261-4.066-1.272-4.33-3.425l-8.83-71.908a2.167 2.167 0 011.887-2.415l7.028-.863" fill="#F0F0FF"></path><path fill-rule="evenodd" clip-rule="evenodd" d="M135.331 5.833H85.978a2.97 2.97 0 00-2.107.873A2.97 2.97 0 0083 8.813v82.333c0 .823.333 1.567.872 2.106a2.97 2.97 0 002.107.873h63.917a2.97 2.97 0 002.106-.873 2.97 2.97 0 00.873-2.106V23.367a2.98 2.98 0 00-.873-2.107L137.437 6.705a2.98 2.98 0 00-2.106-.872z" fill="#fff" stroke="#7B79FF" stroke-width="2.5"></path><path d="M135.811 7.082v12.564a3.25 3.25 0 003.25 3.25h8.595M94.644 78.146h28.167m-28.167-55.25h28.167-28.167zm0 13h46.584-46.584zm0 14.083h46.584-46.584zm0 14.084h46.584-46.584z" stroke="#7B79FF" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"></path></g><defs><clipPath id="EmptyDocuments_svg__clip0_1142_57509"><path fill="#fff" d="M0 0h216v120H0z"></path></clipPath></defs></svg>
        <p style="color: rgb(102, 102, 135);font-weight: 500;font-size: 1rem;line-height: 1.25;">No alignments added. Check classes color fixed to system</p>
<!--

     <q-icon size="2em" :name="filter ? 'filter_b_and_w' : icon"/>-->
      </div>
    </template>
  </q-table>

  <!-- <SelectAlignments :show_dialog="show_dialog" :ds-a="selectedDS[0]" :ds-b="selectedDS[1]"  @close-dialog="show_dialog=false" @add-alignment="addRow"/> -->
  <!-- <q-dialog v-model="show_dialog" full-height full-width>


  </q-dialog> -->

  <q-dialog v-model="showDialog" >

        <q-card flat bordered class="my-card" style="min-width: 30vw;">
            <q-card-section>
                <div class="text-h6">New project</div>
            </q-card-section>

            <q-card-section class="q-pt-none">
                 <AddFolderForm @submit-success="showDialog=false" @cancel-form="showDialog=false"></AddFolderForm>
            </q-card-section>


    </q-card>


  </q-dialog>

</template>

<script setup>
import {computed, ref, onMounted} from 'vue'
import {useQuasar} from "quasar";
import { useProjectsStore } from 'stores/projects.store.js'
import AddFolderForm from 'components/forms/AddFolderForm.vue';
import Folder from 'components/ui/Folder.vue'
import AddFolder from 'components/ui/AddFolder.vue'


const props = defineProps({
    no_shadow: {type: Boolean, default: false},
    alignments: {type :Array, default: []}
});
const projectsStore = useProjectsStore()

const gridEnable = ref(true)
// const emit = defineEmits(['update:alignments'])
const showDialog = ref(false)
const pagination = ref({
        rowsPerPage: 0
      })
// const store = useStore()
    const $q = useQuasar()


    const columns = [
      {name: "id", required: true, label: "ID", align: "center", field: "projectId", sortable: true,},
      {name: "name", required: true, label: "Name", align: "center", field: "projectName", sortable: true,},
      {name: "datasets", required: true, label: "# Datasources", align: "center", field: "datasets", sortable: true, format: (value, row) => value.length},
      {name: "createdBy", required: true, label: "Created by", align: "center", field: "createdBy", sortable: true,},
      {name: "privacy", required: true, label: "Privacy", align: "center", field: "projectPrivacy", sortable: true,}
    ];

    // const projects = ref([])

    onMounted(() => {
      projectsStore.getProjects()
//       console.log(projectsStore.projects)
//       console.log("++d",projectsStore.projects.filter(function (el) {
//   return el.name === 'Survey_NextiaDI'
// }))

    } )

    const title = "Projects";


</script>

<style lang="scss">

</style>
