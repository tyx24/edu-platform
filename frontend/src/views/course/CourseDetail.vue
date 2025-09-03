<template>
  <div class="cosmic-course-detail">
    <!-- 星空背景 -->
    <div class="stars">
      <div v-for="i in 100" :key="i" class="star" :style="getStarStyle(i)"></div>
    </div>

    <!-- 背景星云 -->
    <div class="nebula nebula-1"></div>
    <div class="nebula nebula-2"></div>

    <!-- 漂浮星球 -->
    <div class="floating-orbs">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
      <div class="orb orb-4"></div>
    </div>

    <!-- 主要内容区域 -->
    <div class="course-content-wrapper" v-loading="loading">
      <!-- 课程头部面板 -->
      <div class="glassmorphism-header" v-if="course">
        <div class="header-content">
          <div class="course-info-section">
            <div class="title-area">
              <h1 class="cosmic-title">
                <span class="title-text">{{ course.title }}</span>
              </h1>

              <div class="status-badge" :class="`status-${course.status}`">
                <div class="badge-content">
                  <span class="status-icon">✨</span>
                  <span class="status-text">{{ getStatusText(course.status) }}</span>
                </div>
              </div>
              <div class="badge-glow"></div>
            </div>

            <div class="meta-grid">
              <div class="meta-card instructor-card">
                <div class="meta-icon">👨‍🏫</div>
                <div class="meta-content">
                  <div class="meta-label">授课教师</div>
                  <div class="meta-value">{{ course.teacherName }}</div>
                </div>
              </div>

              <div class="meta-card time-card">
                <div class="meta-icon">📅</div>
                <div class="meta-content">
                  <div class="meta-label">创建时间</div>
                  <div class="meta-value">{{ formatDate(course.createTime) }}</div>
                </div>
              </div>

              <div class="meta-card students-card">
                <div class="meta-icon">👥</div>
                <div class="meta-content">
                  <div class="meta-label">学生数量</div>
                  <div class="meta-value">{{ course.studentCount || 0 }}人</div>
                </div>
              </div>
            </div>

            <div class="description-panel">
              <div class="description-content">
                {{ course.description }}
              </div>
            </div>

            <!-- 选课按钮 -->
            <div class="action-zone" v-if="authStore.hasPermission('student')">
              <button class="enroll-button" :class="{ 'enrolled': isEnrolled, 'loading': enrolling }"
                @click="enrollCourse" :disabled="isEnrolled || enrolling">
                <div class="button-background"></div>
                <div class="button-content">
                  <span class="button-icon">{{ isEnrolled ? '✓' : '🚀' }}</span>
                  <span class="button-text">{{ isEnrolled ? '已选课' : '立即选课' }}</span>
                </div>
              </button>
            </div>
          </div>

          <div class="course-visual-section">
            <div class="cover-container">
              <div class="cover-frame">
                <img :src="course.coverUrl || '/default-course.jpg'" :alt="course.title" class="course-image">
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 主要内容面板 -->
      <div class="main-content-panel" v-if="course">
        <div class="glassmorphism-tabs">
          <div class="tab-header">
            <div class="tab-title">
              <span class="title-icon">📚</span>
              <span class="title-text">课程内容</span>
            </div>

            <div class="tab-navigation">
              <button v-for="tab in tabs" :key="tab.name" class="tab-button"
                :class="{ 'active': activeTab === tab.name, 'disabled': tab.disabled }" @click="activeTab = tab.name"
                :disabled="tab.disabled">
                <div class="tab-content">
                  <span class="tab-icon">{{ tab.icon }}</span>
                  <span class="tab-label">{{ tab.label }}</span>
                </div>
              </button>
            </div>
          </div>

          <!-- 章节内容面板 -->
          <div class="tab-panel" v-show="activeTab === 'chapters'">
            <div class="chapter-container">
              <div class="chapter-header">
                <h3 class="section-title">
                  <span class="title-icon">📖</span>
                  <span>课程章节</span>
                </h3>

                <!-- 教师添加章节按钮 -->
                <button v-if="isTeacher" class="add-chapter-btn" @click="addRootChapter">
                  <div class="btn-content">
                    <span class="btn-icon">➕</span>
                    <span class="btn-text">添加章节</span>
                  </div>
                </button>
              </div>

              <div class="chapter-tree">
                <el-tree :data="chapterTree" :props="{ label: 'title', children: 'children' }" node-key="id"
                  :expand-on-click-node="false" @node-click="handleNodeClick" class="cosmic-tree">
                  <template #default="{ node, data }">
                    <div class="chapter-node">
                      <div class="node-main">
                        <div class="node-icon">
                          <span v-if="data.type === 'chapter'">📁</span>
                          <span v-else>📄</span>
                        </div>
                        <div class="node-content">
                          <span class="node-title">{{ node.label }}</span>
                        </div>
                      </div>

                      <!-- 教师操作按钮 -->
                      <div class="node-actions" v-if="isTeacher && data.type === 'chapter'">
                        <button class="action-btn edit-btn" @click.stop="editChapter(data)">
                          <span class="btn-icon">✏️</span>
                        </button>

                        <button v-if="data.parentId === 0 || (data.parentId !== 0 && !isSubSubChapter(data))"
                          class="action-btn add-btn" @click.stop="addSubChapter(data)">
                          <span class="btn-icon">➕</span>
                        </button>

                        <button class="action-btn delete-btn" @click.stop="deleteChapter(data)">
                          <span class="btn-icon">🗑️</span>
                        </button>
                      </div>
                    </div>
                  </template>
                </el-tree>
              </div>
            </div>
          </div>

          <!-- 课程评论面板 -->
          <div class="tab-panel" v-show="activeTab === 'comments'">
            <div class="comments-container">
              <!-- 评论统计区域 -->
              <div class="stats-panel" v-if="commentStats">
                <div class="stats-header">
                  <h3 class="section-title">
                    <span class="title-icon">⭐</span>
                    <span>评价统计</span>
                  </h3>
                </div>

                <div class="rating-dashboard">
                  <div class="overall-rating">
                    <div class="rating-circle">
                      <div class="rating-value">{{ commentStats.avgRating || 0 }}</div>
                      <div class="rating-max">/10</div>
                      <div class="circle-progress" :style="`--progress: ${(commentStats.avgRating || 0) * 10}%`"></div>
                    </div>

                    <div class="rating-details">
                      <div class="stars-display">
                        <el-rate :model-value="commentStats.avgRating / 2" disabled show-score text-color="#ffd700" />
                      </div>
                      <div class="total-count">{{ commentStats.totalComments || 0 }} 条评价</div>
                    </div>
                  </div>

                  <div class="distribution-chart">
                    <div class="chart-item" v-for="i in 5" :key="i">
                      <span class="star-label">{{ i }}⭐</span>
                      <div class="progress-track">
                        <div class="progress-fill" :style="`width: ${getPercentage(i)}%`"></div>
                        <div class="progress-glow"></div>
                      </div>
                      <span class="percentage-label">{{ getPercentage(i) }}%</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 评论列表 -->
              <div class="comments-feed">
                <div class="feed-header">
                  <h3 class="section-title">
                    <span class="title-icon">💬</span>
                    <span>用户评价</span>
                    <div class="title-glow"></div>
                  </h3>
                </div>

                <div v-if="comments.length === 0" class="empty-state">
                  <div class="empty-icon">🌟</div>
                  <div class="empty-text">暂无评论，快来发表第一条评论吧！</div>
                  <div class="empty-glow"></div>
                </div>

                <div v-else class="comment-list">
                  <div class="comment-card" v-for="comment in comments" :key="comment.id">
                    <div class="comment-background"></div>
                    <div class="comment-content-wrapper">
                      <div class="comment-header">
                        <div class="user-section">
                          <div class="avatar-container">
                            <div class="avatar-circle">
                              <span class="avatar-text">{{ comment.username?.charAt(0) }}</span>
                            </div>
                          </div>
                          <div class="user-details">
                            <div class="username">{{ comment.studentId }}</div>
                            <div class="comment-time">{{ formatDate(comment.createTime) }}</div>
                          </div>
                        </div>
                        <div class="rating-section">
                          <el-rate v-model="comment.rating" disabled text-color="#ffd700" />
                        </div>
                      </div>

                      <div class="comment-body">
                        {{ comment.content }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 评论发表区域 -->
              <div class="comment-composer" v-if="isEnrolled">
                <div class="composer-header">
                  <h3 class="section-title">
                    <span class="title-icon">✍️</span>
                    <span>发表评论</span>
                  </h3>
                </div>

                <div class="composer-form">
                  <el-form :model="commentForm" :rules="commentRules" ref="commentFormRef">
                    <div class="rating-input">
                      <label class="input-label">课程评分</label>
                      <el-rate v-model="commentForm.rating" show-text :texts="['很差', '较差', '一般', '较好', '很好']" />
                    </div>

                    <div class="content-input">
                      <label class="input-label">评价内容</label>
                      <el-input v-model="commentForm.content" type="textarea" :rows="4" placeholder="请输入您对课程的评价..."
                        class="cosmic-textarea" />
                    </div>

                    <div class="submit-section">
                      <button class="submit-btn" @click="submitComment" :disabled="submittingComment">
                        <div class="btn-content">
                          <span class="btn-icon">🚀</span>
                          <span class="btn-text">{{ submittingComment ? '提交中...' : '提交评论' }}</span>
                        </div>
                        <div class="loading-spinner" v-if="submittingComment"></div>
                      </button>
                    </div>
                  </el-form>
                </div>
              </div>
            </div>
          </div>

          <!-- 考试列表面板 -->
          <div class="tab-panel" v-show="activeTab === 'exams' && (isEnrolled || isTeacher)">
            <div class="exams-container">
              <div class="exams-header">
                <h3 class="section-title">
                  <span class="title-icon">📝</span>
                  <span>课程考试</span>
                </h3>

                <button v-if="isTeacher" class="create-exam-btn" @click="createExam">
                  <div class="btn-content">
                    <span class="btn-icon">➕</span>
                    <span class="btn-text">创建考试</span>
                  </div>
                </button>
              </div>

              <div class="exams-grid">
                <div v-if="exams.length === 0" class="empty-state">
                  <div class="empty-icon">📋</div>
                  <div class="empty-text">暂无考试安排</div>
                </div>

                <div v-else class="exam-card" v-for="exam in exams" :key="exam.id">
                  <div class="card-content">
                    <div class="exam-header">
                      <div class="exam-title">{{ exam.title }}</div>
                      <div class="exam-status">📊</div>
                    </div>

                    <div class="exam-details">
                      <div class="detail-item">
                        <span class="detail-icon">🕐</span>
                        <span class="detail-label">开始时间</span>
                        <span class="detail-value">{{ formatDate(exam.startTime) }}</span>
                      </div>

                      <div class="detail-item">
                        <span class="detail-icon">🕑</span>
                        <span class="detail-label">结束时间</span>
                        <span class="detail-value">{{ formatDate(exam.endTime) }}</span>
                      </div>

                      <div class="detail-item">
                        <span class="detail-icon">⏱️</span>
                        <span class="detail-label">考试时长</span>
                        <span class="detail-value">{{ exam.duration }} 分钟</span>
                      </div>
                    </div>

                    <div class="exam-actions">
                      <button v-if="isStudent" class="action-btn primary-btn" @click="takeExam(exam)">
                        <span class="btn-icon">🚀</span>
                        <span class="btn-text">参加考试</span>
                      </button>

                      <button v-if="isTeacher" class="action-btn edit-btn" @click="editExam(exam)">
                        <span class="btn-icon">✏️</span>
                        <span class="btn-text">编辑</span>
                      </button>

                      <button v-if="isTeacher" class="action-btn delete-btn" @click="deleteExam(exam)">
                        <span class="btn-icon">🗑️</span>
                        <span class="btn-text">删除</span>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 作业列表面板 -->
          <div class="tab-panel" v-show="activeTab === 'homeworks' && (isEnrolled || isTeacher)">
            <div class="homework-container">
              <div class="homework-header">
                <h3 class="section-title">
                  <span class="title-icon">📋</span>
                  <span>课程作业</span>
                </h3>

                <button v-if="isTeacher" class="create-homework-btn" @click="createHomework">
                  <div class="btn-content">
                    <span class="btn-icon">➕</span>
                    <span class="btn-text">创建作业</span>
                  </div>
                </button>
              </div>

              <div class="homework-grid">
                <div v-if="homeworks.length === 0" class="empty-state">
                  <div class="empty-icon">📝</div>
                  <div class="empty-text">暂无作业安排</div>
                </div>

                <div v-else class="homework-card" v-for="homework in homeworks" :key="homework.id">
                  <div class="card-content">
                    <div class="homework-header">
                      <div class="homework-title">{{ homework.title }}</div>
                      <div class="homework-status" :class="`status-${homework.status}`">
                        {{ getHomeworkStatusText(homework.status) }}
                      </div>
                    </div>

                    <div class="homework-details">
                      <div class="detail-item">
                        <span class="detail-icon">📅</span>
                        <span class="detail-label">截止日期： </span>
                        <span class="detail-value">{{ formatDate(homework.deadline) }}</span>
                      </div>
                    </div>

                    <div class="homework-actions">
                      <button v-if="isStudent" class="action-btn primary-btn" @click="viewHomework(homework)">
                        <span class="btn-icon">👁️</span>
                        <span class="btn-text">查看作业</span>
                      </button>

                      <button v-if="isTeacher" class="action-btn edit-btn" @click="editHomework(homework)">
                        <span class="btn-icon">✏️</span>
                        <span class="btn-text">编辑</span>
                      </button>

                      <button v-if="isTeacher" class="action-btn delete-btn" @click="deleteHomework(homework)">
                        <span class="btn-icon">🗑️</span>
                        <span class="btn-text">删除</span>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>



        </div>
      </div>
    </div>

    <!-- 章节编辑对话框 -->
    <el-dialog v-model="chapterDialogVisible" :title="getChapterDialogTitle()" width="50%">
      <el-form :model="chapterForm" label-width="80px" :rules="chapterRules" ref="chapterFormRef">
        <el-form-item label="章节名称" prop="title">
          <el-input v-model="chapterForm.title" placeholder="请输入章节名称" />
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

// 响应式数据
const courseId = ref(route.params.id);
const loading = ref(false);
const course = ref(null);
const chapterTree = ref([]);
const comments = ref([]);
const exams = ref([]);
const homeworks = ref([]);
const activeTab = ref('chapters');

// 标签页配置
const tabs = [
  { name: 'chapters', label: '课程章节', icon: '📚' },
  { name: 'comments', label: '课程评价', icon: '💬' },
  { name: 'exams', label: '课程考试', icon: '📝' },
  { name: 'homeworks', label: '课程作业', icon: '📋' }
];

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
  id: null,
  courseId: '',
  title: '',
  content: '',
  parentId: null,
  orderNum: 1
});

