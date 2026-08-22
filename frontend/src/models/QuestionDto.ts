export default interface QuestionDto {
  id: number
  examId: number
  disciplineId: number
  disciplineName?: string
  questionNumber: string
  maxScore: number
  imagePath: string
  pageNumber: number
  cropX?: number
  cropY?: number
  cropWidth?: number
  cropHeight?: number
  orderIndex: number
  score?: number | null
  feedback?: string | null
  status?: 'PENDING_DISTRIBUTION' | 'PENDING_EVALUATION' | 'EVALUATED' | 'IN_REVIEW' | 'REVIEWED'
  hasAnnotation?: boolean
}

export interface QuestionCropRequest {
  questionNumber: string
  maxScore: number
  pageNumber: number
  cropX?: number | null
  cropY?: number | null
  cropWidth?: number | null
  cropHeight?: number | null
  disciplineId?: number | null
}
