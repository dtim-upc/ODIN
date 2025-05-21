<template>
  <q-page class="row items-stretch">
    <div class="col-2 columnHeader">
      <q-scroll-area class="fit">
        <q-list>
          <q-item>
            <h5>Generate Mappings</h5>
          </q-item>
          <q-separator/>

          <q-item v-for="ds in projectsStore.currentProject.integratedDatasets" :key="ds.id">
            <q-btn flat padding="xs" :label="ds.datasetName"
                   class="full-width" :class="selectedSchema === ds.id ? 'activebg' : ''"
                   @click="selectedSchema = ds.id"/>
          </q-item>
        </q-list>
      </q-scroll-area>
    </div>

    <div class="col-10 q-pa-md">
      <div class="q-gutter-md">
        <q-select
          v-model="selectedMappingType"
          :options="mappingTypes"
          label="Select mapping type"
          outlined
          dense
        />

        <!-- Ask for config path only if mapping type is R2RML-CONFIG -->
        <q-file
          v-if="selectedMappingType === 'R2RML-CONFIG'"
          v-model="configFile"
          label="Choose configuration (.properties) file"
          outlined
          dense
          use-chips
          accept=".properties"
        />

        <q-btn
          label="Download Mappings"
          color="primary"
          :disable="!selectedSchema || !selectedMappingType"
          @click="downloadMappings"
        />
      </div>
    </div>
  </q-page>
</template>

<script setup>
import {ref, computed, onMounted} from 'vue'
import {useProjectsStore} from 'src/stores/projectsStore.js'
import {useMappingsStore} from 'src/stores/mappingsStore.js'

const projectsStore = useProjectsStore()
const mappingsStore = useMappingsStore()

const selectedSchema = ref('')
const selectedMappingType = ref('')

const projectID = computed(() => projectsStore.currentProject.projectId)

const mappingTypes = ['R2RML', 'R2RML-CONFIG']  // customize based on supported formats

const configFile = ref(null)

const downloadMappings = () => {
  if (projectID.value && selectedMappingType.value) {
    mappingsStore.downloadMappings(
      projectID.value,
      selectedMappingType.value,
      selectedMappingType.value === 'R2RML-CONFIG' ? configFile.value : null
    )
  }
}



onMounted(() => {
  if (projectsStore.currentProject.integratedDatasets?.length > 0) {
    selectedSchema.value = projectsStore.currentProject.integratedDatasets[0].id
  }
})
</script>

<style lang="scss">
.columnHeader {
  background-color: white;
}

.body--dark .columnHeader {
  background-color: #202024;
}

.activebg {
  background-color: $primary100 !important;
}
</style>
