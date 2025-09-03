<template>
  <div class="my-courses-container">
    <!-- 宇宙科技背景 -->
    <div class="cosmic-background">
      <div class="floating-orb orb-1"></div>
      <div class="floating-orb orb-2"></div>
      <div class="floating-orb orb-3"></div>
      <div class="floating-orb orb-4"></div>
      <div class="floating-orb orb-5"></div>
      <div class="floating-orb orb-6"></div>
      <div class="tech-grid"></div>
      <div class="star-field">
        <div class="star" v-for="n in 50" :key="n" :style="getStarStyle(n)"></div>
      </div>
      <div class="nebula nebula-1"></div>
      <div class="nebula nebula-2"></div>
    </div>

    <!-- 页面头部 -->
    <div class="page-header glass-panel">
      <div class="header-decoration">
        <div class="floating-particles">
          <div class="particle"></div>
          <div class="particle"></div>
          <div class="particle"></div>
        </div>
      </div>

      <div class="header-content">
        <div class="header-icon">
          <div class="icon-core">{{ isTeacher ? '🏫' : '🎓' }}</div>
          <div class="icon-ring"></div>
        </div>
        <h2 class="page-title glowing-text">{{ isTeacher ? '我的教学课程' : '我的学习课程' }}</h2>
        <p class="page-subtitle">{{ isTeacher ? '管理您创建的课程和学生，打造优质教育体验' : '探索您的学习之旅，追踪学习进度与成就' }}</p>
        <div class="header-stats">
          <div class="stat-item">
            <div class="stat-icon">📚</div>
            <div class="stat-number">{{ isTeacher ? teachingCourses.length : enrolledCourses.length }}</div>
            <div class="stat-label">{{ isTeacher ? '创建课程' : '已选课程' }}</div>
            <div class="stat-progress">
              <div class="progress-bar" :style="{ width: '100%' }"></div>
            </div>
          </div>
          <div class="stat-item" v-if="isTeacher">
            <div class="stat-icon">👥</div>
            <div class="stat-number">{{ getTotalStudents() }}</div>
            <div class="stat-label">总学生数</div>
            <div class="stat-progress">
              <div class="progress-bar full-progress"></div>
            </div>
          </div>
          <div class="stat-item" v-if="isStudent">
            <div class="stat-icon">⚡</div>
            <div class="stat-number">{{ getAverageProgress() }}%</div>
            <div class="stat-label">平均进度</div>
            <div class="stat-progress">
              <div class="progress-bar online-progress" :style="{ width: getAverageProgress() + '%' }"></div>
            </div>
          </div>
          <div class="stat-item" v-if="isTeacher">
            <div class="stat-icon">✅</div>
            <div class="stat-number">{{ getApprovedCourses() }}</div>
            <div class="stat-label">已审核</div>
            <div class="stat-progress">
              <div class="progress-bar approved-progress"
                :style="{ width: getApprovedCourses() / teachingCourses.length * 100 + '%' }"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 教师创建课程按钮 -->
      <div class="action-section" v-if="isTeacher">
        <div class="action-card glass-panel">
          <div class="action-content">
            <div class="action-header">
              <div class="action-icon">
                <div class="icon-core">✨</div>
                <div class="icon-ring"></div>
              </div>
              <h3 class="action-title">创建新课程</h3>
              <p class="action-subtitle">开始您的教学之旅，分享知识与智慧</p>
            </div>
            <el-button type="primary" @click="createCourse" class="create-btn tech-button premium-button">
              <div class="btn-content">
                <div class="btn-icon">
                  <i class="el-icon-plus"></i>
                  <div class="icon-spin"></div>
                </div>
                <span class="btn-text">创建新课程</span>
                <div class="btn-arrow">→</div>
              </div>
              <div class="btn-glow"></div>
              <div class="btn-particles"></div>
              <div class="btn-wave"></div>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 学生课程网格 -->
      <div v-if="isStudent" class="courses-grid-section">
        <div v-if="enrolledCourses.length === 0" class="empty-state glass-panel">
          <div class="empty-icon">📚</div>
          <h3 class="empty-title">开始您的学习之旅</h3>
          <p class="empty-subtitle">还没有选择任何课程，去探索丰富的课程资源吧！</p>
          <el-button type="primary" @click="$router.push('/courses')" class="explore-btn tech-button premium-button">
            <div class="btn-content">
              <span class="btn-text">去选课</span>
              <div class="btn-icon">🚀</div>
            </div>
            <div class="btn-particles"></div>
          </el-button>
        </div>

        <div v-else class="course-grid">
          <el-row :gutter="24">
            <el-col v-for="(course, index) in enrolledCourses" :key="course.id" :xs="24" :sm="12" :md="8" :lg="6"
              class="course-col" :style="{ '--delay': index * 0.1 + 's' }">
              <div class="course-card-wrapper">
                <div class="course-card glass-element" @click="viewCourse(course.id)">
                  <div class="course-image">
                    <img :src="course.coverUrl || '/default-course.jpg'" :alt="course.title">
                    <div class="image-overlay">
                      <div class="overlay-content">
                        <div class="play-icon">
                          <div class="play-triangle"></div>
                          <div class="play-ring"></div>
                        </div>
                        <span class="overlay-text">继续学习</span>
                        <div class="overlay-stats">
                          <span class="overlay-progress">{{ course.progress || 0 }}%</span>
                          <span class="overlay-teacher">{{ course.teacherName }}</span>
                        </div>
                      </div>
                    </div>
                    <div class="image-shimmer"></div>

                    <div class="progress-indicator">
                      <div class="progress-ring">
                        <el-progress type="circle" :percentage="course.progress || 0" :width="60" :stroke-width="6"
                          :show-text="false" color="#38bdf8" />
                        <div class="progress-text">{{ course.progress || 0 }}%</div>
                      </div>
                    </div>

                    <div class="course-chapter-badge">
                      <span class="chapter-count">{{ course.chapterCount || 0 }} 章节</span>
                    </div>
                  </div>

                  <div class="course-content">
                    <div class="course-header">
                      <h3 class="course-title">{{ course.title }}</h3>
                      <div class="title-glow"></div>
                    </div>

                    <div class="course-teacher">
                      <div class="teacher-avatar">
                        <div class="avatar-ring"></div>
                        <div class="avatar-core">👨‍🏫</div>
                      </div>
                      <div class="teacher-info">
                        <span class="teacher-label">讲师</span>
                        <span class="teacher-name">{{ course.teacherName || '未知教师' }}</span>
                      </div>
                    </div>

                    <div class="course-progress-section">
                      <div class="progress-header">
                        <span class="progress-label">学习进度</span>
                        <span class="progress-percentage">{{ course.progress || 0 }}%</span>
                      </div>
                      <div class="progress-bar-container">
                        <el-progress :percentage="course.progress || 0" :show-text="false" color="#38bdf8"
                          class="tech-progress" />
                      </div>
                      <div class="progress-meta">
                        <span class="last-learn">{{ formatDate(course.lastLearnTime) }}</span>
                      </div>
                    </div>

                    <div class="course-footer">
                      <div class="course-actions">
                        <el-button type="primary" @click.stop="continueLearning(course)"
                          class="continue-btn tech-button">
                          <div class="btn-content">
                            <span class="btn-text">继续学习</span>
                            <div class="btn-icon">▶️</div>
                          </div>
                          <div class="btn-particles"></div>
                          <div class="btn-ripple"></div>
                        </el-button>

                        <el-button type="danger" @click.stop="dropCourse(course)" class="drop-btn tech-button-outline">
                          <div class="btn-content">
                            <div class="btn-icon">
                              <i class="el-icon-close"></i>
                            </div>
                            <span class="btn-text">退课</span>
                          </div>
                          <div class="btn-border-glow"></div>
                        </el-button>
                      </div>
                    </div>
                  </div>

                  <div class="card-glow"></div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>

      <!-- 教师课程表格 -->
      <div v-if="isTeacher" class="teacher-courses-section">
        <div v-if="teachingCourses.length === 0" class="empty-state glass-panel">
          <div class="empty-icon">🏫</div>
          <h3 class="empty-title">开始您的教学之旅</h3>
          <p class="empty-subtitle">还没有创建任何课程，立即创建第一门课程分享您的知识！</p>
          <el-button type="primary" @click="createCourse" class="create-btn tech-button premium-button">
            <div class="btn-content">
              <span class="btn-text">创建课程</span>
              <div class="btn-icon">✨</div>
            </div>
            <div class="btn-particles"></div>
          </el-button>
        </div>

        <div v-else class="courses-table-container glass-panel">
          <div class="table-header">
            <div class="table-title">
              <div class="title-icon">📊</div>
              <h3>课程管理</h3>
              <div class="title-decoration"></div>
            </div>
            <div class="table-subtitle">管理您的课程和学生</div>
          </div>

          <el-table :data="teachingCourses" class="tech-table" :header-cell-style="{
            background: 'rgba(0, 0, 0, 0.4)',
            border: 'none',
            color: 'rgba(255, 255, 255, 0.95)',
            fontWeight: '700',
            textShadow: '0 0 8px rgba(0, 0, 0, 0.8)',
            fontSize: '0.95rem'
            }" :cell-style="{ background: 'transparent', border: 'none' }">
            <el-table-column prop="title" label="课程信息" min-width="200">
              <template #default="scope">
                <div class="course-info-cell" @click="viewCourse(scope.row.id)">
                  <div class="course-avatar">
                    <img :src="scope.row.coverUrl || '/default-course.jpg'" :alt="scope.row.title" />
                    <div class="avatar-overlay"></div>
                  </div>
                  <div class="course-details">
                    <div class="course-name">{{ scope.row.title }}</div>
                    <div class="course-category">{{ scope.row.category || '其他' }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column prop="createTime" label="创建时间" width="230" align="center">
              <template #default="scope">
                <div class="time-cell">
                  <div class="time-icon">📅</div>
                  <span>{{ formatDate(scope.row.createTime) }}</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column prop="studentCount" label="学生数" width="120" align="center">
              <template #default="scope">
                <div class="student-count-cell">
                  <div class="count-icon">👥</div>
                  <span class="count-number">{{ scope.row.studentCount || 0 }}</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column prop="state" label="状态" width="120" align="center">
              <template #default="scope">
                <div class="status-cell">
                  <el-tag :type="getStateType(scope.row.state)" class="tech-tag">
                    <div class="tag-content">
                      <div class="tag-icon">{{ getStateIcon(scope.row.state) }}</div>
                      <span>{{ getStateText(scope.row.state) }}</span>
                    </div>
                  </el-tag>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="300" align="center">
              <template #default="scope">
                <div class="action-buttons">
                  <el-button type="primary" size="small" @click="viewCourse(scope.row.id)"
                    class="action-btn tech-button">
                    <div class="btn-content">
                      <div class="btn-icon">👁️</div>
                      <span>查看</span>
                    </div>
                  </el-button>

                  <el-button type="success" size="small" @click="viewStudents(scope.row)"
                    class="action-btn tech-button">
                    <div class="btn-content">
                      <div class="btn-icon">👥</div>
                      <span>学生</span>
                    </div>
                  </el-button>

                  <el-button v-if="scope.row.status !== 'approved'" type="warning" size="small"
                    @click="editCourse(scope.row)" class="action-btn tech-button">
                    <div class="btn-content">
                      <div class="btn-icon">✏️</div>
                      <span>编辑</span>
                    </div>
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- 学生名单对话框 -->
    <el-dialog v-model="studentsDialogVisible" :title="`${currentCourse?.title || ''} - 学生名单`" width="68%"
      class="tech-dialog">
      <div class="students-dialog-content">
        <el-table :data="students" class="students-table tech-table" :header-cell-style="{
          background: 'rgba(0, 0, 0, 0.4)',
          border: 'none',
          color: 'rgba(255, 255, 255, 0.95)',
          fontWeight: '700',
          textShadow: '0 0 8px rgba(0, 0, 0, 0.8)'
        }" :cell-style="{ background: 'transparent', border: 'none' }">
          <el-table-column prop="username" label="学生姓名" min-width="120">
            <template #default="scope">
              <div class="student-name-cell">
                <div class="student-avatar">👨‍🎓</div>
                <span>{{ scope.row.username }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="email" label="邮箱" min-width="180" />

          <el-table-column prop="enrollTime" label="选课时间" width="160">
            <template #default="scope">
              <div class="time-cell">
                <div class="time-icon">📅</div>
                <span>{{ formatDate(scope.row.enrollTime) }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="progress" label="学习进度" width="150">
            <template #default="scope">
              <div class="progress-cell">
                <el-progress :percentage="scope.row.progress || 0" :show-text="false" color="#38bdf8" />
                <span class="progress-text">{{ scope.row.progress || 0 }}%</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="150" align="center">
            <template #default="scope">
              <el-button type="primary" size="small" @click="viewStudentDetail(scope.row)" class="tech-button">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="dialog-footer">
          <el-button @click="exportStudentList" type="success" class="tech-button">
            <div class="btn-content">
              <div class="btn-icon">📊</div>
              <span>导出名单</span>
            </div>
          </el-button>
          <el-button @click="studentsDialogVisible = false" class="tech-button-outline">
            关闭
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { enrollmentApi } from '../../api/enrollment';
import { courseApi } from '../../api/course';
import { userinfoApi } from '../../api/userinfo';
import { useAuthStore } from '../../store/auth';

const router = useRouter();
const authStore = useAuthStore();

// 用户角色
const isTeacher = computed(() => authStore.hasPermission('teacher'));
const isStudent = computed(() => authStore.hasPermission('student'));

// 当前激活的标签页
const activeTab = ref(isTeacher.value ? 'teaching' : 'enrolled');

// 课程数据
const enrolledCourses = ref([]);
const teachingCourses = ref([]);

// 学生名单相关
const studentsDialogVisible = ref(false);
const currentCourse = ref(null);
const students = ref([]);

// 美化功能相关方法
const getStarStyle = (index) => {
  return {
    left: Math.random() * 100 + '%',
    top: Math.random() * 100 + '%',
    animationDelay: Math.random() * 3 + 's',
    animationDuration: (Math.random() * 2 + 1) + 's'
  };
};

// 统计数据方法
const getTotalStudents = () => {
  return teachingCourses.value.reduce((total, course) => total + (course.studentCount || 0), 0);
};

const getAverageProgress = () => {
  if (enrolledCourses.value.length === 0) return 0;
  const totalProgress = enrolledCourses.value.reduce((total, course) => total + (course.progress || 0), 0);
  return Math.round(totalProgress / enrolledCourses.value.length);
};

const getApprovedCourses = () => {
  return teachingCourses.value.filter(course => course.state === 1).length;
};

// 获取学生已选课程
const fetchEnrolledCourses = async () => {
  try {
    // 获取选课信息和进度
    const enrollments = await enrollmentApi.getMyEnrollments();

    if (!Array.isArray(enrollments) || enrollments.length === 0) {
      enrolledCourses.value = [];
      return;
    }

    // 获取课程详细信息
    const coursePromises = enrollments.map(enrollment =>
      courseApi.getCourseDetail(enrollment.courseId)
    );
    const courses = await Promise.all(coursePromises);

    // 获取所有教师的详细信息
    const profileData = await userinfoApi.getUserProfile({ pageNum: 1, pageSize: 100 });

    // 合并课程信息、选课信息和教师信息
    enrolledCourses.value = enrollments.map((enrollment, index) => {
      const course = courses[index].course;

      // 查找教师真实姓名
      let teacherName = '未知教师';
      if (course && course.teacherId && profileData && profileData.records) {

        for (let key in profileData.records) {
          if (typeof profileData.records[key] === 'object' &&
            profileData.records[key].userId === course.teacherId) {
            teacherName = profileData.records[key].realName || '未知教师';
            break;
          }
        }
      }

      return {
        ...course,
        progress: enrollment.progress || 0,
        enrollTime: enrollment.enrollTime,
        teacherName: teacherName
      };
    });
  } catch (error) {
    console.error('获取已选课程失败:', error);
    ElMessage.error('获取已选课程失败');
  }
};

// 获取教师课程
const fetchTeachingCourses = async () => {
  try {
    const res = await enrollmentApi.getMyCoursesWithStudents();
    // API返回的是包含course和students的对象数组
    if (Array.isArray(res)) {
      teachingCourses.value = res.map(item => ({
        ...item.course,
        students: item.students || [],
        studentCount: item.students ? item.students.length : 0
      }));
      console.log('teachingCourses', teachingCourses.value);
    } else {
      teachingCourses.value = [];
    }
  } catch (error) {
    console.error('获取教学课程失败:', error);
    ElMessage.error('获取教学课程失败');
  }
};

// 查看课程详情
const viewCourse = (courseId) => {
  router.push(`/course/${courseId}`);
};

// 继续学习
const continueLearning = (course) => {
  // 如果有上次学习的章节，直接跳转到该章节
  if (course.lastLearnChapterId) {
    router.push(`/course/${course.id}/chapter/${course.lastLearnChapterId}`);
  } else {
    // 否则跳转到课程详情页
    router.push(`/course/${course.id}`);
  }
};

// 退课
const dropCourse = async (course) => {
  try {
    await ElMessageBox.confirm(
      `确定要退出课程「${course.title}」吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    await enrollmentApi.dropCourse(course.id);
    ElMessage.success('退课成功');
    // 刷新课程列表
    fetchEnrolledCourses();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退课失败:', error);
      ElMessage.error('退课失败');
    }
  }
};

// 创建课程
const createCourse = () => {
  router.push('/course/create');
};

// 编辑课程
const editCourse = (course) => {
  router.push(`/course/edit/${course.id}`);
};

// 查看学生名单
const viewStudents = async (course) => {
  currentCourse.value = course;
  studentsDialogVisible.value = true;

  try {
    // 课程数据中已经包含了学生列表
    students.value = course.students || [];
  } catch (error) {
    console.error('获取学生名单失败:', error);
    ElMessage.error('获取学生名单失败');
  }
};

// 查看学生详情
const viewStudentDetail = (student) => {
  // 跳转到学生详情页面，或者显示学生详情对话框
  ElMessage.info('查看学生详情功能开发中');
};

// 导出学生名单
const exportStudentList = async () => {
  if (!currentCourse.value) return;

  try {
    const res = await enrollmentApi.exportEnrollments(currentCourse.value.id);
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `${currentCourse.value.title}-学生名单.xlsx`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    ElMessage.success('导出成功');
  } catch (error) {
    console.error('导出学生名单失败:', error);
    ElMessage.error('导出学生名单失败');
  }
};

// 获取课程状态类型
const getStateType = (state) => {
  const statusMap = {
    0: 'info',     // 冻结
    1: 'success',  // 正常
    2: 'danger'    // 封禁 
  };
  return statusMap[state] || 'info';
};

// 获取课程状态文本
const getStateText = (state) => {
  const statusMap = {
    0: '冻结',
    1: '正常',
    2: '封禁'
  };
  return statusMap[state] || '未知状态';
};

// 获取状态图标
const getStateIcon = (state) => {
  const iconMap = {
    0: '❄️',
    1: '✅',
    2: '🚫'
  };
  return iconMap[state] || '❓';
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '暂无记录';
  const date = new Date(dateString);
  return date.toLocaleString();
};

onMounted(async () => {
  if (isStudent.value) {
    await fetchEnrolledCourses();
  }

  if (isTeacher.value) {
    await fetchTeachingCourses();
  }
});
</script>

<style scoped>
/* 基础样式和布局 */
.my-courses-container {
  position: relative;
  min-height: 100vh;
  background: linear-gradient(135deg, #0f172a 0%, #1e1b4b 50%, #312e81 100%);
  padding: 20px;
  overflow: hidden;
}

/* 宇宙科技背景 */
.cosmic-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

/* 浮动光球 */
.floating-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(1px);
  animation: floatOrb 20s infinite linear;
}

.orb-1 {
  width: 120px;
  height: 120px;
  background: radial-gradient(circle at center, #3b82f6 0%, transparent 70%);
  top: 10%;
  left: 10%;
  animation-duration: 25s;
}

.orb-2 {
  width: 80px;
  height: 80px;
  background: radial-gradient(circle at center, #8b5cf6 0%, transparent 70%);
  top: 70%;
  right: 15%;
  animation-duration: 30s;
  animation-direction: reverse;
}

.orb-3 {
  width: 60px;
  height: 60px;
  background: radial-gradient(circle at center, #06b6d4 0%, transparent 70%);
  top: 30%;
  right: 30%;
  animation-duration: 35s;
}

.orb-4 {
  width: 100px;
  height: 100px;
  background: radial-gradient(circle at center, #10b981 0%, transparent 70%);
  bottom: 20%;
  left: 20%;
  animation-duration: 28s;
  animation-direction: reverse;
}

.orb-5 {
  width: 40px;
  height: 40px;
  background: radial-gradient(circle at center, #f59e0b 0%, transparent 70%);
  top: 50%;
  left: 50%;
  animation-duration: 22s;
}

.orb-6 {
  width: 90px;
  height: 90px;
  background: radial-gradient(circle at center, #ef4444 0%, transparent 70%);
  bottom: 40%;
  right: 40%;
  animation-duration: 32s;
  animation-direction: reverse;
}

@keyframes floatOrb {

  0%,
  100% {
    transform: translate(0, 0) scale(1);
  }

  25% {
    transform: translate(30px, -30px) scale(1.1);
  }

  50% {
    transform: translate(-20px, -50px) scale(0.9);
  }

  75% {
    transform: translate(-40px, 20px) scale(1.05);
  }
}

/* 科技网格 */
.tech-grid {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0.03;
  background-image:
    linear-gradient(rgba(59, 130, 246, 0.3) 1px, transparent 1px),
    linear-gradient(90deg, rgba(59, 130, 246, 0.3) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: gridMove 30s linear infinite;
}

@keyframes gridMove {
  0% {
    transform: translate(0, 0);
  }

  100% {
    transform: translate(50px, 50px);
  }
}

/* 星空背景 */
.star-field {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.star {
  position: absolute;
  width: 2px;
  height: 2px;
  background: white;
  border-radius: 50%;
  animation: twinkle 3s infinite;
}

@keyframes twinkle {

  0%,
  100% {
    opacity: 0.3;
    transform: scale(1);
  }

  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}

/* 星云效果 */
.nebula {
  position: absolute;
  border-radius: 50%;
  filter: blur(40px);
  animation: nebulaPulse 15s infinite ease-in-out;
}

.nebula-1 {
  width: 300px;
  height: 200px;
  background: radial-gradient(ellipse at center, rgba(139, 92, 246, 0.2) 0%, transparent 70%);
  top: 20%;
  left: 10%;
}

.nebula-2 {
  width: 250px;
  height: 180px;
  background: radial-gradient(ellipse at center, rgba(59, 130, 246, 0.15) 0%, transparent 70%);
  bottom: 30%;
  right: 20%;
  animation-delay: -7s;
}

@keyframes nebulaPulse {

  0%,
  100% {
    opacity: 0.2;
    transform: scale(1);
  }

  50% {
    opacity: 0.4;
    transform: scale(1.1);
  }
}

/* 玻璃拟态效果 */
.glass-panel {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  box-shadow:
    0 20px 40px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  position: relative;
  z-index: 1;
}

.glass-element {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.glass-element:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.15);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  transform: translateY(-5px);
}

/* 页面头部样式 */
.page-header {
  margin-bottom: 30px;
  padding: 40px;
  position: relative;
  overflow: hidden;
  animation: slideInDown 0.8s ease-out;
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(139, 92, 246, 0.1) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.page-header:hover::before {
  opacity: 1;
}

@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-50px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(50px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.header-decoration {
  position: absolute;
  top: 20px;
  right: 20px;
}

.floating-particles {
  position: relative;
  width: 60px;
  height: 60px;
}

.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: #3b82f6;
  border-radius: 50%;
  animation: particleFloat 4s infinite ease-in-out;
}

.particle:nth-child(1) {
  top: 10px;
  left: 10px;
  animation-delay: 0s;
}

.particle:nth-child(2) {
  top: 30px;
  left: 40px;
  animation-delay: 1.3s;
}

.particle:nth-child(3) {
  top: 50px;
  left: 20px;
  animation-delay: 2.6s;
}

@keyframes particleFloat {

  0%,
  100% {
    transform: translateY(0);
    opacity: 1;
  }

  50% {
    transform: translateY(-20px);
    opacity: 0.5;
  }
}

.header-content {
  position: relative;
  z-index: 2;
}

.header-icon {
  display: inline-block;
  position: relative;
  margin-bottom: 20px;
}

.icon-core {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  border-radius: 50%;
  position: relative;
  z-index: 2;
  box-shadow: 0 10px 30px rgba(59, 130, 246, 0.3);
}

.icon-ring {
  position: absolute;
  top: -10px;
  left: -10px;
  width: 100px;
  height: 100px;
  border: 2px solid rgba(59, 130, 246, 0.3);
  border-radius: 50%;
  animation: iconRingPulse 3s infinite;
}

@keyframes iconRingPulse {

  0%,
  100% {
    transform: scale(1);
    opacity: 1;
  }

  50% {
    transform: scale(1.2);
    opacity: 0.5;
  }
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 0 10px 0;
  background: linear-gradient(135deg, #ffffff 0%, #e2e8f0 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
}

.glowing-text {
  position: relative;
  text-align: center;
}

.page-subtitle {
  color: rgba(255, 255, 255, 0.7);
  font-size: 1.1rem;
  margin-bottom: 30px;
  line-height: 1.6;
  text-align: center;
}

/* 统计卡片样式 */
.header-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 20px;
  margin-top: 30px;
}

.stat-item {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 20px;
  text-align: center;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.stat-item:hover {
  background: rgba(255, 255, 255, 0.08);
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.stat-icon {
  font-size: 2rem;
  margin-bottom: 10px;
  display: block;
}

.stat-number {
  font-size: 2rem;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 5px;
  display: block;
}

.stat-label {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
  margin-bottom: 10px;
  display: block;
}

.stat-progress {
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
  position: relative;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6 0%, #8b5cf6 100%);
  border-radius: 2px;
  transition: width 1s ease;
  position: relative;
}

/* 高级按钮样式 */
.tech-button {
  position: relative;
  overflow: hidden;
  border: none;
  border-radius: 12px;
  padding: 10px;
  background: transparent;
  cursor: pointer;
  transition: all 0.3s ease;
}

.premium-button {
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  box-shadow: 0 8px 25px rgba(59, 130, 246, 0.3);
}

.premium-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 15px 40px rgba(59, 130, 246, 0.4);
  background: linear-gradient(135deg, #2563eb 0%, #7c3aed 100%);
}

.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 15px 30px;
  position: relative;
  z-index: 2;
  font-weight: 600;
  font-size: 1rem;
}

.btn-icon {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-text {
  position: relative;
  z-index: 1;
}

.btn-particles {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  border-radius: inherit;
  overflow: hidden;
}

/* 主要内容区域 */
.main-content {
  position: relative;
  z-index: 1;
}

/* 操作区域样式 */
.action-section {
  margin-bottom: 30px;
  animation: slideInLeft 0.8s ease-out 0.2s both;
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-50px);
  }

  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.action-card {
  padding: 30px;
  transition: all 0.3s ease;
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);
}

.action-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 20px;
}

.action-header {
  flex: 1;
  min-width: 300px;
}

.action-icon {
  display: inline-block;
  position: relative;
  margin-bottom: 15px;
}

.action-icon .icon-core {
  width: 60px;
  height: 60px;
  font-size: 24px;
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  box-shadow: 0 8px 25px rgba(245, 158, 11, 0.3);
}

.action-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #ffffff;
  margin: 0 0 8px 0;
}

.action-subtitle {
  color: rgba(255, 255, 255, 0.7);
  font-size: 1rem;
  margin: 0;
  line-height: 1.5;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 60px 40px;
  animation: fadeIn 0.8s ease-out 0.4s both;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  display: block;
  animation: bounce 2s infinite;
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
    transform: translateY(-10px);
  }

  60% {
    transform: translateY(-5px);
  }
}

.empty-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #ffffff;
  margin: 0 0 10px 0;
}

.empty-subtitle {
  color: rgba(255, 255, 255, 0.7);
  font-size: 1rem;
  margin: 0 0 30px 0;
  line-height: 1.6;
}

/* 课程网格样式 */
.courses-grid-section {
  animation: slideInUp 0.8s ease-out 0.3s both;
}

.course-grid {
  position: relative;
}

.course-col {
  animation: slideInUp 0.6s ease-out both;
  animation-delay: var(--delay, 0s);
}

.course-card-wrapper {
  height: 100%;
  perspective: 1000px;
}

.course-card {
  height: 100%;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  transform-style: preserve-3d;
  position: relative;
  overflow: hidden;
}

.course-card:hover {
  transform: translateY(-10px) rotateX(5deg);
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.4);
}

/* 课程图片样式 */
.course-image {
  position: relative;
  height: 200px;
  overflow: hidden;
  border-radius: 16px 16px 0 0;
}

.course-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.4s ease;
}

.course-card:hover .course-image img {
  transform: scale(1.1);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(0, 0, 0, 0.3) 0%, rgba(0, 0, 0, 0.7) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  opacity: 0;
  transition: all 0.3s ease;
}

.course-card:hover .image-overlay {
  opacity: 1;
}

.overlay-content {
  text-align: center;
  color: white;
}

.play-icon {
  position: relative;
  width: 60px;
  height: 60px;
  margin: 0 auto 15px;
}

.play-triangle {
  width: 0;
  height: 0;
  border-left: 20px solid rgba(255, 255, 255, 0.9);
  border-top: 12px solid transparent;
  border-bottom: 12px solid transparent;
  position: absolute;
  top: 50%;
  left: 55%;
  transform: translate(-50%, -50%);
}

.play-ring {
  position: absolute;
  top: 0;
  left: 0;
  width: 60px;
  height: 60px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  animation: playRingPulse 2s infinite;
}

@keyframes playRingPulse {

  0%,
  100% {
    transform: scale(1);
    opacity: 1;
  }

  50% {
    transform: scale(1.2);
    opacity: 0.7;
  }
}

.overlay-text {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 10px;
  display: block;
}

.overlay-stats {
  display: flex;
  justify-content: center;
  gap: 15px;
  font-size: 0.9rem;
}

.overlay-progress,
.overlay-teacher {
  padding: 4px 8px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  backdrop-filter: blur(10px);
}

/* 进度指示器 */
.progress-indicator {
  position: absolute;
  top: 15px;
  right: 15px;
  z-index: 2;
}

.progress-ring {
  position: relative;
  width: 60px;
  height: 60px;
}

.progress-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 0.8rem;
  font-weight: 600;
  color: #ffffff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.course-chapter-badge {
  position: absolute;
  bottom: 15px;
  left: 15px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 4px 8px;
  border-radius: 8px;
  font-size: 0.8rem;
  backdrop-filter: blur(10px);
}

.chapter-count {
  font-weight: 500;
}

/* 课程内容样式 */
.course-content {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.course-header {
  position: relative;
  margin-bottom: 15px;
}

.course-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #ffffff;
  margin: 0;
  line-height: 1.4;
  cursor: pointer;
  transition: all 0.3s ease;
}

.course-title:hover {
  color: #3b82f6;
  transform: translateX(5px);
}

.title-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  opacity: 0;
  filter: blur(10px);
  transition: opacity 0.3s ease;
  z-index: -1;
}

.course-title:hover+.title-glow {
  opacity: 0.3;
}

/* 教师信息样式 */
.course-teacher {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
}

.teacher-avatar {
  position: relative;
  width: 40px;
  height: 40px;
}

.avatar-ring {
  position: absolute;
  top: -3px;
  left: -3px;
  width: 46px;
  height: 46px;
  border: 2px solid rgba(59, 130, 246, 0.3);
  border-radius: 50%;
  animation: avatarRingPulse 3s infinite;
}

@keyframes avatarRingPulse {

  0%,
  100% {
    transform: scale(1);
    opacity: 1;
  }

  50% {
    transform: scale(1.1);
    opacity: 0.6;
  }
}

.avatar-core {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  border-radius: 50%;
  position: relative;
  z-index: 2;
}

.teacher-info {
  flex: 1;
}

.teacher-label {
  display: block;
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.8rem;
  margin-bottom: 2px;
}

.teacher-name {
  display: block;
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.9rem;
  font-weight: 500;
}

/* 进度条样式 */
.course-progress-section {
  margin-bottom: 20px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-label {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
}

.progress-percentage {
  color: #3b82f6;
  font-weight: 600;
  font-size: 0.9rem;
}

.progress-bar-container {
  margin-bottom: 8px;
}

:deep(.tech-progress) {
  background: rgba(255, 255, 255, 0.1) !important;
}

:deep(.tech-progress .el-progress-bar__outer) {
  background: rgba(255, 255, 255, 0.1) !important;
  border-radius: 10px;
  height: 8px !important;
}

:deep(.tech-progress .el-progress-bar__inner) {
  background: linear-gradient(90deg, #3b82f6 0%, #8b5cf6 100%) !important;
  border-radius: 10px;
  position: relative;
}

:deep(.tech-progress .el-progress-bar__inner::after) {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: progressGlow 2s infinite;
}

@keyframes progressGlow {
  0% {
    transform: translateX(-100%);
  }

  100% {
    transform: translateX(100%);
  }
}

.progress-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.last-learn {
  color: rgba(255, 255, 255, 0.5);
  font-size: 0.8rem;
}

/* 课程底部样式 */
.course-footer {
  margin-top: auto;
}

.course-actions {
  display: flex;
  gap: 10px;
  padding-top: 15px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.continue-btn {
  flex: 1;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  border: none;
  border-radius: 10px;
  color: white;
  font-weight: 500;
  transition: all 0.3s ease;
}

.continue-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.4);
  background: linear-gradient(135deg, #2563eb 0%, #7c3aed 100%);
}

.drop-btn {
  background: transparent;
  border: 1px solid rgba(239, 68, 68, 0.5);
  color: #ef4444;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.drop-btn:hover {
  background: rgba(239, 68, 68, 0.1);
  border-color: #ef4444;
  transform: translateY(-2px);
}

.tech-button-outline {
  position: relative;
  overflow: hidden;
}

.btn-border-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: inherit;
  background: linear-gradient(90deg, transparent, rgba(239, 68, 68, 0.3), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.tech-button-outline:hover .btn-border-glow {
  opacity: 1;
  animation: borderGlow 1.5s infinite;
}

@keyframes borderGlow {
  0% {
    transform: translateX(-100%);
  }

  100% {
    transform: translateX(100%);
  }
}

.btn-ripple {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: all 0.6s ease;
}

.continue-btn:active .btn-ripple {
  width: 200px;
  height: 200px;
}

.card-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(139, 92, 246, 0.1) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: inherit;
  pointer-events: none;
}

.course-card:hover .card-glow {
  opacity: 1;
}

/* 教师课程表格样式 */
.teacher-courses-section {
  animation: slideInUp 0.8s ease-out 0.4s both;
}

.courses-table-container {
  padding: 30px;
  margin-top: 20px;
  background: rgba(0, 0, 0, 0.2) !important;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.table-header {
  margin-bottom: 30px;
  text-align: center;
}

.table-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  margin-bottom: 10px;
  position: relative;
}

.title-icon {
  font-size: 2rem;
  animation: iconBounce 2s infinite;
}

@keyframes iconBounce {

  0%,
  20%,
  50%,
  80%,
  100% {
    transform: translateY(0);
  }

  40% {
    transform: translateY(-8px);
  }

  60% {
    transform: translateY(-4px);
  }
}

.table-title h3 {
  font-size: 1.8rem;
  font-weight: 600;
  color: #ffffff;
  margin: 0;
  background: linear-gradient(135deg, #ffffff 0%, #e2e8f0 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title-decoration {
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(59, 130, 246, 0.3), transparent);
  z-index: -1;
}

.table-subtitle {
  color: rgba(255, 255, 255, 0.7);
  font-size: 1rem;
}

/* 表格样式 */
.tech-table {
  background: transparent !important;
  border-radius: 16px;
  overflow: hidden;
}

:deep(.tech-table) {
  background: transparent !important;
}

:deep(.tech-table .el-table__header-wrapper) {
  background: rgba(0, 0, 0, 0.4) !important;
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(59, 130, 246, 0.3);
}

:deep(.tech-table .el-table__body-wrapper) {
  background: transparent !important;
}

:deep(.tech-table .el-table__row) {
  background: rgba(255, 255, 255, 0.02) !important;
  transition: all 0.3s ease;
}

:deep(.tech-table .el-table__row:hover) {
  background: rgba(255, 255, 255, 0.08) !important;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

:deep(.tech-table th) {
  background: rgba(0, 0, 0, 0.4) !important;
  border: none !important;
  color: rgba(255, 255, 255, 0.95) !important;
  font-weight: 700;
  font-size: 0.95rem;
  padding: 20px 15px;
  text-shadow: 0 0 8px rgba(0, 0, 0, 0.8);
  position: relative;
}

:deep(.tech-table th::before) {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(139, 92, 246, 0.1) 100%);
  pointer-events: none;
}

:deep(.tech-table th:hover) {
  background: rgba(0, 0, 0, 0.6) !important;
  color: #ffffff !important;
  text-shadow: 0 0 12px rgba(59, 130, 246, 0.6);
}

:deep(.tech-table td) {
  border: none !important;
  padding: 15px;
  color: rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.02);
}

/* 表格单元格样式 */
.course-info-cell {
  display: flex;
  align-items: center;
  gap: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 8px;
  border-radius: 10px;
}

.course-info-cell:hover {
  background: rgba(59, 130, 246, 0.1);
  transform: translateX(5px);
}

.course-avatar {
  position: relative;
  width: 50px;
  height: 50px;
  border-radius: 10px;
  overflow: hidden;
}

.course-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.2) 0%, rgba(139, 92, 246, 0.2) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.course-info-cell:hover .avatar-overlay {
  opacity: 1;
}

.course-details {
  flex: 1;
}

.course-name {
  font-size: 1rem;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 4px;
}

.course-category {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.6);
}

.time-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.8);
}

.time-icon {
  font-size: 1rem;
}

.student-count-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.count-icon {
  font-size: 1rem;
}

.count-number {
  font-weight: 600;
  color: #3b82f6;
}

.status-cell {
  display: flex;
  justify-content: center;
}

.tech-tag {
  border: none !important;
  border-radius: 20px !important;
  padding: 8px 16px !important;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.2) 0%, rgba(139, 92, 246, 0.2) 100%) !important;
  backdrop-filter: blur(10px);
  color: #ffffff !important;
  font-weight: 500;
  position: relative;
  overflow: hidden;
}

.tech-tag::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.tech-tag:hover::before {
  left: 100%;
}

.tag-content {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tag-icon {
  font-size: 0.9rem;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.action-btn {
  padding: 8px 16px !important;
  border-radius: 10px !important;
  font-size: 0.85rem !important;
  font-weight: 500;
  transition: all 0.3s ease !important;
  position: relative;
  overflow: hidden;
}

.action-btn .btn-content {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0;
}

.action-btn .btn-icon {
  font-size: 0.9rem;
}

/* 对话框样式 */
:deep(.tech-dialog) {
  background: rgba(15, 23, 42, 0.95) !important;
  border-radius: 20px !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(20px);
  text-align: center;
}

:deep(.tech-dialog .el-dialog__header) {
  background: rgba(255, 255, 255, 0.05);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px 20px 0 0;
  padding: 20px 30px;

}

:deep(.tech-dialog .el-dialog__title) {
  color: #ffffff !important;
  font-size: 1.2rem;
  font-weight: 600;
}

:deep(.tech-dialog .el-dialog__body) {
  padding: 30px;
  background: transparent;
}

.students-dialog-content {
  min-height: 400px;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  padding: 20px;
}

.students-table {
  background: transparent !important;
  border-radius: 8px;
  overflow: hidden;
}

.student-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.student-avatar {
  width: 35px;
  height: 35px;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
}

/* Element Plus 按钮美化 */
:deep(.el-button--primary) {
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.2);
  border-radius: 10px;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover) {
  transform: translateY(-3px);
  box-shadow: 0 12px 35px rgba(59, 130, 246, 0.4);
  background: linear-gradient(135deg, #2563eb 0%, #7c3aed 100%);
}

:deep(.el-button--success) {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 15px rgba(16, 185, 129, 0.2);
}

:deep(.el-button--success:hover) {
  transform: translateY(-3px);
  box-shadow: 0 12px 35px rgba(16, 185, 129, 0.4);
  background: linear-gradient(135deg, #059669 0%, #047857 100%);
}

:deep(.el-button--warning) {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 15px rgba(245, 158, 11, 0.2);
}

:deep(.el-button--warning:hover) {
  transform: translateY(-3px);
  box-shadow: 0 12px 35px rgba(245, 158, 11, 0.4);
  background: linear-gradient(135deg, #d97706 0%, #b45309 100%);
}

:deep(.el-button--danger) {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 15px rgba(239, 68, 68, 0.2);
}

:deep(.el-button--danger:hover) {
  transform: translateY(-3px);
  box-shadow: 0 12px 35px rgba(239, 68, 68, 0.4);
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .header-stats {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  }

  .action-content {
    flex-direction: column;
    text-align: center;
  }

  .action-header {
    min-width: auto;
  }
}

@media (max-width: 768px) {
  .my-courses-container {
    padding: 15px;
  }

  .page-header {
    padding: 25px 20px;
    margin-bottom: 20px;
  }

  .page-title {
    font-size: 2rem;
  }

  .header-stats {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }

  .stat-item {
    padding: 15px;
  }

  .stat-number {
    font-size: 1.5rem;
  }

  .action-card {
    padding: 20px;
  }

  .action-title {
    font-size: 1.2rem;
  }

  .course-image {
    height: 160px;
  }

  .course-content {
    padding: 15px;
  }

  .course-title {
    font-size: 1.1rem;
  }

  .course-actions {
    flex-direction: column;
    gap: 8px;
  }

  .courses-table-container {
    padding: 20px 15px;
  }

  .action-buttons {
    flex-direction: column;
    gap: 6px;
  }

  .action-btn {
    width: 100% !important;
  }

  :deep(.tech-dialog) {
    width: 95% !important;
    margin: 5vh auto !important;
  }
}

@media (max-width: 480px) {
  .my-courses-container {
    padding: 10px;
  }

  .page-header {
    padding: 20px 15px;
  }

  .page-title {
    font-size: 1.5rem;
  }

  .header-stats {
    grid-template-columns: 1fr;
  }

  .icon-core {
    width: 60px;
    height: 60px;
    font-size: 24px;
  }

  .icon-ring {
    width: 80px;
    height: 80px;
  }

  .floating-particles {
    display: none;
  }

  .course-image {
    height: 140px;
  }

  .progress-ring {
    width: 50px;
    height: 50px;
  }

  .course-info-cell {
    flex-direction: column;
    text-align: center;
    gap: 10px;
  }

  .course-avatar {
    align-self: center;
  }
}

/* 动画优化 */
@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

/* 触摸设备优化 */
@media (hover: none) and (pointer: coarse) {

  .course-card:hover,
  .glass-element:hover,
  .tech-button:hover,
  .stat-item:hover {
    transform: none;
  }

  .course-card,
  .glass-element,
  .tech-button,
  .stat-item {
    transition: none;
  }
}

/* 高对比度模式支持 */
@media (prefers-contrast: high) {

  .glass-panel,
  .glass-element {
    background: rgba(0, 0, 0, 0.8);
    border-color: rgba(255, 255, 255, 0.5);
  }

  .page-title,
  .course-title,
  .stat-number {
    color: #ffffff;
    -webkit-text-fill-color: #ffffff;
  }
}

:deep(.el-button--primary:active) {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.3);
}

:deep(.el-button--success) {
  background: linear-gradient(135deg, #48bb78 0%, #38b2ac 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 15px rgba(72, 187, 120, 0.2);
}

:deep(.el-button--success:hover) {
  transform: translateY(-3px);
  box-shadow: 0 12px 35px rgba(72, 187, 120, 0.4);
  background: linear-gradient(135deg, #38a169 0%, #319795 100%);
}

:deep(.el-button--info) {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(247, 250, 252, 0.9) 100%);
  border: 1px solid rgba(226, 232, 240, 0.8);
  color: #4a5568;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

:deep(.el-button--info:hover) {
  transform: translateY(-3px);
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, rgba(255, 255, 255, 1) 0%, rgba(247, 250, 252, 1) 100%);
  border-color: rgba(203, 213, 224, 0.8);
}

:deep(.el-button--warning) {
  background: linear-gradient(135deg, #ed8936 0%, #dd6b20 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 15px rgba(237, 137, 54, 0.2);
}

:deep(.el-button--warning:hover) {
  transform: translateY(-3px);
  box-shadow: 0 12px 35px rgba(237, 137, 54, 0.4);
  background: linear-gradient(135deg, #c05621 0%, #9c4221 100%);
}

:deep(.el-button--danger) {
  background: linear-gradient(135deg, #f56565 0%, #e53e3e 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 15px rgba(245, 101, 101, 0.2);
}

:deep(.el-button--danger:hover) {
  transform: translateY(-3px);
  box-shadow: 0 12px 35px rgba(245, 101, 101, 0.4);
  background: linear-gradient(135deg, #e53e3e 0%, #c53030 100%);
}

:deep(.el-button.is-loading) {
  position: relative;
}

:deep(.el-button.is-loading::after) {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, transparent 30%, rgba(255, 255, 255, 0.3) 50%, transparent 70%);
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }

  100% {
    transform: translateX(100%);
  }
}

/* 页面过渡动画 */
.page-fade-enter-active {
  transition: all 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(30px);
}

/* 元素进入动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(50px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-50px);
  }

  to {
    opacity: 1;
    transform: translateX(0);
  }
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

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* 动画类 */
.animate-fade-in {
  animation: fadeIn 0.8s ease-out forwards;
  opacity: 0;
}

.animate-slide-up {
  animation: slideUp 0.8s ease-out forwards;
  opacity: 0;
}

.animate-slide-left {
  animation: slideInLeft 0.8s ease-out forwards;
  opacity: 0;
}

.animate-slide-right {
  animation: slideInRight 0.8s ease-out forwards;
  opacity: 0;
}

.animate-scale-in {
  animation: scaleIn 0.8s ease-out forwards;
  opacity: 0;
}

/* 悬停时的微动画 */
.animate-hover {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.animate-hover:hover {
  transform: translateY(-2px);
}

/* 移动端响应式设计 */
@media (max-width: 768px) {
  .my-courses-container {
    padding: 10px;
  }

  .page-header {
    padding: 40px 20px;
    margin-bottom: 20px;
  }

  .page-header h1 {
    font-size: 2rem;
  }

  .page-header p {
    font-size: 0.9rem;
  }

  .courses-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .course-card {
    margin-bottom: 16px;
  }

  .course-image {
    height: 180px;
  }

  .course-content {
    padding: 16px;
  }

  .course-title {
    font-size: 1.1rem;
  }

  .meta-item {
    padding: 6px 12px;
    font-size: 0.8rem;
  }

  .course-actions {
    padding: 12px 16px;
    gap: 8px;
  }

  .course-actions .el-button {
    padding: 8px 16px;
    font-size: 0.85rem;
  }

  .action-bar {
    padding: 16px 0;
    flex-direction: column;
    gap: 12px;
  }

  .action-bar .el-button {
    width: 100%;
    padding: 12px 20px;
  }

  /* 表格在移动端的优化 */
  .teacher-courses {
    overflow-x: auto;
  }

  :deep(.el-table) {
    font-size: 0.85rem;
  }

  :deep(.el-table th) {
    padding: 12px 8px;
  }

  :deep(.el-table td) {
    padding: 16px 8px;
  }

  .course-name-cell {
    padding: 6px;
    gap: 8px;
  }

  :deep(.el-avatar) {
    width: 32px;
    height: 32px;
  }

  :deep(.el-table .el-button) {
    padding: 6px 12px;
    font-size: 0.8rem;
    margin: 2px;
  }
}

@media (max-width: 480px) {
  .page-header {
    padding: 30px 15px;
  }

  .page-header h1 {
    font-size: 1.8rem;
  }

  .course-card {
    border-radius: 12px;
  }

  .course-image {
    height: 160px;
  }

  .course-content {
    padding: 12px;
  }

  .course-meta {
    flex-direction: column;
    gap: 8px;
  }

  .meta-item {
    align-self: flex-start;
  }

  .progress-container {
    margin: 12px 0;
  }

  .course-actions {
    padding: 10px 12px;
  }

  .action-bar .el-button {
    padding: 10px 16px;
    font-size: 0.9rem;
  }

  /* 极小屏幕下隐藏部分表格列 */
  :deep(.el-table .hidden-xs-only) {
    display: none;
  }
}

/* 课程卡片交错动画 */
.course-card-enter-active {
  transition: all 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  animation: courseCardSlideIn 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
}

.course-card-enter-from {
  opacity: 0;
  transform: translateY(50px) scale(0.9);
}

.course-card-leave-active {
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.course-card-leave-to {
  opacity: 0;
  transform: translateY(-30px) scale(0.95);
}

@keyframes courseCardSlideIn {
  0% {
    opacity: 0;
    transform: translateY(50px) scale(0.9) rotateX(10deg);
  }

  60% {
    opacity: 0.8;
    transform: translateY(-5px) scale(1.02) rotateX(-2deg);
  }

  100% {
    opacity: 1;
    transform: translateY(0) scale(1) rotateX(0deg);
  }
}

/* 为课程卡片添加交错延迟 */
.course-col {
  animation: courseCardSlideIn 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
  opacity: 0;
}

/* 移动端响应式设计 */
@media (max-width: 1024px) {
  .my-courses-container {
    padding: 16px;
  }

  .page-header h1 {
    font-size: 28px;
  }

  .course-card {
    margin-bottom: 16px;
  }

  :deep(.el-table) {
    font-size: 13px;
  }

  :deep(.el-table th),
  :deep(.el-table td) {
    padding: 12px 8px;
  }
}

@media (max-width: 768px) {
  .my-courses-container {
    padding: 12px;
  }

  .page-header {
    text-align: center;
    margin-bottom: 24px;
  }

  .page-header h1 {
    font-size: 24px;
    margin-bottom: 8px;
  }

  .page-header p {
    font-size: 14px;
  }

  .action-bar {
    flex-direction: column;
    gap: 12px;
    margin-bottom: 20px;
  }

  .courses-section {
    padding: 16px;
  }

  .course-card {
    margin-bottom: 16px;
  }

  .course-content {
    padding: 16px;
  }

  .course-title {
    font-size: 18px;
  }

  .course-meta {
    flex-direction: column;
    gap: 8px;
  }

  .course-actions {
    flex-direction: column;
    gap: 8px;
  }

  :deep(.el-button) {
    width: 100%;
    margin: 0;
  }

  :deep(.el-table) {
    font-size: 12px;
  }

  :deep(.el-table th),
  :deep(.el-table td) {
    padding: 8px 4px;
  }

  .course-name-cell {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  :deep(.el-avatar) {
    width: 32px;
    height: 32px;
  }
}

@media (max-width: 480px) {
  .my-courses-container {
    padding: 8px;
  }

  .page-header h1 {
    font-size: 20px;
  }

  .page-header p {
    font-size: 13px;
  }

  .courses-section {
    padding: 12px;
  }

  .course-content {
    padding: 12px;
  }

  .course-title {
    font-size: 16px;
  }

  .course-progress {
    padding: 12px;
  }

  :deep(.el-button) {
    padding: 10px 16px;
    font-size: 13px;
  }

  :deep(.el-table) {
    font-size: 11px;
  }

  :deep(.el-table th),
  :deep(.el-table td) {
    padding: 6px 2px;
  }

  .course-name-cell span {
    font-size: 12px;
  }

  :deep(.el-tag) {
    padding: 4px 8px;
    font-size: 11px;
  }
}

/* 触摸设备优化 */
@media (hover: none) and (pointer: coarse) {

  .course-card:hover,
  .course-title:hover,
  .course-name-cell:hover,
  :deep(.el-button:hover),
  :deep(.el-table__body tr:hover) {
    transform: none;
  }

  .course-card,
  .course-title,
  .course-name-cell,
  :deep(.el-button),
  :deep(.el-table__body tr) {
    transition: none;
  }
}

/* 对话框样式 */
.dialog-footer {
  margin-top: 20px;
  text-align: right;
}

@media (max-width: 768px) {
  .dialog-footer {
    text-align: center;
  }

  .dialog-footer :deep(.el-button) {
    width: 100%;
    margin-bottom: 8px;
  }
}
</style>