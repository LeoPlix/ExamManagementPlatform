<template>
  <v-container fluid class="pa-0">
    <!-- Header Section -->
    <v-card variant="flat" class="pa-4 mb-4 rounded-xl border bg-surface">
      <v-row align="center" justify="space-between">
        <v-col cols="12" md="6" class="text-left">
          <div class="d-flex align-center gap-2 mb-1">
            <v-icon icon="mdi-clipboard-list-outline" color="success" size="28" class="mr-1"></v-icon>
            <h2 class="text-h6 font-weight-bold text-left mb-0">Pauta de Classificações</h2>
          </div>
          <p class="text-caption text-medium-emphasis mb-0">
            Acompanhe as notas dos alunos, autorize a visualização individual de provas ou publique os resultados finais.
          </p>
        </v-col>

        <v-col cols="12" md="6" class="d-flex align-center justify-md-end flex-wrap gap-2" v-if="auth.hasPermission('EXAM_RELEASE')">
          <!-- Step 1: Publicar Notas (Appears first when grades have not yet been published to students) -->
          <v-tooltip
            v-if="canShowPublishInitialGrades"
            :text="countInCorrection === 0 ? `Publica a nota de cada exame aos respetivos alunos e abre a janela de 48h para pedidos de revisão (${countGradesReady}/${grades.length} exames corrigidos).` : `Ainda existem ${countInCorrection} prova(s) por concluir a avaliação docente. Só é possível publicar notas quando todas as provas distribuídas estiverem corrigidas.`"
            location="bottom"
          >
            <template v-slot:activator="{ props }">
              <span v-bind="props">
                <v-btn
                  color="success"
                  prepend-icon="mdi-send-check"
                  variant="flat"
                  class="text-none font-weight-bold"
                  :loading="publishingInitial"
                  :disabled="countInCorrection > 0 || grades.length === 0"
                  @click="publishInitialGrades"
                >
                  Publicar Notas ({{ countGradesReady }}/{{ grades.length }})
                </v-btn>
              </span>
            </template>
          </v-tooltip>

          <!-- Step 2: Publicar Provas Corrigidas (Replaces Step 1 during the 48h review window to allow bulk release) -->
          <v-tooltip
            v-else-if="canShowBulkRelease"
            :text="countUnreleased > 0 ? `Disponibiliza em lote todas as ${countUnreleased} prova(s) corrigida(s) para consulta direta de todos os alunos (Bulk Release), sem necessidade de pedidos individuais.` : `Todas as provas corrigidas já se encontram disponibilizadas aos alunos.`"
            location="bottom"
          >
            <template v-slot:activator="{ props }">
              <span v-bind="props">
                <v-btn
                  color="primary"
                  prepend-icon="mdi-bullhorn"
                  variant="flat"
                  class="text-none font-weight-bold"
                  :loading="bulkReleasing"
                  :disabled="countUnreleased === 0"
                  @click="bulkRelease"
                >
                  Publicar Provas Corrigidas ({{ countReleased }}/{{ grades.length }})
                </v-btn>
              </span>
            </template>
          </v-tooltip>

          <!-- Step 3a: Review Phase - Informative chip when reviews are in progress -->
          <v-chip
            v-else-if="hasPendingReviewsInProgress"
            color="amber-darken-3"
            variant="tonal"
            class="font-weight-medium"
            prepend-icon="mdi-clock-outline"
          >
            {{ totalPendingReviews }} pedido(s) de revisão em avaliação docente
          </v-chip>

          <!-- Step 3b: Lançar Notas de Revisão (Replaces Step 2 after 48h window / when all reviews are resolved) -->
          <v-tooltip
            v-else-if="canShowPublishReviews"
            :text="`Homologar e publicar as classificações definitivas após revisão (${totalReviewedQuestions} item(ns) revisto(s)).`"
            location="bottom"
          >
            <template v-slot:activator="{ props }">
              <span v-bind="props">
                <v-btn
                  color="purple"
                  prepend-icon="mdi-certificate-outline"
                  variant="flat"
                  class="text-none font-weight-bold"
                  :loading="publishingReviews"
                  @click="publishReviewGrades"
                >
                  Lançar Notas de Revisão ({{ totalReviewedQuestions }})
                </v-btn>
              </span>
            </template>
          </v-tooltip>
        </v-col>
      </v-row>
    </v-card>

    <!-- KPI Summary Metric Cards -->
    <v-row class="mb-4" dense>
      <v-col cols="12" sm="6" md="3">
        <v-card variant="outlined" class="pa-4 h-100 rounded-xl bg-surface border card-hover-lift">
          <div class="d-flex align-center justify-space-between mb-2">
            <span class="text-caption font-weight-bold text-uppercase text-medium-emphasis">Total de Provas</span>
            <v-avatar color="primary" variant="tonal" size="38">
              <v-icon icon="mdi-file-document-multiple-outline" size="20"></v-icon>
            </v-avatar>
          </div>
          <div class="text-h4 font-weight-extrabold text-primary">{{ grades.length }}</div>
          <div class="text-caption text-medium-emphasis mt-1">Registadas no sistema</div>
        </v-card>
      </v-col>

      <v-col cols="12" sm="6" md="3">
        <v-card variant="outlined" class="pa-4 h-100 rounded-xl bg-surface border card-hover-lift">
          <div class="d-flex align-center justify-space-between mb-2">
            <span class="text-caption font-weight-bold text-uppercase text-medium-emphasis">Em Avaliação</span>
            <v-avatar color="blue" variant="tonal" size="38">
              <v-icon icon="mdi-draw-pen" size="20"></v-icon>
            </v-avatar>
          </div>
          <div class="text-h4 font-weight-extrabold text-blue">{{ countInCorrection }}</div>
          <div class="text-caption text-medium-emphasis mt-1">Provas em correção docente</div>
        </v-card>
      </v-col>

      <v-col cols="12" sm="6" md="3">
        <v-card variant="outlined" class="pa-4 h-100 rounded-xl bg-surface border card-hover-lift">
          <div class="d-flex align-center justify-space-between mb-2">
            <span class="text-caption font-weight-bold text-uppercase text-medium-emphasis">Notas Publicadas</span>
            <v-avatar color="teal" variant="tonal" size="38">
              <v-icon icon="mdi-send-check-outline" size="20"></v-icon>
            </v-avatar>
          </div>
          <div class="text-h4 font-weight-extrabold text-teal">
            {{ countGradesPublished }}
          </div>
          <div class="text-caption text-medium-emphasis mt-1">Notas enviadas aos alunos</div>
        </v-card>
      </v-col>

      <v-col cols="12" sm="6" md="3">
        <v-card variant="outlined" class="pa-4 h-100 rounded-xl bg-surface border card-hover-lift">
          <div class="d-flex align-center justify-space-between mb-2">
            <span class="text-caption font-weight-bold text-uppercase text-medium-emphasis">Provas Disponíveis</span>
            <v-avatar color="success" variant="tonal" size="38">
              <v-icon icon="mdi-eye-check-outline" size="20"></v-icon>
            </v-avatar>
          </div>
          <div class="text-h4 font-weight-extrabold text-success">{{ countReleased }}</div>
          <div class="text-caption text-medium-emphasis mt-1">Acesso à prova concedido</div>
        </v-card>
      </v-col>
    </v-row>

    <!-- Filters Bar -->
    <v-card variant="outlined" class="pa-3 mb-4 rounded-xl border bg-surface">
      <v-row dense align="center">
        <v-col cols="12" md="4">
          <v-text-field
            v-model="search"
            label="Pesquisar por aluno, email ou prova..."
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            hide-details
            clearable
          ></v-text-field>
        </v-col>
        <v-col cols="12" sm="6" md="4">
          <v-select
            v-model="selectedDiscipline"
            :items="disciplines"
            item-title="name"
            item-value="id"
            label="Filtrar por Disciplina"
            prepend-inner-icon="mdi-book-education-outline"
            variant="outlined"
            density="compact"
            hide-details
            clearable
            @update:model-value="loadGrades"
          ></v-select>
        </v-col>
        <v-col cols="12" sm="6" md="4">
          <v-select
            v-model="selectedSchool"
            :items="schools"
            item-title="name"
            item-value="id"
            label="Filtrar por Escola"
            prepend-inner-icon="mdi-school-outline"
            variant="outlined"
            density="compact"
            hide-details
            clearable
            @update:model-value="loadGrades"
          ></v-select>
        </v-col>
      </v-row>
    </v-card>

    <!-- Grades Data Table -->
    <v-card variant="outlined" class="rounded-xl border bg-surface overflow-hidden">
      <v-data-table
        :headers="headers"
        :items="filteredGrades"
        :loading="loading"
        item-key="examId"
        class="text-left grades-table"
        no-data-text="Nenhuma classificação encontrada com os filtros selecionados."
      >
        <!-- Student Column -->
        <template v-slot:[`item.studentName`]="{ item }">
          <div class="d-flex align-center py-2">
            <v-avatar color="primary" variant="tonal" size="34" class="mr-3 font-weight-bold text-caption">
              {{ item.studentName?.charAt(0) ?? 'A' }}
            </v-avatar>
            <div>
              <div class="font-weight-bold text-body-2">{{ item.studentName }}</div>
              <div class="text-caption text-medium-emphasis">{{ item.studentEmail }}</div>
            </div>
          </div>
        </template>

        <!-- School Column -->
        <template v-slot:[`item.schoolName`]="{ item }">
          <div class="text-body-2 font-weight-medium">{{ item.schoolName }}</div>
        </template>

        <!-- Discipline Column -->
        <template v-slot:[`item.disciplineName`]="{ item }">
          <v-chip size="small" variant="tonal" color="teal" class="font-weight-medium">
            {{ item.disciplineName }}
          </v-chip>
        </template>

        <!-- Exam Title Column -->
        <template v-slot:[`item.examTitle`]="{ item }">
          <div class="font-weight-medium text-body-2">{{ item.examTitle }}</div>
        </template>

        <!-- Status Column -->
        <template v-slot:[`item.status`]="{ item }">
          <v-chip
            v-if="item.pendingReviewCount && item.pendingReviewCount > 0"
            color="amber-darken-3"
            size="small"
            variant="flat"
            class="font-weight-bold"
            prepend-icon="mdi-file-find-outline"
          >
            Em Revisão ({{ item.pendingReviewCount }})
          </v-chip>
          <v-chip
            v-else-if="item.reviewedCount && item.reviewedCount > 0"
            color="purple"
            size="small"
            variant="tonal"
            class="font-weight-bold"
            prepend-icon="mdi-certificate-outline"
          >
            Revisto ({{ item.reviewedCount }})
          </v-chip>
          <v-chip
            v-else-if="item.released || item.status === 'RELEASED'"
            color="success"
            size="small"
            variant="flat"
            class="font-weight-bold"
            prepend-icon="mdi-check-all"
          >
            Prova Disponibilizada
          </v-chip>
          <v-chip
            v-else-if="item.gradesPublished"
            color="teal"
            size="small"
            variant="flat"
            class="font-weight-bold"
            prepend-icon="mdi-send-check"
          >
            Nota Publicada
          </v-chip>
          <v-chip
            v-else-if="item.status === 'CORRECTED'"
            color="teal"
            size="small"
            variant="tonal"
            prepend-icon="mdi-check-decagram-outline"
          >
            Corrigido (Pendente Publicação)
          </v-chip>
          <v-chip
            v-else-if="item.status === 'DISTRIBUTED'"
            color="blue"
            size="small"
            variant="tonal"
            prepend-icon="mdi-draw-pen"
          >
            Em Avaliação
          </v-chip>
          <v-chip
            v-else-if="item.status === 'SEGMENTED'"
            color="indigo"
            size="small"
            variant="tonal"
            prepend-icon="mdi-puzzle-outline"
          >
            Segmentado
          </v-chip>
          <v-chip
            v-else
            color="orange"
            size="small"
            variant="tonal"
            prepend-icon="mdi-file-outline"
          >
            Digitalizado
          </v-chip>
        </template>

        <!-- Grade Column -->
        <template v-slot:[`item.obtainedScore`]="{ item }">
          <div v-if="(item.gradesPublished || item.released || item.status === 'RELEASED') && item.obtainedScore !== null && item.obtainedScore !== undefined" class="d-flex align-center">
            <v-chip
              :color="(item.obtainedScore ?? 0) >= (item.totalScore ?? 20) * 0.5 ? 'success' : 'error'"
              size="small"
              variant="flat"
              class="font-weight-bold mr-2"
            >
              {{ item.obtainedScore?.toFixed(1) ?? '0.0' }} val.
            </v-chip>
            <span class="text-caption text-medium-emphasis">/ {{ item.totalScore?.toFixed(1) ?? '20.0' }}</span>
          </div>
          <div v-else-if="item.status === 'CORRECTED'" class="d-flex align-center text-caption text-medium-emphasis">
            <v-icon icon="mdi-lock-outline" size="14" class="mr-1 opacity-70"></v-icon>
            <em>Pendente de publicação</em>
          </div>
          <div v-else class="d-flex align-center text-caption text-medium-emphasis">
            <v-icon icon="mdi-clock-outline" size="14" class="mr-1 opacity-70"></v-icon>
            <em>Em avaliação</em>
          </div>
        </template>

        <!-- Actions Column (Individual Approval & Status) -->
        <template v-slot:[`item.actions`]="{ item }">
          <div class="d-inline-flex align-center justify-end text-no-wrap gap-1">
            <v-chip
              v-if="item.released || item.status === 'RELEASED'"
              size="small"
              color="success"
              variant="tonal"
              prepend-icon="mdi-check-all"
            >
              Disponível ao Aluno
            </v-chip>

            <v-tooltip
              v-else-if="item.viewRequested && auth.hasPermission('EXAM_RELEASE')"
              text="Autorizar visualização da prova solicitada pelo aluno"
              location="bottom"
            >
              <template v-slot:activator="{ props }">
                <span v-bind="props">
                  <v-btn
                    size="small"
                    color="primary"
                    variant="flat"
                    prepend-icon="mdi-eye-check-outline"
                    class="text-none font-weight-bold"
                    @click="releaseSingleExam(item)"
                  >
                    Autorizar Visualização
                  </v-btn>
                </span>
              </template>
            </v-tooltip>

            <v-btn
              v-else-if="auth.hasPermission('EXAM_RELEASE') && item.gradesPublished"
              size="small"
              color="secondary"
              variant="tonal"
              prepend-icon="mdi-eye-outline"
              class="text-none"
              @click="releaseSingleExam(item)"
            >
              Disponibilizar Prova
            </v-btn>

            <span v-else class="text-caption text-medium-emphasis opacity-60">Sem Pedido</span>
          </div>
        </template>
      </v-data-table>
    </v-card>

    <!-- Notification snackbar -->
    <v-snackbar v-model="snackbar" :color="snackbarColor" timeout="4000" location="top">
      {{ snackbarText }}
      <template v-slot:actions>
        <v-btn color="white" variant="text" @click="snackbar = false">Fechar</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import type GradeSummaryDto from '@/models/GradeSummaryDto'
