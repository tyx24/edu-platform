<template>
  <div class="course-list-container">
    <!-- 宇宙科技背景 -->
    <div class="cosmic-background">
      <div class="floating-orb orb-1"></div>
      <div class="floating-orb orb-2"></div>
      <div class="floating-orb orb-3"></div>
      <div class="floating-orb orb-4"></div>
      <div class="floating-orb orb-5"></div>
      <div class="floating-orb orb-6"></div>
      <div class="tech-grid"></div>
      <div class="star-field">
        <div class="star" v-for="n in 50" :key="n" :style="getStarStyle(n)"></div>
      </div>
      <div class="nebula nebula-1"></div>
      <div class="nebula nebula-2"></div>
    </div>

    <!-- 页面头部 -->
    <div class="page-header glass-panel">
      <div class="header-decoration">
        <div class="floating-particles">
          <div class="particle"></div>
          <div class="particle"></div>
          <div class="particle"></div>
        </div>
      </div>
      
      <div class="header-content">
        <div class="header-icon">
          <div class="icon-core">🎓</div>
          <div class="icon-ring"></div>
        </div>
        <h2 class="page-title glowing-text">课程中心</h2>
        <p class="page-subtitle">探索丰富的课程资源，开启您的学习之旅</p>
        <div class="header-stats">
          <div class="stat-item">
            <div class="stat-icon">📚</div>
            <div class="stat-number">{{ courseList.length }}</div>
            <div class="stat-label">当前课程</div>
            <div class="stat-progress">
              <div class="progress-bar" :style="{ width: Math.min(courseList.length / pagination.total * 100, 100) + '%' }"></div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon">🌟</div>
            <div class="stat-number">{{ pagination.total }}</div>
            <div class="stat-label">总课程</div>
            <div class="stat-progress">
              <div class="progress-bar full-progress"></div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon">⚡</div>
            <div class="stat-number">{{ getOnlineCourses() }}</div>
            <div class="stat-label">在线课程</div>
            <div class="stat-progress">
              <div class="progress-bar online-progress" :style="{ width: getOnlineCourses() / pagination.total * 100 + '%' }"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <div class="search-card glass-panel">
        <!-- 搜索表单 -->
        <div class="search-form-container">
          <el-row :gutter="24" class="search-form">
            <el-col :span="24">
              <div class="input-group keyword-input">
                <label class="input-label">
                  <span class="label-icon">🔍</span>
                  <span class="label-text">关键词搜索</span>
                  <div class="label-glow"></div>
                </label>
                <div class="input-wrapper">
                  <el-input 
                    v-model="searchForm.keyword" 
                    placeholder="输入课程名称、描述或关键词..." 
                    clearable 
                    @input="handleSearch"
                    size="large" 
                    class="search-input tech-input"
                  >
                    <template #prefix>
                      <div class="input-icon">
                        <i class="el-icon-search"></i>
                        <div class="icon-pulse"></div>
                      </div>
                    </template>
                    <template #suffix>
                      <div class="input-ai-indicator">
                        <span class="ai-text">🔍</span>
                        <div class="ai-glow"></div>
                      </div>
                    </template>
                  </el-input>
                  <div class="input-highlight"></div>
                </div>
              </div>
            </el-col>
          </el-row>
          
          <!-- 筛选器区域 -->
          <div class="filters-section">
            <div class="filters-header">
              <div class="filters-title">
                <span class="title-icon">🎛️</span>
                <span class="title-text">智能筛选器</span>
                <div class="title-decoration"></div>
              </div>
              <div class="filters-subtitle">为您找到最合适的课程</div>
            </div>
            
            <el-row :gutter="20" class="filters-row">
              <el-col :span="6">
                <div class="filter-group category-filter">
                  <label class="filter-label">
                    <span class="label-icon">📚</span>
                    <span class="label-text">课程分类</span>
                    <div class="label-indicator"></div>
                  </label>
                  <div class="select-wrapper premium-select">
                    <el-select 
                      v-model="searchForm.category" 
                      placeholder="选择分类" 
                      clearable 
                      @change="handleSearch" 
                      size="large"
                      class="filter-select tech-select"
                    >
                      <el-option label="🌐 全部分类" value="" />
                      <el-option label="💻 前端开发" value="前端开发" />
                      <el-option label="⚙️ 后端开发" value="后端开发" />
                      <el-option label="📱 移动开发" value="移动开发" />
                      <el-option label="📊 大数据" value="大数据" />
                      <el-option label="🤖 机器学习" value="机器学习" />
                    </el-select>
                    <div class="select-glow"></div>
                  </div>
                </div>
              </el-col>
              
              <el-col :span="6">
                <div class="filter-group difficulty-filter">
                  <label class="filter-label">
                    <span class="label-icon">🏆</span>
                    <span class="label-text">难度级别</span>
                    <div class="label-indicator"></div>
                  </label>
                  <div class="select-wrapper premium-select">
                    <el-select 
                      v-model="searchForm.difficulty" 
                      placeholder="选择难度" 
                      clearable 
                      @change="handleSearch" 
                      size="large"
                      class="filter-select tech-select"
                    >
                      <el-option label="🌐 全部难度" value="" />
                      <el-option label="🟢 初级" value="初级" />
                      <el-option label="🟡 中级" value="中级" />
                      <el-option label="🔴 高级" value="高级" />
                    </el-select>
                    <div class="select-glow"></div>
                  </div>
                </div>
              </el-col>
              
              <el-col :span="6">
                <div class="filter-group state-filter">
                  <label class="filter-label">
                    <span class="label-icon">📋</span>
                    <span class="label-text">审核状态</span>
                    <div class="label-indicator"></div>
                  </label>
                  <div class="select-wrapper premium-select">
                    <el-select 
                      v-model="searchForm.state" 
                      placeholder="选择状态" 
                      clearable 
                      @change="handleSearch" 
                      size="large"
                      class="filter-select tech-select"
                    >
                      <el-option label="🌐 全部状态" value="" />
                      <el-option label="✅ 已审核" value="1" />
                      <el-option label="⏳ 审核中" value="0" />
                      <el-option label="❌ 已拒绝" value="2" />
                    </el-select>
                    <div class="select-glow"></div>
                  </div>
                </div>
              </el-col>
              
              <el-col :span="6">
                <div class="button-group">
                  <label class="filter-label">
                    <span class="label-icon">⚡</span>
                    <span class="label-text">操作</span>
                    <div class="label-indicator"></div>
                  </label>
                  <div class="button-wrapper">
                    <el-button 
                      type="primary" 
                      @click="handleSearch" 
                      size="large" 
                      class="search-btn tech-button premium-button"
                    >
                      <div class="btn-content">
                        <div class="btn-icon">
                          <i class="el-icon-search"></i>
                          <div class="icon-spin"></div>
                        </div>
                        <span class="btn-text">搜索</span>
                        <div class="btn-arrow">➤</div>
                      </div>
                      <div class="btn-glow"></div>
                      <div class="btn-particles"></div>
                      <div class="btn-wave"></div>
                    </el-button>
                    
                    <el-button 
                      @click="resetSearch" 
                      size="large" 
                      class="reset-btn tech-button-outline"
                    >
                      <div class="btn-content">
                        <div class="btn-icon">
                          <i class="el-icon-refresh"></i>
                        </div>
                        <span class="btn-text">重置</span>
                      </div>
                      <div class="btn-border-glow"></div>
                    </el-button>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </div>
        
        <!-- 搜索结果统计 -->
        <div class="search-results-info" v-if="courseList.length > 0 || searchForm.keyword || searchForm.category || searchForm.difficulty || searchForm.state">
          <div class="results-container">
            <div class="results-icon">🎯</div>
            <div class="results-text">
              <span class="results-count">共找到 <strong>{{ pagination.total }}</strong> 门课程</span>
            </div>
            <div class="results-tags" v-if="getActiveFilters().length > 0">
              <span class="filter-tag" v-for="filter in getActiveFilters()" :key="filter">
                {{ filter }}
                <i class="el-icon-close"></i>
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 课程网格 -->
    <div class="course-grid" v-loading="loading">
      <el-row :gutter="24">
        <el-col 
          v-for="(course, index) in courseList" 
          :key="course.id" 
          :xs="24" 
          :sm="12" 
          :md="8" 
          :lg="6"
          class="course-col"
          :style="{ '--delay': index * 0.1 + 's' }"
        >
          <div class="course-card-wrapper">
            <div class="course-card glass-element" @click="viewCourse(course.id)">
              <div class="course-image">
                <img :src="course.coverUrl || '/default-course.jpg'" :alt="course.title">
                <div class="image-overlay">
                  <div class="overlay-content">
                    <div class="play-icon">
                      <div class="play-triangle"></div>
                      <div class="play-ring"></div>
                    </div>
                    <span class="overlay-text">立即学习</span>
                    <div class="overlay-stats">
                      <span class="overlay-duration">{{ getDuration(course) }}</span>
                      <span class="overlay-difficulty">{{ getDifficulty(course) }}</span>
                    </div>
                  </div>
                </div>
                <div class="image-shimmer"></div>
                
                <div class="course-status-badge">
                  <el-tag v-if="course.state === 1" type="success" size="small" effect="dark" class="status-tag">
                    ✅ 已审核
                  </el-tag>
                  <el-tag v-else-if="course.state === 0" type="warning" size="small" effect="dark" class="status-tag">
                    ⏳ 审核中
                  </el-tag>
                  <el-tag v-else type="danger" size="small" effect="dark" class="status-tag">
                    ❌ 已拒绝
                  </el-tag>
                </div>
                
                <div class="course-category">
                  <span class="category-badge">{{ course.category || '其他' }}</span>
                </div>
              </div>

              <div class="course-content">
                <div class="course-header">
                  <h3 class="course-title">{{ course.title }}</h3>
                  <div class="title-glow"></div>
                </div>
                
                <div class="course-rating-info">
                  <div class="course-rating">
                    <div class="stars">
                      <el-rate 
                        :model-value="course.avgRating / 2" 
                        disabled 
                        text-color="#ffa500" 
                        size="small"
                        class="rating-stars"
                      />
                    </div>
                    <span class="rating-text">{{ course.avgRating ? course.avgRating.toFixed(1) : '0.0' }}</span>
                  </div>
                </div>

                <p class="course-description">{{ course.description }}</p>

                <div class="course-meta">
                  <div class="meta-item teacher">
                    <div class="meta-icon">
                      <i class="el-icon-user"></i>
                    </div>
                    <div class="meta-content">
                      <span class="meta-label">👨‍🏫 讲师：</span>
                      <span class="meta-value">{{ course.teacherName || '未知教师' }}</span>
                    </div>
                  </div>
                  
                  <div class="meta-item difficulty">
                    <div class="meta-icon difficulty-icon">
                      <i class="el-icon-medal"></i>
                    </div>
                    <div class="meta-content">
                      <span class="meta-label">    难度：   </span>
                      <span class="meta-value difficulty-badge" :class="`difficulty-${course.difficulty}`">
                        {{ course.difficulty || '初级' }}
                      </span>
                    </div>
                  </div>
                </div>

                <div class="course-footer">
                  <div class="course-price">
                    <span v-if="course.price > 0" class="price">
                      <div class="price-icon">💎</div>
                      <div class="price-content">
                        <span class="currency">¥</span>
                        <span class="amount">{{ course.price }}</span>
                        <div class="price-glow"></div>
                      </div>
                    </span>
                    <span v-else class="free">
                      <div class="free-icon">🎁</div>
                      <div class="free-content">
                        <span>免费学习</span>
                        <div class="free-sparkle"></div>
                      </div>
                    </span>
                  </div>
                  
                  <div class="course-action">
                    <el-button type="primary" size="small" class="view-btn tech-button">
                      <div class="btn-content">
                        <span class="btn-text">立即学习</span>
                        <div class="btn-icon">🚀</div>
                      </div>
                      <div class="btn-particles"></div>
                      <div class="btn-ripple"></div>
                    </el-button>
                  </div>
                </div>
              </div>
              
              <div class="card-glow"></div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 分页 -->
    <div class="pagination-section">
      <div class="pagination-wrapper glass-element">
        <el-pagination 
          v-model:current-page="pagination.pageNum" 
          v-model:page-size="pagination.pageSize"
          :total="pagination.total" 
          :page-sizes="[12, 24, 36, 48]" 
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange" 
          @current-change="handleCurrentChange"
          class="tech-pagination"
        />
      </div>
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
  difficulty: '',
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

