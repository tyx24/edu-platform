<template>
    <div id="app">
        <div class="register-container">
            <!-- 背景动画 -->
            <ul class="bg-bubbles">
                <li v-for="n in 8" :key="n"></li>
            </ul>

            <!-- 注册卡片 -->
            <el-card class="register-card">
                <!-- 标题 -->
                <div class="header">
                    <h2><i class="fas fa-user-plus"></i> 用户注册</h2>
                    <p>加入我们的学习社区，开启知识之旅</p>
                </div>

                <!-- 表单 -->
                <el-form :model="form" ref="formRef" :rules="rules" label-width="80px">
                    <el-form-item label="用户名" prop="username">
                        <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" />
                    </el-form-item>

                    <el-form-item label="邮箱" prop="email">
                        <el-input v-model="form.email" placeholder="请输入邮箱" :prefix-icon="Message" />
                    </el-form-item>

                    <el-form-item label="密码" prop="password">
                        <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock"
                            show-password />
                    </el-form-item>

                    <el-form-item label="角色" prop="role">
                        <el-select v-model="form.role" placeholder="请选择角色" class="role-select">
                            <el-option label="学生" value="student">
                                <i class="fas fa-user-graduate"></i> 学生
                            </el-option>
                            <el-option label="教师" value="teacher">
                                <i class="fas fa-chalkboard-teacher"></i> 教师
                            </el-option>
                        </el-select>
                    </el-form-item>

                    <el-form-item>
                        <el-button type="primary" :loading="loading" @click="onSubmit" class="register-btn">
                            <i class="fas fa-user-plus"></i> 立即注册
                        </el-button>
                    </el-form-item>
                        <el-form-item>
                            <el-button @click="goLogin" class="login-btn">
                                <i class="fas fa-sign-in-alt"></i> 已有账号？立即登录
                            </el-button>
                        </el-form-item>
                </el-form>

                <!-- 底部 -->
                <div class="footer">
                    <p>注册即表示您同意我们的 <a href="#">服务条款</a> 和 <a href="#">隐私政策</a></p>
                </div>
            </el-card>
        </div>
    </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../../store/auth';
import { User, Message, Lock } from '@element-plus/icons-vue';

const router = useRouter();
const authStore = useAuthStore();
const formRef = ref(null);
const loading = ref(false);

const form = reactive({
    username: '',
    email: '',
    password: '',
    role: ''
});

const rules = {
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
    role: [{ required: true, message: '请选择角色', trigger: 'change' }]
};

const onSubmit = () => {
    formRef.value.validate(async (valid) => {
        if (!valid) return;
        loading.value = true;
        try {
            await authStore.register(form);
            ElMessage.success(form.role === 'teacher'
                ? '注册成功，请等待管理员审核'
                : '注册成功，现在可以登录');
            router.push('/login');
        } finally {
            loading.value = false;
        }
    });
};

const goLogin = () => router.push('/login');
</script>

<style scoped>
/* 主题变量 */
:root {
    --primary-color: #4ac7c3;
    --primary-gradient: linear-gradient(135deg, #23b7a4 0%, #4ac7c3 100%);
    --card-bg: rgba(255, 255, 255, 0.15);
    --input-bg: rgba(255, 255, 255, 0.1);
    --input-border: rgba(255, 255, 255, 0.3);
    --input-focus: rgba(255, 255, 255, 0.6);
    --shadow: rgba(31, 38, 135, 0.37);
    --transition: all 0.3s ease; 
}

/* 容器 */
.register-container {
    background: url("https://img.remit.ee/api/file/BQACAgUAAyEGAASHRsPbAAKBBWidbinNfYeoeR9yh0cEfFz-mOVkAAKxGwACSijwVD_nBvAtcPfmNgQ.png") center/cover no-repeat;
    min-height: 98vh;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
}

/* 背景动画 */
.bg-bubbles {
    position: absolute;
    inset: 0;
    overflow: hidden;
    pointer-events: none;
}

.bg-bubbles li {
    position: absolute;
    width: 40px;
    height: 40px;
    background: rgba(255, 255, 255, 0.15);
    bottom: -160px;
    border-radius: 50%;
    animation: float 25s infinite linear;
}

.bg-bubbles li:nth-child(2) {
    left: 20%;
    width: 80px;
    height: 80px;
    animation-delay: 2s;
    animation-duration: 17s;
}

.bg-bubbles li:nth-child(4) {
    left: 40%;
    width: 60px;
    height: 60px;
    background: rgba(255, 255, 255, 0.25);
    animation-duration: 22s;
}

.bg-bubbles li:nth-child(6) {
    left: 80%;
    width: 120px;
    height: 120px;
    background: rgba(255, 255, 255, 0.2);
    animation-delay: 3s;
}

.bg-bubbles li:nth-child(8) {
    left: 55%;
    width: 20px;
    height: 20px;
    animation-delay: 15s;
    animation-duration: 40s;
}

@keyframes float {
    0% {
        transform: translateY(0) rotate(0deg);
        opacity: 1;
    }

    100% {
        transform: translateY(-1000px) rotate(720deg);
        opacity: 0;
    }
}

/* 卡片 */
.register-card {
    width: 100%;
    max-width: 450px;
    padding: 40px;
    background: rgba(255, 255, 255, 0.15);
    border-radius: 20px;
    box-shadow: 0 8px 32px var(--shadow);
    backdrop-filter: blur(12px);
    border: 1px solid rgba(255, 255, 255, 0.3);
    transition: var(--transition);
    z-index: 3;
}

.register-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 40px var(--shadow);
}

/* 标题 */
.header {
    text-align: center;
    margin-bottom: 30px;
}

.header h2 {
    font-size: 28px;
    color: white;
    margin-bottom: 8px;
    font-weight: 600;
}

.header p {
    color: rgba(255, 255, 255, 0.8);
}

/* 输入框 */
.el-input__inner {
    background: var(--input-bg) !important;
    border: 1px solid var(--input-border) !important;
    border-radius: 12px !important;
    color: white !important;
    height: 46px !important;
}

.el-input__inner:focus {
    background: rgba(255, 255, 255, 0.15) !important;
    border-color: var(--input-focus) !important;
    box-shadow: 0 0 8px rgba(74, 199, 195, 0.5) !important;
}

/* 按钮 */
.el-button {
    border-radius: 12px !important;
    font-size: 16px !important;
}

.register-btn {
    background: linear-gradient(135deg, #42b3a4, #66c3bf) !important;
    color: white !important;
    width: 100%;
}

.register-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(35, 183, 164, 0.6) !important;
}

.login-btn {
    background: transparent !important;
    color: white !important;
    border: 1px solid rgba(255, 255, 255, 0.4) !important;
    width: 100%;
}

.login-btn:hover {
    background: rgba(255, 255, 255, 0.1) !important;
}


/* 底部 */
.footer {
    text-align: center;
    font-size: 14px;
    color: rgba(255, 255, 255, 0.7);
}

.footer a {
    color: white;
    font-weight: 500;
}

.footer a:hover {
    color: var(--primary-color);
    text-decoration: underline;
}

/* 响应式 */
@media (max-width: 600px) {
    .register-card {
        padding: 30px 20px;
    }

    .header h2 {
        font-size: 24px;
    }
}

@media (max-width: 400px) {
    .register-card {
        padding: 25px 15px;
    }

    .header h2 {
        font-size: 22px;
    }
}
</style>