// 评论表单
const commentFormRef = ref(null);
const submittingComment = ref(false);
const commentForm = reactive({
  courseId: '',
  content: '',
  rating: 5
});

// 评论统计
const commentStats = ref(null);



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

const commentRules = {
  rating: [
    { required: true, message: '请选择评分', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入评论内容', trigger: 'blur' },
    { min: 5, max: 500, message: '长度在 5 到 500 个字符', trigger: 'blur' }
  ]
};



// 章节树节点点击
const handleNodeClick = (data) => {
  if (data.type === 'chapter') {
    // 点击章节节点时跳转到章节详情界面
    router.push(`/course/${courseId.value}/chapter/${data.id}`);
  }
};

// 判断是否为子章节的子章节（第三层级）
const isSubSubChapter = (chapter) => {
  // 如果章节的parentId不为0，说明它是子章节
  // 需要在章节树中查找其父章节，如果父章节的parentId也不为0，则当前章节为子章节的子章节
  if (chapter.parentId === 0) {
    return false; // 根章节
  }

  // 在章节树中查找父章节
  const findParentChapter = (chapters, targetId) => {
    for (const ch of chapters) {
      if (ch.id === targetId) {
        return ch;
      }
      if (ch.children && ch.children.length > 0) {
        const found = findParentChapter(ch.children, targetId);
        if (found) return found;
      }
    }
    return null;
  };

  const parentChapter = findParentChapter(chapterTree.value, chapter.parentId);
  return parentChapter && parentChapter.parentId !== 0;
};

// 添加根章节
const addRootChapter = () => {
  chapterForm.id = null;
  chapterForm.courseId = courseId.value;
  chapterForm.parentId = 0;
  chapterForm.title = '';
  chapterForm.orderNum = 1;
  chapterDialogVisible.value = true;
};

// 添加子章节
const addSubChapter = (parentChapter) => {
  chapterForm.id = null;
  chapterForm.courseId = courseId.value;
  chapterForm.parentId = parentChapter.id;
  chapterForm.title = '';
  chapterForm.orderNum = 1;
  chapterDialogVisible.value = true;
};

// 编辑章节
const editChapter = (chapter) => {
  chapterForm.id = chapter.id;
  chapterForm.courseId = courseId.value;
  chapterForm.parentId = chapter.parentId;
  chapterForm.title = chapter.title;
  chapterForm.orderNum = chapter.orderNum;
  chapterDialogVisible.value = true;
};

// 保存章节
const saveChapter = async () => {
  if (!chapterFormRef.value) return;

  try {
    await chapterFormRef.value.validate();
    savingChapter.value = true;

    // 为章节数据添加type字段
    const chapterData = {
      ...chapterForm,
      type: 'chapter'
    };

    if (chapterForm.id) {
      // 更新章节
      await courseApi.updateChapter(chapterForm.id, chapterData);
      ElMessage.success('章节更新成功');
    } else {
      // 创建章节
      await courseApi.createChapter(chapterData);
      ElMessage.success('章节创建成功');
    }
    chapterDialogVisible.value = false;
    // 刷新章节树
    await fetchChapterTree();
  } catch (error) {
    console.error('保存章节失败:', error);
    ElMessage.error('保存章节失败');
  } finally {
    savingChapter.value = false;
  }
};

// 获取章节对话框标题
const getChapterDialogTitle = () => {
  if (chapterForm.id) {
    return '编辑章节';
  } else if (chapterForm.parentId === 0) {
    return '添加根章节';
  } else {
    return '添加子章节';
  }
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
    await fetchChapterTree();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除章节失败:', error);
      ElMessage.error('删除章节失败');
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
    await fetchExams();
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
    await fetchHomeworks();
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

// 获取评论统计
const fetchCommentStats = async () => {
  try {
    const res = await commentApi.getCommentStats(courseId.value);
    commentStats.value = res || {
      avgRating: 0,
      totalComments: 0,
      ratingDistribution: { 2: 0, 4: 0, 6: 0, 8: 0, 10: 0 }
    };
  } catch (error) {
    console.error('获取评论统计失败:', error);
    ElMessage.error('获取评论统计失败');
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
        await fetchComments();
        await fetchCommentStats();
      } catch (error) {
        console.error('提交评论失败:', error);
        ElMessage.error('提交评论失败');
      } finally {
        submittingComment.value = false;
      }
    }
  });
};

