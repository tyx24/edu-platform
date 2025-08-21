<template>
  <div class="my-courses-container">
    <div class="page-header">
      <h1>{{ isTeacher ? '我的教学课程' : '我的学习课程' }}</h1>
      <p>{{ isTeacher ? '管理您创建的课程和学生' : '查看您已选的课程和学习进度' }}</p>
    </div>
    
    <!-- 教师创建课程按钮 -->
    <div class="action-bar" v-if="isTeacher">
      <el-button type="primary" @click="createCourse">
        创建新课程
      </el-button>
    </div>
    
    <!-- 课程列表 -->
    <div class="courses-section">
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
            <el-col 
              v-for="course in enrolledCourses" 
              :key="course.id" 
              :xs="24" 
              :sm="12" 
              :md="8" 
              :lg="6"
              class="course-col"
            >
              <el-card class="course-card" shadow="hover">
                <div class="course-image" @click="viewCourse(course.id)">
                  <img :src="course.coverImage || '/default-course.jpg'" :alt="course.title">
                  <div class="progress-overlay">
                    <el-progress 
                      type="circle" 
                      :percentage="course.progress || 0" 
                      :width="60"
                      :stroke-width="6"
                    />
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
          </el-row>
        </el-tab-pane>
        
        <!-- 教师的课程 -->
        <el-tab-pane v-if="isTeacher" label="我的课程" name="teaching">
          <div v-if="teachingCourses.length === 0" class="empty-state">
            <el-empty description="您还没有创建任何课程" />
            <el-button type="primary" @click="createCourse">
              创建课程
            </el-button>
          </div>
          
          <el-table v-else :data="teachingCourses" style="width: 100%">
            <el-table-column prop="title" label="课程名称" min-width="200">
              <template #default="scope">
                <div class="course-name-cell" @click="viewCourse(scope.row.id)">
                  <el-avatar shape="square" :size="40" :src="scope.row.coverImage || '/default-course.jpg'" />
                  <span>{{ scope.row.title }}</span>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column prop="createTime" label="创建时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.createTime) }}
              </template>
            </el-table-column>
            
            <el-table-column prop="studentCount" label="学生数" width="100" />
            
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="250">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewCourse(scope.row.id)">
                  查看
                </el-button>
                <el-button type="success" size="small" @click="viewStudents(scope.row)">
                  学生名单
                </el-button>
                <el-button 
                  v-if="scope.row.status !== 'approved'"
                  type="warning" 
                  size="small" 
                  @click="editCourse(scope.row)"
                >
                  编辑
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <!-- 学生名单对话框 -->
        <el-dialog
          v-model="studentsDialogVisible"
          :title="`${currentCourse?.title || ''} - 学生名单`"
          width="60%"
        >
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
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { enrollmentApi } from '../../api/enrollment';
import { courseApi } from '../../api/course';
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
    const res = await enrollmentApi.getMyCourses();
    enrolledCourses.value = res.data || [];
  } catch (error) {
    console.error('获取已选课程失败:', error);
    ElMessage.error('获取已选课程失败');
  }
};

// 获取教师课程
const fetchTeachingCourses = async () => {
  try {
    const res = await enrollmentApi.getMyCoursesWithStudents();
    teachingCourses.value = res.data || [];
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
    // 假设API返回的课程数据中已经包含了学生列表
    students.value = course.students || [];
    
    // 如果没有学生列表，则需要单独请求
    if (!students.value.length) {
      const res = await enrollmentApi.getCourseStudents(course.id);
      students.value = res.data || [];
    }
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
const getStatusType = (status) => {
  const statusMap = {
    'pending': 'warning',
    'approved': 'success',
    'rejected': 'danger'
  };
  return statusMap[status] || 'info';
};

// 获取课程状态文本
const getStatusText = (status) => {
  const statusMap = {
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '已驳回'
  };
  return statusMap[status] || '未知状态';
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 32px;
  color: #333;
  margin: 0 0 12px 0;
}

.page-header p {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.action-bar {
  margin-bottom: 20px;
  text-align: right;
}

.courses-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.course-col {
  margin-bottom: 20px;
}

.course-card {
  height: 100%;
  transition: transform 0.3s;
}

.course-image {
  position: relative;
  height: 180px;
  overflow: hidden;
  cursor: pointer;
}

.course-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.course-image:hover img {
  transform: scale(1.05);
}

.progress-overlay {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  padding: 5px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.course-content {
  padding: 15px;
}

.course-title {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  cursor: pointer;
  transition: color 0.3s;
}

.course-title:hover {
  color: #409EFF;
}

.course-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  color: #666;
  font-size: 14px;
}

.course-progress {
  margin-bottom: 15px;
}

.progress-text {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  font-size: 12px;
  color: #666;
}

.course-actions {
  display: flex;
  justify-content: space-between;
}

/* 教师课程表格样式 */
.course-name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.course-name-cell span {
  transition: color 0.3s;
}

.course-name-cell:hover span {
  color: #409EFF;
}

/* 对话框样式 */
.dialog-footer {
  margin-top: 20px;
  text-align: right;
}
</style>