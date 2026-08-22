<template>
  <v-container fluid class="pa-0">
    <!-- Header Section -->
    <v-card variant="flat" class="pa-4 mb-4 rounded-xl border bg-surface">
      <v-row align="center" justify="space-between">
        <v-col cols="12" md="8" class="text-left">
          <div class="d-flex align-center gap-2 mb-1">
            <v-icon icon="mdi-draw-pen" color="teal" size="28" class="mr-1"></v-icon>
            <h2 class="text-h6 font-weight-bold text-left mb-0">Centro de Avaliação Docente</h2>
          </div>
          <p class="text-caption text-medium-emphasis mb-0">
            Correção cega e atómica de fragmentos de perguntas e avaliação de pedidos de revisão.
          </p>
        </v-col>
      </v-row>
    </v-card>

    <!-- Tabs: Regular Evaluations vs. Grade Reviews -->
    <v-card variant="outlined" class="mb-4 rounded-xl border bg-surface">
      <v-tabs v-model="activeTab" color="teal" density="comfortable">
        <v-tab value="tasks" class="font-weight-bold text-none">
          <v-icon start icon="mdi-checkbox-marked-circle-outline"></v-icon>
          Tarefas de Correção ({{ pendingTasksCount }} pendentes)
        </v-tab>
        <v-tab value="reviews" class="font-weight-bold text-none">
          <v-icon start icon="mdi-file-document-edit-outline"></v-icon>
          Pedidos de Revisão ({{ pendingReviewsCount }} pendentes)
        </v-tab>
      </v-tabs>
    </v-card>

    <v-window v-model="activeTab">
      <!-- TAB 1: REGULAR QUESTION TASKS -->
      <v-window-item value="tasks">
        <!-- Filter bar -->
        <v-row class="mb-3" align="center">
          <v-col cols="12" sm="6" md="4">
            <v-btn-toggle v-model="taskFilter" mandatory density="comfortable" color="primary">
              <v-btn value="ALL" class="text-none">Todas ({{ tasks.length }})</v-btn>
              <v-btn value="PENDING" class="text-none">Pendentes ({{ pendingTasksCount }})</v-btn>
              <v-btn value="COMPLETED" class="text-none">Concluídas ({{ completedTasksCount }})</v-btn>
            </v-btn-toggle>
          </v-col>
          <v-col cols="12" sm="6" md="4">
            <v-text-field
              v-model="taskSearch"
              label="Pesquisar por item ou disciplina..."
              prepend-inner-icon="mdi-magnify"
              variant="outlined"
              density="comfortable"
              hide-details
              clearable
            ></v-text-field>
          </v-col>
        </v-row>

        <div v-if="loadingTasks" class="pa-12 text-center">
          <v-progress-circular indeterminate color="primary" size="48"></v-progress-circular>
          <p class="mt-4 text-medium-emphasis">A carregar tarefas de avaliação...</p>
        </div>

        <div v-else-if="filteredTasks.length === 0" class="pa-12 text-center text-medium-emphasis">
          <v-icon icon="mdi-check-all" size="64" class="mb-3 opacity-50"></v-icon>
          <h3>Nenhuma tarefa a apresentar</h3>
          <p class="text-caption">Não existem fragmentos de perguntas com o filtro selecionado.</p>
        </div>

        <v-row v-else>
          <v-col
            v-for="task in filteredTasks"
            :key="task.id"
            cols="12"
            md="6"
            lg="4"
          >
            <v-card variant="outlined" class="task-card d-flex flex-column h-100">
              <!-- Thumbnail preview -->
              <div class="task-image-preview" @click="openEvaluationDialog(task)">
                <img
                  v-if="questionImages[task.id]"
                  :src="questionImages[task.id]"
                  alt="Fragmento da Resolução"
                  class="task-thumb-img"
                />
                <div v-else class="d-flex align-center justify-center fill-height bg-grey-lighten-4">
                  <v-progress-circular indeterminate size="24"></v-progress-circular>
                </div>
                <div class="task-thumb-overlay">
                  <v-icon icon="mdi-eye" color="white" class="mr-1"></v-icon>
                  <span>Avaliar Resposta</span>
                </div>
              </div>

              <v-card-text class="pa-4 flex-grow-1">
                <div class="d-flex align-center justify-space-between mb-2">
                  <span class="text-h6 font-weight-bold">Item {{ task.questionNumber }}</span>
                  <div class="d-flex align-center gap-1">
                    <v-chip
                      v-if="task.hasAnnotation"
                      size="x-small"
                      color="purple"
                      variant="tonal"
                      prepend-icon="mdi-draw"
                      title="Contém anotações visuais"
                    >
                      Anotada
                    </v-chip>
                    <v-chip
                      :color="task.score !== null && task.score !== undefined ? 'success' : 'orange'"
                      size="small"
                      variant="flat"
                    >
                      {{ task.score !== null && task.score !== undefined ? 'Corrigido' : 'Pendente' }}
                    </v-chip>
                  </div>
                </div>

                <div class="mb-3">
                  <v-chip size="small" variant="tonal" color="teal" class="mr-2">
                    {{ task.disciplineName ?? 'Disciplina' }}
                  </v-chip>
                  <span class="text-caption text-medium-emphasis">Pág. {{ task.pageNumber }}</span>
                </div>

                <div class="bg-surface-variant pa-3 rounded-lg d-flex align-center justify-space-between mb-2">
                  <span class="text-caption text-medium-emphasis">Cotação Máxima:</span>
                  <span class="font-weight-bold text-body-2">{{ task.maxScore.toFixed(1) }} val.</span>
                </div>

                <div v-if="task.score !== null && task.score !== undefined" class="bg-green-lighten-5 pa-3 rounded-lg">
                  <div class="d-flex align-center justify-space-between text-success">
                    <span class="text-caption font-weight-bold">Nota Atribuída:</span>
                    <span class="font-weight-bold text-body-1">{{ task.score.toFixed(1) }} / {{ task.maxScore.toFixed(1) }}</span>
                  </div>
                  <div v-if="task.feedback" class="text-caption text-medium-emphasis mt-1">
                    "{{ task.feedback }}"
                  </div>
                </div>
              </v-card-text>

              <v-card-actions class="pa-4 pt-0">
                <v-btn
                  block
                  :color="task.score !== null && task.score !== undefined ? 'secondary' : 'primary'"
                  :variant="task.score !== null && task.score !== undefined ? 'outlined' : 'flat'"
                  prepend-icon="mdi-pencil"
                  class="text-none font-weight-bold"
                  @click="openEvaluationDialog(task)"
                >
                  {{ task.score !== null && task.score !== undefined ? 'Alterar Nota' : 'Atribuir Nota' }}
                </v-btn>
              </v-card-actions>
            </v-card>
          </v-col>
        </v-row>
      </v-window-item>

      <!-- TAB 2: REVIEW REQUESTS -->
      <v-window-item value="reviews">
        <div v-if="loadingReviews" class="pa-12 text-center">
          <v-progress-circular indeterminate color="primary" size="48"></v-progress-circular>
          <p class="mt-4 text-medium-emphasis">A carregar pedidos de revisão...</p>
        </div>

        <div v-else-if="reviewTasks.length === 0" class="pa-12 text-center text-medium-emphasis">
          <v-icon icon="mdi-file-check-outline" size="64" class="mb-3 opacity-50"></v-icon>
          <h3>Nenhum pedido de revisão pendente</h3>
          <p class="text-caption">Não tem tarefas de revisão de provas atribuídas neste momento.</p>
        </div>

        <v-row v-else>
          <v-col
            v-for="rev in reviewTasks"
            :key="rev.id"
            cols="12"
            md="6"
          >
            <v-card variant="outlined" class="pa-4 h-100 d-flex flex-column">
              <div class="d-flex align-center justify-space-between mb-3">
                <div class="d-flex align-center">
                  <v-avatar color="purple-lighten-4" size="36" class="mr-2">
                    <v-icon icon="mdi-scale-balance" color="purple"></v-icon>
                  </v-avatar>
                  <div>
                    <h3 class="text-subtitle-1 font-weight-bold">Item {{ rev.questionNumber }} - {{ rev.disciplineName }}</h3>
                    <div class="text-caption text-medium-emphasis">Pedido #{{ rev.id }}</div>
                  </div>
                </div>

                <v-chip
                  :color="rev.status === 'RESOLVED' ? 'success' : 'orange'"
                  size="small"
                  variant="flat"
                >
                  {{ rev.status === 'RESOLVED' ? 'Resolvido' : 'Pendente Revisão' }}
                </v-chip>
              </div>

              <!-- Student Justification Box -->
              <v-alert
                type="info"
                variant="tonal"
                density="comfortable"
                class="mb-3"
                title="Justificação do Aluno:"
              >
                <div class="text-body-2 font-italic">"{{ rev.justification }}"</div>
              </v-alert>

              <div class="d-flex align-center justify-space-between mb-3 bg-surface-variant pa-3 rounded-lg">
                <div>
                  <div class="text-caption text-medium-emphasis">Nota Original:</div>
                  <div class="font-weight-bold">{{ rev.originalScore?.toFixed(1) ?? '0.0' }} val.</div>
                </div>
                <div>
                  <div class="text-caption text-medium-emphasis">Cotação Máxima:</div>
                  <div class="font-weight-bold">{{ rev.maxScore.toFixed(1) }} val.</div>
                </div>
                <div v-if="rev.status === 'RESOLVED'">
                  <div class="text-caption text-medium-emphasis">Nota Definitiva:</div>
                  <div class="font-weight-bold text-success">{{ rev.revisedScore?.toFixed(1) }} val.</div>
                </div>
              </div>

              <v-spacer></v-spacer>

              <v-btn
                block
                color="purple"
                variant="flat"
                prepend-icon="mdi-gavel"
                class="text-none font-weight-bold mt-2"
                @click="openReviewDialog(rev)"
              >
                {{ rev.status === 'RESOLVED' ? 'Ver / Reavaliar Revisão' : 'Avaliar Pedido de Revisão' }}
              </v-btn>
            </v-card>
          </v-col>
        </v-row>
      </v-window-item>
    </v-window>

    <!-- EVALUATION MODAL (Phase 3) -->
    <v-dialog v-model="evaluationDialog" max-width="850">
      <v-card v-if="activeTask">
        <v-toolbar color="primary" density="compact">
          <v-toolbar-title class="text-subtitle-1 font-weight-bold">
            Correção Atómica: Item {{ activeTask.questionNumber }} ({{ activeTask.disciplineName }})
          </v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn icon="mdi-close" @click="evaluationDialog = false"></v-btn>
        </v-toolbar>

        <v-card-text class="pa-4">
          <!-- Canvas Drawing Toolbar -->
          <v-card variant="tonal" color="primary" class="pa-2 mb-3 rounded-lg">
            <div class="d-flex align-center justify-space-between flex-wrap gap-2">
              <div class="d-flex align-center flex-wrap gap-1">
                <span class="text-caption font-weight-bold mr-2 text-uppercase opacity-80">
                  <v-icon icon="mdi-draw" size="16" class="mr-1"></v-icon> Ferramentas de Correção:
                </span>
                <v-btn-toggle v-model="drawingTool" mandatory density="compact" color="primary">
                  <v-btn value="pen" title="Caneta Vermelha" class="text-none">
                    <v-icon icon="mdi-pencil" color="red" size="18" class="mr-1"></v-icon>
                    Caneta
                  </v-btn>
                  <v-btn value="check" title="Carimbar Visto" class="text-none">
                    <v-icon icon="mdi-check-bold" color="green" size="18" class="mr-1"></v-icon>
                    Visto (✓)
                  </v-btn>
                  <v-btn value="cross" title="Carimbar Cruz" class="text-none">
                    <v-icon icon="mdi-close-thick" color="red" size="18" class="mr-1"></v-icon>
                    Cruz (✗)
                  </v-btn>
                  <v-btn value="text" title="Anotação de Texto" class="text-none">
                    <v-icon icon="mdi-format-text" size="18" class="mr-1"></v-icon>
                    Texto
                  </v-btn>
                </v-btn-toggle>

                <v-text-field
                  v-if="drawingTool === 'text'"
                  v-model="customAnnotationText"
                  placeholder="Texto..."
                  density="compact"
                  variant="outlined"
                  hide-details
                  style="max-width: 130px;"
                  class="ml-2"
                ></v-text-field>
              </div>

              <div class="d-flex align-center gap-1">
                <!-- Zoom Controls -->
                <v-btn
                  icon="mdi-magnify-minus-outline"
                  size="small"
                  variant="text"
                  title="Diminuir Zoom"
                  :disabled="evalZoom <= 0.6"
                  @click="evalZoom = Math.max(0.6, evalZoom - 0.2)"
                ></v-btn>
                <span class="text-caption font-weight-bold" style="min-width: 42px; text-align: center;">
                  {{ Math.round(evalZoom * 100) }}%
                </span>
                <v-btn
                  icon="mdi-magnify-plus-outline"
                  size="small"
                  variant="text"
                  title="Aumentar Zoom"
                  :disabled="evalZoom >= 2.5"
                  @click="evalZoom = Math.min(2.5, evalZoom + 0.2)"
                ></v-btn>
                <v-btn
                  icon="mdi-fit-to-screen-outline"
                  size="small"
                  variant="text"
                  title="Repor Zoom (100%)"
                  @click="evalZoom = 1.0"
                ></v-btn>

                <v-divider vertical class="mx-1"></v-divider>

                <v-btn
                  size="small"
                  variant="text"
                  prepend-icon="mdi-undo"
                  class="text-none"
                  :disabled="undoStack.length === 0"
                  @click="undoCanvas"
                >
                  Desfazer
                </v-btn>
                <v-btn
                  size="small"
                  variant="text"
                  color="error"
                  prepend-icon="mdi-eraser"
                  class="text-none"
                  @click="clearCanvas"
                >
                  Limpar
                </v-btn>
              </div>
            </div>
          </v-card>

          <!-- Interactive Question Image + Canvas Overlay -->
          <div class="text-center mb-4 modal-image-container d-flex justify-center align-center overflow-auto" style="max-height: 60vh;">
            <div
              v-if="questionImages[activeTask.id]"
              ref="canvasContainerRef"
              class="canvas-drawing-wrapper"
              :class="{
                'cursor-pen': drawingTool === 'pen',
                'cursor-stamp': drawingTool === 'check' || drawingTool === 'cross' || drawingTool === 'text'
              }"
              :style="{ transform: `scale(${evalZoom})`, transformOrigin: 'top center', transition: 'transform 0.12s ease' }"
            >
              <img
                ref="dialogImageRef"
                :src="questionImages[activeTask.id]"
                alt="Fragmento da Resolução"
                class="modal-question-image"
                @load="initCanvasOverlay"
              />
              <canvas
                ref="drawingCanvasRef"
                class="drawing-canvas-layer"
                @mousedown="startDrawing"
                @mousemove="draw"
                @mouseup="stopDrawing"
                @mouseleave="stopDrawing"
                @touchstart="handleTouchStart"
                @touchmove="handleTouchMove"
                @touchend="stopDrawing"
              ></canvas>
            </div>
            <v-progress-circular v-else indeterminate></v-progress-circular>
          </div>

          <v-form ref="evalForm" v-model="evalFormValid">
            <v-row>
              <v-col cols="12" sm="4">
                <v-text-field
                  v-model.number="givenScore"
                  label="Nota Atribuída (val.)*"
                  type="number"
                  step="0.1"
                  min="0"
                  :max="activeTask.maxScore"
                  density="comfortable"
                  variant="outlined"
                  :rules="[
                    v => (v !== null && v !== '') || 'Nota é obrigatória',
                    v => (v >= 0) || 'Nota não pode ser negativa',
                    v => (!activeTask || v <= activeTask.maxScore) || `Nota não pode exceder ${activeTask?.maxScore} val.`
                  ]"
                ></v-text-field>
                <div class="text-caption text-medium-emphasis">
                  Cotação Máxima: <strong>{{ activeTask.maxScore.toFixed(1) }} val.</strong>
                </div>
              </v-col>

              <v-col cols="12" sm="8">
                <v-text-field
                  v-model="evalFeedback"
                  label="Observações / Feedback ao Aluno (opcional)"
                  placeholder="ex: Justificação completa e raciocínio correto."
                  density="comfortable"
                  variant="outlined"
                  hide-details
                ></v-text-field>
              </v-col>
            </v-row>
          </v-form>
        </v-card-text>

        <v-card-actions class="pa-4 pt-0">
          <v-spacer></v-spacer>
          <v-btn text="Cancelar" variant="plain" @click="evaluationDialog = false"></v-btn>
          <v-btn
            color="success"
            variant="flat"
            prepend-icon="mdi-check"
            class="text-none font-weight-bold"
            :loading="submitting"
            @click="submitEvaluation"
          >
            Gravar Classificação
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- REVIEW EVALUATION MODAL (Phase 5) -->
    <v-dialog v-model="reviewDialog" max-width="850">
      <v-card v-if="activeReview">
        <v-toolbar color="purple" density="compact">
          <v-toolbar-title class="text-subtitle-1 font-weight-bold text-white">
            Avaliação de Revisão: Item {{ activeReview.questionNumber }} ({{ activeReview.disciplineName }})
          </v-toolbar-title>
          <div class="d-flex align-center gap-1 mr-2">
            <v-btn
              icon="mdi-magnify-minus-outline"
              size="small"
              variant="text"
              color="white"
              title="Diminuir Zoom"
              :disabled="reviewZoom <= 0.6"
              @click="reviewZoom = Math.max(0.6, reviewZoom - 0.2)"
            ></v-btn>
            <span class="text-caption font-weight-bold text-white" style="min-width: 42px; text-align: center;">
              {{ Math.round(reviewZoom * 100) }}%
            </span>
            <v-btn
              icon="mdi-magnify-plus-outline"
              size="small"
              variant="text"
              color="white"
              title="Aumentar Zoom"
              :disabled="reviewZoom >= 2.5"
              @click="reviewZoom = Math.min(2.5, reviewZoom + 0.2)"
            ></v-btn>
            <v-btn
              icon="mdi-fit-to-screen-outline"
              size="small"
              variant="text"
              color="white"
              title="Repor Zoom (100%)"
              @click="reviewZoom = 1.0"
            ></v-btn>
          </div>
          <v-btn icon="mdi-close" color="white" @click="reviewDialog = false"></v-btn>
        </v-toolbar>

        <v-card-text class="pa-4">
          <!-- Question Image with Zoom -->
          <div class="text-center mb-4 modal-image-container overflow-auto" style="max-height: 55vh;">
            <div :style="{ transform: `scale(${reviewZoom})`, transformOrigin: 'top center', transition: 'transform 0.12s ease' }">
              <img
                v-if="reviewImages[activeReview.questionId]"
                :src="reviewImages[activeReview.questionId]"
                alt="Fragmento Alvo de Revisão"
                class="modal-question-image"
              />
              <v-progress-circular v-else indeterminate></v-progress-circular>
            </div>
          </div>

          <v-alert
            type="info"
            variant="tonal"
            density="comfortable"
            class="mb-4"
            title="Justificação Apresentada pelo Aluno:"
          >
            <div class="text-body-1 font-italic mt-1">"{{ activeReview.justification }}"</div>
          </v-alert>

          <v-form ref="reviewForm" v-model="reviewFormValid">
            <v-row>
              <v-col cols="12" sm="4">
                <v-text-field
                  v-model.number="revisedScore"
                  label="Nota Definitiva (val.)*"
                  type="number"
                  step="0.1"
                  min="0"
                  :max="activeReview.maxScore"
                  density="comfortable"
                  variant="outlined"
                  :rules="[
                    v => (v !== null && v !== '') || 'Nota é obrigatória',
                    v => (v >= 0) || 'Nota não pode ser negativa',
                    v => (!activeReview || v <= activeReview.maxScore) || `Nota não pode exceder ${activeReview?.maxScore} val.`
                  ]"

                ></v-text-field>
                <div class="text-caption text-medium-emphasis">
                  Nota Original: <strong>{{ activeReview.originalScore?.toFixed(1) ?? '0.0' }}</strong> / {{ activeReview.maxScore.toFixed(1) }} val.
                </div>
              </v-col>

              <v-col cols="12" sm="8">
                <v-textarea
                  v-model="reviewFeedback"
                  label="Decisão / Parecer da Revisão*"
                  placeholder="Justifique a decisão da revisão de prova..."
                  rows="2"
                  density="comfortable"
                  variant="outlined"
                  :rules="[v => !!v || 'Parecer de revisão obrigatório']"
                ></v-textarea>
              </v-col>
            </v-row>
          </v-form>
        </v-card-text>

        <v-card-actions class="pa-4 pt-0">
          <v-spacer></v-spacer>
          <v-btn text="Cancelar" variant="plain" @click="reviewDialog = false"></v-btn>
          <v-btn
            color="purple"
            variant="flat"
            prepend-icon="mdi-gavel"
            class="text-none font-weight-bold"
            :loading="submittingReview"
            @click="submitReview"
          >
            Emitir Decisão Definitiva
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import type TeacherTaskDto from '@/models/TeacherTaskDto'
import type ReviewRequestDto from '@/models/ReviewRequestDto'
import RemoteService from '@/services/RemoteService'

