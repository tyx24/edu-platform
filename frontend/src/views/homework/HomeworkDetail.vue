<template>
  <div class="homework-detail-container">
    <div class="page-header">
      <h1>{{ homework.title }}</h1>
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
        <div class="meta-item">
          <el-icon><User /></el-icon>
          <span>教师: {{ homework.teacherName }}</span>
        </div>
        <div class="meta-item">
          <el-icon><InfoFilled /></el-icon>
          <span>状态: 
            <el-tag :type="getHomeworkStatusType(homework.status)">
              {{ getHomeworkStatusText(homework.status) }}
            </el-tag>
          </span>
        </div>
      </div>
    </div>
    
    <div class="content-container">
      <!-- 作业描述 -->
      <el-card class="description-card">
        <template #header>
          <div class="card-header">
            <span>作业描述</span>
          </div>
        </template>
        <div class="homework-description">
          {{ homework.description }}
        </div>
      </el-card>
      
      <!-- 提交区域 -->
      <el-card class="submission-card">
        <template #header>
          <div class="card-header">
            <span>作业提交</span>
            <div v-if="submission">
              <el-tag :type="getSubmissionStatusType(submission.status)">
                {{ getSubmissionStatusText(submission.status) }}
              </el-tag>
              <span v-if="submission.status === 'GRADED'" class="submission-score">
                得分: {{ submission.score }} / {{ homework.totalScore }}
              </span>
            </div>
          </div>
        </template>
        
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>
        
        <!-- 未提交状态 -->
        <div v-else-if="!submission && isBeforeDeadline" class="submission-form">
          <el-form :model="submissionForm" :rules="submissionRules" ref="submissionFormRef" label-width="100px">
            <el-form-item label="作业内容" prop="content">
              <el-input 
                v-model="submissionForm.content" 
                type="textarea" 
                :rows="6" 
                placeholder="请输入作业内容"
              />
            </el-form-item>
            
            <el-form-item label="附件">
              <el-upload
                :action="uploadUrl"
                :headers="uploadHeaders"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                :on-remove="handleRemoveFile"
                :file-list="fileList"
                multiple
              >
                <el-button type="primary">
                  <el-icon><Upload /></el-icon> 上传文件
                </el-button>
                <template #tip>
                  <div class="el-upload__tip">
                    支持各种格式文件，单个文件不超过10MB
                  </div>
                </template>
              </el-upload>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="submitHomework">提交作业</el-button>
              <el-button @click="saveDraft">保存草稿</el-button>
              <el-button @click="$router.push(`/course/${homework.courseId}/homework`)">返回列表</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 已过截止日期未提交 -->
        <div v-else-if="!submission && !isBeforeDeadline" class="expired-notice">
          <el-empty description="作业已截止，无法提交" />
          <el-button @click="$router.push(`/course/${homework.courseId}/homework`)">返回列表</el-button>
        </div>
        
        <!-- 已提交状态 -->
        <div v-else-if="submission" class="submission-detail">
          <!-- 提交信息 -->
          <div class="submission-info">
            <div class="info-item">
              <span class="info-label">提交时间:</span>
              <span class="info-value">{{ formatDateTime(submission.submitTime) }}</span>
            </div>
            <div v-if="submission.status === 'GRADED'" class="info-item">
              <span class="info-label">评分时间:</span>
              <span class="info-value">{{ formatDateTime(submission.gradeTime) }}</span>
            </div>
          </div>
          
          <!-- 提交内容 -->
          <div v-if="submission.content" class="submission-content">
            <div class="content-label">作业内容:</div>
            <div class="content-value">{{ submission.content }}</div>
          </div>
          
          <!-- 附件列表 -->
          <div v-if="submission.attachments && submission.attachments.length > 0" class="submission-attachments">
            <div class="attachments-label">附件:</div>
            <div class="attachments-list">
              <div 
                v-for="attachment in submission.attachments" 
                :key="attachment.id"
                class="attachment-item"
              >
                <el-link :href="attachment.url" target="_blank" type="primary">
                  <el-icon><Document /></el-icon>
                  {{ attachment.fileName }}
                </el-link>
              </div>
            </div>
          </div>
          
          <!-- 教师评语 -->
          <div v-if="submission.status === 'GRADED' && submission.comment" class="teacher-comment">
            <div class="comment-label">教师评语:</div>
            <div class="comment-value">{{ submission.comment }}</div>
          </div>
          
          <!-- 操作按钮 -->
          <div class="submission-actions">
            <el-button 
              v-if="submission.status !== 'GRADED' && isBeforeDeadline" 
              type="primary" 
              @click="editSubmission"
            >
              编辑提交
            </el-button>
            <el-button @click="$router.push(`/course/${homework.courseId}/homework`)">返回列表</el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Calendar, Timer, Document, User, InfoFilled, Upload } from '@element-plus/icons-vue';
