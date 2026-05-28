<template>
    <AuthLayout hero-title="加入我们" form-title="创建账号" form-subtitle="只需几步，即可开始使用 lilac-blog" :show-deco1="false"
        form-padding="32px" class="register-page">
        <template #hero-desc>
            创建一个账号，打造属于你自己的博客空间。<br />
            在这里记录成长、分享见解、连接世界。
        </template>
        <template #hero-extras>
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
        </template>

        <a-form :model="formState" :rules="rules" layout="vertical" @finish="handleRegister">
            <a-form-item label="账号" name="userAccount">
                <a-input v-model:value="formState.userAccount" placeholder="请输入用户名" size="large" allow-clear>
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

            <a-form-item label="密码" name="password">
                <a-input-password v-model:value="formState.password" placeholder="6-20 位密码" size="large" allow-clear>
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
                    <a class="auth-link-primary">《服务协议》</a>
                    和
                    <a class="auth-link-primary">《隐私政策》</a>
                </a-checkbox>
            </a-form-item>

            <a-form-item>
                <a-button type="primary" html-type="submit" size="large" block :loading="loading"
                    class="auth-submit-btn">
                    注 册
                </a-button>
            </a-form-item>

            <div class="auth-form-footer">
                已有账号？
                <router-link to="/login" class="auth-link-primary">直接登录</router-link>
            </div>
        </a-form>
    </AuthLayout>
</template>

<!--
  Register：前台注册页
  - 复用 AuthLayout 外壳
  - 强制邮箱验证码注册，60 秒重发倒计时
  - 必须勾选服务协议方可提交（走自定义校验器）
-->
<script setup lang="ts">
import { reactive, ref, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import {
    UserOutlined,
    LockOutlined,
    MailOutlined,
    SafetyOutlined,
} from '@ant-design/icons-vue';
import type { Rule } from 'ant-design-vue/es/form';
import { register, sendRegisterCode } from '@/api/userController';
import AuthLayout from '@/components/AuthLayout.vue';

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

/* 注册表单字段较多，间距收紧；标题区间距同原设计 */
:deep(.ant-form-item) {
    margin-bottom: 18px;
}

:deep(.form-header) {
    margin-bottom: 24px;
}
</style>
