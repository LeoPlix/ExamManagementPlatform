export default interface TeacherTaskDto {
  id: number
  examId: number
  questionNumber: string
  maxScore: number
  disciplineId: number
  disciplineName?: string
  disciplineCode?: string
  pageNumber: number
  score?: number | null
  feedback?: string | null
  status?: string
  hasAnnotation?: boolean
}

export interface SubmitEvaluationRequest {
  score: number
  feedback?: string
  annotatedImageData?: string
}
