<template>
    <div class="q-pa-md">
      <q-table :rows="rows" :columns="columns" :filter="search" row-key="id"
               no-data-label="No queries created yet"
               no-results-label="The filter didn't uncover any results" :visible-columns="visibleColumns">
  
        <template v-slot:top-left="">
          <div class="q-table__title">
            Data products
          </div>
        </template>
  
        <template v-slot:top-right="props">
  
          <q-input outlined dense debounce="400" color="primary" v-model="search">
            <template v-slot:append>
              <q-icon name="search"/>
            </template>
          </q-input>
  
          <q-btn flat round dense size="xl" :icon="props.inFullscreen ? 'fullscreen_exit' : 'fullscreen'"
                 @click="props.toggleFullscreen">
            <q-tooltip :disable="$q.platform.is.mobile" v-close-popup>
              {{ props.inFullscreen ? "Exit Fullscreen" : "Toggle Fullscreen" }}
            </q-tooltip>
          </q-btn>
        </template>
  
        <template v-slot:body-cell-actions="props">
          <q-td :props="props">
            <q-btn dense round flat color="grey" @click="editRow(props)" icon="edit"></q-btn>
            <q-btn dense round flat color="grey" @click="deleteRow(props)" icon="delete"></q-btn>
          </q-td>
        </template>
  
      </q-table>
  
      <q-dialog v-model="editDataProduct">
        <q-card flat bordered class="my-card" style="min-width: 30vw;">
          <q-card-section class="q-pt-none">
            <EditDataProductForm
              @submit-success="editDataProduct=false"
              @cancel-form="editDataProduct=false"
              :dataProductData="selectedDataProduct"
            ></EditDataProductForm>
          </q-card-section>
        </q-card>
      </q-dialog>
  
      <ConfirmDialog v-model:show="showConfirmDialog" title="Confirm deletion of a data product" 
                      body="Do you really want to delete the data product?"
                      :onConfirm="confirmDelete"/>
    </div>
  </template>
  
  <script setup>
  import {computed, onMounted, ref} from "vue";
  import {useDataSourceStore} from 'src/stores/datasourcesStore.js'
  import {useDataProductsStore} from 'src/stores/dataProductsStore.js'
  import {useRoute} from "vue-router";
  import ConfirmDialog from "src/components/ConfirmDialog.vue";
  import EditDataProductForm from "src/components/forms/EditDataProductForm.vue";
  
  const route = useRoute()
  
  const storeDS = useDataSourceStore();
  const dataProductsStore = useDataProductsStore()
  
  onMounted(async() => {
    storeDS.setProject()
    await dataProductsStore.getDataProducts(route.params.id)
  })
  
  const selectedDataProduct = ref(null);
  const search = ref("")
  const visibleColumns = ["dataProductID", "dataProductName", "dataProductDescription", "expand", "actions"]; // Columns to be displayed
  
  const showConfirmDialog = ref(false)
  const editDataProduct = ref(false)
  
  const rows = computed(() => {
    return dataProductsStore.dataProducts.map((dp) => {
      return {
        ...dp,
      };
    });
  });
  
  const columns = [
    {name: "dataProductID", label: "ID", align: "center", field: "id", sortable: true},
    {name: "dataProductName", label: "Name", align: "center", field: "datasetName", sortable: true},
    {name: "dataProductDescription", label: "Description", align: "center", field: "datasetDescription", sortable: true},
    {name: "expand", label: "Query", align: "center", field: "expand", sortable: false},
    {name: 'actions', label: 'Actions', align: 'center', field: 'actions', sortable: false,},
  ];
  
  
  let confirmDelete = () => {}
  const deleteRow = (propsRow) => {
    showConfirmDialog.value = true
    confirmDelete = () => {
        dataProductsStore.deleteDataProduct(route.params.id, propsRow.row.id)
    }
  }
  
  const editRow = (propsRow) => {
    editDataProduct.value = true
    selectedDataProduct.value = propsRow.row
  }
  </script>
  
  <style lang="css" scoped>
  .centered-table {
    display: flex;
    justify-content: center; /* Centra horizontalmente el contenido de la tabla */
    align-items: center; /* Centra verticalmente el contenido de la tabla */
  }
  
  .centered-table table {
    width: 100%; /* Asegura que la tabla ocupe todo el ancho disponible */
  }
  </style>
  