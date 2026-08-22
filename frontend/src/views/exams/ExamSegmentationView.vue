<template>
  <v-container fluid class="pa-0">
    <!-- Header / Action Bar -->
    <v-card variant="flat" class="mb-4 pa-4 bg-surface rounded-lg border">
      <v-row align="center" justify="space-between">
        <v-col cols="12" sm="8" class="d-flex align-center flex-wrap gap-2 text-left">
          <v-btn
            icon="mdi-arrow-left"
            variant="text"
            to="/exams"
            class="mr-2"
            title="Voltar aos Exames"
          ></v-btn>
          <div>
            <div class="d-flex align-center">
              <h2 class="text-h6 font-weight-bold mr-3">{{ exam?.title ?? 'Carregando exame...' }}</h2>
              <v-chip
                v-if="exam?.status === 'UPLOADED'"
                color="orange"
                size="small"
                variant="flat"
                class="font-weight-bold"
              >
                Pendente Separação ({{ totalCalculatedScore.toFixed(1) }} / 20.0 val.)
              </v-chip>
              <v-chip
                v-else-if="exam?.status === 'SEGMENTED'"
                color="green"
                size="small"
                variant="flat"
                class="font-weight-bold"
                prepend-icon="mdi-check-circle"
              >
                Segmentado ({{ examQuestions.length }} itens - 20.0 val.)
              </v-chip>
              <v-chip
                v-else-if="isExamLocked"
                color="blue"
                size="small"
                variant="flat"
                class="font-weight-bold"
                prepend-icon="mdi-lock"
              >
                Distribuído / Em Avaliação
              </v-chip>
            </div>
            <div class="text-caption text-medium-emphasis mt-1">
              Escola: <strong>{{ exam?.schoolName }}</strong> |
              Disciplina: <strong>{{ exam?.disciplineName }}</strong> |
              Aluno: <strong>{{ exam?.studentName }}</strong> ({{ exam?.studentEmail }})
            </div>
          </div>
        </v-col>

        <v-col cols="12" sm="4" class="text-sm-right">
          <v-tooltip
            :disabled="isScoreValid && !isExamLocked"
            :text="isExamLocked ? 'Esta prova já foi distribuída e encontra-se bloqueada.' : (isExamSegmented ? 'Esta prova já se encontra segmentada com 20.0 valores.' : 'A cotação total das perguntas deve ser exatamente 20.0 valores para concluir a separação.')"
            location="bottom"
          >
            <template v-slot:activator="{ props }">
              <span v-bind="props">
                <v-btn
                  :color="isExamSegmented ? 'teal' : 'success'"
                  :prepend-icon="isExamSegmented ? 'mdi-check-all' : 'mdi-check-circle'"
                  class="text-none font-weight-bold"
                  :disabled="!isScoreValid || completing || isExamLocked"
                  :loading="completing"
                  @click="completeSegmentation"
                >
                  {{ isExamSegmented ? 'Segmentação Concluída' : 'Concluir Segmentação' }}
                </v-btn>
              </span>
            </template>
          </v-tooltip>
        </v-col>
      </v-row>
    </v-card>

    <!-- Locked Alert -->
    <v-alert
      v-if="isExamLocked"
      type="info"
      variant="tonal"
      density="comfortable"
      class="mb-4"
      icon="mdi-lock"
      title="Prova Bloqueada para Alterações"
    >
      Esta prova já foi distribuída para avaliação docente pelo administrador. A adição e eliminação de perguntas encontram-se permanentemente bloqueadas.
    </v-alert>

    <v-row>
      <!-- LEFT: PDF Viewer & Interactive Bounding Box Tool -->
      <v-col cols="12" lg="7">
        <v-card variant="outlined" class="pa-4 mb-4">
          <!-- Page Navigation & Quick Actions -->
          <v-row align="center" justify="space-between" class="mb-3">
            <v-col cols="auto" class="d-flex align-center">
              <v-btn
                icon="mdi-chevron-left"
                size="small"
                variant="tonal"
                :disabled="currentPage <= 1 || loadingPage"
                @click="changePage(currentPage - 1)"
              ></v-btn>
              <span class="mx-3 font-weight-bold text-body-2">
                Página {{ currentPage }} de {{ exam?.totalPages ?? 1 }}
              </span>
              <v-btn
                icon="mdi-chevron-right"
                size="small"
                variant="tonal"
                :disabled="!exam || currentPage >= exam.totalPages || loadingPage"
                @click="changePage(currentPage + 1)"
              ></v-btn>
            </v-col>

            <v-col cols="auto" class="d-flex align-center gap-2">
              <v-btn
                v-if="selectionMode === 'crop' && hasSelection"
                size="small"
                variant="tonal"
                color="error"
                prepend-icon="mdi-close"
                class="text-none"
                @click="clearSelection"
              >
                Limpar Recorte
              </v-btn>

              <v-btn
                v-if="selectionMode === 'crop'"
                size="small"
                variant="tonal"
                color="primary"
                prepend-icon="mdi-file-document-outline"
                class="text-none"
                @click="selectFullPage"
              >
                Mudar p/ Página Toda
              </v-btn>

              <v-btn
                v-else
                size="small"
                variant="tonal"
                color="primary"
                prepend-icon="mdi-crop"
                class="text-none"
                @click="selectionMode = 'crop'"
              >
                Mudar p/ Recortar Parte
              </v-btn>
            </v-col>
          </v-row>

          <!-- Interactive Canvas / Image Viewer -->
          <div
            ref="viewerContainer"
            class="pdf-viewer-wrapper"
            :class="{ 'crosshair-cursor': selectionMode === 'crop' }"
            @mousedown="onMouseDown"
            @mousemove="onMouseMove"
            @mouseup="onMouseUp"
          >
            <div v-if="loadingPage" class="d-flex align-center justify-center fill-height page-loading-placeholder">
              <v-progress-circular indeterminate color="primary"></v-progress-circular>
            </div>

            <div v-show="!loadingPage && pageImageUrl" class="image-relative-container">
              <img
                ref="pageImage"
                :src="pageImageUrl || undefined"
                class="pdf-page-image"
                alt="Página do Exame"
                @load="onImageLoaded"
                draggable="false"
              />

              <!-- Full Page Selection Overlay -->
              <div v-if="selectionMode === 'full'" class="full-page-overlay">
                <div class="full-page-badge">
                  <v-icon icon="mdi-file-check-outline" size="14" class="mr-1"></v-icon>
                  Página {{ currentPage }} Completa Selecionada
                </div>
              </div>

              <!-- Drawn Crop Bounding Box Overlay -->
              <div
                v-if="selectionMode === 'crop' && selectionRect.active"
                class="bounding-box-overlay"
                :style="{
                  left: selectionRect.displayX + 'px',
                  top: selectionRect.displayY + 'px',
                  width: selectionRect.displayWidth + 'px',
                  height: selectionRect.displayHeight + 'px'
                }"
              >
                <div class="bounding-box-badge">
                  <v-icon icon="mdi-crop" size="12" class="mr-1"></v-icon>
                  Área Recortada
                </div>
              </div>
            </div>
          </div>
        </v-card>
      </v-col>

      <!-- RIGHT: Question Definition & Extracted Fragments -->
      <v-col cols="12" lg="5">
        <!-- Locked Read-only Notice Card -->
        <v-card v-if="isExamLocked" variant="outlined" class="pa-5 mb-6 rounded-xl bg-grey-lighten-4 text-center">
          <v-icon icon="mdi-lock-check" size="36" color="grey" class="mb-2"></v-icon>
          <div class="text-subtitle-2 font-weight-bold">Segmentação Bloqueada</div>
          <div class="text-caption text-medium-emphasis">Esta prova já foi consolidada ou distribuída para avaliação. Não é permitido adicionar ou eliminar perguntas.</div>
        </v-card>

        <!-- New Question Form (Only when editable) -->
        <v-card v-else variant="outlined" class="pa-5 mb-6 rounded-xl bg-surface">
          <v-card-title class="pa-0 mb-4 text-subtitle-1 font-weight-bold d-flex align-center">
            <v-icon icon="mdi-puzzle-plus" color="primary" class="mr-2"></v-icon>
            Adicionar Pergunta
          </v-card-title>

          <!-- Intuitive Mode Selection Cards -->
          <div class="mb-4">
            <div class="text-caption font-weight-bold text-medium-emphasis text-uppercase mb-2 text-left">
              Formato da Resposta:
            </div>
            <v-row dense>
              <v-col cols="6">
                <v-card
                  :variant="selectionMode === 'crop' ? 'tonal' : 'outlined'"
                  :color="selectionMode === 'crop' ? 'primary' : undefined"
                  class="pa-4 text-center cursor-pointer fill-height d-flex flex-column align-center justify-center mode-card"
                  :class="{ 'mode-card-active': selectionMode === 'crop' }"
                  @click="selectionMode = 'crop'"
                >
                  <v-icon icon="mdi-crop" size="26" class="mb-1"></v-icon>
                  <div class="font-weight-bold text-body-2">Recortar Área</div>
                  <div class="text-caption text-medium-emphasis">Desenhar retângulo</div>
                </v-card>
              </v-col>

              <v-col cols="6">
                <v-card
                  :variant="selectionMode === 'full' ? 'tonal' : 'outlined'"
                  :color="selectionMode === 'full' ? 'primary' : undefined"
                  class="pa-4 text-center cursor-pointer fill-height d-flex flex-column align-center justify-center mode-card"
                  :class="{ 'mode-card-active': selectionMode === 'full' }"
                  @click="selectFullPage"
                >
                  <v-icon icon="mdi-file-document-outline" size="26" class="mb-1"></v-icon>
                  <div class="font-weight-bold text-body-2">Página Toda</div>
                  <div class="text-caption text-medium-emphasis">Pág. {{ currentPage }} completa</div>
                </v-card>
              </v-col>
            </v-row>
          </div>

          <!-- Dynamic Mode Instruction Alert -->
          <v-alert
            v-if="selectionMode === 'crop' && !hasSelection"
            density="compact"
            type="info"
            variant="tonal"
            class="mb-4 text-caption text-left rounded-lg"
            icon="mdi-cursor-pointer"
          >
            Arraste o rato sobre a folha à esquerda para selecionar a área da resposta do aluno.
          </v-alert>

          <v-alert
            v-else-if="selectionMode === 'crop' && hasSelection"
            density="compact"
            type="success"
            variant="tonal"
            class="mb-4 text-caption text-left rounded-lg"
            icon="mdi-check-circle-outline"
          >
            Área selecionada na Página {{ currentPage }}.
          </v-alert>

          <v-alert
            v-else
            density="compact"
            type="info"
            variant="tonal"
            class="mb-4 text-caption text-left rounded-lg"
            icon="mdi-file-check-outline"
          >
            A <strong>Página {{ currentPage }} inteira</strong> será associada a esta pergunta.
          </v-alert>

          <v-form ref="questionForm" v-model="questionFormValid">
            <v-row dense>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="questionNumber"
                  label="Número da Pergunta*"
                  placeholder="ex: 1, 2.1, 3"
                  required
                  density="comfortable"
                  variant="outlined"
                  :rules="[v => !!v || 'Indique o número da pergunta']"
                ></v-text-field>
              </v-col>

              <v-col cols="12" sm="6">
                <v-text-field
                  v-model.number="questionScore"
                  label="Cotação (val.)*"
                  placeholder="ex: 2.5"
                  type="number"
                  step="0.1"
                  min="0.1"
                  required
                  density="comfortable"
                  variant="outlined"
                  :rules="[
                    v => (v !== null && v > 0) || 'Cotação deve ser > 0',
                    v => (v !== null && v <= (20.001 - totalCalculatedScore)) || `A soma não pode exceder 20.0 val. (restam ${remainingScore.toFixed(1)} val.)`
                  ]"
                ></v-text-field>
              </v-col>
            </v-row>

            <v-btn
              block
              color="primary"
              size="large"
              prepend-icon="mdi-plus"
              class="text-none font-weight-bold mt-3 py-3 rounded-xl"
              :loading="addingQuestion"
              @click="addQuestionFragment"
            >
              Guardar Pergunta
            </v-btn>
          </v-form>
        </v-card>

        <!-- Extracted Questions List -->
        <v-card variant="outlined" class="pa-5 mb-6 rounded-xl bg-surface">
          <div class="d-flex align-center justify-space-between mb-4">
            <div class="text-subtitle-1 font-weight-bold d-flex align-center">
              <v-icon icon="mdi-format-list-numbered" color="teal" class="mr-2"></v-icon>
              Perguntas Criadas ({{ examQuestions.length }})
            </div>
            <v-chip
              size="small"
              :color="isScoreValid ? 'teal' : totalCalculatedScore > 20 ? 'error' : 'orange'"
              variant="flat"
              class="font-weight-bold px-3"
            >
              Total: {{ totalCalculatedScore.toFixed(1) }} / 20.0 val.
            </v-chip>
          </div>

          <div v-if="examQuestions.length === 0" class="pa-8 text-center text-medium-emphasis">
            <v-icon icon="mdi-image-filter-none" size="48" class="mb-2 opacity-50"></v-icon>
            <p>Nenhuma pergunta adicionada ainda.</p>
            <p class="text-caption">Selecione uma área na folha à esquerda e clique em "Guardar Pergunta".</p>
          </div>

          <v-list v-else class="pa-0 max-questions-list">
            <v-card
              v-for="q in examQuestions"
              :key="q.id"
              variant="outlined"
              class="mb-2 pa-2 question-item-card"
            >
              <div class="d-flex align-center">
                <!-- Thumbnail -->
                <div class="fragment-thumb-wrapper mr-3" @click="previewQuestion(q)">
                  <img
                    v-if="questionImageUrls[q.id]"
                    :src="questionImageUrls[q.id]"
                    alt="Fragmento"
                    class="fragment-thumb"
                  />
                  <v-progress-circular v-else indeterminate size="20" width="2"></v-progress-circular>
                </div>

                <!-- Info -->
                <div class="flex-grow-1 text-left">
                  <div class="d-flex align-center justify-space-between">
                    <span class="font-weight-bold text-subtitle-2">Item {{ q.questionNumber }}</span>
                    <v-chip size="x-small" color="primary" variant="flat" class="font-weight-bold">
                      {{ q.maxScore.toFixed(1) }} val.
                    </v-chip>
                  </div>
                  <div class="text-caption text-medium-emphasis mt-1">
                    Página {{ q.pageNumber }}
                    <span v-if="q.cropWidth && q.cropWidth < 0.99" class="ml-1 text-teal font-weight-bold">• Recorte</span>
                    <span v-else class="ml-1 text-purple font-weight-bold">• Pág. Completa</span>
                  </div>
                </div>

                <!-- Delete (Only when editable) -->
                <v-btn
                  v-if="!isExamLocked"
                  icon="mdi-delete-outline"
                  size="small"
                  variant="text"
                  color="error"
                  title="Eliminar pergunta"
                  @click="deleteQuestion(q.id)"
                ></v-btn>
              </div>
            </v-card>
          </v-list>
        </v-card>
      </v-col>
    </v-row>

    <!-- Question Image Preview Dialog -->
    <v-dialog v-model="previewDialog" max-width="700">
      <v-card>
        <v-toolbar color="primary" density="compact">
          <v-toolbar-title class="text-subtitle-1">
            Visualização: Item {{ activePreviewQuestion?.questionNumber }} ({{ activePreviewQuestion?.maxScore }} val.)
          </v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn icon="mdi-close" @click="previewDialog = false"></v-btn>
        </v-toolbar>
        <v-card-text class="pa-4 text-center">
          <img
            v-if="activePreviewQuestion && questionImageUrls[activePreviewQuestion.id]"
            :src="questionImageUrls[activePreviewQuestion.id]"
            style="max-width: 100%; max-height: 70vh; object-fit: contain;"
            alt="Pré-visualização do Fragmento"
          />
        </v-card-text>
      </v-card>
    </v-dialog>

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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type ExamDto from '@/models/ExamDto'
import type QuestionDto from '@/models/QuestionDto'
import type { QuestionCropRequest } from '@/models/QuestionDto'
import RemoteService from '@/services/RemoteService'

