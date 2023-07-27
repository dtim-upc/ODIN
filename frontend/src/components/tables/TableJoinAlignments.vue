<template>

    <q-table :rows="integrationStore.joinAlignments" :columns="columns" :class="{ 'no-shadow': no_shadow }"
            :visible-columns="visibleCols" id="TableAlignments"
             row-key="uriA"
             virtual-scroll
             :rows-per-page-options="[0]"
             style="height: 60vh"
             v-model:pagination="pagination"
             no-data-label="Consider adding some alignments to start the integration process"
             no-results-label="The filter didn't uncover any results" >
      <template v-slot:top-left>
        <div class="q-table__title">
         <span> {{ title }}  </span>
        </div>
      </template>
  
      <template v-slot:top-right="props">
  
        <q-btn flat round dense :icon="props.inFullscreen ? 'fullscreen_exit' : 'fullscreen'"
               @click="props.toggleFullscreen">
          <q-tooltip :disable="$q.platform.is.mobile" v-close-popup>
            {{ props.inFullscreen ? "Exit Fullscreen" : "Toggle Fullscreen" }}
          </q-tooltip>
        </q-btn>
      </template>
  
      <!-- <template v-slot:body-cell-type="props">
        <q-td :props="props">
          <q-chip>{{props.row.type}}</q-chip>
          –
          <q-chip>{{props.row.type}}</q-chip>
        </q-td>
      </template> -->
  
      <template v-slot:body-cell-labelIntegrated="props">
        <q-td :props="props">
  
          <div>
            <span v-if="props.row.edit == null || !props.row.edit" >{{props.row.l}} 
              <q-icon @click="props.row.edit=true" size="xs" name="edit"/>
            </span>
            <q-input v-else  outlined v-model="props.row.l" dense   v-on:keyup.enter="props.row.edit = false;" >
              <template v-slot:append>
                  <q-icon name="o_task_alt" @click.stop.prevent="props.row.edit=false" class="cursor-pointer" />
              </template>
            </q-input>
          </div>
        </q-td>
      </template>
      <template v-slot:body-cell-actions="props">
        <q-td :props="props">
          <q-btn dense round flat color="grey" @click="deleteRow(props.row)" icon="delete"></q-btn>
        </q-td>
      </template>
  
      <template v-slot:body-cell-semantic="props">
        <q-td :props="props">
            <!-- v-model="model" :options="options" -->
            <div > 
                <div class="q-gutter-xs row items-start">

                
                    <q-input rounded filled v-model="props.row.domainLabelA" label="domainA" class="rounded-cell" dense readonly/>
                
                    <q-input filled v-model="props.row.relationship" label="Relationship" :rules="[ val => !val.includes(' ')|| 'space not supported']" dense>

                    <template v-slot:before>
                   
                    <q-checkbox v-model="props.row.rightArrow" checked-icon="arrow_forward_ios" unchecked-icon="arrow_back_ios_new" color="green" keep-color/>
                    </template>

                    <template v-slot:after>
      <q-checkbox v-model="props.row.rightArrow" checked-icon="arrow_forward_ios" unchecked-icon="arrow_back_ios_new" color="green" keep-color/>
                    </template>
                </q-input>
                <q-input rounded filled v-model="props.row.domainLabelB" label="domainB" class="rounded-cell" dense readonly/>
                

                </div>

                            </div>
            
        </q-td>
      </template>
  
      <template v-slot:no-data="{ icon, message, filter }">
        <div class="full-width row flex-center text-accent q-gutter-sm q-pa-xl" style="flex-direction: column">
  <!--        <q-icon size="2em" name="sentiment_dissatisfied"/>-->
          <!-- <span> {{ message }} </span> -->
          <img src="/assets/icons/AlignmentTable.svg" alt="No alignments added" style="width: 320px; height: 176px"/>
          <!-- <svg width="160px" height="88px" viewBox="0 0 216 120" fill="none" xmlns="http://www.w3.org/2000/svg" class="sc-jIkXHa sc-ZOtfp fXAzWm jPTZgW"><g opacity="0.84" clip-path="url(#EmptyDocuments_svg__clip0_1142_57509)"><path fill-rule="evenodd" clip-rule="evenodd" d="M189.25 19.646a7.583 7.583 0 010 15.166h-43.333a7.583 7.583 0 010 15.167h23.833a7.583 7.583 0 010 15.167h-11.022c-5.28 0-9.561 3.395-9.561 7.583 0 1.956 1.063 3.782 3.19 5.48 2.017 1.608 4.824 1.817 7.064 3.096a7.583 7.583 0 01-3.754 14.174H65.75a7.583 7.583 0 010-15.166H23.5a7.583 7.583 0 110-15.167h43.333a7.583 7.583 0 100-15.167H39.75a7.583 7.583 0 110-15.166h43.333a7.583 7.583 0 010-15.167H189.25zm0 30.333a7.583 7.583 0 110 15.166 7.583 7.583 0 010-15.166z" fill="#D9D8FF" fill-opacity="0.8"></path><path fill-rule="evenodd" clip-rule="evenodd" d="M132.561 19.646l10.077 73.496.906 7.374a4.334 4.334 0 01-3.773 4.829l-63.44 7.789a4.333 4.333 0 01-4.83-3.772l-9.767-79.547a2.166 2.166 0 011.91-2.417l5.262-.59 63.655-7.162zM73.162 26.33l4.97-.557-4.97.557z" fill="#fff"></path><path d="M73.162 26.33l4.97-.557m54.429-6.127l10.077 73.496.906 7.374a4.334 4.334 0 01-3.773 4.829l-63.44 7.789a4.333 4.333 0 01-4.83-3.772l-9.767-79.547a2.166 2.166 0 011.91-2.417l5.262-.59 63.655-7.162z" stroke="#7B79FF" stroke-width="2.5"></path><path fill-rule="evenodd" clip-rule="evenodd" d="M129.818 24.27l9.122 66.608.82 6.682c.264 2.153-1.246 4.11-3.373 4.371l-56.812 6.976c-2.127.261-4.066-1.272-4.33-3.425l-8.83-71.908a2.167 2.167 0 011.887-2.415l7.028-.863" fill="#F0F0FF"></path><path fill-rule="evenodd" clip-rule="evenodd" d="M135.331 5.833H85.978a2.97 2.97 0 00-2.107.873A2.97 2.97 0 0083 8.813v82.333c0 .823.333 1.567.872 2.106a2.97 2.97 0 002.107.873h63.917a2.97 2.97 0 002.106-.873 2.97 2.97 0 00.873-2.106V23.367a2.98 2.98 0 00-.873-2.107L137.437 6.705a2.98 2.98 0 00-2.106-.872z" fill="#fff" stroke="#7B79FF" stroke-width="2.5"></path><path d="M135.811 7.082v12.564a3.25 3.25 0 003.25 3.25h8.595M94.644 78.146h28.167m-28.167-55.25h28.167-28.167zm0 13h46.584-46.584zm0 14.083h46.584-46.584zm0 14.084h46.584-46.584z" stroke="#7B79FF" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"></path></g><defs><clipPath id="EmptyDocuments_svg__clip0_1142_57509"><path fill="#fff" d="M0 0h216v120H0z"></path></clipPath></defs></svg> -->
          <p style="color: rgb(102, 102, 135);font-weight: 500;font-size: 1rem;line-height: 1.25;">No alignments added</p>

        </div>
      </template>
    </q-table>
  
    <!-- <SelectAlignments :show_dialog="show_dialog" :ds-a="selectedDS[0]" :ds-b="selectedDS[1]"  @close-dialog="show_dialog=false" @add-alignment="addRow"/> -->
    <q-dialog v-model="show_dialog" full-height full-width>
      <ManualAlignmentsForm v-if="alignmentView == 'manual'" @close-dialog="show_dialog=!show_dialog"></ManualAlignmentsForm>
    </q-dialog>
  
  
  
  </template>
  
  <script setup>
  import {computed, ref, onMounted} from 'vue'
  import {useQuasar} from "quasar";
  import ManualAlignmentsForm from 'components/forms/ManualAlignmentsForm.vue';
  import { useIntegrationStore } from 'src/stores/integration.store.js'
  // import alerts from "components/hooks/alerts"
  // -------------------------------------------------------------
  //                         PROPS & EMITS
  // -------------------------------------------------------------
  const props = defineProps({
      no_shadow: {type: Boolean, default: false},
      alignments: {type :Array, default: []}
  });
  const emit = defineEmits(['update:alignments'])
  // -------------------------------------------------------------
  //                          STORES & GLOBALS
  // -------------------------------------------------------------
  const integrationStore = useIntegrationStore()
  const $q = useQuasar()
  onMounted(() => {
    // TODO: maybe need refactor
    integrationStore.init()
  })
  // -------------------------------------------------------------
  //                          C
  // -------------------------------------------------------------
  const columns = [
        {name: "uriA", label: "Resource A", align: "center", field: "iriA", sortable: true,},
        {name: "uriB", label: "Resource B", align: "center", field: "iriB", sortable: true,},
        {name: "labelA", label: "Label A", align: "center", field: "labelA", sortable: true,},
        {name: "labelB", label: "Label B", align: "center", field: "labelB", sortable: true,},
        {name: "labelIntegrated", label: "Integrated name", align: "center", field: "l", sortable: true,},
        {name: "type", label: "Type", align: "center", field: "type", sortable: true,},
        {name: "shortType", label: "ShortType", align: "center", field: "shortType", sortable: true,},
        {name: "actions", label: "actions", align: "center", field: "actions", sortable: false,},
        {name: "semantic", label: "semantic relationship", align: "center", field: "semantic", sortable: false,style: 'width: 690px',},

      ];

  const visibleCols = ref(['labelA', 'labelB', 'labelIntegrated', 'actions','semantic'])
  const show_dialog = ref(false)
  const alignmentView = ref('manual')
      // const selectedDS = computed(() => store.state.datasource.selectedDatasources)
      const selectedDS = {}
     const pagination = ref({rowsPerPage: 0})
    
      // const rows = [];
      const title = "Alignments";
      // :visible-columns="visibleColumns"
      const addRow= (props2  )=> {
        if(props2){
          if(props2.row){
            console.log(props.alignments)
            console.log(props.row)
            console.log(props.alignments.indexOf(props2.row))
            if(props.alignments.indexOf(props2.row) === -1) {
              props.alignments.push(props2.row);
              // console.log(this.items);
            }
            // console.log("emitted")
            // emit("update:alignments", props.alignments )
            // notify.positive(`Alignment ${props2.row.l} added`)
            // console.log(this.rows)
          }
        }
      }
      const deleteRow = (alignmentRow) =>{
        integrationStore.deleteJoinAlignment(alignmentRow)
      }
  const setManualView = (view) => {
    alignmentView.value = view
    show_dialog.value = true
  }    
  </script>
  
  <style lang="scss">
  #TableAlignments {
    .q-table__middle {
    flex: unset !important;
    }
  }

  .rounded-cell > div > div{
    border-radius: 28px !important;
  }
  </style>