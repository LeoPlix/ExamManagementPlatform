<template>
  <v-app-bar elevation="0" class="border-b px-2 px-md-4 main-navbar" color="surface" :height="62">
    <!-- Brand / Home Link -->
    <v-btn
      to="/"
      variant="text"
      class="text-none font-weight-bold d-flex align-center text-subtitle-1 brand-btn mr-2"
    >
      <v-avatar color="primary" variant="tonal" size="34" class="mr-2">
        <v-icon icon="mdi-shield-check" color="primary" size="22"></v-icon>
      </v-avatar>
      <span>EduQa-nos <span class="text-primary font-weight-extrabold">EMS</span></span>
    </v-btn>

    <!-- Desktop Navigation Items -->
    <div class="d-none d-md-flex align-center flex-wrap">
      <v-btn
        v-for="item in navbarItems"
        :key="item.name"
        :to="item.path"
        class="text-none mx-1 font-weight-medium nav-link-btn"
        variant="text"
        size="small"
        active-class="nav-link-active"
      >
        <v-icon :icon="item.icon" class="mr-1" size="18"></v-icon>
        {{ item.name }}
      </v-btn>
    </div>

    <v-spacer></v-spacer>

    <!-- Right Side Actions: Role Badge, Theme Switcher, Logout -->
    <div class="d-flex align-center gap-2">
      <!-- Role Badge (Only the role) -->
      <v-chip
        v-if="auth.user"
        :color="roleColor"
        size="small"
        variant="flat"
        class="font-weight-bold mr-1 d-none d-sm-inline-flex role-pill"
      >
        <v-icon start icon="mdi-shield-account" size="16" class="mr-1"></v-icon>
        {{ roleLabel }}
      </v-chip>

      <!-- Theme Switcher -->
      <DarkModeSwitch />

      <!-- Logout Button -->
      <v-btn
        v-if="auth.user"
        variant="text"
        color="error"
        size="small"
        class="text-none font-weight-bold ml-1 logout-btn"
        @click="logout"
      >
        <span class="d-none d-sm-inline">Sair</span>
        <v-icon size="18" class="ms-sm-1" icon="mdi-logout"></v-icon>
      </v-btn>

      <!-- Mobile Hamburger Menu Button -->
      <v-menu v-if="navbarItems && navbarItems.length > 0" class="d-flex d-md-none">
        <template v-slot:activator="{ props }">
          <v-btn
            v-bind="props"
            icon="mdi-menu"
            variant="text"
            class="d-flex d-md-none ml-1"
            aria-label="Abrir Menu de Navegação"
          ></v-btn>
        </template>
        <v-list class="py-2" elevation="4" rounded="xl">
          <v-list-item v-if="auth.user" class="border-b mb-1">
            <v-chip :color="roleColor" size="small" variant="flat" class="font-weight-bold">
              {{ roleLabel }}
            </v-chip>
          </v-list-item>
          <v-list-item
            v-for="item in navbarItems"
            :key="item.name"
            :to="item.path"
            :prepend-icon="item.icon"
            :title="item.name"
            active-class="text-primary font-weight-bold"
          ></v-list-item>
        </v-list>
      </v-menu>
    </div>
  </v-app-bar>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import DarkModeSwitch from './DarkModeSwitch.vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

defineProps<{ navbarItems: { name: string; path: string; icon: string }[] }>()

const auth = useAuthStore()
const router = useRouter()

const ROLE_LABELS: Record<string, string> = {
  ADMINISTRATOR: 'Administrador',
  SCHOOL_STAFF: 'Funcionário Escolar',
  TEACHER: 'Professor',
  STUDENT: 'Aluno'
}

const roleLabel = computed(() => {
  return auth.user?.role ? (ROLE_LABELS[auth.user.role] ?? auth.user.role) : ''
})

const roleColor = computed(() => {
  switch (auth.user?.role) {
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
})

const logout = () => {
  auth.logout()
  router.push({ name: 'login' })
}
</script>

<style scoped>
.main-navbar {
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(100, 116, 139, 0.2) !important;
  z-index: 1005;
}

.brand-btn {
  letter-spacing: -0.01em;
  font-size: 1.05rem !important;
  opacity: 1 !important;
}

.nav-link-btn {
  border-radius: 8px;
  transition: all 0.2s ease;
  opacity: 1 !important;
  font-weight: 600 !important;
  font-size: 0.875rem !important;
}

.nav-link-btn:hover {
  background-color: rgba(var(--v-theme-primary), 0.1) !important;
  color: rgb(var(--v-theme-primary)) !important;
}

.nav-link-active {
  opacity: 1 !important;
  color: rgb(var(--v-theme-primary)) !important;
  background-color: rgba(var(--v-theme-primary), 0.15) !important;
  font-weight: 700 !important;
}

.role-pill {
  letter-spacing: 0.02em;
  font-size: 0.8125rem !important;
}

.logout-btn {
  font-size: 0.875rem !important;
  opacity: 1 !important;
}

.logout-btn:hover {
  background-color: rgba(var(--v-theme-error), 0.1) !important;
}
</style>
