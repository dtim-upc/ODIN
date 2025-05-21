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

          <q-expansion-item :label="'Integrated schemas (' + integratedSchemasLength + ' items)'" expand-icon="arrow_drop_down" :disable="integratedSchemasLength === 0">
            <q-list dense>
              <q-item v-for="ds in projectsStore.currentProject.integratedDatasets" :key="ds.id">
                <q-btn flat padding="xs" :label="ds.datasetName" class="full-width"
                       :class="selectedSchema === ds.id? 'activebg': ''" align="left" @click="setSchema(ds)"/>
              </q-item>

            </q-list>
          </q-expansion-item>

          <q-expansion-item :label="'Local schemas (' + localSchemasLength + ' items)'" expand-icon="arrow_drop_down" :disable="localSchemasLength ===0">
            <q-list dense>
              <q-item v-for="ds in datasourcesStore.datasets" :key="ds.id">
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
import {ref, onMounted, computed} from "vue";
import Graph from 'components/graph/Graph.vue'
import {useDatasetsStore} from 'src/stores/datasetsStore.js'
import {useProjectsStore} from 'src/stores/projectsStore.js'

const datasourcesStore = useDatasetsStore()
const projectsStore = useProjectsStore()
const projectID = useProjectsStore().currentProject.projectId

const graphical = ref('');
const selectedSchema = ref('');

const setSchema = dataset => {
  selectedSchema.value = dataset.id
  graphical.value = dataset.localGraph.graphicalSchema
}

const integratedSchemasLength = computed(() => projectsStore.currentProject.integratedDatasets ? projectsStore.currentProject.integratedDatasets.length : 0);
const localSchemasLength = computed(() => datasourcesStore.datasets ? datasourcesStore.datasets.length : 0);

onMounted(async () => {
  await datasourcesStore.getDatasets(projectID);
  if (datasourcesStore.datasets.length > 0) {
    setGlobalSchema();
  }
});

const setGlobalSchema = () => {
  selectedSchema.value = 'project'
  graphical.value = projectsStore.getGlobalSchema
}

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