const route = useRoute()
const router = useRouter()
const examId = Number(route.params.id)

const exam = ref<ExamDto | null>(null)
const examQuestions = ref<QuestionDto[]>([])
const questionImageUrls = reactive<Record<number, string>>({})

const currentPage = ref(1)
const loadingPage = ref(true)
const pageImageUrl = ref<string | null>(null)

const selectionMode = ref<'crop' | 'full'>('crop')
const questionNumber = ref('1')
const questionScore = ref<number | null>(2.5)
const questionFormValid = ref(false)
const questionForm = ref<any>(null)
const addingQuestion = ref(false)
const completing = ref(false)

const previewDialog = ref(false)
const activePreviewQuestion = ref<QuestionDto | null>(null)

const viewerContainer = ref<HTMLElement | null>(null)
const pageImage = ref<HTMLImageElement | null>(null)
const isDragging = ref(false)
const startX = ref(0)
const startY = ref(0)

const selectionRect = reactive({
  active: false,
  displayX: 0,
  displayY: 0,
  displayWidth: 0,
  displayHeight: 0,
  // Normalized 0.0 to 1.0 coordinates relative to rendered image
  normX: 0,
  normY: 0,
  normWidth: 0,
  normHeight: 0
})

const hasSelection = computed(() => selectionRect.active && selectionRect.normWidth > 0.01)

