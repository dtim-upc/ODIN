<template>
    <q-card-section>
      <div class="text-h6">Edit data product</div>
    </q-card-section>
    <q-card-section>
      <q-form ref="form" @submit="onSubmit" class="q-gutter-md">
        <q-input filled v-model="editedDataProduct.name" label="Data product name" lazy-rules :rules="nameRules" />
        <q-input filled v-model="editedDataProduct.description" label="Data product description" />
        <div class="text-right">
          <q-btn label="Update" type="submit" color="primary" v-close-popup />
          <q-btn label="Cancel" type="reset" color="primary" flat class="q-ml-sm" v-close-popup />
        </div>
      </q-form>
    </q-card-section>
  </template>
  
  <script setup>
  import { defineProps, reactive } from "vue";
  import { useDataProductsStore } from "src/stores/dataProductsStore.js";
  import { useRoute } from "vue-router";
  
  const dataProductsStore = useDataProductsStore();
  const route = useRoute();
  
  const props = defineProps({
    dataProductData: { type: Object, default: null },
  });
  
  const editedDataProduct = reactive({
    id: props.dataProductData?.id || null,
    name: props.dataProductData?.datasetName || '',
    description: props.dataProductData?.datasetDescription || '',
  });
  
  const nameRules = [(val) => (val && val.length > 0) || 'Please type a name'];
  
  const onSubmit = () => {
    const data = new FormData();
    data.append("dataProductID", editedDataProduct.id);
    data.append("dataProductName", editedDataProduct.name);
    data.append("dataProductDescription", editedDataProduct.description);
    dataProductsStore.putDataProduct(editedDataProduct.id, route.params.id, data)
  };
  </script>