const activeTab = ref<'tasks' | 'reviews'>('tasks')

const tasks = ref<TeacherTaskDto[]>([])
const loadingTasks = ref(true)
const taskFilter = ref<'ALL' | 'PENDING' | 'COMPLETED'>('ALL')
const taskSearch = ref('')
const questionImages = reactive<Record<number, string>>({})

const evaluationDialog = ref(false)
const activeTask = ref<TeacherTaskDto | null>(null)
const givenScore = ref<number | null>(null)
const evalFeedback = ref('')
const evalForm = ref<any>(null)
const evalFormValid = ref(false)
const submitting = ref(false)

const reviewTasks = ref<ReviewRequestDto[]>([])
const loadingReviews = ref(true)
const reviewImages = reactive<Record<number, string>>({})

const reviewDialog = ref(false)
const activeReview = ref<ReviewRequestDto | null>(null)
const revisedScore = ref<number | null>(null)
const reviewFeedback = ref('')
const reviewForm = ref<any>(null)
const reviewFormValid = ref(false)
const submittingReview = ref(false)

const pendingTasksCount = computed(() => {
  return tasks.value.filter(t => t.score === null || t.score === undefined).length
})

const completedTasksCount = computed(() => {
  return tasks.value.filter(t => t.score !== null && t.score !== undefined).length
})

