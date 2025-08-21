<template>
  <div class="exam-form-container">
    <div class="page-header">
      <h2>{{ isEditMode ? '编辑考试' : '创建考试' }}</h2>
      <p>{{ isEditMode ? '修改考试信息' : '创建一个新的考试' }}</p>
    </div>

    <el-card class="form-card">
      <el-form
        ref="examFormRef"
        :model="examForm"
        :rules="examRules"
        label-width="120px"
        label-position="top"
      >
        <el-form-item label="考试标题" prop="title">
          <el-input v-model="examForm.title" placeholder="请输入考试标题" />
        </el-form-item>

        <el-form-item label="考试说明" prop="description">
          <el-input
            v-model="examForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入考试说明"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="examForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="examForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="考试时长(分钟)" prop="duration">
              <el-input-number v-model="examForm.duration" :min="1" :max="300" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="总分" prop="totalScore">
              <el-input-number v-model="examForm.totalScore" :min="1" :max="1000" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="及格分数" prop="passingScore">
              <el-input-number 
                v-model="examForm.passingScore" 
                :min="1" 
                :max="examForm.totalScore" 
                style="width: 100%" 
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="所属课程" prop="courseId">
          <el-select v-model="examForm.courseId" placeholder="选择所属课程" style="width: 100%">
            <el-option 
              v-for="course in courseOptions" 
              :key="course.id" 
              :label="course.title" 
              :value="course.id" 
            />
          </el-select>
        </el-form-item>

        <el-form-item label="考试设置">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-checkbox v-model="examForm.shuffleQuestions">随机题目顺序</el-checkbox>
            </el-col>
            <el-col :span="8">
              <el-checkbox v-model="examForm.shuffleOptions">随机选项顺序</el-checkbox>
            </el-col>
            <el-col :span="8">
              <el-checkbox v-model="examForm.showResult">完成后显示结果</el-checkbox>
            </el-col>
          </el-row>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm">{{ isEditMode ? '保存修改' : '创建考试' }}</el-button>
          <el-button @click="cancel">取消</el-button>
          <el-button 
            v-if="isEditMode" 
            type="success" 
            @click="goToQuestionManager"
          >
            管理题目
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { examApi } from '../../api/exam';
import { courseApi } from '../../api/course';

const route = useRoute();
const router = useRouter();

// 判断是否为编辑模式
const isEditMode = computed(() => route.name === 'ExamEdit');
const examId = computed(() => route.params.id);

// 表单引用
const examFormRef = ref(null);

// 课程选项
const courseOptions = ref([]);

// 考试表单数据
const examForm = ref({
  title: '',
  description: '',
  startTime: '',
  endTime: '',
  duration: 60,
  totalScore: 100,
  passingScore: 60,
  courseId: '',
  shuffleQuestions: false,
  shuffleOptions: false,
  showResult: true
});

// 表单验证规则
const examRules = {
  title: [
    { required: true, message: '请输入考试标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入考试说明', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value && examForm.value.startTime && new Date(value) <= new Date(examForm.value.startTime)) {
          callback(new Error('结束时间必须晚于开始时间'));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ],
  duration: [
    { required: true, message: '请设置考试时长', trigger: 'change' },
    { type: 'number', min: 1, message: '时长必须大于0', trigger: 'change' }
  ],
  totalScore: [
    { required: true, message: '请设置总分', trigger: 'change' },
    { type: 'number', min: 1, message: '总分必须大于0', trigger: 'change' }
  ],
  passingScore: [
    { required: true, message: '请设置及格分数', trigger: 'change' },
    { type: 'number', min: 1, message: '及格分数必须大于0', trigger: 'change' },
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
  ],
  courseId: [
    { required: true, message: '请选择所属课程', trigger: 'change' }
  ]
};

// 在组件挂载时获取数据
onMounted(async () => {
  await fetchCourses();
  
  if (isEditMode.value) {
    await fetchExamInfo();
  } else if (route.query.courseId) {
    // 如果是从课程页面跳转过来创建考试，自动选择课程
    examForm.value.courseId = route.query.courseId;
  }
});

// 获取教师的课程列表
const fetchCourses = async () => {
  try {
    const res = await courseApi.getTeacherCourses();
    courseOptions.value = res.data || [];
  } catch (error) {
    console.error('获取课程列表失败:', error);
    ElMessage.error('获取课程列表失败');
  }
};

// 获取考试信息
const fetchExamInfo = async () => {
  try {
    const res = await examApi.getExamById(examId.value);
    const examData = res.data;
    
    // 填充表单数据
    examForm.value = {
      title: examData.title || '',
      description: examData.description || '',
      startTime: examData.startTime || '',
      endTime: examData.endTime || '',
      duration: examData.duration || 60,
      totalScore: examData.totalScore || 100,
      passingScore: examData.passingScore || 60,
      courseId: examData.courseId || '',
      shuffleQuestions: examData.shuffleQuestions || false,
      shuffleOptions: examData.shuffleOptions || false,
      showResult: examData.showResult !== false // 默认为true
    };
  } catch (error) {
    console.error('获取考试信息失败:', error);
    ElMessage.error('获取考试信息失败');
    router.push('/teacher');
  }
};

// 提交表单
const submitForm = async () => {
  if (!examFormRef.value) return;
  
  await examFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const examData = { ...examForm.value };
        
        if (isEditMode.value) {
          // 更新考试
          await examApi.updateExam(examId.value, examData);
          ElMessage.success('考试更新成功');
          
          // 询问是否前往管理题目
          ElMessageBox.confirm(
            '考试信息已更新，是否前往管理题目？',
            '提示',
            {
              confirmButtonText: '前往管理题目',
              cancelButtonText: '返回列表',
              type: 'info',
            }
          )
            .then(() => {
              router.push(`/exam/${examId.value}/questions`);
            })
            .catch(() => {
              router.push(`/exam/course/${examForm.value.courseId}`);
            });
        } else {
          // 创建考试
          const res = await examApi.createExam(examData);
          const newExamId = res.data.id;
          ElMessage.success('考试创建成功');
          
          // 询问是否前往添加题目
          ElMessageBox.confirm(
            '考试已创建，是否立即添加题目？',
            '提示',
            {
              confirmButtonText: '添加题目',
              cancelButtonText: '返回列表',
              type: 'info',
            }
          )
            .then(() => {
              router.push(`/exam/${newExamId}/questions`);
            })
            .catch(() => {
              router.push(`/exam/course/${examForm.value.courseId}`);
            });
        }
      } catch (error) {
        console.error('保存考试失败:', error);
        ElMessage.error('保存考试失败');
      }
    }
  });
};

// 取消操作
const cancel = () => {
  ElMessageBox.confirm(
    '确定要取消吗？所有未保存的更改将丢失',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '返回编辑',
      type: 'warning',
    }
  )
    .then(() => {
      if (examForm.value.courseId) {
        router.push(`/exam/course/${examForm.value.courseId}`);
      } else {
        router.push('/teacher');
      }
    })
    .catch(() => {
      // 用户取消操作，继续编辑
    });
};

// 前往题目管理
const goToQuestionManager = () => {
  router.push(`/exam/${examId.value}/questions`);
};
</script>

<style scoped>
.exam-form-container {
  max-width: 1000px;
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

.form-card {
  margin-bottom: 20px;
}
</style>