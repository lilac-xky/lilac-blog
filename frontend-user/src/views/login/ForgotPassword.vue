<template>
    <AuthLayout hero-title="找回密码" form-title="重置密码" form-subtitle="通过邮箱验证码安全地重置你的登录密码" :show-deco1="false"
        form-padding="32px" class="forgot-page">
        <template #hero-desc>
            忘记密码不要紧。<br />
            验证你的注册邮箱，即可设置一个全新的密码。
        </template>
        <template #hero-extras>
            <div class="hero-steps">
                <div class="step">
                    <div class="step-num">1</div>
                    <div class="step-text">输入注册邮箱</div>
                </div>
                <div class="step">
                    <div class="step-num">2</div>
                    <div class="step-text">填写邮箱验证码</div>
                </div>
                <div class="step">
                    <div class="step-num">3</div>
                    <div class="step-text">设置新密码</div>
                </div>
            </div>
        </template>

        <a-form :model="formState" :rules="rules" layout="vertical" @finish="handleReset">
            <a-form-item label="邮箱" name="email">
                <a-input v-model:value="formState.email" placeholder="请输入注册邮箱" size="large" allow-clear>
                    <template #prefix>
                        <MailOutlined />
                    </template>
                </a-input>
            </a-form-item>

            <a-form-item label="验证码" name="code">
                <div class="code-row">
                    <a-input v-model:value="formState.code" placeholder="请输入邮箱验证码" size="large" allow-clear
                        class="code-input">
                    </a-input>
                    <a-button size="large" class="code-btn" :disabled="sendingCode || countdown > 0"
                        :loading="sendingCode" @click="handleSendCode">
                        {{ countdown > 0 ? `${countdown}s 后重试` : '发送验证码' }}
                    </a-button>
                </div>
            </a-form-item>

            <a-form-item label="新密码" name="newPassword">
                <a-input-password v-model:value="formState.newPassword" placeholder="6-20 位新密码" size="large"
                    allow-clear>
                    <template #prefix>
                        <LockOutlined />
                    </template>
                </a-input-password>
            </a-form-item>

            <a-form-item label="确认新密码" name="checkPassword">
                <a-input-password v-model:value="formState.checkPassword" placeholder="请再次输入新密码" size="large"
                    allow-clear>
                    <template #prefix>
                        <SafetyOutlined />
                    </template>
                </a-input-password>
            </a-form-item>

            <a-form-item>
                <a-button type="primary" html-type="submit" size="large" block :loading="loading"
                    class="auth-submit-btn">
                    重置密码
                </a-button>
            </a-form-item>

            <div class="auth-form-footer">
                想起密码了？
                <router-link to="/login" class="auth-link-primary">返回登录</router-link>
            </div>
        </a-form>
    </AuthLayout>
</template>

<!--
  ForgotPassword：前台忘记密码页
  - 复用 AuthLayout 外壳，交互与 Register 一致
  - 通过注册邮箱接收验证码，校验后重置密码，60 秒重发倒计时
  - 重置成功后跳回登录页
-->
<script setup lang="ts">
import { reactive, ref, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import {
    LockOutlined,
    MailOutlined,
    SafetyOutlined,
} from '@ant-design/icons-vue';
import type { Rule } from 'ant-design-vue/es/form';
import { sendResetCode, resetPassword } from '@/api/userController';
import AuthLayout from '@/components/AuthLayout.vue';

const router = useRouter();
const loading = ref(false);
// 验证码发送状态：发送中 + 重发倒计时
const sendingCode = ref(false);
const countdown = ref(0);
let countdownTimer: ReturnType<typeof setInterval> | null = null;

// 重置密码表单数据
const formState = reactive({
    email: '',
    code: '',
    newPassword: '',
    checkPassword: '',
});

// 确认密码校验：必须与新密码一致
const validateCheckPassword = async (_rule: Rule, value: string) => {
    if (!value) {
        return Promise.reject('请再次输入新密码');
    }
    if (value !== formState.newPassword) {
        return Promise.reject('两次输入的密码不一致');
    }
    return Promise.resolve();
};

// 表单校验规则
const rules: Record<string, Rule[]> = {
    email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' },
    ],
    code: [
        { required: true, message: '请输入邮箱验证码', trigger: 'blur' },
        { len: 6, message: '验证码为 6 位数字', trigger: 'blur' },
    ],
    newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度需在 6-20 个字符之间', trigger: 'blur' },
    ],
    checkPassword: [
        { required: true, validator: validateCheckPassword, trigger: 'blur' },
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
        const res = await sendResetCode({ email });
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

// 重置密码逻辑：成功后跳转到登录页
async function handleReset() {
    loading.value = true;
    try {
        const res = await resetPassword(formState);
        if (res.data?.data) {
            message.success('密码重置成功，请使用新密码登录');
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
    background: rgba(var(--accent-rgb), 0.15);
    border: 1px solid rgba(var(--accent-rgb), 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 14px;
    color: var(--accent);
    backdrop-filter: blur(6px);
    box-shadow: 0 0 16px rgba(var(--accent-rgb), 0.3);
}

.step-text {
    font-size: 14px;
    color: var(--text-secondary);
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
    background: rgba(var(--accent-rgb), 0.12) !important;
    border-color: rgba(var(--accent-rgb), 0.4) !important;
    color: var(--accent) !important;
}

.code-btn:hover:not(:disabled) {
    background: rgba(var(--accent-rgb), 0.2) !important;
    color: #fff !important;
}

:deep(.ant-form-item) {
    margin-bottom: 18px;
}

:deep(.form-header) {
    margin-bottom: 24px;
}
</style>
