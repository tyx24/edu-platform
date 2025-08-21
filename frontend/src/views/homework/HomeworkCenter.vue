<template>
  <div class="homework-center-container">
    <div class="page-header">
      <div class="header-background"></div>
      <h1>作业中心</h1>
      <p v-if="userRole === 'student'">查看所有可提交的作业，按时完成作业任务</p>
      <p v-else-if="userRole === 'teacher'">创建和管理作业，查看学生作业提交</p>
      <p v-else>管理系统作业相关功能</p>
    </div>

    <!-- 教师功能区 -->
    <div v-if="userRole === 'teacher'" class="teacher-actions">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-button type="primary" size="large" @click="createHomework" class="action-btn">
            <el-icon>
              <Plus />
            </el-icon>
            创建作业
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="success" size="large" @click="manageSubmissions" class="action-btn">
            <el-icon>
              <Document />
            </el-icon>
            提交管理
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="info" size="large" @click="viewHomeworkRecords" class="action-btn">
            <el-icon>
              <DataAnalysis />
            </el-icon>
            作业记录
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="warning" size="large" @click="homeworkStatistics" class="action-btn">
            <el-icon>
              <PieChart />
            </el-icon>
            作业统计
          </el-button>
        </el-col>
      </el-row>
    </div>

    <div class="search-filter-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-input v-model="searchForm.keyword" placeholder="搜索作业名称或课程" clearable @clear="fetchHomeworks">
            <template #append>
              <el-button @click="fetchHomeworks">
                <el-icon>
                  <Search />
                </el-icon>
              </el-button>
            </template>
          </el-input>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-select v-model="searchForm.status" placeholder="作业状态" style="width: 100%" clearable
            @change="fetchHomeworks">
            <el-option label="全部" value="" />
            <el-option label="未开始" value="NOT_STARTED" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已截止" value="ENDED" />
            <el-option v-if="userRole === 'teacher'" label="草稿" value="DRAFT" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-select v-model="searchForm.courseId" :placeholder="userRole === 'teacher' ? '选择课程' : '选择课程'"
            style="width: 100%" clearable @change="fetchHomeworks">
            <el-option label="全部课程" value="" />
            <el-option v-for="course in enrolledCourses" :key="course.id" :label="course.title" :value="course.id" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-if="userRole === 'student'">
          <el-select v-model="searchForm.submitted" placeholder="提交状态" style="width: 100%" clearable
            @change="fetchHomeworks">
            <el-option label="全部" value="" />
            <el-option label="未提交" value="false" />
            <el-option label="已提交" value="true" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-else-if="userRole === 'teacher'">
          <el-select v-model="searchForm.difficulty" placeholder="作业难度" style="width: 100%" clearable
            @change="fetchHomeworks">
            <el-option label="全部" value="" />
            <el-option label="简单" value="EASY" />
            <el-option label="中等" value="MEDIUM" />
            <el-option label="困难" value="HARD" />
          </el-select>
        </el-col>
      </el-row>
    </div>

    <div class="homework-stats">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-number">{{ stats.total }}</div>
            <div class="stat-label">{{ userRole === 'teacher' ? '创建作业数' : '总作业数' }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-number">{{ stats.inProgress }}</div>
            <div class="stat-label">进行中</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-number">{{ stats.submitted }}</div>
            <div class="stat-label">{{ userRole === 'teacher' ? '已提交' : '已提交' }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-number">{{ stats.overdue }}</div>
            <div class="stat-label">{{ userRole === 'teacher' ? '已逾期' : '已逾期' }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
      <el-skeleton :rows="3" animated />
    </div>

    <div v-else-if="homeworks.length === 0" class="empty-container">
      <el-empty description="暂无作业" />
    </div>

    <div v-else class="homeworks-container">
      <el-row :gutter="20">
        <el-col v-for="homework in homeworks" :key="homework.id" :xs="24" :sm="12" :md="8" :lg="6" class="homework-col">
          <div class="homework-card" :class="{
            'not-started-homework': getHomeworkStatus(homework) === 'NOT_STARTED',
            'in-progress-homework': getHomeworkStatus(homework) === 'IN_PROGRESS',
            'ended-homework': getHomeworkStatus(homework) === 'ENDED',
            'submitted-homework': homework.submitted
          }">
            <div class="homework-status-tag">
              <el-tag :type="getStatusTagType(homework)" effect="dark" size="small">
                {{ getStatusText(homework) }}
              </el-tag>
              <el-tag v-if="homework.submitted" type="success" effect="plain" size="small">
                已提交
              </el-tag>
              <el-tag v-if="homework.submitted && homework.isLate" type="warning" effect="plain" size="small">
                逾期提交
              </el-tag>
            </div>

            <h3 class="homework-title">{{ homework.title }}</h3>
            <div class="homework-course">{{ homework.courseName }}</div>

            <div class="homework-details">
              <p>
                <el-icon>
                  <Clock />
                </el-icon>
                开始: {{ formatDateTime(homework.startTime) }}
              </p>
              <p>
                <el-icon>
                  <AlarmClock />
                </el-icon>
                截止: {{ formatDateTime(homework.endTime) }}
              </p>
              <p>
                <el-icon>
                  <Document />
                </el-icon>
                总分: {{ homework.totalScore }}分
              </p>
              <p v-if="homework.requiresAttachment">
                <el-icon>
                  <Paperclip />
                </el-icon>
                需要提交附件
              </p>
            </div>

            <div v-if="homework.submitted" class="homework-result">
              <div class="result-item">
                <span class="label">提交时间:</span>
                <span class="value">{{ formatDateTime(homework.submissionTime) }}</span>
              </div>
              <div v-if="homework.graded" class="result-item">
                <span class="label">得分:</span>
                <span class="value score">{{ homework.score }}</span>
              </div>
              <div v-else class="result-item">
                <span class="label">状态:</span>
                <el-tag size="small" type="info">待批改</el-tag>
              </div>
            </div>

            <div class="homework-actions">
              <!-- 学生操作按钮 -->
              <template v-if="userRole === 'student'">
                <el-button v-if="!homework.submitted && getHomeworkStatus(homework) === 'IN_PROGRESS'" type="primary"
                  @click="goToHomework(homework)">
                  提交作业
                </el-button>
                <el-button v-else-if="!homework.submitted && getHomeworkStatus(homework) === 'NOT_STARTED'" type="info"
                  disabled>
                  未开始
                </el-button>
                <el-button v-else-if="!homework.submitted && getHomeworkStatus(homework) === 'ENDED'" type="info"
                  disabled>
                  已截止
                </el-button>
                <el-button v-else-if="homework.submitted && !homework.graded" type="success"
                  @click="goToHomework(homework)">
                  查看提交
                </el-button>
                <el-button v-else type="success" @click="goToHomework(homework)">
                  查看结果
                </el-button>
              </template>

              <!-- 教师操作按钮 -->
              <template v-else-if="userRole === 'teacher'">
                <el-button-group>
                  <el-button type="primary" size="small" @click="viewHomeworkDetail(homework)">
                    <el-icon>
                      <View />
                    </el-icon>
                    详情
                  </el-button>
                  <el-button type="warning" size="small" @click="editHomework(homework)">
                    <el-icon>
                      <Edit />
                    </el-icon>
                    编辑
                  </el-button>
                  <el-button type="danger" size="small" @click="deleteHomework(homework)">
                    <el-icon>
                      <Delete />
                    </el-icon>
                    删除
                  </el-button>
                </el-button-group>
                <div class="teacher-homework-info">
                  <span class="submission-count">提交人数: {{ homework.submissionCount || 0 }}</span>
                </div>
              </template>
            </div>
          </div>
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
import { Search, Clock, AlarmClock, Document, Paperclip, Plus, DataAnalysis, PieChart, View, Edit, Delete } from '@element-plus/icons-vue';
import { useAuthStore } from '../../store/auth';
import { homeworkApi } from '../../api/homework';
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
  submitted: '',
  difficulty: ''
});