const pendingReviewsCount = computed(() => {
  return reviewTasks.value.filter(r => r.status === 'ASSIGNED' || r.status === 'PENDING').length
})

const filteredTasks = computed(() => {
  let list = tasks.value
  if (taskFilter.value === 'PENDING') {
    list = list.filter(t => t.score === null || t.score === undefined)
  } else if (taskFilter.value === 'COMPLETED') {
    list = list.filter(t => t.score !== null && t.score !== undefined)
  }

  if (taskSearch.value && taskSearch.value.trim() !== '') {
    const q = taskSearch.value.toLowerCase().trim()
    list = list.filter(t =>
      t.questionNumber.toLowerCase().includes(q) ||
      t.disciplineName?.toLowerCase().includes(q)
    )
  }
  return list
})

onMounted(async () => {
  await Promise.all([loadTasks(), loadReviews()])
})

const loadTasks = async () => {
  loadingTasks.value = true
  try {
    const data = await RemoteService.getTeacherTasks()
    tasks.value = data
    for (const t of data) {
      if (!questionImages[t.id]) {
        RemoteService.getQuestionImageBlob(t.id).then(url => {
          questionImages[t.id] = url
        }).catch(console.error)
      }
    }
  } catch (err) {
    console.error('Error fetching teacher tasks:', err)
  } finally {
    loadingTasks.value = false
  }
}

