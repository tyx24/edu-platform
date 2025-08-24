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

  // 更新用户信息
  // 更新基本信息（真实姓名、邮箱）
  updateBasicInfo: (basicInfo) => {
    return request.post('/userInfo//updateBasicInfo', basicInfo);
  },

  // 更新联系方式
  updateContactInfo: (contactInfo) => {
    return request.post('/userInfo/updateContactInfo', contactInfo);
  },

  // 更新学生信息
  updateStudentInfo: (studentInfo) => {
    return request.post('/userInfo/updateStudentInfo', studentInfo);
  },

  // 更新教师信息
  updateTeacherInfo: (teacherInfo) => {
    return request.post('/userInfo/updateTeacherInfo', teacherInfo);
  },

  // 更新个人简介
  updateBio: (bioData) => {
    return request.post('/userInfo/updateBio', bioData);
  },

  // // ？？？
  // // 重置密码（通过邮箱）
  // resetPasswordByEmail: (email) => {
  //   return request.post('/user/reset-password', { email });
  // },
  // // 重置密码（通过手机号）
  // resetPasswordByPhone: (phone) => {
  //   return request.post('/user/reset-password', { phone });
  // },

  // // ？？？
  // // 获取用户统计信息
  // getUserStats: () => {
  //   return request.get('/user/stats');
  // },

  // // 绑定第三方账号
  // bindThirdPartyAccount: (platform, accountData) => {
  //   return request.post(`/user/bind/${platform}`, accountData);
  // },

  // // 解绑第三方账号
  // unbindThirdPartyAccount: (platform) => {
  //   return request.delete(`/user/bind/${platform}`);
  // },

  // // 获取绑定的第三方账号列表
  // getBindAccounts: () => {
  //   return request.get('/user/bind-accounts');
  // }
};

export default userinfoApi;