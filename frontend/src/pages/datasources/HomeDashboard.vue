<template>
    <q-page class="flex flex-center">
      <home_pattern style="top:10;right:0;position:absolute;margin-right: 10px;width:500px;"></home_pattern>
      <!-- <q-card class="my-card q-pa-md" style="min-width: 500px" >
        <q-card-section>
          <div  >
            <h6 style="margin:10px">Welcome {{authStore.getUserName}} </h6>
            <q-list bordered separator>

              <q-item clickable style="padding:12px" v-ripple :active="active" @click="addDataSource = true">
                
                <q-item-section avatar>
                  <q-icon :name="storeDS.getDatasourcesNumber >0? 'check_circle':'o_file_upload'" :color="storeDS.getDatasourcesNumber >0? 'green':null"  />
                </q-item-section>

                <q-item-section>
                  <q-item-label>Upload a data source</q-item-label>
                  <q-item-label caption>This will automatically define an schema</q-item-label>
                </q-item-section>

                <q-item-section side>
                  <q-icon name="o_navigate_next" color="green" />
                </q-item-section>

              </q-item>

              <q-item clickable style="padding:12px" v-ripple :active="active" @click="addDataSource = true">
                
                <q-item-section avatar>
                  <q-icon :name="storeDS.getDatasourcesNumber > 1 || (integrationStore.getDatasourcesNumber>0 && storeDS.getDatasourcesNumber >0) ? 'check_circle':'o_file_upload'" :color="storeDS.getDatasourcesNumber > 1 || (integrationStore.getDatasourcesNumber>0 && storeDS.getDatasourcesNumber >0)? 'green':null"   />
                </q-item-section>

                <q-item-section>
                  <q-item-label>Upload a second data source</q-item-label>
                  <q-item-label caption>This will automatically define an schema</q-item-label>
                </q-item-section>

                <q-item-section side>
                  <q-icon name="o_navigate_next" color="green" />
                </q-item-section>

              </q-item>

              <q-item clickable style="padding:12px" v-ripple :active="active">
                
                <q-item-section avatar>
                  <q-icon :name="storeDS.getDatasourcesNumber > 1? 'check_circle':'o_merge'" :color="storeDS.getDatasourcesNumber > 1? 'green':null"  />
                </q-item-section>

                <q-item-section>
                  <q-item-label>Integrate second data source with the project</q-item-label>
                </q-item-section>

                <q-item-section side>
                  <q-icon name="o_navigate_next" color="green" />
                </q-item-section>

              </q-item>

              <q-item clickable style="padding:12px" v-ripple :active="active">
                
                <q-item-section avatar>
                  <q-icon name="mdi-selection-search" />
                </q-item-section>

                <q-item-section>
                  <q-item-label>Explore the integrated data</q-item-label>
                  <q-item-label caption>Not available for this survey</q-item-label>
                </q-item-section>

                <q-item-section side>
                  <q-icon name="o_navigate_next" color="green" />
                </q-item-section>

              </q-item>

            
            </q-list>
          </div>
        </q-card-section>
      </q-card> -->
      
      <div class="q-pa-md" style="min-width:550px; max-width: 850px">

      <q-expansion-item expand-separator class="bg-white-color"  default-opened>

      <template v-slot:header>
          <q-item-section>
            Getting started
          </q-item-section>

          <q-item-section side>
            4 steps
          </q-item-section>

          <q-item-section style="position:absolute; bottom:0;left: 0;right: 0;">
            <q-linear-progress :value="progress()" rounded color="green" class="q-mt-sm" />
          </q-item-section>

          
        </template>
        <q-card style="width:100%">
          <q-card-section>
            
            <q-list  separator>

<q-item clickable style="padding:12px" v-ripple :active="active" @click="addDataSource = true">
  
  <q-item-section avatar>
      <q-btn flat padding="xs" icon="o_file_upload" color="primary600 " class="activebg"></q-btn>
  </q-item-section>

  <q-item-section>
    <q-item-label>Upload a data source</q-item-label>
    <q-item-label caption>This will automatically define an schema</q-item-label>
  </q-item-section>

  <q-item-section side>

    <q-btn v-if="integrationStore.getDatasourcesNumber==0 && storeDS.getDatasourcesNumber ==0" color="primary" label="Start" icon-right="o_navigate_next" dense no-caps />
    <q-icon v-else name="check_circle" color="green"  />
  </q-item-section>

</q-item>


<q-item clickable style="padding:12px" v-ripple :active="active" @click="showIntegrationView">
  
  <q-item-section avatar>
      <q-btn flat padding="xs" icon="o_archive" color="primary600 " class="activebg"></q-btn>
  </q-item-section>

  <q-item-section>
    <q-item-label>Add the data source to this project</q-item-label>
    <q-item-label caption>You will see the generated schema here </q-item-label>
    <q-item-label caption>Upon confirmation, it will be saved into this project</q-item-label>
  </q-item-section>

  <q-item-section side>

    <q-btn v-if="storeDS.getDatasourcesNumber==0" color="primary" label="Start" icon-right="o_navigate_next" dense no-caps />
    <q-icon v-else name="check_circle" color="green"  />
  </q-item-section>

