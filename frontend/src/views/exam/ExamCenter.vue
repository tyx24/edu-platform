<template>
  <div class="exam-center-container">
    <div class="page-header">
      <div class="header-background"></div>
      <h1>考试中心</h1>
      <p v-if="userRole === 'student'">查看所有可参加的考试，按时完成考试任务</p>
      <p v-else-if="userRole === 'teacher'">创建和管理考试，查看学生考试记录</p>
      <p v-else>管理系统考试相关功能</p>
    </div>

    <!-- 教师功能区 -->
    <div v-if="userRole === 'teacher'" class="teacher-actions">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-button type="primary" size="large" @click="createExam" class="action-btn">
            <el-icon>
              <Plus />
            </el-icon>
            创建考试
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="success" size="large" @click="manageQuestions" class="action-btn">
            <el-icon>
              <Document />
            </el-icon>
            题目管理
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="info" size="large" @click="viewExamRecords" class="action-btn">
            <el-icon>
              <DataAnalysis />
            </el-icon>
            考试记录
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="warning" size="large" @click="examStatistics" class="action-btn">
            <el-icon>
              <PieChart />
            </el-icon>
            考试统计
          </el-button>
        </el-col>
      </el-row>
    </div>

    <div class="search-filter-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-input v-model="searchForm.keyword" placeholder="搜索考试名称或课程" clearable @clear="fetchExams">
            <template #append>
              <el-button @click="fetchExams">
                <el-icon>
                  <Search />
                </el-icon>
              </el-button>
            </template>
          </el-input>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-select v-model="searchForm.status" placeholder="考试状态" style="width: 100%" clearable @change="fetchExams">
            <el-option label="全部" value="" />
            <el-option label="即将开始" value="UPCOMING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已结束" value="ENDED" />
            <el-option v-if="userRole === 'teacher'" label="草稿" value="DRAFT" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-select v-model="searchForm.courseId" :placeholder="userRole === 'teacher' ? '选择课程' : '选择课程'"
            style="width: 100%" clearable @change="fetchExams">
            <el-option label="全部课程" value="" />
            <el-option v-for="course in enrolledCourses" :key="course.id" :label="course.title" :value="course.id" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-if="userRole === 'student'">
          <el-select v-model="searchForm.completed" placeholder="完成状态" style="width: 100%" clearable
            @change="fetchExams">
            <el-option label="全部" value="" />
            <el-option label="未参加" value="false" />
            <el-option label="已参加" value="true" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-else-if="userRole === 'teacher'">
          <el-select v-model="searchForm.difficulty" placeholder="考试难度" style="width: 100%" clearable
            @change="fetchExams">
            <el-option label="全部" value="" />
            <el-option label="简单" value="EASY" />
            <el-option label="中等" value="MEDIUM" />
            <el-option label="困难" value="HARD" />
          </el-select>
        </el-col>
      </el-row>
    </div>

    <div class="exam-stats">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">{{ userRole === 'teacher' ? '创建考试数' : '总考试数' }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.upcoming }}</div>
            <div class="stat-label">即将开始</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.inProgress }}</div>
            <div class="stat-label">进行中</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.completed }}</div>
            <div class="stat-label">{{ userRole === 'teacher' ? '已结束' : '已完成' }}</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
      <el-skeleton :rows="3" animated />
    </div>

    <div v-else-if="exams.length === 0" class="empty-container">
      <el-empty description="暂无考试" />
    </div>

    <div v-else class="exams-container">
      <el-row :gutter="20">
        <el-col v-for="exam in exams" :key="exam.id" :xs="24" :sm="12" :md="8" :lg="6" class="exam-col">
          <el-card class="exam-card" :class="{
            'upcoming-exam': getExamStatus(exam) === 'UPCOMING',
            'in-progress-exam': getExamStatus(exam) === 'IN_PROGRESS',
            'ended-exam': getExamStatus(exam) === 'ENDED',
            'completed-exam': exam.completed
          }" shadow="hover">
            <div class="exam-status-tag">
              <el-tag :type="getStatusTagType(exam)" effect="dark" size="small">
                {{ getStatusText(exam) }}
              </el-tag>
              <el-tag v-if="exam.completed" type="success" effect="plain" size="small">
                已完成
              </el-tag>
            </div>

            <h3 class="exam-title">{{ exam.title }}</h3>
            <div class="exam-course">{{ exam.courseName }}</div>

            <div class="exam-info">
              <div class="info-item">
                <el-icon>
                  <Clock />
                </el-icon>
                <span>{{ formatDateTime(exam.startTime) }} - {{ formatDateTime(exam.endTime) }}</span>
              </div>
              <div class="info-item">
                <el-icon>
                  <Timer />
                </el-icon>
                <span>时长: {{ exam.duration }} 分钟</span>
              </div>
              <div class="info-item">
                <el-icon>
                  <Document />
                </el-icon>
                <span>总分: {{ exam.totalScore }}分 (及格: {{ exam.passingScore }}分)</span>
              </div>
            </div>

            <div v-if="exam.completed" class="exam-result">
              <div class="result-item">
                <span class="label">得分:</span>
                <span class="value" :class="{
                  'score-pass': exam.score >= exam.passingScore,
                  'score-fail': exam.score < exam.passingScore
                }">
                  {{ exam.score }}
                </span>
              </div>
              <div class="result-item">
                <span class="label">结果:</span>
                <el-tag :type="exam.score >= exam.passingScore ? 'success' : 'danger'" size="small">
                  {{ exam.score >= exam.passingScore ? '通过' : '未通过' }}
                </el-tag>
              </div>
            </div>

            <div class="exam-actions">
              <!-- 学生操作按钮 -->
              <template v-if="userRole === 'student'">
                <el-button v-if="!exam.completed && getExamStatus(exam) === 'IN_PROGRESS'" type="primary"
                  @click="startExam(exam)">
                  开始考试
                </el-button>
                <el-button v-else-if="!exam.completed && getExamStatus(exam) === 'UPCOMING'" type="info" disabled>
                  未开始
                </el-button>
                <el-button v-else-if="!exam.completed && getExamStatus(exam) === 'ENDED'" type="info" disabled>
                  已结束
                </el-button>
                <el-button v-else type="success" @click="viewExamResult(exam)">
                  查看结果
                </el-button>
              </template>

              <!-- 教师操作按钮 -->
              <template v-else-if="userRole === 'teacher'">
                <el-button-group>
                  <el-button type="primary" size="small" @click="viewExamDetail(exam)">
                    <el-icon>
                      <View />
                    </el-icon>
                    详情
                  </el-button>
                  <el-button type="warning" size="small" @click="editExam(exam)">
                    <el-icon>
                      <Edit />
                    </el-icon>
                    编辑
                  </el-button>
                  <el-button type="danger" size="small" @click="deleteExam(exam)">
                    <el-icon>
                      <Delete />
                    </el-icon>
                    删除
                  </el-button>
                </el-button-group>
                <div class="teacher-exam-info">
                  <span class="participant-count">参与人数: {{ exam.participantCount || 0 }}</span>
                </div>
              </template>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="pagination-container">
      <el-pagination v-model:current-page="page.current" v-model:page-size="page.size" :page-sizes="[8, 16, 24, 32]"
        layout="total, sizes, prev, pager, next" :total="page.total" @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Clock, Timer, Document, Plus, DataAnalysis, PieChart, View, Edit, Delete } from '@element-plus/icons-vue';
