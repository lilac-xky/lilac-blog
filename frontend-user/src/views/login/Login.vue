<template>
    <AuthLayout hero-title="欢迎回来" form-title="登录账号" form-subtitle="请使用你的账号和密码登录">
        <template #hero-desc>
            登录后继续你的阅读之旅。<br />
            记录生活，分享思考，连接更多读者。
        </template>
        <template #hero-extras>
            <div class="hero-badges">
                <div class="badge"><span class="dot" style="background:#52c41a"></span>收藏文章</div>
                <div class="badge"><span class="dot" style="background:#1890ff"></span>订阅作者</div>
                <div class="badge"><span class="dot" style="background:#faad14"></span>留言互动</div>
            </div>
        </template>

        <a-form :model="formState" :rules="rules" layout="vertical" @finish="handleLogin">
            <a-form-item label="账号" name="account">
                <a-input v-model:value="formState.account" placeholder="请输入账号 / 邮箱" size="large" allow-clear>
                    <template #prefix>
                        <UserOutlined />
                    </template>
                </a-input>
            </a-form-item>

            <a-form-item label="密码" name="password">
                <a-input-password v-model:value="formState.password" placeholder="请输入密码" size="large" allow-clear>
                    <template #prefix>
                        <LockOutlined />
                    </template>
                </a-input-password>
            </a-form-item>

            <div class="form-row">
                <a-checkbox v-model:checked="remember">记住我</a-checkbox>
                <router-link to="/forgot-password" class="link-forgot">忘记密码？</router-link>
            </div>

            <a-form-item>
                <a-button type="primary" html-type="submit" size="large" block :loading="loading"
                    class="auth-submit-btn">
                    登 录
                </a-button>
            </a-form-item>

            <div class="auth-form-footer">
                还没有账号？
                <router-link to="/register" class="auth-link-primary">立即注册</router-link>
            </div>
        </a-form>
    </AuthLayout>
</template>

<!--
  Login：前台登录页
  - 复用 AuthLayout 外壳（左侧 hero + 右侧表单）
  - 登录成功后会回跳到 query.redirect 指定的来源路径，未指定则回首页
-->
<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue';
import type { Rule } from 'ant-design-vue/es/form';
import { login } from '@/api/userController';
import { useUserStore } from '@/stores/user';
import AuthLayout from '@/components/AuthLayout.vue';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const loading = ref(false);
const remember = ref(true);

// 登录表单状态
const formState = reactive<API.UserLoginRequest>({
    account: '',
    password: '',
});

// 表单验证规则
const rules: Record<string, Rule[]> = {
    account: [
        { required: true, message: '请输入账号或邮箱', trigger: 'blur' },
        { max: 50, message: '账号不能超过 50 个字符', trigger: 'blur' },
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度需在 6-20 个字符之间', trigger: 'blur' },
    ],
};

// 登录逻辑：成功后写入用户态并跳转回原始路由
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
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid var(--border-soft);
    border-radius: var(--radius-pill);
    font-size: 13px;
    color: var(--text-secondary);
    backdrop-filter: blur(8px);
}

.badge .dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    box-shadow: 0 0 8px currentColor;
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
    color: var(--accent);
}
</style>