const loadReviews = async () => {
  loadingReviews.value = true
  try {
    const data = await RemoteService.getTeacherReviews()
    reviewTasks.value = data
    for (const r of data) {
      if (!reviewImages[r.questionId]) {
        RemoteService.getQuestionImageBlob(r.questionId).then(url => {
          reviewImages[r.questionId] = url
        }).catch(console.error)
      }
    }
  } catch (err) {
    console.error('Error fetching teacher reviews:', err)
  } finally {
    loadingReviews.value = false
  }
}

// Canvas Drawing & Annotation State
const evalZoom = ref(1.0)
const reviewZoom = ref(1.0)
const drawingTool = ref<'pen' | 'check' | 'cross' | 'text'>('pen')
const customAnnotationText = ref('+1.0')
const canvasContainerRef = ref<HTMLDivElement | null>(null)
const dialogImageRef = ref<HTMLImageElement | null>(null)
const drawingCanvasRef = ref<HTMLCanvasElement | null>(null)

const isDrawing = ref(false)
const undoStack = ref<ImageData[]>([])
const hasDrawnAnnotations = ref(false)

const initCanvasOverlay = () => {
  const img = dialogImageRef.value
  const canvas = drawingCanvasRef.value
  if (!img || !canvas) return

  canvas.width = img.naturalWidth || img.width || 800
  canvas.height = img.naturalHeight || img.height || 600

  const ctx = canvas.getContext('2d')
  if (ctx) {
    ctx.lineCap = 'round'
    ctx.lineJoin = 'round'
  }
  undoStack.value = []
  hasDrawnAnnotations.value = false
  saveCanvasState()
}