import { useAuthStore } from '../../store/auth';
import { examApi } from '../../api/exam';
import { courseApi } from '../../api/course';
import { enrollmentApi } from '../../api/enrollment';

const router = useRouter();
const authStore = useAuthStore();

// 用户角色
const userRole = computed(() => authStore.user?.role || 'student');

// 搜索表单
const searchForm = ref({
  keyword: '',
  status: '',
  courseId: '',
  completed: '',
  difficulty: ''
});

// 考试列表
const exams = ref([]);

// 已选课程列表
const enrolledCourses = ref([]);

// 加载状态
const loading = ref(false);

// 分页信息
const page = ref({
  current: 1,
  size: 8,
  total: 0
});

// 统计信息
const stats = ref({
  total: 0,
  upcoming: 0,
  inProgress: 0,
  completed: 0
});

// 在组件挂载时获取数据
onMounted(async () => {
  await fetchEnrolledCourses();
  await fetchExams();
  await fetchStats();
});

// 获取已选课程
const fetchEnrolledCourses = async () => {
  try {
    let res;
    if (userRole.value === 'teacher') {
      // 教师获取自己的课程（包含学生信息）
      res = await enrollmentApi.getMyCoursesWithStudents();
    } else {
      // 学生获取已选课程
      res = await enrollmentApi.getMyCourses();
    }
    enrolledCourses.value = res.data || [];
  } catch (error) {
    console.error('获取已选课程失败:', error);
    ElMessage.error('获取已选课程失败');
  }
};

