<template>
  <div class="home-container">
    <!-- 动态背景层 -->
    <div class="cosmic-background">
      <div class="floating-orb orb-1"></div>
      <div class="floating-orb orb-2"></div>
      <div class="floating-orb orb-3"></div>
      <div class="floating-orb orb-4"></div>
    </div>

    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-background">
        <div class="tech-grid"></div>
        <div class="geometric-shape shape-1"></div>
        <div class="geometric-shape shape-2"></div>
        <div class="geometric-shape shape-3"></div>
      </div>
      
      <div class="welcome-content">
        <div class="welcome-header">
          <h1 class="welcome-title glitch-effect" data-text="欢迎来到教育平台">
            欢迎来到 <span class="brand-highlight">教育平台</span> 🎓
          </h1>
          <p class="welcome-subtitle">
            <span class="typing-effect">在这里开启您的学习之旅，探索无限知识的奥秘</span>
          </p>
        </div>
        
        <div class="quick-actions">
          <el-button 
            type="primary" 
            size="large" 
            class="action-button primary-action"
            @click="$router.push('/courses')"
            :icon="Reading"
          >
            <span>浏览课程</span>
            <div class="button-ripple"></div>
          </el-button>
          <el-button 
            size="large" 
            class="action-button secondary-action"
            @click="$router.push('/my-courses')"
            :icon="Collection"
            plain
          >
            <span>我的课程</span>
            <div class="button-ripple"></div>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <el-row :gutter="32">
        <!-- 左侧主体内容 -->
        <el-col :lg="16" :md="24">
          <!-- 数据统计卡片 -->
          <div class="stats-section">
            <h2 class="section-title">
              <div class="title-icon">📊</div>
              <span>数据统计</span>
              <div class="title-decoration"></div>
            </h2>
            <el-row :gutter="20">
              <el-col 
                v-for="(item, index) in statCards" 
                :key="item.label" 
                :xs="24" 
                :sm="8" 
                :md="8"
              >
                <div class="stat-card glass-card" :class="item.color" :style="{ '--delay': index * 0.1 + 's' }">
                  <div class="stat-icon">{{ item.icon }}</div>
                  <div class="stat-info">
                    <div class="stat-number" :data-value="item.value">{{ item.value }}</div>
                    <div class="stat-label">{{ item.label }}</div>
                  </div>
                  <div class="stat-progress"></div>
                  <div class="hologram-effect"></div>
                </div>
              </el-col>
            </el-row>
          </div>

          <!-- 热门课程推荐 -->
          <div class="recommend-section">
            <h2 class="section-title">
              <div class="title-icon">🌟</div>
              <span>热门课程推荐</span>
              <div class="title-decoration"></div>
            </h2>
            
            <el-skeleton v-if="loading" :rows="3" animated class="course-skeleton" />
            
            <div v-else-if="hotCourses.length === 0" class="empty-state glass-card">
              <el-empty description="暂无课程推荐" />
            </div>
            
            <div v-else class="courses-grid">
              <div 
                v-for="(course, index) in hotCourses" 
                :key="course.id"
                class="course-card glass-card"
                :style="{ '--delay': index * 0.1 + 's' }"
                @click="$router.push('/courses/' + course.id)"
              >
                <div class="course-image">
                  <img :src="course.coverUrl" :alt="course.title" class="course-cover" />
                  <div class="course-overlay">
                    <el-button type="primary" size="small" class="view-course-btn">
                      <span>查看详情</span>
                      <div class="btn-glow"></div>
                    </el-button>
                  </div>
                  <div class="course-badge">热门</div>
                </div>
                
                <div class="course-content">
                  <h3 class="course-title">{{ course.title }}</h3>
                  <p class="course-description">{{ course.description }}</p>
                  
                  <div class="course-meta">
                    <div class="course-teacher">
                      <i class="el-icon-user"></i>
                      {{ course.teacher }}
                    </div>
                    <div class="course-rating">
                      <el-rate 
                        :model-value="course.rating / 2" 
                        disabled 
                        size="small"
                        text-color="#ffa500"
                      />
                      <span class="rating-score">{{ course.rating.toFixed(1) }}</span>
                    </div>
                  </div>
                </div>
                <div class="card-reflection"></div>
              </div>
            </div>
          </div>
        </el-col>

        <!-- 右侧侧边栏 -->
        <el-col :lg="8" :md="24">
          <div class="sidebar">
            <!-- 用户信息卡片 -->
            <div class="user-info-card glass-card">
              <div class="card-header">
                <h3>个人信息</h3>
                <div class="header-decoration"></div>
              </div>
              
              <div v-if="authStore.isAuthenticated" class="user-info">
                <div class="user-avatar-wrapper">
                  <div class="avatar-ring"></div>
                  <img 
                    v-if="userProfile.avatar" 
                    :src="avatarUrl" 
                    class="user-avatar"
                    :alt="user?.username"
                  />
                  <div v-else class="user-avatar-placeholder">
                    {{ user?.username?.charAt(0)?.toUpperCase() || 'U' }}
                  </div>
                  <div class="status-indicator online"></div>
                </div>
                
                <div class="user-details">
                  <h4 class="username">{{ user?.username }}</h4>
                  <span class="user-role" :class="'role-' + user?.role">{{ roleText }}</span>
                </div>
                
                <el-button 
                  type="primary" 
                  plain 
                  size="small" 
                  @click="$router.push('/profile')"
                  class="profile-btn"
                >
                  <span>查看资料</span>
                  <div class="btn-particles"></div>
                </el-button>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- AI聊天助手 -->
    <div class="chat-assistant">
      <el-button 
        class="chat-float-btn" 
        circle 
        type="primary" 
        @click="toggleChat"
      >
        <span class="chat-icon">🤖</span>
        <div class="pulse-ring"></div>
      </el-button>

      <transition name="slide-up">
        <div v-if="showChat" class="chat-window glass-card">
          <div class="chat-header">
            <div class="ai-avatar">🤖</div>
            <span>AI 学习助手</span>
            <el-button link @click="toggleChat" class="close-btn">✕</el-button>
          </div>

          <div class="chat-body">
            <div 
              v-for="(msg, index) in messages" 
              :key="index" 
              :class="['chat-message', msg.role]"
            >
              <span v-if="!msg.loading">{{ msg.text }}</span>
              <span v-else class="loading-dots">
                AI 正在思考<span>.</span><span>.</span><span>.</span>
              </span>
            </div>
          </div>

          <div class="chat-footer">
            <el-input 
              v-model="input" 
              placeholder="请输入问题..." 
              @keyup.enter="sendMessage()"
              class="chat-input"
            />
            <el-button type="primary" @click="sendMessage()" class="send-btn">
              发送
            </el-button>
          </div>

          <div class="faq-section">
            <span class="faq-label">常见问题：</span>
            <el-button 
              v-for="(q, i) in faqs" 
              :key="i" 
              size="small" 
              @click="sendMessage(q)"
              class="faq-btn"
            >
              {{ q }}
            </el-button>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { Collection, Reading } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../store/auth';
