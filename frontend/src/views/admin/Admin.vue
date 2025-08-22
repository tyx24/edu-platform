<template>
  <div class="admin-container">
    <el-tabs v-model="activeTab" class="admin-tabs">
      <el-tab-pane label="用户管理" name="users">
        <div class="tab-header">
          <h2>用户管理</h2>
          <div class="search-box">
            <el-input v-model="userSearchQuery" placeholder="搜索用户名/邮箱" clearable @clear="fetchUsers"
              @keyup.enter="fetchUsers">
              <template #append>
                <el-button @click="fetchUsers">
                  <el-icon>
                    <Search />
                  </el-icon>
                </el-button>
              </template>
            </el-input>
            <el-select v-model="userRoleFilter" placeholder="角色筛选" clearable @change="fetchUsers">
              <el-option label="学生" value="student" />
              <el-option label="教师" value="teacher" />
              <el-option label="管理员" value="admin" />
            </el-select>
            <el-select v-model="userStatusFilter" placeholder="状态筛选" clearable @change="fetchUsers">
              <el-option label="冻结" value=0 />
              <el-option label="正常" value=1 />
              <el-option label="封禁" value=2 />
            </el-select>
          </div>
        </div>

        <el-table v-loading="usersLoading" :data="users" border style="width: 100%" :header-cell-style="{'text-align':'center'}">
          <el-table-column prop="id" label="ID" width="80" align="center" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="email" label="邮箱" width="230" />
          <el-table-column prop="realName" label="姓名" width="120" />
          <el-table-column prop="phone" label="电话" width="120" />
          <el-table-column label="角色" width="100" align="center">
            <template #default="scope">
              <el-tag :type="getRoleTagType(scope.row.role)">
                {{ getRoleText(scope.row.role) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag :type="getStatusTagType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="注册时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="270">
            <template #default="scope">
              <el-dropdown @command="(command) => handleStatusChange(scope.row, command)">
                <el-button size="small" type="warning">
                  修改状态<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :disabled="scope.row.status === 0" command=0>冻结</el-dropdown-item>
                    <el-dropdown-item :disabled="scope.row.status === 1" command=1>正常</el-dropdown-item>
                    <el-dropdown-item :disabled="scope.row.status === 2" command=2>封禁</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-dropdown @command="(command) => handleRoleChange(scope.row, command)">
                <el-button size="small" type="primary">
                  修改角色<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :disabled="scope.row.role === 'student'" command="student">学生</el-dropdown-item>
                    <el-dropdown-item :disabled="scope.row.role === 'teacher'" command="teacher">教师</el-dropdown-item>
                    <el-dropdown-item :disabled="scope.row.role === 'admin'" command="admin">管理员</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-container">
          <el-pagination v-model:current-page="userCurrentPage" v-model:page-size="userPageSize"
            :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="userTotal"
            @size-change="handleUserSizeChange" @current-change="handleUserCurrentChange" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="教师审核" name="teachers">
        <div class="tab-header">
          <h2>教师审核</h2>
        </div>

        <el-table v-loading="teachersLoading" :data="pendingTeachers" border style="width: 100%" :header-cell-style="{'text-align':'center'}">
          <el-table-column prop="id" label="ID" width="80" align="center" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="email" label="邮箱" width="230" />
          <el-table-column prop="realName" label="姓名" width="120" />
          <el-table-column prop="phone" label="电话" width="120" />
          <el-table-column prop="title" label="职称" width="120" />
          <el-table-column prop="department" label="院系" width="150" />
          <el-table-column prop="createTime" label="申请时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="200">
            <template #default="scope">
              <el-button size="small" type="success" @click="approveTeacher(scope.row)">
                通过
              </el-button>
              <el-button size="small" type="danger" @click="rejectTeacher(scope.row)">
                拒绝
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="pendingTeachers.length === 0 && !teachersLoading" description="暂无待审核教师" />
      </el-tab-pane>

      <el-tab-pane label="课程审核" name="courses">
        <div class="tab-header">
          <h2>课程审核</h2>
          <div class="search-box">
            <el-input v-model="courseSearchQuery" placeholder="搜索课程名称" clearable @clear="fetchPendingCourses"
              @keyup.enter="fetchPendingCourses">
              <template #append>
                <el-button @click="fetchPendingCourses">
                  <el-icon>
                    <Search />
                  </el-icon>
                </el-button>
              </template>
            </el-input>
          </div>
        </div>

        <el-table v-loading="coursesLoading" :data="pendingCourses" border style="width: 100%" :header-cell-style="{'text-align':'center'}">
          <el-table-column prop="id" label="ID" width="80" align="center" />
          <el-table-column prop="title" label="课程名称" width="250" />
          <el-table-column label="教师" width="120">
            <template #default="scope">
              <span v-if="scope.row.teacherUsername">{{ scope.row.teacherUsername }}</span>
              <span v-else>教师</span>
            </template>
          </el-table-column>
          <el-table-column prop="category" label="分类" width="120" align="center" />
          <el-table-column label="难度" width="100" align="center">
            <template #default="scope">
              <el-tag :type="getDifficultyTagType(scope.row.difficulty)">
                {{ scope.row.difficulty }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="330">
            <template #default="scope">
              <el-button size="small" type="primary" @click="viewCourseDetail(scope.row)">
                查看详情
              </el-button>
              <el-button size="small" type="success" @click="approveCourse(scope.row)">
                通过
              </el-button>
              <el-button size="small" type="danger" @click="rejectCourse(scope.row)">
                拒绝
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="pendingCourses.length === 0 && !coursesLoading" description="暂无待审核课程" />

        <!-- 拒绝原因对话框 -->
        <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="500px">
          <el-form :model="rejectForm">
            <el-form-item label="拒绝原因" :label-width="'100px'">
              <el-input v-model="rejectForm.reason" type="textarea" :rows="4" placeholder="请输入拒绝原因" />
            </el-form-item>
          </el-form>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="rejectDialogVisible = false">取消</el-button>
              <el-button type="primary" @click="confirmReject">确认</el-button>
            </span>
          </template>
        </el-dialog>
      </el-tab-pane>

      <el-tab-pane label="系统设置" name="settings">
        <div class="tab-header">
          <h2>系统设置</h2>
        </div>

        <el-card class="settings-card">
          <template #header>
            <div class="card-header">
              <span>基本设置</span>
            </div>
          </template>

          <el-form :model="systemSettings" label-width="120px">
            <el-form-item label="系统名称">
              <el-input v-model="systemSettings.siteName" />
            </el-form-item>
            <el-form-item label="系统描述">
              <el-input v-model="systemSettings.siteDescription" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item label="管理员邮箱">
              <el-input v-model="systemSettings.adminEmail" />
            </el-form-item>
            <el-form-item label="是否开放注册">
              <el-switch v-model="systemSettings.allowRegister" />
            </el-form-item>
            <el-form-item label="是否需要邮箱验证">
              <el-switch v-model="systemSettings.requireEmailVerification" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveSystemSettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, ArrowDown } from '@element-plus/icons-vue';
import { userApi } from '../../api/user';
import { courseApi } from '../../api/course';

const router = useRouter();

// 当前激活的标签页
const activeTab = ref('users');

// 用户管理相关
const users = ref([]); // 用户列表
const usersLoading = ref(false); // 用户列表加载状态
const userSearchQuery = ref(''); // 添加用户搜索框
const userRoleFilter = ref(''); // 添加用户角色筛选
const userStatusFilter = ref(); // 添加用户状态筛选
const userCurrentPage = ref(1); // 当前页码
const userPageSize = ref(10); // 每页显示的用户数量
const userTotal = ref(0); // 用户总数

// 教师审核相关
const pendingTeachers = ref([]); // 待审核教师列表
const teachersLoading = ref(false); // 教师搜索查询

// 课程审核相关
const pendingCourses = ref([]); // 待审核课程列表
const coursesLoading = ref(false); // 课程搜索查询
const courseSearchQuery = ref(''); // 课程搜索查询

// 拒绝对话框
const rejectDialogVisible = ref(false);
const rejectForm = ref({
  reason: '',
  courseId: null
});

// 系统设置
const systemSettings = ref({
  siteName: '教育平台',
  siteDescription: '提供优质在线教育服务',
  adminEmail: 'admin@example.com',
  allowRegister: true,
  requireEmailVerification: false
});

// 监听标签页变化
watch(activeTab, (newVal) => {
  if (newVal === 'users') {
    fetchUsers();
  } else if (newVal === 'teachers') {
    fetchPendingTeachers();
  } else if (newVal === 'courses') {
    fetchPendingCourses();
  }
});

// 获取用户列表
const fetchUsers = async () => {
  usersLoading.value = true;
  try {
    const params = {
      page: userCurrentPage.value,
      size: userPageSize.value,
      keyword: userSearchQuery.value,
      state: userStatusFilter.value,
    };
    const role = userRoleFilter.value;

    const res = await userApi.getUserList(params, role);
    users.value = res.records || [];
    userTotal.value = res.total || 0;
  } catch (error) {
    console.error('获取用户列表失败:', error);
    ElMessage.error('获取用户列表失败');
  } finally {
    usersLoading.value = false;
  }
};

// 获取待审核教师列表
const fetchPendingTeachers = async () => {
  teachersLoading.value = true;
  try {
    const res = await userApi.getPendingTeachers();
    pendingTeachers.value = res || [];
  } catch (error) {
    console.error('获取待审核教师列表失败:', error);
    ElMessage.error('获取待审核教师列表失败');
  } finally {
    teachersLoading.value = false;
  }
};

// 获取待审核课程列表
const fetchPendingCourses = async () => {
  coursesLoading.value = true;
  try {
    // 假设有一个获取待审核课程的API
    const params = {
      state: 0,
      keyword: courseSearchQuery.value
    };

    const res = await courseApi.getCourseList(params);
    console.log(res);
    const courses = res.records || [];
    
    // 为每个课程获取教师用户名
    for (const course of courses) {
      if (course.teacherId) {
        try {
          const teacherDetail = await userApi.getUserDetail(course.teacherId);
          course.teacherUsername = teacherDetail.username;
        } catch (error) {
          console.error(`获取教师${course.teacherId}详情失败:`, error);
          course.teacherUsername = '获取失败';
        }
      }
    }
    
    pendingCourses.value = courses;
  } catch (error) {
    console.error('获取待审核课程列表失败:', error);
    ElMessage.error('获取待审核课程列表失败');
  } finally {
    coursesLoading.value = false;
  }
};

// 处理状态变更
const handleStatusChange = async (user, newStatus) => {
  try {
    const params = new URLSearchParams();
    params.append('userId', user.id);
    params.append('status', parseInt(newStatus));

    await userApi.updateUserStatus(params);

    const statusText = getStatusText(newStatus);
    ElMessage.success(`用户状态已更新为${statusText}`);
    fetchUsers();
  } catch (error) {
    console.error('更新用户状态失败:', error);
    ElMessage.error('更新用户状态失败');
  }
};

// 处理角色变更
const handleRoleChange = async (user, newRole) => {
  try {
    const params = new URLSearchParams();
    params.append('userId', user.id);
    params.append('role', newRole);

    await userApi.updateUserRole(params);

    ElMessage.success('用户角色更新成功');
    fetchUsers();
  } catch (error) {
    console.error('更新用户角色失败:', error);
    ElMessage.error('更新用户角色失败');
  }
};

// 审核通过教师
const approveTeacher = async (teacher) => {
  try {
    const approvalData = new URLSearchParams();
    approvalData.append('userId', teacher.id);
    await userApi.approveTeacher(approvalData);

    ElMessage.success('教师审核通过成功');
    fetchPendingTeachers();
  } catch (error) {
    console.error('教师审核通过失败:', error);
    ElMessage.error('教师审核通过失败');
  }
};

// 拒绝教师申请
const rejectTeacher = async (teacher) => {
  try {
    const approvalData = new URLSearchParams();
    approvalData.append('userId', teacher.id);
    await userApi.rejectTeacher(approvalData);

    ElMessage.success('已拒绝教师申请');
    fetchPendingTeachers();
  } catch (error) {
    console.error('拒绝教师申请失败:', error);
    ElMessage.error('拒绝教师申请失败');
  }
};

// 查看课程详情
const viewCourseDetail = (course) => {
  router.push(`/course/${course.id}`);
};

// 审核通过课程
const approveCourse = async (course) => {
  try {
    await courseApi.approveCourse(course.id);

    ElMessage.success('课程审核通过成功');
    fetchPendingCourses();
  } catch (error) {
    console.error('课程审核通过失败:', error);
    ElMessage.error('课程审核通过失败');
  }
};

// 拒绝课程
const rejectCourse = (course) => {
  rejectForm.value.courseId = course.id;
  rejectForm.value.reason = '';
  rejectDialogVisible.value = true;
};

// 确认拒绝课程
const confirmReject = async () => {
  if (!rejectForm.value.reason) {
    ElMessage.warning('请输入拒绝原因');
    return;
  }

  try {
    await courseApi.rejectCourse(rejectForm.value.courseId, {
      reason: rejectForm.value.reason
    });

    ElMessage.success('课程已拒绝');
    rejectDialogVisible.value = false;
    fetchPendingCourses();
  } catch (error) {
    console.error('拒绝课程失败:', error);
    ElMessage.error('拒绝课程失败');
  }
};

// 保存系统设置
const saveSystemSettings = () => {
  // 这里应该调用保存系统设置的API
  ElMessage.success('系统设置保存成功');
};

// 处理用户分页大小变化
const handleUserSizeChange = (size) => {
  userPageSize.value = size;
  fetchUsers();
};

// 处理用户当前页变化
const handleUserCurrentChange = (page) => {
  userCurrentPage.value = page;
  fetchUsers();
};

// 获取角色标签类型
const getRoleTagType = (role) => {
  const typeMap = {
    'student': 'info',
    'teacher': 'success',
    'admin': 'danger'
  };
  return typeMap[role] || 'info';
};

// 获取角色文本
const getRoleText = (role) => {
  const textMap = {
    'student': '学生',
    'teacher': '教师',
    'admin': '管理员'
  };
  return textMap[role] || '未知';
};

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    0: 'info',     // 冻结
    1: 'success',  // 正常
    2: 'danger'    // 封禁 
  };
  return typeMap[status] || 'info';
};

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    0: '冻结',
    1: '正常',
    2: '封禁'
  };
  return textMap[status] || '未知';
};

