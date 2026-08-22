import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '@/views/LoginView.vue'
import PeopleView from '@/views/people/PeopleView.vue'
import SchoolsView from '@/views/schools/SchoolsView.vue'
import DisciplinesView from '@/views/disciplines/DisciplinesView.vue'
import ExamsView from '@/views/exams/ExamsView.vue'
import ExamSegmentationView from '@/views/exams/ExamSegmentationView.vue'
import StatisticsView from '@/views/statistics/StatisticsView.vue'
import TeacherTasksView from '@/views/evaluations/TeacherTasksView.vue'
import GradesTableView from '@/views/grades/GradesTableView.vue'
import StudentExamsView from '@/views/student/StudentExamsView.vue'
import DistributionView from '@/views/distribution/DistributionView.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { public: true }
    },
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/distribution',
      name: 'distribution',
      component: DistributionView,
      meta: { permission: 'EXAM_DISTRIBUTE' }
    },
    {
      path: '/schools',
      name: 'schools',
      component: SchoolsView,
      meta: { permission: 'SCHOOL_CREATE' }
    },
    {
      path: '/disciplines',
      name: 'disciplines',
      component: DisciplinesView,
      meta: { permission: 'DISCIPLINE_CREATE' }
    },
    {
      path: '/people',
      name: 'people',
      component: PeopleView,
      meta: { permission: 'PERSON_CREATE' }
    },
    {
      path: '/exams',
      name: 'exams',
      component: ExamsView,
      meta: { permission: 'EXAM_UPLOAD' }
    },
    {
      path: '/exams/:id/segment',
      name: 'exam-segment',
      component: ExamSegmentationView,
      meta: { permission: 'EXAM_SEGMENT' }
    },
    {
      path: '/teacher/tasks',
      name: 'teacher-tasks',
      component: TeacherTasksView,
      meta: { permission: 'EVALUATION_READ' }
    },
    {
      path: '/grades',
      name: 'grades',
      component: GradesTableView,
      meta: { permission: 'GRADES_READ' }
    },
    {
      path: '/my-exams',
      name: 'my-exams',
      component: StudentExamsView,
      meta: { permission: 'REVIEW_REQUEST' }
    },
    {
      path: '/statistics',
      name: 'statistics',
      component: StatisticsView,
      meta: { permission: 'STATISTICS_READ' }
    }
  ]
})

// Auth guard: /login is public; everything else needs a valid session, and a
// route may additionally require a permission via meta.permission.
router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.public) {
    if (auth.isAuthenticated && to.name === 'login') {
      return { name: 'home' }
    }
    return true
  }

  if (!auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  const required = to.meta.permission as string | undefined
  if (required && !auth.hasPermission(required)) {
    return { name: 'home' }
  }

  return true
})

export default router
