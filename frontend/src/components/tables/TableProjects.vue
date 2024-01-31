<template>

  <q-table :grid="gridEnable" :rows="projectsStore.projects" :columns="columns"
           virtual-scroll
           :v-model:pagination="{rowsPerPage: 0}"
           :rows-per-page-options="[0]"
           row-key="id"
           no-results-label="The filter didn't uncover any results">
    <template v-slot:top-left>
      <div class="q-table__title">
        <span> Projects  </span>
        <q-btn padding="none" color="secondary" icon="add" @click="showDialog = true"/>
      </div>
    </template>

    <template v-slot:top-right>
      <q-btn unelevated padding="none" color="primary700" icon="list"
             @click="gridEnable = !gridEnable"/>
    </template>

    <template v-slot:no-data>
        <div class="full-width row flex-center text-accent q-gutter-sm q-pa-xl" style="flex-direction: column">
          <NoDataImage/>
          <span style="color: rgb(102, 102, 135);font-weight: 500;font-size: 1rem;line-height: 1.25;">No projects.</span>
        </div>
      </template>

    <template v-slot:item="props">
      <div class="q-pa-md col-xs-12 col-sm-6 col-md-2 col-lg-2">
        <Folder :row="props.row" :folderColor="props.row.color"></Folder>
      </div>
    </template>
  </q-table>

  <CreateFolderForm v-model:show="showDialog"/>

</template>

<script setup>
import {ref, onMounted} from 'vue'
import {useProjectsStore} from 'stores/projectsStore.js'
import CreateFolderForm from 'components/forms/CreateFolderForm.vue';
import Folder from 'components/projects/Folder.vue'
import NoDataImage from 'src/assets/NoDataImage.vue';

const projectsStore = useProjectsStore()

const gridEnable = ref(true)
const showDialog = ref(false)


const columns = [
  {name: "id", required: true, label: "ID", align: "center", field: "projectId", sortable: true,},
  {name: "name", required: true, label: "Name", align: "center", field: "projectName", sortable: true,},
  {
    name: "datasets",
    required: true,
    label: "# Datasets",
    align: "center",
    field: "datasets",
    sortable: true,
    format: (value, row) => row.repositories.reduce((total, repo) => total + repo.datasets.length, 0)
  },
  {name: "createdBy", required: true, label: "Created by", align: "center", field: "createdBy", sortable: true,},
  {name: "privacy", required: true, label: "Privacy", align: "center", field: "projectPrivacy", sortable: true,}
];

onMounted(() => {
  projectsStore.getProjects()
})

</script>
