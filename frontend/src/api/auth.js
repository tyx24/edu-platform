import request from './request';

export const authApi = {
  // 用户登录
  login: (username, password) => {
    return request.post('/auth/login', { username, password });
  },

  // 用户注册
  register: (data) => {
    return request.post('/auth/register', data);
  },

  // 用户退出登录
  logout: () => {
    return request.post('/auth/logout');
  },

};
