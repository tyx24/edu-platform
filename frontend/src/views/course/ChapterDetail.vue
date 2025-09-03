<template>
    <div class="chapter-detail-page">
        <div class="page-header">

            <div class="breadcrumb">
                <el-breadcrumb separator=">">
                    <el-breadcrumb-item @click="goBack">课程详情</el-breadcrumb-item>
                    <el-breadcrumb-item>章节详情</el-breadcrumb-item>
                </el-breadcrumb>
            </div>
        </div>

        <el-row :gutter="24" class="main-content">

            <!-- 左侧章节详情 -->
            <el-col :span="16">
                <!-- 可滚动内容区域 -->
                <div class="scrollable-content">
                    <div class="content-card">
                        <div class="chapter-header">
                            <div class="title-section">
                                <h1 class="chapter-title">{{ chapterDetail.title || '章节标题' }}</h1>
                                <div class="chapter-meta">
                                    <el-tag type="info" size="small">章节 ID: {{ chapterDetail.id }}</el-tag>
                                </div>
                            </div>
                            <div class="action-section" v-if="canEdit">
                                <el-button type="primary" @click="startEditContent" size="large">
                                    <el-icon>
                                        <Edit />
                                    </el-icon>
                                    编辑章节内容
                                </el-button>
                                <el-button type="danger" @click="deleteChapter" :loading="deletingChapter" size="large">
                                    <el-icon>
                                        <Delete />
                                    </el-icon>
                                    删除章节
                                </el-button>
                            </div>
                        </div>

                        <!-- 章节内容显示 -->
                        <div class="content-section" v-if="!isEditingContent">
                            <div class="section-header">
                                <h3><el-icon>
                                        <DocumentAdd />
                                    </el-icon>章节内容</h3>
                            </div>
                            <div class="chapter-content" v-html="chapterDetail.content || '暂无内容'">
                            </div>
                        </div>

                        <!-- 富文本编辑器 -->
                        <div class="editor-section" v-if="isEditingContent && canEdit">
                            <div class="section-header">
                                <h3><el-icon>
                                        <Edit />
                                    </el-icon>章节内容编辑</h3>
                            </div>
                            <div class="editor-container">
                                <QuillEditor ref="contentEditor" v-model:content="editingContent" contentType="html"
                                    theme="snow" :options="contentEditorOptions" class="custom-editor" />
                            </div>
                            <div class="content-edit-actions">
                                <el-button type="primary" @click="saveContentChanges" :loading="savingContent">
                                    保存内容
                                </el-button>
                                <el-button @click="cancelEditContent">
                                    取消编辑
                                </el-button>
                            </div>
                        </div>


                    </div>
                </div>
            </el-col>

            <!-- 右侧章节目录 -->
            <el-col :span="8">
                <div class="sidebar-card">
                    <div class="sidebar-header">
                        <h3><el-icon>
                                <Menu />
                            </el-icon>课程目录</h3>
                    </div>
                    <div class="tree-container">
                        <el-tree :data="chapterTree" :props="defaultProps" node-key="id"
                            :default-expanded-keys="[chapterDetail.id]" :highlight-current="true"
                            :expand-on-click-node="false" @node-click="handleChapterClick" class="custom-tree" />
                    </div>
                </div>
            </el-col>
        </el-row>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { DocumentAdd, Edit, Delete, Menu } from '@element-plus/icons-vue';
import { courseApi } from '../../api/course';
import { useAuthStore } from '../../store/auth';
import { QuillEditor, Quill } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css';
import hljs from 'highlight.js';
import 'highlight.js/styles/github.css';
import "../../utils/VideoBlot";

// 注册自定义附件按钮
const icons = Quill.import('ui/icons');
icons['attachment'] = '🗃️';

// 路由
const route = useRoute();
const router = useRouter();
const courseId = route.params.courseId;
const chapterId = route.params.chapterId;
const authStore = useAuthStore();

// 返回课程详情页
const goBack = () => {
    router.push(`/course/${courseId}`);
};

// 权限控制
const canEdit = computed(() => {
    return authStore.hasPermission('teacher') || authStore.hasPermission('admin');
});

