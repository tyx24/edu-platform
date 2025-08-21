<template>
  <div class="submission-list-container">
    <div class="page-header">
      <h2>作业提交列表</h2>
      <p>{{ homework.title || '加载中...' }}</p>
    </div>

    <el-card class="info-card">
      <div class="homework-info">
        <div class="info-item">
          <span class="label">开始时间:</span>
          <span class="value">{{ formatDateTime(homework.startTime) }}</span>
        </div>
        <div class="info-item">
          <span class="label">截止时间:</span>
          <span class="value">{{ formatDateTime(homework.endTime) }}</span>
        </div>
        <div class="info-item">
          <span class="label">总分:</span>
          <span class="value">{{ homework.totalScore }}</span>
        </div>
        <div class="info-item">
          <span class="label">允许迟交:</span>
          <span class="value">{{ homework.allowLateSubmission ? '是' : '否' }}</span>
        </div>
      </div>
    </el-card>

    <div class="action-bar">
      <div class="search-form">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索学生姓名或学号"
          clearable
          @clear="fetchSubmissions"
          style="width: 250px"
        >
          <template #append>
            <el-button @click="fetchSubmissions">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
        
        <el-select 
          v-model="searchForm.status" 
          placeholder="提交状态" 
          clearable
          @change="fetchSubmissions"
          style="width: 150px; margin-left: 10px"
        >
          <el-option label="全部" value="" />
          <el-option label="未提交" value="NOT_SUBMITTED" />
          <el-option label="已提交" value="SUBMITTED" />
          <el-option label="已批改" value="GRADED" />
          <el-option label="迟交" value="LATE" />
        </el-select>
      </div>
      
      <div class="action-buttons">
        <el-button type="primary" @click="exportSubmissions">
          <el-icon><Download /></el-icon> 导出记录
        </el-button>
        <el-button @click="goBack">
          <el-icon><Back /></el-icon> 返回作业
        </el-button>
      </div>
    </div>

    <div class="submission-stats">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">总人数</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.submitted }}</div>
            <div class="stat-label">已提交</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.late }}</div>
            <div class="stat-label">迟交</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.avgScore }}</div>
            <div class="stat-label">平均分</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-table
      v-loading="loading"
      :data="submissions"
      style="width: 100%; margin-top: 20px"
      border
    >
      <el-table-column prop="studentId" label="学号" width="120" />
      <el-table-column prop="studentName" label="姓名" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.submitTime) || '未提交' }}
        </template>
      </el-table-column>
      <el-table-column label="是否迟交" width="100">
        <template #default="scope">
          <el-tag 
            v-if="scope.row.status !== 'NOT_SUBMITTED'"
            :type="scope.row.isLate ? 'warning' : 'success'"
          >
            {{ scope.row.isLate ? '是' : '否' }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="附件" width="100">
        <template #default="scope">
          <el-button 
            v-if="scope.row.attachmentUrl"
            type="primary" 
            link 
            @click="downloadAttachment(scope.row)"
          >
            <el-icon><Download /></el-icon> 下载
          </el-button>
          <span v-else>无附件</span>
        </template>
      </el-table-column>
      <el-table-column label="得分" width="100">
        <template #default="scope">
          <span 
            v-if="scope.row.status === 'GRADED'"
            :class="{
              'score-high': scope.row.score >= homework.totalScore * 0.8,
              'score-medium': scope.row.score >= homework.totalScore * 0.6 && scope.row.score < homework.totalScore * 0.8,
              'score-low': scope.row.score < homework.totalScore * 0.6
            }"
          >
            {{ scope.row.score }}
          </span>
          <span v-else>未批改</span>
        </template>
      </el-table-column>
      <el-table-column label="评语" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.comment || '无评语' }}
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button 
            type="primary" 
            link 
            @click="viewSubmission(scope.row)"
            :disabled="scope.row.status === 'NOT_SUBMITTED'"
          >
            查看
          </el-button>
          <el-button 
            v-if="scope.row.status === 'SUBMITTED' || scope.row.status === 'LATE'"
            type="success" 
            link 
            @click="gradeSubmission(scope.row)"
          >
            批改
          </el-button>
          <el-button 
            v-if="scope.row.status === 'GRADED'"
            type="warning" 
            link 
            @click="regradeSubmission(scope.row)"
          >
            重新批改
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        :total="page.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 批改对话框 -->
    <el-dialog
      v-model="gradeDialogVisible"
      title="批改作业"
      width="60%"
      destroy-on-close
    >
      <div v-if="currentSubmission" class="grade-dialog-content">
        <div class="student-info">
          <p><strong>学生:</strong> {{ currentSubmission.studentName }} ({{ currentSubmission.studentId }})</p>
          <p><strong>提交时间:</strong> {{ formatDateTime(currentSubmission.submitTime) }}</p>
          <p v-if="currentSubmission.isLate" class="late-warning">
            <el-icon><Warning /></el-icon> 该作业为迟交
          </p>
        </div>

        <el-divider content-position="center">作业内容</el-divider>

        <div class="submission-content">
          <div v-if="currentSubmission.content" class="content-text">
            <p><strong>文字内容:</strong></p>
            <div class="content-box">{{ currentSubmission.content }}</div>
          </div>

          <div v-if="currentSubmission.attachmentUrl" class="attachment">
            <p><strong>附件:</strong></p>
            <el-button type="primary" @click="downloadAttachment(currentSubmission)">
              <el-icon><Download /></el-icon> 下载附件
            </el-button>
          </div>
        </div>

        <el-divider content-position="center">评分</el-divider>

        <el-form ref="gradeFormRef" :model="gradeForm" label-width="100px">
          <el-form-item label="分数" prop="score">
            <el-input-number 
              v-model="gradeForm.score" 
              :min="0" 
              :max="homework.totalScore" 
              :step="1"
            />
            <span class="score-hint">满分 {{ homework.totalScore }} 分</span>
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

    <!-- 查看提交对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="查看作业"
      width="60%"
    >
      <div v-if="currentSubmission" class="view-dialog-content">
        <div class="student-info">
          <p><strong>学生:</strong> {{ currentSubmission.studentName }} ({{ currentSubmission.studentId }})</p>
          <p><strong>提交时间:</strong> {{ formatDateTime(currentSubmission.submitTime) }}</p>
          <p v-if="currentSubmission.isLate" class="late-warning">
            <el-icon><Warning /></el-icon> 该作业为迟交
          </p>
        </div>

        <el-divider content-position="center">作业内容</el-divider>

        <div class="submission-content">
          <div v-if="currentSubmission.content" class="content-text">
            <p><strong>文字内容:</strong></p>
            <div class="content-box">{{ currentSubmission.content }}</div>
          </div>

          <div v-if="currentSubmission.attachmentUrl" class="attachment">
            <p><strong>附件:</strong></p>
            <el-button type="primary" @click="downloadAttachment(currentSubmission)">
              <el-icon><Download /></el-icon> 下载附件
            </el-button>
          </div>
        </div>

        <div v-if="currentSubmission.status === 'GRADED'" class="grade-info">
          <el-divider content-position="center">评分信息</el-divider>
          
          <div class="grade-details">
            <p>
              <strong>得分:</strong> 
              <span 
                :class="{
                  'score-high': currentSubmission.score >= homework.totalScore * 0.8,
                  'score-medium': currentSubmission.score >= homework.totalScore * 0.6 && currentSubmission.score < homework.totalScore * 0.8,
                  'score-low': currentSubmission.score < homework.totalScore * 0.6
                }"
              >
                {{ currentSubmission.score }}
              </span> / {{ homework.totalScore }}
            </p>
            <p><strong>评语:</strong> {{ currentSubmission.comment || '无评语' }}</p>
            <p><strong>批改时间:</strong> {{ formatDateTime(currentSubmission.gradedTime) }}</p>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Search, Download, Back, Warning } from '@element-plus/icons-vue';
