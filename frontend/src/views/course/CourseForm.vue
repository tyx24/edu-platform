<template>
  <transition name="page-fade" appear>
    <div class="course-form-container">
    <div class="page-header">
      <h2>{{ isEditMode ? '编辑课程' : '创建课程' }}</h2>
      <p>{{ isEditMode ? '修改课程信息' : '创建一个新的课程' }}</p>
    </div>

    <div class="form-wrapper">
      <el-card class="form-card">
      <el-form ref="courseFormRef" :model="courseForm" :rules="courseRules" label-width="120px" label-position="top">
        <el-form-item label="课程标题" prop="title">
          <el-input v-model="courseForm.title" placeholder="请输入课程标题" />
        </el-form-item>



        <el-form-item label="课程描述" prop="description">
          <el-input v-model="courseForm.description" type="textarea" :rows="6" placeholder="请输入课程详细描述" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程分类" prop="category">
              <el-select v-model="courseForm.category" placeholder="选择课程分类" style="width: 100%">
                <el-option label="全部分类" value="" />
                <el-option label="💻 前端开发" value="前端开发" />
                <el-option label="⚙️ 后端开发" value="后端开发" />
                <el-option label="📱 移动开发" value="移动开发" />
                <el-option label="📊 大数据" value="大数据" />
                <el-option label="🤖 机器学习" value="机器学习" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度级别" prop="difficulty">
              <el-select v-model="courseForm.difficulty" placeholder="选择难度级别" style="width: 100%">
                <el-option label="初级" value="初级" />
                <el-option label="中级" value="中级" />
                <el-option label="高级" value="高级" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="课程封面" prop="coverUrl">
          <el-upload class="cover-uploader" action="/api/upload" :show-file-list="false"
            :on-success="handleCoverSuccess" :before-upload="beforeCoverUpload">
            <img v-if="courseForm.coverUrl" :src="courseForm.coverUrl" class="cover-image" />
            <el-icon v-else class="cover-uploader-icon">
              <Plus />
            </el-icon>
          </el-upload>
          <div class="upload-tip">建议上传16:9比例的图片，大小不超过2MB</div>
        </el-form-item>

        <el-form-item label="最大学生人数" prop="max_students">
          <el-input-number v-model="courseForm.max_students" :min="1" :max="500" />
        </el-form-item>

        <div class="form-actions">
          <el-button type="primary" @click="submitForm" :loading="isSubmitting" :disabled="isSubmitting">
            <span v-if="!isSubmitting">{{ isEditMode ? '更新课程' : '创建课程' }}</span>
            <span v-else>{{ isEditMode ? '更新中...' : '创建中...' }}</span>
          </el-button>
          <el-button @click="cancel" :disabled="isSubmitting">取消</el-button>
        </div>
      </el-form>
      </el-card>
    </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
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

// 加载状态
const isSubmitting = ref(false);

// 课程表单数据
const courseForm = ref({
  title: '',
  description: '',
  coverUrl: '',
  category: '',
  difficulty: '初级',
  max_students: 100
});

// 表单验证规则
const courseRules = {
  title: [
    { required: true, message: '请输入课程标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入课程描述', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择课程分类', trigger: 'change' }
  ],
  difficulty: [
    { required: true, message: '请选择难度级别', trigger: 'change' }
  ],
  maxStudents: [
    { required: true, message: '请设置最大学生人数', trigger: 'change' },
    { type: 'number', min: 1, message: '人数必须大于0', trigger: 'change' }
  ]
};

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
      difficulty: courseData.difficulty || '初级',
      coverUrl: courseData.coverUrl || '',
      max_students: courseData.maxStudents || 100
    };
  } catch (error) {
    console.error('获取课程信息失败:', error);
    ElMessage.error('获取课程信息失败');
    router.push('/my-courses');
  }
};

