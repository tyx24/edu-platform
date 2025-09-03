<script setup>
import { onMounted, ref, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowDown, User, SwitchButton, House, Reading, Document, EditPen, Management } from '@element-plus/icons-vue';
import { useAuthStore } from './store/auth';
import userinfoApi from './api/userinfo';


const router = useRouter();
const route = useRoute();
const AuthStore = useAuthStore();

// 用户下拉菜单
const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile');
      break;
    case 'logout':
      try {
        await AuthStore.logout();
        ElMessage.success('已退出登录');
        router.push('/login');
      } catch (error) {
        console.error('退出登录失败:', error);
        ElMessage.error('退出登录失败，请重试');
      }
      break;
  }
};

// 获取用户信息
onMounted(async () => {
  if (AuthStore.token && !AuthStore.user) {
    await AuthStore.fetchUserInfo();
    // 获取用户详细资料
    await fetchUserProfile();
  }
});

// 响应式变量
const loading = ref(false);
const userProfile = ref({});
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// 动态菜单配置
const menus = [
  { path: '/home', label: '首页', icon: 'House', roles: ['student', 'teacher', 'admin'] },
  { path: '/courses', label: '课程中心', icon: 'Reading', roles: ['student', 'teacher', 'admin'] },
  { path: '/my-courses', label: '我的课程', icon: 'Document', roles: ['student', 'teacher'] },
  { path: '/exam-center', label: '考试中心', icon: 'EditPen', roles: ['student', 'teacher'] },
  { path: '/homework-center', label: '作业中心', icon: 'Document', roles: ['student', 'teacher'] },
  { path: '/teacher', label: '教师中心', icon: 'User', roles: ['teacher'] },
  { path: '/admin', label: '管理后台', icon: 'Management', roles: ['admin'] },
];

// 获取当前页面标题
const getCurrentPageTitle = () => {
  const currentMenu = menus.find(menu => menu.path === route.path);
  return currentMenu ? currentMenu.label : '首页';
};

// 获取用户角色文本
const getUserRoleText = () => {
  const user = AuthStore.user;
  if (!user) return '用户';
  
  const roleMap = {
    'admin': '管理员',
    'teacher': '教师',
    'student': '学生'
  };
  
  return roleMap[user.role] || '用户';
};

// 获取用户资料
const fetchUserProfile = async () => {
  try {
    loading.value = true;
    // 获取详细用户信息（头像、真实姓名、联系方式等）
    const profileData = await userinfoApi.getUserProfile({ pageNum: 1, pageSize: 100 });

    // 从返回的数据中找到当前用户的详细信息
    let userDetailInfo = {};
    const currentUserId = AuthStore.user?.id;

    // 遍历数字索引找到匹配的用户信息
    for (let key in profileData.records) {
      if (typeof profileData.records[key] === 'object' && profileData.records[key].userId === currentUserId) {
        userDetailInfo = profileData.records[key];
        break;
      }
    }
    // 只保存详细用户信息
    userProfile.value = userDetailInfo || {};
  } catch (error) {
    console.error('获取用户资料失败:', error);
    ElMessage.error('获取用户资料失败');
  } finally {
    loading.value = false;
  }
};

// 处理头像URL
const avatarUrl = computed(() => {
  if (!userProfile.value.avatar) return defaultAvatar;
  const avatar = userProfile.value.avatar;
  // 如果是完整的URL（http或https开头），直接返回
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar;
  }
  // 如果已经有/edu前缀，直接返回
  if (avatar.startsWith('/edu')) {
    return avatar;
  }
  // 否则添加/edu前缀
  return `/edu${avatar}`;
});
</script>

