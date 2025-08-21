<template>
  <div class="teacher-dashboard">
    <el-tabs v-model="activeTab" class="teacher-tabs">
      <el-tab-pane label="我的课程" name="courses">
        <div class="tab-header">
          <h2>我的课程</h2>
          <el-button type="primary" @click="createCourse">
            <el-icon><Plus /></el-icon> 创建新课程
          </el-button>
        </div>
        
        <el-table
          v-loading="coursesLoading"
          :data="myCourses"
          border
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="课程名称" width="180" />
          <el-table-column prop="category" label="分类" width="120" />
          <el-table-column label="难度" width="100">
            <template #default="scope">
              <el-tag :type="getDifficultyTagType(scope.row.difficulty)">
                {{ getDifficultyText(scope.row.difficulty) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getCourseStatusType(scope.row.status)">
                {{ getCourseStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="studentCount" label="学生数" width="100" />
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="320">
            <template #default="scope">
              <el-button 
                size="small" 
                type="primary"
                @click="viewCourseDetail(scope.row)"
              >
                查看详情
              </el-button>
              <el-button 
                size="small" 
                type="success"
                @click="viewStudents(scope.row)"
              >
                学生名单
              </el-button>
              <el-button 
                size="small" 
                type="warning"
                @click="editCourse(scope.row)"
                :disabled="scope.row.status === 'APPROVED'"
              >
                编辑
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <el-empty v-if="myCourses.length === 0 && !coursesLoading" description="暂无课程，点击创建新课程" />
      </el-tab-pane>
      
      <el-tab-pane label="考试管理" name="exams">
        <div class="tab-header">
          <h2>考试管理</h2>
          <div class="filter-box">
            <el-select v-model="examCourseFilter" placeholder="选择课程" clearable @change="fetchExams">
              <el-option 
                v-for="course in myCourses" 
                :key="course.id" 
                :label="course.title" 
                :value="course.id" 
              />
            </el-select>
          </div>
        </div>
        
        <el-table
          v-loading="examsLoading"
          :data="myExams"
          border
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="考试名称" width="180" />
          <el-table-column prop="courseName" label="所属课程" width="180" />
          <el-table-column prop="startTime" label="开始时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="结束时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.endTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="duration" label="时长(分钟)" width="120" />
          <el-table-column prop="totalScore" label="总分" width="80" />
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getExamStatusType(scope.row)">
                {{ getExamStatusText(scope.row) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="320">
            <template #default="scope">
              <el-button 
                size="small" 
                type="primary"
                @click="manageQuestions(scope.row)"
              >
                题目管理
              </el-button>
              <el-button 
                size="small" 
                type="success"
                @click="viewExamRecords(scope.row)"
              >
                考试记录
              </el-button>
              <el-button 
                size="small" 
                type="warning"
                @click="editExam(scope.row)"
                :disabled="isExamStarted(scope.row)"
              >
                编辑
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <el-empty v-if="myExams.length === 0 && !examsLoading" description="暂无考试，请先选择课程" />
      </el-tab-pane>
      
      <el-tab-pane label="作业管理" name="homework">
        <div class="tab-header">
          <h2>作业管理</h2>
          <div class="filter-box">
            <el-select v-model="homeworkCourseFilter" placeholder="选择课程" clearable @change="fetchHomework">
              <el-option 
                v-for="course in myCourses" 
                :key="course.id" 
                :label="course.title" 
                :value="course.id" 
              />
            </el-select>
          </div>
        </div>
        
        <el-table
          v-loading="homeworkLoading"
          :data="myHomework"
          border
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="作业名称" width="180" />
          <el-table-column prop="courseName" label="所属课程" width="180" />
          <el-table-column prop="startTime" label="开始时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="截止时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.endTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="totalScore" label="总分" width="80" />
          <el-table-column prop="submissionCount" label="提交数" width="100" />
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getHomeworkStatusType(scope.row.status)">
                {{ getHomeworkStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="320">
            <template #default="scope">
              <el-button 
                size="small" 
                type="primary"
                @click="viewHomeworkDetail(scope.row)"
              >
                查看详情
              </el-button>
              <el-button 
                size="small" 
                type="success"
                @click="viewSubmissions(scope.row)"
              >
                提交记录
              </el-button>
              <el-button 
                size="small" 
                type="warning"
                @click="editHomework(scope.row)"
                :disabled="scope.row.status === 'EXPIRED'"
              >
                编辑
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <el-empty v-if="myHomework.length === 0 && !homeworkLoading" description="暂无作业，请先选择课程" />
      </el-tab-pane>
      
      <el-tab-pane label="学生管理" name="students">
        <div class="tab-header">
          <h2>学生管理</h2>
          <div class="filter-box">
            <el-select v-model="studentCourseFilter" placeholder="选择课程" clearable @change="fetchStudents">
              <el-option 
                v-for="course in myCourses" 
                :key="course.id" 
                :label="course.title" 
                :value="course.id" 
              />
            </el-select>
            <el-input
              v-model="studentSearchQuery"
              placeholder="搜索学生姓名/学号"
              clearable
              @clear="fetchStudents"
              @keyup.enter="fetchStudents"
            >
              <template #append>
                <el-button @click="fetchStudents">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
            <el-button type="success" @click="exportStudentList" :disabled="!studentCourseFilter">
              <el-icon><Download /></el-icon> 导出名单
            </el-button>
          </div>
        </div>
        
        <el-table
          v-loading="studentsLoading"
          :data="courseStudents"
          border
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="realName" label="姓名" width="120" />
          <el-table-column prop="studentId" label="学号" width="120" />
          <el-table-column prop="email" label="邮箱" width="180" />
          <el-table-column prop="enrollTime" label="选课时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.enrollTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="progress" label="学习进度" width="180">
            <template #default="scope">
              <el-progress :percentage="scope.row.progress || 0" />
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="180">
            <template #default="scope">
              <el-button 
                size="small" 
                type="primary"
                @click="viewStudentDetail(scope.row)"
              >
                查看详情
              </el-button>
              <el-button 
                size="small" 
                type="danger"
                @click="removeStudent(scope.row)"
              >
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <el-empty v-if="courseStudents.length === 0 && !studentsLoading" description="暂无学生，请先选择课程" />
      </el-tab-pane>
      
      <el-tab-pane label="个人资料" name="profile">
        <div class="tab-header">
          <h2>个人资料</h2>
        </div>
        
        <el-card class="profile-card">
          <template #header>
            <div class="card-header">
              <span>基本信息</span>
              <el-button type="primary" @click="editProfile">编辑资料</el-button>
            </div>
          </template>
          
          <div class="profile-info">
            <div class="info-item">
              <span class="info-label">用户名:</span>
              <span class="info-value">{{ userProfile.username }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">真实姓名:</span>
              <span class="info-value">{{ userProfile.realName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">邮箱:</span>
              <span class="info-value">{{ userProfile.email }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">电话:</span>
              <span class="info-value">{{ userProfile.phone || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">职称:</span>
              <span class="info-value">{{ userProfile.title || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">院系:</span>
              <span class="info-value">{{ userProfile.department || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">简介:</span>
              <span class="info-value">{{ userProfile.bio || '未设置' }}</span>
            </div>
          </div>
        </el-card>
        
        <el-card class="profile-card">
          <template #header>
            <div class="card-header">
              <span>修改密码</span>
            </div>
          </template>
          
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changePassword">修改密码</el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 学生详情对话框 -->
    <el-dialog
      v-model="studentDetailDialogVisible"
      title="学生详情"
      width="600px"
    >
      <div v-if="selectedStudent" class="student-detail">
        <div class="detail-item">
          <span class="detail-label">用户名:</span>
          <span class="detail-value">{{ selectedStudent.username }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">姓名:</span>
          <span class="detail-value">{{ selectedStudent.realName }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">学号:</span>
          <span class="detail-value">{{ selectedStudent.studentId }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">邮箱:</span>
          <span class="detail-value">{{ selectedStudent.email }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">选课时间:</span>
          <span class="detail-value">{{ formatDateTime(selectedStudent.enrollTime) }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">学习进度:</span>
          <span class="detail-value">
            <el-progress :percentage="selectedStudent.progress || 0" />
          </span>
        </div>
        
        <div class="detail-section">
          <h3>考试记录</h3>
          <el-table :data="studentExamRecords" border style="width: 100%">
            <el-table-column prop="examName" label="考试名称" />
            <el-table-column prop="score" label="得分" width="80" />
            <el-table-column prop="submitTime" label="提交时间" width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.submitTime) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'PASS' ? 'success' : 'danger'">
                  {{ scope.row.status === 'PASS' ? '通过' : '未通过' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="studentExamRecords.length === 0" description="暂无考试记录" />
        </div>
        
        <div class="detail-section">
          <h3>作业记录</h3>
          <el-table :data="studentHomeworkRecords" border style="width: 100%">
            <el-table-column prop="homeworkName" label="作业名称" />
            <el-table-column prop="score" label="得分" width="80" />
            <el-table-column prop="submitTime" label="提交时间" width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.submitTime) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getSubmissionStatusType(scope.row.status)">
                  {{ getSubmissionStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="studentHomeworkRecords.length === 0" description="暂无作业记录" />
        </div>
      </div>
    </el-dialog>
    
    <!-- 编辑个人资料对话框 -->
    <el-dialog
      v-model="profileDialogVisible"
      title="编辑个人资料"
      width="500px"
    >
      <el-form :model="profileForm" :rules="profileRules" ref="profileFormRef" label-width="100px">
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="profileForm.realName" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="profileForm.phone" />
        </el-form-item>
        <el-form-item label="职称" prop="title">
          <el-input v-model="profileForm.title" />
        </el-form-item>
        <el-form-item label="院系" prop="department">
          <el-input v-model="profileForm.department" />
        </el-form-item>
        <el-form-item label="简介" prop="bio">
          <el-input v-model="profileForm.bio" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="profileDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveProfile">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Search, Download } from '@element-plus/icons-vue';
import { courseApi } from '../../api/course';
import { examApi } from '../../api/exam';
import { homeworkApi } from '../../api/homework';
import { enrollmentApi } from '../../api/enrollment';
import { userApi } from '../../api/user';
import { useAuthStore } from '../../store/auth';

const router = useRouter();
const authStore = useAuthStore();

// 当前激活的标签页
const activeTab = ref('courses');

// 我的课程相关
const myCourses = ref([]);
const coursesLoading = ref(false);

// 考试管理相关
const myExams = ref([]);
const examsLoading = ref(false);
const examCourseFilter = ref('');

// 作业管理相关
const myHomework = ref([]);
const homeworkLoading = ref(false);
const homeworkCourseFilter = ref('');

// 学生管理相关
const courseStudents = ref([]);
const studentsLoading = ref(false);
const studentCourseFilter = ref('');
const studentSearchQuery = ref('');

// 学生详情对话框
const studentDetailDialogVisible = ref(false);
const selectedStudent = ref(null);
const studentExamRecords = ref([]);
const studentHomeworkRecords = ref([]);

// 个人资料相关
const userProfile = ref({});
const profileDialogVisible = ref(false);
const profileForm = ref({
  realName: '',
  phone: '',
  title: '',
  department: '',
  bio: ''
});

// 密码表单
const passwordFormRef = ref(null);
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 表单验证规则
const profileRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
};

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

// 监听标签页变化
watch(activeTab, (newVal) => {
  if (newVal === 'courses') {
    fetchMyCourses();
  } else if (newVal === 'exams' && examCourseFilter.value) {
    fetchExams();
  } else if (newVal === 'homework' && homeworkCourseFilter.value) {
    fetchHomework();
  } else if (newVal === 'students' && studentCourseFilter.value) {
    fetchStudents();
  } else if (newVal === 'profile') {
    fetchUserProfile();
  }
});

// 获取我的课程
const fetchMyCourses = async () => {
  coursesLoading.value = true;
  try {
    const res = await enrollmentApi.getTeacherCoursesWithStudents();
    myCourses.value = res.data || [];
  } catch (error) {
    console.error('获取我的课程失败:', error);
    ElMessage.error('获取我的课程失败');
  } finally {
    coursesLoading.value = false;
  }
};

// 获取考试列表
const fetchExams = async () => {
  if (!examCourseFilter.value) return;
  
  examsLoading.value = true;
  try {
    const res = await examApi.getCourseExams(examCourseFilter.value);
    myExams.value = res.data || [];
  } catch (error) {
    console.error('获取考试列表失败:', error);
    ElMessage.error('获取考试列表失败');
  } finally {
    examsLoading.value = false;
  }
};

// 获取作业列表
const fetchHomework = async () => {
  if (!homeworkCourseFilter.value) return;
  
  homeworkLoading.value = true;
  try {
    const res = await homeworkApi.getCourseHomework(homeworkCourseFilter.value);
    myHomework.value = res.data || [];
  } catch (error) {
    console.error('获取作业列表失败:', error);
    ElMessage.error('获取作业列表失败');
  } finally {
    homeworkLoading.value = false;
  }
};

// 获取学生列表
const fetchStudents = async () => {
  if (!studentCourseFilter.value) return;
  
  studentsLoading.value = true;
  try {
    // 假设API支持按课程ID和学生姓名/学号搜索
    const params = {
      courseId: studentCourseFilter.value,
      keyword: studentSearchQuery.value
    };
    
    const res = await enrollmentApi.getCourseStudents(params);
    courseStudents.value = res.data || [];
  } catch (error) {
    console.error('获取学生列表失败:', error);
    ElMessage.error('获取学生列表失败');
  } finally {
    studentsLoading.value = false;
  }
};

// 获取用户资料
const fetchUserProfile = async () => {
  try {
    const res = await userApi.getCurrentUser();
    userProfile.value = res.data || {};
  } catch (error) {
    console.error('获取用户资料失败:', error);
    ElMessage.error('获取用户资料失败');
  }
};

// 创建新课程
const createCourse = () => {
  router.push('/course/create');
};

// 查看课程详情
const viewCourseDetail = (course) => {
  router.push(`/course/${course.id}`);
};

// 查看学生名单
const viewStudents = (course) => {
  studentCourseFilter.value = course.id;
  activeTab.value = 'students';
  fetchStudents();
};

// 编辑课程
const editCourse = (course) => {
  router.push(`/course/edit/${course.id}`);
};

// 管理题目
const manageQuestions = (exam) => {
  router.push(`/exam/${exam.id}/questions`);
};

// 查看考试记录
const viewExamRecords = (exam) => {
  router.push(`/exam/${exam.id}/records`);
};

// 编辑考试
const editExam = (exam) => {
  router.push(`/exam/edit/${exam.id}`);
};

// 查看作业详情
const viewHomeworkDetail = (homework) => {
  router.push(`/homework/${homework.id}`);
};

// 查看提交记录
const viewSubmissions = (homework) => {
  router.push(`/homework/${homework.id}/submissions`);
};

// 编辑作业
const editHomework = (homework) => {
  router.push(`/homework/edit/${homework.id}`);
};

// 导出学生名单
const exportStudentList = async () => {
  if (!studentCourseFilter.value) return;
  
  try {
    const res = await enrollmentApi.exportEnrollments(studentCourseFilter.value);
    // 假设返回的是一个下载链接
    if (res.data && res.data.downloadUrl) {
      window.open(res.data.downloadUrl, '_blank');
      ElMessage.success('导出成功');
    }
  } catch (error) {
    console.error('导出学生名单失败:', error);
    ElMessage.error('导出学生名单失败');
  }
};

// 查看学生详情
const viewStudentDetail = async (student) => {
  selectedStudent.value = student;
  studentDetailDialogVisible.value = true;
  
  // 获取学生的考试记录
  try {
    // 假设有API获取学生在特定课程的考试记录
    const examRes = await examApi.getStudentExamRecords({
      studentId: student.id,
      courseId: studentCourseFilter.value
    });
    studentExamRecords.value = examRes.data || [];
    
    // 获取学生的作业记录
    const homeworkRes = await homeworkApi.getStudentHomeworkRecords({
      studentId: student.id,
      courseId: studentCourseFilter.value
    });
    studentHomeworkRecords.value = homeworkRes.data || [];
  } catch (error) {
    console.error('获取学生记录失败:', error);
    ElMessage.error('获取学生记录失败');
  }
};

// 移除学生
const removeStudent = (student) => {
  ElMessageBox.confirm(
    `确定要将学生 ${student.realName} 从课程中移除吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await enrollmentApi.removeStudentFromCourse({
        studentId: student.id,
        courseId: studentCourseFilter.value
      });
      ElMessage.success('学生已移除');
      fetchStudents();
    } catch (error) {
      console.error('移除学生失败:', error);
      ElMessage.error('移除学生失败');
    }
  }).catch(() => {});
};

// 编辑个人资料
const editProfile = () => {
  profileForm.value = {
    realName: userProfile.value.realName || '',
    phone: userProfile.value.phone || '',
    title: userProfile.value.title || '',
    department: userProfile.value.department || '',
    bio: userProfile.value.bio || ''
  };
  profileDialogVisible.value = true;
};

// 保存个人资料
const saveProfile = async () => {
  try {
    await userApi.updateUserProfile(profileForm.value);
    ElMessage.success('个人资料更新成功');
    profileDialogVisible.value = false;
    fetchUserProfile();
  } catch (error) {
    console.error('更新个人资料失败:', error);
    ElMessage.error('更新个人资料失败');
  }
};

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return;
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await userApi.changePassword({
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword
        });
        ElMessage.success('密码修改成功');
        resetPasswordForm();
      } catch (error) {
        console.error('修改密码失败:', error);
        ElMessage.error('修改密码失败');
      }
    }
  });
};

// 重置密码表单
const resetPasswordForm = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  };
  if (passwordFormRef.value) {
    passwordFormRef.value.resetFields();
  }
};

// 获取难度标签类型
const getDifficultyTagType = (difficulty) => {
  const typeMap = {
    'BEGINNER': 'info',
    'INTERMEDIATE': 'warning',
    'ADVANCED': 'danger'
  };
  return typeMap[difficulty] || 'info';
};

// 获取难度文本
const getDifficultyText = (difficulty) => {
  const textMap = {
    'BEGINNER': '初级',
    'INTERMEDIATE': '中级',
    'ADVANCED': '高级'
  };
  return textMap[difficulty] || '未知';
};

// 获取课程状态类型
const getCourseStatusType = (status) => {
  const typeMap = {
    'DRAFT': 'info',
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  };
  return typeMap[status] || 'info';
};

// 获取课程状态文本
const getCourseStatusText = (status) => {
  const textMap = {
    'DRAFT': '草稿',
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  };
  return textMap[status] || '未知';
};

// 获取考试状态类型
const getExamStatusType = (exam) => {
  const now = new Date();
  const startTime = new Date(exam.startTime);
  const endTime = new Date(exam.endTime);
  
  if (now < startTime) {
    return 'info';
  } else if (now >= startTime && now <= endTime) {
    return 'success';
  } else {
    return 'warning';
  }
};

// 获取考试状态文本
const getExamStatusText = (exam) => {
  const now = new Date();
  const startTime = new Date(exam.startTime);
  const endTime = new Date(exam.endTime);
  
  if (now < startTime) {
    return '未开始';
  } else if (now >= startTime && now <= endTime) {
    return '进行中';
  } else {
    return '已结束';
  }
};

// 判断考试是否已开始
const isExamStarted = (exam) => {
  const now = new Date();
  const startTime = new Date(exam.startTime);
  return now >= startTime;
};

// 获取作业状态类型
const getHomeworkStatusType = (status) => {
  const typeMap = {
    'DRAFT': 'info',
    'PUBLISHED': 'success',
    'EXPIRED': 'warning'
  };
  return typeMap[status] || 'info';
};

// 获取作业状态文本
const getHomeworkStatusText = (status) => {
  const textMap = {
    'DRAFT': '草稿',
    'PUBLISHED': '已发布',
    'EXPIRED': '已截止'
  };
  return textMap[status] || '未知';
};

// 获取提交状态类型
const getSubmissionStatusType = (status) => {
  const typeMap = {
    'DRAFT': 'info',
    'SUBMITTED': 'warning',
    'GRADED': 'success'
  };
  return typeMap[status] || 'info';
};

// 获取提交状态文本
const getSubmissionStatusText = (status) => {
  const textMap = {
    'DRAFT': '草稿',
    'SUBMITTED': '已提交',
    'GRADED': '已评分'
  };
  return textMap[status] || '未知';
};

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString();
};

onMounted(() => {
  fetchMyCourses();
});
</script>

<style scoped>
/* 页面整体效果 */
.teacher-dashboard {
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

/* 背景动画 */
@keyframes gradientFlow {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

/* 标签页样式 */
:deep(.teacher-tabs) {
  margin-top: 20px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

:deep(.teacher-tabs:hover) {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

:deep(.el-tabs__header) {
  margin-bottom: 20px;
}

:deep(.el-tabs__nav-wrap) {
  background: rgba(255, 255, 255, 0.5);
  border-radius: 15px;
  padding: 5px;
}

:deep(.el-tabs__item) {
  border-radius: 12px;
  margin: 0 5px;
  padding: 12px 20px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

:deep(.el-tabs__item::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.2), transparent);
  transition: left 0.5s ease;
}

:deep(.el-tabs__item:hover::before) {
  left: 100%;
}

:deep(.el-tabs__item:hover) {
  color: #667eea;
  transform: translateY(-2px);
}

:deep(.el-tabs__item.is-active) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

/* 标签页头部 */
.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px 0;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
}

.tab-header h2 {
  margin: 0;
  color: #2c3e50;
  font-weight: 700;
  font-size: 24px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 筛选框样式 */
.filter-box {
  display: flex;
  gap: 15px;
  align-items: center;
  flex-wrap: wrap;
}

/* 按钮样式 */
:deep(.el-button) {
  border-radius: 12px;
  font-weight: 600;
  padding: 12px 24px;
  border: none;
  backdrop-filter: blur(10px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

:deep(.el-button::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s ease;
}

:deep(.el-button:hover::before) {
  left: 100%;
}

:deep(.el-button:hover) {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

:deep(.el-button--success) {
  background: linear-gradient(135deg, #56ab2f, #a8e6cf);
  color: white;
}

:deep(.el-button--warning) {
  background: linear-gradient(135deg, #f093fb, #f5576c);
  color: white;
}

:deep(.el-button--danger) {
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  color: white;
}

/* 表格样式 */
:deep(.el-table) {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

:deep(.el-table__header) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
}

:deep(.el-table th) {
  background: transparent;
  color: #2c3e50;
  font-weight: 700;
  border-bottom: 2px solid rgba(102, 126, 234, 0.2);
}

:deep(.el-table td) {
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

:deep(.el-table__row:hover) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  transform: translateX(5px);
}

/* 标签样式 */
:deep(.el-tag) {
  border-radius: 20px;
  padding: 8px 16px;
  font-weight: 600;
  border: none;
  backdrop-filter: blur(10px);
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

:deep(.el-tag:hover) {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

:deep(.el-tag--success) {
  background: linear-gradient(135deg, #56ab2f, #a8e6cf);
}

:deep(.el-tag--warning) {
  background: linear-gradient(135deg, #f093fb, #f5576c);
}

:deep(.el-tag--danger) {
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
}

:deep(.el-tag--info) {
  background: linear-gradient(135deg, #74b9ff, #0984e3);
}

/* 个人资料卡片 */
.profile-card {
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.profile-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.15);
}

:deep(.profile-card .el-card__header) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  border-bottom: 2px solid rgba(102, 126, 234, 0.2);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-weight: 700;
  color: #2c3e50;
  font-size: 18px;
}

/* 个人信息样式 */
.profile-info {
  padding: 20px 0;
}

.info-item {
  display: flex;
  margin-bottom: 15px;
  padding: 10px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.info-label {
  width: 120px;
  font-weight: 600;
  color: #666;
}

.info-value {
  flex: 1;
  color: #2c3e50;
}

/* 表单样式 */
:deep(.el-form-item__label) {
  font-weight: 600;
  color: #2c3e50;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(102, 126, 234, 0.5);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.2);
}

:deep(.el-input__wrapper.is-focus) {
  background: rgba(255, 255, 255, 0.95);
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

:deep(.el-textarea__inner) {
  border-radius: 12px;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 12px;
}

/* 对话框样式 */
:deep(.el-dialog) {
  border-radius: 20px;
  backdrop-filter: blur(20px);
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 20px;
  border-radius: 20px 20px 0 0;
}

:deep(.el-dialog__title) {
  font-weight: 700;
  font-size: 18px;
}

:deep(.el-dialog__body) {
  padding: 30px;
  background: rgba(255, 255, 255, 0.9);
}

/* 学生详情样式 */
.student-detail {
  padding: 20px 0;
}

.detail-item {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 15px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 12px;
  border-left: 4px solid #667eea;
}

.detail-label {
  font-weight: 600;
  color: #667eea;
  font-size: 14px;
}

.detail-value {
  color: #2c3e50;
  font-size: 16px;
}

.detail-section {
  margin-top: 30px;
}

.detail-section h3 {
  color: #2c3e50;
  font-weight: 700;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.2);
}

/* 进度条样式 */
:deep(.el-progress-bar__outer) {
  background: rgba(102, 126, 234, 0.1);
  border-radius: 10px;
}

:deep(.el-progress-bar__inner) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 10px;
}

/* 空状态样式 */
:deep(.el-empty) {
  padding: 60px 20px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 15px;
  margin: 20px 0;
  backdrop-filter: blur(10px);
}

:deep(.el-empty__description) {
  color: #666;
  font-size: 16px;
  font-weight: 500;
}

/* 下拉选择框样式 */
:deep(.el-select-dropdown) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

:deep(.el-select-dropdown__item) {
  transition: all 0.3s ease;
}

:deep(.el-select-dropdown__item:hover) {
  background: linear-gradient(135deg, #74b9ff, #0984e3);
  color: white;
  transform: translateX(5px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .teacher-dashboard {
    padding: 0 10px;
  }
  
  .tab-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .filter-box {
    flex-direction: column;
    align-items: stretch;
  }
  
  :deep(.el-table) {
    font-size: 12px;
  }
  
  .profile-info {
    padding: 10px 0;
  }
  
  .info-item {
    flex-direction: column;
    gap: 5px;
  }
  
  .info-label {
    width: auto;
    font-size: 14px;
  }
}
</style>