import { courseApi } from '../api/course';
import { enrollmentApi } from '../api/enrollment';
import { userApi } from '../api/user';
import { userinfoApi } from '../api/userinfo';
import { examApi } from '../api/exam';
import { homeworkApi } from '../api/homework';
import { commentApi } from '../api/comment';

const authStore = useAuthStore();
const { user } = authStore;

const stats = ref({});
const hotCourses = ref([]);
const loading = ref(true);
const progress = ref(0);
const userProfile = ref({});
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

const statCards = computed(() => {
  const role = user?.role;
  const { value } = stats;
  const cards = {
    student: [
      { icon: "📚", value: value.myCourses || 0, label: "已选课程", color: "stat-blue" },
      { icon: "⏳", value: value.progress || "0%", label: "学习进度", color: "stat-green" },
      { icon: "🏆", value: value.exams || 0, label: "已完成考试", color: "stat-orange" }
    ],
    teacher: [
      { icon: "📖", value: value.publishedCourses || 0, label: "已发布课程", color: "stat-blue" },
      { icon: "👥", value: value.students || 0, label: "学生人数", color: "stat-green" },
      { icon: "📝", value: value.pending || 0, label: "待批改作业", color: "stat-orange" }
    ],
    admin: [
      { icon: "📚", value: value.courseCount || 0, label: "总课程数", color: "stat-blue" },
      { icon: "👥", value: value.userCount || 0, label: "注册用户", color: "stat-green" },
      { icon: "🏆", value: value.examCount || 0, label: "考试总数", color: "stat-orange" }
    ]
  };
  return cards[role] || [];
});

const roleText = computed(() => {
  return { student: "学生", teacher: "教师", admin: "管理员" }[user?.role] || "未知角色";
});

// 获取用户资料
const fetchUserProfile = async () => {
  try {
    loading.value = true;
    const profileData = await userinfoApi.getUserProfile({ pageNum: 1, pageSize: 100 });
    let userDetailInfo = {};
    const currentUserId = authStore.user?.id;

    for (let key in profileData.records) {
      if (typeof profileData.records[key] === 'object' && profileData.records[key].userId === currentUserId) {
        userDetailInfo = profileData.records[key];
        break;
      }
    }
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
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar;
  }
  if (avatar.startsWith('/edu')) {
    return avatar;
  }
  return `/edu${avatar}`;
});

// AI聊天功能
const showChat = ref(false);
const input = ref("");
const messages = ref([
  { role: "ai", text: "你好，我是你的学习助手！有什么问题随时问我～" },
]);

const faqs = ["如何进入下一章节？", "考试时间是什么时候？", "如何查看学习进度？"];

const toggleChat = () => {
  showChat.value = !showChat.value;
};

const sendMessage = async (text) => {
  const userMsg = typeof text === "string" ? text : input.value.trim();
  if (!userMsg) return;

  messages.value.push({ role: "user", text: userMsg });
  input.value = "";

  const loadingMsg = { role: "ai", text: "AI 正在思考...", loading: true };
  messages.value.push(loadingMsg);
  const loadingIndex = messages.value.length - 1;

  try {
    const url = "https://api.siliconflow.cn/v1/chat/completions";
    const options = {
      method: "POST",
      headers: {
        Authorization: "Bearer sk-ghfcmenqyzgnaawsnjmlowebfqztmuhwgawhoucmntybfapb",
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        model: "deepseek-ai/DeepSeek-R1",
        max_tokens: 512,
        enable_thinking: true,
        thinking_budget: 4096,
        min_p: 0.05,
        temperature: 0.7,
        top_p: 0.7,
        top_k: 50,
        frequency_penalty: 0.5,
        n: 1,
        messages: [{ role: "user", content: userMsg }],
      }),
    };

    const response = await fetch(url, options);
    const data = await response.json();

    const reply = data?.choices?.[0]?.message?.content || "对不起，AI 没有返回结果，请稍后再试。";
    messages.value[loadingIndex] = { role: "ai", text: reply, loading: false };
  } catch (error) {
    console.error(error);
    messages.value[loadingIndex] = {
      role: "ai",
      text: "请求失败，请检查网络或API配置。",
      loading: false,
    };
  }
};