<template>
  <div id="app">
    <!-- 宇宙科技背景 -->
    <div class="cosmic-background">
      <div class="floating-orb orb-1"></div>
      <div class="floating-orb orb-2"></div>
      <div class="floating-orb orb-3"></div>
      <div class="floating-orb orb-4"></div>
      <div class="tech-grid"></div>
    </div>

    <!-- 已登录状态 -->
    <el-container v-if="AuthStore.isAuthenticated" class="full-screen-container">
      <!-- 侧边导航栏 -->
      <el-aside class="sidebar glass-panel" width="280px">
        <!-- Logo区域 -->
        <div class="logo-section">
          <div class="logo-icon">
            <div class="logo-core">🎓</div>
            <div class="logo-ring"></div>
          </div>
          <div class="logo-content">
            <h2 class="logo-text">教育平台</h2>
            <div class="logo-subtitle">Education Platform</div>
          </div>
        </div>

        <!-- 导航菜单 -->
        <el-menu
          :default-active="route.path"
          class="sidebar-menu"
          :router="true"
          background-color="transparent"
          text-color="rgba(255, 255, 255, 0.8)"
          active-text-color="#ffffff"
        >
          <template v-for="(item, index) in menus" :key="item.path">
            <el-menu-item 
              v-if="item.roles.some(role => AuthStore.hasPermission(role))" 
              :index="item.path"
              class="menu-item"
              :style="{ '--delay': index * 0.1 + 's' }"
            >
              <div class="menu-content">
                <el-icon class="menu-icon">
                  <component :is="item.icon" />
                </el-icon>
                <span class="menu-text">{{ item.label }}</span>
                <div class="menu-glow"></div>
              </div>
            </el-menu-item>
          </template>
        </el-menu>

        <!-- 侧边栏装饰 -->
        <div class="sidebar-decoration">
          <div class="deco-line deco-1"></div>
          <div class="deco-line deco-2"></div>
          <div class="deco-line deco-3"></div>
        </div>
      </el-aside>

      <!-- 主要内容区域 -->
      <el-container class="main-container">
        <!-- 顶部导航栏 -->
        <el-header class="top-header glass-panel">
          <div class="header-left">
            <div class="page-indicator">
              <div class="indicator-dot active"></div>
              <div class="indicator-line"></div>
            </div>
            <el-breadcrumb separator="/" class="breadcrumb">
              <el-breadcrumb-item>
                <span class="breadcrumb-text">{{ getCurrentPageTitle() }}</span>
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          
          <div class="header-right">
            <!-- 用户信息区 -->
            <el-dropdown @command="handleCommand" class="user-dropdown">
              <div class="user-info glass-element">
                <div class="avatar-container">
                  <div class="avatar-ring"></div>
                  <img v-if="userProfile.avatar" :src="avatarUrl" class="user-avatar" />
                  <div v-else class="user-avatar-placeholder">
                    {{ AuthStore.user?.username?.charAt(0)?.toUpperCase() || 'U' }}
                  </div>
                  <div class="status-indicator online"></div>
                </div>
                <div class="user-details">
                  <span class="username">{{ AuthStore.user?.username || '用户' }}</span>
                  <span class="user-role" :class="'role-' + AuthStore.user?.role">{{ getUserRoleText() }}</span>
                </div>
                <el-icon class="dropdown-icon">
                  <ArrowDown />
                </el-icon>
                <div class="user-info-glow"></div>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="user-dropdown-menu glass-panel">
                  <el-dropdown-item command="profile" class="dropdown-item">
                    <el-icon><User /></el-icon>
                    <span>个人资料</span>
                    <div class="item-shine"></div>
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided class="dropdown-item logout">
                    <el-icon><SwitchButton /></el-icon>
                    <span>退出登录</span>
                    <div class="item-shine"></div>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 主内容区 -->
        <el-main class="main-content">
          <div class="content-wrapper">
            <router-view />
          </div>
        </el-main>
      </el-container>
    </el-container>

    <!-- 未登录状态 -->
    <div v-else class="auth-container">
      <router-view />
    </div>
  </div>
</template>