import { homeworkApi } from '../../api/homework';
import { submissionApi } from '../../api/submission';

const route = useRoute();
const router = useRouter();

// 作业ID
const homeworkId = computed(() => route.params.homeworkId);

// 作业信息
const homework = ref({});

// 提交列表
const submissions = ref([]);

// 加载状态
const loading = ref(false);

// 搜索表单
const searchForm = ref({
  keyword: '',
  status: ''
});

// 分页信息
const page = ref({
  current: 1,
  size: 10,
  total: 0
});

// 统计信息
const stats = ref({
  total: 0,
  submitted: 0,
  late: 0,
  avgScore: 0
});

// 批改对话框
const gradeDialogVisible = ref(false);
const gradeFormRef = ref(null);
const gradeForm = ref({
  score: 0,
  comment: ''
});

// 查看对话框
const viewDialogVisible = ref(false);

// 当前选中的提交
const currentSubmission = ref(null);

// 在组件挂载时获取数据
onMounted(async () => {
  await fetchHomeworkInfo();
  await fetchSubmissions();
  await fetchStats();
});

// 获取作业信息
const fetchHomeworkInfo = async () => {
  try {
    const res = await homeworkApi.getHomeworkById(homeworkId.value);
    homework.value = res.data || {};
  } catch (error) {
    console.error('获取作业信息失败:', error);
    ElMessage.error('获取作业信息失败');
  }
};