// 提交表单
const submitForm = async () => {
  if (!courseFormRef.value || isSubmitting.value) return;

  await courseFormRef.value.validate(async (valid) => {
    if (valid) {
      isSubmitting.value = true;
      try {
        const courseData = { ...courseForm.value };

        if (isEditMode.value) {
          // 更新课程
          await courseApi.updateCourse(courseId.value, courseData);
          ElMessage.success('🎉 课程更新成功！');
        } else {
          // 创建课程
          await courseApi.createCourse(courseData);
          ElMessage.success('🎉 课程创建成功！');
        }

        // 延迟跳转，让用户看到成功消息
        setTimeout(() => {
          router.push('/my-courses');
        }, 1000);
      } catch (error) {
        console.error('保存课程失败:', error);
        ElMessage.error('❌ 保存课程失败，请重试');
      } finally {
        isSubmitting.value = false;
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
  courseForm.value.coverUrl = res.data.url;
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


</script>

<style scoped>
.course-form-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
  position: relative;
}

.course-form-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="50" cy="50" r="1" fill="%23ffffff" opacity="0.1"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>') repeat;
  pointer-events: none;
}

.page-header {
  margin-bottom: 40px;
  text-align: center;
  position: relative;
  z-index: 1;
}

.page-header h2 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 12px;
  color: #ffffff;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
  letter-spacing: -0.5px;
}

.page-header p {
  color: rgba(255, 255, 255, 0.9);
  font-size: 18px;
  font-weight: 300;
  text-shadow: 0 1px 2px rgba(0,0,0,0.2);
}

.form-wrapper {
  max-width: 900px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.form-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1), 0 8px 16px rgba(0, 0, 0, 0.08);
  padding: 40px;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.form-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15), 0 12px 24px rgba(0, 0, 0, 0.12);
}

/* 表单项样式优化 */
:deep(.el-form-item) {
  margin-bottom: 32px;
  animation: slideInUp 0.6s ease-out;
  animation-fill-mode: both;
}

:deep(.el-form-item:nth-child(1)) { animation-delay: 0.1s; }
:deep(.el-form-item:nth-child(2)) { animation-delay: 0.2s; }
:deep(.el-form-item:nth-child(3)) { animation-delay: 0.3s; }
:deep(.el-form-item:nth-child(4)) { animation-delay: 0.4s; }
:deep(.el-form-item:nth-child(5)) { animation-delay: 0.5s; }
:deep(.el-form-item:nth-child(6)) { animation-delay: 0.6s; }
:deep(.el-form-item:nth-child(7)) { animation-delay: 0.7s; }

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

:deep(.el-form-item__label) {
  font-weight: 600;
  font-size: 16px;
  color: #2c3e50;
  margin-bottom: 12px;
  line-height: 1.4;
  position: relative;
  transition: all 0.3s ease;
}

:deep(.el-form-item__label::after) {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #667eea, #764ba2);
  transition: width 0.3s ease;
}

:deep(.el-form-item.is-required .el-form-item__label::after),
:deep(.el-form-item:hover .el-form-item__label::after) {
  width: 30px;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  border: 2px solid #e1e8ed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  background: #ffffff;
}

:deep(.el-input__wrapper:hover) {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1), 0 4px 12px rgba(102, 126, 234, 0.15);
}

:deep(.el-input__inner) {
  font-size: 16px;
  color: #2c3e50;
  padding: 16px 20px;
  line-height: 1.5;
}

:deep(.el-textarea__inner) {
  border-radius: 12px;
  border: 2px solid #e1e8ed;
  font-size: 16px;
  color: #2c3e50;
  padding: 16px 20px;
  line-height: 1.6;
  transition: all 0.3s ease;
  resize: vertical;
  min-height: 120px;
}

:deep(.el-textarea__inner:hover) {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

:deep(.el-textarea__inner:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1), 0 4px 12px rgba(102, 126, 234, 0.15);
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 12px;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input__wrapper) {
  border-radius: 12px;
}

/* 选择器下拉菜单样式优化 */
:deep(.el-select-dropdown) {
  border: none;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15), 0 8px 16px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(20px);
  background: rgba(255, 255, 255, 0.95);
  padding: 8px;
  margin-top: 8px;
}

:deep(.el-select-dropdown__item) {
  border-radius: 8px;
  margin: 2px 0;
  padding: 12px 16px;
  font-size: 15px;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
}