// 获取课程详情
const fetchCourseDetail = async () => {
  const res = await courseApi.getCourseDetail(courseId.value);
  course.value = res.course;
  commentForm.courseId = courseId.value;
};

// 获取章节树
const fetchChapterTree = async () => {
  const res = await courseApi.getChapterTree(courseId.value);

  // 为章节数据添加type字段
  const processChapterData = (chapters) => {
    return chapters.map(chapter => {
      const processedChapter = {
        ...chapter,
        type: 'chapter' // 为章节添加type标识
      };

      // 递归处理子章节
      if (chapter.children && chapter.children.length > 0) {
        processedChapter.children = processChapterData(chapter.children);
      }
      return processedChapter;
    });
  };

  chapterTree.value = processChapterData(res.data || []);
};

// 获取评论列表
const fetchComments = async () => {
  const res = await commentApi.getCourseComments(courseId.value);
  comments.value = res || [];
};

// 获取考试列表
const fetchExams = async () => {
  const res = await examApi.getCourseExams(courseId.value);
  exams.value = res.data || [];
};

// 获取作业列表
const fetchHomeworks = async () => {
  const res = await homeworkApi.getCourseHomework(courseId.value);
  homeworks.value = res.data || [];
};

// 检查选课状态
const checkEnrollment = async () => {
  if (!isStudent.value) return;
  try {
    const response = await enrollmentApi.getMyCourses();
    const courses = response || [];
    isEnrolled.value = courses.some(course => course.id == courseId.value);
  } catch (error) {
    console.error('检查选课状态失败:', error);
  }
};

// 选课
const enrollCourse = async () => {
  try {
    enrolling.value = true;
    await enrollmentApi.enrollCourse(courseId.value);
    isEnrolled.value = true;
    ElMessage.success('选课成功');
  } catch (error) {
    console.error('选课失败:', error);
    ElMessage.error('选课失败');
  } finally {
    enrolling.value = false;
  }
};

// 获取课程状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '待审核',
    1: '已发布',
    2: '关闭'
  };
  return statusMap[status] || '未知状态';
};

// 获取课程状态类型
const getStatusType = (status) => {
  const statusMap = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  };
  return statusMap[status] || 'info';
};

// 获取作业状态文本
const getHomeworkStatusText = (status) => {
  const statusMap = {
    'pending': '待提交',
    'submitted': '已提交',
    'graded': '已评分',
    'overdue': '已逾期'
  };
  return statusMap[status] || '未知状态';
};

// 获取评分百分比
const getPercentage = (star) => {
  if (!commentStats.value || !commentStats.value.totalComments) return 0;
  const distribution = commentStats.value.ratingDistribution || {};
  const count = distribution[star * 2] || 0; // 评分是1-5星，但后端存储是2,4,6,8,10
  return Math.round((count / commentStats.value.totalComments) * 100);
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString();
};

// 星空背景样式
const getStarStyle = (index) => {
  return {
    left: Math.random() * 100 + '%',
    top: Math.random() * 100 + '%',
    animationDelay: Math.random() * 3 + 's',
    animationDuration: (Math.random() * 2 + 1) + 's'
  };
};

// 初始化所有数据
const initializeData = async () => {
  try {
    loading.value = true;

    // 并发获取基础数据
    const basePromises = [
      fetchCourseDetail(),
      fetchChapterTree(),
      fetchComments(),
      fetchCommentStats(),
      fetchExams(),
      fetchHomeworks()
    ];

    // 学生需要额外检查选课状态
    if (isStudent.value) {
      basePromises.push(checkEnrollment());
    }

    await Promise.allSettled(basePromises);
  } catch (error) {
    console.error('初始化数据失败:', error);
    ElMessage.error('加载数据失败，请刷新页面重试');
  } finally {
    loading.value = false;
  }
};

// 组件挂载时获取数据
onMounted(() => {
  initializeData();
});
</script>

<style scoped>
/* 宇宙背景样式 */
.cosmic-course-detail {
  position: relative;
  min-height: 100vh;
  width: 100%;
  overflow-x: hidden;
}

.cosmic-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #0f0c29 0%, #302b63 35%, #24243e 70%, #191654 100%);
  z-index: -1;
}

/* 浮动光球 */
.floating-orbs {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  overflow: hidden;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(1px);
  animation: float 8s ease-in-out infinite;
}

.orb-1 {
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, rgba(138, 43, 226, 0.4) 0%, rgba(75, 0, 130, 0.2) 70%, transparent 100%);
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.orb-2 {
  width: 80px;
  height: 80px;
  background: radial-gradient(circle, rgba(0, 191, 255, 0.4) 0%, rgba(30, 144, 255, 0.2) 70%, transparent 100%);
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.orb-3 {
  width: 100px;
  height: 100px;
  background: radial-gradient(circle, rgba(255, 20, 147, 0.4) 0%, rgba(199, 21, 133, 0.2) 70%, transparent 100%);
  bottom: 30%;
  left: 20%;
  animation-delay: 4s;
}

.orb-4 {
  width: 60px;
  height: 60px;
  background: radial-gradient(circle, rgba(138, 43, 226, 0.5) 0%, rgba(75, 0, 130, 0.3) 70%, transparent 100%);
  top: 40%;
  right: 30%;
  animation-delay: 6s;
}

@keyframes float {

  0%,
  100% {
    transform: translateY(0px) translateX(0px) scale(1);
  }

  25% {
    transform: translateY(-20px) translateX(10px) scale(1.1);
  }

  50% {
    transform: translateY(-10px) translateX(-15px) scale(0.9);
  }

  75% {
    transform: translateY(-30px) translateX(5px) scale(1.05);
  }
}

/* 科技网格 */
.tech-grid {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
}

.grid-layer {
  position: absolute;
  width: 120%;
  height: 120%;
  top: -10%;
  left: -10%;
  opacity: 0.1;
}

.grid-primary {
  background-image:
    linear-gradient(rgba(138, 43, 226, 0.3) 1px, transparent 1px),
    linear-gradient(90deg, rgba(138, 43, 226, 0.3) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: gridMove1 25s linear infinite;
}

.grid-secondary {
  background-image:
    linear-gradient(rgba(0, 191, 255, 0.2) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 191, 255, 0.2) 1px, transparent 1px);
  background-size: 100px 100px;
  animation: gridMove2 15s linear infinite reverse;
}

@keyframes gridMove1 {
  0% {
    transform: translate(0, 0);
  }

  100% {
    transform: translate(50px, 50px);
  }
}

@keyframes gridMove2 {
  0% {
    transform: translate(0, 0);
  }

  100% {
    transform: translate(100px, 100px);
  }
}

/* 星空背景 */
.starfield {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
}

.star {
  position: absolute;
  width: 2px;
  height: 2px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  animation: twinkle 4s ease-in-out infinite;
}

@keyframes twinkle {

  0%,
  100% {
    opacity: 0.3;
    transform: scale(1);
  }

  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}

/* 星云效果 - 优化版 */
.nebula {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  animation: nebulaFloat 20s ease-in-out infinite;
}

.nebula-1 {
  width: 300px;
  height: 200px;
  background: radial-gradient(ellipse, rgba(138, 43, 226, 0.3) 0%, transparent 70%);
  top: 20%;
  right: 10%;
}

.nebula-2 {
  width: 250px;
  height: 150px;
  background: radial-gradient(ellipse, rgba(0, 191, 255, 0.2) 0%, transparent 70%);
  bottom: 30%;
  left: 15%;
  animation-delay: 10s;
}

/* 主要内容区域 */
.course-content-wrapper {
  position: relative;
  z-index: 1;
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px 20px;
  min-height: 100vh;
}

/* 玻璃拟态头部面板 */
.glassmorphism-header {
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(25px);
  border-radius: 25px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.3),
    0 2px 8px rgba(255, 255, 255, 0.1) inset;
  margin-bottom: 40px;
  overflow: hidden;
  position: relative;
  transform-style: preserve-3d;
  transition: all 0.6s ease;
}

.glassmorphism-header:hover {
  transform: perspective(1000px) rotateY(2deg) rotateX(1deg) translateZ(10px);
  box-shadow:
    0 15px 45px rgba(0, 0, 0, 0.4),
    0 4px 12px rgba(255, 255, 255, 0.2) inset;
}

.header-content {
  display: flex;
  gap: 40px;
  padding: 45px;
  align-items: flex-start;
}

.course-info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 30px;
}

/* 标题区域 */
.title-area {
  position: relative;
}

.cosmic-title {
  position: relative;
  margin: 0 0 20px 0;
  overflow: hidden;
}

.title-text {
  font-size: 3rem;
  font-weight: 700;
  background: linear-gradient(135deg, #ffffff 0%, #e0e7ff 50%, #c7d2fe 100%);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  text-shadow: 0 0 30px rgba(255, 255, 255, 0.5);
  display: block;
  line-height: 1.2;
  position: relative;
  z-index: 2;
}

.title-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(138, 43, 226, 0.3), rgba(0, 191, 255, 0.3));
  filter: blur(20px);
  z-index: 1;
  animation: titlePulse 3s ease-in-out infinite;
}

@keyframes titlePulse {

  0%,
  100% {
    opacity: 0.6;
    transform: scale(1);
  }

  50% {
    opacity: 1;
    transform: scale(1.05);
  }
}

/* 状态徽章 */
.status-badge {
  position: relative;
  display: inline-flex;
  align-items: center;
  padding: 12px 24px;
  border-radius: 50px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.status-badge:hover {
  transform: translateY(-2px) scale(1.05);
}

.badge-content {
  display: flex;
  align-items: center;
  gap: 8px;
  position: relative;
  z-index: 2;
}

.status-icon {
  font-size: 1.2rem;
}

.status-text {
  font-weight: 600;
  color: rgba(255, 255, 255, 0.95);
  font-size: 0.95rem;
}

.badge-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: inherit;
  z-index: 1;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.status-approved .badge-glow {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.4), rgba(16, 185, 129, 0.4));
}

