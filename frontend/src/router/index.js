// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../store/auth';
import { ElMessage } from 'element-plus';

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/auth/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/courses',
    name: 'Courses',
    component: () => import('../views/course/CourseList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/course/:id',
    name: 'CourseDetail',
    component: () => import('../views/course/CourseDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/course/:courseId/chapter/:chapterId',
    name: 'ChapterDetail',
    component: () => import('../views/course/ChapterDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/course/create',
    name: 'CourseCreate',
    component: () => import('../views/course/CourseForm.vue'),
    meta: { requiresAuth: true, requiresTeacher: true }
  },
  {
    path: '/course/edit/:id',
    name: 'CourseEdit',
    component: () => import('../views/course/CourseForm.vue'),
    meta: { requiresAuth: true, requiresTeacher: true }
  },
  {
    path: '/my-courses',
    name: 'MyCourses',
    component: () => import('../views/course/MyCourses.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/exam-center',
    name: 'ExamCenter',
    component: () => import('../views/exam/ExamCenter.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/exam/course/:courseId',
    name: 'ExamList',
    component: () => import('../views/exam/ExamList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/exam/:examId',
    name: 'ExamDetail',
    component: () => import('../views/exam/ExamDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/exam/create',
    name: 'ExamCreate',
    component: () => import('../views/exam/ExamForm.vue'),
    meta: { requiresAuth: true, requiresTeacher: true }
  },
  {
    path: '/exam/edit/:id',
    name: 'ExamEdit',
    component: () => import('../views/exam/ExamForm.vue'),
    meta: { requiresAuth: true, requiresTeacher: true }
  },
  {
    path: '/exam/:examId/questions',
    name: 'ExamQuestions',
    component: () => import('../views/exam/QuestionManager.vue'),
    meta: { requiresAuth: true, requiresTeacher: true }
  },
  {
    path: '/exam/:examId/records',
    name: 'ExamRecords',
    component: () => import('../views/exam/ExamRecords.vue'),
    meta: { requiresAuth: true, requiresTeacher: true }
  },
  {
    path: '/homework-center',
    name: 'HomeworkCenter',
    component: () => import('../views/homework/HomeworkCenter.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/homework/course/:courseId',
    name: 'HomeworkList',
    component: () => import('../views/homework/HomeworkList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/homework/:homeworkId',
    name: 'HomeworkDetail',
    component: () => import('../views/homework/HomeworkDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/homework/create',
    name: 'HomeworkCreate',
    component: () => import('../views/homework/HomeworkForm.vue'),
    meta: { requiresAuth: true, requiresTeacher: true }
  },
  {
    path: '/homework/edit/:id',
    name: 'HomeworkEdit',
    component: () => import('../views/homework/HomeworkForm.vue'),
    meta: { requiresAuth: true, requiresTeacher: true }
  },
  {
    path: '/homework/:homeworkId/submissions',
    name: 'HomeworkSubmissions',
    component: () => import('../views/homework/SubmissionList.vue'),
    meta: { requiresAuth: true, requiresTeacher: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/admin/Admin.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/teacher',
    name: 'Teacher',
    component: () => import('../views/teacher/Teacher.vue'),
    meta: { requiresAuth: true, requiresTeacher: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/user/Profile.vue'),
    meta: { requiresAuth: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  
  // 检查是否需要登录认证
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login');
    return;
  }
  
  // 检查是否需要管理员权限
  if (to.meta.requiresAdmin && authStore.user && authStore.user.role !== 'admin') {
    ElMessage.error('您没有权限访问该页面');
    next('/home');
    return;
  }
  
  // 检查是否需要教师权限
  if (to.meta.requiresTeacher && authStore.user && authStore.user.role !== 'teacher') {
    ElMessage.error('您没有权限访问该页面');
    next('/home');
    return;
  }
  
  next();
});

export default router;
