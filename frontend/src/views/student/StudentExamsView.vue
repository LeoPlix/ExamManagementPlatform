<template>
  <v-container fluid class="pa-0">
    <!-- Header Section -->
    <v-card variant="flat" class="pa-4 mb-4 rounded-xl border bg-surface">
      <v-row align="center" justify="space-between">
        <v-col cols="12" md="8" class="text-left">
          <div class="d-flex align-center gap-2 mb-1">
            <v-icon icon="mdi-file-eye-outline" color="primary" size="28" class="mr-1"></v-icon>
            <h2 class="text-h6 font-weight-bold text-left mb-0">As Minhas Provas e Classificações</h2>
          </div>
          <p class="text-caption text-medium-emphasis mb-0">
            Consulta detalhada das provas corrigidas, visualização das anotações dos corretores e submissão de pedidos de revisão.
          </p>
        </v-col>
      </v-row>
    </v-card>

    <div v-if="loading" class="pa-12 text-center">
      <v-progress-circular indeterminate color="primary" size="48"></v-progress-circular>
      <p class="mt-4 text-medium-emphasis">A carregar exames...</p>
    </div>

    <div v-else-if="exams.length === 0" class="pa-12 text-center text-medium-emphasis">
      <v-icon icon="mdi-file-document-outline" size="64" class="mb-3 opacity-50"></v-icon>
      <h3>Nenhum exame associado à sua conta</h3>
      <p class="text-caption">Aguarde que os funcionários da sua escola digitalizem as suas provas.</p>
    </div>

    <v-row v-else dense class="mb-4">
      <v-col
        v-for="exam in exams"
        :key="exam.id"
        cols="12"
        md="6"
      >
        <v-card variant="outlined" class="pa-5 h-100 d-flex flex-column rounded-xl border bg-surface card-hover-lift">
          <div class="d-flex align-center justify-space-between mb-2">
            <h3 class="text-h6 font-weight-bold">{{ exam.title }}</h3>
            <v-chip
              :color="isExamReleased(exam) ? 'success' : 'orange'"
              size="small"
              variant="flat"
              class="font-weight-bold"
            >
              {{ isExamReleased(exam) ? 'Disponibilizado' : 'Em Avaliação' }}
            </v-chip>
          </div>

          <div class="mb-3">
            <v-chip size="small" variant="tonal" color="teal" class="mr-2 font-weight-medium">
              {{ exam.disciplineName }}
            </v-chip>
            <span class="text-caption text-medium-emphasis font-weight-medium">{{ exam.schoolName }}</span>
          </div>

          <!-- Grade Display -->
          <div v-if="isGradePublished(exam)" class="bg-surface-variant pa-4 rounded-xl mb-3 border">
            <div class="d-flex align-center justify-space-between">
              <div>
                <div class="text-caption text-medium-emphasis">Classificação:</div>
                <div class="text-h4 font-weight-extrabold text-primary">
                  {{ exam.obtainedScore?.toFixed(1) ?? '0.0' }}
                  <span class="text-body-1 text-medium-emphasis">/ {{ exam.totalScore.toFixed(1) }} val.</span>
                </div>
              </div>
              <v-chip v-if="isReviewOpen(exam)" color="purple" variant="tonal" size="small" class="font-weight-bold">
                <v-icon start icon="mdi-clock-outline"></v-icon>
                Revisão Aberta
              </v-chip>
              <v-chip v-else color="grey" variant="tonal" size="small">
                Revisão Fechada
              </v-chip>
            </div>

            <div v-if="exam.reviewDeadline" class="text-caption text-medium-emphasis mt-2">
              Prazo de Revisão: <strong>{{ formatDate(exam.reviewDeadline) }}</strong>
            </div>
          </div>

          <div v-else class="pa-6 text-center bg-surface-variant rounded-xl mb-3 border">
            <v-icon icon="mdi-clock-outline" size="32" color="primary" class="mb-2 opacity-80"></v-icon>
            <p class="text-body-2 font-weight-bold mb-1">Prova em processo de correção</p>
            <p class="text-caption text-medium-emphasis mb-0">As classificações serão publicadas após homologação pela escola.</p>
          </div>

          <v-spacer></v-spacer>

          <!-- 1. When exam is released/authorized for viewing: Direct consultation button -->
          <v-btn
            v-if="isExamReleased(exam)"
            block
            color="primary"
            variant="flat"
            prepend-icon="mdi-file-eye-outline"
            class="text-none font-weight-bold mt-2 py-3 rounded-xl"
            @click="openConsultationModal(exam)"
          >
            Consultar Prova e Classificações
          </v-btn>

          <!-- 2. When grades are published, but exam view hasn't been authorized yet -->
          <div v-else-if="isGradePublished(exam)" class="mt-2">
            <v-chip
              v-if="exam.viewRequested"
              color="indigo"
              variant="tonal"
              size="large"
              class="w-100 justify-center font-weight-bold py-3 rounded-xl"
              prepend-icon="mdi-clock-check-outline"
            >
              Visualização Solicitada (Aguardando Aprovação)
            </v-chip>
            <v-btn
              v-else
              block
              color="primary"
              variant="tonal"
              prepend-icon="mdi-file-eye-outline"
              class="text-none font-weight-bold py-3 rounded-xl"
              :loading="requestingViewId === exam.id"
              @click="requestExamView(exam)"
            >
              Pedir Visualização da Prova
            </v-btn>
          </div>

          <!-- 3. Before grades are published -->
          <div v-else class="mt-2">
            <v-chip
              color="grey"
              variant="tonal"
              size="large"
              class="w-100 justify-center font-weight-medium py-3 rounded-xl"
              prepend-icon="mdi-clock-outline"
            >
              Aguardando Lançamento de Notas
            </v-chip>
          </div>
        </v-card>
      </v-col>
    </v-row>

    <!-- SIDE-BY-SIDE CONSULTATION MODAL (Phase 4 & 5) -->
    <v-dialog v-model="consultationDialog" fullscreen transition="dialog-bottom-transition">
      <v-card class="d-flex flex-column h-100" v-if="activeExam">
        <!-- Top Toolbar -->
        <v-toolbar color="primary" density="compact">
          <v-toolbar-title class="text-subtitle-1 font-weight-bold">
            {{ activeExam.title }} - Consulta de Prova e Cotações
          </v-toolbar-title>
          <v-spacer></v-spacer>
          <v-chip color="white" variant="outlined" size="small" class="mr-4 font-weight-bold">
            Nota Final: {{ activeExam.obtainedScore?.toFixed(1) ?? '0.0' }} / {{ activeExam.totalScore.toFixed(1) }} val.
          </v-chip>
          <v-btn icon="mdi-close" @click="consultationDialog = false"></v-btn>
        </v-toolbar>

        <!-- Main Content Area: Side-by-Side -->
        <div class="flex-grow-1 d-flex overflow-hidden consultation-layout">
          <!-- LEFT PANE: PDF Viewer (55% width) -->
          <div class="pdf-pane flex-grow-1 d-flex flex-column">
            <iframe
              v-if="pdfUrl"
              :src="pdfUrl"
              class="w-100 h-100"
              style="border: none;"
            ></iframe>
            <div v-else class="d-flex align-center justify-center fill-height">
              <v-progress-circular indeterminate color="primary"></v-progress-circular>
            </div>
          </div>

          <!-- RIGHT PANE: Questions & Grade Breakdown (45% width) -->
          <div class="questions-pane pa-4 overflow-y-auto" style="width: 45%; min-width: 380px; background-color: var(--v-theme-surface);">
            <div class="d-flex align-center justify-space-between mb-3">
              <h3 class="text-subtitle-1 font-weight-bold">Classificação por Fragmento</h3>
              <v-chip size="small" color="primary" variant="flat">
                {{ activeExamQuestions.length }} Itens
              </v-chip>
            </div>

            <!-- List of Question Breakdown Cards -->
            <v-card
              v-for="q in activeExamQuestions"
              :key="q.id"
              variant="outlined"
              class="mb-3 pa-3 rounded-lg"
            >
              <div class="d-flex align-center justify-space-between mb-2">
                <div class="d-flex align-center gap-1">
                  <span class="font-weight-bold text-subtitle-2">Item {{ q.questionNumber }} (Pág. {{ q.pageNumber }})</span>
                  <v-chip
                    v-if="q.hasAnnotation"
                    size="x-small"
                    color="purple"
                    variant="tonal"
                    prepend-icon="mdi-draw"
                    title="O professor adicionou correções visuais na imagem"
                  >
                    Anotada
                  </v-chip>
                </div>
                <v-chip
                  :color="(q.score ?? 0) >= q.maxScore * 0.7 ? 'success' : ((q.score ?? 0) >= q.maxScore * 0.4 ? 'orange' : 'error')"
                  size="small"
                  variant="flat"
                  class="font-weight-bold"
                >
                  {{ q.score?.toFixed(1) ?? '0.0' }} / {{ q.maxScore.toFixed(1) }} val.
                </v-chip>
              </div>

              <!-- Question Fragment Thumbnail (Annotated / Original) -->
              <div v-if="questionThumbnails[q.id]" class="mb-2 fragment-preview-box" @click="openImageModal(q.hasAnnotation && !showOriginalImage[q.id] ? (annotatedThumbnails[q.id] || questionThumbnails[q.id]) : (originalThumbnails[q.id] || questionThumbnails[q.id]), q.questionNumber)">
                <img
                  :src="q.hasAnnotation && !showOriginalImage[q.id] ? (annotatedThumbnails[q.id] || questionThumbnails[q.id]) : (originalThumbnails[q.id] || questionThumbnails[q.id])"
                  alt="Recorte do Item"
                  class="fragment-img"
                />
                <div class="zoom-badge">
                  <v-icon icon="mdi-magnify-plus" size="14"></v-icon> Ampliar
                </div>
              </div>

              <!-- Toggle between annotated vs original image -->
              <div v-if="q.hasAnnotation" class="d-flex align-center justify-end mb-2">
                <v-btn
                  size="x-small"
                  variant="tonal"
                  color="purple"
                  :prepend-icon="showOriginalImage[q.id] ? 'mdi-draw' : 'mdi-image-outline'"
                  class="text-none"
                  @click.stop="toggleOriginal(q.id)"
                >
                  {{ showOriginalImage[q.id] ? 'Ver Correções do Professor' : 'Ver Imagem Original' }}
                </v-btn>
              </div>

              <div v-if="q.feedback" class="bg-surface-variant pa-2 rounded text-caption mb-2">
                <strong>Observações do Corretor:</strong> "{{ q.feedback }}"
              </div>

              <!-- Review Status / Request Button -->
              <div class="d-flex align-center justify-space-between mt-2 pt-2 border-t">
                <div v-if="getQuestionReview(q.id)">
                  <v-chip
                    :color="getQuestionReview(q.id)?.status === 'RESOLVED' ? 'success' : 'purple'"
                    size="small"
                    variant="tonal"
                  >
                    <v-icon start :icon="getQuestionReview(q.id)?.status === 'RESOLVED' ? 'mdi-check-all' : 'mdi-clock-outline'"></v-icon>
                    {{ getQuestionReview(q.id)?.status === 'RESOLVED' ? 'Revisão Concluída' : 'Revisão Pedida' }}
                  </v-chip>
                  <div v-if="getQuestionReview(q.id)?.reviewerFeedback" class="text-caption text-purple font-weight-medium mt-1">
                    Decisão: "{{ getQuestionReview(q.id)?.reviewerFeedback }}"
                  </div>
                </div>

                <v-btn
                  v-else-if="isReviewOpen(activeExam)"
                  size="small"
                  color="purple"
                  variant="outlined"
                  prepend-icon="mdi-pencil-box-outline"
                  class="text-none font-weight-bold"
                  @click="openReviewModal(q)"
                >
                  Pedir Revisão
                </v-btn>
              </div>
            </v-card>
          </div>
        </div>
      </v-card>
    </v-dialog>

    <!-- SUBMIT REVIEW MODAL (Phase 5) -->
    <v-dialog v-model="reviewDialog" max-width="600">
      <v-card v-if="selectedQuestionForReview">
        <v-toolbar color="purple" density="compact">
          <v-toolbar-title class="text-subtitle-1 font-weight-bold text-white">
            Pedido de Revisão: Item {{ selectedQuestionForReview.questionNumber }}
          </v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn icon="mdi-close" color="white" @click="reviewDialog = false"></v-btn>
        </v-toolbar>

        <v-card-text class="pa-4">
          <div class="bg-purple-lighten-5 pa-3 rounded-lg mb-3">
            <div class="text-caption text-medium-emphasis">Classificação Atual:</div>
            <div class="text-subtitle-1 font-weight-bold text-purple">
              {{ selectedQuestionForReview.score?.toFixed(1) ?? '0.0' }} / {{ selectedQuestionForReview.maxScore.toFixed(1) }} val.
            </div>
          </div>

          <v-form ref="reviewForm" v-model="reviewFormValid">
            <v-textarea
              v-model="reviewJustification"
              label="Fundamentação do Pedido de Revisão*"
              placeholder="Indique detalhadamente as razões pelas quais considera que a resposta merece pontuação superior..."
              rows="4"
              variant="outlined"
              density="comfortable"
              :rules="[v => (!!v && v.trim().length >= 10) || 'Indique uma justificação com pelo menos 10 caracteres']"
            ></v-textarea>
          </v-form>

          <v-alert type="warning" variant="tonal" density="compact" class="text-caption mt-2">
            A prova será reavaliada de forma independente por um professor da disciplina após o fecho do prazo de revisão.
          </v-alert>
        </v-card-text>

        <v-card-actions class="pa-4 pt-0">
          <v-spacer></v-spacer>
          <v-btn text="Cancelar" variant="plain" @click="reviewDialog = false"></v-btn>
          <v-btn
            color="purple"
            variant="flat"
            prepend-icon="mdi-send"
            class="text-none font-weight-bold"
            :loading="submittingReview"
            @click="submitReviewRequest"
          >
            Submeter Pedido
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Image Zoom Modal -->
    <v-dialog v-model="zoomDialog" max-width="900">
      <v-card>
        <v-toolbar color="primary" density="compact">
          <v-toolbar-title class="text-subtitle-1 font-weight-bold text-white">Item {{ zoomTitle }} - Consulta Ampliada</v-toolbar-title>
          <div class="d-flex align-center gap-1 mr-2">
            <v-btn
              icon="mdi-magnify-minus-outline"
              size="small"
              variant="text"
              color="white"
              title="Diminuir Zoom"
              :disabled="studentZoomLevel <= 0.6"
              @click="studentZoomLevel = Math.max(0.6, studentZoomLevel - 0.2)"
            ></v-btn>
            <span class="text-caption font-weight-bold text-white" style="min-width: 42px; text-align: center;">
              {{ Math.round(studentZoomLevel * 100) }}%
            </span>
            <v-btn
              icon="mdi-magnify-plus-outline"
              size="small"
              variant="text"
              color="white"
              title="Aumentar Zoom"
              :disabled="studentZoomLevel >= 3.0"
              @click="studentZoomLevel = Math.min(3.0, studentZoomLevel + 0.2)"
            ></v-btn>
            <v-btn
              icon="mdi-fit-to-screen-outline"
              size="small"
              variant="text"
              color="white"
              title="Repor Zoom (100%)"
              @click="studentZoomLevel = 1.0"
            ></v-btn>
          </div>
          <v-btn icon="mdi-close" color="white" @click="zoomDialog = false"></v-btn>
        </v-toolbar>
        <v-card-text class="pa-4 text-center bg-grey-darken-4 overflow-auto" style="max-height: 75vh;">
          <div :style="{ transform: `scale(${studentZoomLevel})`, transformOrigin: 'top center', transition: 'transform 0.12s ease' }">
            <img :src="zoomImageUrl || undefined" style="max-width: 100%; object-fit: contain;" alt="Zoom" />
          </div>
        </v-card-text>
      </v-card>
    </v-dialog>

    <!-- Feedback Snackbar -->
    <v-snackbar v-model="snackbar" :color="snackbarColor" timeout="4000" location="top">
      {{ snackbarText }}
      <template v-slot:actions>
        <v-btn color="white" variant="text" @click="snackbar = false">Fechar</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type ExamDto from '@/models/ExamDto'