:deep(.el-select-dropdown__item:hover) {
  background: linear-gradient(135deg, #f0f4ff 0%, #e8f2ff 100%);
  color: #667eea;
  transform: translateX(4px);
}

:deep(.el-select-dropdown__item.selected) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  font-weight: 600;
}

:deep(.el-select-dropdown__item.selected::after) {
  content: '✓';
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  font-weight: bold;
}

/* 输入数字组件样式优化 */
:deep(.el-input-number .el-input-number__decrease),
:deep(.el-input-number .el-input-number__increase) {
  border: none;
  background: #f8fafc;
  color: #667eea;
  border-radius: 8px;
  width: 32px;
  height: 32px;
  transition: all 0.2s ease;
}

:deep(.el-input-number .el-input-number__decrease:hover),
:deep(.el-input-number .el-input-number__increase:hover) {
  background: #667eea;
  color: #ffffff;
  transform: scale(1.1);
}

/* 表单验证错误提示样式优化 */
:deep(.el-form-item.is-error .el-input__wrapper) {
  border-color: #f56565 !important;
  box-shadow: 0 0 0 3px rgba(245, 101, 101, 0.1), 0 4px 12px rgba(245, 101, 101, 0.15) !important;
  animation: shake 0.5s ease-in-out;
}

:deep(.el-form-item.is-error .el-textarea__inner) {
  border-color: #f56565 !important;
  box-shadow: 0 0 0 3px rgba(245, 101, 101, 0.1), 0 4px 12px rgba(245, 101, 101, 0.15) !important;
  animation: shake 0.5s ease-in-out;
}

:deep(.el-form-item__error) {
  color: #f56565;
  font-size: 14px;
  font-weight: 500;
  margin-top: 8px;
  padding: 8px 12px;
  background: rgba(245, 101, 101, 0.1);
  border-radius: 8px;
  border-left: 3px solid #f56565;
  animation: slideInDown 0.3s ease-out;
  position: relative;
}

:deep(.el-form-item__error::before) {
  content: '⚠️';
  margin-right: 6px;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-2px); }
  20%, 40%, 60%, 80% { transform: translateX(2px); }
}

@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 成功状态样式 */
:deep(.el-form-item.is-success .el-input__wrapper) {
  border-color: #48bb78 !important;
  box-shadow: 0 0 0 3px rgba(72, 187, 120, 0.1), 0 4px 12px rgba(72, 187, 120, 0.15) !important;
}

:deep(.el-form-item.is-success .el-textarea__inner) {
  border-color: #48bb78 !important;
  box-shadow: 0 0 0 3px rgba(72, 187, 120, 0.1), 0 4px 12px rgba(72, 187, 120, 0.15) !important;
}