.status-pending .badge-glow {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.4), rgba(251, 191, 36, 0.4));
}

.status-rejected .badge-glow {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.4), rgba(248, 113, 113, 0.4));
}

.status-badge:hover .badge-glow {
  opacity: 1;
}

/* 元数据网格 */
.meta-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 25px;
  margin: 30px 0;
}

.meta-card {
  position: relative;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.12) 0%, 
    rgba(255, 255, 255, 0.08) 100%);
  backdrop-filter: blur(20px);
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 30px;
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  transform-style: preserve-3d;
}

.meta-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.15), transparent);
  transition: left 0.8s ease;
  z-index: 1;
}

.meta-card:hover::before {
  left: 100%;
}

.meta-card:hover {
  transform: perspective(1000px) rotateY(-3deg) rotateX(2deg) translateZ(15px) scale(1.03);
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.18) 0%, 
    rgba(255, 255, 255, 0.12) 100%);
  border-color: rgba(138, 43, 226, 0.4);
  box-shadow: 
    0 25px 50px rgba(0, 0, 0, 0.4),
    0 0 30px rgba(138, 43, 226, 0.3),
    inset 0 2px 0 rgba(255, 255, 255, 0.3);
}

.meta-card .card-glow {
  position: absolute;
  top: -3px;
  left: -3px;
  right: -3px;
  bottom: -3px;
  border-radius: 25px;
  background: linear-gradient(45deg, 
    rgba(138, 43, 226, 0.6) 0%, 
    rgba(0, 191, 255, 0.4) 25%, 
    rgba(255, 20, 147, 0.6) 50%,
    rgba(0, 191, 255, 0.4) 75%,
    rgba(138, 43, 226, 0.6) 100%);
  opacity: 0;
  z-index: -1;
  transition: opacity 0.5s ease;
  animation: rotateGlow 6s linear infinite;
}

.meta-card:hover .card-glow {
  opacity: 1;
}

.meta-icon {
  font-size: 3rem;
  filter: drop-shadow(0 0 15px rgba(255, 255, 255, 0.4));
  transition: all 0.4s ease;
  position: relative;
  z-index: 2;
}

.meta-card:hover .meta-icon {
  transform: scale(1.2) rotateY(15deg);
  filter: drop-shadow(0 0 25px rgba(138, 43, 226, 0.8));
}

.meta-content {
  flex: 1;
  position: relative;
  z-index: 2;
}

.meta-label {
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.75);
  margin-bottom: 8px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
}

.meta-card:hover .meta-label {
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 0 10px rgba(138, 43, 226, 0.6);
}

.meta-value {
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.95);
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
  position: relative;
}

.meta-card:hover .meta-value {
  color: rgba(255, 255, 255, 0.98);
  text-shadow: 
    0 0 15px rgba(138, 43, 226, 0.8),
    0 2px 6px rgba(0, 0, 0, 0.4);
  transform: translateX(5px);
}

.meta-value::after {
  content: '';
  position: absolute;
  bottom: -3px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, rgba(138, 43, 226, 0.8), rgba(0, 191, 255, 0.6));
  border-radius: 1px;
  transition: width 0.4s ease;
}

.meta-card:hover .meta-value::after {
  width: 100%;
}



/* 描述面板 */
.description-panel {
  position: relative;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.12) 0%, 
    rgba(255, 255, 255, 0.08) 100%);
  backdrop-filter: blur(25px);
  border-radius: 25px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 40px;
  overflow: hidden;
  margin: 30px 0;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  transform-style: preserve-3d;
}

.description-panel::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transition: left 0.8s ease;
  z-index: 1;
}

.description-panel:hover::before {
  left: 100%;
}

.description-panel:hover {
  transform: perspective(1000px) rotateY(-1deg) rotateX(1deg) translateZ(10px) scale(1.01);
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.15) 0%, 
    rgba(255, 255, 255, 0.1) 100%);
  border-color: rgba(0, 191, 255, 0.4);
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.3),
    0 0 25px rgba(0, 191, 255, 0.3),
    inset 0 2px 0 rgba(255, 255, 255, 0.2);
}

.description-panel .panel-glow {
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  border-radius: 27px;
  background: linear-gradient(45deg, 
    rgba(0, 191, 255, 0.5) 0%, 
    rgba(138, 43, 226, 0.3) 25%, 
    rgba(255, 20, 147, 0.5) 50%,
    rgba(138, 43, 226, 0.3) 75%,
    rgba(0, 191, 255, 0.5) 100%);
  opacity: 0;
  z-index: -1;
  transition: opacity 0.5s ease;
  animation: rotateGlow 8s linear infinite;
}

.description-panel:hover .panel-glow {
  opacity: 1;
}

.description-content {
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.9;
  font-size: 1.15rem;
  position: relative;
  z-index: 2;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
  transition: all 0.3s ease;
  font-weight: 400;
  letter-spacing: 0.3px;
}

.description-panel:hover .description-content {
  color: rgba(255, 255, 255, 0.95);
  text-shadow: 
    0 0 10px rgba(0, 191, 255, 0.6),
    0 2px 10px rgba(0, 0, 0, 0.5);
  transform: translateY(-2px);
}



/* 选课按钮区域 */
.action-zone {
  margin-top: 30px;
}

.enroll-button {
  position: relative;
  background: none;
  border: none;
  padding: 18px 40px;
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.4s ease;
  overflow: hidden;
  transform-style: preserve-3d;
}

.enroll-button:not(.enrolled):not(.loading):hover {
  transform: translateY(-3px) scale(1.05);
}

.button-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: inherit;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 50%, #1e40af 100%);
  transition: all 0.3s ease;
}

.enroll-button.enrolled .button-background {
  background: linear-gradient(135deg, #10b981 0%, #059669 50%, #047857 100%);
}

.button-content {
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
  z-index: 2;
}

.button-icon {
  font-size: 1.2rem;
}

.button-text {
  font-size: 1.1rem;
  font-weight: 600;
  color: white;
  text-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
}

.button-glow {
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  border-radius: inherit;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.8), rgba(29, 78, 216, 0.8));
  z-index: 1;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.enroll-button:not(.enrolled):hover .button-glow {
  opacity: 1;
  animation: buttonPulse 2s ease-in-out infinite;
}

@keyframes buttonPulse {

  0%,
  100% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.05);
  }
}

.ripple-effect {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s ease, height 0.6s ease;
}

.enroll-button:active .ripple-effect {
  width: 300px;
  height: 300px;
}

/* 课程封面区域 */
.course-visual-section {
  flex: 0 0 400px;
}

.cover-container {
  position: relative;
  width: 100%;
  height: 300px;
  transform-style: preserve-3d;
}

.cover-frame {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 25px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.6s ease;
}

.cover-frame:hover {
  transform: perspective(1000px) rotateY(-5deg) rotateX(2deg) translateZ(20px);
}

.course-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.6s ease;
}

.cover-frame:hover .course-image {
  transform: scale(1.1);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(138, 43, 226, 0.2), rgba(0, 191, 255, 0.1));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.cover-frame:hover .image-overlay {
  opacity: 1;
}

.cover-glow {
  position: absolute;
  top: -10px;
  left: -10px;
  right: -10px;
  bottom: -10px;
  border-radius: 35px;
  background: linear-gradient(135deg, rgba(138, 43, 226, 0.4), rgba(0, 191, 255, 0.4));
  filter: blur(20px);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

.cover-frame:hover .cover-glow {
  opacity: 1;
  animation: coverPulse 3s ease-in-out infinite;
}

@keyframes coverPulse {

  0%,
  100% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.1);
  }
}



/* 主要内容面板 */
.main-content-panel {
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(25px);
  border-radius: 25px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  overflow: hidden;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.3),
    0 2px 8px rgba(255, 255, 255, 0.1) inset;
}

.glassmorphism-tabs {
  padding: 0;
}

.tab-header {
  background: rgba(255, 255, 255, 0.05);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding: 30px 45px 20px;
}

.tab-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 25px;
  position: relative;
}

.title-icon {
  font-size: 1.8rem;
  filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.5));
}

.title-text {
  font-size: 1.8rem;
  font-weight: 700;
  background: linear-gradient(135deg, #ffffff 0%, #e0e7ff 100%);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  text-shadow: 0 0 20px rgba(255, 255, 255, 0.3);
}

.title-decoration {
  flex: 1;
  height: 2px;
  background: linear-gradient(90deg, rgba(138, 43, 226, 0.6), rgba(0, 191, 255, 0.4), transparent);
  border-radius: 1px;
  margin-left: 20px;
}

.tab-navigation {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.tab-button {
  position: relative;
  background: none;
  border: none;
  padding: 15px 30px;
  border-radius: 18px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  transform-style: preserve-3d;
}

.tab-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.15), transparent);
  transition: left 0.6s ease;
  z-index: 1;
}

.tab-button:hover::before {
  left: 100%;
}

.tab-button:hover {
  transform: perspective(1000px) rotateY(5deg) rotateX(2deg) translateZ(8px) scale(1.03);
  box-shadow: 
    0 12px 25px rgba(0, 0, 0, 0.4),
    0 0 20px rgba(138, 43, 226, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.tab-button.active {
  transform: perspective(1000px) rotateY(8deg) rotateX(3deg) translateZ(12px) scale(1.05);
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.5),
    0 0 30px rgba(138, 43, 226, 0.6),
    0 0 60px rgba(0, 191, 255, 0.4),
    inset 0 2px 0 rgba(255, 255, 255, 0.3);
}

.tab-button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  transform: none;
}