const saveCanvasState = () => {
  const canvas = drawingCanvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return
  if (undoStack.value.length > 20) {
    undoStack.value.shift()
  }
  undoStack.value.push(ctx.getImageData(0, 0, canvas.width, canvas.height))
}

const undoCanvas = () => {
  const canvas = drawingCanvasRef.value
  if (!canvas || undoStack.value.length <= 1) return
  undoStack.value.pop()
  const prevState = undoStack.value[undoStack.value.length - 1]
  const ctx = canvas.getContext('2d')
  if (ctx && prevState) {
    ctx.putImageData(prevState, 0, 0)
    hasDrawnAnnotations.value = undoStack.value.length > 1
  }
}

const clearCanvas = () => {
  const canvas = drawingCanvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  undoStack.value = []
  hasDrawnAnnotations.value = false
  saveCanvasState()
}

const getCanvasCoordinates = (e: MouseEvent | Touch): { x: number; y: number } => {
  const canvas = drawingCanvasRef.value
  if (!canvas) return { x: 0, y: 0 }
  const rect = canvas.getBoundingClientRect()
  const scaleX = canvas.width / rect.width
  const scaleY = canvas.height / rect.height
  return {
    x: (e.clientX - rect.left) * scaleX,
    y: (e.clientY - rect.top) * scaleY
  }
}

