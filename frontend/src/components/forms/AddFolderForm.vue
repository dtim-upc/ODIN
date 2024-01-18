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

    <q-select v-model="project.projectPrivacy"
              :options="optionsPrivacy"
              label="Privacy"
              class="q-mt-none">
      <template v-slot:prepend>
        <q-icon :name="project.projectPrivacy.icon"/>
      </template>
      <template v-slot:option="scope">
        <q-item v-bind="scope.itemProps">
          <q-item-section avatar>
            <q-icon :name="scope.opt.icon" />
          </q-item-section>
          <q-item-section>
            <q-item-label>{{ scope.opt.label }}</q-item-label>
            <q-item-label caption>{{ scope.opt.description }}</q-item-label>
          </q-item-section>
        </q-item>
      </template>
    </q-select>

    <q-select v-model="project.projectColor"
              :options="optionsColor"
              label="Color"
              class="q-mt-none"
              options-selected-class="text-deep-orange"
    >
      <template v-slot:prepend>
        <q-icon name="folder" :style="{ color: project.projectColor }"/>
      </template>
      <template v-slot:option="scope">
        <q-item v-bind="scope.itemProps">
          <q-item-section avatar>
            <q-icon name="fiber_manual_record" :style="{ color: scope.opt }"/>
          </q-item-section>
          <q-item-section>
            <q-item-label>{{ scope.opt }}</q-item-label>
          </q-item-section>
        </q-item>
      </template>
    </q-select>

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
import {optionsPrivacy} from "../ui/PrivacyOptions";

const props = defineProps({
  showFormButtons: {type: Boolean, default: true},
  projectData: {type: Object, default: null}, // Recibimos los datos del proyecto a través de las props
});

const projectsStore = useProjectsStore();
const emit = defineEmits(["submitSuccess", "cancelForm"]);
const form = ref(null);
const notify = useNotify();
const optionsColor = ['#3dbb94', '#ff5733', '#8866aa', '#f0c342', '#47a1e6', '#b547e6'];

// Inicializamos project con los datos recibidos a través de las props
const project = reactive({
  projectId: props.projectData ? props.projectData.projectId : null,
  projectName: props.projectData ? props.projectData.projectName : "",
  projectDescription: props.projectData ? props.projectData.projectDescription : "",
  projectPrivacy: props.projectData ? optionsPrivacy.find(option => option.value === props.projectData.projectPrivacy) : optionsPrivacy[0],
  projectColor: props.projectData ? props.projectData.projectColor : optionsColor[0],
});

const onReset = () => {
  project.projectId = null;
  project.projectName = "";
  project.projectDescription = "";
  project.projectPrivacy = optionsPrivacy[0];
  project.projectColor = optionsColor[0];
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
  project.projectPrivacy = project.projectPrivacy.value;
  if (props.projectData) {
    projectsStore.putProject(project, success);
  } else {
    projectsStore.postProject(project, success);
  }
};

</script>


<style lang="css" scoped>
</style>
