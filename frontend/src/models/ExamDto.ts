import type QuestionDto from './QuestionDto'

export default interface ExamDto {
  id: number
  title: string
  schoolId: number
  schoolName?: string
  disciplineId: number
  disciplineName?: string
  studentId: number
  studentName?: string
  studentEmail?: string
  pdfFilename: string
  totalPages: number
  status: 'UPLOADED' | 'SEGMENTED' | 'IN_DISTRIBUTION' | 'DISTRIBUTED' | 'CORRECTED' | 'RELEASED'
  createdAt: string
  totalScore: number
  obtainedScore?: number
  gradesPublished?: boolean
  gradesPublishedAt?: string
  released?: boolean
  releasedAt?: string
  reviewDeadline?: string
  viewRequested?: boolean
  questionCount: number
  questions?: QuestionDto[]
}