// 重置搜索条件
const resetSearch = async () => {
  searchForm.keyword = '';
  searchForm.category = '';
  searchForm.difficulty = '';
  searchForm.state = '';
  pagination.pageNum = 1;
  await fetchCourseList();
};

// 获取当前活跃的筛选器
const getActiveFilters = () => {
  const filters = [];
  if (searchForm.keyword) filters.push(`关键词: ${searchForm.keyword}`);
  if (searchForm.category) filters.push(`分类: ${searchForm.category}`);
  if (searchForm.difficulty) filters.push(`难度: ${searchForm.difficulty}`);
  if (searchForm.state) {
    const stateMap = { '1': '已审核', '0': '审核中', '2': '已拒绝' };
    filters.push(`状态: ${stateMap[searchForm.state]}`);
  }
  return filters;
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

const viewCourse = (courseId) => {
  router.push(`/course/${courseId}`);
};

// 美化功能相关方法
const getStarStyle = (index) => {
  return {
    left: Math.random() * 100 + '%',
    top: Math.random() * 100 + '%',
    animationDelay: Math.random() * 3 + 's',
    animationDuration: (Math.random() * 2 + 1) + 's'
  };
};

const getOnlineCourses = () => {
  return courseList.value.filter(course => course.state === 1).length;
};

const getDuration = (course) => {
  // 根据课程难度和类别提供更合理的时长估算
  const difficultyDurationMap = {
    '初级': ['🕒 30分钟', '🕑 1小时', '🕓 1.5小时'],
    '中级': ['🕔 2小时', '🕕 2.5小时', '🕖 3小时'],
    '高级': ['🕗 3.5小时', '🕘 4小时', '🕙 5小时']
  };
  
  const durations = difficultyDurationMap[course.difficulty] || difficultyDurationMap['初级'];
  // 使用课程 ID 作为随机种子，确保相同课程显示相同时长
  const index = course.id % durations.length;
  return durations[index];
};

const getDifficulty = (course) => {
  const difficultyMap = {
    '初级': '🟢 初级',
    '中级': '🟡 中级', 
    '高级': '🔴 高级'
  };
  return difficultyMap[course.difficulty] || '🟢 初级';
};

const fetchCourseList = async () => {
  try {
    loading.value = true;
    courseList.value = [];

    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      category: searchForm.category || undefined,
      state: searchForm.state || undefined
    };
    const difficulty = searchForm.difficulty;

    Object.keys(params).forEach(key => {
      if (params[key] === undefined || params[key] === '') {
        delete params[key];
      }
    });
    
    const response = await courseApi.getCourseList(params, difficulty);
    const courses = response.records || [];

    const coursesWithDetails = await Promise.all(courses.map(async (course) => {
      let teacherName = '未知教师';
      if (course.teacherId) {
        try {
          const teacherResponse = await userApi.getUserDetail(course.teacherId);
          teacherName = teacherResponse.username || '未知教师';
        } catch (error) {
          console.warn(`获取教师 ${course.teacherId} 信息失败:`, error);
        }
      }

      let avgRating = 0;
      try {
        const stats = await commentApi.getCommentStats(course.id);
        avgRating = stats.avgRating || 0;
      } catch (error) {
        console.warn(`获取课程 ${course.id} 评分失败:`, error);
      }

      return {
        ...course,
        teacherName,
        avgRating
      };
    }));

    courseList.value = coursesWithDetails;
    pagination.total = response.total || 0;
  } catch (error) {
    console.error('获取课程列表失败:', error);
    ElMessage.error('获取课程列表失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchCourseList();
});
</script>

<style scoped>
/* ===== 全局容器样式 ===== */
.course-list-container {
  min-height: 100vh;
  background: transparent;
  position: relative;
  overflow: hidden;
  padding: 0;
}

/* ===== 宇宙科技背景 ===== */
.cosmic-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    linear-gradient(135deg, 
      rgba(15, 15, 35, 0.95) 0%, 
      rgba(26, 26, 46, 0.9) 25%,
      rgba(22, 33, 62, 0.95) 75%, 
      rgba(15, 52, 96, 0.9) 100%),
    radial-gradient(ellipse at top, rgba(56, 189, 248, 0.2) 0%, transparent 50%),
    radial-gradient(ellipse at bottom, rgba(139, 92, 246, 0.2) 0%, transparent 50%);
  z-index: -1;
  animation: cosmicShift 30s ease-in-out infinite;
}