const startDrawing = (e: MouseEvent) => {
  const canvas = drawingCanvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const { x, y } = getCanvasCoordinates(e)

  if (drawingTool.value === 'check') {
    drawCheckmark(ctx, x, y)
    hasDrawnAnnotations.value = true
    saveCanvasState()
    return
  } else if (drawingTool.value === 'cross') {
    drawCross(ctx, x, y)
    hasDrawnAnnotations.value = true
    saveCanvasState()
    return
  } else if (drawingTool.value === 'text') {
    drawTextAnnotation(ctx, x, y, customAnnotationText.value || '+1.0')
    hasDrawnAnnotations.value = true
    saveCanvasState()
    return
  }

  isDrawing.value = true
  ctx.beginPath()
  ctx.moveTo(x, y)
  ctx.strokeStyle = '#e53935'
  ctx.lineWidth = Math.max(3, canvas.width / 300)
  ctx.globalAlpha = 1.0
}

const draw = (e: MouseEvent) => {
  if (!isDrawing.value) return
  const canvas = drawingCanvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const { x, y } = getCanvasCoordinates(e)
  ctx.lineTo(x, y)
  ctx.stroke()
  hasDrawnAnnotations.value = true
}

const stopDrawing = () => {
  if (isDrawing.value) {
    isDrawing.value = false
    const canvas = drawingCanvasRef.value
    if (canvas) {
      const ctx = canvas.getContext('2d')
      if (ctx) {
        ctx.closePath()
        ctx.globalAlpha = 1.0
      }
      saveCanvasState()
    }
  }
}