onMounted(async () => {
  try {
    await fetchUserProfile();
    
    const role = user?.role;
    if (role === "student") {
      const myCourses = await enrollmentApi.getMyCourses();
      let examCount = 0;
      const myEnrollments = await enrollmentApi.getMyEnrollments();
      const totalCourses = myEnrollments.length;
      if (totalCourses === 0) {
        progress.value = 0;
      } else {
        const totalProgress = myEnrollments.reduce((sum, course) => sum + course.progress, 0);
        progress.value = totalProgress / totalCourses;
      }

      for (const course of myCourses || []) {
        const exams = await examApi.getCourseExams(course.id);
        for (const ex of exams.data || []) {
          const record = await examApi.getMyExamRecord(ex.id);
          examCount += (record.data || []).filter(s => !s.totalScore).length;
        }
      }
      stats.value = { myCourses: myCourses?.length || 0, progress: progress.value + "%", exams: examCount };
    } else if (role === "teacher") {
      const myCourses = await enrollmentApi.getMyCoursesWithStudents();
      let studentTotal = 0, pending = 0;
      for (const c of myCourses || []) {
        studentTotal += c.students?.length || 0;
        const homeworks = await homeworkApi.getCourseHomework(c.course.id);
        for (const h of homeworks.data || []) {
          const subs = await homeworkApi.getHomeworkSubmissions(h.id);
          pending += (subs.data || []).filter(s => !s.score).length;
        }
      }
      stats.value = { publishedCourses: myCourses?.length || 0, students: studentTotal, pending };
    } else if (role === "admin") {
      const courseList = await courseApi.getCourseList({ pageNum: 1, pageSize: 10 });
      const userList = await userApi.getUserList({ pageNum: 1, pageSize: 10 },'');
      let examTotal = 0;
      const courseAll = await courseApi.getCourseList({ pageNum: 1, pageSize: 100 });
      for (const c of courseAll?.records || []) {
        const exams = await examApi.getCourseExams(c.id);
        examTotal += exams.data?.length || 0;
      }
      stats.value = { courseCount: courseList?.total || 0, userCount: userList?.total || 0, examCount: examTotal };
    }

    const listRes = await courseApi.getCourseList({ pageNum: 1, pageSize: 100, state: 1 });
    const allCourses = listRes?.records || [];
    if (allCourses.length > 0) {
      // 获取所有课程的详细信息和评分
      const detailRes = await Promise.all(allCourses.map(c => courseApi.getCourseDetail(c.id)));
      
      // 按评分降序排序，取前6个
      const sortedByRating = detailRes
        .sort((a, b) => (b.avgRating || 0) - (a.avgRating || 0))
        .slice(0, 6);

      const teacherIds = sortedByRating.map(r => r.course.teacherId).filter(id => id);
      const teacherInfos = {};

      if (teacherIds.length > 0) {
        const teacherResults = await Promise.all(
          teacherIds.map(id => userApi.getUserDetail(id).catch(() => null))
        );

        teacherIds.forEach((id, index) => {
          if (teacherResults[index]) {
            teacherInfos[id] = teacherResults[index].username || "未知讲师";
          }
        });
      }

      hotCourses.value = sortedByRating.map((r => {
        return {
          id: r.course.id,
          title: r.course.title,
          description: r.course.description,
          teacher: r.course.teacherId ? (teacherInfos[r.course.teacherId] || "未知讲师") : "未知讲师",
          coverUrl: r.course.coverUrl || "https://placehold.co/300x160?text=Course",
          rating: r.avgRating || 0
        };
      }));
    }
  } catch (err) {
    console.error("首页数据加载失败:", err);
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
/* ===== 全局容器样式 ===== */
.home-container {
  min-height: 100vh;
  background: 
    radial-gradient(ellipse at top, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(ellipse at bottom, rgba(255, 119, 198, 0.2) 0%, transparent 50%),
    linear-gradient(135deg, #0f0f23 0%, #1a1a2e 25%, #16213e 75%, #0f3460 100%);
  background-attachment: fixed;
  position: relative;
  overflow-x: hidden;
}

/* ===== 动态宇宙背景 ===== */
.cosmic-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.floating-orb {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.4), transparent);
  backdrop-filter: blur(20px);
  animation: float 20s infinite ease-in-out;
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
  top: 40%;
  left: 60%;
  background: radial-gradient(circle at 30% 30%, rgba(34, 197, 94, 0.3), transparent);
  animation-delay: 15s;
}

@keyframes float {
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

/* ===== 欢迎区域 ===== */
.welcome-section {
  position: relative;
  background: 
    linear-gradient(135deg, rgba(15, 15, 35, 0.95) 0%, rgba(26, 26, 46, 0.9) 100%),
    linear-gradient(45deg, rgba(56, 189, 248, 0.1) 0%, rgba(139, 92, 246, 0.1) 100%);
  backdrop-filter: blur(20px) saturate(180%);
  padding: 120px 60px;
  margin-bottom: 60px;
  border-radius: 0 0 40px 40px;
  overflow: hidden;
  color: white;
  z-index: 10;
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.3),
    0 8px 32px rgba(56, 189, 248, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.welcome-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
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
}

@keyframes gridMove {
  0% { transform: translate(0, 0); }
  100% { transform: translate(50px, 50px); }
}

.geometric-shape {
  position: absolute;
  background: linear-gradient(45deg, 
    rgba(56, 189, 248, 0.2) 0%, 
    rgba(139, 92, 246, 0.2) 50%, 
    rgba(236, 72, 153, 0.2) 100%);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  animation: float 25s infinite ease-in-out;
}

.shape-1 {
  width: 200px;
  height: 200px;
  top: -50px;
  right: 10%;
  clip-path: polygon(50% 0%, 100% 50%, 50% 100%, 0% 50%);
  animation-delay: 0s;
}

.shape-2 {
  width: 150px;
  height: 150px;
  bottom: -30px;
  left: 15%;
  border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%;
  animation-delay: 8s;
}

.shape-3 {
  width: 100px;
  height: 100px;
  top: 60%;
  right: 20%;
  clip-path: polygon(25% 0%, 75% 0%, 100% 50%, 75% 100%, 25% 100%, 0% 50%);
  animation-delay: 15s;
}

.welcome-content {
  position: relative;
  z-index: 10;
  text-align: center;
  max-width: 900px;
  margin: 0 auto;
}

.welcome-title {
  font-size: 4rem;
  font-weight: 900;
  margin-bottom: 32px;
  letter-spacing: -2px;
  line-height: 1.1;
  color: #ffffff;
  text-shadow: 
    0 2px 4px rgba(0, 0, 0, 0.8),
    0 4px 12px rgba(0, 0, 0, 0.6),
    0 0 20px rgba(56, 189, 248, 0.4);
  animation: fadeInDown 1s ease-out;
  position: relative;
  z-index: 20;
}

.glitch-effect {
  position: relative;
}


.glitch-effect::before {
  animation: glitch-1 3s infinite;
  color: rgba(255, 0, 128, 0.7);
}

.glitch-effect::after {
  animation: glitch-2 3s infinite;
  color: rgba(0, 255, 255, 0.7);
}

@keyframes glitch-1 {
  0%, 14%, 15%, 49%, 50%, 99%, 100% {
    transform: translate(0);
    opacity: 0;
  }
  15%, 49% {
    transform: translate(-2px, 2px);
    opacity: 0.3;
  }
}

@keyframes glitch-2 {
  0%, 20%, 21%, 62%, 63%, 99%, 100% {
    transform: translate(0);
    opacity: 0;
  }
  21%, 62% {
    transform: translate(2px, -2px);
    opacity: 0.3;
  }
}

.brand-highlight {
  color: #fbbf24;
  text-shadow: 
    0 2px 4px rgba(0, 0, 0, 0.8),
    0 4px 12px rgba(0, 0, 0, 0.6),
    0 0 25px rgba(251, 191, 36, 0.6);
  animation: brandGlow 2s ease-in-out infinite alternate;
}

@keyframes brandGlow {
  from {
    text-shadow: 
      0 2px 4px rgba(0, 0, 0, 0.8),
      0 4px 12px rgba(0, 0, 0, 0.6),
      0 0 25px rgba(251, 191, 36, 0.6);
  }
  to {
    text-shadow: 
      0 2px 4px rgba(0, 0, 0, 0.8),
      0 4px 12px rgba(0, 0, 0, 0.6),
      0 0 35px rgba(251, 191, 36, 0.8),
      0 0 50px rgba(251, 191, 36, 0.4);
  }
}

.welcome-subtitle {
  font-size: 1.5rem;
  margin-bottom: 48px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.95);
  text-shadow: 
    0 2px 4px rgba(0, 0, 0, 0.8),
    0 4px 8px rgba(0, 0, 0, 0.4);
  animation: fadeInUp 1s ease-out 0.3s both;
  position: relative;
  z-index: 20;
  font-weight: 500;
}

.typing-effect {
  display: inline-block;
  position: relative;
}

.typing-effect::after {
  content: '|';
  font-weight: 100;
  color: #38bdf8;
  animation: blink 1s infinite;
  text-shadow: 0 0 10px rgba(56, 189, 248, 0.8);
}

.quick-actions {
  display: flex;
  gap: 32px;
  justify-content: center;
  animation: fadeInUp 1s ease-out 0.6s both;
}

.action-button {
  height: 64px;
  padding: 0 40px;
  border-radius: 16px;
  font-size: 18px;
  font-weight: 700;
  position: relative;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  backdrop-filter: blur(10px);
}

.action-button span {
  position: relative;
  z-index: 2;
}

.button-ripple {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.action-button:hover .button-ripple {
  width: 300px;
  height: 300px;
}

.primary-action {
  background: 
    linear-gradient(135deg, #f97316 0%, #ea580c 50%, #c2410c 100%),
    linear-gradient(45deg, rgba(255, 255, 255, 0.1) 0%, transparent 100%);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  box-shadow: 
    0 12px 35px rgba(249, 115, 22, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.primary-action:hover {
  transform: translateY(-4px) scale(1.05);
  box-shadow: 
    0 20px 50px rgba(249, 115, 22, 0.5),
    0 0 0 1px rgba(255, 255, 255, 0.2),
    0 0 30px rgba(249, 115, 22, 0.6);
}

.secondary-action {
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.3);
  color: white;
  backdrop-filter: blur(20px);
  box-shadow: 
    0 8px 25px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.secondary-action:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.6);
  transform: translateY(-4px) scale(1.05);
  box-shadow: 
    0 15px 40px rgba(0, 0, 0, 0.3),
    0 0 20px rgba(255, 255, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

/* ===== 主要内容区域 ===== */
.main-content {
  padding: 0 60px 80px;
  max-width: 1400px;
  margin: 0 auto;
  position: relative;
  z-index: 5;
}

/* ===== 玻璃拟态卡片通用样式 ===== */
.glass-card {
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
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
  overflow: hidden;
}

.glass-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(56, 189, 248, 0.1), 
    transparent);
  transition: left 0.8s;
}

.glass-card:hover::before {
  left: 100%;
}

.glass-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 
    0 30px 60px rgba(0, 0, 0, 0.15),
    0 12px 40px rgba(56, 189, 248, 0.2),
    0 0 0 1px rgba(56, 189, 248, 0.3),
    inset 0 2px 0 rgba(255, 255, 255, 0.3);
}

/* ===== 区域标题增强 ===== */
.section-title {
  font-size: 2rem;
  font-weight: 800;
  color: rgba(255, 255, 255, 0.95);
  margin-bottom: 40px;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  padding: 20px 0;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.5);
}

.title-icon {
  font-size: 2.5rem;
  background: linear-gradient(135deg, #38bdf8, #8b5cf6, #ec4899);
  border-radius: 50%;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 
    0 8px 25px rgba(56, 189, 248, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  animation: iconPulse 3s ease-in-out infinite;
}

@keyframes iconPulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 8px 25px rgba(56, 189, 248, 0.3);
  }
  50% {
    transform: scale(1.1);
    box-shadow: 0 12px 35px rgba(56, 189, 248, 0.5);
  }
}

.title-decoration {
  flex: 1;
  height: 3px;
  background: linear-gradient(90deg, 
    rgba(56, 189, 248, 0.8) 0%,
    rgba(139, 92, 246, 0.6) 50%,
    transparent 100%);
  border-radius: 2px;
  position: relative;
  overflow: hidden;
}

.title-decoration::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(255, 255, 255, 0.8), 
    transparent);
  animation: shine 2s ease-in-out infinite;
}

@keyframes shine {
  0% { left: -100%; }
  100% { left: 100%; }
}

/* ===== 统计卡片科技化 ===== */
.stats-section {
  margin-bottom: 60px;
  animation: fadeInUp 0.8s ease-out var(--delay, 0s) both;
}

.stat-card {
  background: 
    linear-gradient(135deg, 
      rgba(255, 255, 255, 0.1) 0%, 
      rgba(255, 255, 255, 0.05) 100%);
  backdrop-filter: blur(25px);
  border-radius: 24px;
  padding: 40px 32px;
  text-align: center;
  border: 1px solid rgba(56, 189, 248, 0.3);
  position: relative;
  overflow: hidden;
  height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  animation: cardSlideUp 0.6s ease-out var(--delay, 0s) both;
}

@keyframes cardSlideUp {
  from {
    opacity: 0;
    transform: translateY(50px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, 
    #38bdf8 0%, 
    #8b5cf6 50%, 
    #ec4899 100%);
  opacity: 0;
  transition: opacity 0.4s ease;
}

.stat-card:hover::after {
  opacity: 1;
}

.hologram-effect {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: 
    radial-gradient(ellipse at center, 
      rgba(56, 189, 248, 0.1) 0%, 
      transparent 50%);
  transform: rotate(45deg);
  opacity: 0;
  transition: opacity 0.6s ease;
}

.stat-card:hover .hologram-effect {
  opacity: 1;
  animation: hologramSpin 4s linear infinite;
}

@keyframes hologramSpin {
  from { transform: rotate(45deg); }
  to { transform: rotate(405deg); }
}

.stat-icon {
  font-size: 3.5rem;
  margin-bottom: 20px;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.3));
  animation: iconFloat 3s ease-in-out infinite;
}

@keyframes iconFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.stat-number {
  font-size: 3rem;
  font-weight: 900;
  background: linear-gradient(135deg, 
    #ffffff 0%, 
    #38bdf8 50%, 
    #8b5cf6 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 12px;
  text-shadow: 0 4px 12px rgba(56, 189, 248, 0.5);
  position: relative;
}

.stat-number::before {
  content: attr(data-value);
  position: absolute;
  top: 0;
  left: 0;
  background: linear-gradient(135deg, #ec4899, #8b5cf6);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  opacity: 0;
  transition: opacity 0.4s ease;
}

.stat-card:hover .stat-number::before {
  opacity: 1;
}

.stat-label {
  font-size: 1rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
  text-transform: uppercase;
  letter-spacing: 1px;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
}

.stat-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 4px;
  background: linear-gradient(90deg, 
    #38bdf8 0%, 
    #8b5cf6 50%, 
    #ec4899 100%);
  width: 0;
  transition: width 1.5s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 0 0 24px 24px;
}

.stat-card:hover .stat-progress {
  width: 100%;
}

/* ===== 课程推荐区域 ===== */
.recommend-section {
  margin-bottom: 80px;
  animation: fadeInUp 1s ease-out 0.3s both;
}

.course-skeleton {
  padding: 40px;
  background: 
    linear-gradient(135deg, 
      rgba(255, 255, 255, 0.1) 0%, 
      rgba(255, 255, 255, 0.05) 100%);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(56, 189, 248, 0.2);
}

.empty-state {
  text-align: center;
  padding: 80px 40px;
  border-radius: 24px;
  color: rgba(255, 255, 255, 0.8);
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 32px;
}

.course-card {
  background: 
    linear-gradient(145deg, 
      rgba(255, 255, 255, 0.12) 0%, 
      rgba(255, 255, 255, 0.08) 100%);
  backdrop-filter: blur(25px) saturate(180%);
  border-radius: 28px;
  overflow: hidden;
  border: 1px solid rgba(56, 189, 248, 0.3);
  cursor: pointer;
  position: relative;
  transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  animation: courseCardSlide 0.8s ease-out var(--delay, 0s) both;
  box-shadow: 
    0 15px 35px rgba(0, 0, 0, 0.1),
    0 6px 20px rgba(56, 189, 248, 0.1);
}

@keyframes courseCardSlide {
  from {
    opacity: 0;
    transform: translateX(-50px) rotateY(-15deg);
  }
  to {
    opacity: 1;
    transform: translateX(0) rotateY(0deg);
  }
}

.course-card:hover {
  transform: translateY(-12px) rotateX(5deg) scale(1.02);
  box-shadow: 
    0 30px 60px rgba(0, 0, 0, 0.2),
    0 15px 40px rgba(56, 189, 248, 0.3),
    0 0 0 1px rgba(56, 189, 248, 0.4);
}

.card-reflection {
  position: absolute;
  bottom: -100%;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, 
    rgba(255, 255, 255, 0.05) 0%, 
    transparent 50%);
  transform: scaleY(-1);
  opacity: 0;
  transition: all 0.6s ease;
}

.course-card:hover .card-reflection {
  opacity: 0.3;
  bottom: -50%;
}

.course-image {
  position: relative;
  height: 220px;
  overflow: hidden;
}

.course-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  filter: brightness(0.9) saturate(1.1);
}

.course-card:hover .course-cover {
  transform: scale(1.1) rotate(2deg);
  filter: brightness(1) saturate(1.3);
}

.course-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(ellipse at center, 
      rgba(0, 0, 0, 0.7) 0%, 
      rgba(0, 0, 0, 0.3) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(5px);
}

.course-card:hover .course-overlay {
  opacity: 1;
}

.course-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  background: linear-gradient(135deg, #ec4899, #be185d);
  color: white;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
  box-shadow: 0 4px 15px rgba(236, 72, 153, 0.4);
  animation: badgePulse 2s ease-in-out infinite;
}

@keyframes badgePulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 4px 15px rgba(236, 72, 153, 0.4);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 6px 20px rgba(236, 72, 153, 0.6);
  }
}