</q-item>


<q-item clickable style="padding:12px" v-ripple :active="active" @click="addDataSource = true">
  
  <q-item-section avatar>
    <q-btn flat padding="xs" icon="o_file_upload" color="primary600" class="activebg"/>
    <!-- <q-icon name="o_file_upload" :color="storeDS.getDatasourcesNumber > 1 || (integrationStore.getDatasourcesNumber>0 && storeDS.getDatasourcesNumber >0)? 'green':null"   /> -->
  </q-item-section>

  <q-item-section>
    <q-item-label>Upload a second data source</q-item-label>
    <q-item-label caption>This will automatically define an schema</q-item-label>
  </q-item-section>

  <q-item-section side>
    <q-btn v-if="!getStartedCompleteUpload2ndDS()" color="primary" label="Start" icon-right="o_navigate_next" dense no-caps />
    <q-icon v-else name="check_circle" color="green"  />
  </q-item-section>

</q-item>

<q-item clickable style="padding:12px" v-ripple :active="active" @click="showIntegrationView">
  
  <q-item-section avatar>
    <!-- <q-icon :name="storeDS.getDatasourcesNumber > 1? 'check_circle':'o_merge'" :color="storeDS.getDatasourcesNumber > 1? 'green':null"  /> -->
      <q-btn flat padding="xs" icon="o_merge" color="primary600" class="activebg"/>
  </q-item-section>

  <q-item-section>
    <q-item-label>Integrate second data source with the project</q-item-label>
    <q-item-label caption>The project schema must be aligned with the second data source schema </q-item-label>
    <q-item-label caption>Upon confirmation, it will be saved into this project</q-item-label>
  </q-item-section>

  <q-item-section side>
    <q-btn v-if="storeDS.getDatasourcesNumber < 2" color="primary" label="Start" icon-right="o_navigate_next" dense no-caps />
    <q-icon v-else name="check_circle" color="green"  />
  </q-item-section>

</q-item>

<q-item clickable style="padding:12px" v-ripple :active="active">
  
  <q-item-section avatar>
    <q-btn flat padding="xs" icon="mdi-selection-search" color="primary600" class="activebg"/>
  </q-item-section>

  <q-item-section>
    <q-item-label>Explore the integrated data</q-item-label>
    <q-item-label caption>Not available for this survey</q-item-label>
  </q-item-section>

  <q-item-section side>
    <!-- <q-icon name="o_navigate_next" color="green" /> -->
    <q-btn color="primary" label="Start" icon-right="o_navigate_next" dense no-caps />
  </q-item-section>

</q-item>


</q-list>


          </q-card-section>
        </q-card>
      </q-expansion-item>

     </div>


    <FormNewDataSource v-model:show="addDataSource" :afterSubmitShowGraph="false"></FormNewDataSource>

    </q-page>
  </template>
  
<script setup>
import { ref, onBeforeMount } from "vue";
import { useRouter } from "vue-router";
import FormNewDataSource from "components/forms/FormNewDataSource.vue";
import { useDataSourceStore } from 'src/stores/datasources.store.js'
import { useIntegrationStore } from 'src/stores/integration.store.js'
import home_pattern from "components/icons/home_pattern.vue";

import { useAuthStore } from 'stores/auth.store.js'

const addDataSource = ref(false)
const active = ref(false)

const router = useRouter()

const authStore = useAuthStore()

const storeDS = useDataSourceStore();
const integrationStore = useIntegrationStore()

onBeforeMount( () => {
    storeDS.setProject()
    integrationStore.setProject()
})

const showIntegrationView = () => {
  integrationStore.SelectOneDatasource();
  router.push({name:'dsIntegration'})
}

const getStartedCompleteUpload2ndDS = () => {


  if(storeDS.getDatasourcesNumber > 1){
    return true;
  } else if(integrationStore.getDatasourcesNumber > 0 && storeDS.getDatasourcesNumber >= 1){
    return true;
  }
  return false;

}

const progress = () => {

  if(storeDS.getDatasourcesNumber >= 2){
    return 0.8
  } else if(integrationStore.getDatasourcesNumber == 1 && storeDS.getDatasourcesNumber >= 1 ) {
    return 0.6
  } else if(integrationStore.getDatasourcesNumber == 1) {
    return 0.2
  } else if ( storeDS.getDatasourcesNumber == 1 ) {
    return 0.4
  } else {
    return 0
  }

}

</script>
  
<style lang="scss">
</style>  