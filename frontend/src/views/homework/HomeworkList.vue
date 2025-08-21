<template>
  <div class="homework-list-container">
    <div class="page-header">
      <h1>{{ course.title }} - 作业列表</h1>
      <p v-if="course.description">{{ course.description }}</p>
    </div>
    
    <!-- 教师操作区 -->
    <div v-if="isTeacher" class="teacher-actions">
      <el-button type="primary" @click="showCreateHomeworkDialog">
        <el-icon><Plus /></el-icon> 创建新作业
      </el-button>
      <el-button @click="$router.push(`/course/${courseId}`)">
        返回课程
      </el-button>
    </div>
    
    <!-- 学生操作区 -->
    <div v-else class="student-actions">
      <el-button @click="$router.push(`/course/${courseId}`)">
        返回课程
      </el-button>
    </div>
    
    <!-- 作业列表 -->
    <div class="homework-section">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else-if="homeworkList.length === 0" class="empty-state">
        <el-empty description="暂无作业" />
        <el-button v-if="isTeacher" type="primary" @click="showCreateHomeworkDialog">
          创建新作业
        </el-button>
      </div>
      
      <div v-else class="homework-list">
        <el-card 
          v-for="homework in homeworkList" 
          :key="homework.id"
          class="homework-card"
          shadow="hover"
        >
          <div class="homework-header">
            <div class="homework-title">
              <h2>{{ homework.title }}</h2>
              <el-tag 
                :type="getHomeworkStatusType(homework.status)" 
                effect="dark"
              >
                {{ getHomeworkStatusText(homework.status) }}
              </el-tag>
            </div>
            
            <div class="homework-actions">
              <!-- 教师操作 -->
              <template v-if="isTeacher">
                <el-button type="primary" size="small" @click="editHomework(homework)">
                  编辑
                </el-button>
                <el-button type="success" size="small" @click="viewSubmissions(homework)">
                  查看提交
                </el-button>
                <el-button type="danger" size="small" @click="deleteHomework(homework)">
                  删除
                </el-button>
              </template>
              
              <!-- 学生操作 -->
              <template v-else>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="viewHomeworkDetail(homework)"
                >
                  {{ getStudentActionText(homework) }}
                </el-button>
              </template>
            </div>
          </div>
          
          <div class="homework-content">
            <div class="homework-description">{{ homework.description }}</div>
            
            <div class="homework-meta">
              <div class="meta-item">
                <el-icon><Calendar /></el-icon>
                <span>开始时间: {{ formatDateTime(homework.startTime) }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Timer /></el-icon>
                <span>截止时间: {{ formatDateTime(homework.endTime) }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Document /></el-icon>
                <span>总分: {{ homework.totalScore }} 分</span>
              </div>
              
              <!-- 学生作业状态 -->
              <div v-if="!isTeacher && homework.submission" class="meta-item submission-status">
                <el-icon><InfoFilled /></el-icon>
                <span>
                  提交状态: 
                  <el-tag 
                    :type="getSubmissionStatusType(homework.submission.status)" 
                    size="small"
                  >
                    {{ getSubmissionStatusText(homework.submission.status) }}
                  </el-tag>
                </span>
              </div>
              
              <!-- 学生作业分数 -->
              <div v-if="!isTeacher && homework.submission && homework.submission.score !== null" class="meta-item submission-score">
                <el-icon><Star /></el-icon>
                <span>得分: {{ homework.submission.score }} / {{ homework.totalScore }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
    
    <!-- 创建/编辑作业对话框 -->
    <el-dialog
      v-model="homeworkDialogVisible"
      :title="isEditMode ? '编辑作业' : '创建新作业'"
      width="60%"
    >
      <el-form :model="homeworkForm" :rules="homeworkRules" ref="homeworkFormRef" label-width="100px">
        <el-form-item label="作业标题" prop="title">
          <el-input v-model="homeworkForm.title" placeholder="请输入作业标题" />
        </el-form-item>
        
        <el-form-item label="作业描述" prop="description">
          <el-input 
            v-model="homeworkForm.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入作业描述"
          />
        </el-form-item>
        
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="homeworkForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        
        <el-form-item label="截止时间" prop="endTime">
          <el-date-picker
            v-model="homeworkForm.endTime"
            type="datetime"
            placeholder="选择截止时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        
        <el-form-item label="总分" prop="totalScore">
          <el-input-number v-model="homeworkForm.totalScore" :min="1" :max="100" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="homeworkDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitHomeworkForm">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 作业提交列表对话框 -->
    <el-dialog
      v-model="submissionsDialogVisible"
      title="作业提交列表"
      width="80%"
    >
      <div v-if="submissions.length === 0" class="empty-state">
        <el-empty description="暂无学生提交" />
      </div>
      
      <el-table v-else :data="submissions" style="width: 100%">
        <el-table-column prop="studentName" label="学生姓名" />
        <el-table-column prop="submitTime" label="提交时间">
          <template #default="scope">
            {{ formatDateTime(scope.row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getSubmissionStatusType(scope.row.status)">
              {{ getSubmissionStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="得分">
          <template #default="scope">
            {{ scope.row.score !== null ? `${scope.row.score} / ${currentHomework.totalScore}` : '未评分' }}
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="viewSubmissionDetail(scope.row)"
            >
              查看详情
            </el-button>
            <el-button 
              v-if="scope.row.status === 'SUBMITTED'"
              type="success" 
              size="small" 
              @click="gradeSubmission(scope.row)"
            >
              评分
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
    
    <!-- 评分对话框 -->
    <el-dialog
      v-model="gradeDialogVisible"
      title="作业评分"
      width="50%"
    >
      <div v-if="currentSubmission" class="grade-form">
        <div class="submission-info">
          <h3>{{ currentSubmission.studentName }} 的作业提交</h3>
          <p>提交时间: {{ formatDateTime(currentSubmission.submitTime) }}</p>
          
          <div v-if="currentSubmission.content" class="submission-content">
            <div class="content-label">作业内容:</div>
            <div class="content-value">{{ currentSubmission.content }}</div>
          </div>
          
          <div v-if="currentSubmission.attachments && currentSubmission.attachments.length > 0" class="submission-attachments">
            <div class="attachments-label">附件:</div>
            <div class="attachments-list">
              <div 
                v-for="attachment in currentSubmission.attachments" 
                :key="attachment.id"
                class="attachment-item"
              >
                <el-link :href="attachment.url" target="_blank" type="primary">
                  {{ attachment.fileName }}
                </el-link>
              </div>
            </div>
          </div>
        </div>
        
        <el-form :model="gradeForm" :rules="gradeRules" ref="gradeFormRef" label-width="100px">
          <el-form-item label="分数" prop="score">
            <el-input-number 
              v-model="gradeForm.score" 
              :min="0" 
              :max="currentHomework.totalScore" 
              :step="1"
            />
            <span class="score-total">/ {{ currentHomework.totalScore }}</span>
          </el-form-item>
          
          <el-form-item label="评语" prop="comment">
            <el-input 
              v-model="gradeForm.comment" 
              type="textarea" 
              :rows="4" 
              placeholder="请输入评语"
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="gradeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitGrade">提交评分</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Calendar, Timer, Document, InfoFilled, Star } from '@element-plus/icons-vue';
import { homeworkApi } from '../../api/homework';
import { submissionApi } from '../../api/submission';
import { courseApi } from '../../api/course';
import { useAuthStore } from '../../store/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 课程ID
const courseId = computed(() => route.params.courseId);

// 用户角色
const isTeacher = computed(() => authStore.hasPermission('teacher'));

// 数据加载状态
const loading = ref(true);

// 课程信息
const course = ref({});

// 作业列表
const homeworkList = ref([]);

// 当前操作的作业
const currentHomework = ref(null);

// 作业对话框
const homeworkDialogVisible = ref(false);
const isEditMode = ref(false);
const homeworkFormRef = ref(null);

// 作业表单
const homeworkForm = ref({
  title: '',
  description: '',
  startTime: '',
  endTime: '',
  totalScore: 100
});

// 作业表单验证规则
const homeworkRules = {
  title: [
    { required: true, message: '请输入作业标题', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入作业描述', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择截止时间', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value && homeworkForm.value.startTime && new Date(value) <= new Date(homeworkForm.value.startTime)) {
          callback(new Error('截止时间必须晚于开始时间'));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ],
  totalScore: [
    { required: true, message: '请设置总分', trigger: 'change' },
    { type: 'number', min: 1, message: '总分必须大于0', trigger: 'change' }
  ]
};

// 作业提交列表对话框
const submissionsDialogVisible = ref(false);
const submissions = ref([]);

// 评分对话框
const gradeDialogVisible = ref(false);
const currentSubmission = ref(null);
const gradeFormRef = ref(null);

// 评分表单
const gradeForm = ref({
  score: 0,
  comment: ''
});

// 评分表单验证规则
const gradeRules = {
  score: [
    { required: true, message: '请输入分数', trigger: 'change' },
    { type: 'number', min: 0, message: '分数不能小于0', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value > currentHomework.value.totalScore) {
          callback(new Error(`分数不能超过总分 ${currentHomework.value.totalScore}`));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ]
};

// 获取课程信息
const fetchCourseInfo = async () => {
  try {
    const res = await courseApi.getCourseDetail(courseId.value);
    course.value = res.data || {};
  } catch (error) {
    console.error('获取课程信息失败:', error);
    ElMessage.error('获取课程信息失败');
  }
};

// 获取作业列表
const fetchHomeworkList = async () => {
  loading.value = true;
  try {
    const res = await homeworkApi.getCourseHomeworkList(courseId.value);
    homeworkList.value = res.data || [];
    
    // 如果是学生，获取提交状态
    if (!isTeacher.value) {
      await fetchMySubmissions();
    }
  } catch (error) {
    console.error('获取作业列表失败:', error);
    ElMessage.error('获取作业列表失败');
  } finally {
    loading.value = false;
  }
};

// 获取学生提交状态
const fetchMySubmissions = async () => {
  try {
    const res = await submissionApi.getMySubmissions();
    const mySubmissions = res.data || [];
    
    // 将提交状态关联到作业
    homeworkList.value.forEach(homework => {
      const submission = mySubmissions.find(sub => sub.homeworkId === homework.id);
      if (submission) {
        homework.submission = submission;
      }
    });
  } catch (error) {
    console.error('获取提交状态失败:', error);
  }
};

// 获取作业提交列表
const fetchSubmissions = async (homeworkId) => {
  try {
    const res = await submissionApi.getHomeworkSubmissions(homeworkId);
    submissions.value = res.data || [];
  } catch (error) {
    console.error('获取作业提交列表失败:', error);
    ElMessage.error('获取作业提交列表失败');
  }
};

// 显示创建作业对话框
const showCreateHomeworkDialog = () => {
  isEditMode.value = false;
  homeworkForm.value = {
    title: '',
    description: '',
    startTime: '',
    endTime: '',
    totalScore: 100
  };
  homeworkDialogVisible.value = true;
};

// 编辑作业
const editHomework = (homework) => {
  isEditMode.value = true;
  currentHomework.value = homework;
  
  homeworkForm.value = {
    title: homework.title,
    description: homework.description,
    startTime: homework.startTime,
    endTime: homework.endTime,
    totalScore: homework.totalScore
  };
  
  homeworkDialogVisible.value = true;
};

// 提交作业表单
const submitHomeworkForm = async () => {
  if (!homeworkFormRef.value) return;
  
  await homeworkFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const homeworkData = {
          ...homeworkForm.value,
          courseId: courseId.value
        };
        
        if (isEditMode.value && currentHomework.value) {
          // 更新作业
          await homeworkApi.updateHomework(currentHomework.value.id, homeworkData);
          ElMessage.success('作业更新成功');
        } else {
          // 创建作业
          await homeworkApi.createHomework(homeworkData);
          ElMessage.success('作业创建成功');
        }
        
        homeworkDialogVisible.value = false;
        fetchHomeworkList(); // 刷新作业列表
      } catch (error) {
        console.error('保存作业失败:', error);
        ElMessage.error('保存作业失败');
      }
    }
  });
};

// 删除作业
const deleteHomework = async (homework) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除作业「${homework.title}」吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    
    await homeworkApi.deleteHomework(homework.id);
    ElMessage.success('作业删除成功');
    fetchHomeworkList(); // 刷新作业列表
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除作业失败:', error);
      ElMessage.error('删除作业失败');
    }
  }
};

// 查看作业提交列表
const viewSubmissions = async (homework) => {
  currentHomework.value = homework;
  submissionsDialogVisible.value = true;
  await fetchSubmissions(homework.id);
};

// 查看提交详情
const viewSubmissionDetail = (submission) => {
  // 跳转到提交详情页面
  router.push(`/homework/${currentHomework.value.id}/submission/${submission.id}`);
};

// 评分
const gradeSubmission = (submission) => {
  currentSubmission.value = submission;
  gradeForm.value = {
    score: submission.score || 0,
    comment: submission.comment || ''
  };
  gradeDialogVisible.value = true;
};

// 提交评分
const submitGrade = async () => {
  if (!gradeFormRef.value) return;
  
  await gradeFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await submissionApi.gradeSubmission(currentSubmission.value.id, gradeForm.value);
        ElMessage.success('评分成功');
        gradeDialogVisible.value = false;
        
        // 刷新提交列表
        await fetchSubmissions(currentHomework.value.id);
      } catch (error) {
        console.error('评分失败:', error);
        ElMessage.error('评分失败');
      }
    }
  });
};

