<template>
  <v-container fluid class="pa-0">
    <!-- Header Section -->
    <v-card variant="flat" class="pa-4 mb-4 rounded-xl border bg-surface">
      <v-row align="center" justify="space-between">
        <v-col cols="12" md="8" class="text-left">
          <div class="d-flex align-center gap-2 mb-1">
            <v-icon icon="mdi-school-outline" color="primary" size="28" class="mr-1"></v-icon>
            <h2 class="text-h6 font-weight-bold text-left mb-0">Gestão de Escolas</h2>
          </div>
          <p class="text-caption text-medium-emphasis mb-0">
            Administração das instituições de ensino e centros de realização de provas no sistema nacional.
          </p>
        </v-col>

        <v-col cols="12" md="4" class="text-md-right" v-if="auth.hasPermission('SCHOOL_CREATE')">
          <v-btn
            color="primary"
            prepend-icon="mdi-plus"
            class="text-none font-weight-bold"
            variant="flat"
            @click="openCreateDialog"
          >
            Adicionar Escola
          </v-btn>
        </v-col>
      </v-row>
    </v-card>

    <!-- Filters Bar -->
    <v-card variant="outlined" class="pa-3 mb-4 rounded-xl border bg-surface">
      <v-row dense align="center">
        <v-col cols="12" md="6">
          <v-text-field
            v-model="search"
            label="Pesquisar escola por nome, código ou região..."
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            hide-details
            clearable
          ></v-text-field>
        </v-col>
      </v-row>
    </v-card>

    <!-- Data Table -->
    <v-card variant="outlined" class="rounded-xl border bg-surface overflow-hidden">
      <v-data-table
        :headers="headers"
        :items="filteredSchools"
        :loading="loading"
        item-key="id"
        class="text-left"
        no-data-text="Sem escolas a apresentar."
      >
        <template v-slot:[`item.name`]="{ item }">
          <div class="d-flex align-center py-2">
            <v-avatar color="primary" variant="tonal" size="32" class="mr-3">
              <v-icon icon="mdi-school" size="18"></v-icon>
            </v-avatar>
            <span class="font-weight-bold text-body-2">{{ item.name }}</span>
          </div>
        </template>

        <template v-slot:[`item.code`]="{ item }">
          <v-chip size="small" variant="tonal" color="primary" class="font-weight-bold">
            {{ item.code }}
          </v-chip>
        </template>

        <template v-slot:[`item.region`]="{ item }">
          <v-chip size="small" variant="tonal" color="secondary">
            {{ item.region }}
          </v-chip>
        </template>

        <template v-slot:[`item.actions`]="{ item }">
          <div class="d-inline-flex align-center justify-end text-no-wrap gap-1">
            <v-btn
              v-if="auth.hasPermission('SCHOOL_UPDATE')"
              icon="mdi-pencil-outline"
              size="small"
              variant="tonal"
              color="primary"
              title="Editar escola"
              @click="openEditDialog(item)"
            ></v-btn>
            <v-btn
              v-if="auth.hasPermission('SCHOOL_DELETE')"
              icon="mdi-delete-outline"
              size="small"
              variant="tonal"
              color="error"
              title="Eliminar escola"
              @click="confirmDelete(item)"
            ></v-btn>
          </div>
        </template>
      </v-data-table>
    </v-card>

    <!-- Create / Edit Dialog -->
    <v-dialog v-model="formDialog" max-width="500">
      <v-card
        :prepend-icon="isEditing ? 'mdi-school-outline' : 'mdi-plus-circle-outline'"
        :title="isEditing ? 'Editar Escola' : 'Nova Escola'"
        class="rounded-xl"
      >
        <v-card-text class="pa-4">
          <v-form ref="form" v-model="formValid">
            <v-text-field
              label="Nome da Escola*"
              placeholder="ex: Escola Secundária de Camões"
              required
              variant="outlined"
              density="comfortable"
              class="mb-2"
              :rules="[v => !!v || 'Nome é obrigatório']"
              v-model="currentSchool.name"
            ></v-text-field>

            <v-text-field
              label="Código da Escola* (ex: ESC-CAM)"
              placeholder="ESC-CAM"
              required
              variant="outlined"
              density="comfortable"
              class="mb-2"
              :rules="[v => !!v || 'Código é obrigatório']"
              v-model="currentSchool.code"
            ></v-text-field>

            <v-text-field
              label="Região* (ex: Lisboa, Norte, Centro, Algarve)"
              placeholder="Lisboa e Vale do Tejo"
              required
              variant="outlined"
              density="comfortable"
              :rules="[v => !!v || 'Região é obrigatória']"
              v-model="currentSchool.region"
            ></v-text-field>
          </v-form>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions class="pa-4 pt-2">
          <v-spacer></v-spacer>
          <v-btn text="Cancelar" variant="plain" class="text-none" @click="formDialog = false"></v-btn>
          <v-btn
            color="primary"
            :text="isEditing ? 'Guardar Alterações' : 'Criar Escola'"
            variant="flat"
            class="text-none font-weight-bold"
            :loading="saving"
            @click="saveSchool"
          ></v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="deleteDialog" max-width="440">
      <v-card prepend-icon="mdi-alert-circle-outline" title="Eliminar Escola" class="rounded-xl">
        <v-card-text>
          Tem a certeza que deseja eliminar a escola
          <strong>{{ schoolToDelete?.name }}</strong> ({{ schoolToDelete?.code }})?
        </v-card-text>
        <v-card-actions class="pa-4 pt-0">
          <v-spacer></v-spacer>
          <v-btn text="Cancelar" variant="plain" class="text-none" @click="deleteDialog = false"></v-btn>
          <v-btn
            color="error"
            text="Eliminar"
            variant="flat"
            class="text-none font-weight-bold"
            :loading="deleting"
            @click="executeDelete"
          ></v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import type SchoolDto from '@/models/SchoolDto'