const handleTouchStart = (e: TouchEvent) => {
  if (e.touches.length === 1) {
    e.preventDefault()
    startDrawing(e.touches[0] as unknown as MouseEvent)
  }
}

const handleTouchMove = (e: TouchEvent) => {
  if (e.touches.length === 1) {
    e.preventDefault()
    draw(e.touches[0] as unknown as MouseEvent)
  }
}

const drawCheckmark = (ctx: CanvasRenderingContext2D, x: number, y: number) => {
  const size = 28
  ctx.save()
  ctx.globalAlpha = 0.9
  ctx.fillStyle = '#2e7d32'
  ctx.beginPath()
  ctx.arc(x, y, size, 0, Math.PI * 2)
  ctx.fill()

  ctx.strokeStyle = '#ffffff'
  ctx.lineWidth = 5
  ctx.lineCap = 'round'
  ctx.lineJoin = 'round'
  ctx.beginPath()
  ctx.moveTo(x - size * 0.45, y)
  ctx.lineTo(x - size * 0.1, y + size * 0.4)
  ctx.lineTo(x + size * 0.45, y - size * 0.35)
  ctx.stroke()
  ctx.restore()
}

const drawCross = (ctx: CanvasRenderingContext2D, x: number, y: number) => {
  const size = 28
  ctx.save()
  ctx.globalAlpha = 0.9
  ctx.fillStyle = '#c62828'
  ctx.beginPath()
  ctx.arc(x, y, size, 0, Math.PI * 2)
  ctx.fill()

  ctx.strokeStyle = '#ffffff'
  ctx.lineWidth = 5
  ctx.lineCap = 'round'
  ctx.beginPath()
  ctx.moveTo(x - size * 0.35, y - size * 0.35)
  ctx.lineTo(x + size * 0.35, y + size * 0.35)
  ctx.moveTo(x + size * 0.35, y - size * 0.35)
  ctx.lineTo(x - size * 0.35, y + size * 0.35)
  ctx.stroke()
  ctx.restore()
}

