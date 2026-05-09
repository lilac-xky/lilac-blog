<template>
    <router-link :to="`/article/${article.id}`" class="article-card">
        <!-- 封面 -->
        <div class="cover" :class="{ placeholder: !article.coverUrl }">
            <img v-if="article.coverUrl" :src="article.coverUrl" :alt="article.title" loading="lazy" />
            <div v-else class="ph-emoji">✦</div>
            <span v-if="article.isTop === 1" class="top-badge">置顶</span>
        </div>

        <div class="card-body">
            <!-- 元信息：时间 / 分类 / 浏览量 -->
            <div class="meta-row">
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

            <!-- 标题 -->
            <h3 class="title">{{ article.title }}</h3>

            <!-- 标签 -->
            <div v-if="validTags.length" class="tags">
                <span v-for="tag in validTags" :key="tag.id" class="tag-pill">
                    #{{ tag.tagName }}
                </span>
            </div>

            <!-- 简介 -->
            <p v-if="article.summary" class="summary">{{ article.summary }}</p>
        </div>
    </router-link>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ClockCircleOutlined, EyeOutlined } from '@ant-design/icons-vue';
import dayjs from 'dayjs';

const props = defineProps<{ article: API.ArticleVO }>();

const validTags = computed(
    () => (props.article.tags ?? []).filter((t): t is API.TagVO => !!t && t.id != null),
);

function formatDate(d?: string) {
    if (!d) return '';
    return dayjs(d).format('YYYY-MM-DD HH:mm');
}
</script>

<style scoped>
.article-card {
    display: flex;
    flex-direction: column;
    border-radius: 16px;
    background: rgba(20, 18, 40, 0.28);
    backdrop-filter: blur(var(--blur));
    border: 1px solid rgba(255, 255, 255, 0.06);
    overflow: hidden;
    transition: transform 0.3s ease, border-color 0.3s ease, box-shadow 0.3s ease, background 0.3s ease;
    color: var(--text-primary);
    text-decoration: none;
}

.article-card:hover {
    transform: translateY(-3px);
    border-color: rgba(56, 189, 248, 0.45);
    background: rgba(20, 18, 40, 0.42);
    box-shadow: 0 10px 30px rgba(56, 189, 248, 0.18);
}

/* 封面：横向宽幅 16:9 */
.cover {
    position: relative;
    width: 100%;
    aspect-ratio: 16 / 9;
    background: linear-gradient(135deg, #1e3a5f, #2a4a7a);
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
}

.cover img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s ease;
}

.article-card:hover .cover img {
    transform: scale(1.05);
}

.cover.placeholder .ph-emoji {
    color: rgba(255, 255, 255, 0.32);
    font-size: 48px;
}

.top-badge {
    position: absolute;
    top: 8px;
    left: 8px;
    background: var(--accent);
    color: #fff;
    font-size: 11px;
    padding: 2px 9px;
    border-radius: var(--radius-pill);
    font-weight: 600;
    box-shadow: 0 2px 10px rgba(56, 189, 248, 0.4);
}

/* 卡片正文：留出统一内边距 */
.card-body {
    padding: 14px 16px 16px;
    display: flex;
    flex-direction: column;
    gap: 10px;
}

/* 元信息：纯文本，无深色背景，只靠 padding 与封面拉开距离 */
.meta-row {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 14px;
    color: var(--text-muted);
    font-size: 12px;
}

.meta-item {
    display: inline-flex;
    align-items: center;
    gap: 4px;
}

.meta-category {
    color: var(--accent-pink);
}

/* 标题 */
.title {
    font-size: 15px;
    font-weight: 700;
    color: var(--text-primary);
    line-height: 1.45;
    margin: 0;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
}

.article-card:hover .title {
    color: var(--accent);
}

/* 标签：小巧的紫蓝胶囊 */
.tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
}

.tag-pill {
    font-size: 11.5px;
    padding: 2px 10px;
    border-radius: var(--radius-pill);
    background: rgba(56, 189, 248, 0.1);
    border: 1px solid rgba(56, 189, 248, 0.22);
    color: var(--accent);
    transition: all 0.2s;
}

.tag-pill:hover {
    background: rgba(56, 189, 248, 0.2);
    border-color: rgba(56, 189, 248, 0.5);
}

/* 简介 */
.summary {
    color: var(--text-secondary);
    font-size: 12.5px;
    line-height: 1.65;
    margin: 0;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
}
</style>
