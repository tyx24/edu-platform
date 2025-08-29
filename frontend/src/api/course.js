import request from './request';

export const courseApi = {
  // 分页查询课程
  getCourseList: (params) => {
    return request.post('/course/list', params);
  },

  // 获取课程详情
  getCourseDetail: (courseId) => {
    return request.get(`/course/detail/${courseId}`);
  },

  // 教师发布课程
  createCourse: (courseData) => {
    return request.post('/course/create', courseData);
  },

  // 管理员审核通过课程
  approveCourse: (courseId) => {
    return request.post(`/course/approve/${courseId}`);
  },

  // 管理员驳回课程（可扩展传rejectReason）
  rejectCourse: (courseId) => {
    return request.post(`/course/reject/${courseId}`);
  },

  // ================== 章节 ==================

  // 获取章节树
  getChapterTree: (courseId) => {
    return request.get(`/chapter/tree/${courseId}`);
  },

  // 创建章节
  createChapter: (chapterData) => {
    return request.post('/chapter/create', chapterData);
  },

  // 更新章节
  updateChapter: (id, chapterData) => {
    return request.put(`/chapter/update/${id}`, chapterData);
  },

  // 删除章节
  deleteChapter: (id) => {
    return request.delete(`/chapter/delete/${id}`);
  },

  // 获取章节详情
  getChapterDetail: (id) => {
    return request.get(`/chapter/detail/${id}`);
  },

  // ================== 资源 ==================

  // 获取章节下的资源列表
  getResourceList: (chapterId) => {
    return request.get(`/resource/list/${chapterId}`);
  },

  // 上传资源
  uploadResource: (formData) => {
    return request.post('/resource/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  },
  
  // 更新资源 // ？？？
  updateResource: (resourceId, data) => {
    return request.put(`/resource/${resourceId}`, data);
  },

  // 删除资源
  deleteResource: (id) => {
    return request.delete(`/resource/delete/${id}`);
  }
};