<template>
  <q-table :rows="integrationStore.joinAlignments" :columns="columns" :class="{ 'no-shadow': true }" id="TableAlignments"
           row-key="uriA"
           virtual-scroll
           :rows-per-page-options="[0]"
           :v-model:pagination="{rowsPerPage: 0}">
    <template v-slot:top-left>
      <div class="q-table__title">
        <span> Alignments  </span>
      </div>
    </template>

    <template v-slot:top-right="props">
      <FullScreenToggle :props="props" @toggle="props.toggleFullscreen"/>
    </template>

    <template v-slot:body-cell-labelIntegrated="props">
      <q-td :props="props">
        <div>
          <span v-if="props.row.edit == null || !props.row.edit">{{ props.row.l }}
            <q-icon @click="props.row.edit=true" size="xs" name="edit"/>
          </span>
          <q-input v-else outlined v-model="props.row.l" dense v-on:keyup.enter="props.row.edit = false;">
            <template v-slot:append>
              <q-icon name="o_task_alt" @click.stop.prevent="props.row.edit=false" class="cursor-pointer"/>
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
        <div>
          <div class="q-gutter-xs row items-start">
            <q-input rounded filled v-model="props.row.domainLabelA" label="domainA" class="rounded-cell" dense readonly/>

            <q-input filled v-model="props.row.relationship" label="Relationship" dense
                     :rules="[ val => !val.includes(' ')|| 'space not supported']">

              <template v-slot:before>
                <q-checkbox v-model="props.row.rightArrow" checked-icon="arrow_forward_ios"
                            unchecked-icon="arrow_back_ios_new" color="green" keep-color/>
              </template>

              <template v-slot:after>
                <q-checkbox v-model="props.row.rightArrow" checked-icon="arrow_forward_ios"
                            unchecked-icon="arrow_back_ios_new" color="green" keep-color/>
              </template>
            </q-input>
            <q-input rounded filled v-model="props.row.domainLabelB" label="domainB" class="rounded-cell" dense readonly/>
          </div>
        </div>
      </q-td>
    </template>

    <template v-slot:no-data>
      <div class="full-width row flex-center text-accent q-gutter-sm q-pa-xl" style="flex-direction: column">
        <img src="/assets/icons/AlignmentTable.svg" alt="No alignments added" style="width: 320px; height: 176px"/>
        <p style="color: rgb(102, 102, 135);font-weight: 500;font-size: 1rem;line-height: 1.25;">No alignments added</p>
      </div>
    </template>
  </q-table>
</template>

<script setup>
import {useQuasar} from "quasar";
import {useIntegrationStore} from 'src/stores/integrationStore.js'
import FullScreenToggle from "./TableUtils/FullScreenToggle.vue";

const emit = defineEmits(['update:alignments'])

const integrationStore = useIntegrationStore()
const $q = useQuasar()

const columns = [
  {name: "labelA", label: "Label A", align: "center", field: "labelA", sortable: true,},
  {name: "labelB", label: "Label B", align: "center", field: "labelB", sortable: true,},
  {name: "labelIntegrated", label: "Integrated name", align: "center", field: "l", sortable: true,},
  {name: "type", label: "Type", align: "center", field: "type", sortable: true,},
  {name: "semantic", label: "semantic relationship", align: "center", field: "semantic", sortable: false, style: 'width: 690px',},
  {name: "actions", label: "actions", align: "center", field: "actions", sortable: false,},
];

const deleteRow = (alignmentRow) => {
  integrationStore.deleteJoinAlignment(alignmentRow)
}

</script>

<style lang="scss">
#TableAlignments {
  .q-table__middle {
    flex: unset !important;
  }
}

.rounded-cell > div > div {
  border-radius: 28px !important;
}
</style>
