import axios from 'axios'
import { useAppearanceStore } from '@/stores/appearance'
import { useAuthStore } from '@/stores/auth'
import DeiError from '@/models/DeiError'
import type PersonDto from '@/models/PersonDto'
import type AuthUser from '@/models/AuthUser'
import type SchoolDto from '@/models/SchoolDto'
import type DisciplineDto from '@/models/DisciplineDto'
import type ExamDto from '@/models/ExamDto'
import type QuestionDto from '@/models/QuestionDto'
import type { QuestionCropRequest } from '@/models/QuestionDto'
import type TeacherTaskDto from '@/models/TeacherTaskDto'
import type { SubmitEvaluationRequest } from '@/models/TeacherTaskDto'
import type GradeSummaryDto from '@/models/GradeSummaryDto'
import type ReviewRequestDto from '@/models/ReviewRequestDto'
import type { CreateReviewRequest, SubmitReviewRequest } from '@/models/ReviewRequestDto'
import type StatisticsDto from '@/models/StatisticsDto'

const httpClient = axios.create()
httpClient.defaults.timeout = 50000
httpClient.defaults.baseURL = import.meta.env.VITE_ROOT_API || 'http://localhost:8080'
httpClient.defaults.headers.post['Content-Type'] = 'application/json'


export interface LoginResponse {
  token: string
  expiresInMs: number
  user: AuthUser
}

export default class RemoteServices {
  // People
  static async getPeople(): Promise<PersonDto[]> {
    return httpClient.get('/people')
  }

  static async getPerson(id: number): Promise<PersonDto> {
    return httpClient.get(`/people/${id}`)
  }

  static async createPerson(person: PersonDto): Promise<PersonDto> {
    return httpClient.post('/people', person)
  }

  static async updatePerson(id: number, person: PersonDto): Promise<PersonDto> {
    return httpClient.put(`/people/${id}`, person)
  }

  static async deletePerson(id: number): Promise<void> {
    return httpClient.delete(`/people/${id}`)
  }

  // Schools
  static async getSchools(): Promise<SchoolDto[]> {
    return httpClient.get('/schools')
  }

  static async getSchool(id: number): Promise<SchoolDto> {
    return httpClient.get(`/schools/${id}`)
  }

  static async createSchool(school: SchoolDto): Promise<SchoolDto> {
    return httpClient.post('/schools', school)
  }

  static async updateSchool(id: number, school: SchoolDto): Promise<SchoolDto> {
    return httpClient.put(`/schools/${id}`, school)
  }

  static async deleteSchool(id: number): Promise<void> {
    return httpClient.delete(`/schools/${id}`)
  }

  // Disciplines
  static async getDisciplines(): Promise<DisciplineDto[]> {
    return httpClient.get('/disciplines')
  }

  static async getDiscipline(id: number): Promise<DisciplineDto> {
    return httpClient.get(`/disciplines/${id}`)
  }

  static async createDiscipline(discipline: DisciplineDto): Promise<DisciplineDto> {
    return httpClient.post('/disciplines', discipline)
  }

  static async updateDiscipline(id: number, discipline: DisciplineDto): Promise<DisciplineDto> {
    return httpClient.put(`/disciplines/${id}`, discipline)
  }

  static async deleteDiscipline(id: number): Promise<void> {
    return httpClient.delete(`/disciplines/${id}`)
  }

  // Exams
  static async getExams(filters?: { schoolId?: number; disciplineId?: number; studentId?: number }): Promise<ExamDto[]> {
    return httpClient.get('/exams', { params: filters })
  }

  static async getExam(id: number): Promise<ExamDto> {
    return httpClient.get(`/exams/${id}`)
  }

