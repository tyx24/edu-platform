<template>
  <div class="admin-container">
    <el-tabs v-model="activeTab" class="admin-tabs">
      <el-tab-pane label="用户管理" name="users">
        <div class="tab-header">
          <h2>用户管理</h2>
          <div class="search-box">
            <el-input
              v-model="userSearchQuery"
              placeholder="搜索用户名/邮箱"
              clearable
              @clear="fetchUsers"
              @keyup.enter="fetchUsers"
            >
              <template #append>
                <el-button @click="fetchUsers">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
            <el-select v-model="userRoleFilter" placeholder="角色筛选" clearable @change="fetchUsers">
              <el-option label="学生" value="STUDENT" />
              <el-option label="教师" value="TEACHER" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
            <el-select v-model="userStatusFilter" placeholder="状态筛选" clearable @change="fetchUsers">
              <el-option label="启用" value="ENABLED" />
              <el-option label="禁用" value="DISABLED" />
            </el-select>
          </div>
        </div>
        
        <el-table
          v-loading="usersLoading"
          :data="users"
          border
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="email" label="邮箱" width="180" />
          <el-table-column prop="realName" label="姓名" width="120" />
          <el-table-column prop="phone" label="电话" width="120" />
          <el-table-column label="角色" width="100">
            <template #default="scope">
              <el-tag :type="getRoleTagType(scope.row.role)">
                {{ getRoleText(scope.row.role) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'ENABLED' ? 'success' : 'danger'">
                {{ scope.row.status === 'ENABLED' ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="注册时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="220">
            <template #default="scope">
              <el-button 
                size="small" 
                :type="scope.row.status === 'ENABLED' ? 'danger' : 'success'"
                @click="toggleUserStatus(scope.row)"
              >
                {{ scope.row.status === 'ENABLED' ? '禁用' : '启用' }}
              </el-button>
              <el-dropdown @command="(command) => handleRoleChange(scope.row, command)">
                <el-button size="small" type="primary">
                  修改角色<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :disabled="scope.row.role === 'STUDENT'" command="STUDENT">学生</el-dropdown-item>
                    <el-dropdown-item :disabled="scope.row.role === 'TEACHER'" command="TEACHER">教师</el-dropdown-item>
                    <el-dropdown-item :disabled="scope.row.role === 'ADMIN'" command="ADMIN">管理员</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="userCurrentPage"
            v-model:page-size="userPageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="userTotal"
            @size-change="handleUserSizeChange"
            @current-change="handleUserCurrentChange"
          />
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="教师审核" name="teachers">
        <div class="tab-header">
          <h2>教师审核</h2>
        </div>
        
        <el-table
          v-loading="teachersLoading"
          :data="pendingTeachers"
          border
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="email" label="邮箱" width="180" />
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
              <el-button 
                size="small" 
                type="success"
                @click="approveTeacher(scope.row)"
              >
                通过
              </el-button>
              <el-button 
                size="small" 
                type="danger"
                @click="rejectTeacher(scope.row)"
              >
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
            <el-input
              v-model="courseSearchQuery"
              placeholder="搜索课程名称"
              clearable
              @clear="fetchPendingCourses"
              @keyup.enter="fetchPendingCourses"
            >
              <template #append>
                <el-button @click="fetchPendingCourses">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>
        </div>
        
        <el-table
          v-loading="coursesLoading"
          :data="pendingCourses"
          border
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="课程名称" width="180" />
          <el-table-column prop="teacherName" label="教师" width="120" />
          <el-table-column prop="category" label="分类" width="120" />
          <el-table-column label="难度" width="100">
            <template #default="scope">
              <el-tag :type="getDifficultyTagType(scope.row.difficulty)">
                {{ getDifficultyText(scope.row.difficulty) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="280">
            <template #default="scope">
              <el-button 
                size="small" 
                type="primary"
                @click="viewCourseDetail(scope.row)"
              >
                查看详情
              </el-button>
              <el-button 
                size="small" 
                type="success"
                @click="approveCourse(scope.row)"
              >
                通过
              </el-button>
              <el-button 
                size="small" 
                type="danger"
                @click="rejectCourse(scope.row)"
              >
                拒绝
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <el-empty v-if="pendingCourses.length === 0 && !coursesLoading" description="暂无待审核课程" />
        
        <!-- 拒绝原因对话框 -->
        <el-dialog
          v-model="rejectDialogVisible"
          title="拒绝原因"
          width="500px"
        >
          <el-form :model="rejectForm">
            <el-form-item label="拒绝原因" :label-width="'100px'">
              <el-input 
                v-model="rejectForm.reason" 
                type="textarea" 
                :rows="4"
                placeholder="请输入拒绝原因"
              />
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
const users = ref([]);
const usersLoading = ref(false);
const userSearchQuery = ref('');
const userRoleFilter = ref('');
const userStatusFilter = ref('');
const userCurrentPage = ref(1);
const userPageSize = ref(10);
const userTotal = ref(0);

// 教师审核相关
const pendingTeachers = ref([]);
const teachersLoading = ref(false);

// 课程审核相关
const pendingCourses = ref([]);
const coursesLoading = ref(false);
const courseSearchQuery = ref('');

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
      role: userRoleFilter.value,
      status: userStatusFilter.value
    };
    
    const res = await userApi.getUserPage(params);
    users.value = res.data.records || [];
    userTotal.value = res.data.total || 0;
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
    pendingTeachers.value = res.data || [];
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
      status: 'PENDING',
      keyword: courseSearchQuery.value
    };
    
    const res = await courseApi.getPendingCourses(params);
    pendingCourses.value = res.data || [];
  } catch (error) {
    console.error('获取待审核课程列表失败:', error);
    ElMessage.error('获取待审核课程列表失败');
  } finally {
    coursesLoading.value = false;
  }
};

// 切换用户状态
const toggleUserStatus = async (user) => {
  try {
    const newStatus = user.status === 'ENABLED' ? 'DISABLED' : 'ENABLED';
    await userApi.updateUserStatus({
      userId: user.id,
      status: newStatus
    });
    
    ElMessage.success(`用户${newStatus === 'ENABLED' ? '启用' : '禁用'}成功`);
    fetchUsers();
  } catch (error) {
    console.error('更新用户状态失败:', error);
    ElMessage.error('更新用户状态失败');
  }
};

// 处理角色变更
const handleRoleChange = async (user, newRole) => {
  try {
    await userApi.updateUserRole({
      userId: user.id,
      role: newRole
    });
    
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
    await userApi.approveTeacher({
      userId: teacher.id
    });
    
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
    await userApi.rejectTeacher({
      userId: teacher.id
    });
    
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
    'STUDENT': 'info',
    'TEACHER': 'success',
    'ADMIN': 'danger'
  };
  return typeMap[role] || 'info';
};

// 获取角色文本
const getRoleText = (role) => {
  const textMap = {
    'STUDENT': '学生',
    'TEACHER': '教师',
    'ADMIN': '管理员'
  };
  return textMap[role] || '未知';
};

// 获取难度标签类型
const getDifficultyTagType = (difficulty) => {
  const typeMap = {
    'BEGINNER': 'info',
    'INTERMEDIATE': 'warning',
    'ADVANCED': 'danger'
  };
  return typeMap[difficulty] || 'info';
};

// 获取难度文本
const getDifficultyText = (difficulty) => {
  const textMap = {
    'BEGINNER': '初级',
    'INTERMEDIATE': '中级',
    'ADVANCED': '高级'
  };
  return textMap[difficulty] || '未知';
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
.admin-container {
  padding: 20px;
}

.admin-tabs {
  margin-top: 20px;
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.tab-header h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.search-box {
  display: flex;
  gap: 10px;
  width: 60%;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.settings-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

@media (max-width: 768px) {
  .search-box {
    width: 100%;
    flex-direction: column;
  }
  
  .tab-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>