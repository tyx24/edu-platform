import request from './request';

export const userinfoApi = {
  // 获取用户详细信息
  getUserProfile: (params) => {
    return request.post('/userInfo/page', { params });
  },

   // 上传用户头像
  uploadAvatar: (formData) => {
    return request({
      url: '/userInfo/avatar',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  },

  // 更新用户详细信息
  updateUserProfile: (profileData) => {
    return request.put('/user/profile', profileData);
  },


  // 更新基本信息（真实姓名、邮箱）
  updateBasicInfo: (basicInfo) => {
    return request.put('/user/basic-info', basicInfo);
  },

  // 更新联系方式
  updateContactInfo: (contactInfo) => {
    return request.put('/user/contact-info', contactInfo);
  },

  // 更新学生信息
  updateStudentInfo: (studentInfo) => {
    return request.put('/user/student-info', studentInfo);
  },

  // 更新教师信息
  updateTeacherInfo: (teacherInfo) => {
    return request.put('/user/teacher-info', teacherInfo);
  },

  // 更新个人简介
  updateBio: (bioData) => {
    return request.put('/user/bio', bioData);
  },


  // 重置密码（通过邮箱）
  resetPasswordByEmail: (email) => {
    return request.post('/user/reset-password', { email });
  },

  // 更新用户偏好设置
  updateUserPreferences: (preferences) => {
    return request.put('/user/preferences', preferences);
  },

  // 获取用户偏好设置
  getUserPreferences: () => {
    return request.get('/user/preferences');
  },

  // 删除用户头像
  deleteAvatar: () => {
    return request.delete('/user/avatar');
  },

  // 获取用户统计信息
  getUserStats: () => {
    return request.get('/user/stats');
  },

  // 更新用户状态
  updateUserStatus: (status) => {
    return request.put('/user/status', { status });
  },

  // 获取用户活动日志
  getUserActivityLog: (params) => {
    return request.get('/user/activity-log', { params });
  },

  // 绑定第三方账号
  bindThirdPartyAccount: (platform, accountData) => {
    return request.post(`/user/bind/${platform}`, accountData);
  },

  // 解绑第三方账号
  unbindThirdPartyAccount: (platform) => {
    return request.delete(`/user/bind/${platform}`);
  },

  // 获取绑定的第三方账号列表
  getBindAccounts: () => {
    return request.get('/user/bind-accounts');
  }
};

export default userinfoApi;