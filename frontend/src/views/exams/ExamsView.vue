<template>
  <v-container fluid class="pa-0">
    <!-- Header Section -->
    <v-card variant="flat" class="pa-4 mb-4 rounded-xl border bg-surface">
      <v-row align="center" justify="space-between">
        <v-col cols="12" md="8" class="text-left">
          <div class="d-flex align-center gap-2 mb-1">
            <v-icon icon="mdi-file-document-multiple-outline" color="indigo" size="28" class="mr-1"></v-icon>
            <h2 class="text-h6 font-weight-bold text-left mb-0">Gestão de Exames & Digitalização</h2>
          </div>
          <p class="text-caption text-medium-emphasis mb-0">
            Carregamento de exames digitalizados (PDFs) e segmentação atómica em perguntas individuais.
          </p>
        </v-col>

        <v-col cols="12" md="4" class="text-md-right" v-if="auth.hasPermission('EXAM_UPLOAD')">
          <UploadExamDialog :disabled="submissionsLocked" @exam-uploaded="onExamUploaded" />
        </v-col>
      </v-row>
    </v-card>

    <!-- Distribution Lock Warning Banner -->
    <v-alert
      v-if="submissionsLocked"
      type="warning"
      variant="tonal"
      density="comfortable"
      class="mb-4 rounded-xl"
      icon="mdi-lock-alert"
      title="Submissões e Edições Bloqueadas Permanentemente"
    >
      As provas já foram distribuídas para avaliação docente pelo Administrador. A criação de novos exames e a edição de exames existentes encontram-se permanentemente bloqueadas.
    </v-alert>

    <!-- Filters -->
    <v-card variant="outlined" class="pa-3 mb-4 rounded-xl border bg-surface">
      <v-row dense align="center">
        <v-col cols="12" md="4">
          <v-text-field
            v-model="search"
            label="Pesquisar por título, aluno ou escola..."
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            hide-details
            clearable
          ></v-text-field>
        </v-col>
        <v-col cols="12" sm="6" md="4">
          <v-select
            v-model="selectedStatusFilter"
            :items="statusFilterOptions"
            label="Estado da Prova"
            prepend-inner-icon="mdi-progress-check"
            variant="outlined"
            density="compact"
            hide-details
            clearable
          ></v-select>
        </v-col>
        <v-col cols="12" sm="6" md="4">
          <v-select
            v-model="selectedDisciplineFilter"
            :items="disciplines"
            item-title="name"
            item-value="id"
            label="Filtrar por Disciplina"
            prepend-inner-icon="mdi-book-education-outline"
            variant="outlined"
            density="compact"
            hide-details
            clearable
          ></v-select>
        </v-col>
      </v-row>
    </v-card>

    <!-- Exams Data Table -->
    <v-card variant="outlined" class="rounded-xl border bg-surface overflow-hidden">
      <v-data-table
        :headers="headers"
        :items="filteredExams"
        :loading="loading"
        item-key="id"
        class="text-left"
        no-data-text="Sem exames a apresentar."
      >
        <template v-slot:[`item.title`]="{ item }">
          <div class="py-2">
            <div class="font-weight-bold text-body-2">{{ item.title }}</div>
            <div class="text-caption text-medium-emphasis">{{ item.pdfFilename }} ({{ item.totalPages }} pág.)</div>
          </div>
        </template>

        <template v-slot:[`item.disciplineName`]="{ item }">
          <v-chip size="small" variant="tonal" color="teal" class="font-weight-medium">
            {{ item.disciplineName }}
          </v-chip>
        </template>

        <template v-slot:[`item.studentName`]="{ item }">
          <div>
            <div class="font-weight-medium">{{ item.studentName }}</div>
            <div class="text-caption text-medium-emphasis">{{ item.studentEmail }}</div>
          </div>
        </template>

        <template v-slot:[`item.status`]="{ item }">
          <v-chip
            v-if="item.status === 'UPLOADED'"
            color="orange"
            size="small"
            variant="flat"
            class="font-weight-bold"
          >
            Digitalizado (Pendente Separação)
          </v-chip>
          <v-chip
            v-else-if="item.status === 'SEGMENTED'"
            color="green"
            size="small"
            variant="flat"
            class="font-weight-bold"
          >
            Segmentado ({{ item.questionCount }} itens)
          </v-chip>
          <v-chip
            v-else-if="item.status === 'DISTRIBUTED'"
            color="blue"
            size="small"
            variant="flat"
            class="font-weight-bold"
          >
            Em Correção
          </v-chip>
          <v-chip
            v-else-if="item.status === 'CORRECTED'"
            color="purple"
            size="small"
            variant="flat"
            class="font-weight-bold"
          >
            Corrigido
          </v-chip>
          <v-chip
            v-else
            color="grey"
            size="small"
            variant="flat"
          >
            {{ item.status }}
          </v-chip>
        </template>

        <template v-slot:[`item.totalScore`]="{ item }">
          <span class="font-weight-extrabold text-primary">{{ item.totalScore?.toFixed(1) ?? '0.0' }}</span> val.
          <span class="text-caption text-medium-emphasis ml-1">({{ item.questionCount }} itens)</span>
        </template>

        <template v-slot:[`item.actions`]="{ item }">
          <div class="d-inline-flex align-center justify-end text-no-wrap gap-1">
            <v-btn
              v-if="auth.hasPermission('EXAM_SEGMENT')"
              :icon="item.status === 'UPLOADED' ? 'mdi-crop' : (item.status === 'SEGMENTED' ? 'mdi-puzzle-check-outline' : 'mdi-eye-outline')"
              size="small"
              variant="tonal"
              :color="item.status === 'UPLOADED' ? 'primary' : (item.status === 'SEGMENTED' ? 'teal' : 'secondary')"
              :title="item.status === 'UPLOADED' ? 'Separar Perguntas' : (item.status === 'SEGMENTED' ? 'Consultar/Ajustar Segmentação' : 'Consultar Perguntas')"
              :to="`/exams/${item.id}/segment`"
            ></v-btn>

            <v-btn
              icon="mdi-file-pdf-box"
              size="small"
              variant="tonal"
              color="secondary"
              title="Visualizar PDF Original"
              @click="previewPdf(item)"
            ></v-btn>

            <v-btn
              v-if="auth.hasPermission('EXAM_DELETE') && (item.status === 'UPLOADED' || item.status === 'SEGMENTED') && !submissionsLocked"
              icon="mdi-delete-outline"
              size="small"
              variant="tonal"
              color="error"
              title="Eliminar exame"
              @click="confirmDelete(item)"
            ></v-btn>
          </div>
        </template>
      </v-data-table>
    </v-card>

    <!-- PDF Preview Modal -->
    <v-dialog v-model="pdfDialog" max-width="950" height="88vh">
      <v-card class="d-flex flex-column h-100 rounded-xl overflow-hidden">
        <v-toolbar color="primary" density="compact">
          <v-toolbar-title class="text-subtitle-1 font-weight-bold">
            Visualização do PDF: {{ activeExam?.title }}
          </v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn icon="mdi-close" @click="pdfDialog = false"></v-btn>
        </v-toolbar>
        <v-card-text class="pa-0 flex-grow-1 bg-black">
          <iframe
            v-if="pdfUrl"
            :src="pdfUrl"
            width="100%"
            height="100%"
            style="border: none; min-height: 600px;"
          ></iframe>
        </v-card-text>
      </v-card>
    </v-dialog>

    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="deleteDialog" max-width="440">
      <v-card prepend-icon="mdi-alert-circle-outline" title="Eliminar Exame" class="rounded-xl">
        <v-card-text>
          Tem a certeza que deseja eliminar o exame
          <strong>{{ examToDelete?.title }}</strong> de {{ examToDelete?.studentName }}?
          Esta ação eliminará também todos os fragmentos recortados.
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
import type ExamDto from '@/models/ExamDto'
import type DisciplineDto from '@/models/DisciplineDto'
import RemoteService from '@/services/RemoteService'
import UploadExamDialog from './UploadExamDialog.vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

