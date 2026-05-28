<template>
    <div class="blog-shell">
        <!-- 全局星空背景 -->
        <StarrySky />

        <!-- 顶部导航栏 -->
        <BlogHeader />

        <!-- 主内容区 -->
        <main class="blog-content">
            <router-view />
        </main>

        <!-- 页脚 -->
        <BlogFooter />
    </div>
</template>

<!--
  BasicLayout：前台主布局壳
  星空背景 + 顶部 Header + 主内容 + 页脚，router-view 渲染各页面。
  Header/Footer 单独拆出便于其它无 Header 场景（如登录页）单独引用。
-->
<script setup lang="ts">
import StarrySky from '@/components/StarrySky.vue';
import BlogHeader from './BlogHeader.vue';
import BlogFooter from './BlogFooter.vue';
</script>

<style scoped>
.blog-shell {
    position: relative;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    z-index: 1;
}

.blog-content {
    position: relative;
    flex: 1;
    width: 100%;
    max-width: 1100px;
    margin: 0 auto;
    padding: 36px 28px 60px;
}

@media (max-width: 720px) {
    .blog-content {
        padding: 24px 16px 48px;
    }
}
</style>
