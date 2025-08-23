import request from './request';

export const userApi = {
  // 分页查询用户
  getUserList: (params, role) => {
    return request.post(`/user/admin/page?role=${role}`, params);
  },

  // 查询待审核教师列表
  getPendingTeachers: () => {
    return request.get('/user/admin/pending-teachers');
  },

  // 审核教师账户并通过
  approveTeacher: (approvalData) => {
    return request.post('/user/admin/approve', approvalData);
  },

  // 审核教师账户并拒绝
  rejectTeacher: (approvalData) => {
    return request.post('/user/admin/reject', approvalData);
  },

  // 修改用户状态
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
  },

  // 修改密码
  changePassword: (passwordData) => {
    return request.put('/user/password', passwordData);
  },

};
