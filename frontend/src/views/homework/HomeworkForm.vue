<template>
  <div class="homework-form-container">
    <div class="page-header">
      <h2>{{ isEditMode ? '编辑作业' : '创建作业' }}</h2>
      <p>{{ isEditMode ? '修改作业信息' : '创建一个新的作业' }}</p>
    </div>

    <el-card class="form-card">
      <el-form
        ref="homeworkFormRef"
        :model="homeworkForm"
        :rules="homeworkRules"
        label-width="120px"
        label-position="top"
      >
        <el-form-item label="作业标题" prop="title">
          <el-input v-model="homeworkForm.title" placeholder="请输入作业标题" />
        </el-form-item>

        <el-form-item label="作业描述" prop="description">
          <el-input
            v-model="homeworkForm.description"
            type="textarea"
            :rows="6"
            placeholder="请输入作业描述和要求"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="homeworkForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="截止时间" prop="endTime">
              <el-date-picker
                v-model="homeworkForm.endTime"
                type="datetime"
                placeholder="选择截止时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="总分" prop="totalScore">
              <el-input-number v-model="homeworkForm.totalScore" :min="1" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属课程" prop="courseId">
              <el-select v-model="homeworkForm.courseId" placeholder="选择所属课程" style="width: 100%">
                <el-option 
                  v-for="course in courseOptions" 
                  :key="course.id" 
                  :label="course.title" 
                  :value="course.id" 
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="附件">
          <el-upload
            class="attachment-uploader"
            action="/api/upload"
            :on-success="handleAttachmentSuccess"
            :on-remove="handleAttachmentRemove"
            :before-upload="beforeAttachmentUpload"
            multiple
          >
            <el-button type="primary">上传附件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                可上传任意类型文件，单个文件不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="作业设置">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-checkbox v-model="homeworkForm.allowLateSubmission">允许迟交</el-checkbox>
            </el-col>
            <el-col :span="8">
              <el-checkbox v-model="homeworkForm.requireAttachment">要求附件</el-checkbox>
            </el-col>
            <el-col :span="8">
              <el-checkbox v-model="homeworkForm.allowResubmission">允许重交</el-checkbox>
            </el-col>
          </el-row>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm">{{ isEditMode ? '保存修改' : '创建作业' }}</el-button>
          <el-button @click="cancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { homeworkApi } from '../../api/homework';
import { courseApi } from '../../api/course';

const route = useRoute();
const router = useRouter();

// 判断是否为编辑模式
const isEditMode = computed(() => route.name === 'HomeworkEdit');
const homeworkId = computed(() => route.params.id);

// 表单引用
const homeworkFormRef = ref(null);

// 课程选项
const courseOptions = ref([]);

// 作业表单数据
const homeworkForm = ref({
  title: '',
  description: '',
  startTime: '',
  endTime: '',
  totalScore: 100,
  courseId: '',
  attachments: [],
  allowLateSubmission: true,
  requireAttachment: false,
  allowResubmission: true
});

// 表单验证规则
const homeworkRules = {
  title: [
    { required: true, message: '请输入作业标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
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
  ],
  courseId: [
    { required: true, message: '请选择所属课程', trigger: 'change' }
  ]
};

// 在组件挂载时获取数据
onMounted(async () => {
  await fetchCourses();
  
  if (isEditMode.value) {
    await fetchHomeworkInfo();
  } else if (route.query.courseId) {
    // 如果是从课程页面跳转过来创建作业，自动选择课程
    homeworkForm.value.courseId = route.query.courseId;
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

// 获取作业信息
const fetchHomeworkInfo = async () => {
  try {
    const res = await homeworkApi.getHomeworkById(homeworkId.value);
    const homeworkData = res.data;
    
    // 填充表单数据
    homeworkForm.value = {
      title: homeworkData.title || '',
      description: homeworkData.description || '',
      startTime: homeworkData.startTime || '',
      endTime: homeworkData.endTime || '',
      totalScore: homeworkData.totalScore || 100,
      courseId: homeworkData.courseId || '',
      attachments: homeworkData.attachments || [],
      allowLateSubmission: homeworkData.allowLateSubmission !== false, // 默认为true
      requireAttachment: homeworkData.requireAttachment || false,
      allowResubmission: homeworkData.allowResubmission !== false // 默认为true
    };
  } catch (error) {
    console.error('获取作业信息失败:', error);
    ElMessage.error('获取作业信息失败');
    router.push('/teacher');
  }
};

// 提交表单
const submitForm = async () => {
  if (!homeworkFormRef.value) return;
  
  await homeworkFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const homeworkData = { ...homeworkForm.value };
        
        if (isEditMode.value) {
          // 更新作业
          await homeworkApi.updateHomework(homeworkId.value, homeworkData);
          ElMessage.success('作业更新成功');
        } else {
          // 创建作业
          await homeworkApi.createHomework(homeworkData);
          ElMessage.success('作业创建成功');
        }
        
        // 跳转到作业列表页面
        if (homeworkForm.value.courseId) {
          router.push(`/homework/course/${homeworkForm.value.courseId}`);
        } else {
          router.push('/teacher');
        }
      } catch (error) {
        console.error('保存作业失败:', error);
        ElMessage.error('保存作业失败');
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
      if (homeworkForm.value.courseId) {
        router.push(`/homework/course/${homeworkForm.value.courseId}`);
      } else {
        router.push('/teacher');
      }
    })
    .catch(() => {
      // 用户取消操作，继续编辑
    });
};

// 处理附件上传成功
const handleAttachmentSuccess = (res) => {
  homeworkForm.value.attachments.push({
    id: res.data.id,
    fileName: res.data.fileName,
    fileSize: res.data.fileSize,
    url: res.data.url
  });
  ElMessage.success('附件上传成功');
};

// 处理附件移除
const handleAttachmentRemove = (file) => {
  const index = homeworkForm.value.attachments.findIndex(item => item.url === file.url);
  if (index !== -1) {
    homeworkForm.value.attachments.splice(index, 1);
  }
};

// 上传前检查
const beforeAttachmentUpload = (file) => {
  const isLt10M = file.size / 1024 / 1024 < 10;

  if (!isLt10M) {
    ElMessage.error('上传附件大小不能超过 10MB!');
    return false;
  }
  return true;
};
</script>

<style scoped>
.homework-form-container {
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

.attachment-uploader {
  margin-bottom: 10px;
}
</style>