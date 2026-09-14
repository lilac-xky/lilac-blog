<template>
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
                    <!-- 写文章快捷入口 -->
                    <router-link to="/user/write" class="write-shortcut" title="写文章">
                        <EditOutlined />
                    </router-link>
                    <a-dropdown placement="bottomRight">
                        <div class="user-chip">
                            <a-badge :count="unreadCount" :offset="[-2, 0]" :overflow-count="99">
                                <a-avatar :size="30" :src="userStore.loginUser.avatar || undefined">
                                    <template #icon>
                                        <UserOutlined />
                                    </template>
                                </a-avatar>
                            </a-badge>
                            <span class="user-name">
                                {{ userStore.loginUser.username || userStore.loginUser.userAccount }}
                            </span>
                        </div>
                        <template #overlay>
                            <a-menu @click="handleMenu">
                                <a-menu-item key="center">
                                    <UserOutlined /> 用户中心
                                </a-menu-item>
                                <a-menu-item key="write">
                                    <EditOutlined /> 写文章
                                </a-menu-item>
                                <a-menu-item key="articles">
                                    <FileTextOutlined /> 我的文章
                                </a-menu-item>
                                <a-menu-item key="messages">
                                    <a-badge :count="unreadCount" :offset="[8, 0]" :overflow-count="99" dot
                                        v-if="unreadCount > 0">
                                        <MessageOutlined /> 消息中心
                                    </a-badge>
                                    <template v-else>
                                        <MessageOutlined /> 消息中心
                                    </template>
                                </a-menu-item>
                                <a-menu-divider />
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
</template>

<!--
  BlogHeader：前台顶部导航栏
  - 站点 Logo / 主导航 / 登录态区域三段
  - 已登录展示头像下拉菜单与未读消息红点
  - 写文章入口直接走快捷按钮，避免「点头像 → 写文章」两次点击
-->
<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
    LogoutOutlined,
    UserOutlined,
    EditOutlined,
    FileTextOutlined,
    MessageOutlined,
} from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { useUserStore } from '@/stores/user';
import { logout } from '@/api/userController';
import { usePollUnreadCount } from '@/composables/usePollUnreadCount';

const userStore = useUserStore();
const route = useRoute();
const router = useRouter();

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

// 未读消息计数（轮询 + 路由切换刷新 + 登录态变化刷新）
const isLoggedIn = computed(() => !!userStore.loginUser?.token);
const { unreadCount, stop } = usePollUnreadCount({ isLoggedIn });

// 用户菜单点击：logout 走 finally，无论接口成功失败都必须清前端状态
async function handleMenu({ key }: { key: string }) {
    if (key === 'logout') {
        try {
            await logout({ silentError: true });
        } catch {
            // ignore
        } finally {
            // 清空本地登录态 + 停止未读轮询，避免登出后仍在打接口
            userStore.clearLoginUser();
            unreadCount.value = 0;
            stop();
            message.success('已退出登录');
            router.push('/');
        }
    } else if (key === 'center') {
        router.push('/user');
    } else if (key === 'write') {
        router.push('/user/write');
    } else if (key === 'articles') {
        router.push('/user/articles');
    } else if (key === 'messages') {
        router.push('/user/messages');
    }
}
</script>

<style scoped>
/* 顶栏：浅色玻璃幔层，比页面背景略亮，避免旧版“黑条压顶”把首页切成两截 */
.blog-header {
    position: sticky;
    top: 0;
    z-index: var(--z-header);
    background: var(--bg-header-veil);
    backdrop-filter: blur(var(--blur)) saturate(1.2);
    -webkit-backdrop-filter: blur(var(--blur)) saturate(1.2);
    border-bottom: 1px solid rgba(255, 255, 255, 0.07);
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

/* 导航项：默认比正文稍暗，激活/悬停时提亮到接近纯白 */
.nav-item {
    position: relative;
    padding: 8px 18px;
    color: rgba(232, 234, 252, 0.72);
    font-size: 14px;
    font-weight: 500;
    transition: color 0.2s;
}

.nav-item:hover {
    color: #fff;
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
    box-shadow: 0 6px 22px rgba(var(--accent-strong-rgb), 0.4);
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

.write-shortcut {
    width: 36px;
    height: 36px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background: rgba(var(--accent-rgb), 0.12);
    color: var(--accent) !important;
    font-size: 16px;
    transition: all 0.2s;
}

.write-shortcut:hover {
    background: var(--accent);
    color: #fff !important;
    box-shadow: 0 0 14px rgba(var(--accent-rgb), 0.5);
}

.user-chip:hover {
    border-color: rgba(var(--accent-rgb), 0.4);
}

.user-name {
    font-size: 13px;
    color: var(--text-primary);
    font-weight: 500;
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
}
</style>