.cover-uploader {
  border: 2px dashed #e1e8ed;
  border-radius: 16px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 300px;
  height: 169px;
  /* 16:9 比例 */
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.cover-uploader:hover {
  border-color: #667eea;
  background: linear-gradient(135deg, #f0f4ff 0%, #e8f2ff 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.15);
}

.cover-uploader-icon {
  font-size: 32px;
  color: #667eea;
  width: 80px;
  height: 80px;
  text-align: center;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-uploader-icon::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60px;
  height: 60px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 50%;
  z-index: -1;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 12px;
}

/* 按钮样式优化 */
:deep(.el-button) {
  border-radius: 12px;
  font-weight: 600;
  font-size: 16px;
  padding: 14px 28px;
  transition: all 0.3s ease;
  border: none;
  position: relative;
  overflow: hidden;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
}

:deep(.el-button--primary:active) {
  transform: translateY(0);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

:deep(.el-button.is-loading) {
  position: relative;
  overflow: hidden;
}

:deep(.el-button.is-loading::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% { left: -100%; }
  100% { left: 100%; }
}

:deep(.el-button--primary.is-loading) {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  }
  50% {
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.5), 0 0 20px rgba(102, 126, 234, 0.3);
  }
}

:deep(.el-button--default) {
  background: #ffffff;
  color: #667eea;
  border: 2px solid #e1e8ed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

:deep(.el-button--default:hover) {
  background: #f8fafc;
  border-color: #667eea;
  color: #5a6fd8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

:deep(.el-button--default:active) {
  transform: translateY(0);
}

/* 表单按钮组样式 */
.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 40px;
  padding-top: 32px;
  border-top: 1px solid #f0f2f5;
}

/* 移动端响应式设计优化 */
@media (max-width: 1024px) {
  .course-form-container {
    padding: 30px 15px;
  }
  
  .form-wrapper {
    max-width: 100%;
  }
  
  .form-card {
    padding: 30px 20px;
    border-radius: 20px;
  }
}

@media (max-width: 768px) {
  .course-form-container {
    padding: 20px 10px;
    min-height: auto;
  }
  
  .page-header h2 {
    font-size: 28px;
  }
  
  .page-header p {
    font-size: 16px;
  }
  
  .form-card {
    padding: 25px 15px;
    border-radius: 16px;
    margin-bottom: 15px;
  }
  
  :deep(.el-form-item) {
    margin-bottom: 24px;
  }
  
  :deep(.el-form-item__label) {
    font-size: 15px;
    margin-bottom: 10px;
  }
  
  :deep(.el-input__inner) {
    font-size: 16px;
    padding: 14px 16px;
  }
  
  :deep(.el-textarea__inner) {
    font-size: 16px;
    padding: 14px 16px;
    min-height: 100px;
  }
  
  :deep(.el-row) {
    margin: 0 !important;
  }
  
  :deep(.el-col) {
    padding: 0 !important;
    margin-bottom: 20px;
  }
  
  .cover-uploader {
    width: 100%;
    height: 200px;
    max-width: 300px;
    margin: 0 auto;
  }
  
  .form-actions {
    flex-direction: column;
    align-items: center;
    gap: 12px;
    margin-top: 30px;
    padding-top: 25px;
  }
  
  :deep(.el-button) {
    width: 100%;
    max-width: 280px;
    padding: 16px 28px;
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  .course-form-container {
    padding: 15px 8px;
  }
  
  .page-header {
    margin-bottom: 30px;
  }
  
  .page-header h2 {
    font-size: 24px;
  }
  
  .page-header p {
    font-size: 14px;
  }
  
  .form-card {
    padding: 20px 12px;
    border-radius: 12px;
  }
  
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
  
  :deep(.el-form-item__label) {
    font-size: 14px;
  }
  
  :deep(.el-input__inner) {
    font-size: 15px;
    padding: 12px 14px;
  }
  
  :deep(.el-textarea__inner) {
    font-size: 15px;
    padding: 12px 14px;
    min-height: 80px;
  }
  
  .cover-uploader {
    height: 160px;
  }
  
  .upload-tip {
    font-size: 11px;
    text-align: center;
  }
  
  :deep(.el-button) {
    padding: 14px 24px;
    font-size: 15px;
  }
}

/* 页面过渡动画 */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: all 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(30px) scale(0.98);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-20px) scale(1.02);
}

.page-fade-enter-to,
.page-fade-leave-from {
  opacity: 1;
  transform: translateY(0) scale(1);
}

/* 表单卡片进入动画 */
.form-card {
  animation: cardSlideIn 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.2s both;
}

@keyframes cardSlideIn {
  0% {
    opacity: 0;
    transform: translateY(40px) scale(0.95);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 页面标题动画 */
.page-header {
  animation: headerFadeIn 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
}

@keyframes headerFadeIn {
  0% {
    opacity: 0;
    transform: translateY(-20px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 触摸设备优化 */
@media (hover: none) and (pointer: coarse) {
  :deep(.el-input__wrapper:hover),
  :deep(.el-textarea__inner:hover) {
    border-color: #e1e8ed;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }
  
  :deep(.el-button:hover) {
    transform: none;
  }
  
  .form-card:hover {
    transform: none;
  }
  
  .cover-uploader:hover {
    transform: none;
  }
}

.upload-tip {
  font-size: 12px;
  color: #606266;
  margin-top: 8px;
}

</style>