.tab-button:disabled:hover {
  transform: none;
  box-shadow: none;
}

.tab-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: inherit;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(15px);
  transition: all 0.4s ease;
  z-index: 0;
}

.tab-button.active .tab-background {
  background: linear-gradient(135deg, 
    rgba(138, 43, 226, 0.25) 0%, 
    rgba(0, 191, 255, 0.15) 50%, 
    rgba(255, 20, 147, 0.2) 100%);
  border: 1px solid rgba(138, 43, 226, 0.4);
  backdrop-filter: blur(20px);
}

.tab-button:not(.disabled):not(.active):hover .tab-background {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(18px);
}

.tab-content {
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
  z-index: 3;
  transition: all 0.3s ease;
}

.tab-button:hover .tab-content {
  transform: translateZ(2px);
}

.tab-icon {
  font-size: 1.2rem;
  transition: all 0.3s ease;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.tab-button.active .tab-icon {
  transform: scale(1.1) rotate(5deg);
  filter: drop-shadow(0 0 8px rgba(138, 43, 226, 0.6));
}

.tab-button:hover .tab-icon {
  transform: scale(1.05) rotate(2deg);
}

.tab-label {
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.95rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
  position: relative;
}

.tab-button.active .tab-label {
  color: rgba(255, 255, 255, 0.98);
  text-shadow: 
    0 0 10px rgba(138, 43, 226, 0.8),
    0 2px 6px rgba(0, 0, 0, 0.4);
}

.tab-button.active .tab-label::after {
  content: '';
  position: absolute;
  bottom: -3px;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, rgba(138, 43, 226, 0.8), rgba(0, 191, 255, 0.6));
  border-radius: 1px;
  animation: labelGlow 2s ease-in-out infinite;
}

.tab-glow {
  position: absolute;
  top: -3px;
  left: -3px;
  right: -3px;
  bottom: -3px;
  border-radius: 22px;
  background: linear-gradient(45deg, 
    rgba(138, 43, 226, 0.6) 0%, 
    rgba(0, 191, 255, 0.4) 25%, 
    rgba(255, 20, 147, 0.5) 50%,
    rgba(138, 43, 226, 0.6) 75%,
    rgba(0, 191, 255, 0.4) 100%);
  opacity: 0;
  z-index: -1;
  transition: opacity 0.4s ease;
  animation: rotateGlow 4s linear infinite;
}

.tab-button.active .tab-glow {
  opacity: 1;
}

.tab-button:hover .tab-glow {
  opacity: 0.6;
}

@keyframes tabPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.02);
  }
}

@keyframes labelGlow {
  0%, 100% {
    opacity: 0.8;
    transform: scaleX(1);
  }
  50% {
    opacity: 1;
    transform: scaleX(1.05);
  }
}

@keyframes rotateGlow {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 标签面板 */
.tab-panel {
  padding: 45px;
  min-height: 500px;
}

/* 通用标题样式 */
.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0 0 25px 0;
  position: relative;
}

.section-title .title-icon {
  font-size: 1.5rem;
  filter: drop-shadow(0 0 8px rgba(255, 255, 255, 0.4));
}

.section-title span:not(.title-icon) {
  background: linear-gradient(135deg, #ffffff 0%, #e0e7ff 100%);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  text-shadow: 0 0 15px rgba(255, 255, 255, 0.2);
}



/* 章节内容样式 */
.chapter-container {
  position: relative;
}

.chapter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.add-chapter-btn {
  position: relative;
  background: none;
  border: none;
  padding: 15px 30px;
  border-radius: 18px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  transform-style: preserve-3d;
}

.add-chapter-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s ease;
  z-index: 1;
}

.add-chapter-btn:hover::before {
  left: 100%;
}

.add-chapter-btn:hover {
  transform: perspective(1000px) rotateY(-5deg) rotateX(3deg) translateZ(10px) scale(1.05);
  box-shadow: 
    0 15px 30px rgba(0, 0, 0, 0.4),
    0 0 25px rgba(34, 197, 94, 0.5),
    inset 0 2px 0 rgba(255, 255, 255, 0.3);
}

.add-chapter-btn:active {
  transform: perspective(1000px) rotateY(-2deg) rotateX(1deg) translateZ(5px) scale(1.02);
  transition: all 0.1s ease;
}

.add-chapter-btn .btn-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: inherit;
  background: linear-gradient(135deg, 
    rgba(34, 197, 94, 0.2) 0%, 
    rgba(21, 128, 61, 0.15) 100%);
  border: 1px solid rgba(34, 197, 94, 0.4);
  backdrop-filter: blur(15px);
  transition: all 0.4s ease;
  z-index: 0;
}

.add-chapter-btn:hover .btn-background {
  background: linear-gradient(135deg, 
    rgba(34, 197, 94, 0.35) 0%, 
    rgba(21, 128, 61, 0.25) 100%);
  border-color: rgba(34, 197, 94, 0.6);
  backdrop-filter: blur(20px);
}

.add-chapter-btn .btn-glow {
  position: absolute;
  top: -3px;
  left: -3px;
  right: -3px;
  bottom: -3px;
  border-radius: 22px;
  background: linear-gradient(45deg, 
    rgba(34, 197, 94, 0.8) 0%, 
    rgba(21, 128, 61, 0.6) 25%, 
    rgba(34, 197, 94, 0.8) 50%,
    rgba(21, 128, 61, 0.6) 75%,
    rgba(34, 197, 94, 0.8) 100%);
  opacity: 0;
  z-index: -1;
  transition: opacity 0.4s ease;
  animation: rotateGlow 4s linear infinite;
}

.add-chapter-btn:hover .btn-glow {
  opacity: 1;
}

.add-chapter-btn .btn-content {
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
  z-index: 3;
  transition: all 0.3s ease;
}

.add-chapter-btn:hover .btn-content {
  transform: translateZ(2px);
}

.add-chapter-btn .btn-icon {
  font-size: 1.1rem;
  transition: all 0.3s ease;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.add-chapter-btn:hover .btn-icon {
  transform: scale(1.2) rotate(90deg);
  filter: drop-shadow(0 0 8px rgba(34, 197, 94, 0.8));
}

.add-chapter-btn .btn-text {
  font-weight: 600;
  color: rgba(255, 255, 255, 0.95);
  font-size: 0.95rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
  position: relative;
}

.add-chapter-btn:hover .btn-text {
  color: rgba(255, 255, 255, 0.98);
  text-shadow: 
    0 0 10px rgba(34, 197, 94, 0.8),
    0 2px 6px rgba(0, 0, 0, 0.4);
}

.add-chapter-btn .btn-text::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, rgba(34, 197, 94, 0.8), rgba(21, 128, 61, 0.6));
  border-radius: 1px;
  transition: width 0.3s ease;
}

.add-chapter-btn:hover .btn-text::after {
  width: 100%;
}

/* 章节树样式 */
.chapter-tree {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  padding: 25px;
  backdrop-filter: blur(10px);
}

.cosmic-tree {
  background: transparent;
  color: rgba(255, 255, 255, 0.9);
}

.cosmic-tree .el-tree-node__content {
  position: relative;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.08) 0%, 
    rgba(255, 255, 255, 0.04) 100%);
  border-radius: 16px;
  margin-bottom: 12px;
  padding: 16px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(15px);
  transform-style: preserve-3d;
  overflow: hidden;
}

.cosmic-tree .el-tree-node__content::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(138, 43, 226, 0.1), transparent);
  transition: left 0.6s ease;
  z-index: 1;
}

.cosmic-tree .el-tree-node__content:hover::before {
  left: 100%;
}

.cosmic-tree .el-tree-node__content:hover {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.12) 0%, 
    rgba(138, 43, 226, 0.08) 50%,
    rgba(255, 255, 255, 0.06) 100%);
  border-color: rgba(138, 43, 226, 0.3);
  transform: perspective(1000px) rotateY(2deg) rotateX(1deg) translateZ(5px) translateX(8px);
  box-shadow: 
    0 12px 25px rgba(0, 0, 0, 0.3),
    0 0 20px rgba(138, 43, 226, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.chapter-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  gap: 18px;
  position: relative;
  z-index: 2;
}

.node-main {
  display: flex;
  align-items: center;
  gap: 15px;
  flex: 1;
  transition: all 0.3s ease;
}

.cosmic-tree .el-tree-node__content:hover .node-main {
  transform: translateZ(2px);
}

.node-icon {
  font-size: 1.3rem;
  width: 28px;
  text-align: center;
  transition: all 0.3s ease;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
  position: relative;
}

.node-icon::after {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(138, 43, 226, 0.3), transparent 70%);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

.cosmic-tree .el-tree-node__content:hover .node-icon::after {
  opacity: 1;
}

.cosmic-tree .el-tree-node__content:hover .node-icon {
  transform: scale(1.1) rotate(5deg);
  filter: drop-shadow(0 0 8px rgba(138, 43, 226, 0.6));
}

.node-content {
  position: relative;
  flex: 1;
}

.node-title {
  font-weight: 600;
  color: rgba(255, 255, 255, 0.95);
  font-size: 1.05rem;
  position: relative;
  z-index: 2;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
}

.cosmic-tree .el-tree-node__content:hover .node-title {
  color: rgba(255, 255, 255, 0.98);
  text-shadow: 
    0 0 10px rgba(138, 43, 226, 0.6),
    0 2px 6px rgba(0, 0, 0, 0.4);
  transform: translateX(3px);
}