// 章节详情
const chapterDetail = reactive({
    id: null,
    courseId: null,
    title: '',
    content: '',
    parentId: null,
    orderNum: 0
});

// 编辑状态
const isEditingContent = ref(false);
const editingContent = ref('');
const savingContent = ref(false);
const originalContent = ref(''); // 保存原始内容用于对比
const currentResources = ref([]); // 当前章节的资源列表

// 处理图片上传
const handleContentImageUpload = () => {
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
                formData.append('chapterId', chapterDetail.id);
                // 移除type参数，后端根据文件扩展名自动判断类型
                // formData.append('type', 'image');

                const res = await courseApi.uploadResource(formData);
                const imageUrl = res.data.url;

                // 插入图片到编辑器
                const quill = contentEditor.value.getQuill();
                const range = quill.getSelection();
                quill.insertEmbed(range ? range.index : 0, 'image', imageUrl);

                ElMessage.success('图片上传成功');
            } catch (error) {
                console.error('图片上传失败:', error);
                ElMessage.error('图片上传失败');
            }
        }
    };
};

// 处理视频上传
const handleContentVideoUpload = () => {
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
                formData.append('chapterId', chapterDetail.id);
                // 移除type参数，后端根据文件扩展名自动判断类型
                // formData.append('type', 'video');

                const res = await courseApi.uploadResource(formData);
                const videoUrl = res.data.url;
                console.log('视频上传成功:', videoUrl);

                // 插入视频到编辑器
                const quill = contentEditor.value.getQuill();
                const range = quill.getSelection();
                
                quill.insertEmbed(range ? range.index : 0, "video", videoUrl);
                quill.setSelection(range.index + 1);
                ElMessage.success('视频上传成功');
            } catch (error) {
                console.error('视频上传失败:', error);
                ElMessage.error('视频上传失败');
            }
        }
    };
};

// 处理附件上传（PPT、Word、Excel等）
const handleContentAttachmentUpload = () => {
  const input = document.createElement('input');
  input.setAttribute('type', 'file');
  input.setAttribute('accept', '.ppt,.pptx,.doc,.docx,.xls,.xlsx,.pdf,.txt,.zip,.rar');
  input.click();

  input.onchange = async () => {
    const file = input.files[0];
    if (file) {
      try {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('chapterId', chapterDetail.id);
        formData.append('title', file.name); // 添加文件标题
        // 移除type参数，后端根据文件扩展名自动判断类型
        // formData.append('type', 'attachment');

        const res = await courseApi.uploadResource(formData);

        // OSS返回的是完整的预签名URL，可以直接用于下载
        const fileUrl = res.data.url;
        const fileName = file.name;

        const quill = contentEditor.value.getQuill();
        const range = quill.getSelection();

        // 根据文件类型决定插入方式
        const fileExt = fileName.split('.').pop().toLowerCase();
        
        // 由于OSS预签名URL在iframe中可能存在CORS问题，统一使用下载链接
        // 用户可以点击链接在新窗口中预览或下载文件
        let linkIcon = '📎'; // 默认附件图标
        if (['pdf'].includes(fileExt)) {
          linkIcon = '📄'; // PDF图标
        } else if (['doc', 'docx'].includes(fileExt)) {
          linkIcon = '📝'; // Word图标
        } else if (['xls', 'xlsx'].includes(fileExt)) {
          linkIcon = '📊'; // Excel图标
        } else if (['ppt', 'pptx'].includes(fileExt)) {
          linkIcon = '📽️'; // PPT图标
        } else if (['zip', 'rar'].includes(fileExt)) {
          linkIcon = '🗜️'; // 压缩包图标
        }
        
        const linkHtml = `<a href="${fileUrl}" download="${fileName}" class="attachment-link" target="_blank" rel="noopener noreferrer">${linkIcon} ${fileName}</a>`;
        quill.clipboard.dangerouslyPasteHTML(range ? range.index : 0, linkHtml);

        ElMessage.success('附件上传成功');
      } catch (error) {
        console.error('附件上传失败:', error);
        ElMessage.error('附件上传失败');
      }
    }
  };
};