  static async uploadExam(formData: FormData): Promise<ExamDto> {
    return httpClient.post('/exams', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }

  static async deleteExam(id: number): Promise<void> {
    return httpClient.delete(`/exams/${id}`)
  }

  static async areSubmissionsLocked(): Promise<{ locked: boolean }> {
    return httpClient.get('/exams/submissions-locked')
  }

  static async getExamPdfBlob(id: number): Promise<string> {
    const response = await httpClient.get(`/exams/${id}/pdf`, { responseType: 'blob' })
    return URL.createObjectURL(response as unknown as Blob)
  }

  static async getExamPageImageBlob(id: number, pageNumber: number): Promise<string> {
    const response = await httpClient.get(`/exams/${id}/pages/${pageNumber}/image`, {
      responseType: 'blob'
    })
    return URL.createObjectURL(response as unknown as Blob)
  }

  static async addQuestion(examId: number, cropReq: QuestionCropRequest): Promise<QuestionDto> {
    return httpClient.post(`/exams/${examId}/questions`, cropReq)
  }

  static async getExamQuestions(examId: number): Promise<QuestionDto[]> {
    return httpClient.get(`/exams/${examId}/questions`)
  }

  static async deleteQuestion(examId: number, questionId: number): Promise<void> {
    return httpClient.delete(`/exams/${examId}/questions/${questionId}`)
  }

  static async completeExamSegmentation(examId: number): Promise<ExamDto> {
    return httpClient.post(`/exams/${examId}/complete-segmentation`)
  }

  static async getQuestionImageBlob(questionId: number): Promise<string> {
    const response = await httpClient.get(`/questions/${questionId}/image`, {
      responseType: 'blob'
    })
    return URL.createObjectURL(response as unknown as Blob)
  }

  static async getQuestionAnnotatedImageBlob(questionId: number): Promise<string> {
    const response = await httpClient.get(`/questions/${questionId}/annotated-image`, {
      responseType: 'blob'
    })
    return URL.createObjectURL(response as unknown as Blob)
  }

  // Distribution
  static async distributeExams(): Promise<{ distributedExamsCount: number; distributedQuestionsCount: number }> {
    return httpClient.post('/exams/distribute')
  }

  // Teacher evaluations
  static async getTeacherTasks(status?: string): Promise<TeacherTaskDto[]> {
    return httpClient.get('/teacher/tasks', { params: { status } })
  }

  static async submitTaskEvaluation(questionId: number, data: SubmitEvaluationRequest): Promise<TeacherTaskDto> {
    return httpClient.post(`/teacher/tasks/${questionId}/submit`, data)
  }

  // Grades & Release
  static async getGrades(filters?: { schoolId?: number; disciplineId?: number }): Promise<GradeSummaryDto[]> {
    return httpClient.get('/grades', { params: filters })
  }

  static async publishInitialGrades(filters?: { schoolId?: number; disciplineId?: number }): Promise<GradeSummaryDto[]> {
    return httpClient.post('/grades/publish-initial', null, { params: filters })
  }

  static async requestExamView(id: number): Promise<ExamDto> {
    return httpClient.post(`/exams/${id}/request-view`)
  }

  static async releaseExam(id: number): Promise<GradeSummaryDto> {
    return httpClient.post(`/exams/${id}/release`)
  }

  static async bulkRelease(filters?: { schoolId?: number; disciplineId?: number }): Promise<GradeSummaryDto[]> {
    return httpClient.post('/exams/bulk-release', null, { params: filters })
  }

  static async publishReviewGrades(filters?: { schoolId?: number; disciplineId?: number }): Promise<GradeSummaryDto[]> {
    return httpClient.post('/exams/publish-reviews', null, { params: filters })
  }

  // Reviews
  static async createReviewRequest(examId: number, req: CreateReviewRequest): Promise<ReviewRequestDto> {
    return httpClient.post(`/student/exams/${examId}/reviews`, req)
  }

  static async getStudentReviews(): Promise<ReviewRequestDto[]> {
    return httpClient.get('/student/reviews')
  }

  static async distributeReviews(disciplineId?: number): Promise<{ distributedReviewsCount: number }> {
    return httpClient.post('/reviews/distribute', null, { params: { disciplineId } })
  }

  static async getTeacherReviews(): Promise<ReviewRequestDto[]> {
    return httpClient.get('/teacher/reviews')
  }

  static async submitReviewEvaluation(reviewId: number, req: SubmitReviewRequest): Promise<ReviewRequestDto> {
    return httpClient.post(`/teacher/reviews/${reviewId}/submit`, req)
  }

  // Statistics
  static async getStatistics(): Promise<StatisticsDto> {
    return httpClient.get('/statistics')
  }

  // Authentication
  static async login(email: string, password: string): Promise<LoginResponse> {
    return httpClient.post('/auth/login', { email, password })
  }

  static async getCurrentUser(): Promise<AuthUser> {
    return httpClient.get('/auth/me')
  }

  static async errorMessage(error: any): Promise<string> {
    if (error.message === 'Network Error') {
      return 'Unable to connect to the server'
    } else if (error.message?.split(' ')[0] === 'timeout') {
      return 'Request timeout - Server took too long to respond'
    } else {
      return error.response?.data?.message ?? error.message ?? 'Unknown Error'
    }
  }

  static async handleError(error: any): Promise<never> {
    if (error.response?.status === 401) {
      useAuthStore().logout()
    }
    const deiErr = new DeiError(
      await RemoteServices.errorMessage(error),
      error.response?.data?.code ?? -1
    )
    const appearance = useAppearanceStore()
    appearance.pushError(deiErr)
    appearance.loading = false
    throw deiErr
  }
}

// Attach the JWT (if any) to every outgoing request.
httpClient.interceptors.request.use((request) => {
  const auth = useAuthStore()
  if (auth.token) {
    request.headers.Authorization = `Bearer ${auth.token}`
  }
  return request
}, RemoteServices.handleError)

httpClient.interceptors.response.use((response) => response.data, RemoteServices.handleError)
