<template>
  <q-dialog v-model="showS" @hide="props.show = false">
    <q-card style="width: 400px; max-width: 80vw">
      <q-card-section>
        <div style="overflow-y: auto; max-height: calc(80vh - 140px);">
          <div class="text-h5">Select your repository</div>
          <q-card-section>
            <template v-if="repositoriesStore.repositories.length === 0">
              <div class="text-h6 text-warning">
                No repositories available. Please create a new repository.
              </div>
            </template>
            <template v-else>
              <q-select
                filled
                v-model="selectedRepositoryId"
                :options="repositoriesStore.repositories"
                label="Repository"
                class="q-mt-none"
                emit-value
                map-options
                option-value="id"
                option-label="repositoryName"
                :rules="[(val) => !!val || 'Please select a repository']"
              />
            </template>
          </q-card-section>
        </div>
      </q-card-section>

      <q-form ref="form" @submit="onSubmit" class="q-gutter-md">
        <q-card-section>
          <div align="right">
            <q-btn label="Cancel" type="reset" color="primary" flat class="q-ml-sm" v-close-popup />
            <q-btn v-if="repositoriesStore.repositories.length === 0" label="Create Repository" color="primary" :to="{name:'repositories'}"/>
            <q-btn v-else label="Next" type="submit" color="primary" @click="nextStep" :disable="selectedRepositoryId === null"/>
          </div>
        </q-card-section>
      </q-form>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { useRepositoriesStore } from "src/stores/repositoriesStore";

const repositoriesStore = useRepositoriesStore();

const selectedRepositoryId = ref(null);

const props = defineProps({
  show: { type: Boolean, default: false, required: true },
});

const emit = defineEmits(["update:show", "repository-selected"]);
const showS = computed({
  get() {
    return props.show
  },
  set(newValue) {
    emit('update:show', newValue)
  }
});

const nextStep = () => {
  repositoriesStore.setSelectedRepository(selectedRepositoryId.value);
  emit("repository-selected", selectedRepositoryId); // send back to the parent component that the dataset has been selected
};

onMounted(async () => {
  repositoriesStore.selectedRepository = {};
});

</script>