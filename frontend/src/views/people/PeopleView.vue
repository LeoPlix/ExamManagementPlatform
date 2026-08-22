<template>
  <v-container fluid class="pa-0">
    <!-- Header Section -->
    <v-card variant="flat" class="pa-4 mb-4 rounded-xl border bg-surface">
      <v-row align="center" justify="space-between">
        <v-col cols="12" md="8" class="text-left">
          <div class="d-flex align-center gap-2 mb-1">
            <v-icon icon="mdi-account-group-outline" color="primary" size="28" class="mr-1"></v-icon>
            <h2 class="text-h6 font-weight-bold text-left mb-0">Gestão de Utilizadores</h2>
          </div>
          <p class="text-caption text-medium-emphasis mb-0">
            Administração global de contas de utilizador, atribuição de papéis, escolas e disciplinas associadas.
          </p>
        </v-col>

        <v-col cols="12" md="4" class="text-md-right" v-if="auth.hasPermission('PERSON_CREATE')">
          <CreatePersonDialog @person-created="getPeople" />
        </v-col>
      </v-row>
    </v-card>

    <!-- Filters Bar -->
    <v-card variant="outlined" class="pa-3 mb-4 rounded-xl border bg-surface">
      <v-row dense align="center">
        <v-col cols="12" md="6">
          <v-text-field
            v-model="search"
            label="Pesquisar por nome, email ou escola..."
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            hide-details
            clearable
          ></v-text-field>
        </v-col>
        <v-col cols="12" md="6">
          <v-select
            v-model="selectedRoleFilter"
            :items="roleFilterOptions"
            label="Filtrar por Papel / Categoria"
            prepend-inner-icon="mdi-filter-outline"
            variant="outlined"
            density="compact"
            hide-details
            clearable
          ></v-select>
        </v-col>
      </v-row>
    </v-card>

    <!-- Data Table -->
    <v-card variant="outlined" class="rounded-xl border bg-surface overflow-hidden">
      <v-data-table
        :headers="headers"
        :items="filteredPeople"
        :loading="loading"
        item-key="id"
        class="text-left"
        no-data-text="Sem utilizadores a apresentar."
      >
        <template v-slot:[`item.name`]="{ item }">
          <div class="d-flex align-center py-2">
            <v-avatar :color="roleColor(item.type)" variant="tonal" size="32" class="mr-3 font-weight-bold text-caption">
              {{ item.name?.charAt(0) ?? 'U' }}
            </v-avatar>
            <span class="font-weight-medium">{{ item.name }}</span>
          </div>
        </template>

        <template v-slot:[`item.type`]="{ item }">
          <v-chip :color="roleColor(item.type)" size="small" variant="flat" class="font-weight-bold">
            {{ roleLabel(item.type) }}
          </v-chip>
        </template>

        <template v-slot:[`item.schoolName`]="{ item }">
          <span v-if="item.schoolName" class="font-weight-medium">{{ item.schoolName }}</span>
          <span v-else class="text-medium-emphasis opacity-60">-</span>
        </template>

        <template v-slot:[`item.disciplineNames`]="{ item }">
          <div v-if="item.disciplineNames && item.disciplineNames.length > 0">
            <v-chip
              v-for="d in item.disciplineNames"
              :key="d"
              size="x-small"
              class="mr-1 mb-1 font-weight-medium"
              variant="tonal"
              color="teal"
            >
              {{ d }}
            </v-chip>
          </div>
          <span v-else class="text-medium-emphasis opacity-60">-</span>
        </template>

        <template v-slot:[`item.actions`]="{ item }">
          <div class="d-inline-flex align-center justify-end text-no-wrap gap-1">
            <!-- Administrator cannot edit themselves or other administrators -->
            <v-btn
              v-if="auth.hasPermission('PERSON_UPDATE') && item.id !== auth.user?.id && item.type !== 'ADMINISTRATOR'"
              icon="mdi-pencil-outline"
              size="small"
              variant="tonal"
              color="primary"
              title="Editar utilizador"
              @click="editPerson(item)"
            ></v-btn>
            <!-- Administrator cannot delete themselves or other administrators -->
            <v-btn
              v-if="auth.hasPermission('PERSON_DELETE') && item.id !== auth.user?.id && item.type !== 'ADMINISTRATOR'"
              icon="mdi-delete-outline"
              size="small"
              variant="tonal"
              color="error"
              title="Eliminar utilizador"
              @click="confirmDelete(item)"
            ></v-btn>
            <span v-if="item.id === auth.user?.id || item.type === 'ADMINISTRATOR'" class="text-caption text-medium-emphasis opacity-60">
              —
            </span>
          </div>
        </template>
      </v-data-table>
    </v-card>

    <!-- Edit Dialog -->
    <EditPersonDialog
      v-model="editDialogVisible"
      :person="selectedPerson"
      @person-updated="getPeople"
    />

    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="deleteDialog" max-width="440">
      <v-card prepend-icon="mdi-alert-circle-outline" title="Eliminar Utilizador" class="rounded-xl">
        <v-card-text>
          Tem a certeza que deseja eliminar o utilizador
          <strong>{{ personToDelete?.name }}</strong> ({{ personToDelete?.email }})?
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
import type PersonDto from '@/models/PersonDto'
import RemoteService from '@/services/RemoteService'
import CreatePersonDialog from './CreatePersonDialog.vue'
import EditPersonDialog from './EditPersonDialog.vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

