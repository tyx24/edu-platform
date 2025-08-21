import request from './request';

export const commentApi = {
  // 提交课程评论
  submitComment: (commentData) => {
    return request.post('/comments', commentData);
  },

  // 查询课程评论
  getCourseComments: (courseId) => {
    return request.get(`/comments/${courseId}`);
  },

  // 获取课程评论统计信息
  getCommentStats: (courseId) => {
    return request.get(`/comments/stats/${courseId}`);
  }
};