// 获取提交列表
const fetchSubmissions = async () => {
  loading.value = true;
  try {
    const params = {
      ...searchForm.value,
      page: page.value.current,
      size: page.value.size
    };
    
    const res = await submissionApi.getSubmissionList(homeworkId.value, params);
    submissions.value = res.data.records || [];
    page.value.total = res.data.total || 0;
  } catch (error) {
    console.error('获取提交列表失败:', error);
    ElMessage.error('获取提交列表失败');
  } finally {
    loading.value = false;
  }
};

// 获取统计信息
const fetchStats = async () => {
  try {
    const res = await submissionApi.getSubmissionStats(homeworkId.value);
    stats.value = res.data || {
      total: 0,
      submitted: 0,
      late: 0,
      avgScore: 0
    };
  } catch (error) {
    console.error('获取统计信息失败:', error);
    ElMessage.error('获取统计信息失败');
  }
};

// 处理分页大小变更
const handleSizeChange = (size) => {
  page.value.size = size;
  fetchSubmissions();
};

// 处理当前页变更
const handleCurrentChange = (current) => {
  page.value.current = current;
  fetchSubmissions();
};

// 查看提交
const viewSubmission = (submission) => {
  currentSubmission.value = submission;
  viewDialogVisible.value = true;
};

// 批改提交
const gradeSubmission = (submission) => {
  currentSubmission.value = submission;
  gradeForm.value = {
    score: submission.score || 0,
    comment: submission.comment || ''
  };
  gradeDialogVisible.value = true;
};

// 重新批改提交
const regradeSubmission = (submission) => {
  currentSubmission.value = submission;
  gradeForm.value = {
    score: submission.score || 0,
    comment: submission.comment || ''
  };
  gradeDialogVisible.value = true;
};

// 提交评分
const submitGrade = async () => {
  if (!currentSubmission.value) return;
  
  try {
    await submissionApi.gradeSubmission(currentSubmission.value.id, gradeForm.value);
    ElMessage.success('评分提交成功');
    gradeDialogVisible.value = false;
    fetchSubmissions(); // 刷新列表
    fetchStats(); // 刷新统计
  } catch (error) {
    console.error('评分提交失败:', error);
    ElMessage.error('评分提交失败');
  }
};

// 下载附件
const downloadAttachment = (submission) => {
  if (!submission.attachmentUrl) return;
  
  window.open(submission.attachmentUrl, '_blank');
};

// 导出提交记录
const exportSubmissions = async () => {
  try {
    const res = await submissionApi.exportSubmissions(homeworkId.value, searchForm.value);
    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `${homework.value.title}-提交记录.xlsx`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    ElMessage.success('导出成功');
  } catch (error) {
    console.error('导出提交记录失败:', error);
    ElMessage.error('导出提交记录失败');
  }
};

// 返回作业页面
const goBack = () => {
  router.push(`/homework/${homeworkId.value}`);
};

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    'NOT_SUBMITTED': 'info',
    'SUBMITTED': 'primary',
    'LATE': 'warning',
    'GRADED': 'success'
  };
  return statusMap[status] || 'info';
};

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'NOT_SUBMITTED': '未提交',
    'SUBMITTED': '已提交',
    'LATE': '迟交',
    'GRADED': '已批改'
  };
  return statusMap[status] || '未知';
};

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};
</script>

<style scoped>
.submission-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  text-align: center;
}

.page-header h2 {
  font-size: 24px;
  margin-bottom: 8px;
}

.page-header p {
  color: #606266;
  font-size: 16px;
}

.info-card {
  margin-bottom: 20px;
}

.homework-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.info-item {
  flex: 1;
  min-width: 200px;
}

.info-item .label {
  font-weight: bold;
  margin-right: 8px;
  color: #606266;
}

.action-bar {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  display: flex;
  align-items: center;
}

.submission-stats {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  padding: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.score-high {
  color: #67c23a;
  font-weight: bold;
}

.score-medium {
  color: #e6a23c;
  font-weight: bold;
}

.score-low {
  color: #f56c6c;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.grade-dialog-content,
.view-dialog-content {
  padding: 0 20px;
}

.student-info {
  margin-bottom: 20px;
}

.late-warning {
  color: #e6a23c;
  display: flex;
  align-items: center;
  gap: 5px;
}

.content-box {
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 15px;
  margin: 10px 0;
  white-space: pre-wrap;
  max-height: 200px;
  overflow-y: auto;
}

.attachment {
  margin-top: 20px;
}

.score-hint {
  margin-left: 10px;
  color: #909399;
  font-size: 14px;
}

.grade-details {
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 15px;
}
</style>