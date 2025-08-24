<template>
  <div class="home-container">
    <el-row :gutter="20">
      <!-- 左侧主体 -->
      <el-col :span="16">
        <!-- 欢迎区 -->
        <div class="welcome-section">
          <div class="welcome-bg-shapes">
            <div class="shape shape-1"></div>
            <div class="shape shape-2"></div>
            <div class="shape shape-3"></div>
          </div>
          <h1 class="welcome-title">欢迎来到 <span class="highlight">教育平台 <span class="emoji">🎓</span></span></h1>
          <p class="welcome-subtitle">在这里开启您的学习之旅，探索丰富的课程资源</p>

          <div class="quick-actions">
            <el-button type="primary" size="large" class="gradient-btn pulse" @click="$router.push('/courses')"
              :icon="Reading">
              浏览课程
            </el-button>
            <el-button size="large" class="secondary-btn" @click="$router.push('/my-courses')" :icon="Collection">
              我的课程
            </el-button>
          </div>
        </div>

        <!-- 数据统计 -->
        <div class="stats-section">
          <el-row :gutter="20">
            <el-col v-for="item in statCards" :key="item.label" :span="8">
              <el-card :class="['stat-card', item.color]" shadow="hover">
                <div class="stat-icon">{{ item.icon }}</div>
                <div class="stat-info">
                  <div class="stat-number">{{ item.value }}</div>
                  <div class="stat-label">{{ item.label }}</div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-col>

      <!-- 右侧侧栏 -->
      <el-col :span="8">
        <div class="sidebar">
          <el-card class="user-info-card" shadow="always">
            <template #header>
              <div class="card-header">个人信息</div>
            </template>

            <div v-if="authStore.isAuthenticated" class="user-info">
              <el-avatar :size="72" :src="avatarUrl" style="background:linear-gradient(135deg,#667eea,#764ba2);color:white;">
                <span v-if="!userProfile.avatar">{{ user?.username?.charAt(0)?.toUpperCase() }}</span>
              </el-avatar>
              <h3>{{ user?.username }}</h3>
              <p>{{ roleText }}</p>
              <el-button type="primary" plain size="small" @click="$router.push('/profile')">
                查看详情
              </el-button>
            </div>
          </el-card>
        </div>
        <div>
          <!-- 浮动按钮 -->
          <el-button class="chat-float-btn" circle type="primary" @click="toggleChat">
            💬
          </el-button>

          <!-- 聊天窗口 -->
          <transition name="slide-up">
            <div v-if="showChat" class="chat-window">
              <div class="chat-header">
                <span>AI 学习助手 🤖</span>
                <el-button link @click="toggleChat">❌</el-button>
              </div>

              <div class="chat-body">
                <div v-for="(msg, index) in messages" :key="index" :class="['chat-msg', msg.role]">
                  <span v-if="!msg.loading">{{ msg.text }}</span>
                  <!-- AI 正在思考 动画 -->
                  <span v-else class="loading-dots">AI 正在思考<span>.</span><span>.</span><span>.</span></span>
                </div>
              </div>

              <div class="chat-footer">
                <el-input v-model="input" placeholder="请输入问题..." @keyup.enter="sendMessage()" />
                <el-button type="primary" @click="sendMessage()">发送</el-button>
              </div>

              <!-- FAQ 快捷按钮 -->
              <div class="faq-box">
                <span>常见问题：</span>
                <el-button v-for="(q, i) in faqs" :key="i" size="small" @click="sendMessage(q)">
                  {{ q }}
                </el-button>
              </div>
            </div>
          </transition>
        </div>
      </el-col>
      <!-- 🔥 课程推荐 -->
      <div class="recommend-section">
        <h2 class="section-title">🔥 热门课程推荐</h2>
        <el-skeleton v-if="loading" :rows="3" animated style="padding:20px;" />
        <el-row v-else :gutter="20">
          <el-col :span="8" v-for="course in hotCourses" :key="course.id">
            <el-card shadow="hover" class="course-card" @click="$router.push('/courses/' + course.id)">
              <div class="course-content">
                <img :src="course.coverUrl" alt="课程封面" class="course-cover" />
                <div class="course-info">
                  <h3 class="course-title">{{ course.title }}</h3>
                  <p class="course-desc">{{ course.description }}</p>
                  <div class="course-meta">
                    <span class="teacher">讲师：{{ course.teacher }}</span>
                  </div>
                  <div class="course-rating">
                    <el-rate :model-value="course.rating/2" disabled text-color="#ff9900" size="small" />
                    <span class="rating-score">{{ course.rating.toFixed(1) }}</span>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-row>
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
import { ro } from 'element-plus/es/locales.mjs';

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