const search = ref('')
const selectedStatusFilter = ref<string | null>(null)
const selectedDisciplineFilter = ref<number | null>(null)
const loading = ref(true)
const exams = ref<ExamDto[]>([])
const disciplines = ref<DisciplineDto[]>([])
const submissionsLocked = ref(false)

const pdfDialog = ref(false)
const activeExam = ref<ExamDto | null>(null)
const pdfUrl = ref<string | null>(null)

const deleteDialog = ref(false)
const examToDelete = ref<ExamDto | null>(null)
const deleting = ref(false)

const statusFilterOptions = [
  { title: 'Todos os Estados', value: null },
  { title: 'Digitalizado (Pendente Separação)', value: 'UPLOADED' },
  { title: 'Segmentado', value: 'SEGMENTED' },
  { title: 'Em Correção', value: 'DISTRIBUTED' },
  { title: 'Corrigido', value: 'CORRECTED' }
]

const headers = [
  { title: 'ID', key: 'id', width: '60px', sortable: true },
  { title: 'Prova / Exame', key: 'title', sortable: true },
  { title: 'Disciplina', key: 'disciplineName', sortable: true },
  { title: 'Escola', key: 'schoolName', sortable: true },
  { title: 'Aluno', key: 'studentName', sortable: true },
  { title: 'Estado', key: 'status', sortable: true },
  { title: 'Cotação', key: 'totalScore', sortable: true },
  { title: 'Ações', key: 'actions', width: '140px', sortable: false, align: 'end' as const }
]