/* 不同层级的节点样式 */
.cosmic-tree .el-tree-node[data-level="1"] .el-tree-node__content {
  background: linear-gradient(135deg, 
    rgba(138, 43, 226, 0.15) 0%, 
    rgba(255, 255, 255, 0.08) 100%);
  border-left: 3px solid rgba(138, 43, 226, 0.6);
}

.cosmic-tree .el-tree-node[data-level="2"] .el-tree-node__content {
  background: linear-gradient(135deg, 
    rgba(0, 191, 255, 0.12) 0%, 
    rgba(255, 255, 255, 0.06) 100%);
  border-left: 3px solid rgba(0, 191, 255, 0.5);
  margin-left: 20px;
}

.cosmic-tree .el-tree-node[data-level="3"] .el-tree-node__content {
  background: linear-gradient(135deg, 
    rgba(255, 20, 147, 0.12) 0%, 
    rgba(255, 255, 255, 0.05) 100%);
  border-left: 3px solid rgba(255, 20, 147, 0.5);
  margin-left: 40px;
}

.node-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: all 0.4s ease;
  transform: translateX(10px);
}

.cosmic-tree .el-tree-node__content:hover .node-actions {
  opacity: 1;
  transform: translateX(0);
}

/* 节点连接线美化 */
.cosmic-tree .el-tree-node__expand-icon {
  color: rgba(138, 43, 226, 0.7);
  transition: all 0.3s ease;
}

.cosmic-tree .el-tree-node__expand-icon:hover {
  color: rgba(138, 43, 226, 1);
  transform: scale(1.2);
  filter: drop-shadow(0 0 5px rgba(138, 43, 226, 0.8));
}

.action-btn {
  position: relative;
  background: none;
  border: none;
  width: 40px;
  height: 34px;
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  transform-style: preserve-3d;
  backdrop-filter: blur(10px);
}

.action-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
  z-index: 1;
}

.action-btn:hover::before {
  left: 100%;
}

.action-btn:hover {
  transform: perspective(1000px) rotateY(8deg) rotateX(4deg) translateZ(6px) scale(1.08);
  box-shadow: 
    0 8px 20px rgba(0, 0, 0, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.action-btn:active {
  transform: perspective(1000px) rotateY(4deg) rotateX(2deg) translateZ(2px) scale(1.02);
  transition: all 0.1s ease;
}

.action-btn .btn-icon {
  font-size: 1rem;
  position: relative;
  z-index: 3;
  transition: all 0.3s ease;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.action-btn:hover .btn-icon {
  transform: scale(1.1) rotate(5deg);
}

.action-btn .btn-ripple {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: inherit;
  opacity: 0;
  transition: all 0.4s ease;
  z-index: 0;
}

.action-btn .btn-glow {
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  border-radius: 15px;
  opacity: 0;
  z-index: -1;
  transition: opacity 0.4s ease;
  animation: rotateGlow 3s linear infinite;
}

.action-btn:hover .btn-glow {
  opacity: 1;
}

/* 考试和作业卡片中的按钮样式 */
.exam-actions .action-btn,
.homework-actions .action-btn {
  width: auto;
  padding: 8px 16px;
  gap: 6px;
  border-radius: 10px;
}

.exam-actions .edit-btn,
.homework-actions .edit-btn {
  min-width: 80px;
}

.exam-actions .delete-btn,
.homework-actions .delete-btn {
  min-width: 80px;
}

.action-btn .btn-text {
  font-size: 0.85rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.95);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 3;
}

/* 编辑按钮样式 */
.edit-btn {
  background: linear-gradient(135deg, 
    rgba(245, 158, 11, 0.25) 0%, 
    rgba(217, 119, 6, 0.15) 100%);
  border: 1px solid rgba(245, 158, 11, 0.4);
}

.edit-btn:hover .btn-ripple {
  opacity: 1;
  background: linear-gradient(135deg, 
    rgba(245, 158, 11, 0.4) 0%, 
    rgba(217, 119, 6, 0.3) 100%);
}

.edit-btn .btn-glow {
  background: linear-gradient(45deg, 
    rgba(245, 158, 11, 0.8) 0%, 
    rgba(217, 119, 6, 0.6) 25%, 
    rgba(245, 158, 11, 0.8) 50%,
    rgba(217, 119, 6, 0.6) 75%,
    rgba(245, 158, 11, 0.8) 100%);
}

.edit-btn:hover {
  box-shadow: 
    0 8px 20px rgba(0, 0, 0, 0.4),
    0 0 20px rgba(245, 158, 11, 0.5),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.edit-btn:hover .btn-icon {
  filter: drop-shadow(0 0 8px rgba(245, 158, 11, 0.8));
}

/* 添加按钮样式 */
.add-btn {
  background: linear-gradient(135deg, 
    rgba(34, 197, 94, 0.25) 0%, 
    rgba(21, 128, 61, 0.15) 100%);
  border: 1px solid rgba(34, 197, 94, 0.4);
}

.add-btn:hover .btn-ripple {
  opacity: 1;
  background: linear-gradient(135deg, 
    rgba(34, 197, 94, 0.4) 0%, 
    rgba(21, 128, 61, 0.3) 100%);
}

.add-btn .btn-glow {
  background: linear-gradient(45deg, 
    rgba(34, 197, 94, 0.8) 0%, 
    rgba(21, 128, 61, 0.6) 25%, 
    rgba(34, 197, 94, 0.8) 50%,
    rgba(21, 128, 61, 0.6) 75%,
    rgba(34, 197, 94, 0.8) 100%);
}

.add-btn:hover {
  box-shadow: 
    0 8px 20px rgba(0, 0, 0, 0.4),
    0 0 20px rgba(34, 197, 94, 0.5),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.add-btn:hover .btn-icon {
  filter: drop-shadow(0 0 8px rgba(34, 197, 94, 0.8));
}

/* 删除按钮样式 */
.delete-btn {
  background: linear-gradient(135deg, 
    rgba(239, 68, 68, 0.25) 0%, 
    rgba(185, 28, 28, 0.15) 100%);
  border: 1px solid rgba(239, 68, 68, 0.4);
}

.delete-btn:hover .btn-ripple {
  opacity: 1;
  background: linear-gradient(135deg, 
    rgba(239, 68, 68, 0.4) 0%, 
    rgba(185, 28, 28, 0.3) 100%);
}

.delete-btn .btn-glow {
  background: linear-gradient(45deg, 
    rgba(239, 68, 68, 0.8) 0%, 
    rgba(185, 28, 28, 0.6) 25%, 
    rgba(239, 68, 68, 0.8) 50%,
    rgba(185, 28, 28, 0.6) 75%,
    rgba(239, 68, 68, 0.8) 100%);
}

.delete-btn:hover {
  box-shadow: 
    0 8px 20px rgba(0, 0, 0, 0.4),
    0 0 20px rgba(239, 68, 68, 0.5),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.delete-btn:hover .btn-icon {
  filter: drop-shadow(0 0 8px rgba(239, 68, 68, 0.8));
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  position: relative;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  opacity: 0.6;
  animation: float 3s ease-in-out infinite;
}

.empty-text {
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
}



/* 卡片网格样式 */
.exams-grid,
.homework-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 25px;
  margin-top: 30px;
}

.exam-card,
.homework-card {
  position: relative;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  overflow: hidden;
  transition: all 0.4s ease;
  transform-style: preserve-3d;
}

.exam-card:hover,
.homework-card:hover {
  transform: perspective(1000px) rotateY(2deg) rotateX(1deg) translateZ(10px);
  border-color: rgba(255, 255, 255, 0.2);
}

.card-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(10px);
}

.card-content {
  padding: 30px;
  position: relative;
  z-index: 2;
}

.exam-header,
.homework-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.exam-title,
.homework-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.95);
  line-height: 1.3;
}

.exam-status,
.homework-status {
  font-size: 1.5rem;
  opacity: 0.7;
}

.homework-status {
  padding: 5px 12px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
}

.homework-status.status-pending {
  background: rgba(245, 158, 11, 0.2);
  color: rgba(245, 158, 11, 1);
  border: 1px solid rgba(245, 158, 11, 0.3);
}

.homework-status.status-submitted {
  background: rgba(34, 197, 94, 0.2);
  color: rgba(34, 197, 94, 1);
  border: 1px solid rgba(34, 197, 94, 0.3);
}

.homework-status.status-graded {
  background: rgba(139, 92, 246, 0.2);
  color: rgba(139, 92, 246, 1);
  border: 1px solid rgba(139, 92, 246, 0.3);
}

.homework-status.status-overdue {
  background: rgba(239, 68, 68, 0.2);
  color: rgba(239, 68, 68, 1);
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.exam-details,
.homework-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 25px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
}

.detail-icon {
  font-size: 1.1rem;
  width: 20px;
  text-align: center;
}

.detail-label {
  font-weight: 500;
  color: rgba(255, 255, 255, 0.7);
  min-width: 80px;
}

.detail-value {
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
}

.exam-actions,
.homework-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.primary-btn {
  background: rgba(59, 130, 246, 0.2) !important;
  border: 1px solid rgba(59, 130, 246, 0.3) !important;
  padding: 10px 20px !important;
  border-radius: 10px !important;
  color: rgba(255, 255, 255, 0.95) !important;
  font-weight: 600 !important;
  display: flex !important;
  align-items: center !important;
  gap: 8px !important;
  transition: all 0.3s ease !important;
}

.primary-btn:hover {
  background: rgba(59, 130, 246, 0.4) !important;
  border-color: rgba(59, 130, 246, 0.5) !important;
  transform: translateY(-2px) !important;
}



/* 头部按钮样式统一 */
.create-exam-btn,
.create-homework-btn {
  position: relative;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 16px 32px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  transform-style: preserve-3d;
  box-shadow: 
    0 8px 25px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.create-exam-btn::before,
.create-homework-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s ease;
  z-index: 1;
}

.create-exam-btn:hover::before,
.create-homework-btn:hover::before {
  left: 100%;
}

.create-exam-btn::after,
.create-homework-btn::after {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  border-radius: 22px;
  opacity: 0;
  z-index: -1;
  transition: opacity 0.3s ease;
  filter: blur(6px);
}

.create-exam-btn:hover::after,
.create-homework-btn:hover::after {
  opacity: 1;
}

.create-exam-btn:hover,
.create-homework-btn:hover {
  transform: perspective(1000px) rotateY(8deg) rotateX(3deg) translateZ(15px) scale(1.05);
  box-shadow: 
    0 15px 35px rgba(0, 0, 0, 0.4),
    0 0 25px rgba(59, 130, 246, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

/* 创建考试按钮特定样式 */
.create-exam-btn {
  background: linear-gradient(135deg, 
    rgba(59, 130, 246, 0.25) 0%, 
    rgba(29, 78, 216, 0.15) 100%);
  border-color: rgba(59, 130, 246, 0.4);
}

.create-exam-btn::after {
  background: linear-gradient(135deg, 
    rgba(59, 130, 246, 0.8) 0%, 
    rgba(29, 78, 216, 0.6) 100%);
}

.create-exam-btn:hover {
  background: linear-gradient(135deg, 
    rgba(59, 130, 246, 0.35) 0%, 
    rgba(29, 78, 216, 0.25) 100%);
  border-color: rgba(59, 130, 246, 0.6);
  box-shadow: 
    0 15px 35px rgba(0, 0, 0, 0.4),
    0 0 25px rgba(59, 130, 246, 0.5),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.create-exam-btn .btn-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.3s ease;
}

.create-exam-btn:hover .btn-content {
  transform: translateZ(5px);
}

.create-exam-btn .btn-icon {
  font-size: 1.2rem;
  transition: all 0.3s ease;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.create-exam-btn:hover .btn-icon {
  transform: rotate(180deg) scale(1.2);
  filter: drop-shadow(0 0 10px rgba(59, 130, 246, 0.8));
}

.create-exam-btn .btn-text {
  font-size: 1rem;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.95);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
  position: relative;
}

.create-exam-btn:hover .btn-text {
  color: rgba(255, 255, 255, 1);
  text-shadow: 
    0 0 10px rgba(59, 130, 246, 0.8),
    0 2px 6px rgba(0, 0, 0, 0.4);
}

.create-exam-btn .btn-text::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, 
    rgba(59, 130, 246, 0.8), 
    rgba(29, 78, 216, 0.6));
  transition: width 0.3s ease;
}

.create-exam-btn:hover .btn-text::after {
  width: 100%;
}

/* 创建作业按钮特定样式 */
.create-homework-btn {
  background: linear-gradient(135deg, 
    rgba(139, 92, 246, 0.25) 0%, 
    rgba(124, 58, 237, 0.15) 100%);
  border-color: rgba(139, 92, 246, 0.4);
}

.create-homework-btn::after {
  background: linear-gradient(135deg, 
    rgba(139, 92, 246, 0.8) 0%, 
    rgba(124, 58, 237, 0.6) 100%);
}

.create-homework-btn:hover {
  background: linear-gradient(135deg, 
    rgba(139, 92, 246, 0.35) 0%, 
    rgba(124, 58, 237, 0.25) 100%);
  border-color: rgba(139, 92, 246, 0.6);
  box-shadow: 
    0 15px 35px rgba(0, 0, 0, 0.4),
    0 0 25px rgba(139, 92, 246, 0.5),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.create-homework-btn .btn-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.3s ease;
}

.create-homework-btn:hover .btn-content {
  transform: translateZ(5px);
}

.create-homework-btn .btn-icon {
  font-size: 1.2rem;
  transition: all 0.3s ease;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.create-homework-btn:hover .btn-icon {
  transform: rotate(180deg) scale(1.2);
  filter: drop-shadow(0 0 10px rgba(139, 92, 246, 0.8));
}

.create-homework-btn .btn-text {
  font-size: 1rem;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.95);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
  position: relative;
}

.create-homework-btn:hover .btn-text {
  color: rgba(255, 255, 255, 1);
  text-shadow: 
    0 0 10px rgba(139, 92, 246, 0.8),
    0 2px 6px rgba(0, 0, 0, 0.4);
}

.create-homework-btn .btn-text::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, 
    rgba(139, 92, 246, 0.8), 
    rgba(124, 58, 237, 0.6));
  transition: width 0.3s ease;
}