.view-course-btn {
  background: linear-gradient(135deg, #38bdf8, #1d4ed8);
  border: none;
  border-radius: 12px;
  padding: 16px 32px;
  font-weight: 700;
  position: relative;
  overflow: hidden;
  transform: translateY(20px);
  transition: all 0.4s ease;
}

.course-card:hover .view-course-btn {
  transform: translateY(0);
}

.btn-glow {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(255, 255, 255, 0.4), 
    transparent);
  transition: left 0.6s;
}

.view-course-btn:hover .btn-glow {
  left: 100%;
}

.course-content {
  padding: 32px 28px;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.08) 0%, 
    rgba(255, 255, 255, 0.04) 100%);
  backdrop-filter: blur(10px);
}

.course-title {
  font-size: 1.4rem;
  font-weight: 800;
  color: rgba(255, 255, 255, 0.95);
  margin: 0 0 16px 0;
  line-height: 1.3;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  background: linear-gradient(135deg, 
    #ffffff 0%, 
    #38bdf8 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.course-description {
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.6;
  margin-bottom: 24px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
}

.course-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid rgba(56, 189, 248, 0.2);
}

.course-teacher {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  color: rgba(56, 189, 248, 0.9);
  font-weight: 600;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.course-teacher i {
  color: #38bdf8;
  filter: drop-shadow(0 0 6px rgba(56, 189, 248, 0.5));
}

.course-rating {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rating-score {
  font-size: 0.9rem;
  font-weight: 700;
  color: #ffa500;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

/* ===== 侧边栏科技化 ===== */
.sidebar {
  position: sticky;
  top: 60px;
  z-index: 10;
}

.user-info-card {
  border-radius: 28px;
  overflow: hidden;
  transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  animation: slideInRight 0.8s ease-out 0.5s both;
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(50px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.card-header {
  background: 
    linear-gradient(135deg, 
      rgba(56, 189, 248, 0.2) 0%, 
      rgba(139, 92, 246, 0.15) 100%);
  padding: 28px 32px;
  border-bottom: 1px solid rgba(56, 189, 248, 0.3);
  position: relative;
}

.card-header h3 {
  margin: 0;
  font-size: 1.3rem;
  font-weight: 800;
  color: rgba(255, 255, 255, 0.95);
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.header-decoration {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, 
    #38bdf8 0%, 
    #8b5cf6 50%, 
    #ec4899 100%);
  animation: decorationFlow 3s ease-in-out infinite;
}

@keyframes decorationFlow {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.user-info {
  padding: 40px 32px;
  text-align: center;
  position: relative;
}

.user-avatar-wrapper {
  margin-bottom: 24px;
  position: relative;
  display: inline-block;
}

.avatar-ring {
  position: absolute;
  top: -8px;
  left: -8px;
  right: -8px;
  bottom: -8px;
  border-radius: 50%;
  background: linear-gradient(45deg, 
    #38bdf8, #8b5cf6, #ec4899, #38bdf8);
  background-size: 300% 300%;
  animation: ringRotate 4s linear infinite;
  z-index: -1;
}

@keyframes ringRotate {
  0% { background-position: 0% 50%; }
  100% { background-position: 100% 50%; }
}

.user-avatar {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid rgba(255, 255, 255, 0.3);
  box-shadow: 
    0 12px 30px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  transition: all 0.4s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
  box-shadow: 
    0 16px 40px rgba(0, 0, 0, 0.4),
    0 0 20px rgba(56, 189, 248, 0.5);
}

.user-avatar-placeholder {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: linear-gradient(135deg, #38bdf8, #1d4ed8);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.2rem;
  font-weight: 900;
  color: white;
  border: 3px solid rgba(255, 255, 255, 0.3);
  box-shadow: 
    0 12px 30px rgba(56, 189, 248, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
}

.status-indicator {
  position: absolute;
  bottom: 8px;
  right: 8px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.8);
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
  margin-bottom: 28px;
}

.username {
  font-size: 1.5rem;
  font-weight: 800;
  color: rgba(255, 255, 255, 0.95);
  margin: 0 0 12px 0;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  background: linear-gradient(135deg, 
    #ffffff 0%, 
    #38bdf8 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.user-role {
  display: inline-block;
  padding: 8px 20px;
  border-radius: 25px;
  font-size: 0.9rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
  position: relative;
  overflow: hidden;
  transition: all 0.4s ease;
}

.user-role::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(255, 255, 255, 0.2), 
    transparent);
  transition: left 0.6s;
}

.user-role:hover::before {
  left: 100%;
}

.role-student {
  background: linear-gradient(135deg, #38bdf8, #0ea5e9);
  color: white;
  box-shadow: 0 6px 20px rgba(56, 189, 248, 0.4);
}

.role-teacher {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
  color: white;
  box-shadow: 0 6px 20px rgba(139, 92, 246, 0.4);
}

.role-admin {
  background: linear-gradient(135deg, #ec4899, #db2777);
  color: white;
  box-shadow: 0 6px 20px rgba(236, 72, 153, 0.4);
}

.profile-btn {
  border-radius: 16px;
  padding: 14px 28px;
  font-weight: 700;
  border: 2px solid rgba(56, 189, 248, 0.5);
  color: rgba(56, 189, 248, 0.9);
  background: rgba(56, 189, 248, 0.1);
  backdrop-filter: blur(10px);
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
  overflow: hidden;
}

.profile-btn span {
  position: relative;
  z-index: 2;
}

.btn-particles {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 4px;
  height: 4px;
  background: #38bdf8;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  opacity: 0;
  transition: all 0.6s ease;
}

.profile-btn:hover {
  background: linear-gradient(135deg, #38bdf8, #1d4ed8);
  color: white;
  border-color: rgba(56, 189, 248, 0.8);
  transform: translateY(-3px);
  box-shadow: 
    0 12px 30px rgba(56, 189, 248, 0.4),
    0 0 20px rgba(56, 189, 248, 0.3);
}

.profile-btn:hover .btn-particles {
  opacity: 1;
  animation: particleExplosion 0.6s ease-out;
}

@keyframes particleExplosion {
  0% {
    width: 4px;
    height: 4px;
    opacity: 1;
  }
  100% {
    width: 40px;
    height: 40px;
    opacity: 0;
  }
}

/* ===== AI聊天助手科技化 ===== */
.chat-assistant {
  position: fixed;
  bottom: 0;
  right: 0;
  z-index: 2000;
}

.chat-float-btn {
  position: fixed;
  bottom: 40px;
  right: 40px;
  width: 70px;
  height: 70px;
  border-radius: 50%;
  background: 
    linear-gradient(135deg, #38bdf8, #1d4ed8),
    linear-gradient(45deg, rgba(255, 255, 255, 0.1) 0%, transparent 100%);
  border: 2px solid rgba(255, 255, 255, 0.2);
  color: white;
  font-size: 1.8rem;
  box-shadow: 
    0 12px 35px rgba(56, 189, 248, 0.4),
    0 0 0 0 rgba(56, 189, 248, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  animation: chatFloat 3s ease-in-out infinite;
  backdrop-filter: blur(10px);
}

@keyframes chatFloat {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
    box-shadow: 
      0 12px 35px rgba(56, 189, 248, 0.4),
      0 0 0 0 rgba(56, 189, 248, 0.3);
  }
  50% {
    transform: translateY(-10px) rotate(5deg);
    box-shadow: 
      0 20px 50px rgba(56, 189, 248, 0.5),
      0 0 0 10px rgba(56, 189, 248, 0.1);
  }
}

.chat-icon {
  position: relative;
  z-index: 2;
  display: block;
  animation: iconBounce 2s ease-in-out infinite;
}

@keyframes iconBounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.pulse-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100%;
  height: 100%;
  border: 2px solid rgba(56, 189, 248, 0.6);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  animation: pulseRing 2s cubic-bezier(0.455, 0.03, 0.515, 0.955) infinite;
}

@keyframes pulseRing {
  0% {
    transform: translate(-50%, -50%) scale(0.8);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(2.5);
    opacity: 0;
  }
}

.chat-float-btn:hover {
  transform: scale(1.1) translateY(-5px);
  box-shadow: 
    0 20px 50px rgba(56, 189, 248, 0.6),
    0 0 30px rgba(56, 189, 248, 0.5),
    inset 0 2px 0 rgba(255, 255, 255, 0.3);
}

.chat-window {
  position: fixed;
  bottom: 130px;
  right: 40px;
  width: 420px;
  height: 600px;
  border-radius: 28px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  z-index: 1999;
  animation: windowSlideUp 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes windowSlideUp {
  from {
    opacity: 0;
    transform: scale(0.8) translateY(50px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: scale(0.8) translateY(30px);
  opacity: 0;
}

.chat-header {
  background: 
    linear-gradient(135deg, #38bdf8, #1d4ed8),
    linear-gradient(45deg, rgba(255, 255, 255, 0.1) 0%, transparent 100%);
  color: white;
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 700;
  position: relative;
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.ai-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #ec4899, #be185d);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  box-shadow: 
    0 4px 15px rgba(236, 72, 153, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  animation: avatarPulse 2s ease-in-out infinite;
}

@keyframes avatarPulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 4px 15px rgba(236, 72, 153, 0.4);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 6px 20px rgba(236, 72, 153, 0.6);
  }
}

.close-btn {
  color: white;
  font-size: 1.4rem;
  font-weight: 800;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: scale(1.1);
}

.chat-body {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: 
    linear-gradient(135deg, 
      rgba(15, 15, 35, 0.95) 0%, 
      rgba(26, 26, 46, 0.9) 100%);
  backdrop-filter: blur(25px);
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: relative;
}

.chat-body::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 20%, rgba(56, 189, 248, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(139, 92, 246, 0.05) 0%, transparent 50%);
  pointer-events: none;
}

.chat-body::-webkit-scrollbar {
  width: 8px;
}

.chat-body::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

.chat-body::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #38bdf8, #8b5cf6);
  border-radius: 4px;
  transition: all 0.3s ease;
}

.chat-body::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #0ea5e9, #7c3aed);
}

.chat-message {
  padding: 16px 20px;
  border-radius: 20px;
  max-width: 85%;
  word-wrap: break-word;
  position: relative;
  backdrop-filter: blur(10px);
  animation: messageSlideIn 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.chat-message.user {
  background: linear-gradient(135deg, #38bdf8, #1d4ed8);
  color: white;
  align-self: flex-end;
  border-bottom-right-radius: 6px;
  box-shadow: 
    0 8px 25px rgba(56, 189, 248, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.chat-message.ai {
  background: 
    linear-gradient(135deg, 
      rgba(255, 255, 255, 0.15) 0%, 
      rgba(255, 255, 255, 0.08) 100%);
  color: rgba(255, 255, 255, 0.95);
  align-self: flex-start;
  border-bottom-left-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 
    0 8px 25px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.loading-dots {
  display: inline-block;
}

.loading-dots span {
  display: inline-block;
  animation: dotBlink 1.4s infinite both;
  margin-left: 2px;
}

.loading-dots span:nth-child(1) { animation-delay: 0s; }
.loading-dots span:nth-child(2) { animation-delay: 0.2s; }
.loading-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes dotBlink {
  0%, 80%, 100% { opacity: 0.3; transform: scale(0.8); }
  40% { opacity: 1; transform: scale(1); }
}

.chat-footer {
  display: flex;
  gap: 16px;
  padding: 20px 24px;
  border-top: 1px solid rgba(56, 189, 248, 0.2);
  background: 
    linear-gradient(135deg, 
      rgba(255, 255, 255, 0.1) 0%, 
      rgba(255, 255, 255, 0.05) 100%);
  backdrop-filter: blur(25px);
}

.chat-input {
  flex: 1;
}

.chat-input :deep(.el-input__wrapper) {
  border-radius: 25px;
  border: 1px solid rgba(56, 189, 248, 0.3);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.chat-input :deep(.el-input__wrapper):hover {
  border-color: rgba(56, 189, 248, 0.5);
  box-shadow: 0 0 0 2px rgba(56, 189, 248, 0.2);
}

.chat-input :deep(.el-input__inner) {
  color: rgba(255, 255, 255, 0.9);
  background: transparent;
}

.send-btn {
  border-radius: 25px;
  background: linear-gradient(135deg, #38bdf8, #1d4ed8);
  border: none;
  padding: 0 24px;
  font-weight: 700;
  box-shadow: 0 4px 15px rgba(56, 189, 248, 0.4);
  transition: all 0.3s ease;
}

.send-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(56, 189, 248, 0.5);
}

.faq-section {
  padding: 16px 24px;
  border-top: 1px solid rgba(56, 189, 248, 0.2);
  background: 
    linear-gradient(135deg, 
      rgba(15, 15, 35, 0.95) 0%, 
      rgba(26, 26, 46, 0.9) 100%);
  backdrop-filter: blur(20px);
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.faq-label {
  font-size: 0.85rem;
  font-weight: 700;
  color: rgba(56, 189, 248, 0.9);
  margin-right: 12px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.faq-btn {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 0.8rem;
  background: rgba(56, 189, 248, 0.1);
  color: rgba(56, 189, 248, 0.9);
  border: 1px solid rgba(56, 189, 248, 0.3);
  backdrop-filter: blur(10px);
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  font-weight: 600;
}

.faq-btn:hover {
  background: linear-gradient(135deg, #38bdf8, #1d4ed8);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(56, 189, 248, 0.4);
}

/* ===== 通用动画效果 ===== */
@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.05);
    opacity: 0.8;
  }
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

/* ===== 响应式设计 ===== */
@media (max-width: 1200px) {
  .main-content {
    padding: 0 40px 60px;
  }
  
  .welcome-section {
    padding: 80px 40px;
  }
  
  .welcome-title {
    font-size: 3.2rem;
  }
}

@media (max-width: 768px) {
  .welcome-section {
    padding: 60px 24px;
    border-radius: 0 0 24px 24px;
  }
  
  .welcome-title {
    font-size: 2.5rem;
  }
  
  .welcome-subtitle {
    font-size: 1.2rem;
  }
  
  .main-content {
    padding: 0 24px 60px;
  }
  
  .quick-actions {
    flex-direction: column;
    align-items: center;
    gap: 20px;
  }
  
  .action-button {
    width: 100%;
    max-width: 320px;
    height: 56px;
    font-size: 16px;
  }
  
  .courses-grid {
    grid-template-columns: 1fr;
    gap: 24px;
  }
  
  .stat-card {
    height: 160px;
    padding: 28px 20px;
  }
  
  .stat-icon {
    font-size: 2.8rem;
  }
  
  .stat-number {
    font-size: 2.4rem;
  }
  
  .section-title {
    font-size: 1.6rem;
    padding: 16px 0;
  }
  
  .title-icon {
    width: 50px;
    height: 50px;
    font-size: 2rem;
  }
  
  .chat-window {
    right: 20px;
    left: 20px;
    bottom: 110px;
    width: auto;
    height: 500px;
  }
  
  .chat-float-btn {
    right: 24px;
    bottom: 24px;
    width: 60px;
    height: 60px;
    font-size: 1.5rem;
  }
  
  .user-avatar,
  .user-avatar-placeholder {
    width: 70px;
    height: 70px;
  }
  
  .user-avatar-placeholder {
    font-size: 1.8rem;
  }
}

@media (max-width: 480px) {
  .welcome-title {
    font-size: 2rem;
    letter-spacing: -1px;
  }
  
  .welcome-subtitle {
    font-size: 1rem;
  }
  
  .section-title {
    font-size: 1.4rem;
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .title-decoration {
    width: 100%;
  }
  
  .stat-card {
    height: 140px;
    padding: 20px 16px;
  }
  
  .stat-icon {
    font-size: 2.2rem;
    margin-bottom: 12px;
  }
  
  .stat-number {
    font-size: 2rem;
  }
  
  .course-image {
    height: 180px;
  }
  
  .course-content {
    padding: 24px 20px;
  }
  
  .user-info-card {
    border-radius: 20px;
  }
  
  .card-header {
    padding: 20px 24px;
  }
  
  .user-info {
    padding: 32px 24px;
  }
}

/* ===== 最小屏幕适配 ===== */
@media (max-width: 360px) {
  .welcome-section {
    padding: 40px 16px;
  }
  
  .main-content {
    padding: 0 16px 40px;
  }
  
  .welcome-title {
    font-size: 1.8rem;
  }
  
  .action-button {
    height: 48px;
    padding: 0 24px;
    font-size: 14px;
  }
  
  .stat-card {
    height: 120px;
    padding: 16px 12px;
  }
  
  .stat-number {
    font-size: 1.8rem;
  }
  
  .stat-label {
    font-size: 0.85rem;
  }
}

/* ===== 高分辨率屏幕优化 ===== */
@media (min-width: 1400px) {
  .main-content {
    padding: 0 80px 100px;
  }
  
  .welcome-section {
    padding: 140px 80px;
  }
  
  .welcome-title {
    font-size: 4.5rem;
  }
  
  .welcome-subtitle {
    font-size: 1.6rem;
  }
  
  .courses-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>