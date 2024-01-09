<template>
  <q-card-section>
    <div class="text-h6">Edit repository</div>
  </q-card-section>
  <q-card-section>
    <q-form ref="form" @submit="onSubmit" @reset="onReset" class="q-gutter-md">

      <q-input filled v-model="editedRepository.name" label="Repository name" lazy-rules
                  :rules="[(val) => (val && val.length > 0) || 'Please type a name', ]"/>

      <div align="right">
        <q-btn label="Update" type="submit" color="primary" v-close-popup/>
        <q-btn label="Cancel" type="reset" color="primary" flat class="q-ml-sm" v-close-popup/>
      </div>
    </q-form>
  </q-card-section>
</template>

<script setup>
import { defineProps, reactive} from "vue";
import { useRepositoriesStore } from "src/stores/repositories.store";
import { useRoute } from "vue-router";


const repositoriesStore = useRepositoriesStore();
const route = useRoute()

const props = defineProps({
  repositoryData:{type: Object, default: null},
});

console.log(props.repositoryData)

const editedRepository = reactive({
  id: props.repositoryData ? props.repositoryData.id : null,
  name: props.repositoryData ? props.repositoryData.repositoryName : '',
})

const successCallback = () => {
  repositoriesStore.getRepositories(route.params.id) // get the repositories again to refresh the list of the store
}


const onSubmit = () => {
  const data = new FormData();
  data.append("repositoryID", editedRepository.id);
  data.append("repositoryName", editedRepository.name);
  repositoriesStore.editRepository(data, successCallback)
}

const onReset = () => {

}



</script>