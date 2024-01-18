<template>
  <q-page class="row items-stretch">
    <div class="col-2 columnHeader">
      <q-scroll-area class="fit">
        <q-list>
          <q-item-section>
            <q-item>
              <h5> Query</h5>
            </q-item>
          </q-item-section>
        </q-list>
      </q-scroll-area>
    </div>

    <div class="col-10">
      <Graph :graphical="graphical" :enableSelection="true" :enableQuery="true" :queryFunc="queryGraph"></Graph>
    </div>


    <q-dialog v-model="alert" full-width persistent>
      <q-card>
        <q-card-section>
          <TableQueryResult :columns="columns" :rows="rows" :no_shadow=true />
        </q-card-section>

        <q-card-actions align="between">
          <q-btn flat label="Cancel" color="primary" v-close-popup />
          <q-btn label="Persist data" color="primary" @click="persistQuery=true" />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <q-dialog v-model="persistQuery">
      <q-card>
        <q-card-section>
          <q-form @submit="postQuery" class="text-right">
            <q-input v-model="queryName" label="Query name" :rules="[ val => val && val.length > 0 || 'Insert a name']"/>
            <q-select v-model="queryLabel" label="Query label" :options="queryColumns" :rules="[ val => val && val.length > 0 || 'Insert a label']"/>

            <q-btn type="submit" color="primary" label="Persist" v-close-popup/>
          </q-form>
        </q-card-section>
      </q-card>
    </q-dialog>

  </q-page>
</template>


<script setup>
import {ref, onBeforeMount} from "vue";
import TableQueryResult from "components/tables/TableQueryResult.vue";
import Graph from 'components/graph/Graph.vue'
import {useDataSourceStore} from 'src/stores/datasources.store.js'
import {useQueriesStore} from 'src/stores/queriesStore.js'
import {useAuthStore} from 'stores/auth.store.js'
import {useNotify} from 'src/use/useNotify.js'

const miniState = ref(true)
const storeDS = useDataSourceStore()
const queriesStore = useQueriesStore();
const alert = ref(false);
const notify = useNotify();

const persistQuery = ref(false);
const queryColumns = ref([]);
const queryName = ref("");
const queryLabel = ref("");

const CSVPath = ref('') 
const graphical = ref('')
const graphID = ref('')
let graphType = ""
const selectedSchema = ref('')


const columns = ref([
  {name: "Name", required: true, label: "Name", align: "center", field: "name", sortable: true,},
  {name: "Type", required: true, label: "Type", align: "center", field: "type", sortable: true,},
  {name: "#Wrappers", label: "#Wrappers", align: "center", field: "wrappers", sortable: true,},
  {name: "View Metadata", label: "View Metadata", align: "center", field: "View Metadata", sortable: false,},
  {
    name: "View_Source_Graph", label: "View Source Graph", align: "center",
    field: "View Source Graph", sortable: false,
  },
  {name: "actions", label: "actions", align: "center", field: "actions", sortable: false,},
]);
const rows = ref([]);


const showResultQuery = (columnsQ, rowsQ) => {
  notify.positive("Query done");
  const qcol = []
  for (const col in columnsQ) {
    var c = new Object();

    c.name = columnsQ[col];
    c.label = columnsQ[col];
    c.field = columnsQ[col];
    c.align = "center"
    c.sortable = true;

    qcol.push(c)
  }
  columns.value = qcol;
  const qrows = []
  for (const col in rowsQ) {
    qrows.push(JSON.parse(rowsQ[col]))
  }
  rows.value = qrows;
  alert.value = true;
}

const setSchema = datasource => {
  selectedSchema.value = datasource.id
  graphical.value = datasource.localGraph.graphicalSchema
  graphID.value = datasource.id
  graphType = "source"
}

const setGlobalSchema = () => {
  selectedSchema.value = 'project'
  graphical.value = storeDS.getGlobalSchema
  graphID.value = storeDS.project.projectId
  graphType = "global"
}

const queryGraph = (data) => {
  console.log("query fn..")
  data.graphID = graphID.value
  data.graphType = graphType

  const successCallback = (responseData) => {
    CSVPath.value = responseData.csvpath
    queryColumns.value = responseData.columns
    showResultQuery(responseData.columns, responseData.rows)
  }

  queriesStore.queryGraph(storeDS.project.projectId, data, successCallback)
}

const postQuery = () => {
  console.log("Storing query")
  const data = new FormData();
  data.append("CSVPath", CSVPath.value);
  data.append("queryName", queryName.value);
  data.append("queryLabel", queryLabel.value);

  queriesStore.postQuery(storeDS.project.projectId, data)

}

onBeforeMount(async () => {
  await storeDS.setProject();
  setGlobalSchema();
  document.title = "Query";
})

</script>

<style lang="scss">
.body--light {

  .columnHeader {
    background: white;
  }
}

.body--dark {
  .columnHeader {
    background: #202024;
  }

}


</style>
