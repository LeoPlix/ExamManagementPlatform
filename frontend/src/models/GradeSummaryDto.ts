export default interface GradeSummaryDto {
  examId: number
  examTitle: string
  disciplineId: number
  disciplineName?: string
  disciplineCode?: string
  schoolId: number
  schoolName?: string
  studentId: number
  studentName?: string
  studentEmail?: string
  totalScore: number
  obtainedScore?: number
  gradesPublished?: boolean
  gradesPublishedAt?: string
  status: string
  released: boolean
  releasedAt?: string
  reviewDeadline?: string
  viewRequested?: boolean
  questionCount: number
  pendingReviewCount?: number
  reviewedCount?: number
}