import type DisciplineDto from '@/models/DisciplineDto'
import type SchoolDto from '@/models/SchoolDto'
import RemoteService from '@/services/RemoteService'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

const loading = ref(true)
const grades = ref<GradeSummaryDto[]>([])
const disciplines = ref<DisciplineDto[]>([])
const schools = ref<SchoolDto[]>([])

const selectedDiscipline = ref<number | null>(null)
const selectedSchool = ref<number | null>(null)
const search = ref('')

const publishingInitial = ref(false)
const bulkReleasing = ref(false)
const publishingReviews = ref(false)

const snackbar = ref(false)
const snackbarText = ref('')
const snackbarColor = ref('success')

const headers = [
  { title: 'Aluno', key: 'studentName', sortable: true },
  { title: 'Escola', key: 'schoolName', sortable: true },
  { title: 'Disciplina', key: 'disciplineName', sortable: true },
  { title: 'Prova', key: 'examTitle', sortable: true },
  { title: 'Estado', key: 'status', sortable: true },
  { title: 'Nota Final', key: 'obtainedScore', sortable: true },
  { title: 'Ações', key: 'actions', width: '200px', sortable: false, align: 'end' as const }
]

const countInCorrection = computed(() => {
  return grades.value.filter(g => g.status === 'DISTRIBUTED').length
})

