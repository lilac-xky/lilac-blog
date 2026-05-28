<template>
    <footer class="blog-footer">
        <span>© {{ year }} lilac-blog · 在星海中记录代码与思考</span>
        <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener noreferrer" class="beian-link">
            蜀ICP备2025159235号-1
        </a>
    </footer>
</template>

<!-- BlogFooter：前台站点页脚。展示版权年份（自动取当前年）与备案号 -->
<script setup lang="ts">
const year = new Date().getFullYear();
</script>

<style scoped>
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
</style>
