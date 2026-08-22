<template>
  <v-snackbar
    v-model="show"
    color="error"
    location="top"
    :timeout="6000"
    elevation="6"
    rounded="lg"
    class="error-toast"
  >
    <div class="d-flex align-center">
      <v-icon icon="mdi-alert-circle-outline" class="mr-2" size="22"></v-icon>
      <span class="font-weight-medium">{{ appearanceStore.currentErrorMessage }}</span>
    </div>
    <template v-slot:actions>
      <v-btn
        variant="text"
        color="white"
        icon="mdi-close"
        size="small"
        @click="show = false"
      ></v-btn>
    </template>
  </v-snackbar>
</template>

<script setup lang="ts">
import { useAppearanceStore } from '@/stores/appearance'
import { ref, watch, watchEffect } from 'vue'

const appearanceStore = useAppearanceStore()

const show = ref(false)
watchEffect(() => {
  show.value = !appearanceStore.isStackEmpty
})

watch(show, (value) => {
  if (!value) {
    appearanceStore.popError()
  }
})
</script>

<style scoped>
.error-toast {
  z-index: 9999;
}
</style>
