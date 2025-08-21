import request from './request';

export const enrollmentApi = {
  // 学生选课
  enrollCourse: (courseId) => {
    return request.post(`/enrollment/${courseId}`);
  },

  // 学生退课
  dropCourse: (courseId) => {
    return request.delete(`/enrollment/${courseId}`);
  },

  // 学生查看已选课程
  getMyCourses: () => {
    return request.get('/enrollment/my-courses');
  },

  // 学生查看自己的选课记录
  getMyEnrollments: () => {
    return request.get('/enrollment/my-enrollments');
  },

  // 教师查看自己课程及学生名单
  getMyCoursesWithStudents: () => {
    return request.get('/enrollment/my-courses-with-students');
  },

  // 更新学习进度
  updateProgress: (progressData) => {
    return request.put('/enrollment/progress', progressData);
  },

  // 管理员导出选课名单（Excel）
  exportEnrollments: () => {
    return request.get('/enrollment/export-enrollments', {
      responseType: 'blob'
    });
  }
};