<style scoped>
/* ===== 全局样式重置 ===== */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  height: 100vh;
  width: 100vw;
  font-family: -apple-system, BlinkMacSystemFont, 'Inter', 'SF Pro Display', 'Helvetica Neue', Arial, sans-serif;
  position: relative;
  overflow: hidden;
}

/* ===== 宇宙科技背景 ===== */
.cosmic-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    radial-gradient(ellipse at top, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(ellipse at bottom, rgba(255, 119, 198, 0.2) 0%, transparent 50%),
    linear-gradient(135deg, #0f0f23 0%, #1a1a2e 25%, #16213e 75%, #0f3460 100%);
  z-index: -1;
  animation: cosmicShift 30s ease-in-out infinite;
}

@keyframes cosmicShift {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.floating-orb {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.3), transparent);
  backdrop-filter: blur(20px);
  animation: orbFloat 20s infinite ease-in-out;
}

.orb-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  left: 10%;
  background: radial-gradient(circle at 30% 30%, rgba(56, 189, 248, 0.3), transparent);
  animation-delay: 0s;
}

.orb-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: 15%;
  background: radial-gradient(circle at 30% 30%, rgba(139, 92, 246, 0.3), transparent);
  animation-delay: 5s;
}

.orb-3 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  left: 20%;
  background: radial-gradient(circle at 30% 30%, rgba(236, 72, 153, 0.3), transparent);
  animation-delay: 10s;
}

.orb-4 {
  width: 80px;
  height: 80px;
  top: 30%;
  left: 70%;
  background: radial-gradient(circle at 30% 30%, rgba(34, 197, 94, 0.3), transparent);
  animation-delay: 15s;
}

@keyframes orbFloat {
  0%, 100% {
    transform: translateX(0) translateY(0) rotate(0deg);
    opacity: 0.6;
  }
  25% {
    transform: translateX(30px) translateY(-30px) rotate(90deg);
    opacity: 0.8;
  }
  50% {
    transform: translateX(-20px) translateY(-50px) rotate(180deg);
    opacity: 1;
  }
  75% {
    transform: translateX(-40px) translateY(-20px) rotate(270deg);
    opacity: 0.7;
  }
}

