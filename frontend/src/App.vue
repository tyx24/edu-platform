<script setup>
import { onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowDown } from '@element-plus/icons-vue';
import { useAuthStore } from './store/auth';

const router = useRouter();
const route = useRoute();
const AuthStore = useAuthStore();

// 用户下拉菜单
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile');
      break;
    case 'logout':
      AuthStore.logout();
      ElMessage.success('已退出登录');
      router.push('/login');
      break;
  }
};

// 获取用户信息
onMounted(async () => {
  if (AuthStore.token && !AuthStore.user) {
    await AuthStore.fetchUserInfo();
  }
});

// 动态菜单配置
const menus = [
  { path: '/home', label: '首页', roles: ['student', 'teacher', 'admin'] },
  { path: '/courses', label: '课程中心', roles: ['student', 'teacher', 'admin'] },
  { path: '/my-courses', label: '我的课程', roles: ['student'] },
  { path: '/exam-center', label: '考试中心', roles: ['student', 'teacher'] },
  { path: '/homework-center', label: '作业中心', roles: ['student', 'teacher'] },
  { path: '/teacher', label: '教师中心', roles: ['teacher'] },
  { path: '/admin', label: '管理后台', roles: ['admin'] },
];
</script>

<template>
  <div id="app">
    <!-- 已登录状态 -->
    <el-container v-if="AuthStore.isAuthenticated">
      <el-header class="header">
        <div class="header-content">
          <!-- Logo -->
          <div class="logo">
            <h2>教育平台</h2>
          </div>

          <!-- 导航菜单 -->
          <el-menu mode="horizontal" :router="true" class="nav-menu" :default-active="route.path">
            <template v-for="item in menus" :key="item.path">
              <el-menu-item v-if="item.roles.some(role => AuthStore.hasPermission(role))"
                :index="item.path">
                {{ item.label }}
              </el-menu-item>
            </template>
          </el-menu>

          <!-- 用户下拉 -->
          <div class="user-info">
            <el-dropdown @command="handleCommand">
              <span class="user-dropdown">
                <img v-if="AuthStore.user?.avatar" :src="AuthStore.user.avatar" class="user-avatar" />
                <span v-else class="user-avatar-placeholder">{{ AuthStore.user?.username?.charAt(0)?.toUpperCase() }}</span>
                {{ AuthStore.user?.username || '用户' }}
                <el-icon>
                  <ArrowDown />
                </el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>

    <!-- 未登录状态 -->
    <router-view v-else />
  </div>
</template>

<style scoped>
/* 头部背景：渐变 + 模糊玻璃效果 */
.header {
  background: linear-gradient(135deg, rgba(102,126,234,0.9) 0%, rgba(118,75,162,0.9) 100%);
  backdrop-filter: blur(8px);
  color: white;
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

/* 头部布局 */
.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 30px;
}

/* Logo 优化 */
.logo h2 {
  margin: 0;
  color: white;
  font-weight: 700;
  font-size: 1.6rem;
  letter-spacing: 1px;
}

/* 菜单居中 */
.nav-menu {
  background: transparent;
  border: none;
  flex: 1;
  display: flex;
  justify-content: center;
  flex-wrap: wrap; /* 允许换行 */
}

/* 菜单项美化 */
.nav-menu .el-menu-item {
  color: white;
  border-radius: 8px;
  margin: 0 4px;
  padding: 0 16px;
  transition: all 0.3s ease;
}

/* 悬停与激活效果 */
.nav-menu .el-menu-item:hover,
.nav-menu .el-menu-item.is-active {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

/* 用户信息区 */
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 用户下拉样式 */
.user-dropdown {
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: transform 0.3s ease;
}



/* 悬停放大 */
.user-dropdown:hover {
  transform: scale(1.08);
}

/* 用户头像样式 */
.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}

/* 用户头像占位符样式（当没有头像时显示用户名首字母） */
.user-avatar-placeholder {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(255,255,255,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: #333;
}

/* 主内容区背景 */
.main-content {
  padding: 20px;
  background: linear-gradient(to bottom, #f9f9fc, #f5f7fa);
  min-height: calc(100vh - 60px);
}

/* 小屏幕优化 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 10px;
    height: auto;
    padding: 10px;
  }

  .nav-menu {
    justify-content: flex-start;
  }

  .nav-menu .el-menu-item {
    width: auto;
  }
}
</style>