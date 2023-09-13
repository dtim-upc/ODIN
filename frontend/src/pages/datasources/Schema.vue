<template>
  <q-page class="row items-stretch">
    <div class="col-2 columnHeader">
      <q-scroll-area class="fit">
        <q-list>
          <q-item-section>
            <q-item>
              <h5>Schema</h5>
            </q-item>
          </q-item-section>

          <q-expansion-item label="Global schema" expand-icon="arrow_drop_down" default-opened>
            <q-list dense>

              <q-item>
                <q-btn flat padding="xs" label="project" class="full-width"
                       :class="selectedSchema === 'project'? 'activebg': ''" align="left" @Click="setGlobalSchema()"/>
              </q-item>

            </q-list>
          </q-expansion-item>

          <q-expansion-item label="Local schema" expand-icon="arrow_drop_down">
            <q-list dense>

              <q-item v-for="ds in storeDS.datasources" :key="ds.id">
                <q-btn flat padding="xs" :label="ds.datasetName" class="full-width"
                       :class="selectedSchema === ds.id? 'activebg': ''" align="left" @click="setSchema(ds)"/>
              </q-item>


            </q-list>
          </q-expansion-item>


        </q-list>
      </q-scroll-area>
    </div>
    <div class="col-10">
      <Graph v-if="graphical" :graphical="graphical"></Graph>
      <div v-else class="empty-content">
        <div class="empty-message">
          <p>Content is not available at the moment. Possible reasons:</p>
          <ul style="text-align: left;">
            <li>You haven't integrated any schema yet.</li>
            <li>You haven't defined the project's base schema.</li>
          </ul>
        </div>
      </div>

    </div>

  </q-page>
</template>


<script setup>
import {ref, onMounted} from "vue";
import Graph from 'components/graph/Graph.vue'
import {useDataSourceStore} from 'src/stores/datasources.store.js'

const miniState = ref(true)
const storeDS = useDataSourceStore()

const graphical = ref('')
const selectedSchema = ref('')


const setSchema = datasource => {
  selectedSchema.value = datasource.id
  graphical.value = datasource.localGraph.graphicalSchema
}

const setGlobalSchema = () => {
  console.log("setting global schema view")
  console.log(storeDS.getGlobalSchema)
  selectedSchema.value = 'project'
  graphical.value = storeDS.getGlobalSchema
}

onMounted(async () => {
  try {
    const url = window.location.href; // Get the current URL
    const regex = /project\/(\d+)\//;
    const match = url.match(regex);
    let projectId;
    if (match) {
      projectId = match[1];
      console.log(projectId);
    }
    await storeDS.getDatasources(projectId);

    if (storeDS.datasources.length > 0) {
      setGlobalSchema();
    }
  } catch (error) {
    console.error("Error al cargar los datos de datasources desde la API:", error);
  }
});
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

.empty-content {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.empty-message {
  font-size: 1.5rem;
  color: #555;
  text-align: center;
  padding: 20px;
  background-color: #f7f7f7;
  border-radius: 5px;
  box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.2);
}
</style>
