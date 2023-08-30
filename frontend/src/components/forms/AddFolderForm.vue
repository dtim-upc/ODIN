<template>

  <q-form ref="form" @submit="onSubmit" @reset="onReset" class="q-gutter-md">

    <q-input
      filled
      v-model="project.projectName"
      label="Project name"
      lazy-rules
      :rules="[(val) => (val && val.length > 0) || 'Please type a name', ]"
    />

    <q-input v-model="project.projectDescription" filled autogrow label="Description (Optional)"/>

    <q-select v-model="project.projectPrivacy" :options="optionsPrivacy" label="Privacy" class="q-mt-none"/>

    <q-select v-model="project.projectColor" :options="optionsColor" label="Color" class="q-mt-none"/>

    <div v-if="showFormButtons">
      <q-btn
        :label="props.projectData ? 'Update' : 'Submit'"
        type="submit"
        color="primary"
      />
      <q-btn label="Cancel" type="reset" color="primary" flat class="q-ml-sm" @click="cancelForm()"/>
    </div>
  </q-form>
</template>

<script setup>
import {ref, reactive, defineProps} from "vue";
import {useNotify} from 'src/use/useNotify.js';
import {useProjectsStore} from 'stores/projects.store.js';

const props = defineProps({
  showFormButtons: {type: Boolean, default: true},
  projectData: {type: Object, default: null}, // Recibimos los datos del proyecto a través de las props
});

const projectsStore = useProjectsStore();
const emit = defineEmits(["submitSuccess", "cancelForm"]);
const form = ref(null);
const notify = useNotify();

// Inicializamos project con los datos recibidos a través de las props
const project = reactive({
  projectId: props.projectData ? props.projectData.projectId : null,
  projectName: props.projectData ? props.projectData.projectName : "",
  projectDescription: props.projectData ? props.projectData.projectDescription : "",
  projectPrivacy: props.projectData ? props.projectData.projectPrivacy : "private",
  projectColor: props.projectData ? props.projectData.projectColor : "#dbe2e7",
});

const optionsPrivacy = ["private", "public"];
const optionsColor = ["#dbe2e7", "#4e68f5"];

const onReset = () => {
  project.projectId = null;
  project.projectName = "";
  project.projectDescription = "";
  project.projectPrivacy = "private";
  project.projectColor = "#dbe2e7";
};

const success = () => {
  form.value.resetValidation();
  emit("submitSuccess");
};

const cancelForm = () => {
  form.value.resetValidation();
  emit("cancelForm");
};

const onSubmit = () => {
  if (props.projectData) {
    // If projectData is available, it means we are editing an existing project
    // Perform edit logic here
    projectsStore.editProject(project, success);
  } else {
    // If projectData is not available, it means we are creating a new project
    projectsStore.createProject(project, success);
  }
};

</script>


<style lang="css" scoped>
</style>