const getTopRatedCourses = async (courses, n) => {
  try {
    // 获取所有课程的评分统计信息
    const coursesWithRating = await Promise.all(
      courses.map(async (course) => {
        try {
          const stats = await commentApi.getCommentStats(course.id);
          return {
            ...course,
            avgRating: stats.avgRating || 0
          };
        } catch (error) {
          console.warn(`获取课程 ${course.id} 评分失败:`, error);
          return {
            ...course,
            avgRating: 0
          };
        }
      })
    );

    // 按评分降序排序，取前n个
    return coursesWithRating
      .sort((a, b) => b.avgRating - a.avgRating)
      .slice(0, n);
  } catch (error) {
    console.error('获取课程评分排序失败:', error);
    // 如果获取评分失败，返回前n个课程
    return courses.slice(0, n);
  }
};

// 获取用户资料
const fetchUserProfile = async () => {
  try {
    loading.value = true;
    // 获取详细用户信息（头像、真实姓名、联系方式等）
    const profileData = await userinfoApi.getUserProfile({ pageNum: 1, pageSize: 100 });

    // 从返回的数据中找到当前用户的详细信息
    let userDetailInfo = {};
    const currentUserId = authStore.user?.id;

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

onMounted(async () => {
  try {
    // 获取用户资料
    await fetchUserProfile();
    
    const role = user?.role;
    if (role === "student") {
      const myCourses = await enrollmentApi.getMyCourses();
      let examCount = 0;
      const myEnrollments = await enrollmentApi.getMyEnrollments();
      const totalCourses = myEnrollments.length;
      if (totalCourses === 0) {
        progress.value = 0;
        console.log("总进度: 0% (未选课)");
      } else {
        // 计算所有课程的进度总和
        const totalProgress = myEnrollments.reduce((sum, course) => sum + course.progress, 0);
        // 计算平均进度（总进度 ÷ 课程数量）
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
      const selected = await getTopRatedCourses(allCourses, 6);
      const detailRes = await Promise.all(selected.map(c => courseApi.getCourseDetail(c.id)));

      // 获取所有讲师的用户信息
      const teacherIds = detailRes.map(r => r.course.teacherId).filter(id => id);
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

      hotCourses.value = detailRes.map((r => {
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

  // 用户消息
  messages.value.push({ role: "user", text: userMsg });
  input.value = "";

  // 插入“AI 正在思考中...”消息，并保留索引
  const loadingMsg = { role: "ai", text: "AI 正在思考...", loading: true };
  messages.value.push(loadingMsg);
  const loadingIndex = messages.value.length - 1;

  try {
    const url = "https://api.siliconflow.cn/v1/chat/completions";
    const options = {
      method: "POST",
      headers: {
        Authorization:
          "Bearer sk-ghfcmenqyzgnaawsnjmlowebfqztmuhwgawhoucmntybfapb",
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

    console.log("API 返回：", data); // 调试用

    const reply =
      data?.choices?.[0]?.message?.content ||
      "对不起，AI 没有返回结果，请稍后再试。";

    // ✅ 用 Vue 的响应式更新数组，而不是直接改对象
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

</script>

<style scoped>
.home-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 30px 20px;
  background: linear-gradient(180deg, #f0f4ff 0%, #fdfdfd 100%);
  min-height: calc(100vh - 60px);
}

/* 欢迎区 */
.welcome-section {
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  color: white;
  padding: 70px 40px;
  border-radius: 24px;
  margin-bottom: 40px;
  text-align: center;
  box-shadow: 0 15px 35px rgba(78, 89, 245, 0.2), 0 5px 15px rgba(0, 0, 0, 0.05);
  transition: transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275), box-shadow 0.4s ease;
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.welcome-section:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 40px rgba(78, 89, 245, 0.25), 0 8px 20px rgba(0, 0, 0, 0.1);
}

.welcome-bg-shapes {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  overflow: hidden;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.shape-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -80px;
  animation: float 15s infinite alternate ease-in-out;
}

.shape-2 {
  width: 200px;
  height: 200px;
  bottom: -80px;
  left: -50px;
  animation: float 12s infinite alternate-reverse ease-in-out;
}

.shape-3 {
  width: 160px;
  height: 160px;
  top: 40%;
  left: 30%;
  animation: float 10s infinite alternate ease-in-out;
}

@keyframes float {
  0% {
    transform: translate(0, 0) rotate(0deg);
  }

  100% {
    transform: translate(20px, 20px) rotate(10deg);
  }
}

.welcome-title {
  font-size: 2.8rem;
  font-weight: 800;
  margin-bottom: 16px;
  letter-spacing: -0.5px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  animation: fadeInDown 0.8s ease-out;
}

.welcome-subtitle {
  font-size: 1.2rem;
  opacity: 0.9;
  max-width: 600px;
  margin: 0 auto 30px;
  line-height: 1.6;
  animation: fadeInUp 0.8s ease-out 0.2s both;
}

.highlight {
  color: #FFD86F;
  position: relative;
  display: inline-block;
}

.emoji {
  display: inline-block;
  animation: bounce 2s infinite;
  transform-origin: center bottom;
}

@keyframes bounce {

  0%,
  20%,
  50%,
  80%,
  100% {
    transform: translateY(0);
  }

  40% {
    transform: translateY(-20px);
  }

  60% {
    transform: translateY(-10px);
  }
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.quick-actions {
  display: flex;
  gap: 24px;
  justify-content: center;
  margin-top: 36px;
  animation: fadeInUp 0.8s ease-out 0.4s both;
}

.gradient-btn {
  background: linear-gradient(135deg, #FF9966, #FF5E62);
  border: none;
  font-weight: 700;
  border-radius: 12px;
  padding: 14px 32px;
  box-shadow: 0 8px 20px rgba(255, 94, 98, 0.3);
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
  overflow: hidden;
}

.gradient-btn:hover {
  background: linear-gradient(135deg, #FF8E53, #FF5151);
  transform: translateY(-3px) scale(1.03);
  box-shadow: 0 12px 25px rgba(255, 94, 98, 0.4);
}

.gradient-btn::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, rgba(255, 255, 255, 0) 70%);
  opacity: 0;
  transition: opacity 0.5s ease;
}

.gradient-btn:hover::after {
  opacity: 1;
}

.pulse {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(255, 94, 98, 0.4);
  }

  70% {
    box-shadow: 0 0 0 15px rgba(255, 94, 98, 0);
  }

  100% {
    box-shadow: 0 0 0 0 rgba(255, 94, 98, 0);
  }
}

.secondary-btn {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  font-weight: 600;
  border-radius: 12px;
  padding: 14px 32px;
  transition: all 0.3s ease;
}

.secondary-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

/* 数据统计 */
.stats-section {
  margin-top: 40px;
  margin-bottom: 40px;
}

.stat-card {
  border-radius: 20px;
  text-align: center;
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
  position: relative;
  overflow: hidden;
  border: none;
  height: 100%;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
}

.stat-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  z-index: 1;
}

.stat-card::before {
  content: "";
  position: absolute;
  top: -30%;
  right: -30%;
  width: 150%;
  height: 150%;
  background: radial-gradient(circle at top right, rgba(102, 126, 234, 0.12), transparent 70%);
  transform: rotate(25deg);
  transition: all 0.5s ease;
}

.stat-card:hover::before {
  transform: rotate(35deg) scale(1.2);
}

.stat-icon {
  font-size: 48px;
  margin-bottom: 16px;
  display: inline-block;
  transition: transform 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.stat-card:hover .stat-icon {
  transform: scale(1.2) rotate(5deg);
}

.stat-number {
  font-size: 36px;
  font-weight: 800;
  background: linear-gradient(135deg, #333, #555);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 8px;
  position: relative;
  display: inline-block;
}

.stat-number::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 50%;
  width: 40px;
  height: 3px;
  background: currentColor;
  transform: translateX(-50%);
  opacity: 0.3;
  border-radius: 3px;
}

.stat-label {
  font-size: 16px;
  font-weight: 500;
  color: #555;
  letter-spacing: 0.5px;
  margin-top: 8px;
}

.stat-blue {
  background: linear-gradient(165deg, #f0f7ff, #ffffff);
  border-bottom: 5px solid #409eff;
}

.stat-blue .stat-number {
  background: linear-gradient(135deg, #0078FF, #409EFF);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stat-green {
  background: linear-gradient(165deg, #f6fff2, #ffffff);
  border-bottom: 5px solid #67c23a;
}

.stat-green .stat-number {
  background: linear-gradient(135deg, #52C41A, #67C23A);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stat-orange {
  background: linear-gradient(165deg, #fff9f2, #ffffff);
  border-bottom: 5px solid #e6a23c;
}

.stat-orange .stat-number {
  background: linear-gradient(135deg, #FA8C16, #E6A23C);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* 推荐课程 */
.recommend-section {
  margin-bottom: 30px;
  animation: fadeIn 1s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.section-title {
  font-size: 28px;
  font-weight: 800;
  margin-bottom: 30px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #222;
  position: relative;
  padding-left: 18px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 6px;
  height: 35px;
  background: linear-gradient(to bottom, #4776E6, #8E54E9);
  border-radius: 6px;
}

.course-card {
  border-radius: 24px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.5s cubic-bezier(0.165, 0.84, 0.44, 1);
  border: none;
  height: 100%;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  position: relative;
}

.course-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(71, 118, 230, 0.1), rgba(142, 84, 233, 0.1));
  opacity: 0;
  transition: opacity 0.5s ease;
}

.course-card:hover {
  transform: translateY(-12px) scale(1.03);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.12);
}

.course-card:hover::after {
  opacity: 1;
}

.course-content {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  padding: 24px;
  height: 100%;
}

.course-cover {
  height: 120px;
  object-fit: cover;
  border-radius: 16px;
  flex-shrink: 0;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  transition: all 0.5s ease;
  transform: rotate(-2deg);
}

.course-card:hover .course-cover {
  transform: rotate(0deg) scale(1.05);
  box-shadow: 0 12px 25px rgba(0, 0, 0, 0.2);
}

.course-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
}

.course-title {
  margin: 0 0 12px;
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.4;
  transition: color 0.3s ease;
}

.course-card:hover .course-title {
  color: #4776E6;
}

.course-desc {
  margin-bottom: 16px;
  color: #555;
  font-size: 15px;
  line-height: 1.6;
  max-height: 48px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.course-meta {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #888;
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.course-rating {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
  background: rgba(255, 153, 0, 0.08);
  border-radius: 12px;
  padding: 6px 10px;
  width: fit-content;
}

.rating-score {
  font-weight: 600;
  color: #ff9900;
  font-size: 15px;
}

.teacher {
  font-size: 14px;
  color: #666;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
}

.teacher::before {
  content: '👨‍🏫';
  font-size: 16px;
}

/* 侧栏 */
.sidebar {
  position: sticky;
  top: 30px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.user-info-card {
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  border: none;
  transition: all 0.4s ease;
  background: linear-gradient(165deg, #ffffff, #f8faff);
}

.user-info-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.12);
}

.card-header {
  font-weight: 700;
  font-size: 18px;
  color: #333;
  position: relative;
  padding: 16px 20px;
  background: linear-gradient(to right, rgba(71, 118, 230, 0.05), transparent);
  display: flex;
  align-items: center;
}

.card-header::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  background: linear-gradient(to bottom, #4776E6, #8E54E9);
  border-radius: 4px;
}

.user-info {
  text-align: center;
  padding: 5px;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.user-info::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at center, rgba(71, 118, 230, 0.03), transparent 70%);
  z-index: 0;
}

.user-info h3 {
  margin: 20px 0 10px;
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, #333, #555);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  position: relative;
  z-index: 1;
}

.user-info p {
  margin-bottom: 20px;
  color: #666;
  font-weight: 500;
  font-size: 16px;
  padding: 6px 16px;
  background: rgba(71, 118, 230, 0.1);
  border-radius: 20px;
  display: inline-block;
  position: relative;
  z-index: 1;
}

.user-info .el-avatar {
  position: relative;
  z-index: 1;
  box-shadow: 0 8px 25px rgba(71, 118, 230, 0.25);
  transition: all 0.5s ease;
}

.user-info-card:hover .el-avatar {
  transform: scale(1.1);
}

.user-info .el-avatar::after {
  content: "";
  position: absolute;
  inset: -6px;
  border-radius: 50%;
  background: conic-gradient(from 180deg, #4776E6, #8E54E9, #4776E6);
  animation: spin 8s linear infinite;
  z-index: -1;
}

.user-info .el-button {
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
  border-radius: 10px;
  padding: 10px 20px;
  font-weight: 600;
}

.user-info .el-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 15px rgba(71, 118, 230, 0.2);
}

@keyframes spin {
  0% {
    transform: rotate(0);
  }

  100% {
    transform: rotate(360deg);
  }
}

/* 聊天 */
.chat-float-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 60px;
  height: 60px;
  font-size: 24px;
  box-shadow: 0 5px 20px rgba(71, 118, 230, 0.3);
  z-index: 1000;
  border-radius: 50%;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  border: none;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  animation: pulse-blue 2s infinite;
}

@keyframes pulse-blue {
  0% {
    box-shadow: 0 0 0 0 rgba(71, 118, 230, 0.4);
  }

  70% {
    box-shadow: 0 0 0 15px rgba(71, 118, 230, 0);
  }

  100% {
    box-shadow: 0 0 0 0 rgba(71, 118, 230, 0);
  }
}

.chat-float-btn:hover {
  transform: scale(1.1) rotate(5deg);
}

.chat-window {
  position: fixed;
  bottom: 100px;
  right: 30px;
  width: 360px;
  height: 480px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  z-index: 1000;
  overflow: hidden;
  transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  transform-origin: bottom right;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: scale(0.8) translateY(30px);
  opacity: 0;
}

.chat-header {
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  color: #fff;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 20px 20px 0 0;
  font-weight: 600;
  font-size: 16px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.chat-body {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f9faff;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-body::-webkit-scrollbar {
  width: 6px;
}

.chat-body::-webkit-scrollbar-thumb {
  background: rgba(71, 118, 230, 0.2);
  border-radius: 10px;
}

.chat-msg {
  margin: 0;
  padding: 12px 16px;
  border-radius: 18px;
  max-width: 85%;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  position: relative;
  animation: fadeIn 0.3s ease-out;
}

.chat-msg.user {
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  color: white;
  align-self: flex-end;
  border-bottom-right-radius: 4px;
  margin-left: auto;
}

.chat-msg.ai {
  background: white;
  color: #333;
  align-self: flex-start;
  border-bottom-left-radius: 4px;
  margin-right: auto;
}

.chat-footer {
  display: flex;
  gap: 10px;
  padding: 15px;
  border-top: 1px solid #eee;
  background: white;
}

.chat-footer .el-input {
  border-radius: 20px;
}

.chat-footer .el-input__inner {
  border-radius: 20px;
  padding-left: 16px;
  height: 44px;
  transition: all 0.3s ease;
  border: 1px solid #e0e0e0;
}

.chat-footer .el-input__inner:focus {
  border-color: #4776E6;
  box-shadow: 0 0 0 2px rgba(71, 118, 230, 0.2);
}

.chat-footer .el-button {
  border-radius: 20px;
  padding: 12px 20px;
  height: 44px;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  border: none;
  font-weight: 600;
  transition: all 0.3s ease;
}

.chat-footer .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(71, 118, 230, 0.3);
}

/* AI 正在思考... 动画 */
.loading-dots span {
  animation: blink 1.4s infinite both;
  font-weight: bold;
}

.loading-dots span:nth-child(1) {
  animation-delay: 0s;
}

.loading-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.loading-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes blink {

  0%,
  80%,
  100% {
    opacity: 0;
  }

  40% {
    opacity: 1;
  }
}

.faq-box {
  padding: 12px 15px;
  border-top: 1px solid #eee;
  background: white;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.faq-box span {
  font-weight: 600;
  color: #555;
  margin-right: 5px;
}

.faq-box .el-button {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  background: #f0f4ff;
  color: #4776E6;
  border: 1px solid rgba(71, 118, 230, 0.2);
  transition: all 0.3s ease;
}

.faq-box .el-button:hover {
  background: rgba(71, 118, 230, 0.1);
  transform: translateY(-2px);
  box-shadow: 0 3px 10px rgba(71, 118, 230, 0.1);
}
</style>