// 获取难度标签类型
const getDifficultyTagType = (difficulty) => {
  const typeMap = {
    '初级': 'info',
    '中级': 'warning',
    '高级': 'danger'
  };
  return typeMap[difficulty] || 'info';
};

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString();
};

onMounted(() => {
  fetchUsers();
});
</script>

<style scoped>
/* 页面整体效果 */
.admin-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-size: 400% 400%;
  animation: gradientFlow 20s ease infinite;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

.admin-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(circle at 20% 80%, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(120, 219, 255, 0.2) 0%, transparent 50%);
  pointer-events: none;
  z-index: 1;
}

.admin-container>* {
  position: relative;
  z-index: 2;
}

/* 背景动画 */
@keyframes gradientFlow {

  0%,
  100% {
    background-position: 0% 50%;
  }

  50% {
    background-position: 100% 50%;
  }
}

/* 标签页样式 */
:deep(.admin-tabs) {
  margin-top: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

:deep(.admin-tabs:hover) {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

:deep(.el-tabs__header) {
  margin-bottom: 20px;
}

:deep(.el-tabs__nav-wrap) {
  background: rgba(255, 255, 255, 0.5);
  border-radius: 15px;
  padding: 5px;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.1);
}

:deep(.el-tabs__item) {
  border-radius: 12px;
  margin: 0 5px;
  padding: 12px 20px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

:deep(.el-tabs__item::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.2), transparent);
  transition: left 0.5s ease;
}

