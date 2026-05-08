<template>
    <div class="auth-page">
        <StarrySky />
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

                    <a-form-item label="验证码" name="code">
                        <div class="code-row">
                            <a-input v-model:value="formState.code" placeholder="请输入邮箱验证码" size="large" allow-clear
                                class="code-input">
                                <template #prefix>
                                    <NumberOutlined />
                                </template>
                            </a-input>
                            <a-button size="large" class="code-btn" :disabled="sendingCode || countdown > 0"
                                :loading="sendingCode" @click="handleSendCode">
                                {{ countdown > 0 ? `${countdown}s 后重试` : '发送验证码' }}
                            </a-button>
                        </div>
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
import { reactive, ref, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import {
    UserOutlined,
    LockOutlined,
    MailOutlined,
    SafetyOutlined,
    NumberOutlined,
} from '@ant-design/icons-vue';
import type { Rule } from 'ant-design-vue/es/form';
import { register, sendRegisterCode } from '@/api/userController';
import StarrySky from '@/components/StarrySky.vue';

const router = useRouter();
const loading = ref(false);
// 是否已勾选服务协议
const agree = ref(false);
// 验证码发送状态：发送中 + 重发倒计时
const sendingCode = ref(false);
const countdown = ref(0);
let countdownTimer: ReturnType<typeof setInterval> | null = null;

// 注册表单数据
const formState = reactive<API.UserRegisterRequest>({
    userAccount: '',
    email: '',
    password: '',
    checkPassword: '',
    code: '',
});

// 确认密码校验：必须与 password 一致
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
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' },
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度需在 6-20 个字符之间', trigger: 'blur' },
    ],
    checkPassword: [
        { required: true, validator: validateCheckPassword, trigger: 'blur' },
    ],
    code: [
        { required: true, message: '请输入邮箱验证码', trigger: 'blur' },
        { len: 6, message: '验证码为 6 位数字', trigger: 'blur' },
    ],
};

// 启动 60 秒重发倒计时
function startCountdown() {
    countdown.value = 60;
    countdownTimer = setInterval(() => {
        countdown.value -= 1;
        if (countdown.value <= 0 && countdownTimer) {
            clearInterval(countdownTimer);
            countdownTimer = null;
        }
    }, 1000);
}

// 发送邮箱验证码
async function handleSendCode() {
    const email = formState.email?.trim();
    if (!email) {
        message.warning('请先输入邮箱');
        return;
    }
    // 邮箱格式简单校验
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        message.warning('邮箱格式不正确');
        return;
    }
    sendingCode.value = true;
    try {
        const res = await sendRegisterCode({ email });
        if (res.data?.data) {
            message.success('验证码已发送，请查收邮件');
            startCountdown();
        }
    } catch (err) {
        // 错误提示已由请求拦截器统一处理
    } finally {
        sendingCode.value = false;
    }
}

// 离开页面时清理定时器，避免内存泄漏
onUnmounted(() => {
    if (countdownTimer) clearInterval(countdownTimer);
});

// 注册逻辑：成功后跳转到登录页
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
    position: relative;
    height: 100vh;
    width: 100vw;
    display: grid;
    grid-template-columns: 1.1fr 1fr;
    overflow: hidden;
    background: rgba(8, 7, 15, 0.35);
    color: var(--text-primary);
}

.auth-page>.auth-hero,
.auth-page>.auth-form-wrap {
    position: relative;
    z-index: 1;
}

.auth-hero {
    position: relative;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 48px;
    border-right: 1px solid var(--border-soft);
}

.hero-content {
    position: relative;
    z-index: 2;
    color: var(--text-primary);
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
    font-size: 44px;
    font-weight: 800;
    line-height: 1.2;
    margin-bottom: 16px;
    background: linear-gradient(135deg, #f5f3ff 0%, #7dd3fc 60%, #ec4899 100%);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
}

.hero-desc {
    font-size: 15px;
    color: var(--text-secondary);
    line-height: 1.8;
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
    background: rgba(56, 189, 248, 0.15);
    border: 1px solid rgba(56, 189, 248, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 14px;
    color: var(--accent);
    backdrop-filter: blur(6px);
    box-shadow: 0 0 16px rgba(56, 189, 248, 0.3);
}

.step-text {
    font-size: 14px;
    color: var(--text-secondary);
}

.hero-deco {
    position: absolute;
    border-radius: 50%;
    filter: blur(80px);
    opacity: 0.5;
}

.hero-deco-2 {
    width: 280px;
    height: 280px;
    background: #ec4899;
    bottom: -80px;
    left: -60px;
    opacity: 0.35;
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
    padding: 32px;
    background: var(--bg-card);
    backdrop-filter: blur(var(--blur));
    border: 1px solid var(--border-soft);
    border-radius: var(--radius-card);
    box-shadow: var(--shadow-card);
}

.form-header {
    margin-bottom: 24px;
}

.form-header h2 {
    font-size: 26px;
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
    background: linear-gradient(135deg, var(--accent), var(--accent-pink)) !important;
    border: none !important;
    box-shadow: 0 8px 24px rgba(14, 165, 233, 0.4);
}

.submit-btn:hover {
    box-shadow: 0 12px 30px rgba(14, 165, 233, 0.55) !important;
    transform: translateY(-1px);
}

.form-footer {
    text-align: center;
    font-size: 14px;
    color: var(--text-secondary);
    margin-top: 8px;
}

.link-primary {
    color: var(--accent);
    font-weight: 600;
    margin: 0 2px;
}

.link-primary:hover {
    color: var(--accent-pink);
    text-decoration: underline;
}

:deep(.ant-form-item-label > label) {
    font-weight: 500;
    color: var(--text-secondary);
}

:deep(.ant-form-item) {
    margin-bottom: 18px;
}

.code-row {
    display: flex;
    gap: 10px;
}

.code-input {
    flex: 1;
}

.code-btn {
    flex-shrink: 0;
    min-width: 120px;
    border-radius: var(--radius-pill) !important;
    background: rgba(56, 189, 248, 0.12) !important;
    border-color: rgba(56, 189, 248, 0.4) !important;
    color: var(--accent) !important;
}

.code-btn:hover:not(:disabled) {
    background: rgba(56, 189, 248, 0.2) !important;
    color: #fff !important;
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