// 富文本编辑器配置
const contentEditor = ref(null);
const contentEditorOptions = {
    modules: {
        toolbar: {
            container: [
                ['bold', 'italic', 'underline', 'strike'],
                [{ 'list': 'ordered' }, { 'list': 'bullet' }],
                ['blockquote', 'code-block'],
                [{ 'font': [] }, { 'size': [] }],
                [{ 'color': [] }, { 'background': [] }],
                [{ 'align': [] }],
                ['link', 'image', 'video', 'attachment'],
                ['clean']
            ],
            handlers: {
                'image': handleContentImageUpload,
                'video': handleContentVideoUpload,
                'attachment': handleContentAttachmentUpload
            }
        },
        syntax: {
            highlight: (text, language) => {
                if (language && hljs.getLanguage(language)) {
                    return hljs.highlight(text, { language }).value;
                }
                return hljs.highlightAuto(text).value;
            }
        }
    },
    placeholder: '请输入章节内容...',
    readOnly: false,
    theme: 'snow'
};

// 内容编辑方法
const startEditContent = async () => {
    if (!canEdit.value) {
        ElMessage.warning('您没有编辑权限');
        return;
    }
    isEditingContent.value = true;
    editingContent.value = chapterDetail.content || '';
    originalContent.value = chapterDetail.content || '';

    // 加载当前章节的资源列表
    try {
        const res = await courseApi.getResourceList(chapterDetail.id);
        currentResources.value = res.data || [];
    } catch (error) {
        console.error('加载资源列表失败:', error);
        currentResources.value = [];
    }
};

const cancelEditContent = () => {
    isEditingContent.value = false;
    editingContent.value = '';
};

const saveContentChanges = async () => {
    try {
        savingContent.value = true;

        // 同步资源变化
        await syncResourceChanges();

        const updateData = {
            ...chapterDetail,
            content: editingContent.value
        };
        await courseApi.updateChapter(chapterDetail.id, updateData);
        chapterDetail.content = editingContent.value;
        isEditingContent.value = false;
        ElMessage.success('章节内容保存成功');
    } catch (error) {
        console.error('保存内容失败:', error);
        ElMessage.error('保存内容失败');
    } finally {
        savingContent.value = false;
    }
};

// 同步资源变化
const syncResourceChanges = async () => {
    try {
        // 提取原始内容中的媒体URL
        const originalUrls = extractMediaUrls(originalContent.value);
        // 提取当前编辑内容中的媒体URL
        const currentUrls = extractMediaUrls(editingContent.value);

        // 找出新增的URL
        const addedUrls = currentUrls.filter(url => !originalUrls.includes(url));
        // 找出删除的URL
        const removedUrls = originalUrls.filter(url => !currentUrls.includes(url));

        // 处理删除的资源
        for (const url of removedUrls) {
            // OSS URL匹配：直接匹配完整URL
            const resources = currentResources.value.filter(r => r.url === url);
            
            if (resources.length === 0) {
                console.warn('未找到匹配的资源记录:', url);
                continue;
            }
            
            for (const resource of resources) {
                try {
                    await courseApi.deleteResource(resource.id);
                    console.log('已删除资源:', url);
                } catch (error) {
                    console.error('删除资源失败:', url, error);
                }
            }
        }

        // 新增的资源已经在上传时自动保存到资源表，无需额外处理
        if (addedUrls.length > 0) {
            console.log('新增资源:', addedUrls);
        }

    } catch (error) {
        console.error('同步资源变化失败:', error);
    }
};

