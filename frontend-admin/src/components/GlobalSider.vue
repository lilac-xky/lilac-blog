<template>
    <div id="global-sider">
        <a-layout-sider>
            <a-menu  mode="inline" :items="fixedMenuItems" />
        </a-layout-sider>
    </div>
</template>

<script setup lang="ts">

// 固定菜单项
const fixedMenuItems = [
    {
        key: '/',
        label: '首页',
    },
    {
        key: '/write-blog',
        label: '写blog',
    },
    {
        key: '/blog',
        label: '系统管理',
        children: [
            {
                key: '/blog/category',
                label: '分类管理',
            },
            {
                key: '/blog/tag',
                label: '标签管理',
            },
        ]
    },
    {
        key: '/about',
        label: '内容管理',
        children: [
            {
                key: '/about',
                label: '关于我',
            },
            {
                key: '/message',
                label: '留言管理',
            },
        ]
    },

];
</script>

<style scoped>
</style>