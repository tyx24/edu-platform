<template>
  <div class="course-form-container">
    <div class="page-header">
      <h2>{{ isEditMode ? '编辑课程' : '创建课程' }}</h2>
      <p>{{ isEditMode ? '修改课程信息' : '创建一个新的课程' }}</p>
    </div>

    <el-card class="form-card">
      <el-form
        ref="courseFormRef"
        :model="courseForm"
        :rules="courseRules"
        label-width="120px"
        label-position="top"
      >
        <el-form-item label="课程标题" prop="title">
          <el-input v-model="courseForm.title" placeholder="请输入课程标题" />
        </el-form-item>

        <el-form-item label="课程简介" prop="summary">
          <el-input
            v-model="courseForm.summary"
            type="textarea"
            :rows="3"
            placeholder="请输入课程简介"
          />
        </el-form-item>

        <el-form-item label="课程描述" prop="description">
          <el-input
            v-model="courseForm.description"
            type="textarea"
            :rows="6"
            placeholder="请输入课程详细描述"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程分类" prop="category">
              <el-select v-model="courseForm.category" placeholder="选择课程分类" style="width: 100%">
                <el-option label="前端开发" value="frontend" />
                <el-option label="后端开发" value="backend" />
                <el-option label="移动开发" value="mobile" />
                <el-option label="数据科学" value="data_science" />
                <el-option label="人工智能" value="ai" />
                <el-option label="云计算" value="cloud" />
                <el-option label="网络安全" value="security" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度级别" prop="level">
              <el-select v-model="courseForm.level" placeholder="选择难度级别" style="width: 100%">
                <el-option label="初级" value="beginner" />
                <el-option label="中级" value="intermediate" />
                <el-option label="高级" value="advanced" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="courseForm.startTime"
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
                v-model="courseForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="课程封面" prop="coverImage">
          <el-upload
            class="cover-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleCoverSuccess"
            :before-upload="beforeCoverUpload"
          >
            <img v-if="courseForm.coverImage" :src="courseForm.coverImage" class="cover-image" />
            <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">建议上传16:9比例的图片，大小不超过2MB</div>
        </el-form-item>

        <el-form-item label="最大学生人数" prop="maxStudents">
          <el-input-number v-model="courseForm.maxStudents" :min="1" :max="500" />
        </el-form-item>

        <el-form-item label="课程标签" prop="tags">
          <el-tag
            v-for="tag in courseForm.tags"
            :key="tag"
            closable
            @close="handleTagClose(tag)"
            class="course-tag"
          >
            {{ tag }}
          </el-tag>
          <el-input
            v-if="tagInputVisible"
            ref="tagInputRef"
            v-model="tagInputValue"
            class="tag-input"
            size="small"
            @keyup.enter="handleTagConfirm"
            @blur="handleTagConfirm"
          />
          <el-button v-else class="button-new-tag" size="small" @click="showTagInput">
            + 添加标签
          </el-button>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm">{{ isEditMode ? '保存修改' : '创建课程' }}</el-button>
          <el-button @click="cancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { courseApi } from '../../api/course';

const route = useRoute();
const router = useRouter();

// 判断是否为编辑模式
const isEditMode = computed(() => route.name === 'CourseEdit');
const courseId = computed(() => route.params.id);

// 表单引用
const courseFormRef = ref(null);

// 课程表单数据
const courseForm = ref({
  title: '',
  summary: '',
  description: '',
  category: '',
  level: 'beginner',
  startTime: '',
  endTime: '',
  coverImage: '',
  maxStudents: 100,
  tags: []
});

// 表单验证规则
const courseRules = {
  title: [
    { required: true, message: '请输入课程标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  summary: [
    { required: true, message: '请输入课程简介', trigger: 'blur' },
    { max: 200, message: '长度不能超过 200 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入课程描述', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择课程分类', trigger: 'change' }
  ],
  level: [
    { required: true, message: '请选择难度级别', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value && courseForm.value.startTime && new Date(value) <= new Date(courseForm.value.startTime)) {
          callback(new Error('结束时间必须晚于开始时间'));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ],
  maxStudents: [
    { required: true, message: '请设置最大学生人数', trigger: 'change' },
    { type: 'number', min: 1, message: '人数必须大于0', trigger: 'change' }
  ]
};

// 标签相关
const tagInputVisible = ref(false);
const tagInputValue = ref('');
const tagInputRef = ref(null);

// 在组件挂载时获取课程信息（如果是编辑模式）
onMounted(async () => {
  if (isEditMode.value) {
    await fetchCourseInfo();
  }
});

// 获取课程信息
const fetchCourseInfo = async () => {
  try {
    const res = await courseApi.getCourseById(courseId.value);
    const courseData = res.data;
    
    // 填充表单数据
    courseForm.value = {
      title: courseData.title || '',
      summary: courseData.summary || '',
      description: courseData.description || '',
      category: courseData.category || '',
      level: courseData.level || 'beginner',
      startTime: courseData.startTime || '',
      endTime: courseData.endTime || '',
      coverImage: courseData.coverImage || '',
      maxStudents: courseData.maxStudents || 100,
      tags: courseData.tags || []
    };
  } catch (error) {
    console.error('获取课程信息失败:', error);
    ElMessage.error('获取课程信息失败');
    router.push('/my-courses');
  }
};

// 提交表单
const submitForm = async () => {
  if (!courseFormRef.value) return;
  
  await courseFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const courseData = { ...courseForm.value };
        
        if (isEditMode.value) {
          // 更新课程
          await courseApi.updateCourse(courseId.value, courseData);
          ElMessage.success('课程更新成功');
        } else {
          // 创建课程
          await courseApi.createCourse(courseData);
          ElMessage.success('课程创建成功');
        }
        
        // 跳转到我的课程页面
        router.push('/my-courses');
      } catch (error) {
        console.error('保存课程失败:', error);
        ElMessage.error('保存课程失败');
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
      router.push('/my-courses');
    })
    .catch(() => {
      // 用户取消操作，继续编辑
    });
};

// 处理封面上传成功
const handleCoverSuccess = (res) => {
  courseForm.value.coverImage = res.data.url;
  ElMessage.success('封面上传成功');
};

// 上传前检查
const beforeCoverUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error('上传封面图片只能是图片格式!');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('上传封面图片大小不能超过 2MB!');
    return false;
  }
  return true;
};

// 显示标签输入框
const showTagInput = () => {
  tagInputVisible.value = true;
  nextTick(() => {
    tagInputRef.value.focus();
  });
};

// 处理标签确认
const handleTagConfirm = () => {
  if (tagInputValue.value) {
    if (courseForm.value.tags.indexOf(tagInputValue.value) === -1) {
      courseForm.value.tags.push(tagInputValue.value);
    }
  }
  tagInputVisible.value = false;
  tagInputValue.value = '';
};

// 处理标签关闭
const handleTagClose = (tag) => {
  courseForm.value.tags.splice(courseForm.value.tags.indexOf(tag), 1);
};
</script>

<style scoped>
.course-form-container {
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

.cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 300px;
  height: 169px; /* 16:9 比例 */
  display: flex;
  justify-content: center;
  align-items: center;
}

.cover-uploader:hover {
  border-color: #409eff;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #606266;
  margin-top: 8px;
}

.course-tag {
  margin-right: 10px;
  margin-bottom: 10px;
}

.tag-input {
  width: 90px;
  margin-right: 10px;
  vertical-align: bottom;
}

.button-new-tag {
  margin-bottom: 10px;
}
</style>