// 获取考试列表
const fetchExams = async () => {
  loading.value = true;
  try {
    const params = {
      ...searchForm.value,
      page: page.value.current,
      size: page.value.size
    };

    let res;
    if (userRole.value === 'teacher') {
      // 教师获取自己创建的考试
      res = await examApi.getTeacherExams(params);
    } else {
      // 学生获取可参加的考试
      res = await examApi.getExamCenter(params);
    }

    exams.value = res.data.records || [];
    page.value.total = res.data.total || 0;
  } catch (error) {
    console.error('获取考试列表失败:', error);
    ElMessage.error('获取考试列表失败');
  } finally {
    loading.value = false;
  }
};

// 获取统计信息
const fetchStats = async () => {
  try {
    let res;
    if (userRole.value === 'teacher') {
      // 教师获取自己创建的考试统计
      res = await examApi.getTeacherExamStats();
    } else {
      // 学生获取考试中心统计
      res = await examApi.getExamCenterStats();
    }

    stats.value = res.data || {
      total: 0,
      upcoming: 0,
      inProgress: 0,
      completed: 0
    };
  } catch (error) {
    console.error('获取统计信息失败:', error);
    ElMessage.error('获取统计信息失败');
  }
};

// 处理分页大小变更
const handleSizeChange = (size) => {
  page.value.size = size;
  fetchExams();
};

// 处理当前页变更
const handleCurrentChange = (current) => {
  page.value.current = current;
  fetchExams();
};