// 提取内容中的媒体URL
const extractMediaUrls = (content) => {
    const urls = [];
    if (!content) return urls;

    // 提取图片URL
    const imgRegex = /<img[^>]+src="([^"]+)"/g;
    let match;
    while ((match = imgRegex.exec(content)) !== null) {
        // OSS返回的是完整URL，直接使用
        const url = match[1];
        urls.push(url);
    }

    // 提取视频URL（从video标签的src属性或source子标签中）
    const videoRegex = /<video[^>]+src="([^"]+)"/g;
    while ((match = videoRegex.exec(content)) !== null) {
        const url = match[1];
        urls.push(url);
    }
    
    // 提取video标签内source元素的src属性
    const sourceRegex = /<source[^>]+src="([^"]+)"/g;
    while ((match = sourceRegex.exec(content)) !== null) {
        // OSS返回的是完整URL，直接使用
        const url = match[1];
        urls.push(url);
    }

    // 提取iframe中的URL
    const iframeRegex = /<iframe[^>]+src="([^"]+)"/g;
    while ((match = iframeRegex.exec(content)) !== null) {
        // OSS返回的是完整URL，直接使用
        const url = match[1];
        urls.push(url);
    }

    // 提取附件下载链接URL
    const attachmentRegex = /<a[^>]+href="([^"]+)"[^>]*class="attachment-link"[^>]*>/g;
    while ((match = attachmentRegex.exec(content)) !== null) {
        // OSS返回的是完整URL，直接使用
        const url = match[1];
        urls.push(url);
    }

    return urls;
};

// 删除章节
const deletingChapter = ref(false);
const deleteChapter = async () => {
    if (!canEdit.value) {
        ElMessage.warning('您没有删除权限');
        return;
    }

    try {
        await ElMessageBox.confirm('确定要删除这个章节吗？', '确认删除', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        });

        deletingChapter.value = true;
        await courseApi.deleteChapter(chapterDetail.id);
        ElMessage.success('章节删除成功');
        // 返回课程详情页
        goBack();
    } catch (error) {
        if (error !== 'cancel') {
            console.error('删除章节失败:', error);
            ElMessage.error('删除章节失败');
        }
    } finally {
        deletingChapter.value = false;
    }
};

// 章节树
const chapterTree = ref([]);
const defaultProps = {
    children: 'children',
    label: 'title'
};

const loadChapterTree = async () => {
    const res = await courseApi.getChapterTree(courseId);
    chapterTree.value = res.data || [];
};

const handleChapterClick = (node) => {
    if (node.id === chapterDetail.id) return;
    loadChapterDetail(node.id);
};

// 加载章节详情
const loadChapterDetail = async (id) => {
    const res = await courseApi.getChapterDetail(id);
    Object.assign(chapterDetail, res.data);
};

// 初始化
onMounted(async () => {
    await loadChapterTree();
    await loadChapterDetail(Number(chapterId));
});
</script>

<style scoped>
.chapter-detail-page {
    min-height: 100vh;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    background-attachment: fixed;
    padding: 0;
    position: relative;
}

.chapter-detail-page::before {
    content: '';
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="dots" width="20" height="20" patternUnits="userSpaceOnUse"><circle cx="10" cy="10" r="1.5" fill="%23ffffff" opacity="0.1"/></pattern></defs><rect width="100" height="100" fill="url(%23dots)"/></svg>') repeat;
    pointer-events: none;
    z-index: 0;
}

.page-header {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(15px);
    padding: 20px 32px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    position: relative;
    z-index: 10;
    transition: all 0.3s ease;
}

.page-header:hover {
    background: rgba(255, 255, 255, 0.98);
    box-shadow: 0 6px 30px rgba(0, 0, 0, 0.12);
}

.breadcrumb {
    font-size: 14px;
}

.main-content {
    padding: 32px 24px;
    position: relative;
    z-index: 1;
}

.content-card {
    background: #ffffff;
    border-radius: 20px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08);
    overflow: hidden;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    position: relative;
}

.content-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, #4facfe 0%, #00f2fe 50%, #667eea 100%);
    z-index: 1;
}

.content-card:hover {
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.12);
    transform: translateY(-4px) scale(1.01);
}

.chapter-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding: 40px;
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 50%, #667eea 100%);
    color: white;
    position: relative;
    overflow: hidden;
    margin-top: 4px;
}

.chapter-header::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="50" cy="50" r="1" fill="%23ffffff" opacity="0.1"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>') repeat;
    opacity: 0.3;
}

.title-section {
    position: relative;
    z-index: 1;
}