.create-homework-btn:hover .btn-text::after {
  width: 100%;
}

/* 评论区额外样式 */
.comments-container {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

/* 评论卡片样式 */
.comment-card {
  position: relative;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  padding: 25px;
  margin-bottom: 20px;
  transition: all 0.4s ease;
  transform-style: preserve-3d;
  overflow: hidden;
}

.comment-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transition: left 0.6s ease;
  z-index: 1;
}

.comment-card:hover {
  transform: perspective(1000px) rotateY(2deg) rotateX(1deg) translateZ(10px) scale(1.02);
  border-color: rgba(138, 43, 226, 0.3);
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.3),
    0 0 30px rgba(138, 43, 226, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.comment-card:hover::before {
  left: 100%;
}

.comment-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 50% 50%, rgba(138, 43, 226, 0.05), transparent 70%);
  border-radius: inherit;
  z-index: 0;
}

.comment-content-wrapper {
  position: relative;
  z-index: 2;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

/* 用户信息区域美化 */
.user-section {
  display: flex;
  align-items: center;
  gap: 15px;
  position: relative;
}

.avatar-container {
  position: relative;
  transform-style: preserve-3d;
}

.avatar-circle {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(138, 43, 226, 0.8), rgba(0, 191, 255, 0.6));
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: all 0.3s ease;
  box-shadow: 
    0 8px 16px rgba(0, 0, 0, 0.3),
    0 0 20px rgba(138, 43, 226, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.avatar-circle::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  border-radius: 50%;
  background: linear-gradient(45deg, rgba(138, 43, 226, 0.6), rgba(0, 191, 255, 0.4), rgba(255, 20, 147, 0.5));
  z-index: -1;
  opacity: 0;
  transition: opacity 0.3s ease;
  animation: rotate 3s linear infinite;
}

.comment-card:hover .avatar-circle::before {
  opacity: 1;
}

.comment-card:hover .avatar-circle {
  transform: translateZ(5px) scale(1.1);
  box-shadow: 
    0 12px 24px rgba(0, 0, 0, 0.4),
    0 0 30px rgba(138, 43, 226, 0.6),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.avatar-text {
  font-size: 1.2rem;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.95);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  position: relative;
  z-index: 2;
}

/* 用户详情美化 */
.user-details {
  display: flex;
  flex-direction: column;
  gap: 5px;
  position: relative;
}

.username {
  font-size: 1.1rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.95);
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
  position: relative;
  transition: all 0.3s ease;
}

.username::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, rgba(138, 43, 226, 0.8), rgba(0, 191, 255, 0.6));
  border-radius: 1px;
  transition: width 0.3s ease;
}

.comment-card:hover .username::after {
  width: 100%;
}

.comment-time {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 500;
  position: relative;
  padding-left: 15px;
  transition: all 0.3s ease;
}

.comment-time::before {
  content: '🕒';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  font-size: 0.8rem;
  opacity: 0.7;
  transition: all 0.3s ease;
}

.comment-card:hover .comment-time {
  color: rgba(255, 255, 255, 0.8);
  transform: translateX(3px);
}

.comment-card:hover .comment-time::before {
  opacity: 1;
  transform: translateY(-50%) scale(1.1);
}

/* 评分区域美化 */
.rating-section {
  position: relative;
  padding: 8px 15px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.comment-card:hover .rating-section {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 215, 0, 0.3);
  box-shadow: 0 0 15px rgba(255, 215, 0, 0.2);
}

/* 评论内容美化 */
.comment-body {
  color: rgba(255, 255, 255, 0.85);
  line-height: 1.7;
  font-size: 1rem;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  position: relative;
  padding: 15px 0;
  transition: all 0.3s ease;
}

.comment-card:hover .comment-body {
  color: rgba(255, 255, 255, 0.95);
  transform: translateX(5px);
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.stats-panel {
  position: relative;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.12) 0%, 
    rgba(255, 255, 255, 0.08) 100%);
  border-radius: 25px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 40px;
  backdrop-filter: blur(20px);
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  transform-style: preserve-3d;
  overflow: hidden;
}

.stats-panel::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transition: left 0.8s ease;
  z-index: 1;
}

.stats-panel:hover::before {
  left: 100%;
}

.stats-panel:hover {
  transform: perspective(1000px) rotateY(-1deg) rotateX(1deg) translateZ(10px) scale(1.01);
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.15) 0%, 
    rgba(255, 255, 255, 0.1) 100%);
  border-color: rgba(255, 215, 0, 0.4);
  box-shadow: 
    0 25px 50px rgba(0, 0, 0, 0.3),
    0 0 30px rgba(255, 215, 0, 0.3),
    inset 0 2px 0 rgba(255, 255, 255, 0.2);
}