.tech-grid {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(56, 189, 248, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(56, 189, 248, 0.1) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: gridMove 20s linear infinite;
  opacity: 0.3;
}

@keyframes gridMove {
  0% { transform: translate(0, 0); }
  100% { transform: translate(50px, 50px); }
}

/* ===== 玻璃拟态通用样式 ===== */
.glass-panel {
  background: 
    linear-gradient(135deg, 
      rgba(255, 255, 255, 0.1) 0%, 
      rgba(255, 255, 255, 0.05) 100%);
  backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.1),
    0 8px 32px rgba(56, 189, 248, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.glass-element {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

/* ===== 全屏容器 ===== */
.full-screen-container {
  height: 100vh;
  width: 100vw;
  position: relative;
  z-index: 1;
}

/* ===== 侧边栏科技化 ===== */
.sidebar {
  width: 280px;
  height: 100vh;
  position: relative;
  overflow: hidden;
  transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  border-right: 1px solid rgba(56, 189, 248, 0.3);
}

.sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    linear-gradient(180deg, 
      rgba(15, 15, 35, 0.95) 0%, 
      rgba(26, 26, 46, 0.9) 50%,
      rgba(22, 33, 62, 0.95) 100%);
  z-index: -1;
}

/* Logo区域科技化 */
.logo-section {
  display: flex;
  align-items: center;
  padding: 32px 24px;
  border-bottom: 1px solid rgba(56, 189, 248, 0.2);
  margin-bottom: 24px;
  position: relative;
}

.logo-icon {
  position: relative;
  margin-right: 16px;
}

.logo-core {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #38bdf8, #8b5cf6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  box-shadow: 
    0 8px 25px rgba(56, 189, 248, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  animation: logoFloat 3s ease-in-out infinite;
  position: relative;
  z-index: 2;
}

@keyframes logoFloat {
  0%, 100% {
    transform: translateY(0) scale(1);
    box-shadow: 0 8px 25px rgba(56, 189, 248, 0.4);
  }
  50% {
    transform: translateY(-5px) scale(1.05);
    box-shadow: 0 12px 35px rgba(56, 189, 248, 0.6);
  }
}

.logo-ring {
  position: absolute;
  top: -5px;
  left: -5px;
  right: -5px;
  bottom: -5px;
  border: 2px solid transparent;
  border-radius: 50%;
  background: linear-gradient(45deg, #38bdf8, #8b5cf6, #ec4899, #38bdf8);
  background-size: 300% 300%;
  animation: ringRotate 4s linear infinite;
  z-index: 1;
}

@keyframes ringRotate {
  0% { background-position: 0% 50%; }
  100% { background-position: 100% 50%; }
}

.logo-content {
  flex: 1;
}

.logo-text {
  color: rgba(255, 255, 255, 0.95);
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 0.5px;
  margin: 0 0 4px 0;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  background: linear-gradient(135deg, #ffffff, #38bdf8);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.logo-subtitle {
  color: rgba(56, 189, 248, 0.8);
  font-size: 11px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 2px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

/* 侧边栏菜单科技化 */
.sidebar-menu {
  border: none;
  background: transparent;
  padding: 0 20px 40px;
}

.menu-item {
  margin-bottom: 12px;
  border-radius: 16px;
  position: relative;
  overflow: hidden;
  animation: menuSlideIn 0.6s ease-out var(--delay, 0s) both;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes menuSlideIn {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.menu-content {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  position: relative;
  transition: all 0.4s ease;
}

.menu-item:hover .menu-content {
  background: rgba(56, 189, 248, 0.15);
  border-color: rgba(56, 189, 248, 0.3);
  transform: translateX(8px) scale(1.02);
  box-shadow: 
    0 8px 25px rgba(56, 189, 248, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.menu-item.is-active .menu-content {
  background: 
    linear-gradient(135deg, 
      rgba(56, 189, 248, 0.2) 0%, 
      rgba(139, 92, 246, 0.15) 100%);
  border-color: rgba(56, 189, 248, 0.5);
  box-shadow: 
    0 12px 35px rgba(56, 189, 248, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.menu-icon {
  margin-right: 16px;
  font-size: 20px;
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
  position: relative;
  z-index: 2;
}

.menu-item:hover .menu-icon,
.menu-item.is-active .menu-icon {
  color: #ffffff;
  transform: scale(1.1);
  filter: drop-shadow(0 0 8px rgba(56, 189, 248, 0.6));
}

.menu-text {
  font-size: 15px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
  flex: 1;
  position: relative;
  z-index: 2;
}

.menu-item:hover .menu-text,
.menu-item.is-active .menu-text {
  color: #ffffff;
  text-shadow: 0 0 10px rgba(56, 189, 248, 0.5);
}

.menu-glow {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(56, 189, 248, 0.3), 
    transparent);
  transition: left 0.6s;
  z-index: 1;
}

.menu-item:hover .menu-glow {
  left: 100%;
}

/* 侧边栏装饰 */
.sidebar-decoration {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 80px;
  pointer-events: none;
}

.deco-line {
  position: absolute;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(56, 189, 248, 0.5), 
    transparent);
  height: 2px;
  animation: decoFlow 3s ease-in-out infinite;
}

.deco-1 {
  bottom: 60px;
  left: 20px;
  right: 40px;
  animation-delay: 0s;
}

.deco-2 {
  bottom: 40px;
  left: 40px;
  right: 20px;
  animation-delay: 1s;
}

.deco-3 {
  bottom: 20px;
  left: 30px;
  right: 30px;
  animation-delay: 2s;
}

@keyframes decoFlow {
  0%, 100% {
    opacity: 0.3;
    transform: scaleX(0.8);
  }
  50% {
    opacity: 0.8;
    transform: scaleX(1);
  }
}

/* ===== 主容器科技化 ===== */
.main-container {
  height: 100vh;
  background: transparent;
  position: relative;
}

/* 顶部导航栏科技化 */
.top-header {
  height: 80px;
  padding: 0 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 100;
  border-bottom: 1px solid rgba(56, 189, 248, 0.2);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.page-indicator {
  display: flex;
  align-items: center;
  gap: 12px;
}

.indicator-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, #38bdf8, #8b5cf6);
  box-shadow: 0 0 15px rgba(56, 189, 248, 0.6);
  animation: dotPulse 2s ease-in-out infinite;
}

@keyframes dotPulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 0 15px rgba(56, 189, 248, 0.6);
  }
  50% {
    transform: scale(1.2);
    box-shadow: 0 0 25px rgba(56, 189, 248, 0.8);
  }
}

.indicator-line {
  width: 40px;
  height: 2px;
  background: linear-gradient(90deg, #38bdf8, transparent);
  animation: lineFlow 2s ease-in-out infinite;
}

@keyframes lineFlow {
  0%, 100% {
    opacity: 0.5;
    transform: scaleX(0.8);
  }
  50% {
    opacity: 1;
    transform: scaleX(1);
  }
}

.breadcrumb {
  background: none;
  border: none;
}

.breadcrumb-text {
  font-size: 18px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.95);
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  background: linear-gradient(135deg, #ffffff, #38bdf8);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

/* 用户下拉科技化 */
.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 20px;
  border-radius: 20px;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
  overflow: hidden;
}

.user-info:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 
    0 12px 30px rgba(56, 189, 248, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.avatar-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-ring {
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  border-radius: 50%;
  background: linear-gradient(45deg, #38bdf8, #8b5cf6, #ec4899, #38bdf8);
  background-size: 300% 300%;
  animation: avatarRingRotate 4s linear infinite;
  z-index: 1;
}

@keyframes avatarRingRotate {
  0% { background-position: 0% 50%; }
  100% { background-position: 100% 50%; }
}

.user-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.3);
  box-shadow: 
    0 8px 25px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  transition: all 0.4s ease;
  position: relative;
  z-index: 2;
}

.user-avatar:hover {
  transform: scale(1.05);
  box-shadow: 
    0 12px 35px rgba(56, 189, 248, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.user-avatar-placeholder {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #38bdf8, #1d4ed8);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  color: #ffffff;
  font-size: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  box-shadow: 
    0 8px 25px rgba(56, 189, 248, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 2;
}

.status-indicator {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.8);
  z-index: 3;
}

.status-indicator.online {
  background: #10b981;
  box-shadow: 
    0 0 0 2px rgba(16, 185, 129, 0.3),
    0 0 15px rgba(16, 185, 129, 0.5);
  animation: statusPulse 2s ease-in-out infinite;
}

@keyframes statusPulse {
  0%, 100% {
    box-shadow: 
      0 0 0 2px rgba(16, 185, 129, 0.3),
      0 0 15px rgba(16, 185, 129, 0.5);
  }
  50% {
    box-shadow: 
      0 0 0 4px rgba(16, 185, 129, 0.5),
      0 0 25px rgba(16, 185, 129, 0.8);
  }
}

.user-details {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}

.username {
  font-size: 15px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.95);
  line-height: 1.2;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
}

.user-role {
  font-size: 11px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 10px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  line-height: 1;
}

.role-student {
  background: linear-gradient(135deg, #38bdf8, #0ea5e9);
  color: white;
  box-shadow: 0 2px 8px rgba(56, 189, 248, 0.3);
}

.role-teacher {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
  color: white;
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.3);
}

.role-admin {
  background: linear-gradient(135deg, #ec4899, #db2777);
  color: white;
  box-shadow: 0 2px 8px rgba(236, 72, 153, 0.3);
}

.dropdown-icon {
  color: rgba(255, 255, 255, 0.7);
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  font-size: 16px;
}

.user-info:hover .dropdown-icon {
  color: rgba(255, 255, 255, 0.9);
  transform: rotate(180deg) scale(1.1);
}

.user-info-glow {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(56, 189, 248, 0.2), 
    transparent);
  transition: left 0.8s;
}

.user-info:hover .user-info-glow {
  left: 100%;
}

/* 用户下拉菜单科技化 */
.user-dropdown-menu {
  border-radius: 20px;
  padding: 12px;
  border: 1px solid rgba(56, 189, 248, 0.3);
  box-shadow: 
    0 20px 50px rgba(0, 0, 0, 0.3),
    0 8px 32px rgba(56, 189, 248, 0.2);
  backdrop-filter: blur(25px);
  background: 
    linear-gradient(135deg, 
      rgba(15, 15, 35, 0.95) 0%, 
      rgba(26, 26, 46, 0.9) 100%);
}

.dropdown-item {
  border-radius: 12px;
  padding: 16px 20px;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  display: flex;
  align-items: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.8);
  position: relative;
  overflow: hidden;
  font-weight: 600;
}

.dropdown-item:hover {
  background: rgba(56, 189, 248, 0.15);
  color: rgba(255, 255, 255, 0.95);
  transform: translateX(4px);
  box-shadow: 
    0 4px 15px rgba(56, 189, 248, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.dropdown-item.logout:hover {
  background: rgba(239, 68, 68, 0.15);
  color: #fca5a5;
}

.item-shine {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(255, 255, 255, 0.1), 
    transparent);
  transition: left 0.6s;
}

.dropdown-item:hover .item-shine {
  left: 100%;
}

/* ===== 主内容区域科技化 ===== */
.main-content {
  padding: 0;
  height: calc(100vh - 80px);
  overflow-y: auto;
  overflow-x: hidden;
  background: transparent;
  position: relative;
}

.content-wrapper {
  padding: 32px 40px;
  min-height: 100%;
  background: transparent;
  position: relative;
  animation: contentFadeIn 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes contentFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ===== 未登录状态科技化 ===== */
.auth-container {
  height: 100vh;
  width: 100vw;
  background: 
    linear-gradient(135deg, 
      rgba(15, 15, 35, 0.95) 0%, 
      rgba(26, 26, 46, 0.9) 25%,
      rgba(22, 33, 62, 0.95) 75%, 
      rgba(15, 52, 96, 0.9) 100%),
    radial-gradient(ellipse at top, rgba(56, 189, 248, 0.2) 0%, transparent 50%),
    radial-gradient(ellipse at bottom, rgba(139, 92, 246, 0.2) 0%, transparent 50%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.auth-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    radial-gradient(circle at 25% 25%, rgba(56, 189, 248, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 75% 75%, rgba(139, 92, 246, 0.1) 0%, transparent 50%);
  animation: authBackgroundFlow 20s ease-in-out infinite;
}

@keyframes authBackgroundFlow {
  0%, 100% {
    transform: scale(1) rotate(0deg);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.1) rotate(180deg);
    opacity: 0.8;
  }
}

/* ===== 滚动条科技化 ===== */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, 
    rgba(56, 189, 248, 0.6) 0%, 
    rgba(139, 92, 246, 0.6) 100%);
  border-radius: 4px;
  transition: all 0.3s ease;
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, 
    rgba(56, 189, 248, 0.8) 0%, 
    rgba(139, 92, 246, 0.8) 100%);
  box-shadow: 0 0 10px rgba(56, 189, 248, 0.5);
}

/* ===== 响应式设计 ===== */
@media (max-width: 1400px) {
  .sidebar {
    width: 260px;
  }
  
  .content-wrapper {
    padding: 28px 32px;
  }
}

@media (max-width: 1200px) {
  .sidebar {
    width: 240px;
  }
  
  .logo-text {
    font-size: 20px;
  }
  
  .content-wrapper {
    padding: 24px 28px;
  }
  
  .top-header {
    padding: 0 32px;
  }
}

@media (max-width: 1024px) {
  .sidebar {
    width: 220px;
  }
  
  .logo-section {
    padding: 24px 20px;
  }
  
  .logo-text {
    font-size: 18px;
  }
  
  .logo-subtitle {
    font-size: 10px;
  }
  
  .menu-content {
    padding: 14px 16px;
  }
  
  .menu-text {
    font-size: 14px;
  }
}

@media (max-width: 768px) {
  .sidebar {
    width: 200px;
  }
  
  .top-header {
    height: 70px;
    padding: 0 24px;
  }
  
  .header-right {
    gap: 16px;
  }
  
  .user-details {
    display: none;
  }
  
  .content-wrapper {
    padding: 20px 24px;
  }
  
  .main-content {
    height: calc(100vh - 70px);
  }
  
  .breadcrumb-text {
    font-size: 16px;
  }
}

@media (max-width: 640px) {
  .sidebar {
    width: 180px;
  }
  
  .logo-section {
    padding: 20px 16px;
  }
  
  .logo-core {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
  
  .logo-text {
    font-size: 16px;
  }
  
  .menu-content {
    padding: 12px 14px;
  }
  
  .menu-text {
    font-size: 13px;
  }
  
  .menu-icon {
    font-size: 18px;
  }
  
  .top-header {
    height: 60px;
    padding: 0 20px;
  }
  
  .content-wrapper {
    padding: 16px 20px;
  }
  
  .main-content {
    height: calc(100vh - 60px);
  }
}

@media (max-width: 480px) {
  .sidebar {
    width: 160px;
  }
  
  .logo-section {
    flex-direction: column;
    gap: 8px;
    text-align: center;
    padding: 16px 12px;
  }
  
  .logo-content {
    flex: none;
  }
  
  .logo-text {
    font-size: 14px;
  }
  
  .logo-subtitle {
    font-size: 8px;
  }
  
  .menu-content {
    flex-direction: column;
    align-items: center;
    padding: 10px 8px;
    gap: 6px;
  }
  
  .menu-icon {
    margin-right: 0;
    font-size: 16px;
  }
  
  .menu-text {
    font-size: 11px;
    text-align: center;
  }
  
  .page-indicator {
    display: none;
  }
  
  .breadcrumb-text {
    font-size: 14px;
  }
}

/* ===== Element Plus 组件样式覆盖 ===== */
.el-menu {
  border-right: none !important;
  background-color: transparent !important;
}

.el-menu-item {
  border-radius: 16px !important;
  margin-bottom: 8px !important;
  background: transparent !important;
  border: none !important;
  padding: 0 !important;
}

.el-menu-item:hover {
  background-color: transparent !important;
}

.el-menu-item.is-active {
  background-color: transparent !important;
}

.el-breadcrumb__inner {
  color: rgba(255, 255, 255, 0.95) !important;
  font-weight: 700 !important;
}

.el-dropdown-menu {
  border: none !important;
  box-shadow: none !important;
}

/* ===== 动画效果 ===== */
@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-10px) rotate(180deg);
  }
}

@keyframes glow {
  0%, 100% {
    box-shadow: 0 0 20px rgba(56, 189, 248, 0.5);
  }
  50% {
    box-shadow: 0 0 30px rgba(56, 189, 248, 0.8);
  }
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

/* ===== 全局动画类 ===== */
.animate-float {
  animation: float 6s ease-in-out infinite;
}

.animate-glow {
  animation: glow 2s ease-in-out infinite;
}

.animate-shimmer {
  background: linear-gradient(90deg, 
    transparent, 
    rgba(255, 255, 255, 0.4), 
    transparent);
  background-size: 200% 100%;
  animation: shimmer 2s infinite;
}
</style>