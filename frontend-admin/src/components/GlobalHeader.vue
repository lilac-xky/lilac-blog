<template>
    <div class="global-header">
        <div class="header-left">
            <h1 class="page-title">{{ pageTitle }}</h1>
            <p class="page-subtitle">{{ pageSubtitle }}</p>
        </div>
        <div class="header-right">
            <a-badge :count="0" :dot="true">
                <a-button shape="circle" class="icon-btn">
                    <template #icon>
                        <BellOutlined />
                    </template>
                </a-button>
            </a-badge>
            <a-dropdown placement="bottomRight">
                <div class="user-chip">
                    <a-avatar :size="32" :src="userStore.loginUser?.avatar || undefined" class="user-avatar">
                        <template #icon>
                            <UserOutlined />
                        </template>
                    </a-avatar>
                    <span class="user-name">{{ userStore.loginUser?.username || 'Admin' }}</span>
                </div>
                <template #overlay>
                    <a-menu @click="handleMenuClick">
                        <a-menu-item key="profile">
                            <UserOutlined /> 个人中心
                        </a-menu-item>
                        <a-menu-item key="settings">
                            <SettingOutlined /> 设置
                        </a-menu-item>
                        <a-menu-divider />
                        <a-menu-item key="logout">
                            <LogoutOutlined /> 退出登录
                        </a-menu-item>
                    </a-menu>
                </template>
            </a-dropdown>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message, Modal } from 'ant-design-vue';
import {
    BellOutlined,
    UserOutlined,
    SettingOutlined,
    LogoutOutlined,
} from '@ant-design/icons-vue';
import { logout } from '@/api/adminController';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 页面标题和副标题映射
const titleMap: Record<string, { title: string; subtitle: string }> = {
    '/': { title: '仪表盘', subtitle: '欢迎回到 lilac-blog 后台管理系统' },
    '/write-blog': { title: '写 Blog', subtitle: '创作一篇新的博客文章' },
    '/blog/category': { title: '分类管理', subtitle: '管理你的博客分类' },
    '/blog/tag': { title: '标签管理', subtitle: '管理你的博客标签' },
    '/about': { title: '关于我', subtitle: '维护个人介绍信息' },
    '/message': { title: '留言管理', subtitle: '查看和回复读者留言' },
};

const pageTitle = computed(() => titleMap[route.path]?.title ?? '仪表盘');
const pageSubtitle = computed(() => titleMap[route.path]?.subtitle ?? '');

// 处理菜单点击事件
async function handleMenuClick({ key }: { key: string }) {
    if (key === 'logout') {
        Modal.confirm({
            title: '确认退出登录？',
            okText: '退出',
            cancelText: '取消',
            onOk: async () => {
                try {
                    await logout({ silentError: true });
                } catch {
                    // ignore, still clear local state
                } finally {
                    userStore.clearLoginUser();
                    message.success('已退出登录');
                    router.push('/login');
                }
            },
        });
    }
}
</script>

<style scoped>
.global-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
}

.header-left {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
}

.page-title {
    font-size: 24px;
    font-weight: 700;
    color: var(--text-primary);
    line-height: 1.2;
    margin: 0;
}

.page-subtitle {
    font-size: 13px;
    color: var(--text-secondary);
    margin: 0;
    line-height: 1.2;
}

.header-right {
    display: flex;
    align-items: center;
    gap: 12px;
}

.icon-btn {
    background: #fff !important;
    border: 1px solid var(--border) !important;
    box-shadow: var(--shadow-card);
    color: var(--text-secondary) !important;
}

.icon-btn:hover {
    color: var(--primary) !important;
    border-color: var(--primary-soft) !important;
    transform: translateY(-1px);
}

.user-chip {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 4px 12px 4px 4px;
    background: #fff;
    border-radius: var(--radius-pill);
    cursor: pointer;
    box-shadow: var(--shadow-card);
    transition: all 0.2s ease;
}

.user-chip:hover {
    transform: translateY(-1px);
    box-shadow: var(--shadow-hover);
}

.user-name {
    font-size: 13px;
    font-weight: 500;
    color: var(--text-primary);
}
</style>
