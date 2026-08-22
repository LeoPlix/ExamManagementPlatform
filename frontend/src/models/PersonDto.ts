export default class PersonDto {
  id?: number
  name: string = ''
  email: string = ''
  // Write-only: sent when creating/updating, never returned by the backend.
  password?: string
  type: string = 'STUDENT'
  schoolId?: number | null
  schoolName?: string | null
  disciplineIds?: number[]
  disciplineNames?: string[]

  constructor(obj?: Partial<PersonDto>) {
    Object.assign(this, obj)
  }
}
