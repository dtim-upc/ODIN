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

    <q-dialog v-model="showQueryResultDialog" full-width persistent>
      <q-card>
        <q-card-section>
          <TableQueryResult :columns="columns" :rows="rows" :no_shadow=true />
        </q-card-section>

        <q-card-actions align="right">
          <q-btn flat label="Cancel" color="primary" v-close-popup />
          <q-btn color="primary" icon-right="archive" label="Export to csv" @click="downloadTemporalDataProduct"/>
          <q-btn label="Persist data" color="primary" @click="showPersistDataDialog=true" />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <q-dialog v-model="showPersistDataDialog">
      <q-card style="width: 300px; height: 230px;">
        <q-card-section>
          <q-form @submit="postDataProduct" class="text-right">
            <q-input v-model="dataProductName" label="Data product name" :rules="[ val => val && val.length > 0 || 'Insert a name']"/>
            <q-input v-model="dataProductDescription" label="Data product description"/>

            <q-btn type="submit" color="primary" label="Persist" v-close-popup class="q-mt-md"/>
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
import {useProjectsStore} from 'src/stores/projectsStore.js'
import {useQueriesStore} from 'src/stores/queriesStore.js'
import {useDataProductsStore} from 'src/stores/dataProductsStore.js'
import { useQuasar } from "quasar";

const $q = useQuasar()

const queriesStore = useQueriesStore();
const dataProductsStore = useDataProductsStore();
const projectsStore = useProjectsStore();

const showQueryResultDialog = ref(false);
const showPersistDataDialog = ref(false);

const dataProductColumns = ref([]);
const dataProductName = ref("");
const dataProductDescription = ref("");
const dataProductUUID = ref('') 

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
  showQueryResultDialog.value = true;
}

const setGlobalSchema = () => {
  selectedSchema.value = 'project'
  graphical.value = projectsStore.getGlobalSchema
  graphID.value = projectsStore.currentProject.projectId
  graphType = "global"
}

const queryGraph = (data) => {
  $q.loading.show({message: 'Executing query...'})
  data.graphID = graphID.value
  data.graphType = graphType

  const successCallback = (responseData) => {
    dataProductUUID.value = responseData.dataProductUUID
    dataProductColumns.value = responseData.columns
    showResultQuery(responseData.columns, responseData.rows)
  }

  queriesStore.queryGraph(projectsStore.currentProject.projectId, data, successCallback)
  $q.loading.hide()
}

const postDataProduct = () => {
  const data = new FormData();
  data.append("dataProductUUID", dataProductUUID.value);
  data.append("dataProductName", dataProductName.value);
  data.append("dataProductDescription", dataProductDescription.value);
  data.append("columns", dataProductColumns.value);

  dataProductsStore.postDataProduct(projectsStore.currentProject.projectId, data)
}

const downloadTemporalDataProduct = () => {
  dataProductsStore.downloadTemporalDataProduct(projectsStore.currentProject.projectId, dataProductUUID.value)
}

onBeforeMount(async () => {
  setGlobalSchema();
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