import RemoteService from '@/services/RemoteService'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

const search = ref('')
const loading = ref(true)
const schools = ref<SchoolDto[]>([])

const formDialog = ref(false)
const isEditing = ref(false)
const formValid = ref(false)
const saving = ref(false)
const form = ref<any>(null)

const currentSchool = ref<SchoolDto>({ name: '', code: '', region: '' })

const deleteDialog = ref(false)
const schoolToDelete = ref<SchoolDto | null>(null)
const deleting = ref(false)

const headers = [
  { title: 'ID', key: 'id', width: '70px', sortable: true },
  { title: 'Nome da Escola', key: 'name', sortable: true },
  { title: 'Código', key: 'code', sortable: true },
  { title: 'Região', key: 'region', sortable: true },
  { title: 'Ações', key: 'actions', width: '120px', sortable: false, align: 'end' as const }
]

const filteredSchools = computed(() => {
  if (!search.value || search.value.trim() === '') return schools.value
  const q = search.value.toLowerCase().trim()
  return schools.value.filter(s =>
    s.name.toLowerCase().includes(q) ||
    s.code.toLowerCase().includes(q) ||
    s.region.toLowerCase().includes(q)
  )
})

const getSchools = async () => {
  loading.value = true
  try {
    schools.value = await RemoteService.getSchools()
  } catch (err) {
    console.error('Error fetching schools:', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getSchools()
})

const openCreateDialog = () => {
  isEditing.value = false
  currentSchool.value = { name: '', code: '', region: '' }
  formDialog.value = true
}

const openEditDialog = (school: SchoolDto) => {
  isEditing.value = true
  currentSchool.value = { ...school }
  formDialog.value = true
}

const saveSchool = async () => {
  if (form.value) {
    const { valid } = await form.value.validate()
    if (!valid) return
  }

  saving.value = true
  try {
    if (isEditing.value && currentSchool.value.id) {
      await RemoteService.updateSchool(currentSchool.value.id, currentSchool.value)
    } else {
      await RemoteService.createSchool(currentSchool.value)
    }
    formDialog.value = false
    await getSchools()
  } catch (err) {
    console.error('Error saving school:', err)
  } finally {
    saving.value = false
  }
}

const confirmDelete = (school: SchoolDto) => {
  schoolToDelete.value = school
  deleteDialog.value = true
}

const executeDelete = async () => {
  if (!schoolToDelete.value?.id) return
  deleting.value = true
  try {
    await RemoteService.deleteSchool(schoolToDelete.value.id)
    deleteDialog.value = false
    schoolToDelete.value = null
    await getSchools()
  } catch (err) {
    console.error('Error deleting school:', err)
  } finally {
    deleting.value = false
  }
}
</script>