const countGradesReady = computed(() => {
  return grades.value.filter(g => g.status === 'CORRECTED' || g.status === 'RELEASED').length
})

const countGradesPublished = computed(() => {
  return grades.value.filter(g => g.gradesPublished || g.released || g.status === 'RELEASED').length
})

const countUnreleased = computed(() => {
  return grades.value.filter(g => !g.released && g.status !== 'RELEASED').length
})

const countReleased = computed(() => {
  return grades.value.filter(g => g.released || g.status === 'RELEASED').length
})

// Check for exams with unpublished grades
const hasUnpublishedGrades = computed(() => {
  return grades.value.some(g => !g.gradesPublished)
})

const canShowPublishInitialGrades = computed(() => {
  return grades.value.length > 0 && hasUnpublishedGrades.value
})

// 48-hour window for review requests
const isReviewWindowActive = computed(() => {
  if (hasUnpublishedGrades.value) return false
  return grades.value.some(g => g.gradesPublished && (!g.reviewDeadline || new Date(g.reviewDeadline).getTime() > Date.now()))
})

const canShowBulkRelease = computed(() => {
  return grades.value.length > 0 && !hasUnpublishedGrades.value && isReviewWindowActive.value
})

const totalPendingReviews = computed(() => {
  return grades.value.reduce((acc, g) => acc + (g.pendingReviewCount || 0), 0)
})