.chapter-title {
    font-size: 32px;
    font-weight: 800;
    margin: 0 0 16px 0;
    text-shadow: 0 3px 6px rgba(0, 0, 0, 0.3);
    letter-spacing: -0.5px;
    line-height: 1.2;
    background: linear-gradient(45deg, #ffffff 0%, #f0f9ff 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.chapter-meta {
    margin-top: 8px;
}

.action-section {
    position: relative;
    z-index: 1;
}

.editor-section {
    padding: 32px;
    border-bottom: 1px solid #f0f0f0;
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}

.section-header h3 {
    font-size: 20px;
    font-weight: 700;
    color: #1e293b;
    margin: 0;
    display: flex;
    align-items: center;
    gap: 12px;
    position: relative;
    padding-left: 16px;
}

.section-header h3::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 4px;
    height: 24px;
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    border-radius: 2px;
}

.editor-container {
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    border: 2px solid #e2e8f0;
    transition: all 0.3s ease;
}

.editor-container:hover {
    border-color: #4facfe;
    box-shadow: 0 12px 32px rgba(79, 172, 254, 0.15);
}

.custom-editor {
    height: 500px;
    border: none;
    font-size: 16px;
    line-height: 1.6;
}

:deep(.ql-toolbar) {
    background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
    border-bottom: 2px solid #e2e8f0;
    padding: 12px 16px;
}

:deep(.ql-container) {
    background: #ffffff;
    font-size: 16px;
    line-height: 1.8;
}

:deep(.ql-editor) {
    padding: 24px;
    min-height: 400px;
}

/* 代码块样式优化 */
:deep(.ql-syntax) {
    background: #f6f8fa;
    border: 1px solid #e1e4e8;
    border-radius: 8px;
    padding: 16px;
    margin: 16px 0;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    font-size: 14px;
    line-height: 1.5;
    overflow-x: auto;
    position: relative;
}

:deep(.ql-syntax::before) {
    content: 'Code';
    position: absolute;
    top: 8px;
    right: 12px;
    background: #667eea;
    color: white;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 600;
}

:deep(.ql-code-block-container) {
    position: relative;
}

/* 语法高亮样式 */
:deep(.hljs) {
    background: #f6f8fa !important;
    color: #24292e;
}

:deep(.hljs-keyword) {
    color: #d73a49;
    font-weight: 600;
}

:deep(.hljs-string) {
    color: #032f62;
}

:deep(.hljs-comment) {
    color: #6a737d;
    font-style: italic;
}

:deep(.hljs-number) {
    color: #005cc5;
}

:deep(.hljs-function) {
    color: #6f42c1;
}

:deep(.hljs-variable) {
    color: #e36209;
}

/* 编辑器中的图片样式 */
:deep(.ql-editor img) {
    max-width: 800px;
    width: auto;
    height: auto;
    border-radius: 8px;
    margin: 16px 0;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease;
    cursor: pointer;
}

:deep(.ql-editor img:hover) {
    transform: scale(1.02);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}



/* 响应式编辑器图片 */
@media (max-width: 768px) {
    :deep(.ql-editor img) {
        max-width: 100%;
    }
}

/* 附件链接样式 */
.chapter-content .attachment-link {
    display: inline-flex;
    align-items: center;
    padding: 8px 16px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    text-decoration: none;
    border-radius: 8px;
    margin: 8px 4px;
    font-size: 14px;
    font-weight: 500;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.chapter-content .attachment-link:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
    text-decoration: none;
    color: white;
}

:deep(.ql-editor .attachment-link) {
    display: inline-flex;
    align-items: center;
    padding: 8px 16px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    text-decoration: none;
    border-radius: 8px;
    margin: 8px 4px;
    font-size: 14px;
    font-weight: 500;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

:deep(.ql-editor .attachment-link:hover) {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
    text-decoration: none;
    color: white;
}

.resources-section {
    padding: 32px;
}

.upload-box {
    margin-left: auto;
}

.resources-table {
    margin-top: 24px;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.custom-table {
    border-radius: 12px;
}

.resource-link {
    display: flex;
    align-items: center;
    gap: 6px;
    text-decoration: none;
    max-width: 300px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.sidebar-card {
    background: #ffffff;
    border-radius: 20px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08);
    overflow: hidden;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    height: fit-content;
    position: sticky;
    top: 32px;
    border: 1px solid rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
}

.sidebar-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
    z-index: 1;
}

.sidebar-card:hover {
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.12);
    transform: translateY(-4px) scale(1.02);
}

