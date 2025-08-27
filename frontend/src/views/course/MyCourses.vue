<template>
  <transition name="page-fade" appear>
    <div class="my-courses-container animate-fade-in">
      <div class="page-header animate-fade-in">
        <h1>{{ isTeacher ? '我的教学课程' : '我的学习课程' }}</h1>
        <p>{{ isTeacher ? '管理您创建的课程和学生' : '查看您已选的课程和学习进度' }}</p>
      </div>

      <!-- 课程列表 -->
      <div class="courses-section">
        <!-- 教师创建课程按钮 -->
        <div class="action-bar animate-slide-up" v-if="isTeacher" style="animation-delay: 0.3s;">
          <el-button type="primary" @click="createCourse">
            创建新课程
          </el-button>
        </div>
        <el-tabs v-model="activeTab">
          <!-- 学生的已选课程 -->
          <el-tab-pane v-if="isStudent" label="我的课程" name="enrolled">
            <div v-if="enrolledCourses.length === 0" class="empty-state">
              <el-empty description="您还没有选择任何课程" />
              <el-button type="primary" @click="$router.push('/courses')">
                去选课
              </el-button>
            </div>

            <el-row v-else :gutter="20">
              <transition-group name="course-card" appear>
                <el-col v-for="(course, index) in enrolledCourses" :key="course.id" :xs="24" :sm="12" :md="8" :lg="6"
                  class="course-col animate-slide-up" :style="{ animationDelay: index * 0.1 + 's' }">
                  <el-card class="course-card" shadow="hover">
                    <div class="course-image" @click="viewCourse(course.id)">
                      <img :src="course.coverUrl || '/default-course.jpg'" :alt="course.title">
                      <div class="progress-overlay">
                        <el-progress type="circle" :percentage="course.progress || 0" :width="60" :stroke-width="6" />
                      </div>
                    </div>

                    <div class="course-content">
                      <h3 class="course-title" @click="viewCourse(course.id)">
                        {{ course.title }}
                      </h3>

                      <div class="course-meta">
                        <div class="meta-item">
                          <span>👨‍🏫 {{ course.teacherName }}</span>
                        </div>
                        <div class="meta-item">
                          <span>📚 {{ course.chapterCount || 0 }} 章节</span>
                        </div>
                      </div>

                      <div class="course-progress">
                        <div class="progress-text">
                          <span>学习进度: {{ course.progress || 0 }}%</span>
                          <span>{{ formatDate(course.lastLearnTime) }}</span>
                        </div>
                        <el-progress :percentage="course.progress || 0" :show-text="false" />
                      </div>

                      <div class="course-actions">
                        <el-button type="primary" @click="continueLearning(course)">
                          继续学习
                        </el-button>
                        <el-button type="danger" @click="dropCourse(course)">
                          退课
                        </el-button>
                      </div>
                    </div>
                  </el-card>
                </el-col>
              </transition-group>
            </el-row>
          </el-tab-pane>

          <!-- 教师的课程 -->
          <el-tab-pane v-if="isTeacher" label="我的课程" name="teaching" class="animate-fade-in" style="animation-delay: 0.4s;">
            <div v-if="teachingCourses.length === 0" class="empty-state">
              <el-empty description="您还没有创建任何课程" />
              <el-button type="primary" @click="createCourse">
                创建课程
              </el-button>
            </div>

            <el-table v-else :data="teachingCourses" style="width: 100%" :header-cell-style="{ 'text-align': 'center', 'background-color': '#4782c7' }">
              <el-table-column prop="title" label="课程名称" min-width="100">
                <template #default="scope">
                  <div class="course-name-cell" @click="viewCourse(scope.row.id)">
                    <el-avatar shape="square" :size="40" :src="scope.row.coverUrl || '/default-course.jpg'" />
                    <span>{{ scope.row.title }}</span>
                  </div>
                </template>
              </el-table-column>

              <el-table-column prop="createTime" label="创建时间" width="180" align="center">
                <template #default="scope">
                  {{ formatDate(scope.row.createTime) }}
                </template>
              </el-table-column>

              <el-table-column prop="studentCount" label="学生人数" width="120" align="center" />

              <el-table-column prop="state" label="状态" width="100" align="center">
                <template #default="scope">
                  <el-tag :type="getStateType(scope.row.state)">
                    {{ getStateText(scope.row.state) }}
                  </el-tag>
                </template>
              </el-table-column>

              <el-table-column label="操作" width="350">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewCourse(scope.row.id)">
                    查看
                  </el-button>
                  <el-button type="success" size="small" @click="viewStudents(scope.row)">
                    学生名单
                  </el-button>
                  <el-button v-if="scope.row.status !== 'approved'" type="warning" size="small"
                    @click="editCourse(scope.row)">
                    编辑
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- 学生名单对话框 -->
          <el-dialog v-model="studentsDialogVisible" :title="`${currentCourse?.title || ''} - 学生名单`" width="60%">
            <el-table :data="students" style="width: 100%">
              <el-table-column prop="username" label="学生姓名" />
              <el-table-column prop="email" label="邮箱" />
              <el-table-column prop="enrollTime" label="选课时间">
                <template #default="scope">
                  {{ formatDate(scope.row.enrollTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="progress" label="学习进度">
                <template #default="scope">
                  <el-progress :percentage="scope.row.progress || 0" />
                </template>
              </el-table-column>
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewStudentDetail(scope.row)">
                    查看详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="dialog-footer">
              <el-button @click="exportStudentList" type="success">
                导出名单
              </el-button>
              <el-button @click="studentsDialogVisible = false">
                关闭
              </el-button>
            </div>
          </el-dialog>
        </el-tabs>
      </div>
    </div>
  </transition>
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
            teacherName = profileData.records[key].realName|| '未知教师';
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
    
    console.log('已选课程:', enrolledCourses.value);
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
.my-courses-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
}

.my-courses-container>* {
  position: relative;
  z-index: 1;
}

.page-header {
  position: relative;
  text-align: center;
  margin-bottom: 40px;
  padding: 60px 0;
  max-width: 1250px;
  margin-left: auto;
  margin-right: auto;
  overflow: hidden;
}

.page-header::before {
  content: '';
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

.page-header::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  animation: headerFloat 6s ease-in-out infinite;
}

@keyframes headerFloat {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

.page-header h1 {
  position: relative;
  z-index: 2;
  font-size: 42px;
  font-weight: 700;
  color: white;
  margin: 0 0 16px 0;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
  letter-spacing: 1px;
}

.page-header p {
  position: relative;
  z-index: 2;
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  font-weight: 400;
  text-shadow: 0 1px 5px rgba(0, 0, 0, 0.2);
}

.action-bar {
  text-align: right;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
  padding: 0 20px 20px;
}

.action-bar .el-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  border-radius: 20px;
  font-weight: 600;
  padding: 14px 28px;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
  position: relative;
  overflow: hidden;
}

.action-bar .el-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.action-bar .el-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.6);
}

