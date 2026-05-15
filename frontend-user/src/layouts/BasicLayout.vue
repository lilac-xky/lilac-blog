<template>
    <div class="blog-shell">
        <!-- 全局星空背景 -->
        <StarrySky />

        <!-- 顶部导航栏 -->
        <header class="blog-header">
            <div class="header-inner">
                <!-- 站点 Logo -->
                <router-link to="/" class="brand">
                    <img src="https://lilacs.oss-cn-beijing.aliyuncs.com/lilac-blog/avatar/2026/05/15/2026-05-15AokvgcjLCxWBMW9v.png?x-oss-process=image/resize,w_200"
                        alt="logo" />
                    <span>lilac-blog</span>
                </router-link>

                <!-- 主导航 -->
                <nav class="nav">
                    <router-link v-for="item in navItems" :key="item.path" :to="item.path" class="nav-item"
                        :class="{ active: isActive(item.path) }">
                        {{ item.label }}
                        <span class="dot"></span>
                    </router-link>
                </nav>

                <!-- 登录态：已登录显示头像下拉，未登录显示登录/注册按钮 -->
                <div class="auth-area">
                    <template v-if="userStore.loginUser?.token">
                        <a-dropdown placement="bottomRight">
                            <div class="user-chip">
                                <a-avatar :size="30" :src="userStore.loginUser.avatar || undefined">
                                    <template #icon>
                                        <UserOutlined />
                                    </template>
                                </a-avatar>
                                <span class="user-name">
                                    {{ userStore.loginUser.username || userStore.loginUser.userAccount }}
                                </span>
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
        </header>

        <!-- 主内容区 -->
        <main class="blog-content">
            <router-view />
        </main>

        <!-- 页脚 -->
        <footer class="blog-footer">
            <span>© {{ year }} lilac-blog · 在星海中记录代码与思考</span>
            <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener noreferrer" class="beian-link">
                蜀ICP备2025159235号-1
            </a>
        </footer>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { LogoutOutlined, UserOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { useUserStore } from '@/stores/user';
import { logout } from '@/api/userController';
import StarrySky from '@/components/StarrySky.vue';

const userStore = useUserStore();
const route = useRoute();
const year = new Date().getFullYear();

// 顶部导航项
const navItems = [
    { label: '首页', path: '/' },
    { label: '归档', path: '/archive' },
];

const currentPath = computed(() => route.path);

// 判断导航项是否处于激活态
function isActive(path: string) {
    if (path === '/') return currentPath.value === '/';
    return currentPath.value.startsWith(path);
}

// 用户菜单点击：处理退出登录
async function handleMenu({ key }: { key: string }) {
    if (key === 'logout') {
        try {
            await logout({ silentError: true });
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
.blog-shell {
    position: relative;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    z-index: 1;
}

.blog-header {
    position: sticky;
    top: 0;
    z-index: 50;
    background: rgba(20, 18, 40, 0.28);
    backdrop-filter: blur(22px);
    -webkit-backdrop-filter: blur(22px);
    border-bottom: 1px solid var(--border-soft);
}

.header-inner {
    max-width: 1280px;
    margin: 0 auto;
    padding: 0 32px;
    height: 68px;
    display: flex;
    align-items: center;
    gap: 32px;
}

.brand {
    display: flex;
    align-items: center;
    gap: 10px;
    font-weight: 700;
    font-size: 18px;
    color: var(--text-primary);
    letter-spacing: 0.02em;
}

.brand img {
    width: 32px;
    height: 32px;
    border-radius: 8px;
}

.nav {
    flex: 1;
    display: flex;
    justify-content: center;
    gap: 8px;
}

.nav-item {
    position: relative;
    padding: 8px 18px;
    color: var(--text-secondary);
    font-size: 14px;
    font-weight: 500;
    transition: color 0.2s;
}

.nav-item:hover {
    color: var(--text-primary);
}

.nav-item .dot {
    position: absolute;
    left: 50%;
    bottom: -4px;
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--accent);
    transform: translateX(-50%) scale(0);
    transition: transform 0.25s ease;
    box-shadow: 0 0 10px var(--accent);
}

.nav-item.active {
    color: #fff;
}

.nav-item.active .dot {
    transform: translateX(-50%) scale(1);
}

.auth-area {
    display: flex;
    align-items: center;
    gap: 10px;
}

.btn-ghost,
.btn-primary {
    padding: 0 18px;
    height: 36px;
    line-height: 36px;
    border-radius: var(--radius-pill);
    font-size: 13px;
    font-weight: 500;
    transition: all 0.2s;
    border: 1px solid transparent;
}

.btn-ghost {
    color: var(--text-secondary);
    border-color: var(--border-soft);
}

.btn-ghost:hover {
    color: #fff;
    background: rgba(255, 255, 255, 0.05);
    border-color: var(--border-strong);
}

.btn-primary {
    background: var(--accent);
    color: #fff;
}

.btn-primary:hover {
    background: var(--accent-strong);
    box-shadow: 0 6px 22px rgba(14, 165, 233, 0.4);
}

.user-chip {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 4px 12px 4px 4px;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid var(--border-soft);
    border-radius: var(--radius-pill);
    cursor: pointer;
    line-height: 1;
}

.user-chip:hover {
    border-color: rgba(56, 189, 248, 0.4);
}

.user-name {
    font-size: 13px;
    color: var(--text-primary);
    font-weight: 500;
}

.blog-content {
    position: relative;
    flex: 1;
    width: 100%;
    max-width: 1100px;
    margin: 0 auto;
    padding: 36px 28px 60px;
}

.blog-footer {
    position: relative;
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    align-items: center;
    gap: 6px 14px;
    text-align: center;
    color: var(--text-muted);
    padding: 24px 16px 32px;
    font-size: 12px;
    border-top: 1px solid var(--border-soft);
}

.beian-link {
    color: var(--text-muted);
    transition: color 0.2s;
}

.beian-link:hover {
    color: var(--accent);
}

@media (max-width: 720px) {
    .header-inner {
        padding: 0 16px;
        gap: 14px;
    }

    .brand span {
        display: none;
    }

    .nav {
        gap: 0;
    }

    .nav-item {
        padding: 8px 10px;
        font-size: 13px;
    }

    .blog-content {
        padding: 24px 16px 48px;
    }
}
</style>