// 开始考试
const startExam = async (exam) => {
  try {
    // 确认是否开始考试
    await ElMessageBox.confirm(
      `您即将开始「${exam.title}」考试，考试时间为${exam.duration}分钟。\n开始后计时不可暂停，请确保有足够的时间完成考试。`,
      '开始考试',
      {
        confirmButtonText: '开始考试',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    // 创建考试记录
    const res = await examApi.startExam(exam.id);
    const recordId = res.data;

    // 跳转到考试页面
    router.push(`/exam/${exam.id}/take/${recordId}`);
  } catch (error) {
    if (error !== 'cancel') {
      console.error('开始考试失败:', error);
      ElMessage.error('开始考试失败');
    }
  }
};

// 查看考试结果
const viewExamResult = (exam) => {
  router.push(`/exam/${exam.id}/result/${exam.recordId}`);
};

// 获取考试状态
const getExamStatus = (exam) => {
  const now = new Date().getTime();
  const startTime = new Date(exam.startTime).getTime();
  const endTime = new Date(exam.endTime).getTime();

  if (now < startTime) {
    return 'UPCOMING';
  } else if (now >= startTime && now <= endTime) {
    return 'IN_PROGRESS';
  } else {
    return 'ENDED';
  }
};

// 获取状态标签类型
const getStatusTagType = (exam) => {
  const status = getExamStatus(exam);
  const statusMap = {
    'UPCOMING': 'info',
    'IN_PROGRESS': 'warning',
    'ENDED': 'danger'
  };
  return statusMap[status] || 'info';
};

// 获取状态文本
const getStatusText = (exam) => {
  const status = getExamStatus(exam);
  const statusMap = {
    'UPCOMING': '即将开始',
    'IN_PROGRESS': '进行中',
    'ENDED': '已结束'
  };
  return statusMap[status] || '未知';
};

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

// 教师功能：创建考试
const createExam = () => {
  router.push('/exam/create');
};

// 教师功能：管理题目
const manageQuestions = () => {
  router.push('/exam/questions');
};

// 教师功能：查看考试记录
const viewExamRecords = () => {
  router.push('/exam/records');
};

// 教师功能：考试统计
const examStatistics = () => {
  router.push('/exam/statistics');
};

// 教师功能：编辑考试
const editExam = (exam) => {
  router.push(`/exam/${exam.id}/edit`);
};

// 教师功能：删除考试
const deleteExam = async (exam) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除考试「${exam.title}」吗？此操作不可恢复。`,
      '删除考试',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    await examApi.deleteExam(exam.id);
    ElMessage.success('删除成功');
    await fetchExams();
    await fetchStats();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除考试失败:', error);
      ElMessage.error('删除考试失败');
    }
  }
};

// 教师功能：查看考试详情和学生记录
const viewExamDetail = (exam) => {
  router.push(`/exam/${exam.id}/detail`);
};
</script>

<style scoped>
.exam-center-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  background-size: 400% 400%;
  animation: gradientFlow 20s ease infinite;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

.exam-center-container>* {
  position: relative;
  z-index: 1;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 60px 0;
  position: relative;
  overflow: hidden;
}

.header-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 32px;
  box-shadow: 0 20px 60px rgba(102, 126, 234, 0.4),
    0 10px 30px rgba(118, 75, 162, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.header-background::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  animation: float 6s ease-in-out infinite;
}

@keyframes float {

  0%,
  100% {
    transform: translateY(0px) rotate(0deg);
  }

  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

.page-header h1 {
  font-size: 48px;
  color: white;
  margin: 0 0 20px 0;
  font-weight: 900;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
  letter-spacing: 1.5px;
  animation: titleGlow 3s ease-in-out infinite alternate;
  position: relative;
  z-index: 1;
}

@keyframes titleGlow {
  0% {
    text-shadow: 0 4px 20px rgba(0, 0, 0, 0.4), 0 2px 10px rgba(0, 0, 0, 0.3);
  }

  100% {
    text-shadow: 0 6px 30px rgba(255, 255, 255, 0.3), 0 4px 20px rgba(0, 0, 0, 0.4);
  }
}

.page-header p {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.95);
  margin: 0;
  text-shadow: 0 1px 5px rgba(0, 0, 0, 0.2);
  font-weight: 500;
  letter-spacing: 0.5px;
  position: relative;
  z-index: 1;
}

.teacher-actions {
  margin-bottom: 40px;
  padding: 30px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.15),
    0 5px 20px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.4s ease;
  position: relative;
  overflow: hidden;
}

.teacher-actions::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.05) 0%, transparent 70%);
  animation: teacherActionGlow 8s ease-in-out infinite;
  pointer-events: none;
}

@keyframes teacherActionGlow {

  0%,
  100% {
    transform: rotate(0deg) scale(1);
    opacity: 0.5;
  }

  50% {
    transform: rotate(180deg) scale(1.1);
    opacity: 0.8;
  }
}

.teacher-actions:hover {
  transform: translateY(-3px);
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.2),
    0 10px 30px rgba(0, 0, 0, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.action-btn {
  width: 100%;
  height: 70px;
  font-size: 16px;
  font-weight: 700;
  border-radius: 16px;
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: relative;
  overflow: hidden;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.action-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s ease;
}

.action-btn:hover::before {
  left: 100%;
}

.action-btn:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
}

.search-filter-section {
  margin-bottom: 40px;
  padding: 30px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.15),
    0 5px 20px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.4s ease;
}

.search-filter-section:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.18),
    0 8px 25px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.search-filter-section .el-col {
  margin-bottom: 15px;
}

.exam-stats {
  margin-bottom: 50px;
}

.stat-card {
  text-align: center;
  padding: 35px 25px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.15),
    0 5px 20px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 25%, #f093fb 50%, #f5576c 75%, #4facfe 100%);
  background-size: 200% 100%;
  animation: statCardGradient 3s ease infinite;
}

@keyframes statCardGradient {

  0%,
  100% {
    background-position: 0% 50%;
  }

  50% {
    background-position: 100% 50%;
  }
}

.stat-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.2),
    0 10px 30px rgba(0, 0, 0, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.stat-value {
  font-size: 42px;
  font-weight: 900;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 12px;
  text-shadow: 0 2px 10px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.stat-card:hover .stat-value {
  transform: scale(1.1);
  text-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
}

.stat-label {
  font-size: 16px;
  color: #4a5568;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-top: 8px;
  transition: color 0.3s ease;
}

.stat-card:hover .stat-label {
  color: #2d3748;
}

.loading-container,
.empty-container {
  margin: 40px 0;
  text-align: center;
}

.exams-container {
  margin-bottom: 30px;
}

.exam-col {
  margin-bottom: 20px;
}

.exam-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  padding: 30px;
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.15),
    0 5px 20px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: relative;
  overflow: hidden;
}

.exam-card::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.03) 0%, transparent 70%);
  animation: examCardGlow 10s ease-in-out infinite;
  pointer-events: none;
}

@keyframes examCardGlow {

  0%,
  100% {
    transform: rotate(0deg) scale(1);
    opacity: 0.3;
  }

  50% {
    transform: rotate(180deg) scale(1.1);
    opacity: 0.6;
  }
}

.exam-card:hover {
  transform: translateY(-10px) rotateY(2deg) scale(1.02);
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.2),
    0 10px 30px rgba(0, 0, 0, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.upcoming-exam {
  border-top: 3px solid #909399;
}

.in-progress-exam {
  border-top: 3px solid #e6a23c;
}

.ended-exam {
  border-top: 3px solid #f56c6c;
}

.completed-exam {
  border-top: 3px solid #67c23a;
}

.exam-status-tag {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.exam-title {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 15px 0;
  background: linear-gradient(135deg, #2d3748 0%, #4a5568 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
}

.exam-card:hover .exam-title {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  transform: translateX(5px);
}

.exam-course {
  font-size: 15px;
  color: #718096;
  margin-bottom: 20px;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
}

.exam-card:hover .exam-course {
  color: #4a5568;
  transform: translateX(3px);
}

.exam-info {
  margin-bottom: 20px;
  padding: 15px 20px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
}

.exam-card:hover .exam-info {
  background: rgba(255, 255, 255, 0.8);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  font-size: 15px;
  color: #4a5568;
  font-weight: 500;
  transition: all 0.3s ease;
}

.exam-card:hover .info-item {
  color: #2d3748;
  transform: translateX(3px);
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item .el-icon {
  margin-right: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: transparent;
  -webkit-background-clip: text;
  background-clip: text;
  font-size: 16px;
  transition: all 0.3s ease;
}

.exam-card:hover .info-item .el-icon {
  transform: scale(1.1);
}

.exam-result {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
}

.result-item {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}

.result-item .label {
  width: 50px;
  color: #606266;
}

.score-pass {
  color: #67c23a;
  font-weight: bold;
}

.score-fail {
  color: #f56c6c;
  font-weight: bold;
}

.exam-actions {
  margin-top: auto;
  text-align: center;
  position: relative;
  z-index: 1;
}

.exam-actions .el-button {
  height: 50px;
  border-radius: 16px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: relative;
  overflow: hidden;
  margin: 0 5px;
}

.exam-actions .el-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s ease;
}

.exam-actions .el-button:hover::before {
  left: 100%;
}

.exam-actions .el-button:hover {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
}

.exam-actions .el-button-group {
  display: flex;
  gap: 8px;
  justify-content: center;
  margin-bottom: 10px;
}

.exam-actions .el-button-group .el-button {
  margin: 0;
  flex: 1;
  min-width: 80px;
}

.teacher-exam-info {
  margin-top: 15px;
  text-align: center;
  padding: 15px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-radius: 16px;
  border: 1px solid rgba(102, 126, 234, 0.2);
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.teacher-exam-info::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
}

.exam-card:hover .teacher-exam-info {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.2);
}

.participant-count {
  font-size: 14px;
  color: #4a5568;
  background: rgba(255, 255, 255, 0.8);
  padding: 8px 16px;
  border-radius: 12px;
  display: inline-block;
  font-weight: 600;
  border: 1px solid rgba(102, 126, 234, 0.2);
  backdrop-filter: blur(5px);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.participant-count::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.2), transparent);
  transition: left 0.6s ease;
}

.exam-card:hover .participant-count {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: scale(1.05);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.3);
}

.exam-card:hover .participant-count::before {
  left: 100%;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 50px;
  padding: 25px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.pagination-container:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.pagination-container :deep(.el-pagination) {
  --el-pagination-bg-color: transparent;
  --el-pagination-text-color: rgba(255, 255, 255, 0.9);
  --el-pagination-border-radius: 12px;
}

.pagination-container :deep(.el-pagination .btn-prev),
.pagination-container :deep(.el-pagination .btn-next) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.pagination-container :deep(.el-pagination .btn-prev:hover),
.pagination-container :deep(.el-pagination .btn-next:hover) {
  transform: scale(1.1);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.pagination-container :deep(.el-pagination .el-pager li) {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(116, 108, 108, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  margin: 0 3px;
  transition: all 0.3s ease;
}

.pagination-container :deep(.el-pagination .el-pager li:hover) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.3);
}

.pagination-container :deep(.el-pagination .el-pager li.is-active) {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  transform: scale(1.1);
  box-shadow: 0 5px 20px rgba(240, 147, 251, 0.4);
}
</style>