<template>
    <div class="login-stage">
        <div class="login-card">
            <!-- 左侧：Hero 轮播 -->
            <div class="login-hero" @mouseenter="stopAuto" @mouseleave="startAuto">
                <div class="hero-brand">
                    <div class="brand-mark">L</div>
                    lilac-blog
                </div>

                <div class="hero-slider">
                    <div v-for="(slide, i) in slides" :key="i" class="hero-slide" :class="{ active: current === i }">
                        <div>
                            <div class="slide-ico">{{ slide.ico }}</div>
                            <div class="slide-eyebrow">{{ slide.eyebrow }}</div>
                            <div class="slide-title" v-html="slide.title"></div>
                            <div class="slide-sub">{{ slide.sub }}</div>
                            <div class="slide-features">
                                <div v-for="(f, fi) in slide.features" :key="fi" class="slide-feature">
                                    <span class="d"></span>{{ f }}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="hero-controls">
                    <div class="slide-dots">
                        <button v-for="(s, i) in slides" :key="i" class="slide-dot" :class="{ active: current === i }"
                            @click="goSlide(i)" :aria-label="`slide ${i + 1}`"></button>
                    </div>
                    <div class="slide-counter">
                        <strong>{{ String(current + 1).padStart(2, '0') }}</strong> / {{
                            String(slides.length).padStart(2, '0') }}
                    </div>
                </div>
            </div>

            <!-- 右侧：表单 -->
            <div class="login-form-wrap">
                <div class="login-form">
                    <div class="form-head">
                        <h3>欢迎回来</h3>
                        <p class="sub">请输入管理员账号密码进入后台</p>
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
                    </a-form>

                    <div class="login-foot">仅限管理员访问 · 首次使用请联系站长</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue';
import type { Rule } from 'ant-design-vue/es/form';
import { login } from '@/api/adminController';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const loading = ref(false);
const remember = ref(true);

// 登录表单状态
const formState = reactive<API.UserLoginRequest>({
    userAccount: '',
    password: '',
});

// 表单验证规则
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

// 登录逻辑
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

// ============ Hero 轮播 ============
const slides = [
    {
        ico: '✍',
        eyebrow: 'FOCUS WRITING',
        title: '专注书写<br/>让每一次思考落地',
        sub: '一个安静的后台，没有多余的噪音。所有精力，都留给文字本身。',
        features: ['富文本 · Markdown 双模式', '草稿自动保存，不丢一字', '沉浸式写作界面'],
    },
    {
        ico: '▦',
        eyebrow: 'WELL ORGANIZED',
        title: '井然有序<br/>内容各归其位',
        sub: '系统化管理你的每一篇作品。分类、标签、索引 —— 让思想库灵活生长。',
        features: ['多级分类 · 树形结构', '灵活标签 · 快速检索', '全文搜索 · 毫秒级响应'],
    },
    {
        ico: '◈',
        eyebrow: 'CONNECT',
        title: '与读者共鸣<br/>文字尽头是相遇',
        sub: '留言互动、访问数据一目了然。每一次阅读，都是跨越时空的对话。',
        features: ['留言管理 · 及时回复', '访问统计 · 趋势洞察', '读者画像 · 精准触达'],
    },
];
const current = ref(0);
let slideTimer: ReturnType<typeof setInterval> | null = null;

function goSlide(i: number) {
    current.value = (i + slides.length) % slides.length;
    startAuto();
}

function startAuto() {
    stopAuto();
    slideTimer = setInterval(() => {
        current.value = (current.value + 1) % slides.length;
    }, 5000);
}

function stopAuto() {
    if (slideTimer) {
        clearInterval(slideTimer);
        slideTimer = null;
    }
}

onMounted(startAuto);
onUnmounted(stopAuto);
</script>

<style scoped>
.login-stage {
    height: 100vh;
    width: 100vw;
    padding: 40px 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    background:
        radial-gradient(circle at 20% 30%, rgba(196, 160, 98, 0.08), transparent 45%),
        radial-gradient(circle at 80% 70%, rgba(26, 35, 50, 0.06), transparent 45%),
        var(--bg-page);
    overflow: auto;
}

.login-card {
    width: 100%;
    max-width: 960px;
    background: var(--bg-card);
    border: 1px solid var(--border);
    border-radius: 20px;
    overflow: hidden;
    box-shadow: 0 24px 60px rgba(26, 35, 50, 0.16), 0 8px 20px rgba(26, 35, 50, 0.06);
    display: grid;
    grid-template-columns: 1.15fr 1fr;
    min-height: 540px;
}

/* ============ 左侧 Hero 轮播 ============ */
.login-hero {
    background: linear-gradient(135deg, var(--bg-sider) 0%, var(--bg-sider-light) 100%);
    color: #fff;
    position: relative;
    overflow: hidden;
    min-height: 540px;
}

