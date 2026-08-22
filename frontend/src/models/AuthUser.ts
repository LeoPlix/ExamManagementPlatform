// The authenticated user, as returned by /auth/login and /auth/me.
export default interface AuthUser {
  id: number
  name: string
  email: string
  role: string
  schoolId?: number | null
  schoolName?: string | null
  permissions: string[]
}