const totalCalculatedScore = computed(() => {
  return examQuestions.value.reduce((sum, q) => sum + (q.maxScore || 0), 0)
})

const isScoreValid = computed(() => {
  return Math.abs(totalCalculatedScore.value - 20.0) < 0.001
})

const remainingScore = computed(() => {
  return Math.max(0, +(20.0 - totalCalculatedScore.value).toFixed(1))
})

const submissionsLocked = ref(false)

const isExamSegmented = computed(() => {
  return exam.value?.status === 'SEGMENTED'
})

const isExamLocked = computed(() => {
  if (!exam.value) return false
  return ['DISTRIBUTED', 'CORRECTED', 'RELEASED'].includes(exam.value.status)
})

onMounted(async () => {
  try {
    const lockInfo = await RemoteService.areSubmissionsLocked()
    submissionsLocked.value = lockInfo.locked
  } catch (e) {
    // ignore
  }
  await loadExam()
  await loadPage(currentPage.value)
  await loadQuestions()
})

const loadExam = async () => {
  try {
    exam.value = await RemoteService.getExam(examId)
  } catch (err) {
    console.error('Error fetching exam:', err)
  }
}

const loadPage = async (page: number) => {
  loadingPage.value = true
  clearSelection()
  try {
    currentPage.value = page
    pageImageUrl.value = await RemoteService.getExamPageImageBlob(examId, page)
  } catch (err) {
    console.error('Error fetching page image:', err)
  } finally {
    loadingPage.value = false
  }
}

