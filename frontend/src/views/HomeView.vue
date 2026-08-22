<template>
  <v-container fluid class="pa-0">
    <!-- Welcome Hero Banner -->
    <v-card class="pa-6 pa-md-8 mb-8 text-left rounded-2xl hero-banner border" elevation="2">
      <v-row align="center" justify="space-between">
        <v-col cols="12" md="8">
          <div class="d-flex align-center gap-2 mb-2">
            <v-avatar color="primary" variant="tonal" size="40" class="mr-2 hero-shield">
              <v-icon icon="mdi-shield-check" color="primary" size="24"></v-icon>
            </v-avatar>
            <span class="text-caption text-uppercase font-weight-bold tracking-wider opacity-80">
              Plataforma de Gestão de Exames
            </span>
          </div>
          <h1 class="text-h4 text-md-h3 font-weight-extrabold mb-2 hero-title">
            Bem-vindo(a), {{ auth.user?.name ?? 'Utilizador' }}!
          </h1>
          <p class="text-subtitle-1 opacity-90 mb-0 max-w-xl hero-subtitle">
            Aceda rapidamente às suas tarefas, consulte resultados e acompanhe o processo de avaliação dos exames.
          </p>
        </v-col>

        <v-col cols="12" md="4" class="text-md-right mt-4 mt-md-0">
          <v-chip
            :color="roleColor"
            size="large"
            variant="elevated"
            class="font-weight-bold px-4 py-2"
          >
            <v-icon start icon="mdi-shield-account" class="mr-1"></v-icon>
            {{ roleLabel }}
          </v-chip>
        </v-col>
      </v-row>
    </v-card>

    <!-- Quick Navigation Section -->
    <div class="d-flex align-center justify-space-between mb-4">
      <div>
        <h2 class="text-h6 font-weight-bold text-left mb-1">Acesso Rápido</h2>
        <p class="text-caption text-medium-emphasis text-left mb-0">
          Selecione a área onde deseja trabalhar.
        </p>
      </div>
    </div>

    <v-row class="mb-6" dense>
      <!-- STUDENT CARDS -->
      <v-col cols="12" sm="6" md="4" v-if="auth.hasPermission('REVIEW_REQUEST')">
        <v-card
          to="/my-exams"
          variant="outlined"
          class="pa-5 fill-height d-flex flex-column justify-space-between rounded-xl card-hover-lift border bg-surface"
        >
          <div>
            <div class="d-flex align-center mb-3">
              <v-avatar color="primary" variant="tonal" size="48" class="mr-3">
                <v-icon icon="mdi-file-eye-outline" size="24"></v-icon>
              </v-avatar>
              <div class="text-left">
                <div class="text-subtitle-1 font-weight-bold">As Minhas Provas</div>
                <div class="text-caption text-medium-emphasis">Notas e Revisões</div>
              </div>
            </div>
            <p class="text-body-2 text-medium-emphasis text-left mb-4">
              Consulte as suas provas corrigidas, veja as anotações dos professores e submeta pedidos de revisão.
            </p>
          </div>
          <v-btn block color="primary" variant="flat" class="text-none font-weight-bold">
            <span>Ver Minhas Provas</span>
            <v-icon end icon="mdi-arrow-right" class="ml-1" size="18"></v-icon>
          </v-btn>
        </v-card>
      </v-col>

      <!-- TEACHER CARDS -->
      <v-col cols="12" sm="6" md="4" v-if="auth.hasPermission('EVALUATION_READ')">
        <v-card
          to="/teacher/tasks"
          variant="outlined"
          class="pa-5 fill-height d-flex flex-column justify-space-between rounded-xl card-hover-lift border bg-surface"
        >
          <div>
            <div class="d-flex align-center mb-3">
              <v-avatar color="teal" variant="tonal" size="48" class="mr-3">
                <v-icon icon="mdi-checkbox-marked-circle-outline" size="24"></v-icon>
              </v-avatar>
              <div class="text-left">
                <div class="text-subtitle-1 font-weight-bold">Correção de Provas</div>
                <div class="text-caption text-medium-emphasis">Avaliar Respostas</div>
              </div>
            </div>
            <p class="text-body-2 text-medium-emphasis text-left mb-4">
              Classifique as respostas atribuídas aos seus alunos e analise pedidos de revisão pendentes.
            </p>
          </div>
          <v-btn block color="teal" variant="flat" class="text-none font-weight-bold">
            <span>Corrigir Provas</span>
            <v-icon end icon="mdi-arrow-right" class="ml-1" size="18"></v-icon>
          </v-btn>
        </v-card>
      </v-col>

      <!-- STAFF CARDS -->
      <v-col cols="12" sm="6" md="4" v-if="auth.hasPermission('EXAM_UPLOAD') || auth.hasPermission('EXAM_SEGMENT')">
        <v-card
          to="/exams"
          variant="outlined"
          class="pa-5 fill-height d-flex flex-column justify-space-between rounded-xl card-hover-lift border bg-surface"
        >
          <div>
            <div class="d-flex align-center mb-3">
              <v-avatar color="indigo" variant="tonal" size="48" class="mr-3">
                <v-icon icon="mdi-file-document-multiple" size="24"></v-icon>
              </v-avatar>
              <div class="text-left">
                <div class="text-subtitle-1 font-weight-bold">Exames e Digitalização</div>
                <div class="text-caption text-medium-emphasis">Carregar e Dividir Perguntas</div>
              </div>
            </div>
            <p class="text-body-2 text-medium-emphasis text-left mb-4">
              Envie os PDFs dos exames digitalizados e recorte as perguntas para que os professores as possam corrigir.
            </p>
          </div>
          <v-btn block color="indigo" variant="flat" class="text-none font-weight-bold">
            <span>Gerir Exames</span>
            <v-icon end icon="mdi-arrow-right" class="ml-1" size="18"></v-icon>
          </v-btn>
        </v-card>
      </v-col>

      <v-col cols="12" sm="6" md="4" v-if="auth.hasPermission('GRADES_READ')">
        <v-card
          to="/grades"
          variant="outlined"
          class="pa-5 fill-height d-flex flex-column justify-space-between rounded-xl card-hover-lift border bg-surface"
        >
          <div>
            <div class="d-flex align-center mb-3">
              <v-avatar color="success" variant="tonal" size="48" class="mr-3">
                <v-icon icon="mdi-format-list-numbered" size="24"></v-icon>
              </v-avatar>
              <div class="text-left">
                <div class="text-subtitle-1 font-weight-bold">Pauta de Classificações</div>
                <div class="text-caption text-medium-emphasis">Pauta e Publicação de Notas</div>
              </div>
            </div>
            <p class="text-body-2 text-medium-emphasis text-left mb-4">
              Acompanhe as classificações finais de todos os alunos e publique as notas para ficarem visíveis.
            </p>
          </div>
          <v-btn block color="success" variant="flat" class="text-none font-weight-bold">
            <span>Aceder à Pauta</span>
            <v-icon end icon="mdi-arrow-right" class="ml-1" size="18"></v-icon>
          </v-btn>
        </v-card>
      </v-col>

      <!-- ADMIN GLOBAL DIRECTORY CARDS -->
      <v-col cols="12" sm="6" md="4" v-if="auth.hasPermission('SCHOOL_CREATE')">
        <v-card
          to="/schools"
          variant="outlined"
          class="pa-5 fill-height d-flex flex-column justify-space-between rounded-xl card-hover-lift border bg-surface"
        >
          <div>
            <div class="d-flex align-center mb-3">
              <v-avatar color="blue-grey" variant="tonal" size="48" class="mr-3">
                <v-icon icon="mdi-school" size="24"></v-icon>
              </v-avatar>
              <div class="text-left">
                <div class="text-subtitle-1 font-weight-bold">Escolas</div>
                <div class="text-caption text-medium-emphasis">Registo de Instituições</div>
              </div>
            </div>
            <p class="text-body-2 text-medium-emphasis text-left mb-4">
              Adicione e faça a gestão das escolas e centros onde decorrem os exames.
            </p>
          </div>
          <v-btn block color="blue-grey" variant="flat" class="text-none font-weight-bold">
            <span>Gerir Escolas</span>
            <v-icon end icon="mdi-arrow-right" class="ml-1" size="18"></v-icon>
          </v-btn>
        </v-card>
      </v-col>

      <v-col cols="12" sm="6" md="4" v-if="auth.hasPermission('DISCIPLINE_CREATE')">
        <v-card
          to="/disciplines"
          variant="outlined"
          class="pa-5 fill-height d-flex flex-column justify-space-between rounded-xl card-hover-lift border bg-surface"
        >
          <div>
            <div class="d-flex align-center mb-3">
              <v-avatar color="purple" variant="tonal" size="48" class="mr-3">
                <v-icon icon="mdi-book-education" size="24"></v-icon>
              </v-avatar>
              <div class="text-left">
                <div class="text-subtitle-1 font-weight-bold">Disciplinas</div>
                <div class="text-caption text-medium-emphasis">Cursos e Códigos de Exame</div>
              </div>
            </div>
            <p class="text-body-2 text-medium-emphasis text-left mb-4">
              Defina as disciplinas e os respetivos códigos identificadores de cada exame.
            </p>
          </div>
          <v-btn block color="purple" variant="flat" class="text-none font-weight-bold">
            <span>Gerir Disciplinas</span>
            <v-icon end icon="mdi-arrow-right" class="ml-1" size="18"></v-icon>
          </v-btn>
        </v-card>
      </v-col>

      <v-col cols="12" sm="6" md="4" v-if="auth.hasPermission('PERSON_CREATE')">
        <v-card
          to="/people"
          variant="outlined"
          class="pa-5 fill-height d-flex flex-column justify-space-between rounded-xl card-hover-lift border bg-surface"
        >
          <div>
            <div class="d-flex align-center mb-3">
              <v-avatar color="deep-purple" variant="tonal" size="48" class="mr-3">
                <v-icon icon="mdi-account-group" size="24"></v-icon>
              </v-avatar>
              <div class="text-left">
                <div class="text-subtitle-1 font-weight-bold">Utilizadores</div>
                <div class="text-caption text-medium-emphasis">Contas e Perfis de Acesso</div>
              </div>
            </div>
            <p class="text-body-2 text-medium-emphasis text-left mb-4">
              Crie e administre as contas de alunos, professores e funcionários de cada escola.
            </p>
          </div>
          <v-btn block color="deep-purple" variant="flat" class="text-none font-weight-bold">
            <span>Gerir Utilizadores</span>
            <v-icon end icon="mdi-arrow-right" class="ml-1" size="18"></v-icon>
          </v-btn>
        </v-card>
      </v-col>

      <!-- DISTRIBUTION CARD (Admin: Distribute Exams) -->
      <v-col cols="12" sm="6" md="4" v-if="auth.hasPermission('EXAM_DISTRIBUTE')">
        <v-card
          to="/distribution"
          variant="outlined"
          class="pa-5 fill-height d-flex flex-column justify-space-between rounded-xl card-hover-lift border bg-surface"
        >
          <div>
            <div class="d-flex align-center mb-3">
              <v-avatar color="primary" variant="tonal" size="48" class="mr-3">
                <v-icon icon="mdi-share-variant" size="24"></v-icon>
              </v-avatar>
              <div class="text-left">
                <div class="text-subtitle-1 font-weight-bold">Distribuição de Provas</div>
                <div class="text-caption text-medium-emphasis">Atribuição a Professores</div>
              </div>
            </div>
            <p class="text-body-2 text-medium-emphasis text-left mb-4">
              Distribua automaticamente as perguntas pelos professores de cada disciplina para iniciarem as correções.
            </p>
          </div>
          <v-btn block color="primary" variant="flat" class="text-none font-weight-bold">
            <span>Distribuir Provas</span>
            <v-icon end icon="mdi-arrow-right" class="ml-1" size="18"></v-icon>
          </v-btn>
        </v-card>
      </v-col>

      <!-- STATISTICS CARD (For Admin, Staff, Teacher) -->
      <v-col cols="12" sm="6" md="4" v-if="auth.hasPermission('STATISTICS_READ')">
        <v-card
          to="/statistics"
          variant="outlined"
          class="pa-5 fill-height d-flex flex-column justify-space-between rounded-xl card-hover-lift border bg-surface"
        >
          <div>
            <div class="d-flex align-center mb-3">
              <v-avatar color="amber" variant="tonal" size="48" class="mr-3">
                <v-icon icon="mdi-chart-bar" color="amber-darken-2" size="24"></v-icon>
              </v-avatar>
              <div class="text-left">
                <div class="text-subtitle-1 font-weight-bold">Estatísticas Globais</div>
                <div class="text-caption text-medium-emphasis">Métricas em Tempo Real</div>
              </div>
            </div>
            <p class="text-body-2 text-medium-emphasis text-left mb-4">
              Consulte gráficos e indicadores sobre o progresso das correções e o desempenho dos alunos.
            </p>
          </div>
          <v-btn block color="amber-darken-3" variant="flat" class="text-none font-weight-bold">
            <span>Ver Estatísticas</span>
            <v-icon end icon="mdi-arrow-right" class="ml-1" size="18"></v-icon>
          </v-btn>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

