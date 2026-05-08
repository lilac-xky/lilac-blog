<template>
    <router-link :to="`/article/${article.id}`" class="article-card">
        <!-- 封面：无封面则展示占位图标，置顶文章额外渲染角标 -->
        <div class="cover" :class="{ placeholder: !article.coverUrl }">
            <img v-if="article.coverUrl" :src="article.coverUrl" :alt="article.title" loading="lazy" />
            <div v-else class="ph-emoji">✦</div>
            <span v-if="article.isTop === 1" class="top-badge">置顶</span>
        </div>

        <!-- 文章主体：标题、摘要、元信息、标签 -->
        <div class="body">
            <h3 class="title">{{ article.title }}</h3>
            <p v-if="article.summary" class="summary">{{ article.summary }}</p>
            <div class="meta">
                <span v-if="article.createTime" class="meta-item">
                    <ClockCircleOutlined />
                    {{ formatDate(article.createTime) }}
                </span>
                <span v-if="article.categoryName" class="meta-item meta-category">
                    {{ article.categoryName }}
                </span>
                <span v-if="article.viewCount != null" class="meta-item">
                    <EyeOutlined />
                    {{ article.viewCount }}
                </span>
            </div>
            <div v-if="validTags.length" class="tags">
                <span v-for="tag in validTags" :key="tag.id" class="tag-pill">
                    #{{ tag.tagName }}
                </span>
            </div>
        </div>
    </router-link>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ClockCircleOutlined, EyeOutlined } from '@ant-design/icons-vue';
import dayjs from 'dayjs';

const props = defineProps<{ article: API.ArticleVO }>();

// 过滤掉空标签，确保渲染时拥有有效 id
const validTags = computed(
    () => (props.article.tags ?? []).filter((t): t is API.TagVO => !!t && t.id != null),
);

// 格式化日期：YYYY-MM-DD
function formatDate(d?: string) {
    if (!d) return '';
    return dayjs(d).format('YYYY-MM-DD');
}
</script>

<style scoped>
.article-card {
    display: flex;
    gap: 14px;
    padding: 14px;
    border-radius: var(--radius-card);
    background: var(--bg-card);
    backdrop-filter: blur(var(--blur));
    border: 1px solid var(--border-soft);
    transition: transform 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease;
    color: var(--text-primary);
}

.article-card:hover {
    transform: translateY(-2px);
    border-color: rgba(56, 189, 248, 0.45);
    box-shadow: var(--shadow-hover);
}

.cover {
    position: relative;
    flex: 0 0 140px;
    height: 90px;
    border-radius: 12px;
    overflow: hidden;
    background: linear-gradient(135deg, #1e3a5f, #2a4a7a);
    display: flex;
    align-items: center;
    justify-content: center;
}

.cover img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.cover.placeholder {
    color: rgba(255, 255, 255, 0.35);
    font-size: 40px;
}

.top-badge {
    position: absolute;
    top: 8px;
    left: 8px;
    background: var(--accent);
    color: #fff;
    font-size: 11px;
    padding: 2px 10px;
    border-radius: var(--radius-pill);
    font-weight: 600;
}

.body {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 6px;
}

.title {
    font-size: 16px;
    font-weight: 700;
    color: var(--text-primary);
    text-decoration: underline;
    text-decoration-color: rgba(56, 189, 248, 0.5);
    text-underline-offset: 4px;
    text-decoration-thickness: 2px;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 1;
    line-clamp: 1;
    -webkit-box-orient: vertical;
}

.article-card:hover .title {
    text-decoration-color: var(--accent);
}

.summary {
    color: var(--text-secondary);
    font-size: 12.5px;
    line-height: 1.6;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
}

.meta {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    color: var(--text-muted);
    font-size: 12px;
}

.meta-item {
    display: inline-flex;
    align-items: center;
    gap: 4px;
}

.meta-category {
    color: var(--accent);
}

.tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
}

.tag-pill {
    color: var(--text-muted);
    font-size: 12px;
}

@media (max-width: 640px) {
    .article-card {
        flex-direction: column;
    }

    .cover {
        flex: 0 0 auto;
        width: 100%;
        height: 140px;
    }
}
</style>
