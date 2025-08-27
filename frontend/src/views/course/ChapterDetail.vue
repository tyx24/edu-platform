<template>
  <div class="chapter-detail-container" v-loading="loading">
    <!-- 返回按钮 -->
    <div class="back-section">
      <el-button @click="goBack" type="text" class="back-btn">
        <i class="el-icon-arrow-left"></i>
        返回课程
      </el-button>
    </div>

    <!-- 章节信息 -->
    <div class="chapter-header" v-if="chapter">
      <div class="chapter-info">
        <h1 class="chapter-title">{{ chapter.title }}</h1>
        <div class="chapter-meta">
          <span class="meta-item">
            <i class="el-icon-folder"></i>
            所属课程: {{ course?.title }}
          </span>
          <span class="meta-item">
            <i class="el-icon-sort"></i>
            排序: {{ chapter.orderNum }}
          </span>
          <span class="meta-item" v-if="chapter.parentId !== 0">
            <i class="el-icon-connection"></i>
            父章节: {{ parentChapter?.title }}
          </span>
        </div>
      </div>
    </div>

    <!-- 章节内容 -->
    <div class="chapter-content" v-if="chapter">
      <el-card class="content-card">
        <template #header>
          <div class="card-header">
            <span>章节内容</span>
            <el-button v-if="isTeacher" type="primary" size="small" @click="editChapter">
              编辑章节
            </el-button>
          </div>
        </template>
        
        <div class="chapter-description" v-if="chapter.description">
          <p>{{ chapter.description }}</p>
        </div>
        <div v-else class="no-description">
          <el-empty description="暂无章节描述" :image-size="100" />
        </div>
      </el-card>

      <!-- 子章节列表 -->
      <el-card class="content-card" v-if="subChapters.length > 0">
        <template #header>
          <div class="card-header">
            <span>子章节</span>
            <el-button v-if="isTeacher" type="primary" size="small" @click="addSubChapter">
              添加子章节
            </el-button>
          </div>
        </template>
        
        <div class="sub-chapters-list">
          <div 
            v-for="subChapter in subChapters" 
            :key="subChapter.id" 
            class="sub-chapter-item"
            @click="viewSubChapter(subChapter)"
          >
            <div class="sub-chapter-info">
              <i class="el-icon-folder"></i>
              <span class="sub-chapter-title">{{ subChapter.title }}</span>
              <span class="sub-chapter-order">排序: {{ subChapter.orderNum }}</span>
            </div>
            <div class="sub-chapter-actions" v-if="isTeacher">
              <el-button type="text" size="small" @click.stop="editSubChapter(subChapter)">
                编辑
              </el-button>
              <el-button type="text" size="small" @click.stop="deleteSubChapter(subChapter)">
                删除
              </el-button>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 章节资源 -->
      <el-card class="content-card">
        <template #header>
          <div class="card-header">
            <span>章节资源</span>
            <el-button v-if="isTeacher" type="primary" size="small" @click="addResource">
              添加资源
            </el-button>
          </div>
        </template>
        
        <div v-if="resources.length > 0" class="resources-list">
          <div 
            v-for="resource in resources" 
            :key="resource.id" 
            class="resource-item"
          >
            <div class="resource-content">
              <div class="resource-header">
                <div class="resource-info">
                  <i class="el-icon-document"></i>
                  <span class="resource-title">{{ resource.title }}</span>
                  <el-tag size="small" :type="getResourceTypeColor(resource.type)">
                    {{ getResourceTypeText(resource.type) }}
                  </el-tag>
                </div>
                <div class="resource-actions">
                  <el-button type="text" size="small" @click="viewResource(resource)">
                    查看
                  </el-button>
                  <el-button v-if="isTeacher" type="text" size="small" @click="editResource(resource)">
                    编辑
                  </el-button>
                  <el-button v-if="isTeacher" type="text" size="small" @click="deleteResource(resource)">
                    删除
                  </el-button>
                </div>
              </div>
              <div v-if="resource.description" class="resource-description" v-html="resource.description"></div>
            </div>
          </div>
        </div>
        <div v-else class="no-resources">
          <el-empty description="暂无章节资源" :image-size="100" />
        </div>
      </el-card>
    </div>

    <!-- 章节编辑对话框 -->
    <el-dialog v-model="chapterDialogVisible" title="编辑章节" width="50%">
      <el-form :model="chapterForm" label-width="80px" :rules="chapterRules" ref="chapterFormRef">
        <el-form-item label="章节名称" prop="title">
          <el-input v-model="chapterForm.title" placeholder="请输入章节名称" />
        </el-form-item>
        <el-form-item label="章节内容" prop="content">
          <div class="editor-toolbar-custom">
            <el-button size="small" @click="handleImageUpload" icon="Picture">插入图片</el-button>
            <el-button size="small" @click="handleVideoUpload" icon="VideoPlay">插入视频</el-button>
          </div>
          <QuillEditor 
            ref="chapterEditor"
            v-model:content="chapterForm.content"
            contentType="html"
            theme="snow"
            :options="chapterEditorOptions"
            style="height: 300px;"
          />
        </el-form-item>
        <el-form-item label="排序" prop="orderNum">
          <el-input-number v-model="chapterForm.orderNum" :min="1" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="chapterDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveChapter" :loading="savingChapter">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 资源上传对话框 -->
    <el-dialog v-model="resourceDialogVisible" :title="resourceForm.id ? '编辑资源' : '添加资源'" width="60%">
      <el-form :model="resourceForm" label-width="80px" :rules="resourceRules" ref="resourceFormRef">
        <el-form-item label="资源名称" prop="title">
          <el-input v-model="resourceForm.title" placeholder="请输入资源名称" />
        </el-form-item>
        <el-form-item label="资源类型" prop="type">
          <el-select v-model="resourceForm.type" placeholder="请选择资源类型">
            <el-option label="文档" value="document" />
            <el-option label="视频" value="video" />
            <el-option label="音频" value="audio" />
            <el-option label="图片" value="image" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="资源描述" prop="description">
          <QuillEditor 
            v-model:content="resourceForm.description"
            contentType="html"
            theme="snow"
            :options="editorOptions"
            style="height: 200px;"
          />
        </el-form-item>
        <el-form-item label="资源文件" :prop="resourceForm.id ? '' : 'file'">
          <el-upload 
            class="resource-uploader" 
            :auto-upload="false"
            :on-change="handleFileChange" 
            :limit="1"
          >
            <el-button type="primary">{{ resourceForm.id ? '重新选择文件' : '选择文件' }}</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持各种类型的文件，大小不超过150MB
                {{ resourceForm.id ? '（不选择文件则保持原文件不变）' : '' }}
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resourceDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveResource" :loading="savingResource">
            {{ resourceForm.id ? '更新' : '保存' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { courseApi } from '../../api/course';
import { useAuthStore } from '../../store/auth';
import { QuillEditor } from 'vue-quill-editor';
import 'quill/dist/quill.core.css';
import 'quill/dist/quill.snow.css';
import 'quill/dist/quill.bubble.css';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 路由参数
const courseId = computed(() => route.params.courseId);
const chapterId = computed(() => route.params.chapterId);

// 页面状态
const loading = ref(false);
const course = ref(null);
const chapter = ref(null);
const parentChapter = ref(null);
const subChapters = ref([]);
const resources = ref([]);

// 用户角色
const isTeacher = computed(() => authStore.hasPermission('teacher'));

// 章节对话框
const chapterDialogVisible = ref(false);
const chapterFormRef = ref(null);
const savingChapter = ref(false);
const chapterForm = reactive({
  id: '',
  courseId: '',
  parentId: '',
  title: '',
  content: '',
  orderNum: 1
});

// 资源对话框
const resourceDialogVisible = ref(false);
const resourceFormRef = ref(null);
const savingResource = ref(false);
const resourceForm = reactive({
  chapterId: '',
  title: '',
  type: '',
  description: '',
  url: '',
  file: null
});

// 表单验证规则
const chapterRules = {
  title: [
    { required: true, message: '请输入章节名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  orderNum: [
    { required: true, message: '请输入排序号', trigger: 'blur' },
    { type: 'number', min: 1, max: 999, message: '排序号必须在1-999之间', trigger: 'blur' }
  ]
};

const resourceRules = {
  title: [
    { required: true, message: '请输入资源名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择资源类型', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入资源描述', trigger: 'blur' }
  ]
};

// 上传文件的请求头
const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${localStorage.getItem('token')}`
  };
});

// 资源描述富文本编辑器配置
const editorOptions = {
  modules: {
    toolbar: [
      ['bold', 'italic', 'underline', 'strike'],
      ['blockquote', 'code-block'],
      [{ 'header': 1 }, { 'header': 2 }],
      [{ 'list': 'ordered'}, { 'list': 'bullet' }],
      [{ 'script': 'sub'}, { 'script': 'super' }],
      [{ 'indent': '-1'}, { 'indent': '+1' }],
      [{ 'direction': 'rtl' }],
      [{ 'size': ['small', false, 'large', 'huge'] }],
      [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
      [{ 'color': [] }, { 'background': [] }],
      [{ 'font': [] }],
      [{ 'align': [] }],
      ['clean'],
      ['link', 'image']
    ]
  },
  placeholder: '请输入资源描述...',
  readOnly: false,
  theme: 'snow'
};

// 章节内容富文本编辑器配置
const chapterEditorOptions = {
  modules: {
    toolbar: [
      ['bold', 'italic', 'underline', 'strike'],
      ['blockquote', 'code-block'],
      [{ 'header': 1 }, { 'header': 2 }],
      [{ 'list': 'ordered'}, { 'list': 'bullet' }],
      [{ 'script': 'sub'}, { 'script': 'super' }],
      [{ 'indent': '-1'}, { 'indent': '+1' }],
      [{ 'direction': 'rtl' }],
      [{ 'size': ['small', false, 'large', 'huge'] }],
      [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
      [{ 'color': [] }, { 'background': [] }],
      [{ 'font': [] }],
      [{ 'align': [] }],
      ['clean'],
      ['link']
    ]
  },
  placeholder: '请输入章节内容...',
  readOnly: false,
  theme: 'snow'
};

// 编辑器引用
const chapterEditor = ref(null);

// 处理图片上传
const handleImageUpload = () => {
  const input = document.createElement('input');
  input.setAttribute('type', 'file');
  input.setAttribute('accept', 'image/*');
  input.click();

  input.onchange = async () => {
    const file = input.files[0];
    if (file) {
      try {
        const formData = new FormData();
        formData.append('file', file);
        
        const response = await fetch('/api/resource/upload', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: formData
        });
        
        const result = await response.json();
        if (result.code === 200) {
          // 获取编辑器实例并插入图片
          const quill = chapterEditor.value?.getQuill();
          if (quill) {
            const range = quill.getSelection(true);
            quill.insertEmbed(range ? range.index : 0, 'image', result.data.url);
          }
          ElMessage.success('图片上传成功');
        } else {
          ElMessage.error('图片上传失败');
        }
      } catch (error) {
        console.error('图片上传失败:', error);
        ElMessage.error('图片上传失败');
      }
    }
  };
};

// 处理视频上传
const handleVideoUpload = () => {
  const input = document.createElement('input');
  input.setAttribute('type', 'file');
  input.setAttribute('accept', 'video/*');
  input.click();

  input.onchange = async () => {
    const file = input.files[0];
    if (file) {
      try {
        const formData = new FormData();
        formData.append('file', file);
        
        const response = await fetch('/api/resource/upload', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: formData
        });
        
        const result = await response.json();
        if (result.code === 200) {
          // 获取编辑器实例并插入视频
          const quill = chapterEditor.value?.getQuill();
          if (quill) {
            const range = quill.getSelection(true);
            const videoHtml = `<video controls style="max-width: 100%; height: auto;"><source src="${result.data.url}" type="video/mp4">您的浏览器不支持视频播放。</video>`;
            quill.clipboard.dangerouslyPasteHTML(range ? range.index : 0, videoHtml);
          }
          ElMessage.success('视频上传成功');
        } else {
          ElMessage.error('视频上传失败');
        }
      } catch (error) {
        console.error('视频上传失败:', error);
        ElMessage.error('视频上传失败');
      }
    }
  };
};



// 获取章节详情
const fetchChapterDetail = async () => {
  try {
    loading.value = true;
    
    // 获取课程信息和章节树
    const courseRes = await courseApi.getCourseDetail(courseId.value);
    course.value = courseRes.course;
    
    const chapterTreeRes = await courseApi.getChapterTree(courseId.value);
    const chapterTree = chapterTreeRes.data || [];
    
    // 在章节树中查找当前章节
    const findChapter = (chapters, targetId) => {
      for (const ch of chapters) {
        if (ch.id == targetId) {
          return ch;
        }
        if (ch.children && ch.children.length > 0) {
          const found = findChapter(ch.children, targetId);
          if (found) return found;
        }
      }
      return null;
    };
    
    chapter.value = findChapter(chapterTree, chapterId.value);
    
    // 如果有父章节，查找父章节信息
    if (chapter.value && chapter.value.parentId && chapter.value.parentId !== 0) {
      parentChapter.value = findChapter(chapterTree, chapter.value.parentId);
    }
    
    // 获取子章节（从当前章节的children中获取非资源项）
    if (chapter.value && chapter.value.children) {
      subChapters.value = chapter.value.children.filter(child => !child.url);
    }
    
    // 获取章节资源
    await fetchResources();
  } catch (error) {
    console.error('获取章节详情失败:', error);
    ElMessage.error('获取章节详情失败');
  } finally {
    loading.value = false;
  }
};

// 获取子章节（已在fetchChapterDetail中处理）
const fetchSubChapters = async () => {
  // 子章节已在fetchChapterDetail中从章节树获取
  // 这里保留函数以保持兼容性
};

// 获取章节资源
const fetchResources = async () => {
  try {
    const res = await courseApi.getResourceList(chapterId.value);
    resources.value = res.data || [];
  } catch (error) {
    console.error('获取章节资源失败:', error);
  }
};

// 返回课程详情页
const goBack = () => {
  router.push(`/course/${courseId.value}`);
};

// 查看子章节
const viewSubChapter = (subChapter) => {
  router.push(`/course/${courseId.value}/chapter/${subChapter.id}`);
};

// 编辑章节
const editChapter = () => {
  chapterForm.id = chapter.value.id;
  chapterForm.courseId = chapter.value.courseId;
  chapterForm.parentId = chapter.value.parentId;
  chapterForm.title = chapter.value.title;
  chapterForm.content = chapter.value.content || '';
  chapterForm.orderNum = chapter.value.orderNum;
  chapterDialogVisible.value = true;
};

// 编辑子章节
const editSubChapter = (subChapter) => {
  chapterForm.id = subChapter.id;
  chapterForm.courseId = subChapter.courseId;
  chapterForm.parentId = subChapter.parentId;
  chapterForm.title = subChapter.title;
  chapterForm.content = subChapter.content || '';
  chapterForm.orderNum = subChapter.orderNum;
  chapterDialogVisible.value = true;
};

// 添加子章节
const addSubChapter = () => {
  chapterForm.id = '';
  chapterForm.courseId = courseId.value;
  chapterForm.parentId = chapterId.value;
  chapterForm.title = '';
  chapterForm.content = '';
  chapterForm.orderNum = subChapters.value.length + 1;
  chapterDialogVisible.value = true;
};

// 保存章节
const saveChapter = async () => {
  if (!chapterFormRef.value) return;

  await chapterFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        savingChapter.value = true;
        if (chapterForm.id) {
          // 更新章节
          await courseApi.updateChapter(chapterForm.id, chapterForm);
          ElMessage.success('章节更新成功');
        } else {
          // 创建章节
          await courseApi.createChapter(chapterForm);
          ElMessage.success('章节创建成功');
        }
        chapterDialogVisible.value = false;
        // 刷新页面数据
        await fetchChapterDetail();
      } catch (error) {
        console.error('保存章节失败:', error);
        ElMessage.error('保存章节失败');
      } finally {
        savingChapter.value = false;
      }
    }
  });
};

// 删除子章节
const deleteSubChapter = async (subChapter) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该子章节吗？删除后将无法恢复。',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    await courseApi.deleteChapter(subChapter.id);
    ElMessage.success('子章节删除成功');
    // 刷新子章节列表
    await fetchSubChapters();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除子章节失败:', error);
      ElMessage.error('删除子章节失败');
    }
  }
};

// 添加资源
const addResource = () => {
  resourceForm.chapterId = chapterId.value;
  resourceForm.title = '';
  resourceForm.type = '';
  resourceForm.description = '';
  resourceForm.url = '';
  resourceForm.file = null;
  resourceDialogVisible.value = true;
};

// 编辑资源
const editResource = (resource) => {
  resourceForm.id = resource.id;
  resourceForm.chapterId = resource.chapterId;
  resourceForm.title = resource.title;
  resourceForm.type = resource.type;
  resourceForm.description = resource.description || '';
  resourceForm.url = resource.url;
  resourceForm.file = null;
  resourceDialogVisible.value = true;
};

// 上传资源前的验证
const beforeResourceUpload = (file) => {
  const isLt150M = file.size / 1024 / 1024 < 150;
  if (!isLt150M) {
    ElMessage.error('上传文件大小不能超过 150MB!');
  }
  return isLt150M;
};

// 文件选择回调
const handleFileChange = (file) => {
  const isLt150M = file.size / 1024 / 1024 < 150;
  if (!isLt150M) {
    ElMessage.error('上传文件大小不能超过 150MB!');
    return false;
  }
  resourceForm.file = file.raw;
  ElMessage.success('文件选择成功');
};

// 保存资源
const saveResource = async () => {
  if (!resourceFormRef.value) return;

  await resourceFormRef.value.validate(async (valid) => {
    if (valid) {
      if (!resourceForm.file) {
        ElMessage.warning('请先选择资源文件');
        return;
      }

      try {
        savingResource.value = true;
        if (resourceForm.id) {
          // 更新资源
          const updateData = {
            title: resourceForm.title,
            type: resourceForm.type,
            description: resourceForm.description
          };
          
          if (resourceForm.file) {
            // 如果有新文件，需要重新上传
            const formData = new FormData();
            formData.append('file', resourceForm.file);
            formData.append('chapterId', resourceForm.chapterId);
            formData.append('title', resourceForm.title);
            formData.append('description', resourceForm.description);
            await courseApi.uploadResource(formData);
          } else {
            // 只更新资源信息
            await courseApi.updateResource(resourceForm.id, updateData);
          }
        } else {
          // 创建FormData对象
          const formData = new FormData();
          formData.append('file', resourceForm.file);
          formData.append('chapterId', resourceForm.chapterId);
          formData.append('title', resourceForm.title);
          formData.append('description', resourceForm.description);
          
          // 上传资源文件并保存记录
          await courseApi.uploadResource(formData);
        }
        ElMessage.success(resourceForm.id ? '资源更新成功' : '资源添加成功');
        resourceDialogVisible.value = false;
        // 刷新资源列表
        await fetchResources();
      } catch (error) {
        console.error('保存资源失败:', error);
        ElMessage.error('保存资源失败');
      } finally {
        savingResource.value = false;
      }
    }
  });
};

// 查看资源
const viewResource = (resource) => {
  window.open(resource.url, '_blank');
};

// 删除资源
const deleteResource = async (resource) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该资源吗？删除后将无法恢复。',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    await courseApi.deleteResource(resource.id);
    ElMessage.success('资源删除成功');
    // 刷新资源列表
    await fetchResources();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除资源失败:', error);
      ElMessage.error('删除资源失败');
    }
  }
};

// 获取资源类型颜色
const getResourceTypeColor = (type) => {
  const colorMap = {
    'document': 'primary',
    'video': 'success',
    'audio': 'warning',
    'image': 'info',
    'other': 'default'
  };
  return colorMap[type] || 'default';
};

// 获取资源类型文本
const getResourceTypeText = (type) => {
  const textMap = {
    'document': '文档',
    'video': '视频',
    'audio': '音频',
    'image': '图片',
    'other': '其他'
  };
  return textMap[type] || '未知';
};

// 页面初始化
onMounted(() => {
  fetchChapterDetail();
});
</script>

<style scoped>
.chapter-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.back-section {
  margin-bottom: 20px;
}

.back-btn {
  font-size: 16px;
  color: #409eff;
  padding: 8px 0;
}

.back-btn:hover {
  color: #66b1ff;
}

.chapter-header {
  background: white;
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.chapter-title {
  font-size: 28px;
  color: #333;
  margin: 0 0 16px 0;
  font-weight: 600;
}

.chapter-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  color: #666;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.meta-item i {
  color: #409eff;
}

.chapter-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.content-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
}

.chapter-description {
  padding: 20px 0;
  line-height: 1.8;
  color: #555;
  font-size: 15px;
}

.no-description {
  padding: 40px 0;
  text-align: center;
}

.sub-chapters-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sub-chapter-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e9ecef;
}

.sub-chapter-item:hover {
  background: #e3f2fd;
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.sub-chapter-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.sub-chapter-info i {
  color: #409eff;
  font-size: 16px;
}

.sub-chapter-title {
  font-weight: 500;
  color: #333;
  font-size: 15px;
}

.sub-chapter-order {
  color: #999;
  font-size: 13px;
  margin-left: auto;
  margin-right: 20px;
}

.sub-chapter-actions {
  display: flex;
  gap: 8px;
}

.resources-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.resource-item {
  padding: 16px 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  transition: all 0.3s ease;
}

.resource-item:hover {
  background: #fff3e0;
  border-color: #ff9800;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 152, 0, 0.15);
}

.resource-content {
  width: 100%;
}

.resource-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.resource-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.resource-info i {
  color: #ff9800;
  font-size: 16px;
}

.resource-title {
  font-weight: 500;
  color: #333;
  font-size: 15px;
}

.resource-actions {
  display: flex;
  gap: 8px;
}

.resource-description {
  margin-top: 12px;
  padding: 12px;
  background: #ffffff;
  border-radius: 6px;
  border: 1px solid #e1e8ed;
  font-size: 14px;
  line-height: 1.6;
  color: #555;
}

.resource-description :deep(p) {
  margin: 0 0 8px 0;
}

.resource-description :deep(p:last-child) {
  margin-bottom: 0;
}

.resource-description :deep(h1),
.resource-description :deep(h2),
.resource-description :deep(h3),
.resource-description :deep(h4),
.resource-description :deep(h5),
.resource-description :deep(h6) {
  margin: 12px 0 8px 0;
  color: #333;
}

.resource-description :deep(ul),
.resource-description :deep(ol) {
  margin: 8px 0;
  padding-left: 20px;
}

.resource-description :deep(blockquote) {
  margin: 8px 0;
  padding: 8px 12px;
  background: #f5f5f5;
  border-left: 4px solid #409eff;
  color: #666;
}

.resource-description :deep(code) {
  background: #f1f2f3;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
}

.resource-description :deep(pre) {
  background: #f8f8f8;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 8px 0;
}

/* 自定义编辑器工具栏样式 */
.editor-toolbar-custom {
  margin-bottom: 10px;
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

.editor-toolbar-custom .el-button {
  margin-right: 8px;
}

/* 富文本编辑器样式优化 */
:deep(.ql-editor) {
  min-height: 150px;
  font-size: 14px;
  line-height: 1.6;
}

:deep(.ql-toolbar) {
  border-top: 1px solid #ccc;
  border-left: 1px solid #ccc;
  border-right: 1px solid #ccc;
  border-bottom: none;
}

:deep(.ql-container) {
  border-bottom: 1px solid #ccc;
  border-left: 1px solid #ccc;
  border-right: 1px solid #ccc;
  border-top: none;
}

.no-resources {
  padding: 40px 0;
  text-align: center;
}

.dialog-footer {
  margin-top: 20px;
  text-align: right;
}

.resource-uploader {
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chapter-detail-container {
    padding: 10px;
  }
  
  .chapter-header {
    padding: 20px;
  }
  
  .chapter-title {
    font-size: 24px;
  }
  
  .chapter-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .sub-chapter-item,
  .resource-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .sub-chapter-actions,
  .resource-actions {
    align-self: flex-end;
  }
  
  .dialog-footer {
    text-align: center;
  }
  
  .dialog-footer :deep(.el-button) {
    width: 100%;
    margin-bottom: 8px;
  }
}

/* 动画效果 */
.content-card {
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.sub-chapter-item,
.resource-item {
  animation: slideInLeft 0.4s ease-out;
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
</style>