const roleLabel = computed(() => {
  switch (auth.user?.role) {
    case 'ADMINISTRATOR':
      return 'Administrador Geral'
    case 'SCHOOL_STAFF':
      return 'Funcionário da Escola'
    case 'TEACHER':
      return 'Professor Avaliador'
    case 'STUDENT':
      return 'Aluno'
    default:
      return auth.user?.role ?? 'Utilizador'
  }
})

const roleColor = computed(() => {
  switch (auth.user?.role) {
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
})
</script>

<style scoped>
.hero-banner {
  background: linear-gradient(135deg, rgba(var(--v-theme-primary), 0.12) 0%, rgba(var(--v-theme-surface), 0.95) 100%);
  position: relative;
  overflow: hidden;
}

.v-theme--light .hero-banner {
  background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 100%);
  color: #ffffff !important;
}

.v-theme--light .hero-title,
.v-theme--light .hero-subtitle {
  color: #ffffff !important;
}

.v-theme--light .hero-shield {
  background-color: rgba(255, 255, 255, 0.2) !important;
}

.v-theme--light .hero-shield .v-icon {
  color: #ffffff !important;
}

.v-theme--dark .hero-banner {
  background: linear-gradient(135deg, #131b2e 0%, #1e293b 100%);
  border-color: rgba(59, 130, 246, 0.3) !important;
}
</style>
