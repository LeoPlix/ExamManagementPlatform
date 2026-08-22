<template>
  <v-dialog v-model="visible" max-width="560">
    <v-card prepend-icon="mdi-account-edit-outline" title="Editar Utilizador" class="rounded-xl">
      <v-card-text class="pa-4">
        <v-form ref="form" v-model="valid">
          <v-text-field
            label="Nome Completo*"
            required
            variant="outlined"
            density="comfortable"
            class="mb-2"
            :rules="[v => !!v || 'Nome é obrigatório']"
            v-model="personData.name"
          ></v-text-field>

          <v-text-field
            label="Email*"
            type="email"
            required
            variant="outlined"
            density="comfortable"
            class="mb-2"
            :rules="[v => !!v || 'Email é obrigatório', v => /.+@.+\..+/.test(v) || 'Email inválido']"
            v-model="personData.email"
          ></v-text-field>

          <v-text-field
            label="Nova Palavra-passe (opcional)"
            type="password"
            placeholder="Deixe em branco para manter a atual"
            variant="outlined"
            density="comfortable"
            class="mb-2"
            v-model="personData.password"
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
            v-model="personData.type"
          ></v-select>

          <v-select
            v-if="personData.type"
            :items="schools"
            item-title="name"
            item-value="id"
            label="Escola Associada"
            variant="outlined"
            density="comfortable"
            class="mb-2"
            clearable
            v-model="personData.schoolId"
          ></v-select>

          <v-select
            v-if="personData.type === 'TEACHER'"
            :items="disciplines"
            item-title="name"
            item-value="id"
            label="Disciplinas Lecionadas"
            variant="outlined"
            density="comfortable"
            multiple
            chips
            clearable
            v-model="personData.disciplineIds"
          ></v-select>
        </v-form>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions class="pa-4 pt-2">
        <v-spacer></v-spacer>
        <v-btn text="Cancelar" variant="plain" class="text-none" @click="visible = false"></v-btn>
        <v-btn
          color="primary"
          text="Guardar Alterações"
          variant="flat"
          class="text-none font-weight-bold"
          :loading="saving"
          @click="saveChanges"
        ></v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import PersonDto from '@/models/PersonDto'
import type SchoolDto from '@/models/SchoolDto'
import type DisciplineDto from '@/models/DisciplineDto'
import RemoteService from '@/services/RemoteService'

const props = defineProps<{
  modelValue: boolean
  person: PersonDto | null
}>()

const emit = defineEmits(['update:modelValue', 'person-updated'])

const visible = ref(props.modelValue)
const valid = ref(false)
const saving = ref(false)
const form = ref<any>(null)

const schools = ref<SchoolDto[]>([])
const disciplines = ref<DisciplineDto[]>([])

// Only non-admin roles can be assigned
const roles = [
  { label: 'Funcionário da Escola', value: 'SCHOOL_STAFF' },
  { label: 'Professor', value: 'TEACHER' },
  { label: 'Aluno', value: 'STUDENT' }
]

const personData = ref<PersonDto>(new PersonDto())

watch(() => props.modelValue, async (val) => {
  visible.value = val
  if (val && props.person) {
    personData.value = new PersonDto({
      ...props.person,
      password: '',
      disciplineIds: props.person.disciplineIds ? [...props.person.disciplineIds] : []
    })
    try {
      const [fetchedSchools, fetchedDisciplines] = await Promise.all([
        RemoteService.getSchools(),
        RemoteService.getDisciplines()
      ])
      schools.value = fetchedSchools
      disciplines.value = fetchedDisciplines
    } catch (err) {
      console.error(err)
    }
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const saveChanges = async () => {
  if (form.value) {
    const { valid } = await form.value.validate()
    if (!valid) return
  }
  if (!personData.value.id) return

  saving.value = true
  try {
    await RemoteService.updatePerson(personData.value.id, personData.value)
    visible.value = false
    emit('person-updated')
  } catch (err) {
    console.error(err)
  } finally {
    saving.value = false
  }
}
</script>
