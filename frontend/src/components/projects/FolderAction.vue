<template>
    <q-item clickable v-close-popup @click="onItemClick">
        <q-item-section avatar style="min-width: 30px;padding:0">
            <q-icon :color="color" :name="icon"/>
        </q-item-section>
        <q-item-section>
            <q-item-label>{{ label }}</q-item-label>
        </q-item-section>
    </q-item>
</template>

<script setup>

const props = defineProps({
    icon: { type: String, required: true },
    label: { type: String, required: true },
    onItemClick: { type: Function, required: true },
    color: { type: String, default: 'primary' }
});
</script>