const changePage = (page: number) => {
  if (page >= 1 && exam.value && page <= exam.value.totalPages) {
    loadPage(page)
  }
}

const onImageLoaded = () => {
  clearSelection()
}

const selectFullPage = () => {
  selectionMode.value = 'full'
  clearSelection()
}

const clearSelection = () => {
  Object.assign(selectionRect, {
    active: false,
    displayX: 0,
    displayY: 0,
    displayWidth: 0,
    displayHeight: 0,
    normX: 0,
    normY: 0,
    normWidth: 0,
    normHeight: 0
  })
}

const onMouseDown = (e: MouseEvent) => {
  if (selectionMode.value !== 'crop' || !viewerContainer.value || !pageImage.value) return

  const imageRect = pageImage.value.getBoundingClientRect()
  if (e.clientX < imageRect.left || e.clientX > imageRect.right || e.clientY < imageRect.top || e.clientY > imageRect.bottom) {
    return
  }

  isDragging.value = true
  startX.value = e.clientX - imageRect.left
  startY.value = e.clientY - imageRect.top

  selectionRect.active = true
  selectionRect.displayX = startX.value
  selectionRect.displayY = startY.value
  selectionRect.displayWidth = 0
  selectionRect.displayHeight = 0
}

const onMouseMove = (e: MouseEvent) => {
  if (!isDragging.value || !pageImage.value) return

  const imageRect = pageImage.value.getBoundingClientRect()

  let currentX = Math.max(0, Math.min(e.clientX - imageRect.left, imageRect.width))
  let currentY = Math.max(0, Math.min(e.clientY - imageRect.top, imageRect.height))

  const x = Math.min(startX.value, currentX)
  const y = Math.min(startY.value, currentY)
  const w = Math.abs(currentX - startX.value)
  const h = Math.abs(currentY - startY.value)

  selectionRect.displayX = x
  selectionRect.displayY = y
  selectionRect.displayWidth = w
  selectionRect.displayHeight = h

  // Calculate normalized coordinates (0.0 to 1.0)
  selectionRect.normX = x / imageRect.width
  selectionRect.normY = y / imageRect.height
  selectionRect.normWidth = w / imageRect.width
  selectionRect.normHeight = h / imageRect.height
}

