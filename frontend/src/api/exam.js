import request from './request';

export const examApi = {
  // 创建考试
  createExam: (examData) => {
    return request.post('/exam', examData);
  },

  // 更新考试
  updateExam: (id, examData) => {
    return request.put(`/exam/${id}`, examData);
  },

  // 删除考试
  deleteExam: (id) => {
    return request.delete(`/exam/${id}`);
  },

  // 获取课程考试列表
  getCourseExams: (courseId) => {
    return request.get(`/exam/course/${courseId}`);
  },

  // 获取考试记录
  getExamRecords: (examId) => {
    return request.get(`/examRecord/exam/${examId}`);
  },

  // 获取学生的考试记录
  getMyExamRecord: (examId) => {
    return request.get(`/examRecord/my/${examId}`);
  },

  // 提交考试记录
  submitExamRecord: (recordData) => {
    return request.post('/examRecord/submit', recordData);
  },

  // 创建题目
  createQuestion: (questionData) => {
    return request.post('/question/create', questionData);
  },

  // 更新题目
  updateQuestion: (id, questionData) => {
    return request.put(`/question/update/${id}`, questionData);
  },

  // 删除题目
  deleteQuestion: (id) => {
    return request.delete(`/question/delete/${id}`);
  },

  // 获取学生考试题目
  getExamQuestions: (examId) => {
    return request.get(`/question/exam/${examId}`);
  },

  // 获取题目列表
  getQuestionList: (examId) => {
    return request.get(`/question/list/${examId}`);
  },

  // 教师获取指定考试的题目列表
  getTeacherExamQuestions: (examId) => {
    return request.get(`/question/teacher/exam/${examId}`);
  }
};
