<template>
    <!-- <div class="q-pa-md"> -->

<!-- @selection="validateSelection2" -->
<!-- v-model:selected="selecti" -->
 <!-- @update:selected="setSelected" -->
        <q-table ref="tableRef" :rows="integrationStore.datasources" :columns="columns" :filter="search" selection="multiple"
            :class="{ 'no-shadow': no_shadow }" :selected="integrationStore.selectedDS" @selection="validateSelection"   row-key="id"
            no-data-label="I didn't find anything for you. Consider creating a new data source."
            no-results-label="The filter didn't uncover any results" :visible-columns="visibleColumns">

            <template v-slot:top-left="">
                <div class="q-table__title">
                    {{ title }}
                    <q-btn unelevated padding="none" color="primary700" icon="add" @click="addDataSource = true" />
                    <!-- <q-btn unelevated v-if="view === 'datasources'" padding="none" color="primary700" icon="add"
                        @click="addDataSource = true" /> -->
                </div>

            </template>

            <template v-slot:top-right="props">
                <q-input outlined dense debounce="400" color="primary" v-model="search">
                    <template v-slot:append>
                        <q-icon name="search" />
                    </template>
                </q-input>


                <q-btn flat round dense :icon="props.inFullscreen ? 'fullscreen_exit' : 'fullscreen'"
                    @click="props.toggleFullscreen">
                    <q-tooltip :disable="$q.platform.is.mobile" v-close-popup>
                        {{ props.inFullscreen ? "Exit Fullscreen" : "Toggle Fullscreen" }}
                    </q-tooltip>
                </q-btn>
            </template>


            <template v-slot:body-cell-status="props">
                <q-td :props="props">
                    <q-chip text-color="white" color="accent" v-if="props.row.graphicalGraph === ''">
                        Missing Data Sources
                    </q-chip>
                    <q-chip text-color="white" color="blue" v-else> Completed</q-chip>
                </q-td>
            </template>

            <template v-slot:body-cell-View_Source_Graph="props">
                <q-td :props="props">
                    <q-btn dense round flat color="grey"
                        icon="download" @click="integrationStore.downloadSourceTemporal(props.row.id)"></q-btn>

                </q-td>
            </template>

            <template v-if="view === 'datasources'" v-slot:body-cell-actions="props">
                <q-td :props="props">
                    <!-- <q-btn dense round flat color="grey" :to="'/dataSources/view/' + props.row.id"
                        icon="remove_red_eye"></q-btn> -->
                    <!-- <q-btn dense round flat color="grey" @click="editRow(props)" icon="edit"></q-btn> -->
                    <q-btn dense round flat color="grey" @click="deleteRow(props.row)" icon="delete"></q-btn>
                </q-td>
            </template>

            <template v-slot:no-data="{ icon, message, filter }">
                <div class="full-width row flex-center text-accent q-gutter-sm q-pa-xl" style="flex-direction: column">
                    <svg width="160px" height="88px" viewBox="0 0 216 120" fill="none" xmlns="http://www.w3.org/2000/svg" class="sc-jIkXHa sc-ZOtfp fXAzWm jPTZgW"><g opacity="0.84" clip-path="url(#EmptyDocuments_svg__clip0_1142_57509)"><path fill-rule="evenodd" clip-rule="evenodd" d="M189.25 19.646a7.583 7.583 0 010 15.166h-43.333a7.583 7.583 0 010 15.167h23.833a7.583 7.583 0 010 15.167h-11.022c-5.28 0-9.561 3.395-9.561 7.583 0 1.956 1.063 3.782 3.19 5.48 2.017 1.608 4.824 1.817 7.064 3.096a7.583 7.583 0 01-3.754 14.174H65.75a7.583 7.583 0 010-15.166H23.5a7.583 7.583 0 110-15.167h43.333a7.583 7.583 0 100-15.167H39.75a7.583 7.583 0 110-15.166h43.333a7.583 7.583 0 010-15.167H189.25zm0 30.333a7.583 7.583 0 110 15.166 7.583 7.583 0 010-15.166z" fill="#D9D8FF" fill-opacity="0.8"></path><path fill-rule="evenodd" clip-rule="evenodd" d="M132.561 19.646l10.077 73.496.906 7.374a4.334 4.334 0 01-3.773 4.829l-63.44 7.789a4.333 4.333 0 01-4.83-3.772l-9.767-79.547a2.166 2.166 0 011.91-2.417l5.262-.59 63.655-7.162zM73.162 26.33l4.97-.557-4.97.557z" fill="#fff"></path><path d="M73.162 26.33l4.97-.557m54.429-6.127l10.077 73.496.906 7.374a4.334 4.334 0 01-3.773 4.829l-63.44 7.789a4.333 4.333 0 01-4.83-3.772l-9.767-79.547a2.166 2.166 0 011.91-2.417l5.262-.59 63.655-7.162z" stroke="#7B79FF" stroke-width="2.5"></path><path fill-rule="evenodd" clip-rule="evenodd" d="M129.818 24.27l9.122 66.608.82 6.682c.264 2.153-1.246 4.11-3.373 4.371l-56.812 6.976c-2.127.261-4.066-1.272-4.33-3.425l-8.83-71.908a2.167 2.167 0 011.887-2.415l7.028-.863" fill="#F0F0FF"></path><path fill-rule="evenodd" clip-rule="evenodd" d="M135.331 5.833H85.978a2.97 2.97 0 00-2.107.873A2.97 2.97 0 0083 8.813v82.333c0 .823.333 1.567.872 2.106a2.97 2.97 0 002.107.873h63.917a2.97 2.97 0 002.106-.873 2.97 2.97 0 00.873-2.106V23.367a2.98 2.98 0 00-.873-2.107L137.437 6.705a2.98 2.98 0 00-2.106-.872z" fill="#fff" stroke="#7B79FF" stroke-width="2.5"></path><path d="M135.811 7.082v12.564a3.25 3.25 0 003.25 3.25h8.595M94.644 78.146h28.167m-28.167-55.25h28.167-28.167zm0 13h46.584-46.584zm0 14.083h46.584-46.584zm0 14.084h46.584-46.584z" stroke="#7B79FF" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"></path></g><defs><clipPath id="EmptyDocuments_svg__clip0_1142_57509"><path fill="#fff" d="M0 0h216v120H0z"></path></clipPath></defs></svg>
                    <span style="color: rgb(102, 102, 135);font-weight: 500;font-size: 1rem;line-height: 1.25;">Data sources not found.</span>
                    <span style="color: rgb(102, 102, 135);font-weight: 500;font-size: 1rem;line-height: 1.25;">To integrate data sources with the project, please add at least two sources.</span>
                </div>
                </template>
        </q-table>

    <!-- <q-dialog v-model="addDataSource" >
      <StepNewDataSource style="max-width: calc(100vh - 48px)" @finished="addDataSource = false"/>
    </q-dialog>  -->
    <FormNewDataSource v-model:show="addDataSource"></FormNewDataSource>

    <!-- </div> -->