.login-hero::before {
    content: '';
    position: absolute;
    width: 500px;
    height: 500px;
    border-radius: 50%;
    border: 1px solid rgba(196, 160, 98, 0.08);
    top: 30%;
    left: -20%;
    box-shadow:
        0 0 0 40px rgba(196, 160, 98, 0.04),
        0 0 0 80px rgba(196, 160, 98, 0.02);
    pointer-events: none;
}

.login-hero::after {
    content: '';
    position: absolute;
    width: 300px;
    height: 300px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(196, 160, 98, 0.18), transparent 65%);
    top: -80px;
    right: -80px;
    pointer-events: none;
}

.hero-brand {
    position: absolute;
    top: 36px;
    left: 40px;
    z-index: 3;
    display: flex;
    align-items: center;
    gap: 10px;
    color: #fff;
    font-weight: 600;
    font-size: 15px;
    letter-spacing: 0.5px;
}

.brand-mark {
    width: 34px;
    height: 34px;
    border-radius: 8px;
    background: var(--primary);
    color: var(--bg-sider);
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 800;
    font-size: 15px;
    letter-spacing: -0.5px;
    box-shadow: 0 4px 12px rgba(196, 160, 98, 0.3);
}

.hero-slider {
    position: relative;
    width: 100%;
    height: 100%;
    padding: 120px 48px 90px;
    display: flex;
    align-items: center;
}

.hero-slide {
    position: absolute;
    inset: 120px 48px 90px;
    opacity: 0;
    transform: translateX(20px);
    transition: opacity 0.5s ease, transform 0.5s ease;
    pointer-events: none;
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.hero-slide.active {
    opacity: 1;
    transform: translateX(0);
    pointer-events: auto;
}

.slide-ico {
    width: 52px;
    height: 52px;
    border-radius: 12px;
    background: rgba(196, 160, 98, 0.15);
    border: 1px solid rgba(196, 160, 98, 0.3);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    margin-bottom: 22px;
    backdrop-filter: blur(8px);
}

.slide-eyebrow {
    font-size: 11px;
    color: var(--primary);
    letter-spacing: 4px;
    text-transform: uppercase;
    font-weight: 700;
    margin-bottom: 10px;
}

.slide-title {
    font-size: 34px;
    font-weight: 800;
    line-height: 1.2;
    color: #fff;
    letter-spacing: -0.5px;
}

.slide-sub {
    font-size: 15px;
    color: rgba(255, 255, 255, 0.6);
    margin-top: 10px;
    line-height: 1.7;
}

.slide-features {
    margin-top: 24px;
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.slide-feature {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 13px;
    color: rgba(255, 255, 255, 0.78);
}

.slide-feature .d {
    width: 5px;
    height: 5px;
    border-radius: 50%;
    background: var(--primary);
}

.hero-controls {
    position: absolute;
    bottom: 32px;
    left: 48px;
    right: 48px;
    z-index: 3;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.slide-dots {
    display: flex;
    gap: 6px;
}

.slide-dot {
    width: 24px;
    height: 3px;
    border-radius: 2px;
    background: rgba(255, 255, 255, 0.2);
    border: 0;
    cursor: pointer;
    padding: 0;
    transition: 0.3s;
}

.slide-dot:hover {
    background: rgba(255, 255, 255, 0.4);
}

.slide-dot.active {
    background: var(--primary);
    width: 36px;
}

.slide-counter {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.5);
    font-family: 'JetBrains Mono', 'Courier New', monospace;
    letter-spacing: 1px;
}

.slide-counter strong {
    color: var(--primary);
    font-weight: 700;
}

/* ============ 右侧表单 ============ */
.login-form-wrap {
    padding: 56px 48px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    background: var(--bg-card);
    overflow-y: auto;
}

.login-form {
    max-width: 340px;
    margin: 0 auto;
    width: 100%;
}

.form-head {
    margin-bottom: 26px;
}

.form-head h3 {
    font-size: 24px;
    font-weight: 700;
    color: var(--text-primary);
    margin: 0 0 6px;
}

.form-head .sub {
    color: var(--text-secondary);
    font-size: 13px;
    margin: 0;
}

.form-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin: 6px 0 20px;
    font-size: 13px;
    color: var(--text-secondary);
}

.link-forgot {
    color: var(--primary-hover);
    cursor: pointer;
    font-weight: 500;
    transition: color 0.2s;
}

.link-forgot:hover {
    color: var(--primary);
}

.submit-btn {
    height: 46px !important;
    border-radius: var(--radius-sm) !important;
    font-size: 15px;
    font-weight: 600;
    letter-spacing: 2px;
}

.login-foot {
    margin-top: 20px;
    padding-top: 18px;
    border-top: 1px solid var(--border-soft);
    text-align: center;
    font-size: 12px;
    color: var(--text-muted);
}

:deep(.ant-form-item-label > label) {
    font-weight: 500;
    color: var(--text-primary);
}

/* ===== 响应式：窄屏隐藏 Hero ===== */
@media (max-width: 900px) {
    .login-card {
        grid-template-columns: 1fr;
        max-width: 420px;
    }

    .login-hero {
        display: none;
    }

    .login-form-wrap {
        padding: 40px 32px;
    }
}
</style>
