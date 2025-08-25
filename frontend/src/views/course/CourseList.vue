<template>
  <div class="course-list-container">
    <div class="page-header">
      <div class="header-background"></div>
      <div class="header-content">
        <h2>🎓 课程中心</h2>
        <p>探索丰富的课程资源，开启您的学习之旅</p>
      </div>
    </div>

    <div class="search-section">
      <div class="search-card">
        <el-row :gutter="20" class="search-form">
          <el-col :span="8">
            <div class="input-wrapper">
              <el-input v-model="searchForm.keyword" placeholder="🔍 搜索课程名称或描述" clearable @input="handleSearch"
                size="large" class="search-input" />
            </div>
          </el-col>
          <el-col :span="5">
            <el-select v-model="searchForm.category" placeholder="📚 选择分类" clearable @change="handleSearch" size="large"
              class="search-select">
              <el-option label="全部分类" value="" />
              <el-option label="💻 前端开发" value="前端开发" />
              <el-option label="⚙️ 后端开发" value="后端开发" />
              <el-option label="📱 移动开发" value="移动开发" />
              <el-option label="📊 大数据" value="大数据" />
              <el-option label="🤖 机器学习" value="机器学习" />
            </el-select>
          </el-col>
          <el-col :span="5">
            <el-select v-model="searchForm.state" placeholder="📋 选择状态" clearable @change="handleSearch" size="large"
              class="search-select">
              <el-option label="全部状态" value="" />
              <el-option label="✅ 已审核" value="1" />
              <el-option label="⏳ 审核中" value="0" />
              <el-option label="❌ 已拒绝" value="2" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-button type="primary" @click="handleSearch" size="large" class="search-btn">
              <i class="el-icon-search"></i>
              搜索课程
            </el-button>
          </el-col>
        </el-row>
      </div>
    </div>

    <div class="course-grid" v-loading="loading">
      <el-row :gutter="20">
        <el-col v-for="course in courseList" :key="course.id" :xs="24" :sm="12" :md="8" :lg="6">
          <div class="course-card-wrapper">
            <el-card class="course-card" @click="viewCourse(course.id)" shadow="hover">
              <div class="course-image">
                <img :src="course.coverUrl || '/default-course.jpg'" :alt="course.title">
                <div class="image-overlay">
                  <div class="overlay-content">
                    <i class="el-icon-view"></i>
                    <span>查看详情</span>
                  </div>
                </div>
                <div class="course-status-badge">
                  <el-tag v-if="course.state === 1" type="success" size="small" effect="dark">✅ 已审核</el-tag>
                  <el-tag v-else-if="course.state === 0" type="warning" size="small" effect="dark">⏳ 审核中</el-tag>
                  <el-tag v-else type="danger" size="small" effect="dark">❌ 已拒绝</el-tag>
                </div>
              </div>

              <div class="course-content">
                <div class="course-header">
                  <h3 class="course-title">{{ course.title }}</h3>
                </div>
                <hr/>
                <div class="course-rating-info">
                  <div class="course-rating">
                    <div class="stars">
                      <el-rate :model-value="course.avgRating / 2" disabled text-color="#ff9900" size="small" />
                    </div>
                    <span class="rating-text">{{ course.avgRating ? course.avgRating.toFixed(1) : '0.0' }}</span>
                  </div>
                </div>

                <p class="course-description">{{ course.description }}</p>

                <div class="course-meta">
                  <div class="meta-item teacher">
                    <div class="meta-content">
                      <span class="meta-label">👨‍🏫 讲师：</span>
                      <span class="meta-value">{{ course.teacherName || '未知教师' }}</span>
                    </div>
                  </div>
                </div>

                <div class="course-footer">
                  <div class="course-price">
                    <span v-if="course.price > 0" class="price">
                      <span class="currency">¥</span>
                      <span class="amount">{{ course.price }}</span>
                    </span>
                    <span v-else class="free">
                      <i class="el-icon-present"></i>
                      免费学习
                    </span>
                  </div>
                  <div class="course-action">
                    <el-button type="primary" size="small" class="view-btn">
                      立即学习
                      <i class="el-icon-right"></i>
                    </el-button>
                  </div>
                </div>
              </div>
            </el-card>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="pagination-section">
      <el-pagination v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize"
        :total="pagination.total" :page-sizes="[12, 24, 36, 48]" layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { courseApi } from '../../api/course';
