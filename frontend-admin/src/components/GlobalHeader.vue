<template>
    <div class="global-header">
        <div class="header-left">
            <h1 class="page-title">{{ pageTitle }}</h1>
            <p class="page-subtitle">{{ pageSubtitle }}</p>
        </div>
        <div class="header-right">
            <a-input-search placeholder="搜索..." class="header-search" />
            <a-badge :count="0" :dot="true">
                <a-button shape="circle" class="icon-btn">
                    <template #icon>
                        <BellOutlined />
                    </template>
                </a-button>
            </a-badge>
            <a-dropdown placement="bottomRight">
                <div class="user-chip">
                    <a-avatar :size="32" src="https://zos.alipayobjects.com/rmsportal/ODTLcjxAfvqbxHnVXCYX.png" />
                    <span class="user-name">Admin</span>
                </div>
                <template #overlay>
                    <a-menu>
                        <a-menu-item key="1">
                            <UserOutlined /> 个人中心
                        </a-menu-item>
                        <a-menu-item key="2">
                            <SettingOutlined /> 设置
                        </a-menu-item>
                        <a-menu-divider />
                        <a-menu-item key="3">
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
import { useRoute } from 'vue-router';
import {
    BellOutlined,
    UserOutlined,
    SettingOutlined,
    LogoutOutlined,
} from '@ant-design/icons-vue';

const route = useRoute();

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

.header-search {
    width: 220px;
}

.icon-btn {
    background: #fff !important;
    border: none !important;
    box-shadow: var(--shadow-card);
    color: var(--text-primary) !important;
}

.icon-btn:hover {
    color: var(--text-primary) !important;
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