:deep(.el-tabs__item:hover::before) {
  left: 100%;
}

:deep(.el-tabs__item:hover) {
  color: #667eea;
  transform: translateY(-2px);
}

:deep(.el-tabs__item.is-active) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

/* 标签页头部 */
.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px 0;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
}

.tab-header h2 {
  margin: 0;
  color: #2c3e50;
  font-weight: 700;
  font-size: 24px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
}

.tab-header h2::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 0;
  height: 3px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 2px;
  transition: width 0.3s ease;
}

.tab-header:hover h2::after {
  width: 100%;
}

/* 搜索框样式 */
.search-box {
  display: flex;
  gap: 15px;
  align-items: center;
  flex-wrap: wrap;
  width: 60%;
}

/* 按钮样式 */
:deep(.el-button) {
  border-radius: 12px;
  font-weight: 600;
  padding: 12px 24px;
  border: none;
  backdrop-filter: blur(10px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

:deep(.el-button::after) {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: width 0.6s ease, height 0.6s ease;
}

:deep(.el-button:active::after) {
  width: 300px;
  height: 300px;
}

:deep(.el-button::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s ease;
}

:deep(.el-button:hover::before) {
  left: 100%;
}

:deep(.el-button:hover) {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

:deep(.el-button--success) {
  background: linear-gradient(135deg, #56ab2f, #a8e6cf);
  color: white;
}

:deep(.el-button--warning) {
  background: linear-gradient(135deg, #f093fb, #f5576c);
  color: white;
}

:deep(.el-button--danger) {
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  color: white;
}

/* 表格样式 */
:deep(.el-table) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  animation: tableAppear 0.8s ease-out;
}

@keyframes tableAppear {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

:deep(.el-table__header) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
}

:deep(.el-table th) {
  background: transparent;
  color: #2c3e50;
  font-weight: 700;
  border-bottom: 2px solid rgba(102, 126, 234, 0.2);
  position: relative;
}

:deep(.el-table th::before) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  transition: width 0.3s ease;
}

:deep(.el-table th:hover::before) {
  width: 100%;
}

:deep(.el-table td) {
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

:deep(.el-table__row) {
  transition: all 0.3s ease;
}

:deep(.el-table__row:hover) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  transform: translateX(5px);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
}

/* 标签样式 */
:deep(.el-tag) {
  border-radius: 20px;
  padding: 8px 16px;
  font-weight: 600;
  border: none;
  backdrop-filter: blur(10px);
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

:deep(.el-tag::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

:deep(.el-tag:hover::before) {
  left: 100%;
}

:deep(.el-tag:hover) {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

:deep(.el-tag--success) {
  background: linear-gradient(135deg, #56ab2f, #a8e6cf);
}

:deep(.el-tag--warning) {
  background: linear-gradient(135deg, #f093fb, #f5576c);
}

:deep(.el-tag--danger) {
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
}

:deep(.el-tag--info) {
  background: linear-gradient(135deg, #74b9ff, #0984e3);
}

/* 分页样式 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 20px 0;
}

:deep(.el-pagination) {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 15px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

:deep(.el-pagination .btn-prev),
:deep(.el-pagination .btn-next) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-radius: 8px;
  border: none;
  transition: all 0.3s ease;
}

:deep(.el-pagination .btn-prev:hover),
:deep(.el-pagination .btn-next:hover) {
  transform: scale(1.1);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

:deep(.el-pager li) {
  border-radius: 8px;
  margin: 0 2px;
  transition: all 0.3s ease;
}

:deep(.el-pager li:hover) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  transform: translateY(-2px);
}

:deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

/* 设置卡片样式 */
.settings-card {
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
  animation: cardAppear 0.6s ease-out;
}

@keyframes cardAppear {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.settings-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.15);
}

:deep(.settings-card .el-card__header) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  border-bottom: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 20px 20px 0 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-weight: 700;
  color: #2c3e50;
  font-size: 18px;
}

/* 表单样式 */
:deep(.el-form-item__label) {
  font-weight: 600;
  color: #2c3e50;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

:deep(.el-input__wrapper:hover) {
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(102, 126, 234, 0.5);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.2);
  transform: translateY(-1px);
}

:deep(.el-input__wrapper.is-focus) {
  background: rgba(255, 255, 255, 0.95);
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
  transform: scale(1.02);
}

:deep(.el-textarea__inner) {
  border-radius: 12px;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 12px;
}

/* 开关样式 */
:deep(.el-switch) {
  --el-switch-on-color: linear-gradient(135deg, #667eea, #764ba2);
}

:deep(.el-switch.is-checked .el-switch__core) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-color: #667eea;
}

/* 对话框样式 */
:deep(.el-dialog) {
  border-radius: 20px;
  backdrop-filter: blur(20px);
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  animation: dialogAppear 0.4s ease-out;
}

@keyframes dialogAppear {
  from {
    opacity: 0;
    transform: scale(0.8) translateY(-50px);
  }

  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 20px;
  border-radius: 20px 20px 0 0;
  position: relative;
}

:deep(.el-dialog__header::before) {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #ff6b6b, #feca57, #48dbfb, #ff9ff3, #54a0ff);
  background-size: 300% 100%;
  animation: headerShimmer 3s ease-in-out infinite;
}

@keyframes headerShimmer {

  0%,
  100% {
    background-position: 0% 50%;
  }

  50% {
    background-position: 100% 50%;
  }
}

:deep(.el-dialog__title) {
  font-weight: 700;
  font-size: 18px;
}

:deep(.el-dialog__body) {
  padding: 30px;
  background: rgba(255, 255, 255, 0.9);
}

/* 下拉菜单样式 */
:deep(.el-dropdown-menu) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  animation: dropdownAppear 0.3s ease-out;
}

@keyframes dropdownAppear {
  from {
    opacity: 0;
    transform: translateY(-10px) scale(0.95);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

:deep(.el-dropdown-menu__item) {
  transition: all 0.3s ease;
  border-radius: 8px;
  margin: 2px 4px;
}

:deep(.el-dropdown-menu__item:hover) {
  background: linear-gradient(135deg, #74b9ff, #0984e3);
  color: white;
  transform: translateX(5px);
}

/* 空状态样式 */
:deep(.el-empty) {
  padding: 60px 20px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 15px;
  margin: 20px 0;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  animation: emptyAppear 0.6s ease-out;
}

@keyframes emptyAppear {
  from {
    opacity: 0;
    transform: scale(0.9);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}

:deep(.el-empty__description) {
  color: #666;
  font-size: 16px;
  font-weight: 500;
}

/* 加载动画 */
:deep(.el-loading-mask) {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

:deep(.el-loading-spinner) {
  color: #667eea;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-container {
    padding: 0 10px;
  }

  .search-box {
    width: 100%;
    flex-direction: column;
  }

  .tab-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  :deep(.el-table) {
    font-size: 12px;
  }

  :deep(.admin-tabs) {
    padding: 15px;
    border-radius: 15px;
  }

  .pagination-container {
    justify-content: center;
  }
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 4px;
  transition: all 0.3s ease;
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #5a67d8, #6b46c1);
}

/* 文本选择样式 */
::selection {
  background: rgba(102, 126, 234, 0.3);
  color: #2c3e50;
}

::-moz-selection {
  background: rgba(102, 126, 234, 0.3);
  color: #2c3e50;
}
</style>