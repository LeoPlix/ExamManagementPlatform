<template>
  <v-container fluid class="pa-0">
    <!-- Header -->
    <v-card variant="flat" class="pa-4 mb-4 rounded-xl border bg-surface">
      <v-row align="center" justify="space-between">
        <v-col cols="12" md="8" class="text-left">
          <div class="d-flex align-center gap-2 mb-1">
            <v-icon icon="mdi-chart-areaspline" color="primary" size="28" class="mr-1"></v-icon>
            <h2 class="text-h6 font-weight-bold text-left mb-0">Dashboard Estatístico Nacional</h2>
          </div>
          <p class="text-caption text-medium-emphasis mb-0">
            Métricas e indicadores em tempo real sobre a execução dos exames nacionais, progresso de avaliação e pedidos de revisão.
          </p>
        </v-col>

        <v-col cols="12" md="4" class="text-md-right">
          <v-btn
            color="primary"
            variant="tonal"
            prepend-icon="mdi-refresh"
            class="text-none font-weight-bold"
            :loading="loading"
            @click="loadStatistics"
          >
            Atualizar Dados
          </v-btn>
        </v-col>
      </v-row>
    </v-card>

    <div v-if="loading" class="pa-12 text-center">
      <v-progress-circular indeterminate color="primary" size="48"></v-progress-circular>
      <p class="mt-4 text-medium-emphasis">A carregar métricas estatísticas...</p>
    </div>

    <div v-else-if="stats">
      <!-- KPI Metric Cards -->
      <v-row class="mb-4" dense>
        <v-col cols="12" sm="6" md="3">
          <v-card variant="outlined" class="pa-4 h-100 rounded-xl bg-surface border card-hover-lift">
            <div class="d-flex align-center justify-space-between mb-2">
              <span class="text-caption text-medium-emphasis text-uppercase font-weight-bold">Total de Exames</span>
              <v-avatar color="primary" variant="tonal" size="38">
                <v-icon icon="mdi-file-document-multiple" size="20"></v-icon>
              </v-avatar>
            </div>
            <div class="text-h4 font-weight-extrabold text-primary">{{ stats.totalExams }}</div>
            <div class="text-caption text-medium-emphasis mt-1">
              <strong>{{ stats.totalStudents }}</strong> alunos inscritos
            </div>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="3">
          <v-card variant="outlined" class="pa-4 h-100 rounded-xl bg-surface border card-hover-lift">
            <div class="d-flex align-center justify-space-between mb-2">
              <span class="text-caption text-medium-emphasis text-uppercase font-weight-bold">Progresso de Correção</span>
              <v-avatar color="teal" variant="tonal" size="38">
                <v-icon icon="mdi-progress-check" size="20"></v-icon>
              </v-avatar>
            </div>
            <div class="text-h4 font-weight-extrabold text-teal">{{ stats.correctionRate.toFixed(0) }}%</div>
            <v-progress-linear
              :model-value="stats.correctionRate"
              color="teal"
              height="6"
              rounded
              class="mt-2"
            ></v-progress-linear>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="3">
          <v-card variant="outlined" class="pa-4 h-100 rounded-xl bg-surface border card-hover-lift">
            <div class="d-flex align-center justify-space-between mb-2">
              <span class="text-caption text-medium-emphasis text-uppercase font-weight-bold">Média Global</span>
              <v-avatar color="indigo" variant="tonal" size="38">
                <v-icon icon="mdi-school" size="20"></v-icon>
              </v-avatar>
            </div>
            <div class="text-h4 font-weight-extrabold text-indigo">
              {{ stats.globalAverage.toFixed(1) }} <span class="text-body-2 text-medium-emphasis">val.</span>
            </div>
            <div class="text-caption text-medium-emphasis mt-1">
              <strong>{{ stats.countCorrected + stats.countReleased }}</strong> provas avaliadas
            </div>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="3">
          <v-card variant="outlined" class="pa-4 h-100 rounded-xl bg-surface border card-hover-lift">
            <div class="d-flex align-center justify-space-between mb-2">
              <span class="text-caption text-medium-emphasis text-uppercase font-weight-bold">Pedidos de Revisão</span>
              <v-avatar color="purple" variant="tonal" size="38">
                <v-icon icon="mdi-scale-balance" size="20"></v-icon>
              </v-avatar>
            </div>
            <div class="text-h4 font-weight-extrabold text-purple">{{ stats.totalReviews }}</div>
            <div class="text-caption text-medium-emphasis mt-1">
              <strong>{{ stats.resolvedReviews }}</strong> revistos ({{ stats.reviewResolutionRate.toFixed(0) }}%)
            </div>
          </v-card>
        </v-col>
      </v-row>

      <!-- Workflow Pipeline Stages -->
      <v-card variant="outlined" class="pa-5 mb-4 rounded-xl text-left bg-surface border">
        <h3 class="text-subtitle-1 font-weight-bold mb-4 d-flex align-center">
          <v-icon icon="mdi-timeline-outline" color="primary" class="mr-2"></v-icon>
          Distribuição dos Exames por Estado do Processo
        </h3>

        <v-row dense>
          <v-col cols="12" sm="6" md="3">
            <div class="pa-4 rounded-xl bg-surface-variant border">
              <div class="text-caption font-weight-bold text-orange">Digitalização & Separação</div>
              <div class="text-h5 font-weight-extrabold mt-1">{{ stats.countUploaded + stats.countSegmented }}</div>
              <div class="text-caption text-medium-emphasis">Aguardando recorte / pronto</div>
            </div>
          </v-col>

          <v-col cols="12" sm="6" md="3">
            <div class="pa-4 rounded-xl bg-surface-variant border">
              <div class="text-caption font-weight-bold text-indigo">Em Avaliação Atómica</div>
              <div class="text-h5 font-weight-extrabold mt-1 text-indigo">{{ stats.countInDistribution + stats.countDistributed }}</div>
              <div class="text-caption text-medium-emphasis">Correção distribuída</div>
            </div>
          </v-col>

          <v-col cols="12" sm="6" md="3">
            <div class="pa-4 rounded-xl bg-surface-variant border">
              <div class="text-caption font-weight-bold text-purple">Totalmente Corrigidos</div>
              <div class="text-h5 font-weight-extrabold mt-1 text-purple">{{ stats.countCorrected }}</div>
              <div class="text-caption text-medium-emphasis">Pronto p/ Disponibilização</div>
            </div>
          </v-col>

          <v-col cols="12" sm="6" md="3">
            <div class="pa-4 rounded-xl bg-surface-variant border">
              <div class="text-caption font-weight-bold text-success">Disponibilizados</div>
              <div class="text-h5 font-weight-extrabold mt-1 text-success">{{ stats.countReleased }}</div>
              <div class="text-caption text-medium-emphasis">Consulta & Revisão</div>
            </div>
          </v-col>
        </v-row>
      </v-card>

      <!-- Discipline Breakdown -->
      <v-card variant="outlined" class="pa-5 mb-4 rounded-xl text-left bg-surface border overflow-hidden">
        <h3 class="text-subtitle-1 font-weight-bold mb-4 d-flex align-center">
          <v-icon icon="mdi-book-education-outline" color="teal" class="mr-2"></v-icon>
          Desempenho e Classificações por Disciplina
        </h3>

        <v-data-table
          :headers="disciplineTableHeaders"
          :items="stats.disciplineStats"
          item-key="id"
          density="comfortable"
          no-data-text="Sem dados para apresentar."
        >
          <template v-slot:[`item.code`]="{ item }">
            <v-chip size="small" variant="tonal" color="teal" class="font-weight-bold">
              {{ item.code }}
            </v-chip>
          </template>

          <template v-slot:[`item.avgScore`]="{ item }">
            <span v-if="item.countCorrected > 0" class="font-weight-bold text-primary">
              {{ item.avgScore.toFixed(2) }} val.
            </span>
            <span v-else class="text-caption text-medium-emphasis opacity-60">
              Sem dados avaliados
            </span>
          </template>

          <template v-slot:[`item.progress`]="{ item }">
            <div class="d-flex align-center">
              <v-progress-linear
                :model-value="item.totalExams > 0 ? (item.countCorrected / item.totalExams) * 100 : 0"
                color="teal"
                height="8"
                rounded
                class="mr-2"
                style="max-width: 120px;"
              ></v-progress-linear>
              <span class="text-caption font-weight-bold">
                {{ item.countCorrected }} / {{ item.totalExams }}
              </span>
            </div>
          </template>
        </v-data-table>
      </v-card>
    </div>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type StatisticsDto from '@/models/StatisticsDto'
import RemoteService from '@/services/RemoteService'

const loading = ref(true)
const stats = ref<StatisticsDto | null>(null)

const disciplineTableHeaders = [
  { title: 'Disciplina', key: 'name', sortable: true },
  { title: 'Código', key: 'code', sortable: true },
  { title: 'Total Provas', key: 'totalExams', sortable: true },
  { title: 'Progresso', key: 'progress', sortable: false },
  { title: 'Classificação Média', key: 'avgScore', sortable: true }
]

onMounted(async () => {
  await loadStatistics()
})

const loadStatistics = async () => {
  loading.value = true
  try {
    stats.value = await RemoteService.getStatistics()
  } catch (err) {
    console.error('Error loading statistics:', err)
  } finally {
    loading.value = false
  }
}
</script>