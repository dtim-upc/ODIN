<template>
    <q-item :clickable="!isDisabled" :disable="isDisabled" @click="onClick">
      <q-item-section horizontal>
        <div class="icon-text">
          <q-icon :color="iconColor" :name="iconName" />
          <span>{{ actionLabel }}</span>
        </div>
      </q-item-section>
    </q-item>
  </template>
  
  <script setup>
  import { defineEmits } from 'vue';
  
  const props = defineProps({
    iconName: { type: String, required: true },
    actionLabel: { type: String, required: true },
    isDisabled: { type: Boolean, default: false },
    iconColor: { type: String, default: 'grey' }
  });
  
  const emit = defineEmits(['click']);
  
  const onClick = () => {
    emit('click');
  };
  </script>
  
  <style scoped>
  .icon-text {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  </style>