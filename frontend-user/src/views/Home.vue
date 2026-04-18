<template>
    <div class="home">
        <section class="hero">
            <h1>欢迎来到 lilac-blog</h1>
            <p>记录生活，分享思考，连接更多读者。</p>
            <div v-if="!userStore.loginUser?.token" class="hero-actions">
                <router-link to="/register" class="cta-primary">立即注册</router-link>
                <router-link to="/login" class="cta-ghost">已有账号，登录</router-link>
            </div>
            <div v-else class="hero-actions">
                <span class="greet">你好，{{ userStore.loginUser.username || userStore.loginUser.userAccount }} 👋</span>
            </div>
        </section>

        <section class="placeholder">
            <a-card title="最新文章">
                <p class="muted">文章列表开发中……</p>
            </a-card>
        </section>
    </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();
</script>

<style scoped>
.home {
    display: flex;
    flex-direction: column;
    gap: 24px;
}

.hero {
    background: linear-gradient(135deg, #1a1a1a 0%, #3a3a3a 100%);
    color: #fff;
    padding: 48px 32px;
    border-radius: var(--radius-card);
    box-shadow: var(--shadow-card);
}

.hero h1 {
    font-size: 32px;
    font-weight: 800;
    margin-bottom: 12px;
}

.hero p {
    color: rgba(255, 255, 255, 0.75);
    font-size: 15px;
    margin-bottom: 24px;
}

.hero-actions {
    display: flex;
    gap: 12px;
    align-items: center;
}

.cta-primary,
.cta-ghost {
    display: inline-block;
    padding: 10px 22px;
    border-radius: var(--radius-pill);
    font-size: 14px;
    font-weight: 600;
    text-decoration: none;
    transition: all 0.2s;
}

.cta-primary {
    background: #fff;
    color: #1a1a1a;
}

.cta-primary:hover {
    background: #f0f0f0;
}

.cta-ghost {
    border: 1px solid rgba(255, 255, 255, 0.35);
    color: #fff;
}

.cta-ghost:hover {
    background: rgba(255, 255, 255, 0.08);
}

.greet {
    font-size: 15px;
    color: rgba(255, 255, 255, 0.85);
}

.muted {
    color: var(--text-muted);
    font-size: 14px;
}
</style>
