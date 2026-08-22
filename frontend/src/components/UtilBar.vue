<template>
  <v-app-bar
    :order="-1"
    :height="40"
    elevation="0"
    color="surface-variant"
    class="border-b px-2 px-md-4 util-top-bar"
  >
    <!-- DEI Link & Institutional Affiliation -->
    <div class="d-flex align-center">
      <v-btn
        href="https://dei.tecnico.ulisboa.pt/"
        target="_blank"
        rel="noopener noreferrer"
        variant="text"
        size="small"
        class="text-none font-weight-bold dei-link px-2"
      >
        <v-icon icon="mdi-school" size="16" class="mr-1 text-primary"></v-icon>
        <span>Departamento de Engenharia Informática</span>
      </v-btn>
    </div>

    <v-spacer></v-spacer>

    <!-- User Role Badge & Utility Actions -->
    <div class="d-flex align-center gap-2">
      <!-- Role Badge (Only the role, without user name as requested) -->
      <v-chip
        v-if="authStore.user"
        :color="roleColor(authStore.user.role)"
        size="small"
        variant="flat"
        class="font-weight-bold mr-2 role-pill"
      >
        <v-icon start icon="mdi-shield-account" size="16" class="mr-1"></v-icon>
        {{ roleLabel(authStore.user.role) }}
      </v-chip>

      <!-- Theme Switcher -->
      <DarkModeSwitch />

      <!-- Logout Button -->
      <v-btn
        size="small"
        variant="text"
        color="error"
        class="text-none font-weight-bold ml-1 logout-btn"
        @click="logout"
      >
        <span>Sair</span>
        <v-icon size="16" class="ms-1" icon="mdi-logout"></v-icon>
      </v-btn>
    </div>
  </v-app-bar>
</template>

<script setup lang="ts">
import DarkModeSwitch from './DarkModeSwitch.vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const ROLE_LABELS: Record<string, string> = {
  ADMINISTRATOR: 'Administrador',
  SCHOOL_STAFF: 'Funcionário Escolar',
  TEACHER: 'Professor',
  STUDENT: 'Aluno'
}

const roleLabel = (role: string) => ROLE_LABELS[role] ?? role

const roleColor = (role: string) => {
  switch (role) {
    case 'ADMINISTRATOR':
      return 'purple'
    case 'SCHOOL_STAFF':
      return 'indigo'
    case 'TEACHER':
      return 'teal'
    case 'STUDENT':
      return 'primary'
    default:
      return 'secondary'
  }
}

const logout = () => {
  authStore.logout()
  router.push({ name: 'login' })
}
</script>

<style scoped>
.util-top-bar {
  z-index: 1005;
  border-bottom: 1px solid rgba(100, 116, 139, 0.2) !important;
}

.dei-link {
  font-size: 0.8125rem !important;
  opacity: 1 !important;
  color: inherit !important;
}

.dei-link:hover {
  background-color: rgba(var(--v-theme-primary), 0.08) !important;
}

.role-pill {
  letter-spacing: 0.02em;
  font-size: 0.75rem !important;
}

.logout-btn {
  font-size: 0.8125rem !important;
  opacity: 1 !important;
}

.logout-btn:hover {
  background-color: rgba(var(--v-theme-error), 0.1) !important;
}
</style>