const totalReviewedQuestions = computed(() => {
  return grades.value.reduce((acc, g) => acc + (g.reviewedCount || 0), 0)
})

const hasPendingReviewsInProgress = computed(() => {
  return (
    grades.value.length > 0 &&
    !hasUnpublishedGrades.value &&
    !isReviewWindowActive.value &&
    totalPendingReviews.value > 0
  )
})

const canShowPublishReviews = computed(() => {
  return (
    grades.value.length > 0 &&
    !hasUnpublishedGrades.value &&
    totalPendingReviews.value === 0 &&
    (!isReviewWindowActive.value || totalReviewedQuestions.value > 0)
  )
})

const filteredGrades = computed(() => {
  let list = grades.value
  if (search.value && search.value.trim() !== '') {
    const q = search.value.toLowerCase().trim()
    list = list.filter(g =>
      g.studentName?.toLowerCase().includes(q) ||
      g.studentEmail?.toLowerCase().includes(q) ||
      g.examTitle?.toLowerCase().includes(q) ||
      g.schoolName?.toLowerCase().includes(q) ||
      g.disciplineName?.toLowerCase().includes(q)
    )
  }
  return list
})

onMounted(async () => {
  await Promise.all([loadGrades(), loadFilters()])
})

const loadFilters = async () => {
  try {
    const [discList, schoolList] = await Promise.all([
      RemoteService.getDisciplines(),
      RemoteService.getSchools()
    ])
    disciplines.value = discList
    schools.value = schoolList
  } catch (err) {
    console.error('Error fetching filters:', err)
  }
}

