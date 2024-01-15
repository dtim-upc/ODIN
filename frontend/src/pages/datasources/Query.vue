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
      <Graph :graphical="graphical" :enableSelection="true" :enableQuery="true" :queryFunc="query"></Graph>
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
          <q-form @submit="storeQuery" class="text-right">
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
import {useAuthStore} from 'stores/auth.store.js'
import queryAPI from "src/api/query.api.js";
import {useNotify} from 'src/use/useNotify.js'

const miniState = ref(true)
const storeDS = useDataSourceStore()
const authStore = useAuthStore();
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

const query = (data) => {
  console.log("query fn..")
  data.graphID = graphID.value
  data.graphType = graphType

  console.log("dd", data)
  queryAPI.queryGraph(data, storeDS.project.projectId, authStore.user.accessToken).then(response => {

    console.log("query success", response)
    if (response.data == '')
      notify.positive("Query result is empty")
    if (response.data)
      if (response.data != '') {
        CSVPath.value = response.data.csvpath
        queryColumns.value = response.data.columns
        showResultQuery(response.data.columns, response.data.rows)
      }
  }).catch(err => {
    console.log("error query graph", err)
  })

}

const storeQuery = () => {
  console.log("Storing query")
  const data2 = new FormData();
  data2.append("CSVPath", CSVPath.value);
  data2.append("projectID", storeDS.project.projectId);
  data2.append("queryName", queryName.value);
  data2.append("queryLabel", queryLabel.value);

  queryAPI.storeQuery(data2).then(response => {

    console.log("query success", response)
    if (response.status === 200) {
      notify.positive("Query stored successfully")
    }
  }).catch(err => {
    notify.negative("Error storing query")
    console.log("error query graph", err)
  })

}

onBeforeMount(async () => {
  await storeDS.setProject();
  setGlobalSchema();
  document.title = "Query"; // Título de la pestaña
})

// onMounted ( () => {
//   if(storeDS.datasources.length > 0) {
//   //  setSchema( storeDS.datasources[0] )
//   setGlobalSchema()
//   } else {
//     console.log("empty")
//   }
// })

// const updateGraphical = d => { graphical.value =  }


// const nodes = [
//     {"id":"Class1","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/19e8aa2d8c1f41598019a2eb1449c926/ds1","iriType":"http://www.w3.org/2000/01/rdf-schema#Class","type":"class","label":"ds1","domain":null,"range":null,"linkId":null},
//     {"id":"Class2","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/19e8aa2d8c1f41598019a2eb1449c926/ds1.title","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","type":"objectProperty","label":"title","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/19e8aa2d8c1f41598019a2eb1449c926/ds1","range":"http://www.w3.org/2001/XMLSchema#string","linkId":"Link1"},
//     {"id":"Class3","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/19e8aa2d8c1f41598019a2eb1449c926/ds1.createdAt","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","type":"objectProperty","label":"createdAt","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/19e8aa2d8c1f41598019a2eb1449c926/ds1","range":"http://www.w3.org/2001/XMLSchema#string","linkId":"Link2"},
//     {"id":"Class5","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/19e8aa2d8c1f41598019a2eb1449c926/ds1.domain","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","type":"objectProperty","label":"domain","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/19e8aa2d8c1f41598019a2eb1449c926/ds1","range":"http://www.w3.org/2001/XMLSchema#string","linkId":"Link3"},
//     {"id":"Class6","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/19e8aa2d8c1f41598019a2eb1449c926/ds1.madeBy","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","type":"objectProperty","label":"madeBy","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/19e8aa2d8c1f41598019a2eb1449c926/ds1","range":"http://www.w3.org/2001/XMLSchema#string","linkId":"Link4"},
//     {"id":"Class7","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/19e8aa2d8c1f41598019a2eb1449c926/ds1.idObject","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","type":"objectProperty","label":"idObject","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/19e8aa2d8c1f41598019a2eb1449c926/ds1","range":"http://www.w3.org/2001/XMLSchema#string","linkId":"Link5"},
//     {"id":"Datatype8","iri":"http://www.w3.org/2001/XMLSchema#string","iriType":null,"type":"xsdType","label":"string","domain":null,"range":null,"linkId":null},
//     {"id":"Datatype9","iri":"http://www.w3.org/2001/XMLSchema#string","iriType":null,"type":"xsdType","label":"string","domain":null,"range":null,"linkId":null},
//     {"id":"Datatype10","iri":"http://www.w3.org/2001/XMLSchema#string","iriType":null,"type":"xsdType","label":"string","domain":null,"range":null,"linkId":null},
//     {"id":"Datatype11","iri":"http://www.w3.org/2001/XMLSchema#string","iriType":null,"type":"xsdType","label":"string","domain":null,"range":null,"linkId":null},
//     {"id":"Datatype12","iri":"http://www.w3.org/2001/XMLSchema#string","iriType":null,"type":"xsdType","label":"string","domain":null,"range":null,"linkId":null}
// ]

// const links = [


//  {"id":"Link1","source":"Class1","target":"Datatype8","label":"title"},
//  {"id":"Link2","source":"Class1","target":"Datatype9","label":"createdAt"},
//  {"id":"Link3","source":"Class1","target":"Datatype10","label":"domain"},
//  {"id":"Link4","source":"Class1","target":"Datatype11","label":"madeBy"},
//  {"id":"Link5","source":"Class1","target":"Datatype12","label":"idObject"}