const search = ref('')
const selectedRoleFilter = ref<string | null>(null)
const loading = ref(true)
const people = ref<PersonDto[]>([])

const editDialogVisible = ref(false)
const selectedPerson = ref<PersonDto | null>(null)

const deleteDialog = ref(false)
const personToDelete = ref<PersonDto | null>(null)
const deleting = ref(false)

const roleFilterOptions = [
  { title: 'Todos os Papéis', value: null },
  { title: 'Administrador', value: 'ADMINISTRATOR' },
  { title: 'Funcionário da Escola', value: 'SCHOOL_STAFF' },
  { title: 'Professor', value: 'TEACHER' },
  { title: 'Aluno', value: 'STUDENT' }
]

const headers = [
  { title: 'ID', key: 'id', width: '70px', sortable: true },
  { title: 'Nome', key: 'name', sortable: true },
  { title: 'Email', key: 'email', sortable: true },
  { title: 'Papel', key: 'type', sortable: true },
  { title: 'Escola', key: 'schoolName', sortable: true },
  { title: 'Disciplinas', key: 'disciplineNames', sortable: false },
  { title: 'Ações', key: 'actions', width: '120px', sortable: false, align: 'end' as const }
]

const roleLabel = (type: string) => {
  switch (type) {
    case 'ADMINISTRATOR':
      return 'Administrador'
    case 'SCHOOL_STAFF':
      return 'Funcionário Escolar'
    case 'TEACHER':
      return 'Professor'
    case 'STUDENT':
      return 'Aluno'
    default:
      return type
  }
}

const roleColor = (type: string) => {
  switch (type) {
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

const filteredPeople = computed(() => {
  let list = people.value
  if (selectedRoleFilter.value) {
    list = list.filter(p => p.type === selectedRoleFilter.value)
  }
  if (search.value && search.value.trim() !== '') {
    const q = search.value.toLowerCase().trim()
    list = list.filter(p =>
      p.name?.toLowerCase().includes(q) ||
      p.email?.toLowerCase().includes(q) ||
      p.schoolName?.toLowerCase().includes(q)
    )
  }
  return list
})

const getPeople = async () => {
  loading.value = true
  try {
    people.value = await RemoteService.getPeople()
  } catch (err) {
    console.error('Error fetching people:', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getPeople()
})

const editPerson = (person: PersonDto) => {
  selectedPerson.value = person
  editDialogVisible.value = true
}

const confirmDelete = (person: PersonDto) => {
  personToDelete.value = person
  deleteDialog.value = true
}

const executeDelete = async () => {
  if (!personToDelete.value?.id) return
  deleting.value = true
  try {
    await RemoteService.deletePerson(personToDelete.value.id)
    deleteDialog.value = false
    personToDelete.value = null
    await getPeople()
  } catch (err) {
    console.error('Error deleting person:', err)
  } finally {
    deleting.value = false
  }
}
</script>
