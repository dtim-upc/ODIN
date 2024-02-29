<template>
  <q-page class="flex flex-center">
    <home_pattern style="top:10;right:0;position:absolute;margin-right: 10px;width:500px;"></home_pattern>
    <div class="q-pa-md" style="min-width:550px; max-width: 850px">

      <q-expansion-item expand-separator class="bg-white-color" default-opened>

        <template v-slot:header>
          <q-item-section>
            Getting started
          </q-item-section>

          <q-item-section side>
            4 steps
          </q-item-section>

          <q-item-section style="position:absolute; bottom:0;left: 0;right: 0;">
            <q-linear-progress :value="progress()" rounded color="green" class="q-mt-sm"/>
          </q-item-section>

        </template>
        <q-card style="width:100%">
          <q-card-section>
            <q-list separator>

              <q-item clickable style="padding:12px" v-ripple :active="active" @click="router.push({ name: 'repositories'})">
                <q-item-section avatar>
                  <q-btn flat padding="xs" icon="mdi-database" color="primary600 " class="activebg"></q-btn>
                </q-item-section>
                <q-item-section>
                  <q-item-label>Create a repository</q-item-label>
                  <q-item-label caption>Needed to start uploading data</q-item-label>
                </q-item-section>
                <q-item-section side>
                  <q-btn v-if="datasourcesStore.datasets.length == 0"
                         color="primary" label="Start" icon-right="o_navigate_next" dense no-caps/>
                  <q-icon v-else name="check_circle" color="green"/>
                </q-item-section>
              </q-item>

              <q-item clickable style="padding:12px" v-ripple :active="active" @click="postDataset = true">
                <q-item-section avatar>
                  <q-btn flat padding="xs" icon="o_file_upload" color="primary600 " class="activebg"></q-btn>
                </q-item-section>
                <q-item-section>
                  <q-item-label>Upload a dataset</q-item-label>
                  <q-item-label caption>This will automatically define a schema</q-item-label>
                </q-item-section>
                <q-item-section side>
                  <q-btn v-if="datasourcesStore.datasets.length == 0"
                         color="primary" label="Start" icon-right="o_navigate_next" dense no-caps/>
                  <q-icon v-else name="check_circle" color="green"/>
                </q-item-section>
              </q-item>

              <q-item clickable style="padding:12px" v-ripple :active="active" @click="showIntegrationView">
                <q-item-section avatar>
                  <q-btn flat padding="xs" icon="o_archive" color="primary600 " class="activebg"></q-btn>
                </q-item-section>
                <q-item-section>
                  <q-item-label>Add the dataset to this project</q-item-label>
                  <q-item-label caption>You will see the generated schema here</q-item-label>
                  <q-item-label caption>Upon confirmation, it will be saved into this project</q-item-label>
                </q-item-section>
                <q-item-section side>
                  <q-btn v-if="datasourcesStore.getDatasetsNumber==0" color="primary" label="Start"
                         icon-right="o_navigate_next" dense no-caps/>
                  <q-icon v-else name="check_circle" color="green"/>
                </q-item-section>
              </q-item>

              <q-item clickable style="padding:12px" v-ripple :active="active" @click="postDataset = true">
                <q-item-section avatar>
                  <q-btn flat padding="xs" icon="o_file_upload" color="primary600" class="activebg"/>
                </q-item-section>
                <q-item-section>
                  <q-item-label>Upload a second dataset</q-item-label>
                  <q-item-label caption>This will automatically define an schema</q-item-label>
                </q-item-section>
                <q-item-section side>
                  <q-btn v-if="!getStartedCompleteUpload2ndDS()" color="primary" label="Start"
                         icon-right="o_navigate_next" dense no-caps/>
                  <q-icon v-else name="check_circle" color="green"/>
                </q-item-section>
              </q-item>

              <q-item clickable style="padding:12px" v-ripple :active="active" @click="showIntegrationView">
                <q-item-section avatar>
                  <q-btn flat padding="xs" icon="o_merge" color="primary600" class="activebg"/>
                </q-item-section>
                <q-item-section>
                  <q-item-label>Integrate second dataset with the project</q-item-label>
                  <q-item-label caption>The project schema must be aligned with the second dataset schema
                  </q-item-label>
                  <q-item-label caption>Upon confirmation, it will be saved into this project</q-item-label>
                </q-item-section>
                <q-item-section side>
                  <q-btn v-if="datasourcesStore.getDatasetsNumber < 2" color="primary" label="Start"
                         icon-right="o_navigate_next" dense no-caps/>
                  <q-icon v-else name="check_circle" color="green"/>
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
                  <q-btn color="primary" label="Start" icon-right="o_navigate_next" dense no-caps/>
                </q-item-section>
              </q-item>

            </q-list>
          </q-card-section>
        </q-card>
      </q-expansion-item>
    </div>

    <CreateDatasetForm v-model:show="postDataset" :afterSubmitShowGraph="false" />

  </q-page>
</template>

<script setup>
import {ref} from "vue";
import {useRouter} from "vue-router";
import CreateDatasetForm from "components/forms/CreateDatasetForm.vue";
import {useDatasetsStore} from 'src/stores/datasetsStore.js'
import home_pattern from "components/icons/home_pattern.vue";
import { useRoute } from "vue-router";

const postDataset = ref(false)
const active = ref(false)

const router = useRouter()
const route = useRoute()

const datasourcesStore = useDatasetsStore();

const showIntegrationView = () => {
  router.push({name: 'dsIntegration'})
}

const getStartedCompleteUpload2ndDS = () => {
  if (datasourcesStore.getDatasetsNumber > 1) {
    return true;
  }
  return false;
}

const progress = () => {
  // todo: remake
  return 0;
}

</script>