.sidebar-header {
    padding: 32px 24px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    position: relative;
    overflow: hidden;
    margin-top: 4px;
}

.sidebar-header::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="dots" width="20" height="20" patternUnits="userSpaceOnUse"><circle cx="10" cy="10" r="1" fill="%23ffffff" opacity="0.2"/></pattern></defs><rect width="100" height="100" fill="url(%23dots)"/></svg>') repeat;
}

.sidebar-header h3 {
    font-size: 18px;
    font-weight: 600;
    margin: 0;
    display: flex;
    align-items: center;
    gap: 8px;
    position: relative;
    z-index: 1;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.tree-container {
    padding: 24px;
    max-height: 600px;
    overflow-y: auto;
}

.custom-tree {
    background: transparent;
}

/* 自定义滚动条 */
.tree-container::-webkit-scrollbar {
    width: 6px;
}

.tree-container::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
}

.tree-container::-webkit-scrollbar-thumb {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 3px;
}

.tree-container::-webkit-scrollbar-thumb:hover {
    background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
}

/* Element Plus 组件样式覆盖 */
:deep(.el-button) {
    border-radius: 12px;
    font-weight: 600;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    padding: 12px 24px;
    font-size: 14px;
    letter-spacing: 0.5px;
    position: relative;
    overflow: hidden;
}

:deep(.el-button::before) {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    transition: left 0.5s;
}

:deep(.el-button:hover::before) {
    left: 100%;
}

:deep(.el-button:hover) {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

:deep(.el-button--primary) {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    border: none;
    box-shadow: 0 4px 15px rgba(79, 172, 254, 0.3);
}

:deep(.el-button--primary:hover) {
    background: linear-gradient(135deg, #3b9cfd 0%, #00e5fd 100%);
    box-shadow: 0 8px 25px rgba(79, 172, 254, 0.4);
}

:deep(.el-button--danger) {
    background: linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%);
    border: none;
    box-shadow: 0 4px 15px rgba(255, 107, 107, 0.3);
}

:deep(.el-button--danger:hover) {
    background: linear-gradient(135deg, #ff5252 0%, #e53e3e 100%);
    box-shadow: 0 8px 25px rgba(255, 107, 107, 0.4);
}

:deep(.el-table) {
    border-radius: 12px;
}

:deep(.el-table th) {
    background: #f8fafc;
    color: #475569;
    font-weight: 600;
    border: none;
}

:deep(.el-table td) {
    border: none;
    padding: 16px 12px;
}

:deep(.el-table tr:hover > td) {
    background: #f1f5f9;
}

:deep(.el-tree-node__content) {
    padding: 12px 16px;
    border-radius: 12px;
    margin: 4px 0;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;
}

:deep(.el-tree-node__content::before) {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    width: 4px;
    height: 100%;
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    transform: scaleY(0);
    transition: transform 0.3s ease;
}

:deep(.el-tree-node__content:hover) {
    background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
    transform: translateX(8px);
    box-shadow: 0 4px 12px rgba(79, 172, 254, 0.1);
}

:deep(.el-tree-node__content:hover::before) {
    transform: scaleY(1);
}

:deep(.el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content) {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    font-weight: 600;
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.3);
    transform: translateX(8px);
}

:deep(.el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content::before) {
    background: rgba(255, 255, 255, 0.3);
    transform: scaleY(1);
}

:deep(.el-tag) {
    border-radius: 20px;
    font-weight: 600;
    padding: 6px 16px;
    font-size: 12px;
    letter-spacing: 0.5px;
    background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
    color: #0369a1;
    border: 1px solid #7dd3fc;
    box-shadow: 0 2px 4px rgba(3, 105, 161, 0.1);
}

:deep(.el-breadcrumb__inner) {
    color: #64748b;
    font-weight: 500;
}

:deep(.el-breadcrumb__inner:hover) {
    color: #3b82f6;
}

/* 动画效果 */
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

@keyframes slideInRight {
    from {
        opacity: 0;
        transform: translateX(30px);
    }

    to {
        opacity: 1;
        transform: translateX(0);
    }
}

@keyframes pulse {

    0%,
    100% {
        transform: scale(1);
    }

    50% {
        transform: scale(1.05);
    }
}

.content-card {
    animation: fadeInUp 0.6s ease-out;
}

.sidebar-card {
    animation: slideInRight 0.6s ease-out 0.2s both;
}

.chapter-header {
    animation: fadeInUp 0.6s ease-out 0.1s both;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .main-content {
        padding: 16px;
    }

    .chapter-header {
        flex-direction: column;
        gap: 20px;
        align-items: stretch;
        padding: 32px 24px;
    }

    .chapter-title {
        font-size: 28px;
    }

    .editor-section,
    .content-section {
        padding: 24px 20px;
    }

    .sidebar-card {
        position: relative;
        top: auto;
        margin-top: 24px;
    }

    .content-edit-actions {
        flex-direction: column;
        gap: 12px;
    }

    .content-edit-actions .el-button {
        width: 100%;
    }
}

/* 内容显示区域 */
.content-section {
    margin-bottom: 24px;
}

.chapter-content {
    min-height: 200px;
    padding: 32px;
    background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
    border-radius: 16px;
    border: 1px solid #e2e8f0;
    line-height: 1.8;
    font-size: 16px;
    color: #334155;
    box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.02);
    transition: all 0.3s ease;
    position: relative;
}