import type QuestionDto from '@/models/QuestionDto'
import type ReviewRequestDto from '@/models/ReviewRequestDto'
import RemoteService from '@/services/RemoteService'

const loading = ref(true)
const exams = ref<ExamDto[]>([])
const myReviews = ref<ReviewRequestDto[]>([])

const consultationDialog = ref(false)
const activeExam = ref<ExamDto | null>(null)
const activeExamQuestions = ref<QuestionDto[]>([])
const pdfUrl = ref<string | null>(null)
const questionThumbnails = reactive<Record<number, string>>({})
const annotatedThumbnails = reactive<Record<number, string>>({})
const originalThumbnails = reactive<Record<number, string>>({})
const showOriginalImage = reactive<Record<number, boolean>>({})

const reviewDialog = ref(false)
const selectedQuestionForReview = ref<QuestionDto | null>(null)
const reviewJustification = ref('')
const reviewForm = ref<any>(null)
const reviewFormValid = ref(false)
const submittingReview = ref(false)

const zoomDialog = ref(false)
const zoomImageUrl = ref<string | null>(null)
const zoomTitle = ref('')
const studentZoomLevel = ref(1.0)

onMounted(async () => {
  await Promise.all([loadExams(), loadReviews()])
})

const loadExams = async () => {
  loading.value = true
  try {
    const list = await RemoteService.getExams()
    exams.value = list
  } catch (err) {
    console.error('Error fetching student exams:', err)
  } finally {
    loading.value = false
  }
}

