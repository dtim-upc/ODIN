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
                       :class="selectedSchema == 'project'? 'activebg': ''" align="left" @Click="setGlobalSchema()"/>
              </q-item>

            </q-list>
          </q-expansion-item>

          <q-expansion-item label="Local schema" expand-icon="arrow_drop_down">
            <q-list dense>

              <q-item v-for="ds in storeDS.datasources">
                <q-btn flat padding="xs" :label="ds.datasetName" class="full-width"
                       :class="selectedSchema == ds.datasetId? 'activebg': ''" align="left" @Click="setSchema(ds)"/>
              </q-item>

            </q-list>
          </q-expansion-item>


        </q-list>
      </q-scroll-area>
    </div>
    <div class="col-10">
      <Graph :graphical="graphical"></Graph>

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
  selectedSchema.value = datasource.datasetId
  graphical.value = datasource.localGraph.graphicalSchema
}

const setGlobalSchema = () => {
  console.log("setting global schema view")
  console.log(storeDS.getGlobalSchema)
  selectedSchema.value = 'project'
  graphical.value = storeDS.getGlobalSchema
}

onMounted(() => {
  if (storeDS.datasources.length > 0) {
    //  setSchema( storeDS.datasources[0] )
    setGlobalSchema()
  }
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
