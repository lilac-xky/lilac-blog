<template>
    <div id="global-sider">
        <div class="brand">
            <img src="https://lilacs.oss-cn-beijing.aliyuncs.com/lilac-blog/avatar/2026/05/15/2026-05-15AokvgcjLCxWBMW9v.png?x-oss-process=image/resize,w_200"
                alt="logo" class="brand-logo" />
            <span class="brand-name">lilac-blog</span>
        </div>
        <a-menu class="sider-menu" mode="inline" theme="dark" :items="fixedMenuItems" :selected-keys="selectedKeys"
            :open-keys="openKeys" @update:openKeys="(k: string[]) => openKeys = k" @click="handleMenuClick" />
    </div>
</template>

<script setup lang="ts">
import { h, ref, watch, computed } from 'vue';
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
    TeamOutlined,
    FileTextOutlined,
    AuditOutlined,
} from '@ant-design/icons-vue';
import { hasPermission, hasRole } from '@/utils/permission';

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

// 菜单项配置接口
interface MenuItem {
    key: string;
    icon?: () => any;
    label: string;
    permission?: string; // 需要的权限标识
    role?: string; // 需要的角色标识
    children?: MenuItem[];
}

// 完整菜单配置（包含权限信息）
const menuConfig: MenuItem[] = [
    {
        key: '/',
        icon: () => h(HomeOutlined),
        label: '首页',
    },
    {
        key: '/write-blog',
        icon: () => h(EditOutlined),
        label: '写blog',
        permission: 'article:add', // 需要文章添加权限
    },
    {
        key: '/blog',
        icon: () => h(AppstoreOutlined),
        label: '系统管理',
        children: [
            {
                key: '/user/manage',
                icon: () => h(TeamOutlined),
                label: '用户管理',
                permission: 'user:list', // 需要用户列表权限
            },
            {
                key: '/role',
                icon: () => h(TeamOutlined),
                label: '角色管理',
                permission: 'role:list', // 需要角色列表权限
            },
            {
                key: '/permission',
                icon: () => h(MessageOutlined),
                label: '权限管理',
                permission: 'permission:list', // 需要权限列表权限
            },
            {
                key: '/blog/category',
                icon: () => h(FolderOutlined),
                label: '分类管理',
                permission: 'category:list', // 需要分类列表权限
            },
            {
                key: '/blog/tag',
                icon: () => h(TagsOutlined),
                label: '标签管理',
                permission: 'tag:list', // 需要标签列表权限
            },
        ]
    },
    {
        key: '/about',
        icon: () => h(ProfileOutlined),
        label: '内容管理',
        children: [
            {
                key: '/article/manage',
                icon: () => h(FileTextOutlined),
                label: '文章管理',
                permission: 'article:list', // 需要文章列表权限
            },
            {
                key: '/article/review',
                icon: () => h(AuditOutlined),
                label: '文章审核',
                permission: 'article:review', // 需要文章审核权限
            },
            {
                key: '/about',
                icon: () => h(UserOutlined),
                label: '关于我',
            },
            {
                key: '/message',
                icon: () => h(MessageOutlined),
                label: '留言管理',
                permission: 'message:list', // 需要留言列表权限
            },
        ]
    },
];

/**
 * 检查菜单项是否有权限访问
 * @param item 菜单项
 * @returns 是否有权限
 */
function hasMenuPermission(item: MenuItem): boolean {
    // 没有配置权限要求的菜单项，所有人可见
    if (!item.permission && !item.role) return true;

    // 检查权限标识（包含超级管理员 '*' 权限的处理）
    if (item.permission && !hasPermission(item.permission)) return false;

    // 检查角色标识
    if (item.role && !hasRole(item.role)) return false;

    return true;
}

/**
 * 过滤菜单项（根据权限）
 * @param items 菜单项列表
 * @returns 过滤后的菜单项列表
 */
function filterMenuByPermission(items: MenuItem[]): MenuItem[] {
    return items
        .filter(item => hasMenuPermission(item))
        .map(item => {
            // 如果有子菜单，递归过滤
            if (item.children && item.children.length > 0) {
                const filteredChildren = filterMenuByPermission(item.children);
                // 如果过滤后子菜单为空，则不显示父菜单
                if (filteredChildren.length === 0) return null;
                return { ...item, children: filteredChildren };
            }
            return item;
        })
        .filter((item): item is MenuItem => item !== null);
}

// 根据权限过滤后的菜单项（响应式）
const fixedMenuItems = computed(() => filterMenuByPermission(menuConfig));
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
