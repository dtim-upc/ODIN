<template>
  <q-dialog v-model="showComponent" persistent>
    <q-card flat bordered class="my-card" style="min-width: 30vw;">
      <q-card-section class="q-pt-none">
        <q-card-section>
          <div class="text-h6">Edit dataset</div>
        </q-card-section>
        <q-card-section>
          <q-form ref="form" @submit="onSubmit" class="q-gutter-md">

            <q-input filled v-model="editedDataset.name" label="Dataset name" lazy-rules
                      :rules="[(val) => (val && val.length > 0) || 'Please type a name', ]"/>

            <q-input v-model="editedDataset.description" filled autogrow label="Dataset description"/>

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
import { ref, reactive, watch } from "vue";
import { useDatasetsStore } from "stores/datasetsStore";
import { useProjectsStore } from "stores/projectsStore";

const props = defineProps({
  show: {type: Boolean, default: false, required: true},
  datasetData:{type: Object, default: null},
});

const datasetsStore = useDatasetsStore();
const projectID = useProjectsStore().currentProject.projectId

// Emits to associate the show prop with the parent component
const emit = defineEmits(["update:show"])
const showComponent = ref(props.show);
watch(() => props.show, (newVal) => {
  showComponent.value = newVal
})

// Data to be edited, we need to watch it, because the parent component can change it (initially we do not know which row is selected)
const editedDataset = reactive({
  id: props.datasetData ? props.datasetData.id : null,
  name: props.datasetData ? props.datasetData.datasetName : '',
  description: props.datasetDescription ? props.datasetData.datasetDescription : '',
})
watch(() => props.datasetData, (newVal) => {
  editedDataset.id = newVal ? newVal.id : null;
  editedDataset.name = newVal ? newVal.datasetName : '';
  editedDataset.description = newVal ? newVal.datasetDescription : '';
}, { immediate: true });


const onSubmit = () => {
  const data = new FormData();
  data.append("datasetName", editedDataset.name);
  data.append("datasetDescription", editedDataset.description);
  datasetsStore.putDataset(projectID, editedDataset.id, data, () => emit('update:show', false))
  showComponent.value = false;
}

</script>