@keyframes cosmicShift {
  0%, 100% {
    transform: scale(1) rotate(0deg);
    filter: brightness(1);
  }
  50% {
    transform: scale(1.05) rotate(1deg);
    filter: brightness(1.1);
  }
}

.floating-orb {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(45deg, 
    rgba(56, 189, 248, 0.8), 
    rgba(139, 92, 246, 0.6));
  filter: blur(40px);
  animation: float 6s ease-in-out infinite;
}

.orb-1 {
  width: 300px;
  height: 300px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.orb-2 {
  width: 200px;
  height: 200px;
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.orb-3 {
  width: 150px;
  height: 150px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

.orb-4 {
  width: 250px;
  height: 250px;
  top: 30%;
  right: 40%;
  animation-delay: 1s;
}

.orb-5 {
  width: 180px;
  height: 180px;
  bottom: 10%;
  right: 30%;
  animation-delay: 3s;
}

.orb-6 {
  width: 120px;
  height: 120px;
  top: 70%;
  left: 60%;
  animation-delay: 5s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
    opacity: 0.7;
  }
  50% {
    transform: translate(30px, -30px) scale(1.1);
    opacity: 0.9;
  }
}

.tech-grid {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(56, 189, 248, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(56, 189, 248, 0.1) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: gridMove 20s linear infinite;
  opacity: 0.3;
}

@keyframes gridMove {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(50px, 50px);
  }
}

.star-field {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.star {
  position: absolute;
  width: 2px;
  height: 2px;
  background: #ffffff;
  border-radius: 50%;
  animation: twinkle 2s ease-in-out infinite alternate;
  box-shadow: 0 0 6px rgba(255, 255, 255, 0.8);
}

@keyframes twinkle {
  from {
    opacity: 0.3;
    transform: scale(0.5);
  }
  to {
    opacity: 1;
    transform: scale(1.5);
  }
}

.nebula {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.3;
  animation: nebulaFloat 15s ease-in-out infinite;
}

.nebula-1 {
  width: 400px;
  height: 400px;
  top: 20%;
  left: 20%;
  background: radial-gradient(circle, rgba(168, 85, 247, 0.4), rgba(59, 130, 246, 0.2));
  animation-delay: 0s;
}

.nebula-2 {
  width: 300px;
  height: 300px;
  bottom: 30%;
  right: 25%;
  background: radial-gradient(circle, rgba(236, 72, 153, 0.4), rgba(139, 92, 246, 0.2));
  animation-delay: 7s;
}

@keyframes nebulaFloat {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(20px, -30px) scale(1.1);
  }
  66% {
    transform: translate(-15px, 20px) scale(0.9);
  }
}

/* ===== 玻璃拟态基础样式 ===== */
.glass-panel {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(25px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 30px;
  box-shadow: 
    0 20px 50px rgba(0, 0, 0, 0.3),
    0 8px 32px rgba(56, 189, 248, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.glass-element {
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 20px;
  box-shadow: 
    0 12px 30px rgba(0, 0, 0, 0.2),
    0 4px 20px rgba(56, 189, 248, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.08);
}

/* ===== 页面头部科技化 ===== */
.page-header {
  position: relative;
  padding: 80px 40px;
  margin-bottom: 40px;
  overflow: hidden;
  text-align: center;
  animation: headerSlideIn 1s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes headerSlideIn {
  from {
    opacity: 0;
    transform: translateY(-50px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.header-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.floating-particles {
  position: absolute;
  width: 100%;
  height: 100%;
}

.particle {
  position: absolute;
  width: 8px;
  height: 8px;
  background: linear-gradient(45deg, #38bdf8, #8b5cf6);
  border-radius: 50%;
  animation: particleFloat 4s ease-in-out infinite;
  box-shadow: 0 0 15px rgba(56, 189, 248, 0.6);
}

.particle:nth-child(1) {
  top: 20%;
  left: 15%;
  animation-delay: 0s;
}

.particle:nth-child(2) {
  top: 60%;
  right: 20%;
  animation-delay: 2s;
}

.particle:nth-child(3) {
  bottom: 30%;
  left: 70%;
  animation-delay: 1s;
}

@keyframes particleFloat {
  0%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.7;
  }
  50% {
    transform: translateY(-20px) scale(1.2);
    opacity: 1;
  }
}

.header-content {
  position: relative;
  z-index: 10;
}

.header-icon {
  position: relative;
  display: inline-block;
  margin-bottom: 24px;
}

.icon-core {
  font-size: 64px;
  filter: drop-shadow(0 8px 16px rgba(56, 189, 248, 0.5));
  animation: iconPulse 3s ease-in-out infinite;
}

@keyframes iconPulse {
  0%, 100% {
    transform: scale(1);
    filter: drop-shadow(0 8px 16px rgba(56, 189, 248, 0.5));
  }
  50% {
    transform: scale(1.1);
    filter: drop-shadow(0 12px 24px rgba(56, 189, 248, 0.8));
  }
}

.icon-ring {
  position: absolute;
  top: -20px;
  left: -20px;
  right: -20px;
  bottom: -20px;
  border: 2px solid rgba(56, 189, 248, 0.4);
  border-radius: 50%;
  animation: ringRotate 8s linear infinite;
}

@keyframes ringRotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.page-title {
  font-size: 3.5rem;
  font-weight: 800;
  margin: 0 0 20px 0;
  letter-spacing: -1px;
  color: rgba(255, 255, 255, 0.95);
  text-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.3),
    0 0 30px rgba(56, 189, 248, 0.4);
  position: relative;
}

.glowing-text {
  background: linear-gradient(135deg, #ffffff, #38bdf8, #8b5cf6);
  background-size: 200% 200%;
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: textGlow 4s ease-in-out infinite;
}

@keyframes textGlow {
  0%, 100% {
    background-position: 0% 50%;
    filter: drop-shadow(0 0 20px rgba(56, 189, 248, 0.5));
  }
  50% {
    background-position: 100% 50%;
    filter: drop-shadow(0 0 30px rgba(139, 92, 246, 0.6));
  }
}

.page-subtitle {
  font-size: 1.3rem;
  color: rgba(255, 255, 255, 0.8);
  margin: 0 0 30px 0;
  line-height: 1.6;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.header-stats {
  display: flex;
  justify-content: center;
  gap: 60px;
  margin-top: 30px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  min-width: 120px;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.stat-item:hover {
  transform: translateY(-5px) scale(1.05);
  background: rgba(255, 255, 255, 0.15);
  box-shadow: 0 12px 30px rgba(56, 189, 248, 0.3);
}

.stat-number {
  font-size: 2.2rem;
  font-weight: 800;
  color: #38bdf8;
  margin-bottom: 8px;
  text-shadow: 0 0 15px rgba(56, 189, 248, 0.6);
}

.stat-icon {
  font-size: 2rem;
  margin-bottom: 8px;
  filter: drop-shadow(0 4px 8px rgba(56, 189, 248, 0.5));
  animation: iconBounce 3s ease-in-out infinite;
}

@keyframes iconBounce {
  0%, 100% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-5px) scale(1.05);
  }
}

.stat-progress {
  width: 100%;
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  margin-top: 8px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  border-radius: 2px;
  transition: width 1.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
  overflow: hidden;
  background: linear-gradient(90deg, #38bdf8, #8b5cf6);
}

.full-progress {
  width: 100% !important;
  background: linear-gradient(90deg, #10b981, #059669);
}

.online-progress {
  background: linear-gradient(90deg, #f59e0b, #d97706);
}

.progress-bar::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  animation: progressShine 2s infinite;
}

@keyframes progressShine {
  0% {
    left: -100%;
  }
  100% {
    left: 100%;
  }
}

.stat-label {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 600;
}

/* ===== 智能搜索区域科技化 ===== */
.search-section {
  padding: 0 40px;
  margin-bottom: 40px;
}

.search-card {
  padding: 40px;
  position: relative;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  border-radius: 32px;
}

.search-card:hover {
  transform: translateY(-8px);
  box-shadow: 
    0 30px 80px rgba(0, 0, 0, 0.35),
    0 15px 50px rgba(56, 189, 248, 0.3);
}

.search-header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 2px solid rgba(255, 255, 255, 0.1);
  position: relative;
}

.search-header::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #38bdf8, #8b5cf6);
  transition: width 1.5s ease;
}

.search-card:hover .search-header::after {
  width: 100%;
}

.search-icon-container {
  position: relative;
}

.search-icon {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, 
    rgba(56, 189, 248, 0.2), 
    rgba(139, 92, 246, 0.2));
  border-radius: 50%;
  backdrop-filter: blur(20px);
  border: 2px solid rgba(255, 255, 255, 0.2);
}

.search-icon .icon-core {
  font-size: 40px;
  filter: drop-shadow(0 8px 16px rgba(56, 189, 248, 0.5));
  animation: iconFloatSpin 4s ease-in-out infinite;
}

@keyframes iconFloatSpin {
  0%, 100% {
    transform: scale(1) rotate(0deg);
  }
  50% {
    transform: scale(1.1) rotate(5deg);
  }
}

.search-orbit {
  position: absolute;
  border: 1px solid rgba(56, 189, 248, 0.3);
  border-radius: 50%;
  animation: orbitRotate 8s linear infinite;
}

.orbit-1 {
  top: -15px;
  left: -15px;
  right: -15px;
  bottom: -15px;
  animation-duration: 8s;
}

.orbit-2 {
  top: -25px;
  left: -25px;
  right: -25px;
  bottom: -25px;
  animation-duration: 12s;
  animation-direction: reverse;
}

@keyframes orbitRotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.search-pulse {
  position: absolute;
  top: -10px;
  left: -10px;
  right: -10px;
  bottom: -10px;
  border: 2px solid rgba(56, 189, 248, 0.4);
  border-radius: 50%;
  animation: advancedPulse 3s ease-in-out infinite;
}

@keyframes advancedPulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.7;
    border-color: rgba(56, 189, 248, 0.4);
  }
  50% {
    transform: scale(1.2);
    opacity: 1;
    border-color: rgba(139, 92, 246, 0.6);
  }
}

.search-title-section {
  flex: 1;
}

.search-title {
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.title-main {
  font-size: 2rem;
  font-weight: 800;
  background: linear-gradient(135deg, #ffffff, #38bdf8, #8b5cf6);
  background-size: 200% 200%;
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: titleGlow 3s ease-in-out infinite;
}

@keyframes titleGlow {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.title-subtitle {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 500;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.search-stats {
  display: flex;
  gap: 20px;
  margin-top: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
}

.stat-item:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 8px 20px rgba(56, 189, 248, 0.2);
}

.stat-icon {
  font-size: 1rem;
}

.search-form-container {
  margin-bottom: 30px;
}

.keyword-input {
  margin-bottom: 30px;
}

.keyword-input .input-wrapper {
  position: relative;
  transform: scale(1.02);
}

.keyword-input .tech-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(56, 189, 248, 0.3);
  border-radius: 25px;
  padding: 20px 28px;
  font-size: 1.1rem;
  box-shadow: 
    inset 0 2px 0 rgba(255, 255, 255, 0.15),
    0 8px 32px rgba(56, 189, 248, 0.2);
}

/* ===== 筛选器区域 ===== */
.filters-section {
  background: linear-gradient(135deg, 
    rgba(56, 189, 248, 0.05), 
    rgba(139, 92, 246, 0.05));
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  padding: 30px;
  margin-top: 20px;
  backdrop-filter: blur(20px);
  position: relative;
  overflow: hidden;
}

.filters-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(56, 189, 248, 0.1), 
    transparent);
  animation: sectionShimmer 4s ease-in-out infinite;
}

@keyframes sectionShimmer {
  0% {
    left: -100%;
  }
  50% {
    left: 100%;
  }
  100% {
    left: -100%;
  }
}

.filters-header {
  text-align: center;
  margin-bottom: 25px;
  position: relative;
  z-index: 2;
}

.filters-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 8px;
  position: relative;
}

.title-icon {
  font-size: 1.8rem;
  animation: titleIconFloat 3s ease-in-out infinite;
}

@keyframes titleIconFloat {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-4px) rotate(5deg);
  }
}

.title-text {
  font-size: 1.4rem;
  font-weight: 700;
  background: linear-gradient(135deg, #38bdf8, #8b5cf6, #ec4899);
  background-size: 200% 200%;
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: titleTextGlow 3s ease-in-out infinite;
}

@keyframes titleTextGlow {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.title-decoration {
  position: absolute;
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #38bdf8, #8b5cf6, #ec4899);
  border-radius: 1px;
  animation: decorationGrow 2s ease-in-out infinite;
}

@keyframes decorationGrow {
  0%, 100% {
    width: 0;
  }
  50% {
    width: 80px;
  }
}

.filters-subtitle {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.7);
  font-style: italic;
  text-align: center;
}

.filters-row {
  position: relative;
  z-index: 2;
}

.filter-group {
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.filter-label {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.95rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  position: relative;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}

.filter-label:hover {
  background: rgba(255, 255, 255, 0.08);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(56, 189, 248, 0.2);
}

.filter-label .label-icon {
  font-size: 1.2rem;
  filter: drop-shadow(0 2px 4px rgba(56, 189, 248, 0.4));
}

.label-indicator {
  position: absolute;
  top: 0;
  right: 0;
  width: 8px;
  height: 8px;
  background: linear-gradient(135deg, #38bdf8, #8b5cf6);
  border-radius: 50%;
  animation: indicatorPulse 2s ease-in-out infinite;
}

@keyframes indicatorPulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.7;
  }
  50% {
    transform: scale(1.3);
    opacity: 1;
  }
}

/* ===== 专门的筛选器类型样式 ===== */
.category-filter .filter-label {
  border-color: rgba(34, 197, 94, 0.3);
}

.category-filter .filter-label:hover {
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.3);
}

.category-filter .label-indicator {
  background: linear-gradient(135deg, #22c55e, #16a34a);
}

.difficulty-filter .filter-label {
  border-color: rgba(245, 158, 11, 0.3);
}

.difficulty-filter .filter-label:hover {
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
}

.difficulty-filter .label-indicator {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.state-filter .filter-label {
  border-color: rgba(139, 92, 246, 0.3);
}

.state-filter .filter-label:hover {
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);
}

.state-filter .label-indicator {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
}

.premium-select {
  position: relative;
  flex: 1;
}

.select-glow {
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, 
    rgba(56, 189, 248, 0.5), 
    rgba(139, 92, 246, 0.5), 
    rgba(236, 72, 153, 0.5));
  background-size: 300% 300%;
  border-radius: 22px;
  opacity: 0;
  transition: opacity 0.4s ease;
  animation: selectGlowMove 3s ease-in-out infinite;
  z-index: -1;
}

@keyframes selectGlowMove {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.premium-select:hover .select-glow {
  opacity: 1;
}

.filter-select :deep(.el-select__wrapper) {
  background: rgba(255, 255, 255, 0.08);
  border: 2px solid rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  padding: 16px 20px;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  backdrop-filter: blur(15px);
  box-shadow: 
    inset 0 1px 0 rgba(255, 255, 255, 0.1),
    0 4px 20px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

.filter-select :deep(.el-select__wrapper)::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(255, 255, 255, 0.1), 
    transparent);
  transition: left 0.6s;
}

.filter-select :deep(.el-select__wrapper):hover::before {
  left: 100%;
}

.filter-select :deep(.el-select__wrapper):hover {
  border-color: rgba(56, 189, 248, 0.4);
  background: rgba(255, 255, 255, 0.12);
  transform: translateY(-2px) scale(1.02);
  box-shadow: 
    0 12px 30px rgba(56, 189, 248, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

.filter-select :deep(.el-select__wrapper.is-focused) {
  border-color: rgba(56, 189, 248, 0.8);
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-3px) scale(1.03);
  box-shadow: 
    0 0 0 4px rgba(56, 189, 248, 0.2),
    0 15px 35px rgba(56, 189, 248, 0.3);
}

.filter-select :deep(.el-select__selected-item) {
  color: rgba(255, 255, 255, 0.95);
  font-weight: 600;
  font-size: 1rem;
}

.filter-select :deep(.el-select__placeholder) {
  color: rgba(255, 255, 255, 0.5);
  font-style: italic;
}

.filter-select :deep(.el-select__caret) {
  color: rgba(56, 189, 248, 0.7);
  transition: all 0.3s ease;
  font-size: 16px;
}

.filter-select :deep(.el-select__wrapper.is-focused .el-select__caret) {
  color: rgba(56, 189, 248, 1);
  transform: rotate(180deg) scale(1.2);
}

/* ===== 特定筛选器的选择框样式 ===== */
.category-filter .filter-select :deep(.el-select__wrapper):hover {
  border-color: rgba(34, 197, 94, 0.4);
  box-shadow: 
    0 12px 30px rgba(34, 197, 94, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

.category-filter .filter-select :deep(.el-select__wrapper.is-focused) {
  border-color: rgba(34, 197, 94, 0.8);
  box-shadow: 
    0 0 0 4px rgba(34, 197, 94, 0.2),
    0 15px 35px rgba(34, 197, 94, 0.3);
}

.difficulty-filter .filter-select :deep(.el-select__wrapper):hover {
  border-color: rgba(245, 158, 11, 0.4);
  box-shadow: 
    0 12px 30px rgba(245, 158, 11, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

.difficulty-filter .filter-select :deep(.el-select__wrapper.is-focused) {
  border-color: rgba(245, 158, 11, 0.8);
  box-shadow: 
    0 0 0 4px rgba(245, 158, 11, 0.2),
    0 15px 35px rgba(245, 158, 11, 0.3);
}

.state-filter .filter-select :deep(.el-select__wrapper):hover {
  border-color: rgba(139, 92, 246, 0.4);
  box-shadow: 
    0 12px 30px rgba(139, 92, 246, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

.state-filter .filter-select :deep(.el-select__wrapper.is-focused) {
  border-color: rgba(139, 92, 246, 0.8);
  box-shadow: 
    0 0 0 4px rgba(139, 92, 246, 0.2),
    0 15px 35px rgba(139, 92, 246, 0.3);
}

.search-form {
  gap: 24px;
}

.input-group,
.button-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.input-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
}

.input-label:hover {
  color: rgba(255, 255, 255, 0.95);
  transform: translateX(4px);
}

.label-icon {
  font-size: 1.1rem;
  filter: drop-shadow(0 2px 4px rgba(56, 189, 248, 0.3));
}

.label-glow {
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #38bdf8, #8b5cf6);
  border-radius: 1px;
  transition: width 0.4s ease;
}

.input-label:hover .label-glow {
  width: 100%;
}

.input-wrapper,
.select-wrapper {
  position: relative;
}

.input-highlight,
.select-highlight {
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #38bdf8, #8b5cf6);
  border-radius: 1px;
  transition: width 0.4s ease;
}

.input-wrapper:focus-within .input-highlight,
.select-wrapper:focus-within .select-highlight {
  width: 100%;
}

.tech-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.08);
  border: 2px solid rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  padding: 18px 24px;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  backdrop-filter: blur(15px);
  box-shadow: 
    inset 0 1px 0 rgba(255, 255, 255, 0.1),
    0 4px 20px rgba(0, 0, 0, 0.1);
}

.tech-input :deep(.el-input__wrapper):hover {
  border-color: rgba(56, 189, 248, 0.4);
  background: rgba(255, 255, 255, 0.12);
  transform: translateY(-2px);
  box-shadow: 
    0 12px 30px rgba(56, 189, 248, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

.tech-input :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(56, 189, 248, 0.8);
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-3px) scale(1.02);
  box-shadow: 
    0 0 0 4px rgba(56, 189, 248, 0.2),
    0 15px 35px rgba(56, 189, 248, 0.3);
}

.tech-input :deep(.el-input__inner) {
  color: rgba(255, 255, 255, 0.95);
  font-weight: 500;
  font-size: 1rem;
}

.tech-input :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.5);
  font-style: italic;
}

.input-icon {
  color: rgba(56, 189, 248, 0.7);
  font-size: 18px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-pulse {
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  border: 1px solid rgba(56, 189, 248, 0.3);
  border-radius: 50%;
  animation: iconPulseEffect 2s ease-in-out infinite;
}

@keyframes iconPulseEffect {
  0%, 100% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.8;
  }
}

.input-ai-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 20px;
  background: linear-gradient(135deg, #8b5cf6, #ec4899);
  border-radius: 10px;
  position: relative;
  overflow: hidden;
}



@keyframes aiGlow {
  0% {
    left: -100%;
  }
  50% {
    left: 100%;
  }
  100% {
    left: -100%;
  }
}

.tech-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 16px 20px;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  backdrop-filter: blur(10px);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.tech-input :deep(.el-input__wrapper):hover {
  border-color: rgba(56, 189, 248, 0.5);
  background: rgba(255, 255, 255, 0.12);
  box-shadow: 
    0 8px 25px rgba(56, 189, 248, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

.tech-input :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(56, 189, 248, 0.8);
  background: rgba(255, 255, 255, 0.15);
  box-shadow: 
    0 0 0 4px rgba(56, 189, 248, 0.2),
    0 12px 30px rgba(56, 189, 248, 0.3);
}

.tech-input :deep(.el-input__inner) {
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

.tech-input :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.5);
}

.input-icon {
  color: rgba(56, 189, 248, 0.7);
  font-size: 16px;
}

.tech-select :deep(.el-select__wrapper) {
  background: rgba(255, 255, 255, 0.08);
  border: 2px solid rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  padding: 18px 24px;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  backdrop-filter: blur(15px);
  box-shadow: 
    inset 0 1px 0 rgba(255, 255, 255, 0.1),
    0 4px 20px rgba(0, 0, 0, 0.1);
}

.tech-select :deep(.el-select__wrapper):hover {
  border-color: rgba(56, 189, 248, 0.4);
  background: rgba(255, 255, 255, 0.12);
  transform: translateY(-2px);
  box-shadow: 
    0 12px 30px rgba(56, 189, 248, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

.tech-select :deep(.el-select__wrapper.is-focused) {
  border-color: rgba(56, 189, 248, 0.8);
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-3px) scale(1.02);
  box-shadow: 
    0 0 0 4px rgba(56, 189, 248, 0.2),
    0 15px 35px rgba(56, 189, 248, 0.3);
}

.tech-select :deep(.el-select__selected-item) {
  color: rgba(255, 255, 255, 0.95);
  font-weight: 500;
  font-size: 1rem;
}

.tech-select :deep(.el-select__placeholder) {
  color: rgba(255, 255, 255, 0.5);
  font-style: italic;
}

.tech-select :deep(.el-select__caret) {
  color: rgba(56, 189, 248, 0.7);
  transition: all 0.3s ease;
}

.tech-select :deep(.el-select__wrapper.is-focused .el-select__caret) {
  color: rgba(56, 189, 248, 1);
  transform: rotate(180deg) scale(1.2);
}

.button-wrapper {
  display: flex;
  gap: 12px;
  align-items: stretch;
}

.premium-button {
  flex: 1;
  padding: 18px 32px;
  border-radius: 20px;
  background: linear-gradient(135deg, #38bdf8, #8b5cf6, #ec4899);
  background-size: 200% 200%;
  border: none;
  color: white;
  font-weight: 700;
  position: relative;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  box-shadow: 
    0 8px 25px rgba(56, 189, 248, 0.4),
    0 4px 15px rgba(139, 92, 246, 0.3);
  animation: gradientShift 4s ease-in-out infinite;
}

@keyframes gradientShift {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.premium-button:hover {
  transform: translateY(-4px) scale(1.05);
  box-shadow: 
    0 20px 50px rgba(56, 189, 248, 0.6),
    0 8px 30px rgba(139, 92, 246, 0.5);
}

.premium-button .btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  position: relative;
  z-index: 3;
}

.premium-button .btn-icon {
  position: relative;
  font-size: 1.2rem;
}

.icon-spin {
  position: absolute;
  top: -6px;
  left: -6px;
  right: -6px;
  bottom: -6px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  animation: iconSpin 3s linear infinite;
}

@keyframes iconSpin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.btn-text {
  font-size: 1rem;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.btn-arrow {
  font-size: 1.2rem;
  transition: transform 0.3s ease;
}

.premium-button:hover .btn-arrow {
  transform: translateX(4px) scale(1.2);
}

.btn-wave {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(255, 255, 255, 0.3), 
    transparent);
  transition: left 0.8s;
  transform: skewX(-20deg);
}

.premium-button:hover .btn-wave {
  left: 100%;
}

.reset-btn {
  padding: 18px 24px;
  border-radius: 20px;
  background: transparent;
  border: 2px solid rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.8);
  font-weight: 600;
  position: relative;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  backdrop-filter: blur(10px);
}

.reset-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(56, 189, 248, 0.5);
  color: rgba(255, 255, 255, 0.95);
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 8px 25px rgba(56, 189, 248, 0.2);
}

.btn-border-glow {
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, #38bdf8, #8b5cf6, #ec4899, #38bdf8);
  background-size: 400% 400%;
  border-radius: 22px;
  opacity: 0;
  transition: opacity 0.4s ease;
  animation: borderGlow 3s ease-in-out infinite;
  z-index: -1;
}

.reset-btn:hover .btn-border-glow {
  opacity: 1;
}

@keyframes borderGlow {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

/* ===== 搜索结果统计区域 ===== */
.search-results-info {
  padding-top: 25px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  animation: slideInUp 0.6s ease-out;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.results-container {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.results-icon {
  font-size: 1.5rem;
  animation: resultsPulse 2s ease-in-out infinite;
}

@keyframes resultsPulse {
  0%, 100% {
    transform: scale(1);
    filter: drop-shadow(0 0 8px rgba(56, 189, 248, 0.5));
  }
  50% {
    transform: scale(1.1);
    filter: drop-shadow(0 0 12px rgba(139, 92, 246, 0.7));
  }
}

.results-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.results-count {
  font-size: 1rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
}

.results-count strong {
  color: #38bdf8;
  font-size: 1.1rem;
  text-shadow: 0 0 8px rgba(56, 189, 248, 0.5);
}

.results-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-left: auto;
}

.filter-tag {
  padding: 6px 12px;
  background: linear-gradient(135deg, 
    rgba(56, 189, 248, 0.2), 
    rgba(139, 92, 246, 0.2));
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  gap: 6px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  cursor: pointer;
}

.filter-tag:hover {
  background: linear-gradient(135deg, 
    rgba(56, 189, 248, 0.3), 
    rgba(139, 92, 246, 0.3));
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 12px rgba(56, 189, 248, 0.3);
}

.filter-tag i {
  font-size: 0.7rem;
  opacity: 0.7;
  transition: opacity 0.3s ease;
}

.filter-tag:hover i {
  opacity: 1;
}

/* ===== 课程网格科技化 ===== */
.course-grid {
  padding: 0 40px;
  margin-bottom: 60px;
}

.course-col {
  animation: cardSlideUp 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  animation-delay: var(--delay);
  animation-fill-mode: both;
}

@keyframes cardSlideUp {
  from {
    opacity: 0;
    transform: translateY(40px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.course-card-wrapper {
  height: 100%;
  margin-bottom: 32px;
  position: relative;
}

.course-card {
  height: 100%;
  border-radius: 24px;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  position: relative;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.course-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 
    0 25px 60px rgba(0, 0, 0, 0.3),
    0 12px 40px rgba(56, 189, 248, 0.25);
}

.card-glow {
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  background: linear-gradient(45deg, #38bdf8, #8b5cf6, #ec4899, #38bdf8);
  border-radius: 28px;
  opacity: 0;
  filter: blur(8px);
  z-index: -1;
  animation: cardGlow 4s linear infinite;
  transition: opacity 0.4s ease;
}

.course-card:hover .card-glow {
  opacity: 0.6;
}

@keyframes cardGlow {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

/* ===== 课程图片科技化 ===== */
.course-image {
  position: relative;
  height: 220px;
  overflow: hidden;
  border-radius: 20px 20px 0 0;
}

.course-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
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
  background: linear-gradient(135deg, 
    rgba(56, 189, 248, 0.8), 
    rgba(139, 92, 246, 0.8));
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.4s ease;
}

.course-card:hover .image-overlay {
  opacity: 1;
}

.overlay-content {
  text-align: center;
  color: white;
}

.play-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-bottom: 12px;
  transition: all 0.3s ease;
  border: 2px solid rgba(255, 255, 255, 0.3);
  position: relative;
}

.play-triangle {
  width: 0;
  height: 0;
  border-left: 16px solid white;
  border-top: 12px solid transparent;
  border-bottom: 12px solid transparent;
  margin-left: 4px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.play-ring {
  position: absolute;
  top: -12px;
  left: -12px;
  right: -12px;
  bottom: -12px;
  border: 2px solid rgba(255, 255, 255, 0.5);
  border-radius: 50%;
  animation: playRingPulse 2s ease-in-out infinite;
}

@keyframes playRingPulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

.overlay-stats {
  display: flex;
  gap: 16px;
  margin-top: 12px;
  font-size: 0.8rem;
}

.overlay-duration,
.overlay-difficulty {
  padding: 4px 8px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  font-weight: 600;
}

.image-shimmer {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transform: skewX(-20deg);
  transition: left 0.8s;
}

.course-card:hover .image-shimmer {
  left: 100%;
}

.play-icon:hover {
  transform: scale(1.1);
  background: rgba(255, 255, 255, 0.3);
}

.overlay-text {
  font-size: 1rem;
  font-weight: 600;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
}

.course-status-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 10;
}

.status-tag {
  border-radius: 12px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
}

.course-category {
  position: absolute;
  bottom: 16px;
  left: 16px;
  z-index: 10;
}

.category-badge {
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 8px;
  font-size: 0.8rem;
  font-weight: 600;
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

/* ===== 课程内容科技化 ===== */
.course-content {
  padding: 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
}

.course-header {
  position: relative;
  margin-bottom: 16px;
}

.course-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.95);
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
}

.title-glow {
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #38bdf8, #8b5cf6);
  border-radius: 1px;
  transition: width 0.4s ease;
}

.course-card:hover .title-glow {
  width: 100%;
}

.course-rating-info {
  margin-bottom: 16px;
}

.course-rating {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rating-stars :deep(.el-rate__item) {
  margin-right: 2px;
}

.rating-text {
  font-size: 0.9rem;
  font-weight: 600;
  color: #ffa500;
  text-shadow: 0 0 8px rgba(255, 165, 0, 0.5);
}

.course-description {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.6;
  margin-bottom: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.course-meta {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.meta-item:hover {
  background: rgba(255, 255, 255, 0.08);
  transform: translateX(5px);
}

.meta-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #38bdf8, #8b5cf6);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  box-shadow: 0 2px 8px rgba(56, 189, 248, 0.3);
  transition: all 0.3s ease;
}

.difficulty-icon {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.3);
}

.meta-item:hover .meta-icon {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 4px 12px rgba(56, 189, 248, 0.5);
}

.meta-item:hover .difficulty-icon {
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.5);
}

.meta-content {
  flex: 1;
}

.meta-label {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 2px;
}

.meta-value {
  font-size: 0.9rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
}

.difficulty-badge {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 0.8rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

.difficulty-初级 {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
  box-shadow: 0 0 12px rgba(16, 185, 129, 0.4);
}

.difficulty-中级 {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: white;
  box-shadow: 0 0 12px rgba(245, 158, 11, 0.4);
}

.difficulty-高级 {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: white;
  box-shadow: 0 0 12px rgba(239, 68, 68, 0.4);
}

.difficulty-badge:hover {
  transform: scale(1.05);
  filter: brightness(1.1);
}

.course-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  margin-top: auto;
}

.course-price {
  display: flex;
  align-items: center;
}

.price {
  color: #ef4444;
  font-weight: 700;
  font-size: 1.1rem;
  text-shadow: 0 0 10px rgba(239, 68, 68, 0.5);
  display: flex;
  align-items: center;
}

.price-icon,
.free-icon {
  font-size: 1.2rem;
  margin-right: 8px;
  animation: iconFloat 3s ease-in-out infinite;
}

@keyframes iconFloat {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-2px) rotate(5deg);
  }
}

.price-content,
.free-content {
  position: relative;
  display: flex;
  align-items: center;
}

.price-glow {
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #ef4444, transparent);
  animation: priceGlow 2s ease-in-out infinite;
}

@keyframes priceGlow {
  0%, 100% {
    opacity: 0.5;
    transform: scaleX(0.8);
  }
  50% {
    opacity: 1;
    transform: scaleX(1.2);
  }
}

.free-sparkle {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 8px;
  height: 8px;
  background: #10b981;
  border-radius: 50%;
  animation: sparkle 1.5s ease-in-out infinite;
  box-shadow: 0 0 6px rgba(16, 185, 129, 0.8);
}

@keyframes sparkle {
  0%, 100% {
    opacity: 0;
    transform: scale(0) rotate(0deg);
  }
  50% {
    opacity: 1;
    transform: scale(1) rotate(180deg);
  }
}

.currency {
  font-size: 0.9rem;
  margin-right: 2px;
}

.amount {
  font-size: 1.2rem;
}

.free {
  color: #10b981;
  font-weight: 700;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 4px;
  text-shadow: 0 0 10px rgba(16, 185, 129, 0.5);
}

.view-btn {
  padding: 8px 20px;
  border-radius: 12px;
  font-size: 0.9rem;
  position: relative;
  overflow: hidden;
}

.btn-content {
  display: flex;
  align-items: center;
  gap: 8px;
  position: relative;
  z-index: 2;
}

.btn-text {
  font-weight: 600;
}

.btn-icon {
  font-size: 1rem;
  animation: rocketLaunch 2s ease-in-out infinite;
}

@keyframes rocketLaunch {
  0%, 100% {
    transform: translateX(0) rotate(0deg);
  }
  50% {
    transform: translateX(2px) rotate(5deg);
  }
}

.btn-ripple {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.tech-button:active .btn-ripple {
  width: 300px;
  height: 300px;
}

.btn-particles {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(255, 255, 255, 0.2), 
    transparent);
  transition: left 0.6s;
}

.view-btn:hover .btn-particles {
  left: 100%;
}

/* ===== 分页区域科技化 ===== */
.pagination-section {
  padding: 0 40px 60px;
  display: flex;
  justify-content: center;
}

.pagination-wrapper {
  padding: 24px 40px;
  border-radius: 20px;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.pagination-wrapper:hover {
  transform: translateY(-3px);
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.2),
    0 8px 25px rgba(56, 189, 248, 0.2);
}

.tech-pagination :deep(.el-pagination) {
  gap: 12px;
}

.tech-pagination :deep(.el-pagination__total),
.tech-pagination :deep(.el-pagination__sizes),
.tech-pagination :deep(.el-pagination__jump) {
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}

.tech-pagination :deep(.el-pager li),
.tech-pagination :deep(.btn-prev),
.tech-pagination :deep(.btn-next) {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  margin: 0 4px;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  backdrop-filter: blur(10px);
}

.tech-pagination :deep(.el-pager li:hover),
.tech-pagination :deep(.btn-prev:hover),
.tech-pagination :deep(.btn-next:hover) {
  background: rgba(56, 189, 248, 0.2);
  border-color: rgba(56, 189, 248, 0.5);
  color: rgba(255, 255, 255, 0.9);
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 8px 20px rgba(56, 189, 248, 0.3);
}

.tech-pagination :deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, #38bdf8, #8b5cf6);
  border-color: transparent;
  color: white;
  font-weight: 700;
  box-shadow: 0 8px 25px rgba(56, 189, 248, 0.4);
}

.tech-pagination :deep(.el-select .el-select__wrapper) {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.tech-pagination :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.tech-pagination :deep(.el-input__inner) {
  color: rgba(255, 255, 255, 0.8);
}

/* ===== 加载状态科技化 ===== */
.course-grid :deep(.el-loading-mask) {
  background: rgba(15, 15, 35, 0.8);
  backdrop-filter: blur(20px);
}

.course-grid :deep(.el-loading-spinner) {
  color: #38bdf8;
}

.course-grid :deep(.el-loading-text) {
  color: rgba(255, 255, 255, 0.8);
  font-weight: 600;
}

/* ===== 响应式设计 ===== */
@media (max-width: 1400px) {
  .course-grid,
  .search-section,
  .pagination-section {
    padding-left: 32px;
    padding-right: 32px;
  }
  
  .page-header {
    padding: 70px 32px;
  }
  
  .page-title {
    font-size: 3rem;
  }
  
  .header-stats {
    gap: 40px;
  }
}

@media (max-width: 1200px) {
  .course-grid,
  .search-section,
  .pagination-section {
    padding-left: 28px;
    padding-right: 28px;
  }
  
  .page-header {
    padding: 60px 28px;
  }
  
  .page-title {
    font-size: 2.8rem;
  }
  
  .search-card {
    padding: 32px;
  }
}

@media (max-width: 768px) {
  .course-grid,
  .search-section,
  .pagination-section {
    padding-left: 20px;
    padding-right: 20px;
  }
  
  .page-header {
    padding: 50px 20px;
  }
  
  .page-title {
    font-size: 2.2rem;
  }
  
  .page-subtitle {
    font-size: 1.1rem;
  }
  
  .header-stats {
    flex-direction: column;
    gap: 20px;
    margin-top: 20px;
  }
  
  .stat-item {
    min-width: auto;
    width: 200px;
    margin: 0 auto;
  }
  
  .search-card {
    padding: 24px;
  }
  
  .search-form {
    gap: 16px;
  }
  
  .search-form .el-col {
    width: 100% !important;
    max-width: none !important;
    flex: none;
  }
  
  .course-image {
    height: 180px;
  }
  
  .course-content {
    padding: 20px;
  }
  
  .pagination-wrapper {
    padding: 20px 24px;
  }
}

@media (max-width: 480px) {
  .page-header {
    padding: 40px 16px;
  }
  
  .page-title {
    font-size: 1.8rem;
  }
  
  .page-subtitle {
    font-size: 1rem;
  }
  
  .header-icon .icon-core {
    font-size: 48px;
  }
  
  .search-card {
    padding: 20px;
  }
  
  .search-header h3 {
    font-size: 1.2rem;
  }
  
  .course-content {
    padding: 16px;
  }
  
  .course-title {
    font-size: 1.1rem;
  }
  
  .course-footer {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .view-btn {
    width: 100%;
    justify-content: center;
  }
}

/* ===== 动画效果 ===== */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ===== Element Plus 组件样式覆盖 ===== */
.el-loading-mask {
  background-color: rgba(15, 15, 35, 0.8) !important;
}

.el-loading-spinner .el-loading-text {
  color: rgba(255, 255, 255, 0.8) !important;
}

.el-tag {
  border-radius: 8px !important;
  font-weight: 600 !important;
}

.el-rate__item {
  margin-right: 4px !important;
}

/* ===== 全局动画类 ===== */
.animate-float {
  animation: float 6s ease-in-out infinite;
}

.animate-glow {
  animation: textGlow 2s ease-in-out infinite;
}

.animate-shimmer {
  background: linear-gradient(90deg, 
    transparent, 
    rgba(255, 255, 255, 0.4), 
    transparent);
  background-size: 200% 100%;
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}
</style>