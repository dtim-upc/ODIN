<template>
  <q-dialog v-model="showComponent" persistent>
    <q-card flat bordered class="my-card" style="min-width: 30vw;">
      <q-card-section class="q-pt-none">
        <q-card-section>
          <div class="text-h6">Edit repository</div>
        </q-card-section>
        <q-card-section>
          <q-form ref="form" @submit="onSubmit" class="q-gutter-md">

            <q-input filled v-model="editedRepository.name" label="Repository name" lazy-rules
                      :rules="[(val) => (val && val.length > 0) || 'Please type a name', ]"/>

            <div>
              <q-btn label="Update" type="submit" color="primary" v-close-popup/>
              <q-btn label="Cancel" type="reset" color="primary" flat class="q-ml-sm" v-close-popup @click="emit('update:show', false)"/>
            </div>
          </q-form>
        </q-card-section>
      </q-card-section>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { reactive, ref, watch } from "vue";
import { useRepositoriesStore } from "src/stores/repositoriesStore";
import { useProjectsStore } from "src/stores/projectsStore";

const props = defineProps({
  show: {type: Boolean, default: false, required: true},
  repositoryData:{type: Object, default: null},
});

const repositoriesStore = useRepositoriesStore();
const projectID = useProjectsStore().currentProject.projectId

// Emits to associate the show prop with the parent component
const emit = defineEmits(["update:show"])
const showComponent = ref(props.show);
watch(() => props.show, (newVal) => {
  showComponent.value = newVal
})

// Data to be edited, we need to watch it, because the parent component can change it (initially we do not know which row is selected)
const editedRepository = reactive({
  id: props.repositoryData ? props.repositoryData.id : null,
  name: props.repositoryData ? props.repositoryData.repositoryName : '',
})
watch(() => props.repositoryData, (newVal) => {
  editedRepository.id = newVal ? newVal.id : null;
  editedRepository.name = newVal ? newVal.repositoryName : '';
}, { immediate: true });

const onSubmit = () => {
  const data = new FormData();
  data.append("repositoryID", editedRepository.id);
  data.append("repositoryName", editedRepository.name);
  repositoriesStore.putRepository(editedRepository.id, projectID, data, () => emit('update:show', false))
}

</script>