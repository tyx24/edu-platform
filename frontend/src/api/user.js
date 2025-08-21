import request from './request';

export const userApi = {
  // 分页查询用户
  getUserList: (params) => {
    return request.get('/user/admin/page', { params });
  },

  // 查询待审核教师列表
  getPendingTeachers: () => {
    return request.get('/user/admin/pending-teachers');
  },

  // 审核教师账户
  approveTeacher: (approvalData) => {
    return request.post('/user/admin/approve', approvalData);
  },

  // 启用-禁用用户
  updateUserStatus: (statusData) => {
    return request.post('/user/admin/status', statusData);
  },

  // 修改用户角色
  updateUserRole: (roleData) => {
    return request.post('/user/admin/update-role', roleData);
  },

  // 获取当前用户信息
  getUserInfo: () => {
    return request.get('/user/me');
  },

  // 获取用户信息
  getUserDetail: (userId) => {
    return request.get(`/user/${userId}`);
  }

};
