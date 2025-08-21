import { defineStore } from 'pinia';
import { authApi } from '../api/auth';
import { userApi } from '../api/user';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: null
  }),
  getters: {
    // 认证状态
    isAuthenticated: state => !!state.token,

    // 用户角色
    userRole: state => state.user?.role || ''

  },
  actions: {
    // 登录
    async login(username, password) {
      try {
        const res = await authApi.login(username, password);
        this.token = res.token;
        localStorage.setItem('token', res.token);
        await this.fetchUserInfo(); // 获取用户信息
        return { success: true };
      } catch (error) {
        return { success: false, error: error.response?.data?.message || '登录失败' };
      }

    },

    // 注册
    async register(data) {
      try {
        const response = await authApi.register(data);
        return { success: true, data: response.data };
      } catch (error) {
        return { success: false, error: error.response?.data?.message || '注册失败' };
      }

    },

    // 获取用户信息
    async fetchUserInfo() {
      try {
        const res = await userApi.getUserInfo();
        this.user = res;
        return { success: true };
      } catch (error) {
        return { success: false, error: error.response?.data?.message || '获取用户信息失败' };
      }

    },

    // 退出登录
    logout() {
      this.token = '';
      this.user = null;
      localStorage.removeItem('token');
      console.log('用户已退出登录')
    },


    // 检查权限
    hasPermission(requiredRole) {
      if (!this.user) return false; // 如果用户未登录，则没有权限
      if (this.user.role === 'admin') return true; // 管理员拥有所有权限
      return this.user.role === requiredRole; // 检查用户角色是否匹配
    }
  }
});
