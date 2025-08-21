<template>
  <div class="exam-list-container">
    <div class="page-header">
      <h1>{{ courseTitle }} - 考试列表</h1>
      <p>本课程的所有考试</p>
    </div>
    
    <!-- 教师创建考试按钮 -->
    <div class="action-bar" v-if="isTeacher">
      <el-button type="primary" @click="showCreateExamDialog">
        <el-icon><Plus /></el-icon> 创建新考试
      </el-button>
    </div>
    
    <!-- 考试列表 -->
    <div class="exams-section">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>
      
      <div v-else-if="exams.length === 0" class="empty-state">
        <el-empty description="暂无考试" />
        <el-button v-if="isTeacher" type="primary" @click="showCreateExamDialog">
          创建考试
        </el-button>
        <el-button v-else type="primary" @click="$router.push(`/course/${courseId}`)">
          返回课程
        </el-button>
      </div>
      
      <el-card v-else shadow="hover" class="exam-list-card">
        <el-table :data="exams" style="width: 100%">
          <el-table-column prop="title" label="考试名称" min-width="200">
            <template #default="scope">
              <div class="exam-title-cell">
                <el-icon><Document /></el-icon>
                <span>{{ scope.row.title }}</span>
              </div>
            </template>
          </el-table-column>
          
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
          
          <el-table-column prop="duration" label="考试时长" width="120">
            <template #default="scope">
              {{ scope.row.duration }} 分钟
            </template>
          </el-table-column>
          
          <el-table-column prop="totalScore" label="总分" width="80">
            <template #default="scope">
              {{ scope.row.totalScore }} 分
            </template>
          </el-table-column>
          
          <el-table-column label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getExamStatusType(scope.row)">
                {{ getExamStatusText(scope.row) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <!-- 学生操作 -->
              <template v-if="isStudent">
                <el-button 
                  v-if="canTakeExam(scope.row)" 
                  type="primary" 
                  size="small" 
                  @click="takeExam(scope.row)"
                >
                  参加考试
                </el-button>
                <el-button 
                  v-else-if="hasSubmitted(scope.row)" 
                  type="info" 
                  size="small" 
                  @click="viewExamResult(scope.row)"
                >
                  查看成绩
                </el-button>
                <el-button 
                  v-else 
                  type="info" 
                  size="small" 
                  disabled
                >
                  {{ getStudentActionText(scope.row) }}
                </el-button>
              </template>
              
              <!-- 教师操作 -->
              <template v-else-if="isTeacher">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="viewExamDetail(scope.row)"
                >
                  查看
                </el-button>
                <el-button 
                  v-if="!isExamStarted(scope.row)" 
                  type="warning" 
                  size="small" 
                  @click="editExam(scope.row)"
                >
                  编辑
                </el-button>
                <el-button 
                  v-if="!isExamStarted(scope.row)" 
                  type="danger" 
                  size="small" 
                  @click="deleteExam(scope.row)"
                >
                  删除
                </el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
    
    <!-- 创建/编辑考试对话框 -->
    <el-dialog
      v-model="examDialogVisible"
      :title="isEditMode ? '编辑考试' : '创建新考试'"
      width="50%"
    >
      <el-form :model="examForm" :rules="examRules" ref="examFormRef" label-width="100px">
        <el-form-item label="考试名称" prop="title">
          <el-input v-model="examForm.title" placeholder="请输入考试名称" />
        </el-form-item>
        
        <el-form-item label="考试说明" prop="description">
          <el-input 
            v-model="examForm.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入考试说明"
          />
        </el-form-item>
        
        <el-form-item label="考试时间" prop="examTime">
          <el-date-picker
            v-model="examForm.examTime"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled="isEditMode && isExamStarted(currentExam)"
          />
        </el-form-item>
        
        <el-form-item label="考试时长" prop="duration">
          <el-input-number 
            v-model="examForm.duration" 
            :min="10" 
            :max="180" 
            :step="5"
            :disabled="isEditMode && isExamStarted(currentExam)"
          /> 分钟
        </el-form-item>
        
        <el-form-item label="总分" prop="totalScore">
          <el-input-number 
            v-model="examForm.totalScore" 
            :min="10" 
            :max="100" 
            :step="5"
            :disabled="isEditMode && isExamStarted(currentExam)"
          /> 分
        </el-form-item>
        
        <el-form-item label="及格分数" prop="passingScore">
          <el-input-number 
            v-model="examForm.passingScore" 
            :min="1" 
            :max="examForm.totalScore" 
            :step="1"
          /> 分
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="examDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitExamForm">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Document } from '@element-plus/icons-vue';
import { examApi } from '../../api/exam';
import { courseApi } from '../../api/course';
import { examRecordApi } from '../../api/examRecord';
import { useAuthStore } from '../../store/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 课程ID和标题
const courseId = computed(() => route.params.courseId);
const courseTitle = ref('');

// 用户角色
const isTeacher = computed(() => authStore.hasPermission('teacher'));
const isStudent = computed(() => authStore.hasPermission('student'));

// 考试列表数据
const loading = ref(true);
const exams = ref([]);
const myExamRecords = ref([]);

// 考试对话框
const examDialogVisible = ref(false);
const examFormRef = ref(null);
const isEditMode = ref(false);
const currentExam = ref(null);

// 考试表单
const examForm = ref({
  title: '',
  description: '',
  examTime: [],
  duration: 60,
  totalScore: 100,
  passingScore: 60,
});

// 表单验证规则
const examRules = {
  title: [
    { required: true, message: '请输入考试名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入考试说明', trigger: 'blur' }
  ],
  examTime: [
    { required: true, message: '请选择考试时间', trigger: 'change' },
    { 
      validator: (rule, value, callback) => {
        if (value && value.length === 2) {
          const startTime = new Date(value[0]);
          const endTime = new Date(value[1]);
          const now = new Date();
          
          if (startTime < now && !isEditMode.value) {
            callback(new Error('开始时间不能早于当前时间'));
          } else if (endTime <= startTime) {
            callback(new Error('结束时间必须晚于开始时间'));
          } else {
            callback();
          }
        } else {
          callback(new Error('请选择完整的考试时间范围'));
        }
      }, 
      trigger: 'change' 
    }
  ],
  duration: [
    { required: true, message: '请设置考试时长', trigger: 'change' }
  ],
  totalScore: [
    { required: true, message: '请设置总分', trigger: 'change' }
  ],
  passingScore: [
    { required: true, message: '请设置及格分数', trigger: 'change' },
    { 
      validator: (rule, value, callback) => {
        if (value > examForm.value.totalScore) {
          callback(new Error('及格分数不能大于总分'));
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
    courseTitle.value = res.data.title || '未知课程';
  } catch (error) {
    console.error('获取课程信息失败:', error);
    ElMessage.error('获取课程信息失败');
  }
};

// 获取考试列表
const fetchExams = async () => {
  loading.value = true;
  try {
    const res = await examApi.getCourseExams(courseId.value);
    exams.value = res.data || [];
    
    // 如果是学生，获取考试记录
    if (isStudent.value) {
      await fetchMyExamRecords();
    }
  } catch (error) {
    console.error('获取考试列表失败:', error);
    ElMessage.error('获取考试列表失败');
  } finally {
    loading.value = false;
  }
};

// 获取学生考试记录
const fetchMyExamRecords = async () => {
  try {
    // 为每个考试获取学生的考试记录
    const recordPromises = exams.value.map(async (exam) => {
      try {
        const res = await examRecordApi.getMyExamRecord(exam.id);
        return { examId: exam.id, record: res.data };
      } catch (error) {
        return { examId: exam.id, record: null };
      }
    });
    
    const records = await Promise.all(recordPromises);
    myExamRecords.value = records.reduce((acc, curr) => {
      if (curr.record) {
        acc[curr.examId] = curr.record;
      }
      return acc;
    }, {});
  } catch (error) {
    console.error('获取考试记录失败:', error);
  }
};

// 显示创建考试对话框
const showCreateExamDialog = () => {
  isEditMode.value = false;
  currentExam.value = null;
  examForm.value = {
    title: '',
    description: '',
    examTime: [],
    duration: 60,
    totalScore: 100,
    passingScore: 60,
  };
  examDialogVisible.value = true;
};

// 编辑考试
const editExam = (exam) => {
  isEditMode.value = true;
  currentExam.value = exam;
  
  examForm.value = {
    title: exam.title,
    description: exam.description || '',
    examTime: [exam.startTime, exam.endTime],
    duration: exam.duration,
    totalScore: exam.totalScore,
    passingScore: exam.passingScore,
  };
  
  examDialogVisible.value = true;
};

// 提交考试表单
const submitExamForm = async () => {
  if (!examFormRef.value) return;
  
  await examFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const examData = {
          title: examForm.value.title,
          description: examForm.value.description,
          startTime: examForm.value.examTime[0],
          endTime: examForm.value.examTime[1],
          duration: examForm.value.duration,
          totalScore: examForm.value.totalScore,
          passingScore: examForm.value.passingScore,
          courseId: courseId.value
        };
        
        if (isEditMode.value && currentExam.value) {
          // 更新考试
          await examApi.updateExam(currentExam.value.id, examData);
          ElMessage.success('考试更新成功');
        } else {
          // 创建考试
          await examApi.createExam(examData);
          ElMessage.success('考试创建成功');
        }
        
        examDialogVisible.value = false;
        fetchExams(); // 刷新考试列表
      } catch (error) {
        console.error('保存考试失败:', error);
        ElMessage.error('保存考试失败');
      }
    }
  });
};

