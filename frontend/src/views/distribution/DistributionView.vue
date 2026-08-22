<template>
  <v-container fluid class="pa-0">
    <!-- Header Section -->
    <v-card variant="flat" class="pa-4 mb-4 rounded-xl border bg-surface">
      <v-row align="center" justify="space-between">
        <v-col cols="12" md="8" class="text-left">
          <div class="d-flex align-center gap-2 mb-1">
            <v-icon icon="mdi-share-variant-outline" color="primary" size="28" class="mr-1"></v-icon>
            <h2 class="text-h6 font-weight-bold text-left mb-0">Distribuição Global de Provas & Tarefas</h2>
          </div>
          <p class="text-caption text-medium-emphasis mb-0">
            Distribuição global, cega e equilibrada de todas as tarefas de correção pelos professores avaliadores de cada disciplina.
          </p>
        </v-col>

        <v-col cols="12" md="4" class="text-md-right">
          <v-btn
            color="primary"
            variant="tonal"
            prepend-icon="mdi-refresh"
            class="text-none font-weight-bold"
            :loading="loading"
            @click="loadData"
          >
            Atualizar Dados
          </v-btn>
        </v-col>
      </v-row>
    </v-card>

    <!-- Alert: Unsegmented Exams Warning -->
    <v-alert
      v-if="totalUploadedExams > 0"
      type="warning"
      variant="tonal"
      density="comfortable"
      class="mb-4 rounded-xl text-left"
      icon="mdi-alert-circle-outline"
      title="Separação de Itens Pendente"
    >
      Existem <strong>{{ totalUploadedExams }} exame(s) digitalizado(s) pendente(s) de segmentação</strong> por itens. Todos os exames carregados no sistema têm de estar completamente segmentados em perguntas (totalizando 20.0 val.) antes de poder iniciar a distribuição global aos professores.
    </v-alert>

    <!-- Hero Card: Global Distribution -->
    <v-card variant="outlined" class="pa-6 mb-4 rounded-xl bg-surface border card-hover-lift">
      <div class="d-flex align-center justify-space-between flex-wrap gap-3 mb-4">
        <div class="d-flex align-center">
          <v-avatar color="primary" variant="tonal" size="52" class="mr-4">
            <v-icon icon="mdi-share-variant" size="28"></v-icon>
          </v-avatar>
          <div class="text-left">
            <h3 class="text-h6 font-weight-bold">Distribuição Global de Tarefas</h3>
            <p class="text-caption text-medium-emphasis mb-0">
              Distribuição simultânea de todas as perguntas segmentadas pelos professores de todas as disciplinas registadas.
            </p>
          </div>
        </div>

        <v-tooltip
          :text="totalUploadedExams > 0 ? `Existem ${totalUploadedExams} exame(s) pendente(s) de segmentação. Todos os exames devem estar segmentados antes da distribuição global.` : (totalSegmentedExams === 0 ? 'Não existem provas segmentadas prontas para distribuir.' : `Inicia a distribuição de todas as ${totalSegmentedExams} prova(s) pelos respetivos professores.`)"
          location="bottom"
        >
          <template v-slot:activator="{ props }">
            <span v-bind="props">
              <v-btn
                color="primary"
                size="large"
                prepend-icon="mdi-share-variant"
                class="text-none font-weight-bold px-6 py-3 rounded-xl"
                elevation="2"
                :loading="distributingActive"
                :disabled="totalSegmentedExams === 0 || totalUploadedExams > 0"
                @click="confirmDialog = true"
              >
                Distribuir Todas as Provas ({{ totalSegmentedExams }})
              </v-btn>
            </span>
          </template>
        </v-tooltip>
      </div>

      <!-- Metrics Cards -->
      <v-row dense class="mt-2">
        <v-col cols="12" sm="4">
          <v-card variant="tonal" color="primary" class="pa-4 text-left rounded-xl fill-height">
            <div class="d-flex align-center justify-space-between">
              <span class="text-caption font-weight-bold text-uppercase">Provas Prontas p/ Distribuição</span>
              <v-icon icon="mdi-file-document-check-outline" size="20"></v-icon>
            </div>
            <div class="text-h4 font-weight-extrabold mt-2">{{ totalSegmentedExams }}</div>
            <div class="text-caption opacity-80 mt-1">Exames segmentados (20.0 val.)</div>
          </v-card>
        </v-col>

        <v-col cols="12" sm="4">
          <v-card variant="tonal" color="teal" class="pa-4 text-left rounded-xl fill-height">
            <div class="d-flex align-center justify-space-between">
              <span class="text-caption font-weight-bold text-uppercase">Professores Avaliadores</span>
              <v-icon icon="mdi-account-tie-outline" size="20"></v-icon>
            </div>
            <div class="text-h4 font-weight-extrabold mt-2 text-teal">{{ totalTeachersCount }}</div>
            <div class="text-caption opacity-80 mt-1">Docentes ativos no sistema</div>
          </v-card>
        </v-col>

        <v-col cols="12" sm="4">
          <v-card variant="tonal" color="indigo" class="pa-4 text-left rounded-xl fill-height">
            <div class="d-flex align-center justify-space-between">
              <span class="text-caption font-weight-bold text-uppercase">Disciplinas c/ Provas</span>
              <v-icon icon="mdi-book-education-outline" size="20"></v-icon>
            </div>
            <div class="text-h4 font-weight-extrabold mt-2 text-indigo">{{ activeDisciplinesCount }}</div>
            <div class="text-caption opacity-80 mt-1">Matrizes curriculares ativas</div>
          </v-card>
        </v-col>
      </v-row>
    </v-card>

    <!-- Overview by Discipline (Status & Readiness) -->
    <v-card variant="outlined" class="rounded-xl border bg-surface overflow-hidden">
      <div class="pa-4 border-b">
        <h3 class="text-subtitle-1 font-weight-bold d-flex align-center mb-0 text-left">
          <v-icon icon="mdi-book-education" color="primary" class="mr-2"></v-icon>
          Estado das Disciplinas
        </h3>
      </div>

      <v-data-table
        :headers="disciplineHeaders"
        :items="disciplineRows"
        :loading="loading"
        item-key="id"
        density="comfortable"
        class="text-left"
        no-data-text="Sem disciplinas registadas."
      >
        <template v-slot:[`item.code`]="{ item }">
          <v-chip size="small" variant="tonal" color="teal" class="font-weight-bold">
            {{ item.code }}
          </v-chip>
        </template>

        <template v-slot:[`item.name`]="{ item }">
          <span class="font-weight-bold text-body-2">{{ item.name }}</span>
        </template>

        <template v-slot:[`item.segmentedCount`]="{ item }">
          <span v-if="item.segmentedCount > 0" class="font-weight-bold text-primary">
            {{ item.segmentedCount }} exames
          </span>
          <span v-else class="text-caption text-medium-emphasis opacity-60">
            0 pendentes
          </span>
        </template>

        <template v-slot:[`item.uploadedCount`]="{ item }">
          <span v-if="item.uploadedCount > 0" class="font-weight-bold text-warning">
            {{ item.uploadedCount }} exames
          </span>
          <span v-else class="text-caption text-medium-emphasis opacity-60">
            0
          </span>
        </template>

        <template v-slot:[`item.teachersCount`]="{ item }">
          <span :class="item.teachersCount > 0 ? 'text-body-2 font-weight-medium' : 'text-error font-weight-bold'">
            {{ item.teachersCount }} docentes
          </span>
        </template>

        <template v-slot:[`item.statusBadge`]="{ item }">
          <v-chip
            v-if="item.uploadedCount > 0"
            size="small"
            color="warning"
            variant="tonal"
            class="font-weight-bold"
          >
            {{ item.uploadedCount }} exames p/ segmentar
          </v-chip>
          <v-chip
            v-else-if="item.segmentedCount === 0"
            size="small"
            color="grey"
            variant="tonal"
          >
            Sem Provas Pendentes
          </v-chip>
          <v-chip
            v-else-if="item.teachersCount === 0"
            size="small"
            color="error"
            variant="tonal"
          >
            Sem Professores Atribuídos
          </v-chip>
          <v-chip
            v-else
            size="small"
            color="success"
            variant="flat"
            class="font-weight-bold"
          >
            Pronto p/ Distribuição
          </v-chip>
        </template>
      </v-data-table>
    </v-card>

    <!-- Confirmation Dialog: Global Distribution -->
    <v-dialog v-model="confirmDialog" max-width="500">
      <v-card prepend-icon="mdi-share-variant" title="Confirmar Distribuição Global" class="rounded-xl">
        <v-card-text>
          <p class="mb-2">
            Deseja iniciar a <strong>distribuição global de todas as provas</strong> ({{ totalSegmentedExams }} exame(s) segmentado(s)) pelos respetivos professores avaliadores de todas as disciplinas?
          </p>
          <p class="text-caption text-medium-emphasis mb-0">
            Ao confirmar, todas as tarefas serão distribuídas de forma cega e equilibrada e a edição de exames ficará permanentemente bloqueada.
          </p>
        </v-card-text>
        <v-card-actions class="pa-4 pt-0">
          <v-spacer></v-spacer>
          <v-btn text="Cancelar" variant="plain" class="text-none" @click="confirmDialog = false"></v-btn>
          <v-btn
            color="primary"
            text="Confirmar Distribuição"
            variant="flat"
            class="text-none font-weight-bold"
            :loading="distributingActive"
            @click="executeDistribution"
          ></v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Success / Error Snackbar -->
    <v-snackbar v-model="snackbar" :color="snackbarColor" timeout="5000" location="top">
      {{ snackbarText }}
      <template v-slot:actions>
        <v-btn variant="text" @click="snackbar = false">Fechar</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import type DisciplineDto from '@/models/DisciplineDto'
