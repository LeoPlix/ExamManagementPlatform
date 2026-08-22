export interface DisciplineStatDto {
  id: number
  name: string
  code: string
  totalExams: number
  countUploaded?: number
  countSegmented: number
  countCorrected: number
  avgScore: number
}

export default interface StatisticsDto {
  totalExams: number
  totalStudents: number
  countUploaded: number
  countSegmented: number
  countInDistribution: number
  countDistributed: number
  countCorrected: number
  countReleased: number
  correctionRate: number
  globalAverage: number
  totalReviews: number
  resolvedReviews: number
  reviewResolutionRate: number
  disciplineStats: DisciplineStatDto[]
}
