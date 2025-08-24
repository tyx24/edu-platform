import request from './request';

export const homeworkApi = {
  // 创建作业
  createHomework: (homeworkData) => {
    return request.post('/homework', homeworkData);
  },

  // 更新作业
  updateHomework: (id, homeworkData) => {
    return request.put(`/homework/${id}`, homeworkData);
  },

  // 删除作业
  deleteHomework: (id) => {
    return request.delete(`/homework/${id}`);
  },

  // 获取课程作业列表
  getCourseHomework: (courseId) => {
    return request.get(`/homework/course/${courseId}`);
  },

  // 提交作业
  submitHomework: (submissionData) => {
    return request.post('/submission', submissionData);
  },

  // 上传作业文件
  uploadHomeworkFile: (formData) => {
    return request.post('/submission/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  },

  // 给作业评分
  scoreHomework: (id, scoreData) => {
    return request.post(`/submission/${id}/score`, scoreData);
  },

  // 获取作业提交记录
  getHomeworkSubmissions: (homeworkId) => {
    return request.get(`/submission/homework/${homeworkId}`);
  },

  // 获取学生作业提交记录
  getMySubmissions: () => {
    return request.get('/submission/my');
  },

  // =====================================
  // 获取作业详情
  getHomeworkById: (id) => {
    return request.get(`/homework/${id}`);
  },

  // 获取教师作业列表（基于getCourseHomework的模拟实现）
  getTeacherHomeworks: async (params) => {
    // 这里应该调用获取教师课程的接口，然后遍历获取每个课程的作业
    // 暂时返回模拟数据
    return {
      data: {
        records: [],
        total: 0
      }
    };
  },

  // 获取作业中心数据（学生视角）
  getHomeworkCenter: async (params) => {
    // 这里应该调用获取学生课程的接口，然后遍历获取每个课程的作业
    // 暂时返回模拟数据
    return {
      data: {
        records: [],
        total: 0
      }
    };
  },

  // 获取教师作业统计
  getTeacherHomeworkStats: () => {
    return {
      data: {
        total: 0,
        inProgress: 0,
        submitted: 0,
        overdue: 0
      }
    };
  },

  // 获取作业中心统计
  getHomeworkCenterStats: () => {
    return {
      data: {
        total: 0,
        inProgress: 0,
        submitted: 0,
        overdue: 0
      }
    };
  }
};
