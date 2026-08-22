<template>
  <v-app id="app" :theme="appearanceStore.currentTheme">
    <TopBar v-if="authStore.isAuthenticated" />

    <v-main class="app-main-content">
      <v-container fluid class="py-6 px-4 px-md-8 app-container">
        <ErrorMessage />
        <LoadingOverlay />

        <RouterView v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </RouterView>
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
import TopBar from '@/components/TopBar.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'
import LoadingOverlay from '@/components/LoadingOverlay.vue'

import { RouterView } from 'vue-router'
import { useAppearanceStore } from './stores/appearance'
import { useAuthStore } from './stores/auth'
import { onMounted, onUnmounted } from 'vue'

const appearanceStore = useAppearanceStore()
const authStore = useAuthStore()

const setWidth = () => (appearanceStore.windowWidth = window.innerWidth)
setWidth()

onMounted(async () => {
  window.addEventListener('resize', setWidth)
  appearanceStore.clearErrors()

  // Refresh identity/permissions if a persisted token exists.
  if (authStore.isAuthenticated) {
    try {
      await authStore.fetchMe()
    } catch {
      authStore.logout()
    }
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', setWidth)
})
</script>

<style>
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-main-content {
  background-color: rgb(var(--v-theme-background));
  min-height: 100vh;
  transition: background-color 0.25s ease;
}

.app-container {
  max-width: 1440px;
  margin: 0 auto;
}

/* Page Transition Animation */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(6px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