const onMouseUp = () => {
  if (isDragging.value) {
    isDragging.value = false
    if (selectionRect.displayWidth < 10 || selectionRect.displayHeight < 10) {
      clearSelection()
    }
  }
}

const computeNextQuestionNumber = (current: string): string => {
  if (!current) return '1'
  const normalized = current.trim().replace(',', '.')
  const num = parseFloat(normalized)
  if (!isNaN(num)) {
    return String(Math.floor(num) + 1)
  }
  return String(examQuestions.value.length + 1)
}

const loadQuestions = async (updateNumber = true) => {
  try {
    const list = await RemoteService.getExamQuestions(examId)
    examQuestions.value = list
    for (const q of list) {
      if (!questionImageUrls[q.id]) {
        RemoteService.getQuestionImageBlob(q.id).then(url => {
          questionImageUrls[q.id] = url
        }).catch(console.error)
      }
    }
    if (updateNumber) {
      if (list.length > 0) {
        const lastQ = list[list.length - 1]
        questionNumber.value = computeNextQuestionNumber(lastQ.questionNumber)
      } else {
        questionNumber.value = '1'
      }
    }
  } catch (err) {
    console.error('Error fetching questions:', err)
  }
}

const snackbar = ref(false)
const snackbarText = ref('')
const snackbarColor = ref('success')