.stats-panel .panel-glow {
  position: absolute;
  top: -3px;
  left: -3px;
  right: -3px;
  bottom: -3px;
  border-radius: 28px;
  background: linear-gradient(45deg, 
    rgba(255, 215, 0, 0.6) 0%, 
    rgba(138, 43, 226, 0.4) 25%, 
    rgba(0, 191, 255, 0.6) 50%,
    rgba(138, 43, 226, 0.4) 75%,
    rgba(255, 215, 0, 0.6) 100%);
  opacity: 0;
  z-index: -1;
  transition: opacity 0.5s ease;
  animation: rotateGlow 8s linear infinite;
}

.stats-panel:hover .panel-glow {
  opacity: 1;
}

.rating-dashboard {
  display: flex;
  gap: 50px;
  align-items: center;
  flex-wrap: wrap;
  position: relative;
  z-index: 2;
}

.overall-rating {
  display: flex;
  align-items: center;
  gap: 40px;
  position: relative;
}

.rating-circle {
  position: relative;
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: conic-gradient(from 0deg, 
    rgba(255, 215, 0, 0.9) var(--progress, 0%), 
    rgba(255, 255, 255, 0.15) var(--progress, 0%));
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  transition: all 0.4s ease;
  transform-style: preserve-3d;
  box-shadow: 
    0 10px 30px rgba(0, 0, 0, 0.3),
    inset 0 2px 0 rgba(255, 255, 255, 0.3);
}

.rating-circle:hover {
  transform: perspective(1000px) rotateY(10deg) rotateX(5deg) translateZ(15px) scale(1.05);
  box-shadow: 
    0 20px 50px rgba(0, 0, 0, 0.4),
    0 0 40px rgba(255, 215, 0, 0.5),
    inset 0 2px 0 rgba(255, 255, 255, 0.4);
}

.rating-circle::before {
  content: '';
  position: absolute;
  width: 110px;
  height: 110px;
  border-radius: 50%;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.15) 0%, 
    rgba(255, 255, 255, 0.08) 100%);
  backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.4s ease;
}

.rating-circle:hover::before {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.2) 0%, 
    rgba(255, 255, 255, 0.12) 100%);
  border-color: rgba(255, 215, 0, 0.4);
}

.rating-circle .circle-glow {
  position: absolute;
  top: -5px;
  left: -5px;
  right: -5px;
  bottom: -5px;
  border-radius: 50%;
  background: linear-gradient(45deg, 
    rgba(255, 215, 0, 0.8) 0%, 
    rgba(255, 193, 7, 0.6) 25%, 
    rgba(255, 215, 0, 0.8) 50%,
    rgba(255, 193, 7, 0.6) 75%,
    rgba(255, 215, 0, 0.8) 100%);
  opacity: 0;
  z-index: -1;
  transition: opacity 0.4s ease;
  animation: rotateGlow 6s linear infinite;
}

.rating-circle:hover .circle-glow {
  opacity: 1;
}

.rating-value {
  font-size: 2.8rem;
  font-weight: 800;
  color: rgba(255, 255, 255, 0.98);
  position: relative;
  z-index: 3;
  text-shadow: 
    0 0 15px rgba(255, 215, 0, 0.8),
    0 2px 6px rgba(0, 0, 0, 0.4);
  transition: all 0.3s ease;
}

.rating-circle:hover .rating-value {
  transform: scale(1.1);
  text-shadow: 
    0 0 25px rgba(255, 215, 0, 1),
    0 2px 8px rgba(0, 0, 0, 0.5);
}

.rating-max {
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.8);
  position: relative;
  z-index: 3;
  margin-top: -8px;
  font-weight: 600;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
}

.rating-circle:hover .rating-max {
  color: rgba(255, 255, 255, 0.9);
  transform: translateY(-2px);
}

.rating-details {
  text-align: center;
  position: relative;
}

.total-count {
  color: rgba(255, 255, 255, 0.85);
  margin-top: 15px;
  font-size: 1.2rem;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
}

.overall-rating:hover .total-count {
  color: rgba(255, 255, 255, 0.95);
  text-shadow: 
    0 0 10px rgba(255, 215, 0, 0.6),
    0 2px 6px rgba(0, 0, 0, 0.4);
  transform: translateY(-2px);
}

.distribution-chart {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-width: 320px;
  position: relative;
}

.chart-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 12px 0;
  transition: all 0.3s ease;
  position: relative;
}

.chart-item:hover {
  transform: translateX(5px);
}

.star-label {
  width: 70px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 700;
  font-size: 1rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
  position: relative;
}

.chart-item:hover .star-label {
  color: rgba(255, 215, 0, 0.95);
  text-shadow: 
    0 0 10px rgba(255, 215, 0, 0.8),
    0 2px 6px rgba(0, 0, 0, 0.4);
  transform: scale(1.1);
}

.progress-track {
  flex: 1;
  height: 16px;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.15) 0%, 
    rgba(255, 255, 255, 0.08) 100%);
  border-radius: 10px;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.chart-item:hover .progress-track {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.2) 0%, 
    rgba(255, 255, 255, 0.12) 100%);
  border-color: rgba(255, 215, 0, 0.3);
  box-shadow: 
    0 4px 15px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, 
    rgba(255, 215, 0, 0.9) 0%, 
    rgba(255, 193, 7, 0.7) 50%,
    rgba(255, 215, 0, 0.9) 100%);
  border-radius: inherit;
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.progress-fill::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: left 0.6s ease;
}

.chart-item:hover .progress-fill::before {
  left: 100%;
}

.progress-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, 
    rgba(255, 215, 0, 0.3) 0%, 
    rgba(255, 193, 7, 0.2) 50%,
    rgba(255, 215, 0, 0.3) 100%);
  border-radius: inherit;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.chart-item:hover .progress-fill::after {
  opacity: 1;
}

.progress-glow {
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  border-radius: 12px;
  background: linear-gradient(90deg, 
    rgba(255, 215, 0, 0.6) 0%, 
    rgba(255, 193, 7, 0.4) 50%,
    rgba(255, 215, 0, 0.6) 100%);
  opacity: 0;
  z-index: -1;
  transition: opacity 0.3s ease;
  filter: blur(4px);
}

.chart-item:hover .progress-glow {
  opacity: 1;
}

.percentage-label {
  width: 60px;
  text-align: right;
  color: rgba(255, 255, 255, 0.85);
  font-weight: 700;
  font-size: 0.95rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
}

.chart-item:hover .percentage-label {
  color: rgba(255, 255, 255, 0.95);
  text-shadow: 
    0 0 8px rgba(255, 215, 0, 0.6),
    0 2px 6px rgba(0, 0, 0, 0.4);
  transform: scale(1.05);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .header-content {
    flex-direction: column;
    gap: 30px;
  }

  .course-visual-section {
    flex: none;
    align-self: center;
  }

  .meta-grid {
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  }
}

@media (max-width: 768px) {
  .course-content-wrapper {
    padding: 20px 15px;
  }

  .header-content {
    padding: 30px;
  }

  .cosmic-title .title-text {
    font-size: 2.2rem;
  }

  .meta-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .tab-header {
    padding: 20px 25px 15px;
  }

  .tab-panel {
    padding: 25px;
  }

  .tab-navigation {
    flex-direction: column;
    gap: 8px;
  }

  .tab-button {
    width: 100%;
    justify-content: center;
  }

  .cover-container {
    height: 250px;
  }
}

@media (max-width: 480px) {
  .cosmic-title .title-text {
    font-size: 1.8rem;
  }

  .header-content {
    padding: 20px;
  }

  .tab-header {
    padding: 15px 20px;
  }

  .tab-panel {
    padding: 20px;
  }

  .meta-card {
    padding: 20px;
  }

  .meta-icon {
    font-size: 2rem;
  }

  .cover-container {
    height: 200px;
  }

  .chapter-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .node-actions {
    opacity: 1;
  }
}

/* 深色主题优化 */
.cosmic-tree .el-tree-node__expand-icon {
  color: rgba(255, 255, 255, 0.7);
}

.cosmic-tree .el-tree-node__expand-icon:hover {
  color: rgba(255, 255, 255, 1);
}

/* Element Plus 组件样式覆盖 */
.el-loading-mask {
  background-color: rgba(15, 12, 41, 0.8) !important;
}

.el-dialog {
  background: rgba(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(25px) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4) !important;
}

.el-dialog__header {
  background: rgba(255, 255, 255, 0.05) !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
}

.el-dialog__title {
  color: rgba(255, 255, 255, 0.95) !important;
  font-weight: 600 !important;
}

.el-dialog__body {
  background: transparent !important;
}

.el-form-item__label {
  color: rgba(255, 255, 255, 0.9) !important;
  font-weight: 500 !important;
}

.el-input__wrapper {
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  border-radius: 10px !important;
  backdrop-filter: blur(10px) !important;
}

.el-input__wrapper:hover {
  border-color: rgba(255, 255, 255, 0.3) !important;
}

.el-input__wrapper.is-focus {
  border-color: rgba(138, 43, 226, 0.6) !important;
  box-shadow: 0 0 0 2px rgba(138, 43, 226, 0.2) !important;
}

.el-input__inner {
  color: rgba(255, 255, 255, 0.95) !important;
}

.el-input__inner::placeholder {
  color: rgba(255, 255, 255, 0.5) !important;
}

.el-button--primary {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%) !important;
  border: none !important;
  border-radius: 10px !important;
  font-weight: 600 !important;
}

.el-button--primary:hover {
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%) !important;
  transform: translateY(-1px) !important;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4) !important;
}

/* 动画优化 */
* {
  scrollbar-width: thin;
  scrollbar-color: rgba(138, 43, 226, 0.5) transparent;
}

::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, rgba(138, 43, 226, 0.5), rgba(0, 191, 255, 0.5));
  border-radius: 10px;
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, rgba(138, 43, 226, 0.7), rgba(0, 191, 255, 0.7));
}
</style>