

<template>
  <div>
    <q-dialog v-model="dialogVisible" persistent>
      <q-card>
        <q-card-section>
          <div class="text-h6">{{ props.title }} </div>
        </q-card-section>

        <q-card-section>
          <div class="text-body1"> {{ props.body }} </div>
        </q-card-section>

        <q-card-actions align="right">
          <q-btn label="Confirm" color="primary" v-close-popup @click="confirm"/>
          <q-btn flat label="Cancel" color="primary" v-close-popup @click="cancel"/>
        </q-card-actions>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup>
import { ref, defineEmits, watch } from 'vue';

const props = defineProps({
    show: {type: Boolean, required: true},
    title: {type: String, default: 'Confirm'},
    body: {type: String, default: 'Confirm'},
    onConfirm: {type: Function, required: true}
});

const dialogVisible = ref(props.show);

const emits = defineEmits(['update:show'])

watch(() => props.show, (newVal) => {
  dialogVisible.value = newVal
})

const confirm = () => {
  props.onConfirm()
  emits('update:show', false)
}

const cancel = () => {
  emits('update:show', false)
}
</script>