// ]


// const nodes = [{"id":"Class1","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22.museum","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","type":"datatypeProperty","label":"museum","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22","range":"http://www.w3.org/2001/XMLSchema#string","linkId":"Link1"},
// {"id":"Class2","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22.has_artworks","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","type":"objectProperty","label":"has_artworks","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22","range":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22.Seq1","linkId":"Link2"},
// {"id":"Class3","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22.Seq1.artworks.title","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","type":"datatypeProperty","label":"title","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22.Seq1.artworks","range":"http://www.w3.org/2001/XMLSchema#string","linkId":"Link3"},
// {"id":"Class4","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22.Seq1.artworks.domain","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","type":"datatypeProperty","label":"domain","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22.Seq1.artworks","range":"http://www.w3.org/2001/XMLSchema#string","linkId":"Link4"},
// {"id":"Class5","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22","iriType":"http://www.w3.org/2000/01/rdf-schema#Class","type":"class","label":"ds22","domain":null,"range":null,"linkId":null},
// {"id":"Class6","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22.location","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","type":"datatypeProperty","label":"location","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22","range":"http://www.w3.org/2001/XMLSchema#string","linkId":"Link5"},
// {"id":"Class7","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22.Seq1.artworks.idObject","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","type":"datatypeProperty","label":"idObject","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22.Seq1.artworks","range":"http://www.w3.org/2001/XMLSchema#string","linkId":"Link6"},
// {"id":"Class8","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22.Seq1.artworks.createdAt","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","type":"datatypeProperty","label":"createdAt","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22.Seq1.artworks","range":"http://www.w3.org/2001/XMLSchema#string","linkId":"Link7"},
// {"id":"Class10","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22.Seq1.ContainerMembershipProperty1","iriType":"http://www.w3.org/2000/01/rdf-schema#ContainerMembershipProperty","type":"objectProperty","label":"ContainerMembershipProperty1","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22.Seq1","range":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22.Seq1.artworks","linkId":"Link8"},
// {"id":"Class11","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22.category","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","type":"datatypeProperty","label":"category","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22","range":"http://www.w3.org/2001/XMLSchema#string","linkId":"Link9"},
// {"id":"Class12","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22.Seq1","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq","type":"class","label":"Seq1","domain":null,"range":null,"linkId":null},
// {"id":"Class13","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22.Seq1.artworks","iriType":"http://www.w3.org/2000/01/rdf-schema#Class","type":"class","label":"artworks","domain":null,"range":null,"linkId":null},
// {"id":"Class14","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22.Seq1.artworks.madeBy","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","type":"datatypeProperty","label":"madeBy","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/4f6e561d834a4fef9c6812de470e579b/ds22.Seq1.artworks","range":"http://www.w3.org/2001/XMLSchema#string","linkId":"Link10"},
// {"id":"Datatype15","iri":"http://www.w3.org/2001/XMLSchema#string","iriType":null,"type":"xsdType","label":"string","domain":null,"range":null,"linkId":null},
// {"id":"Datatype16","iri":"http://www.w3.org/2001/XMLSchema#string","iriType":null,"type":"xsdType","label":"string","domain":null,"range":null,"linkId":null},
// {"id":"Datatype17","iri":"http://www.w3.org/2001/XMLSchema#string","iriType":null,"type":"xsdType","label":"string","domain":null,"range":null,"linkId":null},
// {"id":"Datatype18","iri":"http://www.w3.org/2001/XMLSchema#string","iriType":null,"type":"xsdType","label":"string","domain":null,"range":null,"linkId":null},
// {"id":"Datatype19","iri":"http://www.w3.org/2001/XMLSchema#string","iriType":null,"type":"xsdType","label":"string","domain":null,"range":null,"linkId":null},
// {"id":"Datatype20","iri":"http://www.w3.org/2001/XMLSchema#string","iriType":null,"type":"xsdType","label":"string","domain":null,"range":null,"linkId":null},
// {"id":"Datatype21","iri":"http://www.w3.org/2001/XMLSchema#string","iriType":null,"type":"xsdType","label":"string","domain":null,"range":null,"linkId":null},
// {"id":"Datatype22","iri":"http://www.w3.org/2001/XMLSchema#string","iriType":null,"type":"xsdType","label":"string","domain":null,"range":null,"linkId":null}]
// const links = [{"id":"Link1","source":"Class5","target":"Datatype15","label":"museum"},
// {"id":"Link2","source":"Class5","target":"Class12","label":"has_artworks"},
// {"id":"Link3","source":"Class13","target":"Datatype16","label":"title"},
// {"id":"Link4","source":"Class13","target":"Datatype17","label":"domain"},
// {"id":"Link5","source":"Class5","target":"Datatype18","label":"location"},
// {"id":"Link6","source":"Class13","target":"Datatype19","label":"idObject"},
// {"id":"Link7","source":"Class13","target":"Datatype20","label":"createdAt"},
// {"id":"Link8","source":"Class12","target":"Class13","label":"ContainerMembershipProperty1"},
// {"id":"Link9","source":"Class5","target":"Datatype21","label":"category"},
// {"id":"Link10","source":"Class13","target":"Datatype22","label":"madeBy"}]

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