</template>


<script setup>
import {  onMounted, defineProps, ref } from "vue";
import { useIntegrationStore } from 'src/stores/integration.store.js'
import {useNotify} from 'src/use/useNotify.js'
import FormNewDataSource from "components/forms/FormNewDataSource.vue";


const props = defineProps({
    no_shadow: { type: Boolean, default: false },
    view: { type: String, default: "datasources" },
    selected: {type: String, default: ""}
});
const addDataSource = ref(false)



if(props.selected != "" ){
    console.log("complete code!!!")
    // selectedDS.value.push(props.selected)
}

 const notify = useNotify()
const integrationStore = useIntegrationStore();

onMounted( () => {
    integrationStore.setProject()
})

// select, name, tag, size, type -> owner, members -> delete, view local schema
const columns = [
    { name: "id", label: "Id", align: "center", field: "datasetId", sortable: true, },
    { name: "Name", label: "Name", align: "center", field: "datasetName", sortable: true, },
    { name: "Type", label: "Type", align: "center", field: "datasetType", sortable: true, },
    // {name: "#Wrappers", label: "#Wrappers", align: "center", field: "wrappers", sortable: true,},
    // { name: "View Metadata", label: "View Metadata", align: "center", field: "View Metadata", sortable: false, },
    {
        name: "View_Source_Graph", label: "Source Graph", align: "center", field: "View Source Graph",
        sortable: false,
    },
    { name: "actions", label: "actions", align: "center", field: "actions", sortable: false, },
];

const views = {
    "integration": ['Name', 'Type'],
    "datasources": ['Name', 'Type', '#Wrappers', 'View Metadata', 'View_Source_Graph', 'actions']
}



const title = "Landing Data Sources";
const search = ref("")

const visibleColumns = views[props.view]

const validateSelection = ({rows, added, evt}) => {

    let maxSelection = 1;
    // if(integrationStore.project.numberOfDS === "0" ){
    //     maxSelection = 2;
    // }

    console.log("validateSelection...", maxSelection)

    if (rows.length === 0) {
        console.log("not added")
        return
    } else if (integrationStore.selectedDS.length >= maxSelection && added) {
        // console.log("can only select 2")
        // show error, we can only select 2
        notify.negative("You can only select "+maxSelection+" datasources for integration")
      } else if (rows.length === 1) {

        const row = rows[0]
        console.log("selected is ")
        console.log(row)
        if (added) {
            integrationStore.addSelectedDatasource(row)
        } else {
            integrationStore.deleteSelectedDatasource(row)
        }

      } else {
        //  do nothing
      }



}


    const deleteRow = (row) => {

        integrationStore.deleteTemporalDS(row)

        // // odinApi.delete(`/dataSource/${props2.row.id}`)
        // api.deleteDS(props2.row.id)
        // .then((response) => {
        //   if (response.status == 204) {
        //     notify.positive("Successfully deleted")
        //     storeDS.deleteDataSource(props2.row)
        //     // store.dispatch('deleteDatasource', props2.row)
        //   } else {
        //     // 500
        //     notify.negative("Something went wrong in the server.")
        //   }
        // });

    }



</script>

<style lang="css" scoped>
</style>
