<template>
  <div class="exam-records-container">
    <div class="page-header">
      <h2>考试记录</h2>
      <p>{{ exam.title || '加载中...' }}</p>
    </div>

    <el-card class="info-card">
      <div class="exam-info">
        <div class="info-item">
          <span class="label">考试时间:</span>
          <span class="value">{{ formatDateTime(exam.startTime) }} - {{ formatDateTime(exam.endTime) }}</span>
        </div>
        <div class="info-item">
          <span class="label">考试时长:</span>
          <span class="value">{{ exam.duration }} 分钟</span>
        </div>
        <div class="info-item">
          <span class="label">总分:</span>
          <span class="value">{{ exam.totalScore }}</span>
        </div>
        <div class="info-item">
          <span class="label">及格分数:</span>
          <span class="value">{{ exam.passingScore }}</span>
        </div>
      </div>
    </el-card>

    <div class="action-bar">
      <div class="search-form">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索学生姓名或学号"
          clearable
          @clear="fetchRecords"
          style="width: 250px"
        >
          <template #append>
            <el-button @click="fetchRecords">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
        
        <el-select 
          v-model="searchForm.status" 
          placeholder="考试状态" 
          clearable
          @change="fetchRecords"
          style="width: 150px; margin-left: 10px"
        >
          <el-option label="全部" value="" />
          <el-option label="未开始" value="NOT_STARTED" />
          <el-option label="进行中" value="IN_PROGRESS" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已批改" value="GRADED" />
        </el-select>
        
        <el-select 
          v-model="searchForm.result" 
          placeholder="考试结果" 
          clearable
          @change="fetchRecords"
          style="width: 150px; margin-left: 10px"
        >
          <el-option label="全部" value="" />
          <el-option label="通过" value="PASS" />
          <el-option label="未通过" value="FAIL" />
        </el-select>
      </div>
      
      <div class="action-buttons">
        <el-button type="primary" @click="exportRecords">
          <el-icon><Download /></el-icon> 导出记录
        </el-button>
        <el-button @click="goBack">
          <el-icon><Back /></el-icon> 返回考试
        </el-button>
      </div>
    </div>

    <div class="records-stats">
      <el-row :gutter="20">
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">总人数</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.completed }}</div>
            <div class="stat-label">已完成</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.inProgress }}</div>
            <div class="stat-label">进行中</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.notStarted }}</div>
            <div class="stat-label">未开始</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.passRate }}%</div>
            <div class="stat-label">通过率</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-value">{{ stats.avgScore }}</div>
            <div class="stat-label">平均分</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-table
      v-loading="loading"
      :data="records"
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
      <el-table-column label="开始时间" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.startTime) || '未开始' }}
        </template>
      </el-table-column>
      <el-table-column label="提交时间" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.submitTime) || '未提交' }}
        </template>
      </el-table-column>
      <el-table-column label="用时" width="100">
        <template #default="scope">
          {{ calculateDuration(scope.row.startTime, scope.row.submitTime) }}
        </template>
      </el-table-column>
      <el-table-column label="得分" width="100">
        <template #default="scope">
          <span 
            :class="{
              'score-pass': scope.row.score >= exam.passingScore,
              'score-fail': scope.row.score < exam.passingScore && scope.row.status === 'GRADED'
            }"
          >
            {{ scope.row.status === 'GRADED' ? scope.row.score : '未批改' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="结果" width="100">
        <template #default="scope">
          <el-tag 
            v-if="scope.row.status === 'GRADED'"
            :type="scope.row.score >= exam.passingScore ? 'success' : 'danger'"
          >
            {{ scope.row.score >= exam.passingScore ? '通过' : '未通过' }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button 
            type="primary" 
            link 
            @click="viewRecord(scope.row)"
            :disabled="scope.row.status === 'NOT_STARTED'"
          >
            查看答卷
          </el-button>
          <el-button 
            v-if="scope.row.status === 'COMPLETED'"
            type="success" 
            link 
            @click="gradeRecord(scope.row)"
          >
            批改
          </el-button>
          <el-button 
            v-if="scope.row.status === 'GRADED'"
            type="warning" 
            link 
            @click="regradeRecord(scope.row)"
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Search, Download, Back } from '@element-plus/icons-vue';
import { examApi } from '../../api/exam';
import { examRecordApi } from '../../api/examRecord';

const route = useRoute();
const router = useRouter();

// 考试ID
const examId = computed(() => route.params.examId);

// 考试信息
const exam = ref({});

// 考试记录列表
const records = ref([]);

// 加载状态
const loading = ref(false);

// 搜索表单
const searchForm = ref({
  keyword: '',
  status: '',
  result: ''
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
  completed: 0,
  inProgress: 0,
  notStarted: 0,
  passRate: 0,
  avgScore: 0
});

// 在组件挂载时获取数据
onMounted(async () => {
  await fetchExamInfo();
  await fetchRecords();
  await fetchStats();
});

// 获取考试信息
const fetchExamInfo = async () => {
  try {
    const res = await examApi.getExamById(examId.value);
    exam.value = res.data || {};
  } catch (error) {
    console.error('获取考试信息失败:', error);
    ElMessage.error('获取考试信息失败');
  }
};

// 获取考试记录列表
const fetchRecords = async () => {
  loading.value = true;
  try {
    const params = {
      ...searchForm.value,
      page: page.value.current,
      size: page.value.size
    };
    
    const res = await examRecordApi.getExamRecords(examId.value, params);
    records.value = res.data.records || [];
    page.value.total = res.data.total || 0;
  } catch (error) {
    console.error('获取考试记录失败:', error);
    ElMessage.error('获取考试记录失败');
  } finally {
    loading.value = false;
  }
};

// 获取统计信息
const fetchStats = async () => {
  try {
    const res = await examRecordApi.getExamStats(examId.value);
    stats.value = res.data || {
      total: 0,
      completed: 0,
      inProgress: 0,
      notStarted: 0,
      passRate: 0,
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
  fetchRecords();
};

// 处理当前页变更
const handleCurrentChange = (current) => {
  page.value.current = current;
  fetchRecords();
};

// 查看考试记录
const viewRecord = (record) => {
  router.push(`/exam/${examId.value}/record/${record.id}`);
};

// 批改考试
const gradeRecord = (record) => {
  router.push(`/exam/${examId.value}/record/${record.id}/grade`);
};

// 重新批改考试
const regradeRecord = (record) => {
  router.push(`/exam/${examId.value}/record/${record.id}/grade`);
};

// 导出考试记录
const exportRecords = async () => {
  try {
    const res = await examRecordApi.exportExamRecords(examId.value, searchForm.value);
    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `${exam.value.title}-考试记录.xlsx`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    ElMessage.success('导出成功');
  } catch (error) {
    console.error('导出考试记录失败:', error);
    ElMessage.error('导出考试记录失败');
  }
};

// 返回考试页面
const goBack = () => {
  router.push(`/exam/${examId.value}`);
};

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    'NOT_STARTED': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'GRADED': 'success'
  };
  return statusMap[status] || 'info';
};

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'NOT_STARTED': '未开始',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'GRADED': '已批改'
  };
  return statusMap[status] || '未知';
};

// 计算考试用时
const calculateDuration = (startTime, submitTime) => {
  if (!startTime || !submitTime) return '-';
  
  const start = new Date(startTime).getTime();
  const end = new Date(submitTime).getTime();
  const duration = Math.floor((end - start) / (1000 * 60)); // 分钟
  
  if (duration < 60) {
    return `${duration}分钟`;
  } else {
    const hours = Math.floor(duration / 60);
    const minutes = duration % 60;
    return `${hours}小时${minutes}分钟`;
  }
};

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};
</script>

<style scoped>
.exam-records-container {
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

.exam-info {
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

.records-stats {
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

.score-pass {
  color: #67c23a;
  font-weight: bold;
}

.score-fail {
  color: #f56c6c;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>