// 查看作业详情（学生）
const viewHomeworkDetail = (homework) => {
  router.push(`/homework/${homework.id}`);
};

// 获取作业状态类型
const getHomeworkStatusType = (status) => {
  const statusMap = {
    'DRAFT': 'info',
    'PUBLISHED': 'success',
    'EXPIRED': 'warning'
  };
  return statusMap[status] || 'info';
};

// 获取作业状态文本
const getHomeworkStatusText = (status) => {
  const statusMap = {
    'DRAFT': '草稿',
    'PUBLISHED': '已发布',
    'EXPIRED': '已截止'
  };
  return statusMap[status] || '未知状态';
};

// 获取提交状态类型
const getSubmissionStatusType = (status) => {
  const statusMap = {
    'DRAFT': 'info',
    'SUBMITTED': 'warning',
    'GRADED': 'success'
  };
  return statusMap[status] || 'info';
};

// 获取提交状态文本
const getSubmissionStatusText = (status) => {
  const statusMap = {
    'DRAFT': '草稿',
    'SUBMITTED': '已提交',
    'GRADED': '已评分'
  };
  return statusMap[status] || '未知状态';
};

// 获取学生操作按钮文本
const getStudentActionText = (homework) => {
  if (!homework.submission) {
    return '提交作业';
  }
  
  switch (homework.submission.status) {
    case 'DRAFT':
      return '继续编辑';
    case 'SUBMITTED':
      return '查看提交';
    case 'GRADED':
      return '查看评分';
    default:
      return '查看详情';
  }
};

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return '未设置';
  const date = new Date(dateString);
  return date.toLocaleString();
};

onMounted(async () => {
  await fetchCourseInfo();
  await fetchHomeworkList();
});
</script>

<style scoped>
.homework-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  color: #333;
  margin: 0 0 10px 0;
}

.page-header p {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.teacher-actions, .student-actions {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
}

.homework-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.loading-container {
  padding: 20px;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.homework-card {
  margin-bottom: 20px;
}

.homework-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.homework-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.homework-title h2 {
  margin: 0;
  font-size: 18px;
}

.homework-content {
  margin-bottom: 15px;
}

.homework-description {
  margin-bottom: 15px;
  color: #666;
  white-space: pre-wrap;
}

.homework-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  font-size: 14px;
  color: #666;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.submission-status, .submission-score {
  font-weight: bold;
}

.dialog-footer {
  text-align: right;
  margin-top: 20px;
}

.submission-info {
  margin-bottom: 20px;
}

.submission-info h3 {
  margin: 0 0 10px 0;
}

.submission-content, .submission-attachments {
  margin-top: 15px;
}

.content-label, .attachments-label {
  font-weight: bold;
  margin-bottom: 5px;
}

.content-value {
  white-space: pre-wrap;
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
}

.attachments-list {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.score-total {
  margin-left: 10px;
  color: #666;
}
</style>