import type PersonDto from '@/models/PersonDto'
import type StatisticsDto from '@/models/StatisticsDto'
import RemoteService from '@/services/RemoteService'

const loading = ref(true)
const disciplines = ref<DisciplineDto[]>([])
const people = ref<PersonDto[]>([])
const stats = ref<StatisticsDto | null>(null)

const confirmDialog = ref(false)
const distributingActive = ref(false)

const snackbar = ref(false)
const snackbarText = ref('')
const snackbarColor = ref('success')

const disciplineHeaders = [
  { title: 'Código', key: 'code', width: '100px', sortable: true },
  { title: 'Disciplina', key: 'name', sortable: true },
  { title: 'Exames Segmentados', key: 'segmentedCount', sortable: true },
  { title: 'Exames p/ Segmentar', key: 'uploadedCount', sortable: true },
  { title: 'Professores Ativos', key: 'teachersCount', sortable: true },
  { title: 'Estado', key: 'statusBadge', sortable: false }
]

const totalSegmentedExams = computed(() => {
  return stats.value?.countSegmented ?? 0
})

const totalUploadedExams = computed(() => {
  return stats.value?.countUploaded ?? 0
})

const totalTeachersCount = computed(() => {
  return people.value.filter(p => p.type === 'TEACHER').length
})