const showToast = (text: string, color = 'success') => {
  snackbarText.value = text
  snackbarColor.value = color
  snackbar.value = true
}

const addQuestionFragment = async () => {
  if (questionForm.value) {
    const { valid } = await questionForm.value.validate()
    if (!valid) return
  }

  // If in crop mode but user forgot to draw a box, prompt them
  if (selectionMode.value === 'crop' && !hasSelection.value) {
    showToast('Por favor desenhe uma caixa retangular com o rato sobre a folha para delimitar a resposta, ou mude para o modo "Página Toda".', 'warning')
    return
  }

  const isCrop = selectionMode.value === 'crop' && hasSelection.value
  const currentItem = questionNumber.value.trim()

  const req: QuestionCropRequest = {
    questionNumber: currentItem,
    maxScore: Number(questionScore.value),
    pageNumber: currentPage.value,
    cropX: isCrop ? selectionRect.normX : null,
    cropY: isCrop ? selectionRect.normY : null,
    cropWidth: isCrop ? selectionRect.normWidth : null,
    cropHeight: isCrop ? selectionRect.normHeight : null,
    disciplineId: exam.value?.disciplineId ?? null
  }

  addingQuestion.value = true
  try {
    await RemoteService.addQuestion(examId, req)
    await loadQuestions(false)
    await loadExam()
    clearSelection()
    // Increment to next question number (next integer if decimal, e.g. 1.5 -> 2, 1 -> 2)
    questionNumber.value = computeNextQuestionNumber(currentItem)
    if (remainingScore.value > 0) {
      questionScore.value = Math.min(2.5, remainingScore.value)
    } else {
      questionScore.value = 0
    }
    showToast(`Item ${currentItem} adicionado (${req.maxScore} val.)! Total acumulado: ${totalCalculatedScore.value.toFixed(1)}/20.0 val.`, 'success')
  } catch (err: any) {
    console.error('Error adding question:', err)
    const msg = err.response?.data?.message || err.message || 'Erro ao adicionar pergunta.'
    showToast(msg, 'error')
  } finally {
    addingQuestion.value = false
  }
}

