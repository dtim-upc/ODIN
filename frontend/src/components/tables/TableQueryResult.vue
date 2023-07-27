<template>
  <div class="q-pa-md">
    <!-- :filter="search" -->
    <q-table :rows="rows" :columns="columns" 
             row-key="name" no-data-label="No result."
             no-results-label="The filter didn't uncover any results" :class="{ 'no-shadow': no_shadow }"
>
      <template v-slot:top-left="">
        <div class="q-table__title">
          {{ title }}
        </div>
      </template>

      <template v-slot:top-right="props">

        <!-- <q-input outlined dense debounce="400" color="primary" v-model="search">
          <template v-slot:append>
            <q-icon name="search"/>
          </template>
        </q-input> -->
        <q-btn
          color="primary"
          icon-right="archive"
          label="Export to csv"
          no-caps
          @click="exportTable"
          v-if="enableExport"
        />

        <q-btn flat round dense :icon="props.inFullscreen ? 'fullscreen_exit' : 'fullscreen'"
               @click="props.toggleFullscreen">
          <q-tooltip :disable="$q.platform.is.mobile" v-close-popup>
            {{ props.inFullscreen ? "Exit Fullscreen" : "Toggle Fullscreen" }}
          </q-tooltip>
        </q-btn>
      </template>

    </q-table>
  </div>
</template>


<script setup >
import { ref, onMounted} from "vue";
import { exportFile } from 'quasar'

  const props = defineProps({
        no_shadow: {type: Boolean, default: false},
        view: {type: String, default: "datasources"},
        columns: {type: Array},
        rows: {type:Array},
        enableExport: {type: Boolean, default: true}
  });

const title = "Query result";


   const wrapCsvValue = (val, formatFn, row) => {
      let formatted = formatFn !== void 0
        ? formatFn(val, row)
        : val

      formatted = formatted === void 0 || formatted === null
        ? ''
        : String(formatted)

      formatted = formatted.split('"').join('""')
      /**
       * Excel accepts \n and \r in strings, but some other CSV parsers do not
       * Uncomment the next two lines to escape new lines
       */
      // .split('\n').join('\\n')
      // .split('\r').join('\\r')

      return `"${formatted}"`
    }

    onMounted(wrapCsvValue)

const exportTable = () => {
        // naive encoding to csv format
        const content = [props.columns.map(col => wrapCsvValue(col.label))].concat(
          props.rows.map(row => props.columns.map(col => wrapCsvValue(
            typeof col.field === 'function'
              ? col.field(row)
              : row[ col.field === void 0 ? col.name : col.field ],
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

<style lang="css" scoped>
</style>