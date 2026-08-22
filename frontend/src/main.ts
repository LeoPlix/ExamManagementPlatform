import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// Vuetify
import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import { aliases, mdi } from 'vuetify/iconsets/mdi'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import 'material-design-icons-iconfont/dist/material-design-icons.css'
import '@mdi/font/css/materialdesignicons.css'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

const app = createApp(App)

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
app.use(pinia)
app.use(router)

const vuetify = createVuetify({
  icons: {
    defaultSet: 'mdi',
    aliases,
    sets: {
      mdi
    }
  },
  components,
  directives,
  theme: {
    defaultTheme: 'light',
    themes: {
      light: {
        dark: false,
        colors: {
          primary: '#1d4ed8',
          'primary-darken-1': '#1e40af',
          secondary: '#334155',
          accent: '#0284c7',
          error: '#dc2626',
          info: '#0284c7',
          success: '#059669',
          warning: '#d97706',
          background: '#f8fafc',
          surface: '#ffffff',
          'surface-variant': '#f1f5f9',
          contrast: '#0f172a'
        }
      },
      dark: {
        dark: true,
        colors: {
          primary: '#3b82f6',
          'primary-darken-1': '#2563eb',
          secondary: '#64748b',
          accent: '#38bdf8',
          error: '#ef4444',
          info: '#38bdf8',
          success: '#10b981',
          warning: '#f59e0b',
          background: '#0b0f19',
          surface: '#131b2e',
          'surface-variant': '#1e293b',
          contrast: '#f8fafc'
        }
      }
    }
  }
})

app.use(vuetify)
app.mount('#app')
