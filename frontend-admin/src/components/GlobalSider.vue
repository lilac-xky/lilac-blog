<template>
    <div id="global-sider">
        <div class="brand">
            <img src="../assets/logo.png" alt="logo" class="brand-logo" />
            <span class="brand-name">lilac-blog</span>
        </div>
        <a-menu class="sider-menu" mode="inline" theme="dark" :items="fixedMenuItems" :selected-keys="selectedKeys"
            :open-keys="openKeys" @update:openKeys="(k: string[]) => openKeys = k" @click="handleMenuClick" />
    </div>
</template>

<script setup lang="ts">
import { h, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
    HomeOutlined,
    EditOutlined,
    AppstoreOutlined,
    FolderOutlined,
    TagsOutlined,
    ProfileOutlined,
    UserOutlined,
    MessageOutlined,
} from '@ant-design/icons-vue';

const route = useRoute();
const router = useRouter();

const selectedKeys = ref<string[]>([route.path]);
const openKeys = ref<string[]>(['/blog', '/about']);

watch(
    () => route.path,
    (path) => {
        selectedKeys.value = [path];
    }
);

// 菜单点击处理函数
const handleMenuClick = ({ key }: { key: string }) => {
    if (key && key !== route.path) {
        router.push(key);
    }
};

// 固定菜单项
const fixedMenuItems = [
    {
        key: '/',
        icon: () => h(HomeOutlined),
        label: '首页',
    },
    {
        key: '/write-blog',
        icon: () => h(EditOutlined),
        label: '写blog',
    },
    {
        key: '/blog',
        icon: () => h(AppstoreOutlined),
        label: '系统管理',
        children: [
            {
                key: '/blog/category',
                icon: () => h(FolderOutlined),
                label: '分类管理',
            },
            {
                key: '/blog/tag',
                icon: () => h(TagsOutlined),
                label: '标签管理',
            },
        ]
    },
    {
        key: '/about',
        icon: () => h(ProfileOutlined),
        label: '内容管理',
        children: [
            {
                key: '/about',
                icon: () => h(UserOutlined),
                label: '关于我',
            },
            {
                key: '/message',
                icon: () => h(MessageOutlined),
                label: '留言管理',
            },
        ]
    },
];
</script>

<style scoped>
#global-sider {
    height: 100%;
    display: flex;
    flex-direction: column;
}

.brand {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 4px 12px 20px 12px;
}

.brand-logo {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    object-fit: cover;
}

.brand-name {
    color: #fff;
    font-size: 18px;
    font-weight: 700;
    letter-spacing: 0.3px;
}

.sider-menu {
    background: transparent !important;
    flex: 1;
    font-size: 14px;
}

:deep(.ant-menu-dark),
:deep(.ant-menu-dark .ant-menu-sub) {
    background: transparent !important;
}

:deep(.ant-menu-dark .ant-menu-item),
:deep(.ant-menu-dark .ant-menu-submenu-title) {
    margin-block: 4px;
    margin-inline: 0;
    width: 100%;
    border-radius: 8px;
    color: rgba(255, 255, 255, 0.72);
    height: 44px;
    line-height: 44px;
    transition: all 0.2s ease;
    position: relative;
}

:deep(.ant-menu-dark .ant-menu-item:hover),
:deep(.ant-menu-dark .ant-menu-submenu-title:hover) {
    background: rgba(255, 255, 255, 0.04) !important;
    color: #fff !important;
}

:deep(.ant-menu-dark .ant-menu-item-selected) {
    background: var(--bg-sider-active) !important;
    color: #fff !important;
    font-weight: 500;
}

/* 激活态左侧 3px 琥珀金竖条 */
:deep(.ant-menu-dark .ant-menu-item-selected)::before {
    content: '';
    position: absolute;
    left: 0;
    top: 8px;
    bottom: 8px;
    width: 3px;
    background: var(--sider-active-bar);
    border-radius: 0 2px 2px 0;
}

:deep(.ant-menu-dark.ant-menu-inline .ant-menu-sub.ant-menu-inline) {
    background: transparent !important;
}

:deep(.ant-menu-dark .ant-menu-sub .ant-menu-item) {
    padding-left: 44px !important;
}
</style>
