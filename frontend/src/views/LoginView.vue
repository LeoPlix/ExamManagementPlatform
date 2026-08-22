<template>
  <div class="login-page-wrapper d-flex align-center justify-center">
    <!-- Top-right theme toggle for pre-login page -->
    <div class="login-theme-toggle">
      <DarkModeSwitch />
    </div>

    <v-card width="440" class="pa-8 rounded-2xl login-card border" elevation="6">
      <div class="text-center mb-6">
        <v-avatar color="primary" variant="tonal" size="56" class="mb-3">
          <v-icon icon="mdi-shield-check" color="primary" size="32"></v-icon>
        </v-avatar>
        <h1 class="text-h5 font-weight-bold mb-1">
          EduQa-nos <span class="text-primary font-weight-extrabold">EMS</span>
        </h1>
        <p class="text-caption text-medium-emphasis mb-0">
          Plataforma de Gestão e Avaliação de Exames Nacionais
        </p>
      </div>

      <v-divider class="mb-6"></v-divider>

      <div class="text-left mb-4">
        <h2 class="text-subtitle-1 font-weight-bold mb-1">Iniciar Sessão</h2>
        <p class="text-caption text-medium-emphasis">Insira as suas credenciais para aceder à plataforma.</p>
      </div>

      <v-form @submit.prevent="onSubmit">
        <v-text-field
          v-model="email"
          label="Endereço de Email"
          type="email"
          prepend-inner-icon="mdi-email-outline"
          autocomplete="username"
          variant="outlined"
          density="comfortable"
          required
          class="mb-2"
        ></v-text-field>

        <v-text-field
          v-model="password"
          label="Palavra-passe"
          :type="showPassword ? 'text' : 'password'"
          prepend-inner-icon="mdi-lock-outline"
          :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
          @click:append-inner="showPassword = !showPassword"
          autocomplete="current-password"
          variant="outlined"
          density="comfortable"
          required
          class="mb-3"
        ></v-text-field>

        <v-alert
          v-if="error"
          type="error"
          variant="tonal"
          density="compact"
          class="mb-4 text-caption rounded-lg"
          closable
          @click:close="error = ''"
        >
          {{ error }}
        </v-alert>

        <v-btn
          type="submit"
          color="primary"
          block
          size="large"
          class="text-none font-weight-bold py-3 rounded-xl mt-2"
          elevation="2"
          :loading="loading"
        >
          <span>Entrar no Sistema</span>
          <v-icon end icon="mdi-arrow-right" class="ml-1"></v-icon>
        </v-btn>
      </v-form>
    </v-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import DarkModeSwitch from '@/components/DarkModeSwitch.vue'

const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)
const showPassword = ref(false)

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const onSubmit = async () => {
  error.value = ''
  loading.value = true
  try {
    await auth.login(email.value, password.value)
    const redirect = route.query.redirect as string | undefined
    router.push(redirect ?? { name: 'home' })
  } catch (e: any) {
    error.value = e?.message ?? 'Falha na autenticação. Verifique o email e a palavra-passe.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page-wrapper {
  min-height: calc(100vh - 80px);
  position: relative;
}

.login-theme-toggle {
  position: absolute;
  top: 16px;
  right: 16px;
}

.login-card {
  background-color: rgb(var(--v-theme-surface));
  backdrop-filter: blur(8px);
}
</style>
