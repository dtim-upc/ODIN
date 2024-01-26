<template>
  <div class="q-pa-md">
    <q-table :rows="rows" :columns="columns" row-key="name"
             no-results-label="The filter didn't uncover any results" :class="{ 'no-shadow': true }">
      <template v-slot:top-left="">
        <div class="q-table__title">
          Query result
        </div>
      </template>

      <template v-slot:top-right="props">
        <q-btn color="primary" icon-right="archive" label="Export to csv" no-caps @click="exportTable"/>
        <FullScreenToggle :props="props" @toggle="props.toggleFullscreen"/>
      </template>

    </q-table>
  </div>
</template>

<script setup>
import {exportFile} from 'quasar'
import FullScreenToggle from "./TableUtils/FullScreenToggle.vue";

const props = defineProps({
  columns: {type: Array},
  rows: {type: Array},
});

const wrapCsvValue = (val, formatFn, row) => {
  let formatted = formatFn !== void 0
    ? formatFn(val, row)
    : val

  formatted = formatted === void 0 || formatted === null
    ? ''
    : String(formatted)

  formatted = formatted.split('"').join('""')
  return `"${formatted}"`
}

const exportTable = () => {
  const content = [props.columns.map(col => wrapCsvValue(col.label))].concat(
    props.rows.map(row => props.columns.map(col => wrapCsvValue(
      typeof col.field === 'function'
        ? col.field(row)
        : row[col.field === void 0 ? col.name : col.field],
      col.format,
      row
    )).join(','))
  ).join('\r\n')

  const status = exportFile(
    'table-export.csv',
    content,
    'text/csv'
  )

  if (status !== true) {
    notify.negative("Browser denied file download.")
  }
}
</script>