.chapter-content::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
    border-radius: 16px 16px 0 0;
}

.chapter-content:hover {
    background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
    box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.04), 0 4px 12px rgba(0, 0, 0, 0.05);
}

.chapter-content img {
    max-width: 800px;
    width: auto;
    height: auto;
    border-radius: 8px;
    margin: 16px 0;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease;
    cursor: pointer;
}

.chapter-content img:hover {
    transform: scale(1.02);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

/* 小图片保持原始尺寸 */
.chapter-content img[width] {
    max-width: none;
}

/* 响应式图片 */
@media (max-width: 768px) {
    .chapter-content img {
        max-width: 100%;
    }
}

.chapter-content video {
    max-width: 100%;
    height: auto;
    border-radius: 4px;
    margin: 10px 0;
}

/* 章节内容中的代码块样式 */
.chapter-content pre {
    background: #f6f8fa;
    border: 1px solid #e1e4e8;
    border-radius: 8px;
    padding: 16px;
    margin: 16px 0;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    font-size: 14px;
    line-height: 1.5;
    overflow-x: auto;
    position: relative;
}

.chapter-content pre::before {
    content: 'Code';
    position: absolute;
    top: 8px;
    right: 12px;
    background: #667eea;
    color: white;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 600;
}

.chapter-content code {
    background: #f1f3f4;
    padding: 2px 6px;
    border-radius: 4px;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    font-size: 13px;
    color: #d73a49;
}



/* 编辑器样式 */
.content-edit-actions {
    margin-top: 24px;
    display: flex;
    gap: 16px;
    justify-content: flex-end;
    padding: 20px;
    background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
    border-radius: 12px;
    border: 1px solid #e2e8f0;
}

.custom-editor {
    border-radius: 8px;
    overflow: hidden;
}

.custom-editor .ql-toolbar {
    border-top: 1px solid #ccc;
    border-left: 1px solid #ccc;
    border-right: 1px solid #ccc;
    background: #f8f9fa;
}

.custom-editor .ql-container {
    border-bottom: 1px solid #ccc;
    border-left: 1px solid #ccc;
    border-right: 1px solid #ccc;
    min-height: 300px;
}

.custom-editor .ql-editor {
    min-height: 300px;
    font-size: 14px;
    line-height: 1.6;
}
</style>