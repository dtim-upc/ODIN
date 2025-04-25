<template>
  <q-dialog v-model="showComponent" persistent>
    <q-card flat bordered class="my-card" style="min-width: 30vw;">
      <q-card-section class="q-pt-none">
        <q-card-section>
          <div class="text-h6">Edit data product</div>
        </q-card-section>
        <q-card-section>
          <q-form ref="form" @submit="onSubmit" class="q-gutter-md">

            <q-input filled v-model="editedDataProduct.name" label="Data product name" lazy-rules 
                      :rules="[(val) => (val && val.length > 0) || 'Please type a name']" />

            <q-input filled v-model="editedDataProduct.description" label="Data product description" />

            <div class="text-right">
              <q-btn label="Update" type="submit" color="primary" v-close-popup />
              <q-btn label="Cancel" type="reset" color="primary" flat class="q-ml-sm" v-close-popup @click="emit('update:show', false)" />
            </div>
          </q-form>
        </q-card-section>
      </q-card-section>
    </q-card>
  </q-dialog>
</template>
  
<script setup>
import { reactive, ref, watch } from "vue";
import { useDataProductsStore } from "src/stores/dataProductsStore.js";
import { useProjectsStore } from "src/stores/projectsStore.js";

const props = defineProps({
  show: {type: Boolean, default: false, required: true},
  dataProductData:{type: Object, default: null},
});

const dataProductsStore = useDataProductsStore()
const projectID = useProjectsStore().currentProject.projectId

// Emits to associate the show prop with the parent component
const emit = defineEmits(["update:show"])
const showComponent = ref(props.show);
watch(() => props.show, (newVal) => {
  showComponent.value = newVal
})

// Data to be edited, we need to watch it, because the parent component can change it (initially we do not know which row is selected)
const editedDataProduct = reactive({
  id: props.dataProductData?.id || null,
  name: props.dataProductData?.datasetName || '',
  description: props.dataProductData?.datasetDescription || '',
});
watch(() => props.dataProductData, (newVal) => {
  editedDataProduct.name = newVal ? newVal.datasetName : null;
  editedDataProduct.description = newVal ? newVal.datasetDescription : '';
}, { immediate: true });

const onSubmit = () => {
  const data = new FormData();
  data.append("dataProductName", editedDataProduct.name);
  data.append("dataProductDescription", editedDataProduct.description);
  dataProductsStore.putDataProduct(editedDataProduct.id, projectID, data, () => emit('update:show', false))
};
</script>