import { commentApi } from '../../api/comment';
import { userApi } from '../../api/user';

const router = useRouter();

const searchForm = reactive({
  keyword: '',
  category: '' || undefined,
  state: '' || undefined
});

const courseList = ref([]);
const loading = ref(false);

const pagination = reactive({
  pageNum: 1,
  pageSize: 12,
  total: 0
});

const handleSearch = async () => {
  pagination.pageNum = 1;
  await fetchCourseList();
};

const handleSizeChange = async (size) => {
  pagination.pageSize = size;
  pagination.pageNum = 1;
  await fetchCourseList();
};

const handleCurrentChange = async (page) => {
  pagination.pageNum = page;
  await fetchCourseList();
};

const fetchCourseList = async () => {
  try {
    loading.value = true;

    // 清空当前课程列表，避免显示旧数据
    courseList.value = [];

    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      category: searchForm.category || undefined,
      state: searchForm.state || undefined
    };

    // 过滤掉空值参数
    Object.keys(params).forEach(key => {
      if (params[key] === undefined || params[key] === '') {
        delete params[key];
      }
    });
    const response = await courseApi.getCourseList(params);
    const courses = response.records || [];

    // 获取每个课程的教师名称和评分信息
    const coursesWithDetails = await Promise.all(courses.map(async (course) => {
      // 获取教师信息
      let teacherName = '未知教师';
      if (course.teacherId) {
        try {
          const teacherResponse = await userApi.getUserDetail(course.teacherId);
          teacherName = teacherResponse.username || '未知教师';
        } catch (err) {
          console.error('获取教师信息失败:', err);
        }
      }

      // 获取课程评分
      let avgRating = course.rating || 0;
      try {
        const statsResponse = await commentApi.getCommentStats(course.id);
        if (statsResponse && statsResponse.avgRating !== undefined) {
          avgRating = statsResponse.avgRating;
        }
      } catch (err) {
        console.error('获取课程评分失败:', err);
      }

      return {
        ...course,
        teacherName,
        avgRating
      };
    }));

    courseList.value = coursesWithDetails;
    console.log('处理后的课程列表:', coursesWithDetails);
    console.log('课程数量:', coursesWithDetails.length);

    pagination.total = response.total || 0;
    console.log('总数:', pagination.total);
  } catch (error) {
    ElMessage.error('获取课程列表失败');
    console.error('获取课程列表错误:', error);
  } finally {
    loading.value = false;
  }
};

const viewCourse = (courseId) => {
  router.push(`/course/${courseId}`);
};

onMounted(() => {
  fetchCourseList();
});
</script>

<style scoped>
.course-list-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
}

/* 页面头部样式 */
.page-header {
  position: relative;
  text-align: center;
  margin-bottom: 40px;
  padding: 60px 0;
  overflow: hidden;
}

.header-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 32px;
  box-shadow: 0 20px 60px rgba(102, 126, 234, 0.4),
    0 10px 30px rgba(118, 75, 162, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.header-background::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  animation: float 6s ease-in-out infinite;
}

