<template>
  <q-table :rows="integrationStore.alignments" :columns="columns" :class="{ 'no-shadow': true }"
           id="TableAlignments"
           row-key="uriA"
           virtual-scroll
           :rows-per-page-options="[0]"
           style="height: 60vh"
           :v-model:pagination="{rowsPerPage: 0}">
    <template v-slot:top-left="">
      <div class="q-table__title">
        <span> Alignments </span>
        <q-btn-dropdown color="primary" dropdown-icon="add" no-icon-animation padding="none" menu-anchor="top right"
                        menu-self="top left">
          <q-list>
            <q-item clickable v-close-popup @click="showManualAlignmentsForm = true">
              <q-item-section>
                <q-item-label>Manual alignment</q-item-label>
              </q-item-section>
            </q-item>

            <q-item clickable v-close-popup @click="getAutomaticAlignments">
              <q-item-section>
                <q-item-label>Automatic alignments</q-item-label>
              </q-item-section>
            </q-item>

          </q-list>
        </q-btn-dropdown>
      </div>
    </template>

    <template v-slot:top-right="props">
      <FullScreenToggle :props="props" @toggle="props.toggleFullscreen"/>
    </template>

    <template v-slot:body-cell-type="props">
      <q-td :props="props">
        <q-chip>{{ props.row.type }}</q-chip>
        <q-chip>{{ props.row.type }}</q-chip>
      </q-td>
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

    <template v-slot:body-cell-identifier="props">
      <q-td :props="props">
        hola{{ props.row.identifier }}
        <q-checkbox v-model="props.row.identifier" color="teal"/>
      </q-td>
    </template>

    <template v-slot:no-data>
      <div class="full-width row flex-center text-accent " style="flex-direction: column">
        <img src="/assets/icons/AlignmentTable.svg" alt="No alignments added" style="width: 320px; height: 176px"/>
        <p style="color: rgb(102, 102, 135);font-weight: 500;font-size: 1rem;line-height: 1.25;">No alignments added</p>
      </div>
    </template>
  </q-table>

  <q-dialog v-model="showManualAlignmentsForm" full-height full-width>
    <ManualAlignmentsForm @close-dialog="showManualAlignmentsForm=!showManualAlignmentsForm"></ManualAlignmentsForm>
  </q-dialog>

</template>

<script setup>
import {ref} from 'vue'
import ManualAlignmentsForm from 'components/forms/ManualAlignmentsForm.vue';
import {useIntegrationStore} from 'src/stores/integrationStore.js'
import {useQuasar} from 'quasar'
import FullScreenToggle from './TableUtils/FullScreenToggle.vue';

const integrationStore = useIntegrationStore()
const $q = useQuasar()

const showManualAlignmentsForm = ref(false)

const columns = [
  {name: "labelA", label: "Label A", align: "center", field: "labelA", sortable: true,},
  {name: "labelB", label: "Label B", align: "center", field: "labelB", sortable: true,},
  {name: "labelIntegrated", label: "Integrated name", align: "center", field: "l", sortable: true,},
  {name: "type", label: "Type", align: "center", field: "type", sortable: true,},
  {name: "similarity",
    label: "Similarity",
    align: "center",
    field: "similarity",
    sortable: true,
    format: (value) => {
      return `${(value * 100).toFixed(2)}%`;
    },
  },
  {name: "actions", label: "Actions", align: "center", field: "actions", sortable: false},
];

const deleteRow = (alignmentRow) => {
  integrationStore.deleteAlignment(alignmentRow)
}

const getAutomaticAlignments = async () => {
  $q.loading.show({message: 'Getting alignments...'})
  await integrationStore.getAutomaticAlignments();
  $q.loading.hide()
};

</script>

<style lang="scss">
#TableAlignments {
  .q-table__middle {
    flex: unset !important;
  }
}

.center {
  margin: auto;
  text-align: center;
  align-items: center;
  vertical-align: middle;
  padding: 90px 0px;
}
</style>