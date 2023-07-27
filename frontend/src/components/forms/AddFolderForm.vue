<template>

        <q-form ref="form" @submit="onSubmit" @reset="onReset" class="q-gutter-md">
          <q-input filled v-model="project.projectName" label="Project name" lazy-rules
                   :rules="[(val) => (val && val.length > 0) || 'Please type a name', ]"/>

          <q-input v-model="project.projectDescription" filled autogrow label="Description (Optional)"/>

          <q-select v-model="project.projectPrivacy" :options="optionsPrivacy" label="Privacy" class="q-mt-none"/>

          <q-select v-model="project.projectColor" :options="optionsColor" label="Color" class="q-mt-none"/>

          <div v-if="showFormButtons" >
            <q-btn label="Submit" type="submit" color="primary"/>
            <q-btn label="Cancel" type="reset" color="primary" flat class="q-ml-sm" @click="cancelForm()"/>
          </div>
        </q-form>


</template>

<script setup>
import {ref, reactive, onMounted} from "vue";
import api from "src/api/dataSourcesAPI.js";
import {useNotify} from 'src/use/useNotify.js'
import { useDataSourceStore } from 'src/stores/datasources.store.js'
import { useProjectsStore } from 'stores/projects.store.js'

const props = defineProps({
    showFormButtons: { type: Boolean, default: true },
});
const projectsStore = useProjectsStore()

// onMounted(() => {
//     projectsStore.initStores()
// })

const emit = defineEmits(["submitSuccess","cancelForm"])
const form = ref(null)
const notify = useNotify()

defineExpose({
  form
})

const project = reactive({
    projectName : "",
    projectDescription : "",
    projectPrivacy : "private",
    projectColor:"#dbe2e7"
})



const optionsPrivacy = ["private", "public"]
const optionsColor = ["#dbe2e7", "#4e68f5"]


    // I think no needed since component is unmounted and then mounted with reset project
    const onReset = () => {

      project.projectName = ""
      project.projectDescription = ""
      project.projectPrivacy = "private"
      project.projectColor = "#dbe2e7"

    }

    const success = () => {
        //  console.log("success callback")
         form.value.resetValidation()
        //  console.log("reset validation")
         emit("submitSuccess")
    }

    const cancelForm = () => {
      form.value.resetValidation()
      emit("cancelForm")
    }


    const onSubmit = () => {
        projectsStore.createProject(project, success )
    }





</script>

<style lang="css" scoped>
</style>
