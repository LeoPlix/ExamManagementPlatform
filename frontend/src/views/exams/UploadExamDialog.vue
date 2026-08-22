<template>
  <v-dialog v-model="visible" max-width="580">
    <template v-slot:activator="{ props: activatorProps }">
      <v-tooltip
        :disabled="!disabled"
        text="A criação de exames está bloqueada permanentemente após a distribuição das provas pelo administrador."
        location="bottom"
      >
        <template v-slot:activator="{ props: tooltipProps }">
          <span v-bind="tooltipProps">
            <v-btn
              color="indigo"
              prepend-icon="mdi-upload"
              class="text-none font-weight-bold"
              variant="flat"
              v-bind="activatorProps"
              :disabled="disabled"
              @click="openDialog"
            >
              Carregar Novo Exame (PDF)
            </v-btn>
          </span>
        </template>
      </v-tooltip>
    </template>

    <v-card prepend-icon="mdi-file-pdf-box" title="Carregar Exame Digitalizado" class="rounded-xl">
      <v-card-text class="pa-4">
        <v-form ref="form" v-model="valid">
          <v-text-field
            label="Título da Prova*"
            placeholder="ex: Exame Nacional Matemática A - 2026 - 1ª Fase"
            required
            variant="outlined"
            density="comfortable"
            class="mb-2"
            :rules="[v => !!v || 'Título é obrigatório']"
            v-model="examTitle"
          ></v-text-field>

          <!-- Read-only school for School Staff (strictly their school) -->
          <v-text-field
            v-if="auth.user?.schoolName"
            label="Escola da Prova*"
            :model-value="auth.user.schoolName"
            readonly
            variant="outlined"
            density="comfortable"
            prepend-inner-icon="mdi-school"
            class="mb-2"
          ></v-text-field>

          <v-select
            v-else
            :items="schools"
            item-title="name"
            item-value="id"
            label="Escola*"
            required
            variant="outlined"
            density="comfortable"
            class="mb-2"
            :rules="[v => !!v || 'Escola é obrigatória']"
            v-model="selectedSchoolId"
          ></v-select>

          <v-select
            :items="disciplines"
            item-title="name"
            item-value="id"
            label="Disciplina*"
            required
            variant="outlined"
            density="comfortable"
            class="mb-2"
            :rules="[v => !!v || 'Disciplina é obrigatória']"
            v-model="selectedDisciplineId"
          ></v-select>

          <v-autocomplete
            :items="filteredStudents"
            item-title="name"
            item-value="id"
            label="Aluno*"
            required
            variant="outlined"
            density="comfortable"
            class="mb-2"
            :rules="[
              v => !!v || 'Aluno é obrigatório',
              v => !hasExamForDiscipline(v) || 'Este aluno já tem um exame associado a esta disciplina.'
            ]"
            v-model="selectedStudentId"
            :custom-filter="customStudentFilter"
            :no-data-text="filteredStudents.length === 0 ? 'Sem alunos registados nesta escola.' : 'Nenhum aluno encontrado.'"
          >
            <template v-slot:item="{ props, item }">
              <v-list-item
                v-bind="props"
                :subtitle="item.raw.email"
                :disabled="hasExamForDiscipline(item.raw.id)"
              >
                <template v-slot:append v-if="hasExamForDiscipline(item.raw.id)">
                  <v-chip size="x-small" color="orange" variant="tonal">Já inscrito</v-chip>
                </template>
              </v-list-item>
            </template>
          </v-autocomplete>

          <v-file-input
            label="Ficheiro PDF do Exame*"
            accept="application/pdf,.pdf"
            prepend-icon="mdi-paperclip"
            show-size
            required
            variant="outlined"
            density="comfortable"
            :rules="[v => validatePdfInput(v)]"
            v-model="pdfFile"
          ></v-file-input>
        </v-form>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions class="pa-4 pt-2">
        <v-spacer></v-spacer>
        <v-btn text="Cancelar" variant="plain" class="text-none" @click="visible = false"></v-btn>
        <v-btn
          color="indigo"
          text="Carregar Exame"
          variant="flat"
          class="text-none font-weight-bold"
          :loading="uploading"
          @click="uploadExam"
        ></v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type SchoolDto from '@/models/SchoolDto'