import { homeworkApi } from '../../api/homework';
import { submissionApi } from '../../api/submission';
import { useAuthStore } from '../../store/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 作业ID
const homeworkId = computed(() => route.params.homeworkId);

// 数据加载状态
const loading = ref(true);

// 作业信息
const homework = ref({});

// 提交信息
const submission = ref(null);

// 是否在截止日期前
const isBeforeDeadline = computed(() => {
  if (!homework.value.endTime) return true;
  return new Date() < new Date(homework.value.endTime);
});

// 提交表单
const submissionFormRef = ref(null);
const submissionForm = ref({
  content: '',
  attachments: []
});

// 文件列表
const fileList = ref([]);

// 上传URL
const uploadUrl = computed(() => {
  return '/edu/submission/upload';
});

// 上传请求头
const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${localStorage.getItem('token')}`
  };
});

// 表单验证规则
const submissionRules = {
  content: [
    { required: true, message: '请输入作业内容', trigger: 'blur' }
  ]
};

// 获取作业信息
const fetchHomeworkInfo = async () => {
  loading.value = true;
  try {
    const res = await homeworkApi.getHomeworkById(homeworkId.value);
    homework.value = res.data || {};
  } catch (error) {
    console.error('获取作业信息失败:', error);
    ElMessage.error('获取作业信息失败');
  } finally {
    loading.value = false;
  }
};

// 获取提交信息
const fetchSubmission = async () => {
  loading.value = true;
  try {
    const res = await submissionApi.getMySubmissionByHomeworkId(homeworkId.value);
    if (res.data) {
      submission.value = res.data;
      
      // 如果是草稿状态，填充表单
      if (submission.value.status === 'DRAFT') {
        submissionForm.value.content = submission.value.content || '';
        
        // 填充附件列表
        if (submission.value.attachments && submission.value.attachments.length > 0) {
          fileList.value = submission.value.attachments.map(attachment => ({
            name: attachment.fileName,
            url: attachment.url,
            id: attachment.id
          }));
          submissionForm.value.attachments = submission.value.attachments.map(attachment => attachment.id);
        }
      }
    }
  } catch (error) {
    console.error('获取提交信息失败:', error);
    // 如果是404，说明没有提交记录，不显示错误
    if (error.response && error.response.status !== 404) {
      ElMessage.error('获取提交信息失败');
    }
  } finally {
    loading.value = false;
  }
};

// 处理文件上传成功
const handleUploadSuccess = (response, file) => {
  if (response.code === 0) {
    ElMessage.success('文件上传成功');
    // 添加附件ID到表单
    submissionForm.value.attachments.push(response.data.id);
  } else {
    ElMessage.error(response.message || '文件上传失败');
  }
};

// 处理文件上传失败
const handleUploadError = () => {
  ElMessage.error('文件上传失败');
};

// 处理移除文件
const handleRemoveFile = (file) => {
  // 从附件列表中移除
  const index = submissionForm.value.attachments.findIndex(id => {
    const fileObj = fileList.value.find(f => f.id === id);
    return fileObj && fileObj.name === file.name;
  });
  
  if (index !== -1) {
    submissionForm.value.attachments.splice(index, 1);
  }
};

// 提交作业
const submitHomework = async () => {
  if (!submissionFormRef.value) return;
  
  await submissionFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const submitData = {
          homeworkId: homeworkId.value,
          content: submissionForm.value.content,
          attachmentIds: submissionForm.value.attachments,
          status: 'SUBMITTED'
        };
        
        // 如果已有草稿，则更新
        if (submission.value && submission.value.status === 'DRAFT') {
          await submissionApi.updateSubmission(submission.value.id, submitData);
        } else {
          await submissionApi.createSubmission(submitData);
        }
        
        ElMessage.success('作业提交成功');
        // 刷新提交信息
        await fetchSubmission();
      } catch (error) {
        console.error('提交作业失败:', error);
        ElMessage.error('提交作业失败');
      }
    }
  });
};

// 保存草稿
const saveDraft = async () => {
  try {
    const draftData = {
      homeworkId: homeworkId.value,
      content: submissionForm.value.content,
      attachmentIds: submissionForm.value.attachments,
      status: 'DRAFT'
    };
    
    // 如果已有草稿，则更新
    if (submission.value && submission.value.status === 'DRAFT') {
      await submissionApi.updateSubmission(submission.value.id, draftData);
    } else {
      await submissionApi.createSubmission(draftData);
    }
    
    ElMessage.success('草稿保存成功');
    // 刷新提交信息
    await fetchSubmission();
  } catch (error) {
    console.error('保存草稿失败:', error);
    ElMessage.error('保存草稿失败');
  }
};

// 编辑提交
const editSubmission = () => {
  if (!submission.value) return;
  
  // 填充表单
  submissionForm.value.content = submission.value.content || '';
  
  // 填充附件列表
  if (submission.value.attachments && submission.value.attachments.length > 0) {
    fileList.value = submission.value.attachments.map(attachment => ({
      name: attachment.fileName,
      url: attachment.url,
      id: attachment.id
    }));
    submissionForm.value.attachments = submission.value.attachments.map(attachment => attachment.id);
  } else {
    fileList.value = [];
    submissionForm.value.attachments = [];
  }
  
  // 清除提交信息，显示编辑表单
  submission.value = null;
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

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return '未设置';
  const date = new Date(dateString);
  return date.toLocaleString();
};

onMounted(async () => {
  await fetchHomeworkInfo();
  await fetchSubmission();
});
</script>

<style scoped>
.homework-detail-container {
  max-width: 1000px;
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
  margin: 0 0 15px 0;
}

.homework-meta {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 15px;
  font-size: 14px;
  color: #666;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.content-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.description-card, .submission-card {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.homework-description {
  white-space: pre-wrap;
  line-height: 1.6;
}

.loading-container {
  padding: 20px;
}

.submission-form {
  padding: 20px 0;
}

.expired-notice {
  text-align: center;
  padding: 40px 0;
}

.submission-detail {
  padding: 20px 0;
}

.submission-info {
  margin-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.info-label {
  font-weight: bold;
  color: #666;
}

.submission-content, .submission-attachments, .teacher-comment {
  margin-bottom: 20px;
}

.content-label, .attachments-label, .comment-label {
  font-weight: bold;
  margin-bottom: 10px;
  color: #333;
}

.content-value {
  white-space: pre-wrap;
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  line-height: 1.6;
}

.attachments-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.attachment-item {
  display: flex;
  align-items: center;
}

.comment-value {
  background-color: #f0f9eb;
  padding: 15px;
  border-radius: 4px;
  border-left: 4px solid #67c23a;
  white-space: pre-wrap;
  line-height: 1.6;
}

.submission-actions {
  margin-top: 30px;
  display: flex;
  gap: 10px;
}

.submission-score {
  margin-left: 10px;
  font-weight: bold;
  color: #f56c6c;
}
</style>