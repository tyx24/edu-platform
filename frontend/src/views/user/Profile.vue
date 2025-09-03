<template>
  <div class="profile-container">
    <!-- 极简页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title" data-text="个人资料">个人资料</h1>
        <p class="page-subtitle">管理您的个人信息和账户设置</p>
      </div>
    </div>

    <!-- 主要内容 -->
    <div class="main-content">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="6" animated />
      </div>

      <div v-else class="profile-content">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <div class="avatar-container">
            <el-avatar :size="120" :src="avatarUrl" class="user-avatar">
              {{ userProfile.username ? userProfile.username.charAt(0).toUpperCase() : 'U' }}
            </el-avatar>
          </div>
          
          <div class="user-info">
            <h3 class="user-name">{{ userProfile.realName || userProfile.username || '用户' }}</h3>
            <div class="user-role-highlight">
              <div class="role-badge" :class="'role-' + userProfile.role">
                <span class="role-text">{{ getRoleText(userProfile.role) }}</span>
              </div>
            </div>
          </div>
          
          <el-upload class="avatar-uploader" action="/edu/userInfo/avatar" :headers="uploadHeaders"
          :data="{ userId: userProfile.id }" :show-file-list="false" :on-success="handleAvatarSuccess" :before-upload="beforeAvatarUpload">
            <el-button size="small" type="primary">更换头像</el-button>
          </el-upload>
        </div>

        <!-- 基本信息卡片 -->
        <div class="info-section">
          <div class="section-header">
            <h3 class="section-title">基本信息</h3>
            <el-button type="primary" class="edit-btn" @click="editBasicInfo">编辑信息</el-button>
          </div>
          
          <div class="info-grid">
            <div class="info-item">
              <div class="info-label">用户名</div>
              <div class="info-value">{{ userProfile.username || '未设置' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">邮箱</div>
              <div class="info-value">{{ userProfile.email || '未设置' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">真实姓名</div>
              <div class="info-value">{{ userProfile.realName || '未设置' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">角色</div>
              <div class="info-value">
                <span class="role-badge" :class="'role-' + userProfile.role">
                  {{ getRoleText(userProfile.role) }}
                </span>
              </div>
            </div>
            <div class="info-item">
              <div class="info-label">注册时间</div>
              <div class="info-value">{{ formatDateTime(userProfile.createTime) }}</div>
            </div>
          </div>
        </div>

        <!-- 联系方式卡片 -->
        <div class="info-section">
          <div class="section-header">
            <h3 class="section-title">联系方式</h3>
            <el-button type="primary" class="edit-btn" @click="editContactInfo">编辑信息</el-button>
          </div>
          
          <div class="info-grid">
            <div class="info-item">
              <div class="info-label">手机号码</div>
              <div class="info-value">{{ userProfile.phone || '未设置' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">QQ号</div>
              <div class="info-value">{{ userProfile.qq || '未设置' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">微信号</div>
              <div class="info-value">{{ userProfile.wechat || '未设置' }}</div>
            </div>
          </div>
        </div>

        <!-- 学生信息卡片 -->
        <div v-if="userProfile.role === 'student'" class="info-section">
          <div class="section-header">
            <h3 class="section-title">学生信息</h3>
            <el-button type="primary" class="edit-btn" @click="editStudentInfo">编辑信息</el-button>
          </div>
          
          <div class="info-grid">
            <div class="info-item">
              <div class="info-label">学号</div>
              <div class="info-value">{{ userProfile.studentId || '未设置' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">院系</div>
              <div class="info-value">{{ userProfile.department || '未设置' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">专业</div>
              <div class="info-value">{{ userProfile.major || '未设置' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">年级</div>
              <div class="info-value">{{ userProfile.grade || '未设置' }}</div>
            </div>
          </div>
        </div>

        <!-- 教师信息卡片 -->
        <div v-if="userProfile.role === 'teacher'" class="info-section">
          <div class="section-header">
            <h3 class="section-title">教师信息</h3>
            <el-button type="primary" class="edit-btn" @click="editTeacherInfo">编辑信息</el-button>
          </div>
          
          <div class="info-grid">
            <div class="info-item">
              <div class="info-label">职称</div>
              <div class="info-value">{{ userProfile.title || '未设置' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">院系</div>
              <div class="info-value">{{ userProfile.department || '未设置' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">研究方向</div>
              <div class="info-value">{{ userProfile.research || '未设置' }}</div>
            </div>
          </div>
        </div>

        <!-- 个人简介卡片 -->
        <div class="info-section">
          <div class="section-header">
            <h3 class="section-title">个人简介</h3>
            <el-button type="primary" class="edit-btn" @click="editBio">编辑信息</el-button>
          </div>
          
          <div class="bio-content">
            <p>{{ userProfile.bio || '暂无个人简介' }}</p>
          </div>
        </div>

        <!-- 安全设置卡片 -->
        <div class="info-section">
          <div class="section-header">
            <h3 class="section-title">安全设置</h3>
          </div>
          
          <div class="security-list">
            <div class="security-item" @click="showChangePasswordDialog">
              <div class="security-content">
                <div class="security-title">修改密码</div>
                <div class="security-desc">定期更换密码，保护账户安全</div>
              </div>
              <el-icon class="security-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑信息对话框 -->
    <el-dialog
      v-model="showEditDialog"
      :title="getDialogTitle()"
      width="500px"
      class="custom-dialog"
    >
      <el-form :model="editForm" label-width="100px" class="edit-form">
        <!-- 基本信息编辑 -->
        <template v-if="editDialogType === 'basic'">
          <el-form-item label="用户名">
            <el-input v-model="editForm.username" disabled />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="editForm.email" />
          </el-form-item>
          <el-form-item label="真实姓名">
            <el-input v-model="editForm.realName" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="editForm.phone" />
          </el-form-item>
        </template>
        
        <!-- 联系方式编辑 -->
        <template v-else-if="editDialogType === 'contact'">
          <el-form-item label="手机号">
            <el-input v-model="editForm.phone" />
          </el-form-item>
          <el-form-item label="QQ号">
            <el-input v-model="editForm.qq" />
          </el-form-item>
          <el-form-item label="微信号">
            <el-input v-model="editForm.wechat" />
          </el-form-item>
        </template>
        
        <!-- 学生信息编辑 -->
        <template v-else-if="editDialogType === 'student'">
          <el-form-item label="学号">
            <el-input v-model="editForm.studentId" />
          </el-form-item>
          <el-form-item label="院系">
            <el-input v-model="editForm.department" />
          </el-form-item>
          <el-form-item label="专业">
            <el-input v-model="editForm.major" />
          </el-form-item>
          <el-form-item label="年级">
            <el-input v-model="editForm.grade" />
          </el-form-item>
        </template>
        
        <!-- 教师信息编辑 -->
        <template v-else-if="editDialogType === 'teacher'">
          <el-form-item label="职称">
            <el-input v-model="editForm.title" />
          </el-form-item>
          <el-form-item label="院系">
            <el-input v-model="editForm.department" />
          </el-form-item>
          <el-form-item label="研究方向">
            <el-input v-model="editForm.research" type="textarea" :rows="3" />
          </el-form-item>
        </template>
        
        <!-- 个人简介编辑 -->
        <template v-else-if="editDialogType === 'bio'">
          <el-form-item label="个人简介">
            <el-input v-model="editForm.bio" type="textarea" :rows="4" placeholder="请输入个人简介" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" @click="saveInfo">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="showPasswordDialog"
      title="修改密码"
      width="500px"
      class="custom-dialog"
    >
      <el-form :model="passwordForm" label-width="100px" class="edit-form">
        <el-form-item label="当前密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showPasswordDialog = false">取消</el-button>
          <el-button type="primary" @click="savePassword">确认修改</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import { userApi } from '../../api/user'
import { userinfoApi } from '../../api/userinfo'
import { useAuthStore } from '../../store/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 加载状态
const loading = ref(true)

// 用户资料数据
const userProfile = ref({})

// 上传头像的请求头
const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${localStorage.getItem('token')}`
  }
})

// 处理头像URL
const avatarUrl = computed(() => {
  if (!userProfile.value.avatar) return defaultAvatar
  const avatar = userProfile.value.avatar
  // 如果是完整的URL（http或https开头），直接返回
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }
  // 如果已经有/edu前缀，直接返回
  if (avatar.startsWith('/edu')) {
    return avatar
  }
  // 否则添加/edu前缀
  return `/edu${avatar}`
})

// 对话框显示状态
const showEditDialog = ref(false)
const showPasswordDialog = ref(false)
const editDialogType = ref('basic') // 'basic', 'contact', 'student', 'teacher', 'bio'

// 表单数据
const editForm = ref({
  username: '',
  email: '',
  realName: '',
  phone: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 获取用户资料
const fetchUserProfile = async () => {
  loading.value = true
  try {
    // 获取基本用户信息（用户名、邮箱、角色、注册时间）
    const basicInfo = await userApi.getUserInfo()
    // 获取详细用户信息（头像、真实姓名、联系方式等）
    const profileData = await userinfoApi.getUserProfile({ pageNum: 1, pageSize: 100 })

    // 从返回的数据中找到当前用户的详细信息
    let userDetailInfo = {}
    const currentUserId = basicInfo.id

    // 遍历数字索引找到匹配的用户信息
    for (let key in profileData.records) {
      if (typeof profileData.records[key] === 'object' && profileData.records[key].userId === currentUserId) {
        userDetailInfo = profileData.records[key]
        break
      }
    }

    // 合并基本信息和详细信息
    userProfile.value = { ...basicInfo, ...userDetailInfo } || {}
  } catch (error) {
    console.error('获取用户资料失败:', error)
    ElMessage.error('获取用户资料失败')
  } finally {
    loading.value = false
  }
}

// 处理头像上传成功
const handleAvatarSuccess = (response) => {
  if (typeof response === 'string') {
    userProfile.value.avatar = response
    ElMessage.success('头像上传成功')
  } else if (response && response.code === 0) {
    userProfile.value.avatar = response.data || response
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response?.message || '头像上传失败')
  }
}

// 头像上传前的校验
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('上传头像图片只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 编辑基本信息
const editBasicInfo = () => {
  editForm.value = {
    username: userProfile.value.username || '',
    email: userProfile.value.email || '',
    realName: userProfile.value.realName || '',
    phone: userProfile.value.phone || ''
  }
  editDialogType.value = 'basic'
  showEditDialog.value = true
}

// 编辑联系方式
const editContactInfo = () => {
  editForm.value = {
    phone: userProfile.value.phone || '',
    qq: userProfile.value.qq || '',
    wechat: userProfile.value.wechat || ''
  }
  editDialogType.value = 'contact'
  showEditDialog.value = true
}

// 编辑学生信息
const editStudentInfo = () => {
  editForm.value = {
    studentId: userProfile.value.studentId || '',
    department: userProfile.value.department || '',
    major: userProfile.value.major || '',
    grade: userProfile.value.grade || ''
  }
  editDialogType.value = 'student'
  showEditDialog.value = true
}

// 编辑教师信息
const editTeacherInfo = () => {
  editForm.value = {
    title: userProfile.value.title || '',
    department: userProfile.value.department || '',
    research: userProfile.value.research || ''
  }
  editDialogType.value = 'teacher'
  showEditDialog.value = true
}

// 编辑个人简介
const editBio = () => {
  editForm.value = {
    bio: userProfile.value.bio || ''
  }
  editDialogType.value = 'bio'
  showEditDialog.value = true
}

// 保存信息 - 根据类型调用不同的API
const saveInfo = async () => {
  try {
    const requestData = {
      ...editForm.value,
      userId: userProfile.value.id
    }

    // 根据编辑类型调用不同的API
    switch (editDialogType.value) {
      case 'basic':
        await userinfoApi.updateBasicInfo(requestData)
        break
      case 'contact':
        await userinfoApi.updateContactInfo(requestData)
        break
      case 'student':
        await userinfoApi.updateStudentInfo(requestData)
        break
      case 'teacher':
        await userinfoApi.updateTeacherInfo(requestData)
        break
      case 'bio':
        await userinfoApi.updateBio(requestData)
        break
      default:
        throw new Error('未知的编辑类型')
    }
    
    ElMessage.success('信息更新成功')
    showEditDialog.value = false
    await fetchUserProfile() // 重新获取用户信息
  } catch (error) {
    console.error('更新信息失败:', error)
    ElMessage.error(error.response?.data?.message || '更新信息失败')
  }
}

// 显示修改密码对话框
const showChangePasswordDialog = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  showPasswordDialog.value = true
}

// 修改密码
const savePassword = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }

  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    ElMessage.error('请填写完整的密码信息')
    return
  }

  if (passwordForm.value.newPassword.length < 6) {
    ElMessage.error('新密码长度不能少于6位')
    return
  }

  try {
    const res = await userApi.changePassword({
      userId: userProfile.value.id,
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    
    // 检查响应状态
    if (res && (res.code === 200 || res.code === 0 || res.success)) {
      ElMessage.success('密码修改成功，请重新登录')
      showPasswordDialog.value = false
      
      // 密码修改成功后，退出登录
      setTimeout(async () => {
        try {
          await authStore.logout()
          router.push('/login')
        } catch (logoutError) {
          console.error('登出失败:', logoutError)
          // 即使登出失败也要清除本地信息并跳转
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          router.push('/login')
        }
      }, 1500) // 1.5秒后自动登出
    } else {
      ElMessage.error(res?.message || res?.msg || '密码修改失败')
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    const errorMessage = error.response?.data?.message || error.response?.data?.msg || error.message || '修改密码失败'
    ElMessage.error(errorMessage)
  }
}

// 获取对话框标题
const getDialogTitle = () => {
  const titleMap = {
    'basic': '编辑基本信息',
    'contact': '编辑联系方式',
    'student': '编辑学生信息',
    'teacher': '编辑教师信息',
    'bio': '编辑个人简介'
  }
  return titleMap[editDialogType.value] || '编辑信息'
}

// 获取角色文本
const getRoleText = (role) => {
  const textMap = {
    'student': '学生',
    'teacher': '教师',
    'admin': '管理员'
  }
  return textMap[role] || '未知'
}

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  fetchUserProfile()
})
</script>

<style scoped>
/* ===== 全屏容器样式 ===== */
.profile-container {
  min-height: calc(100vh - 60px);
  background: 
    linear-gradient(135deg, #0f0f23 0%, #1a1a2e 25%, #16213e 75%, #0f3460 100%),
    radial-gradient(ellipse at top, rgba(120, 119, 198, 0.1) 0%, transparent 50%),
    radial-gradient(ellipse at bottom, rgba(255, 119, 198, 0.08) 0%, transparent 50%);
  background-attachment: fixed;
  display: flex;
  flex-direction: column;
  width: 100%;
  overflow-x: hidden;
  position: relative;
}

.profile-container::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 30% 20%, rgba(56, 189, 248, 0.15) 0%, transparent 45%),
    radial-gradient(circle at 70% 80%, rgba(139, 92, 246, 0.12) 0%, transparent 40%),
    radial-gradient(circle at 20% 70%, rgba(236, 72, 153, 0.1) 0%, transparent 35%),
    linear-gradient(135deg, rgba(59, 130, 246, 0.05) 0%, rgba(147, 51, 234, 0.05) 100%);
  pointer-events: none;
  animation: cosmicDrift 30s ease-in-out infinite;
  z-index: 0;
}

@keyframes cosmicDrift {
  0%, 100% {
    transform: translateX(0) translateY(0) rotate(0deg);
    opacity: 1;
  }
  25% {
    transform: translateX(5px) translateY(-10px) rotate(1deg);
    opacity: 0.8;
  }
  50% {
    transform: translateX(-3px) translateY(-5px) rotate(-0.5deg);
    opacity: 0.9;
  }
  75% {
    transform: translateX(-8px) translateY(-15px) rotate(1.5deg);
    opacity: 0.7;
  }
}

/* ===== 现代科技感页面头部 ===== */
.page-header {
  background: 
    linear-gradient(135deg, rgba(15, 15, 35, 0.95) 0%, rgba(26, 26, 46, 0.9) 100%),
    linear-gradient(45deg, rgba(56, 189, 248, 0.1) 0%, rgba(139, 92, 246, 0.1) 100%);
  backdrop-filter: blur(20px) saturate(180%);
  color: white;
  padding: 50px 0;
  position: relative;
  margin: 0;
  border-bottom: 1px solid rgba(56, 189, 248, 0.2);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.3),
    0 1px 0 rgba(255, 255, 255, 0.1) inset;
  z-index: 10;
  overflow: hidden;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 40px;
}

.page-title {
  font-size: 3rem;
  font-weight: 900;
  margin: 0 0 16px 0;
  letter-spacing: -0.02em;
  line-height: 1.1;
  background: linear-gradient(135deg, 
    #ffffff 0%, 
    #38bdf8 25%, 
    #8b5cf6 50%, 
    #ec4899 75%, 
    #ffffff 100%);
  background-size: 300% 300%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: titleGradient 6s ease-in-out infinite;
  text-shadow: 0 0 30px rgba(56, 189, 248, 0.5);
  text-align: center;
}

@keyframes titleGradient {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.page-subtitle {
  font-size: 1.25rem;
  margin: 0;
  font-weight: 500;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  text-align: center;
}

/* ===== 主要内容区域 ===== */
.main-content {
  flex: 1;
  max-width: 1400px;
  margin: 0 auto;
  padding: 60px 40px 80px;
  position: relative;
  z-index: 5;
  width: 100%;
  box-sizing: border-box;
}

/* ===== 简洁头像区域样式 ===== */
.avatar-section {
  text-align: center;
  padding: 40px;
  background: 
    linear-gradient(135deg, 
      rgba(15, 15, 35, 0.85) 0%, 
      rgba(26, 26, 46, 0.8) 100%);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(56, 189, 248, 0.2);
  margin-bottom: 24px;
  color: rgba(255, 255, 255, 0.95);
}

.avatar-container {
  margin-bottom: 24px;
}

.user-avatar {
  border: 3px solid rgba(56, 189, 248, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
}

.user-avatar:hover {
  border-color: rgba(56, 189, 248, 0.6);
  box-shadow: 0 12px 40px rgba(56, 189, 248, 0.2);
  transform: scale(1.05);
}

.user-info {
  margin-bottom: 24px;
}

.user-name {
  font-size: 1.8rem;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.95);
  margin: 0 0 16px 0;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

/* ===== 突出用户角色样式 ===== */
.user-role-highlight {
  display: flex;
  justify-content: center;
  margin-bottom: 8px;
}

.role-badge {
  display: inline-flex;
  align-items: center;
  padding: 5px 50px;
  border-radius: 25px;
  font-size: 1.1rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
  overflow: hidden;
}

.role-badge::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s;
}

.role-badge:hover::before {
  left: 100%;
}

.role-badge.role-student {
  background: linear-gradient(135deg, #38bdf8 0%, #0ea5e9 100%);
  color: white;
  border: 2px solid rgba(56, 189, 248, 0.5);
}

.role-badge.role-student:hover {
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 15px 35px rgba(56, 189, 248, 0.4);
}

.role-badge.role-teacher {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  color: white;
  border: 2px solid rgba(139, 92, 246, 0.5);
}

.role-badge.role-teacher:hover {
  background: linear-gradient(135deg, #7c3aed 0%, #6d28d9 100%);
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 15px 35px rgba(139, 92, 246, 0.4);
}

.role-badge.role-admin {
  background: linear-gradient(135deg, #ec4899 0%, #db2777 100%);
  color: white;
  border: 2px solid rgba(236, 72, 153, 0.5);
}

.role-badge.role-admin:hover {
  background: linear-gradient(135deg, #db2777 0%, #be185d 100%);
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 15px 35px rgba(236, 72, 153, 0.4);
}

.role-text {
  position: relative;
  z-index: 1;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.avatar-uploader {
  display: inline-block;
}

.avatar-uploader .el-button {
  background: linear-gradient(135deg, #4f46e5, #7c3aed) !important;
  border: none !important;
  border-radius: 20px !important;
  padding: 20px 50px !important;
  font-weight: 600 !important;
  color: white !important;
  transition: all 0.3s ease !important;
  box-shadow: 0 4px 15px rgba(79, 70, 229, 0.3) !important;
}

.avatar-uploader .el-button:hover {
  background: linear-gradient(135deg, #4338ca, #6d28d9) !important;
  transform: translateY(-2px) !important;
  box-shadow: 0 8px 25px rgba(79, 70, 229, 0.4) !important;
}

/* ===== 未来感信息卡片样式 ===== */
.info-section {
  background: 
    linear-gradient(135deg, 
      rgba(15, 15, 35, 0.85) 0%, 
      rgba(26, 26, 46, 0.8) 50%, 
      rgba(22, 33, 62, 0.9) 100%),
    radial-gradient(ellipse at top left, rgba(56, 189, 248, 0.1) 0%, transparent 50%),
    radial-gradient(ellipse at bottom right, rgba(139, 92, 246, 0.08) 0%, transparent 50%);
  backdrop-filter: blur(20px) saturate(180%);
  border-radius: 24px;
  margin-bottom: 40px;
  padding: 48px;
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.4),
    0 8px 32px rgba(56, 189, 248, 0.1),
    0 0 0 1px rgba(56, 189, 248, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(56, 189, 248, 0.2);
  transition: all 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
  overflow: hidden;
  color: rgba(255, 255, 255, 0.95);
}

.info-section:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 
    0 32px 80px rgba(0, 0, 0, 0.5),
    0 16px 48px rgba(56, 189, 248, 0.2),
    0 0 0 1px rgba(56, 189, 248, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
  padding-bottom: 28px;
  border-bottom: 2px solid rgba(56, 189, 248, 0.2);
}

.section-title {
  font-size: 1.4rem;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.95);
  margin: 0;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.edit-btn {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 50%, #c2410c 100%) !important;
  border: none !important;
  border-radius: 12px !important;
  padding: 12px 24px !important;
  font-weight: 600 !important;
  color: white !important;
  box-shadow: 0 8px 25px rgba(249, 115, 22, 0.3) !important;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275) !important;
  position: relative;
  overflow: hidden;
}

.edit-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.25), transparent);
  transition: left 0.6s;
}

.edit-btn:hover::before {
  left: 100%;
}

.edit-btn:hover {
  background: linear-gradient(135deg, #ea580c 0%, #c2410c 50%, #9a3412 100%) !important;
  transform: translateY(-3px) scale(1.08) !important;
  box-shadow: 
    0 20px 40px rgba(249, 115, 22, 0.4),
    0 8px 16px rgba(234, 88, 12, 0.3) !important;
}

/* ===== 未来感信息网格布局 ===== */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 28px;
  margin-top: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 32px;
  background: 
    linear-gradient(135deg, 
      rgba(26, 26, 46, 0.8) 0%, 
      rgba(22, 33, 62, 0.9) 100%);
  backdrop-filter: blur(15px);
  border-radius: 20px;
  border: 1px solid rgba(56, 189, 248, 0.3);
  transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
  overflow: hidden;
  color: rgba(255, 255, 255, 0.9);
}

.info-item:hover {
  transform: translateY(-6px) scale(1.02);
  border-color: rgba(56, 189, 248, 0.5);
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.4),
    0 0 30px rgba(56, 189, 248, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.info-label {
  font-size: 0.9rem;
  color: rgba(56, 189, 248, 0.9);
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
  text-shadow: 0 0 10px rgba(56, 189, 248, 0.5);
}

.info-value {
  font-size: 1.3rem;
  color: rgba(255, 255, 255, 0.95);
  font-weight: 600;
  line-height: 1.4;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

/* ===== 个人简介区域 ===== */
.bio-content {
  padding: 24px;
  background: 
    linear-gradient(135deg, 
      rgba(26, 26, 46, 0.6) 0%, 
      rgba(22, 33, 62, 0.7) 100%);
  border-radius: 16px;
  border: 1px solid rgba(56, 189, 248, 0.2);
  backdrop-filter: blur(10px);
}

.bio-content p {
  color: rgba(255, 255, 255, 0.9);
  font-size: 1.1rem;
  line-height: 1.8;
  margin: 0;
  font-weight: 400;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

/* ===== 未来感安全设置样式 ===== */
.security-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  background: 
    linear-gradient(135deg, rgba(15, 15, 35, 0.6), rgba(26, 26, 46, 0.7));
  border-radius: 16px;
  overflow: hidden;
  margin-top: 12px;
  border: 1px solid rgba(56, 189, 248, 0.2);
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32px 36px;
  background: 
    linear-gradient(135deg, 
      rgba(26, 26, 46, 0.8) 0%, 
      rgba(22, 33, 62, 0.9) 100%);
  cursor: pointer;
  transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
  overflow: hidden;
}

.security-item:hover {
  background: 
    linear-gradient(135deg, 
      rgba(26, 26, 46, 0.95) 0%, 
      rgba(22, 33, 62, 0.98) 100%);
  transform: translateX(20px);
  box-shadow: 
    0 15px 35px rgba(0, 0, 0, 0.4),
    0 0 30px rgba(56, 189, 248, 0.2);
}

.security-content {
  flex: 1;
}

.security-title {
  font-size: 1.2rem;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.95);
  margin-bottom: 8px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.security-desc {
  font-size: 0.95rem;
  color: rgba(156, 163, 175, 0.9);
  line-height: 1.5;
}

.security-arrow {
  color: rgba(56, 189, 248, 0.7);
  font-size: 22px;
  transition: all 0.4s ease;
}

.security-item:hover .security-arrow {
  color: rgba(56, 189, 248, 1);
  transform: translateX(8px) scale(1.1);
  filter: drop-shadow(0 0 10px rgba(56, 189, 248, 0.7));
}

/* ===== 未来感对话框样式 ===== */
.custom-dialog :deep(.el-dialog) {
  border-radius: 24px;
  box-shadow: 
    0 40px 80px rgba(0, 0, 0, 0.5),
    0 20px 40px rgba(56, 189, 248, 0.2);
  overflow: hidden;
  background: 
    linear-gradient(135deg, 
      rgba(15, 15, 35, 0.95) 0%, 
      rgba(26, 26, 46, 0.9) 100%);
  backdrop-filter: blur(25px);
  border: 1px solid rgba(56, 189, 248, 0.3);
}

.custom-dialog :deep(.el-dialog__header) {
  background: 
    linear-gradient(135deg, 
      rgba(56, 189, 248, 0.2) 0%, 
      rgba(139, 92, 246, 0.15) 100%);
  color: white;
  padding: 32px 40px;
  margin: 0;
  border-bottom: 1px solid rgba(56, 189, 248, 0.3);
}

.custom-dialog :deep(.el-dialog__title) {
  color: white;
  font-weight: 700;
  font-size: 1.3rem;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.edit-form {
  padding: 40px 0;
}

.edit-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: rgba(56, 189, 248, 0.9);
  font-size: 1rem;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.edit-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(56, 189, 248, 0.3);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.edit-form :deep(.el-input__wrapper):hover {
  border-color: rgba(56, 189, 248, 0.6);
  box-shadow: 0 0 0 1px rgba(56, 189, 248, 0.4);
}

.edit-form :deep(.el-input__inner) {
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
  background: transparent;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 20px;
  padding: 32px 40px;
  background: 
    linear-gradient(135deg, 
      rgba(15, 15, 35, 0.95) 0%, 
      rgba(26, 26, 46, 0.9) 100%);
  margin: 0 -40px -32px;
  border-top: 1px solid rgba(56, 189, 248, 0.2);
}

.dialog-footer .el-button {
  border-radius: 12px;
  padding: 12px 32px;
  font-weight: 600;
  transition: all 0.4s ease;
  min-width: 120px;
}

.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #38bdf8, #8b5cf6, #ec4899) !important;
  border: none !important;
  box-shadow: 0 8px 25px rgba(56, 189, 248, 0.4) !important;
}

.dialog-footer .el-button--primary:hover {
  transform: translateY(-3px) scale(1.05) !important;
  box-shadow: 
    0 15px 35px rgba(56, 189, 248, 0.5),
    0 8px 20px rgba(139, 92, 246, 0.4) !important;
}

/* ===== 响应式设计 ===== */
@media (max-width: 768px) {
  .header-content {
    padding: 0 20px;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .main-content {
    padding: 40px 20px 60px;
  }
  
  .info-section {
    padding: 32px 24px;
    margin-bottom: 24px;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .info-item {
    padding: 24px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}
</style>