import type DisciplineDto from '@/models/DisciplineDto'
import type PersonDto from '@/models/PersonDto'
import type ExamDto from '@/models/ExamDto'
import RemoteService from '@/services/RemoteService'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

defineProps<{
  disabled?: boolean
}>()

const visible = ref(false)
const valid = ref(false)
const uploading = ref(false)
const form = ref<any>(null)

const schools = ref<SchoolDto[]>([])
const disciplines = ref<DisciplineDto[]>([])
const students = ref<PersonDto[]>([])
const existingExams = ref<ExamDto[]>([])

const examTitle = ref('')
const selectedSchoolId = ref<number | null>(null)
const selectedDisciplineId = ref<number | null>(null)
const selectedStudentId = ref<number | null>(null)
const pdfFile = ref<File | File[] | null>(null)

const emit = defineEmits(['exam-uploaded'])

const effectiveSchoolId = computed(() => {
  return auth.user?.schoolId ?? selectedSchoolId.value
})

const filteredStudents = computed(() => {
  const schoolId = effectiveSchoolId.value
  if (!schoolId) return students.value
  return students.value.filter(s => s.schoolId === schoolId)
})

const hasExamForDiscipline = (studentId?: number | null) => {
  if (!studentId || !selectedDisciplineId.value) return false
  return existingExams.value.some(e => e.studentId === studentId && e.disciplineId === selectedDisciplineId.value)
}

const customStudentFilter = (itemTitle: string, queryText: string, item: any) => {
  const text = (item.raw.name + ' ' + item.raw.email).toLowerCase()
  return text.includes(queryText.toLowerCase())
}

const validatePdfInput = (v: any) => {
  if (!v) return 'Ficheiro PDF é obrigatório'
  if (Array.isArray(v)) {
    return v.length > 0 || 'Ficheiro PDF é obrigatório'
  }
  return true
}

const openDialog = async () => {
  try {
    const [fetchedSchools, fetchedDisciplines, fetchedPeople, fetchedExams] = await Promise.all([
      RemoteService.getSchools(),
      RemoteService.getDisciplines(),
      RemoteService.getPeople(),
      RemoteService.getExams()
    ])
    schools.value = fetchedSchools
    disciplines.value = fetchedDisciplines
    students.value = fetchedPeople.filter(p => p.type === 'STUDENT')
    existingExams.value = fetchedExams

    if (auth.user?.schoolId) {
      selectedSchoolId.value = auth.user.schoolId
    }
  } catch (err) {
    console.error('Error fetching data for exam upload:', err)
  }
}

const uploadExam = async () => {
  if (form.value) {
    const { valid } = await form.value.validate()
    if (!valid) return
  }
  const rawFile = pdfFile.value
  if (!rawFile) return

  const file = Array.isArray(rawFile) ? rawFile[0] : rawFile
  if (!file) return

  const formData = new FormData()
  formData.append('file', file)
  formData.append('title', examTitle.value.trim())
  formData.append('schoolId', String(effectiveSchoolId.value))
  formData.append('disciplineId', String(selectedDisciplineId.value))
  formData.append('studentId', String(selectedStudentId.value))

  uploading.value = true
  try {
    const createdExam = await RemoteService.uploadExam(formData)
    visible.value = false
    examTitle.value = ''
    pdfFile.value = null
    selectedStudentId.value = null
    emit('exam-uploaded', createdExam)
  } catch (err) {
    console.error('Error uploading exam:', err)
  } finally {
    uploading.value = false
  }
}
</script>