const filteredExams = computed(() => {
  let list = exams.value
  if (selectedStatusFilter.value) {
    list = list.filter(e => e.status === selectedStatusFilter.value)
  }
  if (selectedDisciplineFilter.value) {
    list = list.filter(e => e.disciplineId === selectedDisciplineFilter.value)
  }
  if (search.value && search.value.trim() !== '') {
    const q = search.value.toLowerCase().trim()
    list = list.filter(e =>
      e.title.toLowerCase().includes(q) ||
      e.studentName?.toLowerCase().includes(q) ||
      e.schoolName?.toLowerCase().includes(q) ||
      e.disciplineName?.toLowerCase().includes(q)
    )
  }
  return list
})

const getExams = async () => {
  loading.value = true
  try {
    const [fetchedExams, fetchedDisciplines, lockInfo] = await Promise.all([
      RemoteService.getExams(),
      RemoteService.getDisciplines(),
      RemoteService.areSubmissionsLocked().catch(() => ({ locked: false }))
    ])
    exams.value = fetchedExams
    disciplines.value = fetchedDisciplines
    submissionsLocked.value = lockInfo.locked
  } catch (err) {
    console.error('Error fetching exams:', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getExams()
})

const onExamUploaded = (createdExam: ExamDto) => {
  getExams()
  // Navigate directly to segmentation studio for great UX
  if (createdExam?.id) {
    router.push(`/exams/${createdExam.id}/segment`)
  }
}

const previewPdf = async (exam: ExamDto) => {
  activeExam.value = exam
  try {
    pdfUrl.value = await RemoteService.getExamPdfBlob(exam.id)
    pdfDialog.value = true
  } catch (err) {
    console.error('Error opening PDF:', err)
  }
}

const confirmDelete = (exam: ExamDto) => {
  examToDelete.value = exam
  deleteDialog.value = true
}

const executeDelete = async () => {
  if (!examToDelete.value?.id) return
  deleting.value = true
  try {
    await RemoteService.deleteExam(examToDelete.value.id)
    deleteDialog.value = false
    examToDelete.value = null
    await getExams()
  } catch (err) {
    console.error('Error deleting exam:', err)
  } finally {
    deleting.value = false
  }
}
</script>