.action-bar .el-button:hover::before {
  left: 100%;
}

.action-bar .el-button:active {
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

/* 不同类型按钮的样式 */
.action-bar .el-button--success {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  box-shadow: 0 4px 15px rgba(72, 187, 120, 0.3);
}

.action-bar .el-button--success:hover {
  box-shadow: 0 8px 25px rgba(72, 187, 120, 0.6);
}

.action-bar .el-button--warning {
  background: linear-gradient(135deg, #ed8936 0%, #dd6b20 100%);
  box-shadow: 0 4px 15px rgba(237, 137, 54, 0.3);
}

.action-bar .el-button--warning:hover {
  box-shadow: 0 8px 25px rgba(237, 137, 54, 0.6);
}

.action-bar .el-button--danger {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  box-shadow: 0 4px 15px rgba(240, 147, 251, 0.3);
}

.action-bar .el-button--danger:hover {
  box-shadow: 0 8px 25px rgba(240, 147, 251, 0.6);
}

.courses-section {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 30px;
  box-shadow:
    0 20px 40px rgba(0, 0, 0, 0.1),
    0 8px 32px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.2);
  max-width: 1200px;
  margin: 0 auto 40px;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.courses-section:hover {
  transform: translateY(-2px);
  box-shadow:
    0 25px 50px rgba(0, 0, 0, 0.15),
    0 12px 40px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.course-col {
  margin-bottom: 30px;
}

.course-card {
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  height: 100%;
  border-radius: 20px;
  overflow: hidden;
  border: none;
  background: white;
  position: relative;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.course-card:hover {
  transform: translateY(-10px) scale(1.02);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

.course-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea, #764ba2, #f093fb, #f5576c);
  z-index: 1;
}

.course-image {
  position: relative;
  height: 220px;
  overflow: hidden;
  cursor: pointer;
}

.course-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.course-card:hover .course-image img {
  transform: scale(1.1);
}

.course-image::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.8), rgba(118, 75, 162, 0.8));
  opacity: 0;
  transition: opacity 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.course-card:hover .course-image::after {
  opacity: 1;
}

.progress-overlay {
  position: absolute;
  top: 6px;
  right: 6px;
  z-index: 2;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 36%;
  padding: 8px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  border: 2px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.progress-overlay:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.2);
}

.course-content {
  padding: 24px;
}

.course-title {
  margin: 0 0 16px 0;
  font-size: 20px;
  font-weight: 700;
  color: #2c3e50;
  cursor: pointer;
  transition: all 0.3s ease;
  line-height: 1.4;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.course-title:hover {
  color: #667eea;
}

.course-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: linear-gradient(135deg, #f8f9ff 0%, #e8f2ff 100%);
  border-radius: 12px;
  border-left: 4px solid #667eea;
  font-size: 14px;
  color: #2c3e50;
  font-weight: 500;
  transition: all 0.3s ease;
  flex: 1;
}

.meta-item:hover {
  background: linear-gradient(135deg, #e8f2ff 0%, #dbeafe 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.course-progress {
  margin-bottom: 20px;
  padding: 16px;
  background: linear-gradient(135deg, #f8f9ff 0%, #e8f2ff 100%);
  border-radius: 12px;
  border: 1px solid rgba(102, 126, 234, 0.1);
  transition: all 0.3s ease;
}

.course-progress:hover {
  background: linear-gradient(135deg, #e8f2ff 0%, #dbeafe 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
}

.progress-text {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
  color: #4a5568;
  font-weight: 600;
}

.course-actions {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.course-actions .el-button {
  border-radius: 20px;
  font-weight: 600;
  transition: all 0.3s ease;
  flex: 1;
}

.course-actions .el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.course-actions .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

.course-actions .el-button--danger {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(240, 147, 251, 0.3);
}

.course-actions .el-button--danger:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(240, 147, 251, 0.5);
}

/* 教师课程表格样式 */
:deep(.el-table) {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: none;
}

:deep(.el-table__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

:deep(.el-table__header th) {
  background: transparent;
  border: none;
  color: white;
  font-weight: 700;
  font-size: 14px;
  padding: 20px 16px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

:deep(.el-table__body tr) {
  background: white;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  animation: tableRowSlideIn 0.5s cubic-bezier(0.25, 0.8, 0.25, 1) forwards;
  opacity: 0;
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
}

:deep(.el-table__body tr:nth-child(1)) {
  animation-delay: 0.1s;
}

:deep(.el-table__body tr:nth-child(2)) {
  animation-delay: 0.2s;
}

:deep(.el-table__body tr:nth-child(3)) {
  animation-delay: 0.3s;
}

:deep(.el-table__body tr:nth-child(4)) {
  animation-delay: 0.4s;
}

:deep(.el-table__body tr:nth-child(5)) {
  animation-delay: 0.5s;
}

:deep(.el-table__body tr:nth-child(n+6)) {
  animation-delay: 0.6s;
}

@keyframes tableRowSlideIn {
  0% {
    opacity: 0;
    transform: translateX(-30px);
  }

  100% {
    opacity: 1;
    transform: translateX(0);
  }
}

:deep(.el-table__body tr:hover) {
  background: linear-gradient(135deg, #f8f9ff 0%, #e8f2ff 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.15);
}

:deep(.el-table__body td) {
  border: none;
  padding: 20px 16px;
  vertical-align: middle;
}

.course-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.course-name-cell:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.05) 100%);
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
}

.course-name-cell span {
  font-weight: 600;
  color: #2c3e50;
  transition: all 0.3s ease;
}

.course-name-cell:hover span {
  color: #667eea;
}

:deep(.el-avatar) {
  border: 2px solid rgba(102, 126, 234, 0.2);
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.course-name-cell:hover :deep(.el-avatar) {
  border-color: #667eea;
  transform: scale(1.05);
}

/* 表格按钮样式 */
:deep(.el-table .el-button) {
  border-radius: 20px;
  font-weight: 600;
  transition: all 0.3s ease;
  margin: 0 4px;
}

:deep(.el-table .el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

:deep(.el-table .el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

:deep(.el-table .el-button--success) {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(72, 187, 120, 0.3);
}

:deep(.el-table .el-button--success:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(72, 187, 120, 0.5);
}

:deep(.el-table .el-button--warning) {
  background: linear-gradient(135deg, #ed8936 0%, #dd6b20 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(237, 137, 54, 0.3);
}

:deep(.el-table .el-button--warning:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(237, 137, 54, 0.5);
}

.course-name-cell:hover :deep(.el-avatar) {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

:deep(.el-tag) {
  border-radius: 20px;
  padding: 6px 16px;
  font-weight: 600;
  border: none;
  backdrop-filter: blur(10px);
}

:deep(.el-tag.el-tag--success) {
  background: linear-gradient(135deg, rgba(72, 187, 120, 0.2) 0%, rgba(56, 178, 172, 0.2) 100%);
  color: #2f855a;
}

:deep(.el-tag.el-tag--info) {
  background: linear-gradient(135deg, rgba(113, 128, 150, 0.2) 0%, rgba(74, 85, 104, 0.2) 100%);
  color: #4a5568;
}

:deep(.el-tag.el-tag--danger) {
  background: linear-gradient(135deg, rgba(245, 101, 101, 0.2) 0%, rgba(229, 62, 62, 0.2) 100%);
  color: #c53030;
}

/* 按钮样式 */
:deep(.el-button) {
  border-radius: 12px;
  font-weight: 600;
  padding: 12px 24px;
  font-size: 14px;
  letter-spacing: 0.3px;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

:deep(.el-button::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

:deep(.el-button:hover::before) {
  left: 100%;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.2);
}

:deep(.el-button--primary:hover) {
  transform: translateY(-3px);
  box-shadow: 0 12px 35px rgba(102, 126, 234, 0.4);
  background: linear-gradient(135deg, #5a67d8 0%, #6b46c1 100%);
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