// 删除考试
const deleteExam = async (exam) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除考试「${exam.title}」吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    
    await examApi.deleteExam(exam.id);
    ElMessage.success('考试删除成功');
    fetchExams(); // 刷新考试列表
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除考试失败:', error);
      ElMessage.error('删除考试失败');
    }
  }
};

// 查看考试详情（教师）
const viewExamDetail = (exam) => {
  router.push(`/exam/${exam.id}/manage`);
};

// 参加考试（学生）
const takeExam = (exam) => {
  router.push(`/exam/${exam.id}/take`);
};

// 查看考试结果（学生）
const viewExamResult = (exam) => {
  router.push(`/exam/${exam.id}/result`);
};

// 判断考试是否已开始
const isExamStarted = (exam) => {
  if (!exam) return false;
  const now = new Date();
  const startTime = new Date(exam.startTime);
  return now >= startTime;
};

// 判断考试是否已结束
const isExamEnded = (exam) => {
  if (!exam) return false;
  const now = new Date();
  const endTime = new Date(exam.endTime);
  return now > endTime;
};

// 判断学生是否可以参加考试
const canTakeExam = (exam) => {
  if (!isStudent.value) return false;
  
  const now = new Date();
  const startTime = new Date(exam.startTime);
  const endTime = new Date(exam.endTime);
  
  // 检查是否在考试时间范围内
  const isInTimeRange = now >= startTime && now <= endTime;
  
  // 检查是否已提交过
  const hasSubmittedExam = hasSubmitted(exam);
  
  return isInTimeRange && !hasSubmittedExam;
};

// 判断学生是否已提交考试
const hasSubmitted = (exam) => {
  return !!myExamRecords.value[exam.id];
};

// 获取学生操作按钮文本
const getStudentActionText = (exam) => {
  if (hasSubmitted(exam)) {
    return '已完成';
  }
  
  const now = new Date();
  const startTime = new Date(exam.startTime);
  const endTime = new Date(exam.endTime);
  
  if (now < startTime) {
    return '未开始';
  } else if (now > endTime) {
    return '已结束';
  }
  
  return '参加考试';
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
    return 'danger';
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

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return '未设置';
  const date = new Date(dateString);
  return date.toLocaleString();
};

onMounted(async () => {
  await fetchCourseInfo();
  await fetchExams();
});
</script>

<style scoped>
.exam-list-container {
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

.action-bar {
  margin-bottom: 20px;
  text-align: right;
}

.exams-section {
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

.exam-list-card {
  margin-bottom: 20px;
}

.exam-title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dialog-footer {
  text-align: right;
  margin-top: 20px;
}
</style>