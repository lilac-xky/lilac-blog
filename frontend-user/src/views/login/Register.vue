<template>
    <div class="auth-page">
        <!-- 左侧品牌区 -->
        <div class="auth-hero">
            <div class="hero-content">
                <div class="hero-logo">
                    <img src="@/assets/logo.png" alt="logo" />
                    <span>lilac-blog</span>
                </div>
                <h1 class="hero-title">加入我们</h1>
                <p class="hero-desc">
                    创建一个账号，打造属于你自己的博客空间。<br />
                    在这里记录成长、分享见解、连接世界。
                </p>
                <div class="hero-steps">
                    <div class="step">
                        <div class="step-num">1</div>
                        <div class="step-text">填写账号信息</div>
                    </div>
                    <div class="step">
                        <div class="step-num">2</div>
                        <div class="step-text">设置安全密码</div>
                    </div>
                    <div class="step">
                        <div class="step-num">3</div>
                        <div class="step-text">开启创作之旅</div>
                    </div>
                </div>
            </div>
            <div class="hero-deco hero-deco-2"></div>
        </div>

        <!-- 右侧表单区 -->
        <div class="auth-form-wrap">
            <div class="auth-form">
                <div class="form-header">
                    <h2>创建账号</h2>
                    <p>只需几步，即可开始使用 lilac-blog</p>
                </div>

                <a-form :model="formState" :rules="rules" layout="vertical" @finish="handleRegister">
                    <a-form-item label="账号" name="userAccount">
                        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" size="large" allow-clear>
                            <template #prefix>
                                <UserOutlined />
                            </template>
                        </a-input>
                    </a-form-item>

                    <a-form-item label="邮箱" name="email">
                        <a-input v-model:value="formState.email" placeholder="请输入邮箱" size="large" allow-clear>
                            <template #prefix>
                                <MailOutlined />
                            </template>
                        </a-input>
                    </a-form-item>

                    <a-form-item label="密码" name="password">
                        <a-input-password v-model:value="formState.password" placeholder="6-20 位密码" size="large"
                            allow-clear>
                            <template #prefix>
                                <LockOutlined />
                            </template>
                        </a-input-password>
                    </a-form-item>

                    <a-form-item label="确认密码" name="checkPassword">
                        <a-input-password v-model:value="formState.checkPassword" placeholder="请再次输入密码" size="large"
                            allow-clear>
                            <template #prefix>
                                <SafetyOutlined />
                            </template>
                        </a-input-password>
                    </a-form-item>

                    <a-form-item name="agree" :rules="[{ validator: validateAgree }]">
                        <a-checkbox v-model:checked="agree">
                            我已阅读并同意
                            <a class="link-primary">《服务协议》</a>
                            和
                            <a class="link-primary">《隐私政策》</a>
                        </a-checkbox>
                    </a-form-item>

                    <a-form-item>
                        <a-button type="primary" html-type="submit" size="large" block :loading="loading"
                            class="submit-btn">
                            注 册
                        </a-button>
                    </a-form-item>

                    <div class="form-footer">
                        已有账号？
                        <router-link to="/login" class="link-primary">直接登录</router-link>
                    </div>
                </a-form>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import {
    UserOutlined,
    LockOutlined,
    MailOutlined,
    SafetyOutlined,
} from '@ant-design/icons-vue';
import type { Rule } from 'ant-design-vue/es/form';
import { register } from '@/api/userController';

const router = useRouter();
const loading = ref(false);
const agree = ref(false);

// 表单数据
const formState = reactive<API.UserRegisterRequest>({
    userAccount: '',
    email: '',
    password: '',
    checkPassword: '',
});

// 确认密码校验
const validateCheckPassword = async (_rule: Rule, value: string) => {
    if (!value) {
        return Promise.reject('请再次输入密码');
    }
    if (value !== formState.password) {
        return Promise.reject('两次输入的密码不一致');
    }
    return Promise.resolve();
};

// 同意协议校验
const validateAgree = async () => {
    if (!agree.value) {
        return Promise.reject('请先阅读并同意服务协议');
    }
    return Promise.resolve();
};

// 表单校验规则
const rules: Record<string, Rule[]> = {
    userAccount: [
        { required: true, message: '请输入账号', trigger: 'blur' },
        { max: 50, message: '账号不能超过 50 个字符', trigger: 'blur' },
    ],
    email: [
        { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' },
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度需在 6-20 个字符之间', trigger: 'blur' },
    ],
    checkPassword: [
        { required: true, validator: validateCheckPassword, trigger: 'blur' },
    ],
};

// 注册逻辑
async function handleRegister() {
    loading.value = true;
    try {
        const res = await register(formState);
        if (res.data?.data) {
            message.success('注册成功，请登录');
            router.push('/login');
        }
    } catch (err) {
        // 错误提示已由请求拦截器统一处理
    } finally {
        loading.value = false;
    }
}
</script>

<style scoped>
.auth-page {
    height: 100vh;
    width: 100vw;
    display: grid;
    grid-template-columns: 1.1fr 1fr;
    overflow: hidden;
    background: var(--bg-page);
}

.auth-hero {
    position: relative;
    background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 60%, #3a3a3a 100%);
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 48px;
}

.hero-content {
    position: relative;
    z-index: 2;
    color: #fff;
    max-width: 440px;
}

.hero-logo {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 40px;
}

.hero-logo img {
    width: 40px;
    height: 40px;
    border-radius: 10px;
}

.hero-logo span {
    font-size: 20px;
    font-weight: 700;
    letter-spacing: 0.5px;
}

.hero-title {
    font-size: 40px;
    font-weight: 800;
    line-height: 1.2;
    margin-bottom: 16px;
}

.hero-desc {
    font-size: 15px;
    color: rgba(255, 255, 255, 0.7);
    line-height: 1.7;
    margin-bottom: 40px;
}

.hero-steps {
    display: flex;
    flex-direction: column;
    gap: 14px;
}

.step {
    display: flex;
    align-items: center;
    gap: 14px;
}

.step-num {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.15);
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 14px;
    color: #fff;
    backdrop-filter: blur(6px);
}

.step-text {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.85);
}

.hero-deco {
    position: absolute;
    border-radius: 50%;
    filter: blur(60px);
    opacity: 0.35;
}

.hero-deco-2 {
    width: 260px;
    height: 260px;
    background: #f59e0b;
    bottom: -60px;
    left: -60px;
}

.auth-form-wrap {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 32px;
    overflow-y: auto;
}

.auth-form {
    width: 100%;
    max-width: 400px;
}

.form-header {
    margin-bottom: 28px;
}

.form-header h2 {
    font-size: 28px;
    font-weight: 700;
    color: var(--text-primary);
    margin-bottom: 8px;
}

.form-header p {
    font-size: 14px;
    color: var(--text-secondary);
}

.submit-btn {
    height: 46px !important;
    border-radius: var(--radius-pill) !important;
    font-size: 15px;
    font-weight: 600;
    letter-spacing: 2px;
}

.form-footer {
    text-align: center;
    font-size: 14px;
    color: var(--text-secondary);
    margin-top: 8px;
}

.link-primary {
    color: var(--text-primary);
    font-weight: 600;
    margin: 0 2px;
}

.link-primary:hover {
    text-decoration: underline;
}


:deep(.ant-form-item-label > label) {
    font-weight: 500;
    color: var(--text-primary);
}

:deep(.ant-form-item) {
    margin-bottom: 18px;
}

@media (max-width: 900px) {
    .auth-page {
        grid-template-columns: 1fr;
    }

    .auth-hero {
        display: none;
    }
}
</style>
