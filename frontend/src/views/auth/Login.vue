<template>
    <div id="app">
        <div class="login-container">
            <!-- 背景动画 -->
            <ul class="bg-bubbles">
                <li v-for="n in 8" :key="n"></li>
            </ul>
            <el-card class="login-card">
                <!-- 标题 -->
                <div class="header">
                    <h2><i class="fa fa-users"></i> 用户登录</h2>
                    <p>欢迎进入学习社区，开启知识之旅</p>
                </div>

                <el-form :model="form" @submit.prevent="onSubmit">
                    <el-form-item label="用户名">
                        <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" />
                    </el-form-item>
                    <el-form-item label="密码">
                        <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock"
                            show-password />
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" :loading="loading" @click="onSubmit" class="login-btn">登录</el-button>
                    </el-form-item>
                    <el-form-item>
                        <el-button @click="goRegister" class="register-btn">注册</el-button>
                    </el-form-item>
                </el-form>

                <!-- 社交登录 -->
                <div class="social-login">
                    <div class="social-btn google"><i class="fab fa-google"></i></div>
                    <div class="social-btn github"><i class="fab fa-github"></i></div>
                    <div class="social-btn wechat"><i class="fab fa-weixin"></i></div>
                </div>
            </el-card>
        </div>
    </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../../store/auth';
import { ElMessage } from 'element-plus';
import { User, Lock } from '@element-plus/icons-vue';

const router = useRouter();
const authStore = useAuthStore();
const loading = ref(false);

const form = reactive({
    username: '',
    password: ''
});

const onSubmit = async () => {
    loading.value = true;
    try {
        const result = await authStore.login(form.username, form.password);
        console.log(result.success);
        if (result.success) {
            ElMessage.success('登录成功');
            router.push('/');
        } else {
            ElMessage.error(result.error || '登录失败');
        }
    } catch (error) {
        console.error('登录错误:', error);
    } finally {
        loading.value = false;
    }
};

const goRegister = () => {
    router.push('/register');
};
</script>

<style scoped>
/* 背景样式 */
.login-container {
    background: url("https://img.remit.ee/api/file/BQACAgUAAyEGAASHRsPbAAKBBWidbinNfYeoeR9yh0cEfFz-mOVkAAKxGwACSijwVD_nBvAtcPfmNgQ.png") center/cover no-repeat;
    min-height: 98vh;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    overflow: hidden;
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

/* 卡片样式 */
.login-card {
    width: 400px;
    padding: 40px 30px;
    background: rgba(255, 255, 255, 0.15);
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.3);
    transition: all 0.3s ease;
    animation: slideIn 0.5s ease-out;
    z-index: 3;
}

.login-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 36px rgba(0, 0, 0, 0.3);
}

/* 表单项样式 */
.el-form-item {
    margin-bottom: 20px;
}

/* 输入框 */
.el-input__inner {
    background: rgba(255, 255, 255, 0.15) !important;
    border: 1px solid rgba(255, 255, 255, 0.3) !important;
    border-radius: 12px !important;
    color: white !important;
    height: 46px !important;
}

.el-input__inner:focus {
    background: rgba(255, 255, 255, 0.15) !important;
    border-color: rgba(35, 183, 164, 0.8) !important;
    box-shadow: 0 0 8px rgba(74, 199, 195, 0.5) !important;
}

/* 按钮 */
.el-button {
    border-radius: 12px !important;
    font-size: 16px !important;
}

.login-btn {
    background: linear-gradient(135deg, #42b3a4, #66c3bf) !important;
    color: white !important;
    width: 100%;
}

.login-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(35, 183, 164, 0.6) !important;
}

.register-btn {
    background: transparent !important;
    color: white !important;
    border: 1px solid rgba(255, 255, 255, 0.4) !important;
    width: 100%;
}

.register-btn:hover {
    background: rgba(255, 255, 255, 0.1) !important;
}


/* 动画 */
@keyframes slideIn {
    0% {
        transform: translateY(-20px);
        opacity: 0;
    }

    100% {
        transform: translateY(0);
        opacity: 1;
    }
}

/* 社交按钮 */
.social-login {
    display: flex;
    justify-content: center;
    gap: 15px;
    margin: 25px 0;
}

.social-btn {
    width: 45px;
    height: 45px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.1);
    color: white;
    cursor: pointer;
    transition: var(--transition);
}

.social-btn:hover {
    transform: translateY(-3px);
    background: rgba(255, 255, 255, 0.2);
}

.google {
    color: #4285F4;
}

.github {
    color: #333;
}

.wechat {
    color: #07C160;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .login-card {
        width: 90%;
        padding: 25px;
    }
}

@media (max-width: 480px) {
    .login-card {
        width: 95%;
        padding: 20px;
    }
}
</style>