<template>
  <v-overlay
    :model-value="appearanceStore.loading"
    class="align-center justify-center loading-overlay-backdrop"
    persistent
  >
    <v-card class="pa-6 text-center rounded-xl loading-card" elevation="8" color="surface">
      <v-progress-circular
        color="primary"
        size="52"
        width="4"
        indeterminate
        class="mb-3"
      ></v-progress-circular>
      <div class="text-subtitle-2 font-weight-bold">A processar...</div>
      <div class="text-caption text-medium-emphasis">Por favor aguarde</div>
    </v-card>
  </v-overlay>
</template>

<script setup lang="ts">
import { useAppearanceStore } from '@/stores/appearance'
import { onMounted } from 'vue'

const appearanceStore = useAppearanceStore()

onMounted(() => {
  appearanceStore.loading = false
})
</script>

<style scoped>
.loading-overlay-backdrop {
  backdrop-filter: blur(4px);
}

.loading-card {
  min-width: 180px;
}
</style>