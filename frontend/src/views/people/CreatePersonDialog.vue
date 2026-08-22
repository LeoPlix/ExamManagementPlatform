<template>
  <div class="text-right">
    <v-dialog v-model="dialog" max-width="560">
      <template v-slot:activator="{ props: activatorProps }">
        <v-btn
          class="text-none font-weight-bold"
          prepend-icon="mdi-plus"
          text="Adicionar Utilizador"
          v-bind="activatorProps"
          color="primary"
          variant="flat"
          @click="openDialog"
        ></v-btn>
      </template>

      <v-card prepend-icon="mdi-account-plus-outline" title="Novo Utilizador" class="rounded-xl">
        <v-card-text class="pa-4">
          <v-form ref="form" v-model="valid">
            <v-text-field
              label="Nome Completo*"
              placeholder="ex: Maria Santos"
              required
              variant="outlined"
              density="comfortable"
              class="mb-2"
              :rules="[v => !!v || 'Nome é obrigatório']"
              v-model="newPerson.name"
            ></v-text-field>

            <v-text-field
              label="Email*"
              placeholder="ex: maria.santos@escola.pt"
              type="email"
              required
              variant="outlined"
              density="comfortable"
              class="mb-2"
              :rules="[v => !!v || 'Email é obrigatório', v => /.+@.+\..+/.test(v) || 'Email inválido']"
              v-model="newPerson.email"
            ></v-text-field>

            <v-text-field
              label="Palavra-passe*"
              type="password"
              required
              variant="outlined"
              density="comfortable"
              class="mb-2"
              :rules="[v => !!v || 'Palavra-passe é obrigatória', v => (v && v.length >= 4) || 'Mínimo 4 caracteres']"
              v-model="newPerson.password"
            ></v-text-field>

            <v-select
              :items="roles"
              item-title="label"
              item-value="value"
              label="Papel / Categoria*"
              required
              variant="outlined"
              density="comfortable"
              class="mb-2"
              :rules="[v => !!v || 'Papel é obrigatório']"
              v-model="newPerson.type"
            ></v-select>

            <v-select
              v-if="newPerson.type"
              :items="schools"
              item-title="name"
              item-value="id"
              label="Escola Associada"
              variant="outlined"
              density="comfortable"
              class="mb-2"
              clearable
              v-model="newPerson.schoolId"
            ></v-select>

            <v-select
              v-if="newPerson.type === 'TEACHER'"
              :items="disciplines"
              item-title="name"
              item-value="id"
              label="Disciplinas Lecionadas"
              variant="outlined"
              density="comfortable"
              multiple
              chips
              clearable
              v-model="newPerson.disciplineIds"
            ></v-select>
          </v-form>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions class="pa-4 pt-2">
          <v-spacer></v-spacer>
          <v-btn text="Cancelar" variant="plain" class="text-none" @click="dialog = false"></v-btn>
          <v-btn
            color="primary"
            text="Criar Utilizador"
            variant="flat"
            class="text-none font-weight-bold"
            :loading="saving"
            @click="savePerson"
          ></v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PersonDto from '@/models/PersonDto'
import type SchoolDto from '@/models/SchoolDto'
import type DisciplineDto from '@/models/DisciplineDto'
import RemoteService from '@/services/RemoteService'

const dialog = ref(false)
const valid = ref(false)
const saving = ref(false)
const form = ref<any>(null)

const schools = ref<SchoolDto[]>([])
const disciplines = ref<DisciplineDto[]>([])

const emit = defineEmits(['person-created'])

// Administrators can only create School Staff, Teachers, and Students
const roles = [
  { label: 'Funcionário da Escola', value: 'SCHOOL_STAFF' },
  { label: 'Professor', value: 'TEACHER' },
  { label: 'Aluno', value: 'STUDENT' }
]

const newPerson = ref<PersonDto>(new PersonDto({
  name: '',
  email: '',
  password: '',
  type: 'STUDENT',
  schoolId: null,
  disciplineIds: []
}))

const openDialog = async () => {
  try {
    const [fetchedSchools, fetchedDisciplines] = await Promise.all([
      RemoteService.getSchools(),
      RemoteService.getDisciplines()
    ])
    schools.value = fetchedSchools
    disciplines.value = fetchedDisciplines
  } catch (err) {
    console.error('Error fetching schools or disciplines:', err)
  }
}

const savePerson = async () => {
  if (form.value) {
    const { valid } = await form.value.validate()
    if (!valid) return
  }

  saving.value = true
  try {
    await RemoteService.createPerson(newPerson.value)
    dialog.value = false
    newPerson.value = new PersonDto({
      name: '',
      email: '',
      password: '',
      type: 'STUDENT',
      schoolId: null,
      disciplineIds: []
    })
    emit('person-created')
  } catch (err) {
    console.error(err)
  } finally {
    saving.value = false
  }
}
</script>
