<template>
  <v-container fluid class="pa-0">
    <!-- Header Section -->
    <v-card variant="flat" class="pa-4 mb-4 rounded-xl border bg-surface">
      <v-row align="center" justify="space-between">
        <v-col cols="12" md="8" class="text-left">
          <div class="d-flex align-center gap-2 mb-1">
            <v-icon icon="mdi-book-education-outline" color="purple" size="28" class="mr-1"></v-icon>
            <h2 class="text-h6 font-weight-bold text-left mb-0">Gestão de Disciplinas</h2>
          </div>
          <p class="text-caption text-medium-emphasis mb-0">
            Administração dos códigos de exame e matrizes curriculares das provas nacionais.
          </p>
        </v-col>

        <v-col cols="12" md="4" class="text-md-right" v-if="auth.hasPermission('DISCIPLINE_CREATE')">
          <v-btn
            color="purple"
            prepend-icon="mdi-plus"
            class="text-none font-weight-bold"
            variant="flat"
            @click="openCreateDialog"
          >
            Adicionar Disciplina
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
            label="Pesquisar disciplina por nome ou código..."
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
        :items="filteredDisciplines"
        :loading="loading"
        item-key="id"
        class="text-left"
        no-data-text="Sem disciplinas a apresentar."
      >
        <template v-slot:[`item.name`]="{ item }">
          <div class="d-flex align-center py-2">
            <v-avatar color="purple" variant="tonal" size="32" class="mr-3">
              <v-icon icon="mdi-book-open-page-variant" size="18"></v-icon>
            </v-avatar>
            <span class="font-weight-bold text-body-2">{{ item.name }}</span>
          </div>
        </template>

        <template v-slot:[`item.code`]="{ item }">
          <v-chip size="small" variant="tonal" color="purple" class="font-weight-bold">
            {{ item.code }}
          </v-chip>
        </template>

        <template v-slot:[`item.actions`]="{ item }">
          <div class="d-inline-flex align-center justify-end text-no-wrap gap-1">
            <v-btn
              v-if="auth.hasPermission('DISCIPLINE_UPDATE')"
              icon="mdi-pencil-outline"
              size="small"
              variant="tonal"
              color="primary"
              title="Editar disciplina"
              @click="openEditDialog(item)"
            ></v-btn>
            <v-btn
              v-if="auth.hasPermission('DISCIPLINE_DELETE')"
              icon="mdi-delete-outline"
              size="small"
              variant="tonal"
              color="error"
              title="Eliminar disciplina"
              @click="confirmDelete(item)"
            ></v-btn>
          </div>
        </template>
      </v-data-table>
    </v-card>

    <!-- Create / Edit Dialog -->
    <v-dialog v-model="formDialog" max-width="500">
      <v-card
        :prepend-icon="isEditing ? 'mdi-book-edit-outline' : 'mdi-plus-circle-outline'"
        :title="isEditing ? 'Editar Disciplina' : 'Nova Disciplina'"
        class="rounded-xl"
      >
        <v-card-text class="pa-4">
          <v-form ref="form" v-model="formValid">
            <v-text-field
              label="Nome da Disciplina*"
              placeholder="ex: Matemática A"
              required
              variant="outlined"
              density="comfortable"
              class="mb-2"
              :rules="[v => !!v || 'Nome é obrigatório']"
              v-model="currentDiscipline.name"
            ></v-text-field>

            <v-text-field
              label="Código da Disciplina* (ex: MAT-A, FQA, PORT)"
              placeholder="MAT-A"
              required
              variant="outlined"
              density="comfortable"
              :rules="[v => !!v || 'Código é obrigatório']"
              v-model="currentDiscipline.code"
            ></v-text-field>
          </v-form>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions class="pa-4 pt-2">
          <v-spacer></v-spacer>
          <v-btn text="Cancelar" variant="plain" class="text-none" @click="formDialog = false"></v-btn>
          <v-btn
            color="purple"
            :text="isEditing ? 'Guardar Alterações' : 'Criar Disciplina'"
            variant="flat"
            class="text-none font-weight-bold"
            :loading="saving"
            @click="saveDiscipline"
          ></v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="deleteDialog" max-width="440">
      <v-card prepend-icon="mdi-alert-circle-outline" title="Eliminar Disciplina" class="rounded-xl">
        <v-card-text>
          Tem a certeza que deseja eliminar a disciplina
          <strong>{{ disciplineToDelete?.name }}</strong> ({{ disciplineToDelete?.code }})?
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
import type DisciplineDto from '@/models/DisciplineDto'
import RemoteService from '@/services/RemoteService'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

const search = ref('')
const loading = ref(true)
const disciplines = ref<DisciplineDto[]>([])

const formDialog = ref(false)
const isEditing = ref(false)
const formValid = ref(false)
const saving = ref(false)
const form = ref<any>(null)

const currentDiscipline = ref<DisciplineDto>({ name: '', code: '' })

const deleteDialog = ref(false)
const disciplineToDelete = ref<DisciplineDto | null>(null)
const deleting = ref(false)

const headers = [
  { title: 'ID', key: 'id', width: '70px', sortable: true },
  { title: 'Nome da Disciplina', key: 'name', sortable: true },
  { title: 'Código', key: 'code', sortable: true },
  { title: 'Ações', key: 'actions', width: '120px', sortable: false, align: 'end' as const }
]

const filteredDisciplines = computed(() => {
  if (!search.value || search.value.trim() === '') return disciplines.value
  const q = search.value.toLowerCase().trim()
  return disciplines.value.filter(d =>
    d.name.toLowerCase().includes(q) ||
    d.code.toLowerCase().includes(q)
  )
})

const getDisciplines = async () => {
  loading.value = true
  try {
    disciplines.value = await RemoteService.getDisciplines()
  } catch (err) {
    console.error('Error fetching disciplines:', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getDisciplines()
})

const openCreateDialog = () => {
  isEditing.value = false
  currentDiscipline.value = { name: '', code: '' }
  formDialog.value = true
}

const openEditDialog = (discipline: DisciplineDto) => {
  isEditing.value = true
  currentDiscipline.value = { ...discipline }
  formDialog.value = true
}

const saveDiscipline = async () => {
  if (form.value) {
    const { valid } = await form.value.validate()
    if (!valid) return
  }

  saving.value = true
  try {
    if (isEditing.value && currentDiscipline.value.id) {
      await RemoteService.updateDiscipline(currentDiscipline.value.id, currentDiscipline.value)
    } else {
      await RemoteService.createDiscipline(currentDiscipline.value)
    }
    formDialog.value = false
    await getDisciplines()
  } catch (err) {
    console.error('Error saving discipline:', err)
  } finally {
    saving.value = false
  }
}

const confirmDelete = (discipline: DisciplineDto) => {
  disciplineToDelete.value = discipline
  deleteDialog.value = true
}

const executeDelete = async () => {
  if (!disciplineToDelete.value?.id) return
  deleting.value = true
  try {
    await RemoteService.deleteDiscipline(disciplineToDelete.value.id)
    deleteDialog.value = false
    disciplineToDelete.value = null
    await getDisciplines()
  } catch (err) {
    console.error('Error deleting discipline:', err)
  } finally {
    deleting.value = false
  }
}
</script>
