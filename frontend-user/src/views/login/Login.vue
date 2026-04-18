<template>
    <div class="auth-page">
        <!-- 左侧品牌区 -->
        <div class="auth-hero">
            <div class="hero-content">
                <div class="hero-logo">
                    <img src="@/assets/logo.png" alt="logo" />
                    <span>lilac-blog</span>
                </div>
                <h1 class="hero-title">欢迎回来</h1>
                <p class="hero-desc">
                    登录后继续你的阅读之旅。<br />
                    记录生活，分享思考，连接更多读者。
                </p>
                <div class="hero-badges">
                    <div class="badge"><span class="dot" style="background:#52c41a"></span>收藏文章</div>
                    <div class="badge"><span class="dot" style="background:#1890ff"></span>订阅作者</div>
                    <div class="badge"><span class="dot" style="background:#faad14"></span>留言互动</div>
                </div>
            </div>
            <div class="hero-deco hero-deco-1"></div>
            <div class="hero-deco hero-deco-2"></div>
        </div>

        <!-- 右侧表单区 -->
        <div class="auth-form-wrap">
            <div class="auth-form">
                <div class="form-header">
                    <h2>登录账号</h2>
                    <p>请使用你的账号和密码登录</p>
                </div>

                <a-form :model="formState" :rules="rules" layout="vertical" @finish="handleLogin">
                    <a-form-item label="账号" name="userAccount">
                        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" size="large" allow-clear>
                            <template #prefix>
                                <UserOutlined />
                            </template>
                        </a-input>
                    </a-form-item>

                    <a-form-item label="密码" name="password">
                        <a-input-password v-model:value="formState.password" placeholder="请输入密码" size="large"
                            allow-clear>
                            <template #prefix>
                                <LockOutlined />
                            </template>
                        </a-input-password>
                    </a-form-item>

                    <div class="form-row">
                        <a-checkbox v-model:checked="remember">记住我</a-checkbox>
                        <a class="link-forgot">忘记密码？</a>
                    </div>

                    <a-form-item>
                        <a-button type="primary" html-type="submit" size="large" block :loading="loading"
                            class="submit-btn">
                            登 录
                        </a-button>
                    </a-form-item>

                    <div class="form-footer">
                        还没有账号？
                        <router-link to="/register" class="link-primary">立即注册</router-link>
                    </div>
                </a-form>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue';
import type { Rule } from 'ant-design-vue/es/form';
import { login } from '@/api/userController';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const loading = ref(false);
const remember = ref(true);

const formState = reactive<API.UserLoginRequest>({
    userAccount: '',
    password: '',
});

const rules: Record<string, Rule[]> = {
    userAccount: [
        { required: true, message: '请输入账号', trigger: 'blur' },
        { max: 50, message: '账号不能超过 50 个字符', trigger: 'blur' },
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度需在 6-20 个字符之间', trigger: 'blur' },
    ],
};

async function handleLogin() {
    loading.value = true;
    try {
        const res = await login(formState);
        if (res.data?.data) {
            userStore.setLoginUser(res.data.data);
            message.success('登录成功');
            const redirect = route.query.redirect;
            router.replace(typeof redirect === 'string' ? redirect : '/');
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
    margin-bottom: 36px;
}

.hero-badges {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
}

.badge {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 6px 14px;
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: var(--radius-pill);
    font-size: 13px;
    color: rgba(255, 255, 255, 0.85);
    backdrop-filter: blur(6px);
}

.badge .dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
}

.hero-deco {
    position: absolute;
    border-radius: 50%;
    filter: blur(60px);
    opacity: 0.35;
}

.hero-deco-1 {
    width: 320px;
    height: 320px;
    background: #667eea;
    top: -80px;
    right: -80px;
}

.hero-deco-2 {
    width: 260px;
    height: 260px;
    background: #ec4899;
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
    margin-bottom: 32px;
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

.form-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    font-size: 13px;
}

.link-forgot {
    color: var(--text-secondary);
    cursor: pointer;
    transition: color 0.2s;
}

.link-forgot:hover {
    color: var(--text-primary);
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
    margin-left: 4px;
}

.link-primary:hover {
    text-decoration: underline;
}

:deep(.ant-input-affix-wrapper-lg) {
    padding-block: 8px;
}

:deep(.ant-form-item-label > label) {
    font-weight: 500;
    color: var(--text-primary);
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
