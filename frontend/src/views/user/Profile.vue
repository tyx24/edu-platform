<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h2>个人资料</h2>
        </div>
      </template>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="6" animated />
      </div>
      
      <div v-else class="profile-content">
        <div class="avatar-section">
          <div class="avatar-container">
            <el-avatar :size="100" :src="userProfile.avatar || defaultAvatar">
              {{ userProfile.username ? userProfile.username.charAt(0).toUpperCase() : 'U' }}
            </el-avatar>
          </div>
          <el-upload
            class="avatar-uploader"
            action="/edu/user/upload-avatar"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <el-button size="small" type="primary">更换头像</el-button>
          </el-upload>
        </div>
        
        <div class="info-section">
          <div class="info-header">
            <h3>基本信息</h3>
            <el-button type="primary" @click="editBasicInfo">编辑</el-button>
          </div>
          
          <div class="info-content">
            <div class="info-item">
              <span class="info-label">用户名:</span>
              <span class="info-value">{{ userProfile.username }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">邮箱:</span>
              <span class="info-value">{{ userProfile.email }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">真实姓名:</span>
              <span class="info-value">{{ userProfile.realName || '未设置' }}</span>
            </div>
            <div class="info-item role-item">
              <span class="info-label">角色:</span>
              <span class="info-value">
                <div class="role-badge" :class="'role-' + userProfile.role">
                  <component :is="getRoleIconClass(userProfile.role)" class="role-icon" />
                  <span>{{ getRoleText(userProfile.role) }}</span>
                </div>
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">注册时间:</span>
              <span class="info-value">{{ formatDateTime(userProfile.createTime) }}</span>
            </div>
          </div>
        </div>
        
        <div class="info-section">
          <div class="info-header">
            <h3>联系方式</h3>
            <el-button type="primary" @click="editContactInfo">编辑</el-button>
          </div>
          
          <div class="info-content">
            <div class="info-item">
              <span class="info-label">手机号码:</span>
              <span class="info-value">{{ userProfile.phone || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">QQ:</span>
              <span class="info-value">{{ userProfile.qq || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">微信:</span>
              <span class="info-value">{{ userProfile.wechat || '未设置' }}</span>
            </div>
          </div>
        </div>
        
        <div v-if="userProfile.role === 'student'" class="info-section">
          <div class="info-header">
            <h3>学生信息</h3>
            <el-button type="primary" @click="editStudentInfo">编辑</el-button>
          </div>
          
          <div class="info-content">
            <div class="info-item">
              <span class="info-label">学号:</span>
              <span class="info-value">{{ userProfile.studentId || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">院系:</span>
              <span class="info-value">{{ userProfile.department || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">专业:</span>
              <span class="info-value">{{ userProfile.major || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">年级:</span>
              <span class="info-value">{{ userProfile.grade || '未设置' }}</span>
            </div>
          </div>
        </div>
        
        <div v-if="userProfile.role === 'teacher'" class="info-section">
          <div class="info-header">
            <h3>教师信息</h3>
            <el-button type="primary" @click="editTeacherInfo">编辑</el-button>
          </div>
          
          <div class="info-content">
            <div class="info-item">
              <span class="info-label">职称:</span>
              <span class="info-value">{{ userProfile.title || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">院系:</span>
              <span class="info-value">{{ userProfile.department || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">研究方向:</span>
              <span class="info-value">{{ userProfile.research || '未设置' }}</span>
            </div>
          </div>
        </div>
        
        <div class="info-section">
          <div class="info-header">
            <h3>个人简介</h3>
            <el-button type="primary" @click="editBio">编辑</el-button>
          </div>
          
          <div class="info-content">
            <div class="bio-content">
              {{ userProfile.bio || '暂无个人简介' }}
            </div>
          </div>
        </div>
        
        <div class="info-section">
          <div class="info-header">
            <h3>安全设置</h3>
          </div>
          
          <div class="security-content">
            <div class="security-item">
              <div class="security-info">
                <div class="security-title">修改密码</div>
                <div class="security-desc">定期修改密码可以保护账号安全</div>
              </div>
              <el-button @click="showChangePasswordDialog">修改</el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 基本信息编辑对话框 -->
    <el-dialog
      v-model="basicInfoDialogVisible"
      title="编辑基本信息"
      width="500px"
    >
      <el-form :model="basicInfoForm" :rules="basicInfoRules" ref="basicInfoFormRef" label-width="100px">
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="basicInfoForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="basicInfoForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="basicInfoDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveBasicInfo">保存</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 联系方式编辑对话框 -->
    <el-dialog
      v-model="contactInfoDialogVisible"
      title="编辑联系方式"
      width="500px"
    >
      <el-form :model="contactInfoForm" :rules="contactInfoRules" ref="contactInfoFormRef" label-width="100px">
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="contactInfoForm.phone" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item label="QQ" prop="qq">
          <el-input v-model="contactInfoForm.qq" placeholder="请输入QQ号码" />
        </el-form-item>
        <el-form-item label="微信" prop="wechat">
          <el-input v-model="contactInfoForm.wechat" placeholder="请输入微信号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="contactInfoDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveContactInfo">保存</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 学生信息编辑对话框 -->
    <el-dialog
      v-model="studentInfoDialogVisible"
      title="编辑学生信息"
      width="500px"
    >
      <el-form :model="studentInfoForm" :rules="studentInfoRules" ref="studentInfoFormRef" label-width="100px">
        <el-form-item label="学号" prop="studentId">
          <el-input v-model="studentInfoForm.studentId" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="院系" prop="department">
          <el-input v-model="studentInfoForm.department" placeholder="请输入院系" />
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-input v-model="studentInfoForm.major" placeholder="请输入专业" />
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model="studentInfoForm.grade" placeholder="请输入年级" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="studentInfoDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveStudentInfo">保存</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 教师信息编辑对话框 -->
    <el-dialog
      v-model="teacherInfoDialogVisible"
      title="编辑教师信息"
      width="500px"
    >
      <el-form :model="teacherInfoForm" :rules="teacherInfoRules" ref="teacherInfoFormRef" label-width="100px">
        <el-form-item label="职称" prop="title">
          <el-input v-model="teacherInfoForm.title" placeholder="请输入职称" />
        </el-form-item>
        <el-form-item label="院系" prop="department">
          <el-input v-model="teacherInfoForm.department" placeholder="请输入院系" />
        </el-form-item>
        <el-form-item label="研究方向" prop="research">
          <el-input v-model="teacherInfoForm.research" placeholder="请输入研究方向" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="teacherInfoDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveTeacherInfo">保存</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 个人简介编辑对话框 -->
    <el-dialog
      v-model="bioDialogVisible"
      title="编辑个人简介"
      width="500px"
    >
      <el-form :model="bioForm" ref="bioFormRef" label-width="100px">
        <el-form-item label="个人简介">
          <el-input 
            v-model="bioForm.bio" 
            type="textarea" 
            :rows="6" 
            placeholder="请输入个人简介"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="bioDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveBio">保存</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="500px"
    >
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="changePassword">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { userApi } from '../../api/user';
import { userinfoApi } from '../../api/userinfo';
import { useAuthStore } from '../../store/auth';
import { UserFilled, School, Setting } from '@element-plus/icons-vue';

const authStore = useAuthStore();

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// 加载状态
const loading = ref(true);

// 用户资料
const userProfile = ref({});

// 上传头像的请求头
const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${localStorage.getItem('token')}`
  };
});

// 对话框显示状态
const basicInfoDialogVisible = ref(false);
const contactInfoDialogVisible = ref(false);
const studentInfoDialogVisible = ref(false);
const teacherInfoDialogVisible = ref(false);
const bioDialogVisible = ref(false);
const passwordDialogVisible = ref(false);

// 表单引用
const basicInfoFormRef = ref(null);
const contactInfoFormRef = ref(null);
const studentInfoFormRef = ref(null);
const teacherInfoFormRef = ref(null);
const bioFormRef = ref(null);
const passwordFormRef = ref(null);

// 表单数据
const basicInfoForm = ref({
  realName: '',
  email: ''
});

const contactInfoForm = ref({
  phone: '',
  qq: '',
  wechat: ''
});

const studentInfoForm = ref({
  studentId: '',
  department: '',
  major: '',
  grade: ''
});

const teacherInfoForm = ref({
  title: '',
  department: '',
  research: ''
});

const bioForm = ref({
  bio: ''
});

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 表单验证规则
const basicInfoRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
};

const contactInfoRules = {
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  qq: [
    { pattern: /^\d{5,11}$/, message: '请输入正确的QQ号码', trigger: 'blur' }
  ]
};

const studentInfoRules = {
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' }
  ]
};

const teacherInfoRules = {
  title: [
    { required: true, message: '请输入职称', trigger: 'blur' }
  ],
  department: [
    { required: true, message: '请输入院系', trigger: 'blur' }
  ]
};

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

// 获取用户资料
const fetchUserProfile = async () => {
  loading.value = true;
  try {
    // 获取基本用户信息（用户名、邮箱、角色、注册时间）
    const basicInfo = await userApi.getUserInfo();
    // 获取详细用户信息（头像、真实姓名、联系方式等）
    const profileData = await userinfoApi.getUserProfile({ pageNum: 1, pageSize: 100 });
    
    // 从返回的数据中找到当前用户的详细信息
    let userDetailInfo = {};
    const currentUserId = basicInfo.id;
    
    // 遍历数字索引找到匹配的用户信息
    for (let key in profileData.records) {
      if (typeof profileData.records[key] === 'object' && profileData.records[key].userId === currentUserId) {
        userDetailInfo = profileData.records[key];
        break;
      }
    }
    console.log(userDetailInfo);
    
    // 合并基本信息和详细信息
    userProfile.value = { ...basicInfo, ...userDetailInfo } || {};
    console.log(userProfile.value);
  } catch (error) {
    console.error('获取用户资料失败:', error);
    ElMessage.error('获取用户资料失败');
  } finally {
    loading.value = false;
  }
};

// 处理头像上传成功
const handleAvatarSuccess = (response) => {
  if (response.code === 0) {
    userProfile.value.avatar = response.data.url;
    ElMessage.success('头像上传成功');
  } else {
    ElMessage.error(response.message || '头像上传失败');
  }
};

// 头像上传前的校验
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg';
  const isPNG = file.type === 'image/png';
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isJPG && !isPNG) {
    ElMessage.error('上传头像图片只能是 JPG 或 PNG 格式!');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!');
    return false;
  }
  return true;
};

// 编辑基本信息
const editBasicInfo = () => {
  basicInfoForm.value.realName = userProfile.value.realName || '';
  basicInfoForm.value.email = userProfile.value.email || '';
  basicInfoDialogVisible.value = true;
};

// 保存基本信息
const saveBasicInfo = async () => {
  if (!basicInfoFormRef.value) return;
  
  await basicInfoFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await userinfoApi.updateBasicInfo(basicInfoForm.value);
        ElMessage.success('基本信息更新成功');
        basicInfoDialogVisible.value = false;
        fetchUserProfile();
      } catch (error) {
        console.error('更新基本信息失败:', error);
        ElMessage.error('更新基本信息失败');
      }
    }
  });
};

// 编辑联系方式
const editContactInfo = () => {
  contactInfoForm.value = {
    phone: userProfile.value.phone || '',
    qq: userProfile.value.qq || '',
    wechat: userProfile.value.wechat || ''
  };
  contactInfoDialogVisible.value = true;
};

// 保存联系方式
const saveContactInfo = async () => {
  if (!contactInfoFormRef.value) return;
  
  await contactInfoFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await userinfoApi.updateContactInfo(contactInfoForm.value);
        ElMessage.success('联系方式更新成功');
        contactInfoDialogVisible.value = false;
        fetchUserProfile();
      } catch (error) {
        console.error('更新联系方式失败:', error);
        ElMessage.error('更新联系方式失败');
      }
    }
  });
};

// 编辑学生信息
const editStudentInfo = () => {
  studentInfoForm.value = {
    studentId: userProfile.value.studentId || '',
    department: userProfile.value.department || '',
    major: userProfile.value.major || '',
    grade: userProfile.value.grade || ''
  };
  studentInfoDialogVisible.value = true;
};

// 保存学生信息
const saveStudentInfo = async () => {
  if (!studentInfoFormRef.value) return;
  
  await studentInfoFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await userinfoApi.updateStudentInfo(studentInfoForm.value);
        ElMessage.success('学生信息更新成功');
        studentInfoDialogVisible.value = false;
        fetchUserProfile();
      } catch (error) {
        console.error('更新学生信息失败:', error);
        ElMessage.error('更新学生信息失败');
      }
    }
  });
};

// 编辑教师信息
const editTeacherInfo = () => {
  teacherInfoForm.value = {
    title: userProfile.value.title || '',
    department: userProfile.value.department || '',
    research: userProfile.value.research || ''
  };
  teacherInfoDialogVisible.value = true;
};

// 保存教师信息
const saveTeacherInfo = async () => {
  if (!teacherInfoFormRef.value) return;
  
  await teacherInfoFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await userinfoApi.updateTeacherInfo(teacherInfoForm.value);
        ElMessage.success('教师信息更新成功');
        teacherInfoDialogVisible.value = false;
        fetchUserProfile();
      } catch (error) {
        console.error('更新教师信息失败:', error);
        ElMessage.error('更新教师信息失败');
      }
    }
  });
};

// 编辑个人简介
const editBio = () => {
  bioForm.value.bio = userProfile.value.bio || '';
  bioDialogVisible.value = true;
};

// 保存个人简介
const saveBio = async () => {
  try {
    await userinfoApi.updateBio(bioForm.value);
    ElMessage.success('个人简介更新成功');
    bioDialogVisible.value = false;
    fetchUserProfile();
  } catch (error) {
    console.error('更新个人简介失败:', error);
    ElMessage.error('更新个人简介失败');
  }
};

// 显示修改密码对话框
const showChangePasswordDialog = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  };
  passwordDialogVisible.value = true;
};

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return;
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await userinfoApi.changePassword({
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword
        });
        ElMessage.success('密码修改成功');
        passwordDialogVisible.value = false;
      } catch (error) {
        console.error('修改密码失败:', error);
        ElMessage.error('修改密码失败');
      }
    }
  });
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

// 获取角色图标类
const getRoleIconClass = (role) => {
  const iconMap = {
    'student': 'UserFilled',
    'teacher': 'School',
    'admin': 'Setting'
  };
  return iconMap[role] || 'UserFilled';
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

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString();
};

onMounted(() => {
  fetchUserProfile();
});
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 30px auto;
  padding: 0 30px;
  animation: fadeIn 0.8s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.profile-card {
  margin-bottom: 30px;
  border-radius: 16px;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
  overflow: hidden;
  transition: all 0.4s ease;
  border: none;
}

.profile-card:hover {
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.15);
  transform: translateY(-5px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #1890ff, #36cfc9, #52c41a);
  padding: 20px 30px;
  color: white;
}

.card-header h2 {
  margin: 0;
  font-size: 28px;
  color: white;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
  letter-spacing: 1px;
}

.loading-container {
  padding: 30px;
}

.profile-content {
  padding: 40px 30px;
  background-color: #fafafa;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 50px;
  position: relative;
  padding: 30px 0;
  background: linear-gradient(to bottom, rgba(24, 144, 255, 0.05), transparent);
  border-radius: 20px;
}

.avatar-container {
  margin-bottom: 25px;
  position: relative;
  z-index: 1;
}

.avatar-container .el-avatar {
  border: 6px solid white;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  transition: all 0.4s ease;
  width: 150px !important;
  height: 150px !important;
  font-size: 60px;
}

.avatar-container .el-avatar:hover {
  transform: scale(1.08);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.2);
  border-color: #f0f7ff;
}

.avatar-uploader .el-button {
  background: linear-gradient(135deg, #1890ff, #36cfc9, #52c41a);
  border: none;
  padding: 12px 25px;
  transition: all 0.4s ease;
  font-weight: 600;
  font-size: 16px;
  border-radius: 50px;
}

.avatar-uploader .el-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 16px rgba(24, 144, 255, 0.4);
  letter-spacing: 1px;
}

.info-section {
  margin-bottom: 35px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  padding-bottom: 25px;
  transition: all 0.3s ease;
}

.info-section:hover {
  border-bottom-color: rgba(24, 144, 255, 0.3);
}

.info-section:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.info-header h3 {
  margin: 0;
  font-size: 18px;
  color: #1890ff;
  font-weight: 600;
  position: relative;
  padding-left: 12px;
}

.info-header h3::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 18px;
  background: linear-gradient(to bottom, #1890ff, #36cfc9);
  border-radius: 2px;
}

.info-header .el-button {
  background: linear-gradient(135deg, #1890ff, #36cfc9, #52c41a);
  border: none;
  padding: 10px 20px;
  transition: all 0.4s ease;
  border-radius: 50px;
  font-weight: 600;
}

.info-header .el-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 16px rgba(24, 144, 255, 0.4);
  letter-spacing: 0.5px;
}

.info-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 25px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 12px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border-left: 4px solid #1890ff;
}

.info-item:hover {
  background-color: #f0f7ff;
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.08);
  border-left: 4px solid #52c41a;
}

.role-item {
  overflow: visible;
}

.role-badge {
  display: inline-flex;
  align-items: center;
  border-radius: 50px;
  font-weight: 700;
  font-size: 16px;
  color: white;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
  transition: all 0.4s ease;
  position: relative;
  overflow: hidden;
  min-width: 140px;
  justify-content: center;
  letter-spacing: 1px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  animation: roleBadgePulse 2s infinite alternate;
}

.role-badge::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(45deg, rgba(255,255,255,0.1), rgba(255,255,255,0));
  z-index: 1;
}

.role-badge:hover {
  transform: translateY(-5px) scale(1.08);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.25);
  animation-play-state: paused;
}

@keyframes roleBadgePulse {
  0% {
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
  }
  100% {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.25), 0 0 0 5px rgba(255, 255, 255, 0.1);
  }
}

.role-badge .role-icon {
  margin-right: 10px;
  font-size: 20px;
  animation: iconRotate 3s infinite alternate;
}

@keyframes iconRotate {
  0% {
    transform: rotate(-5deg);
  }
  100% {
    transform: rotate(5deg);
  }
}

.role-student {
  background: linear-gradient(135deg, #1890ff, #36cfc9);
  border: 2px solid rgba(24, 144, 255, 0.3);
}

.role-teacher {
  background: linear-gradient(135deg, #52c41a, #85ce61);
  border: 2px solid rgba(82, 196, 26, 0.3);
}

.role-admin {
  background: linear-gradient(135deg, #f5222d, #ff7875);
  border: 2px solid rgba(245, 34, 45, 0.3);
}

.info-label {
  font-weight: 600;
  color: #555;
  font-size: 15px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  color: #222;
  font-size: 18px;
  font-weight: 500;
}

.role-tag {
  font-size: 15px;
  padding: 5px 15px;
}
.bio-content {
  grid-column: span 3;
  white-space: pre-wrap;
  line-height: 1.8;
  color: #333;
  background-color: #f0f7ff;
  padding: 30px;
  border-radius: 12px;
  box-shadow: inset 0 2px 6px rgba(0, 0, 0, 0.05);
  border-left: 6px solid #52c41a;
  font-size: 16px;
  position: relative;
}

.bio-content::before {
  content: '"';
  position: absolute;
  top: 10px;
  left: 15px;
  font-size: 60px;
  color: rgba(24, 144, 255, 0.1);
  font-family: serif;
  line-height: 1;
}

.security-content {
  grid-column: span 3;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25px 30px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border-left: 4px solid #ff4d4f;
  margin-top: 10px;
}

.security-item:hover {
  background-color: #fff1f0;
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.security-title {
  font-weight: 700;
  margin-bottom: 10px;
  color: #ff4d4f;
  font-size: 18px;
}

.security-desc {
  color: #666;
  font-size: 14px;
}

.el-dialog {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  border: none;
}

.el-dialog__header {
  background: linear-gradient(135deg, #1890ff, #36cfc9, #52c41a);
  padding: 20px 30px;
}

.el-dialog__title {
  color: white;
  font-weight: 700;
  font-size: 22px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

.el-dialog__body {
  padding: 40px 30px;
  background-color: #fafafa;
}

.el-dialog__footer {
  padding: 15px 20px;
  border-top: 1px solid #eee;
}

.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #1890ff, #36cfc9, #52c41a);
  border: none;
  padding: 12px 25px;
  transition: all 0.4s ease;
  border-radius: 50px;
  font-weight: 600;
}

.dialog-footer .el-button--primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 16px rgba(24, 144, 255, 0.4);
  letter-spacing: 0.5px;
}

@media (max-width: 1200px) {
  .info-content {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .bio-content {
    grid-column: span 2;
  }
  
  .security-content {
    grid-column: span 2;
  }
}

@media (max-width: 768px) {
  .info-content {
    grid-template-columns: 1fr;
  }
  
  .bio-content {
    grid-column: span 1;
  }
  
  .security-content {
    grid-column: span 1;
  }
  
  .profile-container {
    padding: 0 15px;
    margin: 15px auto;
  }
  
  .profile-content {
    padding: 25px 15px;
  }
  
  .card-header {
    padding: 15px 20px;
  }
  
  .card-header h2 {
    font-size: 22px;
  }
}
</style>