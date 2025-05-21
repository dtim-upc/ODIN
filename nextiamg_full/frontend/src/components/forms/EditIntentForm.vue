<template>
  <q-dialog v-model="showComponent" persistent>
    <q-card flat bordered class="my-card" style="min-width: 30vw;">
      <q-card-section class="q-pt-none">
        <q-card-section>
          <div class="text-h6">Edit intent</div>
        </q-card-section>
        <q-card-section>
          <q-form ref="form" @submit="onSubmit" class="q-gutter-md">

            <q-input filled v-model="editedIntent.name" label="Intent name" lazy-rules 
                      :rules="[(val) => (val && val.length > 0) || 'Please type a name']" />
            <div class="text-right">
              <q-btn label="Update" type="submit" color="primary" v-close-popup />
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
import { useIntentsStore } from "src/stores/intentsStore.js";
import { useProjectsStore } from "src/stores/projectsStore.js";

const props = defineProps({
  show: {type: Boolean, default: false, required: true},
  intentData: { type: Object, default: null },
});

const intentsStore = useIntentsStore();
const projectID = useProjectsStore().currentProject.projectId

// Emits to associate the show prop with the parent component
const emit = defineEmits(["update:show"])
const showComponent = ref(props.show);
watch(() => props.show, (newVal) => {
  showComponent.value = newVal
})

// Data to be edited, we need to watch it, because the parent component can change it (initially we do not know which row is selected)
const editedIntent = reactive({
  id: props.intentData ? props.intentData.intentID : null,
  name: props.intentData ? props.intentData.intentName : '',
})
watch(() => props.intentData, (newVal) => {
  editedIntent.id = newVal ? newVal.intentID : null;
  editedIntent.name = newVal ? newVal.intentName : '';
}, { immediate: true });

const onSubmit = () => {
  const data = new FormData();
  data.append("intentName", editedIntent.name);
  intentsStore.putIntent(editedIntent.id, projectID, data, () => emit('update:show', false))
}
</script>