const activeDisciplinesCount = computed(() => {
  return disciplines.value.length
})

const disciplineRows = computed(() => {
  return disciplines.value.map(d => {
    const discId = d.id ?? 0
    const teachersForDisc = people.value.filter(
      p => p.type === 'TEACHER' && p.disciplineIds && p.disciplineIds.includes(discId)
    )

    // Check stats for segmented and uploaded exams in this discipline
    const discStat = stats.value?.disciplineStats.find(s => s.id === discId)
    const segmented = discStat ? discStat.countSegmented : 0
    const uploaded = discStat ? (discStat.countUploaded ?? 0) : 0

    return {
      id: discId,
      name: d.name,
      code: d.code,
      uploadedCount: uploaded,
      segmentedCount: segmented,
      teachersCount: teachersForDisc.length
    }
  })
})

const loadData = async () => {
  loading.value = true
  try {
    const [fetchedDisciplines, fetchedPeople, fetchedStats] = await Promise.all([
      RemoteService.getDisciplines(),
      RemoteService.getPeople(),
      RemoteService.getStatistics()
    ])
    disciplines.value = fetchedDisciplines
    people.value = fetchedPeople
    stats.value = fetchedStats
  } catch (err) {
    console.error('Error loading distribution data:', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})

const executeDistribution = async () => {
  distributingActive.value = true
  try {
    const res = await RemoteService.distributeExams()
    confirmDialog.value = false
    snackbarText.value = `${res.distributedExamsCount} exame(s) e ${res.distributedQuestionsCount} pergunta(s) distribuídas com sucesso por todos os docentes!`
    snackbarColor.value = 'success'
    snackbar.value = true
    await loadData()
  } catch (err: any) {
    console.error('Error distributing exams:', err)
    const msg = err.response?.data?.message || err.message || 'Erro ao distribuir exames.'
    snackbarText.value = msg
    snackbarColor.value = 'error'
    snackbar.value = true
  } finally {
    distributingActive.value = false
  }
}
</script>
