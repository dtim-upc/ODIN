<template>
  <q-dialog v-model="showComponent" persistent>
    <q-card flat bordered class="my-card" style="min-width: 30vw;">
      <q-card-section>
        <div class="text-h6">Project</div>
      </q-card-section>
      <q-card-section class="q-pt-none">
        <q-form ref="form" @submit="onSubmit" class="q-gutter-md">

          <q-input filled v-model="project.projectName" label="Project name" lazy-rules
            :rules="[(val) => (val && val.length > 0) || 'Please type a name', ]"/>

          <q-input v-model="project.projectDescription" filled autogrow label="Description (Optional)"/>

          <q-select v-model="project.projectPrivacy" :options="optionsPrivacy" label="Privacy" class="q-mt-none">

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

          <q-select v-model="project.projectColor" :options="optionsColor" label="Color" class="q-mt-none"
                    options-selected-class="text-deep-orange">

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

          <div>
            <q-btn :label="props.projectData ? 'Update' : 'Submit'" type="submit" color="primary"/>
            <q-btn label="Cancel" type="reset" color="primary" flat class="q-ml-sm" @click="cancelForm()"/>
          </div>
        </q-form>
      </q-card-section>
    </q-card>
  </q-dialog>
</template>

<script setup>
import {ref, reactive, watch} from "vue";
import {useProjectsStore} from 'stores/projectsStore.js';
import {optionsPrivacy} from '../projects/PrivacyOptions';

const props = defineProps({
  show: {type: Boolean, default: false, required: true},
  projectData: {type: Object, default: null},
});

const projectsStore = useProjectsStore()
const optionsColor = ['#3dbb94', '#ff5733', '#8866aa', '#f0c342', '#47a1e6', '#b547e6']
const form = ref(null)

// Emits to associate the show prop with the parent component
const emit = defineEmits(["update:show"])
const showComponent = ref(props.show);
watch(() => props.show, (newVal) => {
  showComponent.value = newVal
})

// Data to be edited, we need to watch it, because the parent component can change it (initially we do not know which row is selected)
const project = reactive({
  projectId: props.projectData ? props.projectData.projectId : null,
  projectName: props.projectData ? props.projectData.projectName : "",
  projectDescription: props.projectData ? props.projectData.projectDescription : "",
  projectPrivacy: props.projectData ? optionsPrivacy.find(option => option.value === props.projectData.projectPrivacy) : optionsPrivacy[0],
  projectColor: props.projectData ? props.projectData.projectColor : optionsColor[0],
});
watch(() => props.projectData, (newVal) => {
  project.projectId = newVal ? newVal.projectId : null;
  project.projectName = newVal ? newVal.projectName : '';
  project.projectDescription = newVal ? newVal.projectDescription : '';
  project.projectPrivacy = newVal ? optionsPrivacy.find(option => option.value === newVal.projectPrivacy) : optionsPrivacy[0];
  project.projectColor = newVal ? newVal.projectColor : optionsColor[0];
}, { immediate: true });

const cancelForm = () => {
  emit('update:show', false)
};

const onSubmit = () => {
  project.projectPrivacy = project.projectPrivacy.value;
  if (props.projectData) {
    projectsStore.putProject(project, () => emit('update:show', false));
  } else {
    projectsStore.postProject(project, () => emit('update:show', false));
  }
};

</script>