const deleteQuestion = async (questionId: number) => {
  try {
    await RemoteService.deleteQuestion(examId, questionId)
    delete questionImageUrls[questionId]
    await loadQuestions()
    await loadExam()
    showToast('Pergunta eliminada com sucesso.', 'info')
  } catch (err: any) {
    console.error('Error deleting question:', err)
    const msg = err.response?.data?.message || err.message || 'Erro ao eliminar pergunta.'
    showToast(msg, 'error')
  }
}

const previewQuestion = (q: QuestionDto) => {
  activePreviewQuestion.value = q
  previewDialog.value = true
}

const completeSegmentation = async () => {
  completing.value = true
  try {
    const updated = await RemoteService.completeExamSegmentation(examId)
    exam.value = updated
    showToast('Segmentação concluída com sucesso (20.0 valores)! O exame está agora reconhecido como segmentado.', 'success')
    setTimeout(() => {
      router.push('/exams')
    }, 1200)
  } catch (err: any) {
    console.error('Error completing segmentation:', err)
    const msg = err.response?.data?.message || err.message || 'Erro ao concluir a segmentação.'
    showToast(msg, 'error')
  } finally {
    completing.value = false
  }
}
</script>

<style scoped>
.pdf-viewer-wrapper {
  position: relative;
  min-height: 520px;
  background-color: #0b0f19;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 20px;
  user-select: none;
  border: 1px solid rgba(100, 116, 139, 0.2);
}

.crosshair-cursor {
  cursor: crosshair;
}

.image-relative-container {
  position: relative;
  display: inline-block;
  max-width: 100%;
}

.pdf-page-image {
  max-width: 100%;
  height: auto;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5);
  border-radius: 6px;
  display: block;
}

.page-loading-placeholder {
  min-height: 420px;
}

.full-page-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border: 3px solid #10b981;
  background-color: rgba(16, 185, 129, 0.18);
  pointer-events: none;
  border-radius: 6px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 14px;
  z-index: 10;
}

.full-page-badge {
  background-color: #10b981;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  padding: 4px 14px;
  border-radius: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
}

.bounding-box-overlay {
  position: absolute;
  border: 2px dashed #3b82f6;
  background-color: rgba(59, 130, 246, 0.25);
  pointer-events: none;
  border-radius: 4px;
  z-index: 10;
}

.bounding-box-badge {
  position: absolute;
  top: -26px;
  left: 0;
  background-color: #1d4ed8;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 6px;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.mode-card {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 12px;
}

.mode-card:hover {
  transform: translateY(-2px);
}

.mode-card-active {
  border-width: 2px;
}

.max-questions-list {
  max-height: 440px;
  overflow-y: auto;
}

.question-item-card {
  transition: all 0.2s ease;
  border-radius: 10px;
  background-color: rgb(var(--v-theme-surface));
}

.question-item-card:hover {
  border-color: rgb(var(--v-theme-primary));
}

.fragment-thumb-wrapper {
  width: 72px;
  height: 52px;
  background-color: #0b0f19;
  border-radius: 6px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border: 1px solid rgba(100, 116, 139, 0.2);
  transition: transform 0.2s ease;
}

.fragment-thumb-wrapper:hover {
  transform: scale(1.05);
}

.fragment-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
