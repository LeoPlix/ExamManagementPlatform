<template>
  <v-tooltip :text="appearanceStore.isDarkTheme ? 'Mudar para Modo Claro' : 'Mudar para Modo Escuro'" location="bottom">
    <template v-slot:activator="{ props }">
      <v-btn
        v-bind="props"
        icon
        size="small"
        variant="tonal"
        :color="appearanceStore.isDarkTheme ? 'amber' : 'primary'"
        class="theme-toggle-btn"
        @click="toggleTheme"
        :aria-label="appearanceStore.isDarkTheme ? 'Modo Claro' : 'Modo Escuro'"
      >
        <v-icon
          :icon="appearanceStore.isDarkTheme ? 'mdi-weather-sunny' : 'mdi-weather-night'"
          size="20"
          :color="appearanceStore.isDarkTheme ? '#f59e0b' : '#1d4ed8'"
        ></v-icon>
      </v-btn>
    </template>
  </v-tooltip>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useTheme } from 'vuetify'
import { useAppearanceStore } from '@/stores/appearance'

const theme = useTheme()
const appearanceStore = useAppearanceStore()

const applyTheme = () => {
  theme.global.name.value = appearanceStore.currentTheme
}

onMounted(() => {
  applyTheme()
})

const toggleTheme = () => {
  appearanceStore.toggleTheme()
  applyTheme()
}
</script>

<style scoped>
.theme-toggle-btn {
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1), background-color 0.2s ease;
}

.theme-toggle-btn:hover {
  transform: rotate(15deg) scale(1.08);
}
</style>