// 作业列表
const homeworks = ref([]);

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
  inProgress: 0,
  submitted: 0,
  overdue: 0
});

// 在组件挂载时获取数据
onMounted(async () => {
  await fetchEnrolledCourses();
  await fetchHomeworks();
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

// 获取作业列表
const fetchHomeworks = async () => {
  loading.value = true;
  try {
    const params = {
      ...searchForm.value,
      page: page.value.current,
      size: page.value.size
    };

    let res;
    if (userRole.value === 'teacher') {
      // 教师获取自己创建的作业
      res = await homeworkApi.getTeacherHomeworks(params);
    } else {
      // 学生获取可提交的作业
      res = await homeworkApi.getHomeworkCenter(params);
    }

    homeworks.value = res.data.records || [];
    page.value.total = res.data.total || 0;
  } catch (error) {
    console.error('获取作业列表失败:', error);
    ElMessage.error('获取作业列表失败');
  } finally {
    loading.value = false;
  }
};

// 获取统计信息
const fetchStats = async () => {
  try {
    let res;
    if (userRole.value === 'teacher') {
      // 教师获取自己创建的作业统计
      res = await homeworkApi.getTeacherHomeworkStats();
    } else {
      // 学生获取作业中心统计
      res = await homeworkApi.getHomeworkCenterStats();
    }

    stats.value = res.data || {
      total: 0,
      inProgress: 0,
      submitted: 0,
      overdue: 0
    };
  } catch (error) {
    console.error('获取统计信息失败:', error);
    ElMessage.error('获取统计信息失败');
  }
};

// 处理分页大小变更
const handleSizeChange = (size) => {
  page.value.size = size;
  fetchHomeworks();
};

// 处理当前页变更
const handleCurrentChange = (current) => {
  page.value.current = current;
  fetchHomeworks();
};

// 前往作业详情页
const goToHomework = (homework) => {
  router.push(`/homework/${homework.id}`);
};

// 获取作业状态
const getHomeworkStatus = (homework) => {
  const now = new Date().getTime();
  const startTime = new Date(homework.startTime).getTime();
  const endTime = new Date(homework.endTime).getTime();

  if (now < startTime) {
    return 'NOT_STARTED';
  } else if (now >= startTime && now <= endTime) {
    return 'IN_PROGRESS';
  } else {
    return 'ENDED';
  }
};

// 获取状态标签类型
const getStatusTagType = (homework) => {
  const status = getHomeworkStatus(homework);
  const statusMap = {
    'NOT_STARTED': 'info',
    'IN_PROGRESS': 'warning',
    'ENDED': 'danger'
  };
  return statusMap[status] || 'info';
};

// 获取状态文本
const getStatusText = (homework) => {
  const status = getHomeworkStatus(homework);
  const statusMap = {
    'NOT_STARTED': '未开始',
    'IN_PROGRESS': '进行中',
    'ENDED': '已截止'
  };
  return statusMap[status] || '未知';
};

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

// 教师功能：创建作业
const createHomework = () => {
  router.push('/homework/create');
};

// 教师功能：管理提交
const manageSubmissions = () => {
  router.push('/homework/submissions');
};

// 教师功能：查看作业记录
const viewHomeworkRecords = () => {
  router.push('/homework/records');
};

// 教师功能：作业统计
const homeworkStatistics = () => {
  router.push('/homework/statistics');
};

// 教师功能：编辑作业
const editHomework = (homework) => {
  router.push(`/homework/${homework.id}/edit`);
};

// 教师功能：删除作业
const deleteHomework = async (homework) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除作业「${homework.title}」吗？此操作不可恢复。`,
      '删除作业',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    await homeworkApi.deleteHomework(homework.id);
    ElMessage.success('删除成功');
    await fetchHomeworks();
    await fetchStats();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除作业失败:', error);
      ElMessage.error('删除作业失败');
    }
  }
};

// 教师功能：查看作业详情和学生提交
const viewHomeworkDetail = (homework) => {
  router.push(`/homework/${homework.id}/detail`);
};
</script>

<style scoped>
.homework-center-container {
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

.homework-center-container>* {
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

.homework-stats {
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

.stat-card .stat-number {
  font-size: 48px;
  font-weight: 800;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  line-height: 1;
}

.stat-card .stat-label {
  font-size: 16px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin: 0;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
}

.stat-card:hover {
  transform: translateY(-10px) scale(1.02);
  background: rgba(255, 255, 255, 0.25);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2), 0 5px 20px rgba(255, 255, 255, 0.1) inset;
  border-color: rgba(255, 255, 255, 0.5);
}

.stat-value {
  font-size: 36px;
  font-weight: 800;
  color: white;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
  margin-bottom: 8px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 1px 5px rgba(0, 0, 0, 0.2);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.loading-container,
.empty-container {
  max-width: 1200px;
  margin: 60px auto;
  padding: 60px 20px;
  text-align: center;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(15px);
  border-radius: 25px;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.homeworks-container {
  max-width: 1200px;
  margin: 0 auto 50px;
  padding: 0 20px;
}

.homework-col {
  margin-bottom: 30px;
}

.homework-card {
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

.homework-card::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, #667eea, #764ba2, #f093fb, #f5576c, #4facfe, #00f2fe);
  background-size: 300% 300%;
  border-radius: 26px;
  z-index: -1;
  opacity: 0;
  animation: homeworkCardBorder 6s ease infinite;
  transition: opacity 0.4s ease;
}

@keyframes homeworkCardBorder {

  0%,
  100% {
    background-position: 0% 50%;
  }

  50% {
    background-position: 100% 50%;
  }
}

.homework-card:hover::before {
  opacity: 1;
}

.homework-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.2),
    0 10px 30px rgba(0, 0, 0, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.not-started-homework::before {
  background: linear-gradient(90deg, #909399 0%, #b0b3b8 100%);
}

.in-progress-homework::before {
  background: linear-gradient(90deg, #e6a23c 0%, #f0b90b 100%);
}

.ended-homework::before {
  background: linear-gradient(90deg, #f56c6c 0%, #ff7875 100%);
}

.submitted-homework::before {
  background: linear-gradient(90deg, #67c23a 0%, #85ce61 100%);
}

.homework-status-tag {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 15px;
}

.homework-status-tag .el-tag {
  border-radius: 25px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
  backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  transition: all 0.4s ease;
  position: relative;
  overflow: hidden;
}

.homework-status-tag .el-tag::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s ease;
}

.homework-status-tag .el-tag:hover::before {
  left: 100%;
}

.homework-status-tag .el-tag:hover {
  transform: scale(1.05);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.homework-title {
  font-size: 22px;
  font-weight: 800;
  margin: 0 0 15px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.3;
  background: linear-gradient(135deg, #2d3748 0%, #4a5568 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  transition: all 0.3s ease;
}

.homework-card:hover .homework-title {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.homework-course {
  font-size: 14px;
  font-weight: 600;
  color: #667eea;
  margin-bottom: 20px;
  padding: 10px 16px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-radius: 20px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(102, 126, 234, 0.2);
  display: inline-block;
  transition: all 0.3s ease;
}

.homework-card:hover .homework-course {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.2);
}

.homework-info {
  margin-bottom: 20px;
  flex-grow: 1;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  background: rgba(255, 255, 255, 0.9);
  padding: 12px 16px;
  border-radius: 16px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(102, 126, 234, 0.1);
  transition: all 0.3s ease;
}

.info-item:hover {
  background: rgba(255, 255, 255, 1);
  color: #475569;
  transform: translateX(5px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.15);
}

.info-item .el-icon {
  margin-right: 12px;
  color: #667eea;
  font-size: 16px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  padding: 8px;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.info-item:hover .el-icon {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.2) 0%, rgba(118, 75, 162, 0.2) 100%);
  transform: rotate(5deg) scale(1.1);
}

.homework-result {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(15px);
  padding: 20px;
  border-radius: 16px;
  margin-bottom: 20px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.homework-result:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.result-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.result-item:last-child {
  margin-bottom: 0;
}

.result-item .label {
  width: 50px;
  color: rgba(255, 255, 255, 0.8);
}

.score-pass {
  color: #67c23a;
  font-weight: bold;
}

.score-fail {
  color: #f56c6c;
  font-weight: bold;
}

.homework-actions {
  margin-top: auto;
  text-align: center;
  position: relative;
  z-index: 1;
  padding-top: 20px;
  border-top: 1px solid rgba(102, 126, 234, 0.1);
}

.homework-actions .el-button {
  height: 50px;
  border-radius: 16px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: relative;
  overflow: hidden;
  margin: 0 5px;
  backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.homework-actions .el-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s ease;
}

.homework-actions .el-button:hover::before {
  left: 100%;
}

.homework-actions .el-button:hover {
  transform: translateY(-4px) scale(1.03);
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.2);
}

.homework-actions .el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: white;
}

.homework-actions .el-button--primary:hover {
  background: linear-gradient(135deg, #5a67d8 0%, #6b46c1 100%);
}

.homework-actions .el-button--success {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  border-color: transparent;
  color: white;
}

.homework-actions .el-button--info {
  background: linear-gradient(135deg, #4299e1 0%, #3182ce 100%);
  border-color: transparent;
  color: white;
}

.homework-actions .el-button--warning {
  background: linear-gradient(135deg, #ed8936 0%, #dd6b20 100%);
  border-color: transparent;
  color: white;
}

.homework-actions .el-button--danger {
  background: linear-gradient(135deg, #f56565 0%, #e53e3e 100%);
  border-color: transparent;
  color: white;
}

.homework-actions .el-button-group {
  display: flex;
  gap: 8px;
  justify-content: center;
  margin-bottom: 10px;
}

.homework-actions .el-button-group .el-button {
  margin: 0;
  flex: 1;
  min-width: 80px;
}

.teacher-homework-info {
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

.teacher-homework-info::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
}

.homework-card:hover .teacher-homework-info {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.2);
}

.submission-count {
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

.submission-count::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.2), transparent);
  transition: left 0.6s ease;
}

.homework-card:hover .submission-count {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: scale(1.05);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.3);
}

.homework-card:hover .submission-count::before {
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
  --el-pagination-button-color: #64748b;
  --el-pagination-button-bg-color: rgba(255, 255, 255, 0.8);
  --el-pagination-button-disabled-color: #cbd5e1;
  --el-pagination-button-disabled-bg-color: rgba(255, 255, 255, 0.4);
  --el-pagination-hover-color: #667eea;
  --el-pagination-hover-bg-color: rgba(102, 126, 234, 0.1);
  --el-pagination-border-radius: 16px;
}

.pagination-container :deep(.el-pagination .btn-prev),
.pagination-container :deep(.el-pagination .btn-next) {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 16px;
  color: #64748b;
  font-weight: 600;
  transition: all 0.4s ease;
  backdrop-filter: blur(15px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.pagination-container :deep(.el-pagination .btn-prev:hover),
.pagination-container :deep(.el-pagination .btn-next:hover) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.2);
  color: #667eea;
  border-color: rgba(102, 126, 234, 0.3);
}

.pagination-container :deep(.el-pagination .el-pager li) {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 16px;
  margin: 0 4px;
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  backdrop-filter: blur(15px);
  font-weight: 600;
  color: #64748b;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.pagination-container :deep(.el-pagination .el-pager li:hover) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.2);
  color: #667eea;
  border-color: rgba(102, 126, 234, 0.3);
}

.pagination-container :deep(.el-pagination .el-pager li.is-active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 800;
  transform: scale(1.1);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
  border-color: transparent;
}
</style>