<template>
  <div class="course-detail-container" v-loading="loading">
    <!-- 课程基本信息 -->
    <div class="course-header" v-if="course">
      <el-row :gutter="20">
        <el-col :span="16">
          <h1 class="course-title">{{ course.title }}</h1>
          <div class="course-meta">
            <span class="meta-item">
              <i class="el-icon-user"></i> 讲师: {{ course.teacherName }}
            </span>
            <span class="meta-item">
              <i class="el-icon-date"></i> 创建时间: {{ formatDate(course.createTime) }}
            </span>
            <span class="meta-item">
              <i class="el-icon-user"></i> 学生数: {{ course.studentCount || 0 }}
            </span>
            <el-tag size="small" :type="getStatusType(course.status)">
              {{ getStatusText(course.status) }}
            </el-tag>
          </div>
          <div class="course-description">
            {{ course.description }}
          </div>
          
          <!-- 选课按钮 -->
          <div class="action-buttons" v-if="authStore.hasPermission('student')">
            <el-button 
              type="primary" 
              size="large" 
              @click="enrollCourse" 
              :disabled="isEnrolled || enrolling"
              :loading="enrolling"
            >
              {{ isEnrolled ? '已选课' : '选课' }}
            </el-button>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="course-cover">
            <img :src="course.coverImage || '/default-course.jpg'" :alt="course.title">
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 课程内容区域 -->
    <div class="course-content" v-if="course">
      <el-tabs v-model="activeTab" class="course-tabs">
        <!-- 章节内容 -->
        <el-tab-pane label="章节内容" name="chapters">
          <div class="chapter-list">
            <el-tree
              :data="chapterTree"
              :props="{
                label: 'title',
                children: 'children'
              }"
              node-key="id"
              :expand-on-click-node="false"
              @node-click="handleNodeClick"
            >
              <template #default="{ node, data }">
                <div class="chapter-node">
                  <span class="node-label">
                    <i class="el-icon-folder" v-if="data.type === 'chapter'"></i>
                    <i class="el-icon-document" v-else></i>
                    {{ node.label }}
                  </span>
                  
                  <!-- 教师可以编辑章节 -->
                  <div class="node-actions" v-if="isTeacher && data.type === 'chapter'">
                    <el-button type="text" size="small" @click.stop="editChapter(data)">
                      编辑
                    </el-button>
                    <el-button type="text" size="small" @click.stop="addSubChapter(data)">
                      添加子章节
                    </el-button>
                    <el-button type="text" size="small" @click.stop="addResource(data)">
                      添加资源
                    </el-button>
                    <el-button type="text" size="small" @click.stop="deleteChapter(data)">
                      删除
                    </el-button>
                  </div>
                  
                  <!-- 资源操作 -->
                  <div class="node-actions" v-if="data.type === 'resource'">
                    <el-button type="text" size="small" @click.stop="viewResource(data)">
                      查看
                    </el-button>
                    <el-button 
                      v-if="isTeacher" 
                      type="text" 
                      size="small" 
                      @click.stop="deleteResource(data)"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
              </template>
            </el-tree>
            
            <!-- 教师可以添加章节 -->
            <div class="add-chapter" v-if="isTeacher">
              <el-button type="primary" @click="addRootChapter">
                添加章节
              </el-button>
            </div>
          </div>
        </el-tab-pane>
        
        <!-- 课程评论 -->
        <el-tab-pane label="课程评论" name="comments">
          <div class="comments-section">
            <!-- 评论统计 -->
            <div class="comments-stats" v-if="commentStats">
              <div class="rating-overview">
                <div class="average-rating">
                  <span class="rating-number">{{ commentStats.averageRating || 0 }}</span>
                  <div class="rating-stars">
                    <el-rate
                      v-model="commentStats.averageRating"
                      disabled
                      show-score
                      text-color="#ff9900"
                    />
                  </div>
                  <div class="total-ratings">{{ commentStats.totalComments || 0 }} 条评价</div>
                </div>
                
                <div class="rating-distribution">
                  <div class="rating-bar" v-for="i in 5" :key="i">
                    <span class="star-level">{{ i }} 星</span>
                    <el-progress 
                      :percentage="getPercentage(i)" 
                      :stroke-width="12" 
                      :show-text="false" 
                      :color="'#ffd04b'"
                    />
                    <span class="percentage">{{ getPercentage(i) }}%</span>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 评论列表 -->
            <div class="comments-list">
              <div v-if="comments.length === 0" class="no-comments">
                暂无评论，快来发表第一条评论吧！
              </div>
              
              <div v-else class="comment-item" v-for="comment in comments" :key="comment.id">
                <div class="comment-header">
                  <div class="user-info">
                    <el-avatar size="small">{{ comment.username?.charAt(0) }}</el-avatar>
                    <span class="username">{{ comment.username }}</span>
                  </div>
                  <div class="comment-rating">
                    <el-rate
                      v-model="comment.rating"
                      disabled
                      text-color="#ff9900"
                    />
                  </div>
                </div>
                <div class="comment-content">
                  {{ comment.content }}
                </div>
                <div class="comment-footer">
                  <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                </div>
              </div>
            </div>
            
            <!-- 添加评论 -->
            <div class="add-comment" v-if="isEnrolled">
              <h3>发表评论</h3>
              <el-form :model="commentForm" :rules="commentRules" ref="commentFormRef">
                <el-form-item prop="rating">
                  <el-rate
                    v-model="commentForm.rating"
                    show-text
                    :texts="['很差', '较差', '一般', '较好', '很好']"
                  />
                </el-form-item>
                <el-form-item prop="content">
                  <el-input
                    v-model="commentForm.content"
                    type="textarea"
                    :rows="4"
                    placeholder="请输入您对课程的评价"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitComment" :loading="submittingComment">
                    提交评论
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </el-tab-pane>
        
        <!-- 考试列表 -->
        <el-tab-pane label="考试" name="exams" v-if="isEnrolled || isTeacher">
          <div class="exams-section">
            <div class="section-header">
              <h3>课程考试</h3>
              <el-button v-if="isTeacher" type="primary" @click="createExam">
                创建考试
              </el-button>
            </div>
            
            <el-table :data="exams" style="width: 100%">
              <el-table-column prop="title" label="考试名称" />
              <el-table-column prop="startTime" label="开始时间">
                <template #default="scope">
                  {{ formatDate(scope.row.startTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="endTime" label="结束时间">
                <template #default="scope">
                  {{ formatDate(scope.row.endTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="duration" label="考试时长">
                <template #default="scope">
                  {{ scope.row.duration }} 分钟
                </template>
              </el-table-column>
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button 
                    v-if="isStudent" 
                    type="primary" 
                    size="small" 
                    @click="takeExam(scope.row)"
                  >
                    参加考试
                  </el-button>
                  <el-button 
                    v-if="isTeacher" 
                    type="primary" 
                    size="small" 
                    @click="editExam(scope.row)"
                  >
                    编辑
                  </el-button>
                  <el-button 
                    v-if="isTeacher" 
                    type="danger" 
                    size="small" 
                    @click="deleteExam(scope.row)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <!-- 作业列表 -->
        <el-tab-pane label="作业" name="homework" v-if="isEnrolled || isTeacher">
          <div class="homework-section">
            <div class="section-header">
              <h3>课程作业</h3>
              <el-button v-if="isTeacher" type="primary" @click="createHomework">
                创建作业
              </el-button>
            </div>
            
            <el-table :data="homeworks" style="width: 100%">
              <el-table-column prop="title" label="作业名称" />
              <el-table-column prop="deadline" label="截止日期">
                <template #default="scope">
                  {{ formatDate(scope.row.deadline) }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态">
                <template #default="scope">
                  <el-tag :type="getHomeworkStatusType(scope.row.status)">
                    {{ getHomeworkStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button 
                    v-if="isStudent" 
                    type="primary" 
                    size="small" 
                    @click="viewHomework(scope.row)"
                  >
                    查看作业
                  </el-button>
                  <el-button 
                    v-if="isTeacher" 
                    type="primary" 
                    size="small" 
                    @click="editHomework(scope.row)"
                  >
                    编辑
                  </el-button>
                  <el-button 
                    v-if="isTeacher" 
                    type="danger" 
                    size="small" 
                    @click="deleteHomework(scope.row)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <!-- 章节编辑对话框 -->
    <el-dialog
      v-model="chapterDialogVisible"
      :title="chapterForm.id ? '编辑章节' : '添加章节'"
      width="50%"
    >
      <el-form :model="chapterForm" label-width="80px" :rules="chapterRules" ref="chapterFormRef">
        <el-form-item label="章节名称" prop="title">
          <el-input v-model="chapterForm.title" placeholder="请输入章节名称" />
        </el-form-item>
        <el-form-item label="章节描述" prop="description">
          <el-input 
            v-model="chapterForm.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入章节描述"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="chapterForm.sort" :min="1" :max="100" />
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
    <el-dialog
      v-model="resourceDialogVisible"
      title="上传资源"
      width="50%"
    >
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
        <el-form-item label="资源文件" prop="file">
          <el-upload
            class="resource-uploader"
            action="/edu/resource/upload"
            :headers="uploadHeaders"
            :on-success="handleResourceUploadSuccess"
            :before-upload="beforeResourceUpload"
            :limit="1"
          >
            <el-button type="primary">点击上传</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持各种类型的文件，大小不超过50MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resourceDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveResource" :loading="savingResource">
            保存
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
import { commentApi } from '../../api/comment';
import { enrollmentApi } from '../../api/enrollment';
import { examApi } from '../../api/exam';
import { homeworkApi } from '../../api/homework';
import { useAuthStore } from '../../store/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 课程ID
const courseId = computed(() => route.params.id);

// 页面状态
const loading = ref(false);
const course = ref(null);
const chapterTree = ref([]);
const comments = ref([]);
const commentStats = ref(null);
const exams = ref([]);
const homeworks = ref([]);
const activeTab = ref('chapters');

// 用户角色
const isTeacher = computed(() => authStore.hasPermission('teacher'));
const isStudent = computed(() => authStore.hasPermission('student'));

// 选课状态
const isEnrolled = ref(false);
const enrolling = ref(false);

// 章节对话框
const chapterDialogVisible = ref(false);
const chapterFormRef = ref(null);
const savingChapter = ref(false);
const chapterForm = reactive({
  id: '',
  courseId: '',
  parentId: '',
  title: '',
  description: '',
  sort: 1
});

// 资源对话框
const resourceDialogVisible = ref(false);
const resourceFormRef = ref(null);
const savingResource = ref(false);
const resourceForm = reactive({
  chapterId: '',
  title: '',
  type: '',
  url: '',
  file: null
});

// 评论表单
const commentFormRef = ref(null);
const submittingComment = ref(false);
const commentForm = reactive({
  courseId: '',
  rating: 5,
  content: ''
});

// 表单验证规则
const chapterRules = {
  title: [
    { required: true, message: '请输入章节名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '长度不能超过 200 个字符', trigger: 'blur' }
  ]
};

const resourceRules = {
  title: [
    { required: true, message: '请输入资源名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择资源类型', trigger: 'change' }
  ]
};

const commentRules = {
  rating: [
    { required: true, message: '请选择评分', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入评论内容', trigger: 'blur' },
    { min: 5, max: 500, message: '长度在 5 到 500 个字符', trigger: 'blur' }
  ]
};

// 上传文件的请求头
const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${localStorage.getItem('token')}`
  };
});

// 获取课程详情
const fetchCourseDetail = async () => {
  try {
    loading.value = true;
    const res = await courseApi.getCourseDetail(courseId.value);
    course.value = res.data;
    commentForm.courseId = courseId.value;
  } catch (error) {
    console.error('获取课程详情失败:', error);
    ElMessage.error('获取课程详情失败');
  } finally {
    loading.value = false;
  }
};

// 获取章节树
const fetchChapterTree = async () => {
  try {
    const res = await courseApi.getChapterTree(courseId.value);
    chapterTree.value = res.data || [];
  } catch (error) {
    console.error('获取章节树失败:', error);
    ElMessage.error('获取章节树失败');
  }
};

// 获取课程评论
const fetchComments = async () => {
  try {
    const res = await commentApi.getCourseComments(courseId.value);
    comments.value = res.data || [];
  } catch (error) {
    console.error('获取课程评论失败:', error);
    ElMessage.error('获取课程评论失败');
  }
};

// 获取评论统计
const fetchCommentStats = async () => {
  try {
    const res = await commentApi.getCommentStats(courseId.value);
    commentStats.value = res.data || {
      averageRating: 0,
      totalComments: 0,
      ratingDistribution: {1: 0, 2: 0, 3: 0, 4: 0, 5: 0}
    };
  } catch (error) {
    console.error('获取评论统计失败:', error);
    ElMessage.error('获取评论统计失败');
  }
};

// 获取考试列表
const fetchExams = async () => {
  try {
    const res = await examApi.getCourseExams(courseId.value);
    exams.value = res.data || [];
  } catch (error) {
    console.error('获取考试列表失败:', error);
    ElMessage.error('获取考试列表失败');
  }
};

// 获取作业列表
const fetchHomeworks = async () => {
  try {
    const res = await homeworkApi.getCourseHomeworks(courseId.value);
    homeworks.value = res.data || [];
  } catch (error) {
    console.error('获取作业列表失败:', error);
    ElMessage.error('获取作业列表失败');
  }
};

// 检查是否已选课
const checkEnrollment = async () => {
  try {
    const res = await enrollmentApi.getMyCourses();
    const courses = res.data || [];
    isEnrolled.value = courses.some(c => c.id === courseId.value);
  } catch (error) {
    console.error('检查选课状态失败:', error);
  }
};

// 选课
const enrollCourse = async () => {
  try {
    enrolling.value = true;
    await enrollmentApi.enrollCourse(courseId.value);
    ElMessage.success('选课成功');
    isEnrolled.value = true;
  } catch (error) {
    console.error('选课失败:', error);
    ElMessage.error('选课失败');
  } finally {
    enrolling.value = false;
  }
};

// 提交评论
const submitComment = async () => {
  if (!commentFormRef.value) return;
  
  await commentFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        submittingComment.value = true;
        await commentApi.submitComment(commentForm);
        ElMessage.success('评论提交成功');
        // 重置表单
        commentForm.content = '';
        commentForm.rating = 5;
        // 刷新评论列表和统计
        fetchComments();
        fetchCommentStats();
      } catch (error) {
        console.error('提交评论失败:', error);
        ElMessage.error('提交评论失败');
      } finally {
        submittingComment.value = false;
      }
    }
  });
};

// 章节树节点点击
const handleNodeClick = (data) => {
  if (data.type === 'resource') {
    viewResource(data);
  }
};

// 添加根章节
const addRootChapter = () => {
  chapterForm.id = '';
  chapterForm.courseId = courseId.value;
  chapterForm.parentId = '0';
  chapterForm.title = '';
  chapterForm.description = '';
  chapterForm.sort = 1;
  chapterDialogVisible.value = true;
};

// 添加子章节
const addSubChapter = (parentChapter) => {
  chapterForm.id = '';
  chapterForm.courseId = courseId.value;
  chapterForm.parentId = parentChapter.id;
  chapterForm.title = '';
  chapterForm.description = '';
  chapterForm.sort = 1;
  chapterDialogVisible.value = true;
};

// 编辑章节
const editChapter = (chapter) => {
  chapterForm.id = chapter.id;
  chapterForm.courseId = chapter.courseId;
  chapterForm.parentId = chapter.parentId;
  chapterForm.title = chapter.title;
  chapterForm.description = chapter.description;
  chapterForm.sort = chapter.sort;
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
        // 刷新章节树
        fetchChapterTree();
      } catch (error) {
        console.error('保存章节失败:', error);
        ElMessage.error('保存章节失败');
      } finally {
        savingChapter.value = false;
      }
    }
  });
};

// 删除章节
const deleteChapter = async (chapter) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该章节吗？删除后将无法恢复。',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    
    await courseApi.deleteChapter(chapter.id);
    ElMessage.success('章节删除成功');
    // 刷新章节树
    fetchChapterTree();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除章节失败:', error);
      ElMessage.error('删除章节失败');
    }
  }
};

// 添加资源
const addResource = (chapter) => {
  resourceForm.chapterId = chapter.id;
  resourceForm.title = '';
  resourceForm.type = '';
  resourceForm.url = '';
  resourceForm.file = null;
  resourceDialogVisible.value = true;
};

// 上传资源前的验证
const beforeResourceUpload = (file) => {
  const isLt50M = file.size / 1024 / 1024 < 50;
  if (!isLt50M) {
    ElMessage.error('上传文件大小不能超过 50MB!');
  }
  return isLt50M;
};

// 资源上传成功回调
const handleResourceUploadSuccess = (res) => {
  if (res.code === 200) {
    resourceForm.url = res.data.url;
    ElMessage.success('文件上传成功');
  } else {
    ElMessage.error(res.message || '文件上传失败');
  }
};

// 保存资源
const saveResource = async () => {
  if (!resourceFormRef.value) return;
  
  await resourceFormRef.value.validate(async (valid) => {
    if (valid) {
      if (!resourceForm.url) {
        ElMessage.warning('请先上传资源文件');
        return;
      }
      
      try {
        savingResource.value = true;
        // 创建资源记录
        await courseApi.createResource(resourceForm);
        ElMessage.success('资源添加成功');
        resourceDialogVisible.value = false;
        // 刷新章节树
        fetchChapterTree();
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
    // 刷新章节树
    fetchChapterTree();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除资源失败:', error);
      ElMessage.error('删除资源失败');
    }
  }
};

// 创建考试
const createExam = () => {
  router.push(`/exam/create?courseId=${courseId.value}`);
};

// 编辑考试
const editExam = (exam) => {
  router.push(`/exam/edit/${exam.id}`);
};

// 删除考试
const deleteExam = async (exam) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该考试吗？删除后将无法恢复。',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    
    await examApi.deleteExam(exam.id);
    ElMessage.success('考试删除成功');
    // 刷新考试列表
    fetchExams();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除考试失败:', error);
      ElMessage.error('删除考试失败');
    }
  }
};

// 参加考试
const takeExam = (exam) => {
  router.push(`/exam/${exam.id}`);
};

// 创建作业
const createHomework = () => {
  router.push(`/homework/create?courseId=${courseId.value}`);
};

// 编辑作业
const editHomework = (homework) => {
  router.push(`/homework/edit/${homework.id}`);
};

// 删除作业
const deleteHomework = async (homework) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该作业吗？删除后将无法恢复。',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    
    await homeworkApi.deleteHomework(homework.id);
    ElMessage.success('作业删除成功');
    // 刷新作业列表
    fetchHomeworks();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除作业失败:', error);
      ElMessage.error('删除作业失败');
    }
  }
};

// 查看作业
const viewHomework = (homework) => {
  router.push(`/homework/${homework.id}`);
};

// 获取课程状态类型
const getStatusType = (status) => {
  const statusMap = {
    'pending': 'warning',
    'approved': 'success',
    'rejected': 'danger'
  };
  return statusMap[status] || 'info';
};

// 获取课程状态文本
const getStatusText = (status) => {
  const statusMap = {
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '已驳回'
  };
  return statusMap[status] || '未知状态';
};

// 获取作业状态类型
const getHomeworkStatusType = (status) => {
  const statusMap = {
    'pending': 'info',
    'submitted': 'success',
    'graded': 'warning',
    'overdue': 'danger'
  };
  return statusMap[status] || 'info';
};

// 获取作业状态文本
const getHomeworkStatusText = (status) => {
  const statusMap = {
    'pending': '待提交',
    'submitted': '已提交',
    'graded': '已批改',
    'overdue': '已逾期'
  };
  return statusMap[status] || '未知状态';
};

// 获取评分百分比
const getPercentage = (star) => {
  if (!commentStats.value || !commentStats.value.totalComments) return 0;
  const distribution = commentStats.value.ratingDistribution || {};
  const count = distribution[star] || 0;
  return Math.round((count / commentStats.value.totalComments) * 100);
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString();
};

onMounted(async () => {
  await fetchCourseDetail();
  await fetchChapterTree();
  await fetchComments();
  await fetchCommentStats();
  await fetchExams();
  await fetchHomeworks();
  
  if (isStudent.value) {
    await checkEnrollment();
  }
});
</script>

<style scoped>
.course-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.course-header {
  background: white;
  border-radius: 8px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.course-title {
  font-size: 28px;
  color: #333;
  margin: 0 0 16px 0;
}

.course-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 20px;
  color: #666;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.course-description {
  color: #555;
  line-height: 1.6;
  margin-bottom: 20px;
}

.action-buttons {
  margin-top: 20px;
}

.course-cover {
  width: 100%;
  height: 240px;
  overflow: hidden;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.course-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.course-content {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.course-tabs {
  margin-bottom: 20px;
}

/* 章节树样式 */
.chapter-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 8px 0;
}

.node-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.node-actions {
  display: flex;
  gap: 8px;
}

.add-chapter {
  margin-top: 20px;
  text-align: center;
}

/* 评论区样式 */
.comments-section {
  padding: 20px 0;
}

.comments-stats {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 30px;
}

.rating-overview {
  display: flex;
  gap: 40px;
}

.average-rating {
  text-align: center;
}

.rating-number {
  font-size: 48px;
  font-weight: bold;
  color: #ff9900;
}

.total-ratings {
  color: #666;
  margin-top: 8px;
}

.rating-distribution {
  flex: 1;
}

.rating-bar {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.star-level {
  width: 40px;
  text-align: right;
  margin-right: 10px;
}

.percentage {
  width: 40px;
  text-align: left;
  margin-left: 10px;
}

.comments-list {
  margin-bottom: 30px;
}

.no-comments {
  text-align: center;
  color: #999;
  padding: 30px 0;
}

.comment-item {
  border-bottom: 1px solid #eee;
  padding: 20px 0;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  font-weight: 500;
}

.comment-content {
  line-height: 1.6;
  color: #333;
  margin-bottom: 12px;
}

.comment-footer {
  color: #999;
  font-size: 12px;
}

.add-comment {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 20px;
}

.add-comment h3 {
  margin-top: 0;
  margin-bottom: 16px;
}

/* 考试和作业区域 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  margin: 0;
}

/* 资源上传 */
.resource-uploader {
  width: 100%;
}
</style>