const loadReviews = async () => {
  try {
    const list = await RemoteService.getStudentReviews()
    myReviews.value = list
  } catch (err) {
    console.error('Error fetching reviews:', err)
  }
}

const isGradePublished = (exam: ExamDto) => {
  return !!exam.gradesPublished || !!exam.released || exam.status === 'RELEASED'
}

const isExamReleased = (exam: ExamDto) => {
  return !!exam.released || exam.status === 'RELEASED'
}

const isReviewOpen = (exam: ExamDto | null) => {
  if (!exam || !isGradePublished(exam)) return false
  if (!exam.reviewDeadline) return true
  return new Date(exam.reviewDeadline).getTime() > Date.now()
}

const formatDate = (dateStr: string) => {
  try {
    const d = new Date(dateStr)
    return d.toLocaleString('pt-PT', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
  } catch {
    return dateStr
  }
}

const getQuestionReview = (questionId: number): ReviewRequestDto | undefined => {
  return myReviews.value.find(r => r.questionId === questionId)
}

const toggleOriginal = (questionId: number) => {
  showOriginalImage[questionId] = !showOriginalImage[questionId]
}

const openConsultationModal = async (exam: ExamDto) => {
  activeExam.value = exam
  consultationDialog.value = true
  try {
    pdfUrl.value = await RemoteService.getExamPdfBlob(exam.id)
    const questions = await RemoteService.getExamQuestions(exam.id)
    activeExamQuestions.value = questions

    for (const q of questions) {
      // Load original image
      if (!originalThumbnails[q.id]) {
        RemoteService.getQuestionImageBlob(q.id).then(url => {
          originalThumbnails[q.id] = url
          if (!questionThumbnails[q.id]) {
            questionThumbnails[q.id] = url
          }
        }).catch(console.error)
      }

      // Load annotated image if question has annotations
      if (q.hasAnnotation && !annotatedThumbnails[q.id]) {
        RemoteService.getQuestionAnnotatedImageBlob(q.id).then(url => {
          annotatedThumbnails[q.id] = url
        }).catch(console.error)
      }
    }
  } catch (err) {
    console.error('Error loading consultation details:', err)
  }
}

const openReviewModal = (q: QuestionDto) => {
  selectedQuestionForReview.value = q
  reviewJustification.value = ''
  reviewDialog.value = true
}

const submitReviewRequest = async () => {
  if (reviewForm.value) {
    const { valid } = await reviewForm.value.validate()
    if (!valid) return
  }
  if (!activeExam.value || !selectedQuestionForReview.value) return

  submittingReview.value = true
  try {
    await RemoteService.createReviewRequest(activeExam.value.id, {
      questionId: selectedQuestionForReview.value.id,
      justification: reviewJustification.value.trim()
    })
    reviewDialog.value = false
    await loadReviews()
  } catch (err) {
    console.error('Error submitting review request:', err)
  } finally {
    submittingReview.value = false
  }
}

const requestingViewId = ref<number | null>(null)
const snackbar = ref(false)
const snackbarText = ref('')
const snackbarColor = ref('success')

const requestExamView = async (exam: ExamDto) => {
  requestingViewId.value = exam.id
  try {
    await RemoteService.requestExamView(exam.id)
    exam.viewRequested = true
    snackbarText.value = `Pedido de visualização submetido com sucesso para a prova "${exam.title}"!`
    snackbarColor.value = 'success'
    snackbar.value = true
  } catch (err: any) {
    console.error('Error requesting exam view:', err)
    snackbarText.value = 'Ocorreu um erro ao submeter o pedido de visualização.'
    snackbarColor.value = 'error'
    snackbar.value = true
  } finally {
    requestingViewId.value = null
  }
}

const openImageModal = (url: string, title: string) => {
  zoomImageUrl.value = url
  zoomTitle.value = title
  studentZoomLevel.value = 1.0
  zoomDialog.value = true
}
</script>

<style scoped>
.consultation-layout {
  height: calc(100vh - 48px);
}

.pdf-pane {
  background-color: #0b0f19;
}

.questions-pane {
  border-left: 1px solid rgba(100, 116, 139, 0.2);
}

.fragment-preview-box {
  height: 100px;
  background-color: #0b0f19;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(100, 116, 139, 0.2);
  transition: transform 0.2s ease, border-color 0.2s ease;
}

.fragment-preview-box:hover {
  transform: scale(1.02);
  border-color: rgb(var(--v-theme-primary));
}

.fragment-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.zoom-badge {
  position: absolute;
  bottom: 6px;
  right: 6px;
  background: rgba(15, 23, 42, 0.85);
  backdrop-filter: blur(2px);
  color: white;
  font-size: 11px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 6px;
}
</style>
