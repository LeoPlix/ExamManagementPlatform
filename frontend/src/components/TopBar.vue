<template>
  <NavBar :navbarItems="navbarItems" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import NavBar from '@/components/NavBar.vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

const navbarItems = computed(() => {
  const items = []

  items.push({ name: 'Início', path: '/', icon: 'mdi-home' })

  if (auth.hasPermission('REVIEW_REQUEST')) {
    items.push({ name: 'As Minhas Provas', path: '/my-exams', icon: 'mdi-file-eye-outline' })
  }

  if (auth.hasPermission('EVALUATION_READ')) {
    items.push({ name: 'Minhas Correções', path: '/teacher/tasks', icon: 'mdi-checkbox-marked-circle-outline' })
  }

  if (auth.hasPermission('EXAM_UPLOAD') || auth.hasPermission('EXAM_SEGMENT')) {
    items.push({ name: 'Exames & Digitalização', path: '/exams', icon: 'mdi-file-document-multiple' })
  }

  if (auth.hasPermission('GRADES_READ')) {
    items.push({ name: 'Pauta de Notas', path: '/grades', icon: 'mdi-format-list-numbered' })
  }

  if (auth.hasPermission('SCHOOL_CREATE')) {
    items.push({ name: 'Escolas', path: '/schools', icon: 'mdi-school' })
  }

  if (auth.hasPermission('DISCIPLINE_CREATE')) {
    items.push({ name: 'Disciplinas', path: '/disciplines', icon: 'mdi-book-education' })
  }

  if (auth.hasPermission('PERSON_CREATE')) {
    items.push({ name: 'Utilizadores', path: '/people', icon: 'mdi-account-group' })
  }

  if (auth.hasPermission('EXAM_DISTRIBUTE')) {
    items.push({ name: 'Distribuição de Provas', path: '/distribution', icon: 'mdi-share-variant' })
  }

  if (auth.hasPermission('STATISTICS_READ')) {
    items.push({ name: 'Estatísticas', path: '/statistics', icon: 'mdi-chart-bar' })
  }

  return items
})
</script>
