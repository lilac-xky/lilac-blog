<template>
    <a-layout class="blog-layout">
        <a-layout-header class="blog-header">
            <div class="header-inner">
                <router-link to="/" class="brand">
                    <img src="@/assets/logo.png" alt="logo" />
                    <span>lilac-blog</span>
                </router-link>
                <nav class="nav">
                    <router-link to="/" class="nav-item">首页</router-link>
                </nav>
                <div class="auth-area">
                    <template v-if="userStore.loginUser?.token">
                        <a-dropdown placement="bottomRight">
                            <div class="user-chip">
                                <a-avatar :size="32" :src="userStore.loginUser.avater || defaultAvatar" />
                                <span class="user-name">{{ userStore.loginUser.username || userStore.loginUser.userAccount }}</span>
                            </div>
                            <template #overlay>
                                <a-menu @click="handleMenu">
                                    <a-menu-item key="logout">
                                        <LogoutOutlined /> 退出登录
                                    </a-menu-item>
                                </a-menu>
                            </template>
                        </a-dropdown>
                    </template>
                    <template v-else>
                        <router-link to="/login" class="btn-ghost">登录</router-link>
                        <router-link to="/register" class="btn-primary">注册</router-link>
                    </template>
                </div>
            </div>
        </a-layout-header>
        <a-layout-content class="blog-content">
            <router-view />
        </a-layout-content>
        <a-layout-footer class="blog-footer">created by lilac</a-layout-footer>
    </a-layout>
</template>

<script setup lang="ts">
import { LogoutOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { useUserStore } from '@/stores/user';
import { logout } from '@/api/userController';

const userStore = useUserStore();
const defaultAvatar = 'https://zos.alipayobjects.com/rmsportal/ODTLcjxAfvqbxHnVXCYX.png';

async function handleMenu({ key }: { key: string }) {
    if (key === 'logout') {
        try {
            await logout();
        } catch {
            // ignore, still clear local state
        } finally {
            userStore.clearLoginUser();
            message.success('已退出登录');
        }
    }
}
</script>

<style scoped>
.blog-layout {
    min-height: 100vh;
    background: var(--bg-page);
}

.blog-header {
    background: #fff;
    padding: 0;
    height: 64px;
    line-height: 64px;
    box-shadow: var(--shadow-card);
    position: sticky;
    top: 0;
    z-index: 10;
}

.header-inner {
    max-width: 1180px;
    margin: 0 auto;
    padding: 0 24px;
    display: flex;
    align-items: center;
    gap: 32px;
}

.brand {
    display: flex;
    align-items: center;
    gap: 10px;
    color: var(--text-primary);
    font-weight: 700;
    font-size: 18px;
    text-decoration: none;
}

.brand img {
    width: 32px;
    height: 32px;
    border-radius: 8px;
}

.nav {
    flex: 1;
    display: flex;
    gap: 8px;
}

.nav-item {
    padding: 0 14px;
    color: var(--text-secondary);
    text-decoration: none;
    font-size: 14px;
    transition: color 0.2s;
}

.nav-item:hover,
.nav-item.router-link-exact-active {
    color: var(--text-primary);
}

.auth-area {
    display: flex;
    align-items: center;
    gap: 12px;
}

.btn-ghost,
.btn-primary {
    padding: 0 18px;
    height: 36px;
    line-height: 36px;
    border-radius: var(--radius-pill);
    font-size: 14px;
    font-weight: 500;
    text-decoration: none;
    transition: all 0.2s;
}

.btn-ghost {
    color: var(--text-primary);
}

.btn-ghost:hover {
    background: #f0f0f0;
}

.btn-primary {
    background: var(--text-primary);
    color: #fff;
}

.btn-primary:hover {
    background: #333;
    color: #fff;
}

.user-chip {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 4px 12px 4px 4px;
    background: #fafafa;
    border-radius: var(--radius-pill);
    cursor: pointer;
    line-height: 1;
}

.user-name {
    font-size: 13px;
    color: var(--text-primary);
    font-weight: 500;
}

.blog-content {
    max-width: 1180px;
    width: 100%;
    margin: 0 auto;
    padding: 32px 24px;
}

.blog-footer {
    background: transparent;
    text-align: center;
    color: var(--text-muted);
    padding: 16px;
    font-size: 12px;
}
</style>
