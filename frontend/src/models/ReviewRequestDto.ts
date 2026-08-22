export default interface ReviewRequestDto {
  id: number
  questionId: number
  examId?: number
  examTitle?: string
  questionNumber: string
  maxScore: number
  disciplineName?: string
  studentId: number
  studentName?: string
  justification: string
  originalScore?: number | null
  revisedScore?: number | null
  reviewerId?: number | null
  reviewerName?: string | null
  reviewerFeedback?: string | null
  status: 'PENDING' | 'ASSIGNED' | 'RESOLVED' | 'REJECTED'
  createdAt: string
  resolvedAt?: string | null
}

export interface CreateReviewRequest {
  questionId: number
  justification: string
}

export interface SubmitReviewRequest {
  revisedScore: number
  reviewerFeedback?: string
}
