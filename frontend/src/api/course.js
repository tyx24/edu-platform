import request from './request';

export const courseApi = {
  // 分页查询课程
  getCourseList: (params, difficulty) => {
    const url = difficulty ? `/course/list?difficulty=${encodeURIComponent(difficulty)}` : '/course/list';
    return request.post(url, params);
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
      },
        timeout: 300000,
        onUploadProgress: (progressEvent) => {
            const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total);
            console.log(`上传进度：${percent}%`);
        }
    });
  },


  // 删除资源
  deleteResource: (id) => {
    return request.delete(`/resource/delete/${id}`);
  }
};