const loadGrades = async () => {
  loading.value = true
  try {
    const data = await RemoteService.getGrades({
      disciplineId: selectedDiscipline.value ?? undefined,
      schoolId: selectedSchool.value ?? undefined
    })
    grades.value = data
  } catch (err) {
    console.error('Error loading grades:', err)
  } finally {
    loading.value = false
  }
}

const publishInitialGrades = async () => {
  publishingInitial.value = true
  try {
    const res = await RemoteService.publishInitialGrades({
      disciplineId: selectedDiscipline.value ?? undefined,
      schoolId: selectedSchool.value ?? undefined
    })
    showToast(`Notas publicadas com sucesso para ${res.length} aluno(s)! Abriu o período de 48h para pedidos de revisão.`, 'success')
    await loadGrades()
  } catch (err: any) {
    console.error('Error publishing initial grades:', err)
    const msg = err.response?.data?.message || err.message || 'Erro ao publicar notas.'
    showToast(msg, 'error')
  } finally {
    publishingInitial.value = false
  }
}

const bulkRelease = async () => {
  bulkReleasing.value = true
  try {
    const releasedList = await RemoteService.bulkRelease({
      disciplineId: selectedDiscipline.value ?? undefined,
      schoolId: selectedSchool.value ?? undefined
    })
    showToast(`Provas corrigidas publicadas com sucesso para ${releasedList.length} aluno(s)! Todos têm agora acesso direto à consulta da prova.`, 'success')
    await loadGrades()
  } catch (err: any) {
    console.error('Error bulk releasing:', err)
    const msg = err.response?.data?.message || err.message || 'Erro ao publicar provas em lote.'
    showToast(msg, 'error')
  } finally {
    bulkReleasing.value = false
  }
}

const releaseSingleExam = async (item: GradeSummaryDto) => {
  try {
    await RemoteService.releaseExam(item.examId)
    showToast(`Visualização da prova autorizada para o aluno ${item.studentName}!`, 'success')
    await loadGrades()
  } catch (err: any) {
    console.error('Error releasing single exam:', err)
    const msg = err.response?.data?.message || err.message || 'Erro ao autorizar visualização.'
    showToast(msg, 'error')
  }
}

const publishReviewGrades = async () => {
  publishingReviews.value = true
  try {
    const res = await RemoteService.publishReviewGrades({
      disciplineId: selectedDiscipline.value ?? undefined,
      schoolId: selectedSchool.value ?? undefined
    })
    showToast(`Notas de revisão lançadas e homologadas com sucesso para ${res.length} provas!`, 'purple')
    await loadGrades()
  } catch (err: any) {
    console.error('Error publishing review grades:', err)
    const msg = err.response?.data?.message || err.message || 'Erro ao publicar notas de revisão.'
    showToast(msg, 'error')
  } finally {
    publishingReviews.value = false
  }
}

const showToast = (text: string, color = 'success') => {
  snackbarText.value = text
  snackbarColor.value = color
  snackbar.value = true
}
</script>