@keyframes float {

  0%,
  100% {
    transform: translateY(0px) rotate(0deg);
  }

  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

.header-content {
  position: relative;
  z-index: 2;
}

.header-content h2 {
  font-size: 42px;
  color: white;
  margin: 0 0 16px 0;
  font-weight: 700;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
  letter-spacing: 1px;
}

.header-content p {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 0 30px 0;
  text-shadow: 0 1px 5px rgba(0, 0, 0, 0.2);
}

.header-stats {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-top: 20px;
}

.stat-item {
  text-align: center;
  color: white;
}

.stat-number {
  display: block;
  font-size: 32px;
  font-weight: 700;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.stat-label {
  display: block;
  font-size: 14px;
  opacity: 0.9;
  margin-top: 5px;
}

/* 搜索区域样式 */
.search-section {
  margin-bottom: 40px;
}

.search-card {
  background: white;
  padding: 30px;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.search-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.search-title i {
  color: #667eea;
  font-size: 20px;
}

.search-form {
  align-items: end;
}

.input-wrapper {
  position: relative;
}

.search-input {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-select {
  border-radius: 12px;
}

.search-btn {
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

/* 课程网格样式 */
.course-grid {
  margin-bottom: 40px;
}

.course-card-wrapper {
  margin-bottom: 30px;
  height: 100%;
}

.course-card {
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  height: 100%;
  border-radius: 20px;
  overflow: hidden;
  border: none;
  background: white;
  position: relative;
}

.course-card:hover {
  transform: translateY(-10px) scale(1.02);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

.course-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea, #764ba2, #f093fb, #f5576c);
  z-index: 1;
}

/* 课程图片样式 */
.course-image {
  position: relative;
  height: 220px;
  overflow: hidden;
}

.course-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.course-card:hover .course-image img {
  transform: scale(1.1);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.8), rgba(118, 75, 162, 0.8));
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.course-card:hover .image-overlay {
  opacity: 1;
}

.overlay-content {
  text-align: center;
  color: white;
  font-weight: 600;
}

.overlay-content i {
  font-size: 24px;
  margin-bottom: 8px;
  display: block;
}

.course-status-badge {
  position: absolute;
  top: 15px;
  right: 15px;
  z-index: 2;
}

/* 课程内容样式 */
.course-content {
  padding: 24px;
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 15px;
}

.course-title {
  font-size: 20px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
  line-height: 1.4;
  flex: 1;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.course-rating-info {
  margin-left:110px ; 
}

.course-rating {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.stars {
  display: flex;
  gap: 2px;
}

.rating-text {
  font-size: 14px;
  font-weight: 600;
  color: #666;
}

.course-description {
  color: #7f8c8d;
  font-size: 15px;
  line-height: 1.6;
  margin: 0 0 20px 0;
  height: 48px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 课程元信息样式 */
.course-meta {
  margin-bottom: 20px;
}

.meta-item.teacher {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: linear-gradient(135deg, #f8f9ff 0%, #e8f2ff 100%);
  border-radius: 12px;
  border-left: 4px solid #667eea;
}

.meta-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.meta-content {
  flex: 1;
}

.meta-label {
  font-size: 13px;
  color: #8e8e93;
  font-weight: 500;
}

.meta-value {
  font-size: 15px;
  color: #2c3e50;
  font-weight: 600;
  margin-left: 8px;
}

/* 课程底部样式 */
.course-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.course-price {
  flex: 1;
}

.course-price .price {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.currency {
  font-size: 16px;
  font-weight: 600;
  color: #e74c3c;
}

.amount {
  font-size: 24px;
  font-weight: 700;
  color: #e74c3c;
}

.course-price .free {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 600;
  color: #27ae60;
}

.course-price .free i {
  font-size: 18px;
}

.course-action {
  flex-shrink: 0;
}

.view-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 20px;
  padding: 8px 20px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.view-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

/* 分页样式 */
.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 50px;
  padding: 25px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.pagination-section:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.pagination-section :deep(.el-pagination .btn-prev),
.pagination-section :deep(.el-pagination .btn-next) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.pagination-section :deep(.el-pagination .btn-prev:hover),
.pagination-section :deep(.el-pagination .btn-next:hover) {
  transform: scale(1.1);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.pagination-section :deep(.el-pagination .el-pager li) {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(116, 108, 108, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  margin: 0 3px;
  transition: all 0.3s ease;
}

.pagination-section :deep(.el-pagination .el-pager li:hover) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.3);
}

.pagination-section :deep(.el-pagination .el-pager li.is-active) {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  transform: scale(1.1);
  box-shadow: 0 5px 20px rgba(240, 147, 251, 0.4);
}


/* 响应式设计 */
@media (max-width: 768px) {
  .course-list-container {
    padding: 0 15px;
  }

  .header-content h2 {
    font-size: 32px;
  }

  .header-stats {
    gap: 20px;
  }

  .search-card {
    padding: 20px;
  }

  .search-form .el-col {
    margin-bottom: 15px;
  }

  .course-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .course-rating {
    align-self: flex-end;
  }
}

/* 加载动画 */
@keyframes pulse {
  0% {
    opacity: 1;
  }

  50% {
    opacity: 0.5;
  }

  100% {
    opacity: 1;
  }
}

.course-card-wrapper:nth-child(even) .course-card {
  animation-delay: 0.1s;
}

.course-card-wrapper:nth-child(3n) .course-card {
  animation-delay: 0.2s;
}
</style>
