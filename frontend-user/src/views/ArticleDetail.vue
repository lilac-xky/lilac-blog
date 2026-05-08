<template>
    <div class="article-detail">
        <!-- 加载中骨架 -->
        <div v-if="loading" class="loading">
            <a-spin size="large" />
        </div>
        <!-- 正文区域 -->
        <div v-else-if="article" class="article-wrap">
            <!-- 顶部封面（可选） -->
            <div v-if="article.coverUrl" class="cover-banner">
                <img :src="article.coverUrl" :alt="article.title" />
                <div class="cover-mask"></div>
            </div>

            <!-- 标题 + 元信息 + 摘要 -->
            <header class="article-head glass-card">
                <h1>{{ article.title }}</h1>
                <div class="meta">
                    <span class="meta-item">
                        <ClockCircleOutlined />
                        {{ formatDate(article.createTime) }}
                    </span>
                    <span v-if="article.categoryName" class="meta-item meta-cat">
                        <FolderOpenOutlined />
                        {{ article.categoryName }}
                    </span>
                    <span v-for="t in validTags" :key="t.id" class="meta-item meta-tag">
                        #{{ t.tagName }}
                    </span>
                    <span class="meta-item">
                        <EyeOutlined />
                        {{ article.viewCount ?? 0 }} 次阅读
                    </span>
                </div>
                <p v-if="article.summary" class="summary">{{ article.summary }}</p>
            </header>

            <!-- Markdown 正文 -->
            <div class="md-body">
                <MdPreview :model-value="article.content || ''" theme="dark" preview-theme="vuepress"
                    code-theme="atom" :show-code-row-number="false" />
            </div>
        </div>
        <!-- 文章不存在或被删除 -->
        <div v-else class="not-found glass-card">
            <FileSearchOutlined />
            <p>文章不存在或已被删除</p>
            <router-link to="/" class="back-home">← 返回首页</router-link>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import {
    ClockCircleOutlined,
    EyeOutlined,
    FileSearchOutlined,
    FolderOpenOutlined,
} from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import { MdPreview } from 'md-editor-v3';
import 'md-editor-v3/lib/preview.css';
import { getArticle } from '@/api/articleController';

const route = useRoute();
const article = ref<API.ArticleVO | null>(null);
const loading = ref(true);

// 过滤掉无效标签
const validTags = computed(
    () => (article.value?.tags ?? []).filter((t): t is API.TagVO => !!t && t.id != null),
);

// 格式化日期：YYYY-MM-DD HH:mm
function formatDate(d?: string) {
    return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '';
}

// 根据路由参数加载文章详情
async function loadArticle() {
    loading.value = true;
    try {
        const raw = route.params.id;
        const id = Array.isArray(raw) ? raw[0] : raw;
        if (!id) {
            article.value = null;
            return;
        }
        const res = await getArticle({ id }, { silentError: true });
        article.value = res.data?.data ?? null;
    } catch {
        article.value = null;
    } finally {
        loading.value = false;
    }
}

onMounted(loadArticle);
// 路由 id 变化时重新加载（同页面切换不同文章）
watch(() => route.params.id, loadArticle);
</script>

<style scoped>
.article-detail {
    display: flex;
    flex-direction: column;
    gap: 22px;
}

.loading {
    text-align: center;
    padding: 80px 0;
}

.article-wrap {
    display: flex;
    flex-direction: column;
    gap: 22px;
}

.cover-banner {
    position: relative;
    height: 280px;
    border-radius: var(--radius-card);
    overflow: hidden;
    box-shadow: var(--shadow-card);
}

.cover-banner img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    filter: saturate(1.1);
}

.cover-mask {
    position: absolute;
    inset: 0;
    background: linear-gradient(180deg, transparent 30%, rgba(13, 10, 31, 0.6) 100%);
}

.article-head {
    padding: 30px 32px;
}

.article-head h1 {
    font-size: 32px;
    font-weight: 800;
    line-height: 1.4;
    background: linear-gradient(135deg, #f5f3ff, #7dd3fc);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    margin-bottom: 16px;
}

.meta {
    display: flex;
    flex-wrap: wrap;
    gap: 18px;
    color: var(--text-muted);
    font-size: 13px;
    margin-bottom: 16px;
}

.meta-item {
    display: inline-flex;
    align-items: center;
    gap: 5px;
}

.meta-cat {
    color: var(--accent);
}

.meta-tag {
    padding: 2px 10px;
    border-radius: var(--radius-pill);
    background: rgba(56, 189, 248, 0.12);
    border: 1px solid rgba(56, 189, 248, 0.3);
    color: var(--accent);
    font-size: 12px;
    line-height: 1.5;
}

.summary {
    color: var(--text-secondary);
    font-size: 14px;
    line-height: 1.8;
    padding-left: 14px;
    border-left: 3px solid var(--accent);
}

.md-body {
    padding: 32px 36px;
    border-radius: var(--radius-card);
    background: var(--bg-card);
    backdrop-filter: blur(var(--blur));
    border: 1px solid var(--border-soft);
    color: var(--text-primary);
    box-shadow: var(--shadow-card);
}

/* 让 md-editor-v3 的 preview 透明融入玻璃卡，padding 由外层 .md-body 负责 */
.md-body :deep(.md-editor),
.md-body :deep(.md-editor-dark),
.md-body :deep(.md-editor-preview-wrapper),
.md-body :deep(.md-editor-content) {
    background: transparent !important;
}

.md-body :deep(.md-editor-preview-wrapper) {
    padding: 0 !important;
}

.md-body :deep(.md-editor-preview) {
    background: transparent !important;
    color: var(--text-primary);
    font-size: 15px;
    line-height: 1.85;
}

.not-found {
    text-align: center;
    padding: 80px 12px;
    color: var(--text-muted);
}

.not-found :deep(.anticon) {
    font-size: 48px;
    margin-bottom: 12px;
}

.back-home {
    display: inline-block;
    margin-top: 14px;
    color: var(--accent);
}

@media (max-width: 720px) {
    .article-head {
        padding: 24px 20px;
    }

    .article-head h1 {
        font-size: 24px;
    }

    .md-body {
        padding: 22px 20px;
    }

    .cover-banner {
        height: 180px;
    }
}
</style>