const drawTextAnnotation = (ctx: CanvasRenderingContext2D, x: number, y: number, text: string) => {
  ctx.save()
  ctx.font = 'bold 22px Arial, sans-serif'
  const textWidth = ctx.measureText(text).width
  const paddingX = 10
  const height = 30

  ctx.fillStyle = '#d32f2f'
  ctx.beginPath()
  ctx.roundRect(x, y - height / 2, textWidth + paddingX * 2, height, 6)
  ctx.fill()

  ctx.fillStyle = '#ffffff'
  ctx.textBaseline = 'middle'
  ctx.fillText(text, x + paddingX, y)
  ctx.restore()
}

const getAnnotatedCanvasDataUrl = (): string => {
  const img = dialogImageRef.value
  const drawingCanvas = drawingCanvasRef.value
  if (!img || !drawingCanvas) return ''

  const composite = document.createElement('canvas')
  composite.width = drawingCanvas.width
  composite.height = drawingCanvas.height
  const ctx = composite.getContext('2d')
  if (!ctx) return ''

  ctx.drawImage(img, 0, 0, composite.width, composite.height)
  ctx.drawImage(drawingCanvas, 0, 0)

  return composite.toDataURL('image/png')
}

const openEvaluationDialog = (task: TeacherTaskDto) => {
  activeTask.value = task
  givenScore.value = task.score ?? null
  evalFeedback.value = task.feedback ?? ''
  evaluationDialog.value = true
  setTimeout(() => {
    initCanvasOverlay()
  }, 100)
}

const submitEvaluation = async () => {
  if (evalForm.value) {
    const { valid } = await evalForm.value.validate()
    if (!valid) return
  }
  if (!activeTask.value || givenScore.value === null) return

  submitting.value = true
  try {
    const annotatedData = hasDrawnAnnotations.value ? getAnnotatedCanvasDataUrl() : undefined
    await RemoteService.submitTaskEvaluation(activeTask.value.id, {
      score: Number(givenScore.value),
      feedback: evalFeedback.value.trim() || undefined,
      annotatedImageData: annotatedData
    })
    evaluationDialog.value = false
    await loadTasks()
  } catch (err) {
    console.error('Error submitting evaluation:', err)
  } finally {
    submitting.value = false
  }
}

const openReviewDialog = (review: ReviewRequestDto) => {
  activeReview.value = review
  revisedScore.value = review.revisedScore ?? review.originalScore ?? null
  reviewFeedback.value = review.reviewerFeedback ?? ''
  reviewDialog.value = true
}

const submitReview = async () => {
  if (reviewForm.value) {
    const { valid } = await reviewForm.value.validate()
    if (!valid) return
  }
  if (!activeReview.value || revisedScore.value === null) return

  submittingReview.value = true
  try {
    await RemoteService.submitReviewEvaluation(activeReview.value.id, {
      revisedScore: Number(revisedScore.value),
      reviewerFeedback: reviewFeedback.value.trim()
    })
    reviewDialog.value = false
    await loadReviews()
  } catch (err) {
    console.error('Error submitting review evaluation:', err)
  } finally {
    submittingReview.value = false
  }
}
</script>

<style scoped>
.task-card {
  transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 12px;
  background-color: rgb(var(--v-theme-surface));
}

.task-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.05);
}

.v-theme--dark .task-card:hover {
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.4), 0 8px 10px -6px rgba(0, 0, 0, 0.3);
}

.task-image-preview {
  height: 190px;
  background-color: #0b0f19;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top-left-radius: 12px;
  border-top-right-radius: 12px;
}

.task-thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.task-image-preview:hover .task-thumb-img {
  transform: scale(1.04);
}

.task-thumb-overlay {
  position: absolute;
  inset: 0;
  background-color: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 13px;
  font-weight: 700;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.task-image-preview:hover .task-thumb-overlay {
  opacity: 1;
}

.modal-image-container {
  max-height: 480px;
  overflow-y: auto;
  background-color: #0b0f19;
  padding: 16px;
  border-radius: 12px;
  border: 1px solid rgba(100, 116, 139, 0.2);
}

.canvas-drawing-wrapper {
  position: relative;
  display: inline-block;
  max-width: 100%;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
}

.drawing-canvas-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  touch-action: none;
  z-index: 10;
}

.cursor-pen {
  cursor: crosshair;
}

.cursor-stamp {
  cursor: pointer;
}

.modal-question-image {
  max-width: 100%;
  max-height: 420px;
  object-fit: contain;
  border-radius: 6px;
  display: block;
}
</style>
