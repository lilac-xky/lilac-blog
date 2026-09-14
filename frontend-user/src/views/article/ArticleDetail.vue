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
                <MdPreview :editor-id="editorId" :md-heading-id="makeHeadingId" :model-value="article.content || ''"
                    theme="dark" preview-theme="vuepress" code-theme="atom" :show-code-row-number="false"
                    @on-get-catalog="onGetCatalog" />
            </div>
        </div>
        <!-- 文章不存在或被删除 -->
        <div v-else class="not-found glass-card">
            <FileSearchOutlined />
            <p>文章不存在或已被删除</p>
            <router-link to="/" class="back-home">← 返回首页</router-link>
        </div>

        <!-- 左侧目录 + 右下角回到顶部 -->
        <ArticleToc v-if="article && headings.length" :headings="headings" :make-id="makeHeadingId" />
        <BackToTop />
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
import type { HeadList, MdHeadingId } from 'md-editor-v3';
import 'md-editor-v3/lib/preview.css';
import { getArticle } from '@/api/articleController';
import ArticleToc from '@/components/ArticleToc.vue';
import BackToTop from '@/components/BackToTop.vue';

const route = useRoute();
const article = ref<API.ArticleVO | null>(null);
const loading = ref(true);

// Markdown 目录：抽取 h1~h6 用于左侧大纲
const editorId = 'article-md';
const headings = ref<HeadList[]>([]);
const makeHeadingId: MdHeadingId = ({ index }) => `${editorId}-h-${index}`;
function onGetCatalog(list: HeadList[]) {
    headings.value = list;
}

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
    headings.value = [];
    try {
        const raw = route.params.id;
        const id = Array.isArray(raw) ? raw[0] : raw;
        if (!id) {
            article.value = null;
            return;
        }
        const res = await getArticle({ id: id as unknown as number }, { silentError: true });
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
    background: rgba(var(--accent-rgb), 0.12);
    border: 1px solid rgba(var(--accent-rgb), 0.3);
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

    /* ===== 借 md-editor-v3 自己的主题变量改配色，比用 !important 硬压更稳 ===== */
    /* 表格：默认偶数行是 #0c0c0c 近黑条纹，在玻璃卡上像一块黑砖，改成极淡的白色 */
    --md-theme-table-stripe-color: rgba(255, 255, 255, 0.03);
    --md-theme-table-tr-bg-color: transparent;
    --md-theme-table-td-border-color: rgba(255, 255, 255, 0.1);
    /* 代码块：默认 #1a1a1a 是一块没有层次的纯黑，换成带蓝调的深灰，和站点冷灰体系一致 */
    --md-theme-code-block-bg-color: rgba(32, 38, 56, 0.78);
    --md-theme-code-before-bg-color: rgba(32, 38, 56, 0.78);
    --md-theme-code-block-color: #c8d3e6;
    --md-theme-code-block-radius: 12px;
    /* 行内代码：改用站点主色的浅底 */
    --md-theme-code-inline-color: var(--accent-light);
    --md-theme-code-inline-bg-color: rgba(var(--accent-rgb), 0.14);
    --md-theme-code-inline-radius: 5px;
    /* 链接与引用线也拉回站点配色 */
    --md-theme-link-color: var(--accent-light);
    --md-theme-link-hover-color: var(--accent-pink);
    --md-theme-border-color: rgba(255, 255, 255, 0.1);
}

/* ========== Markdown 表格 ==========
 * 表头淡主色底 + 分隔线式单元格 + 圆角描边，替代默认的近黑条纹表格；
 * 作用范围限在 .md-body 内部，优先级高于 md-editor-v3 自带的表格样式。
 */
.md-body :deep(.md-editor-preview table) {
    width: 100%;
    margin: 1.5em 0;
    border: 1px solid rgba(255, 255, 255, 0.12);
    border-collapse: separate;
    border-spacing: 0;
    border-radius: var(--radius-sm);
    overflow: hidden;
    font-size: 14px;
}

/* 表头单元格：淡主色底 + 提高字重，让首行从正文里跳出来 */
.md-body :deep(.md-editor-preview table thead th) {
    background: rgba(var(--accent-rgb), 0.16);
    color: var(--text-primary);
    font-weight: 700;
    text-align: left;
    white-space: nowrap;
}

/* 所有单元格：去掉默认四边框，只留一条极淡的下分隔线 */
.md-body :deep(.md-editor-preview table th),
.md-body :deep(.md-editor-preview table td) {
    padding: 11px 15px;
    border: none;
    border-bottom: 1px solid rgba(255, 255, 255, 0.09);
    line-height: 1.75;
    word-break: break-word;
}

/* 最后一行不再需要下分隔线 */
.md-body :deep(.md-editor-preview table tbody tr:last-child td) {
    border-bottom: none;
}

/* 悬停整行高亮，横向读长表格时不容易串行 */
.md-body :deep(.md-editor-preview table tbody tr:hover) {
    background-color: rgba(var(--accent-rgb), 0.09);
}

/* ========== Markdown 代码块 ==========
 * 外框做成“卡片里的卡片”：圆角 + 描边 + 落影，避免整块纯黑贴在玻璃卡上。
 */
.md-body :deep(.md-editor-code) {
    margin: 1.5em 0;
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: var(--radius-sm);
    overflow: hidden;
    box-shadow: 0 16px 32px -22px rgba(0, 0, 0, 0.95);
}

/* 顶部信息条（语言标签 + 复制按钮）：用渐变替掉纯黑底，
 * 同时用 position/z-index 覆盖组件默认的 sticky + z-index:10000，防止它浮到站点导航栏上面
 */
.md-body :deep(.md-editor-code .md-editor-code-head) {
    position: relative;
    z-index: 1;
    background: linear-gradient(180deg, rgba(44, 52, 74, 0.95), rgba(31, 37, 54, 0.95));
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

/* 语言标签与复制按钮：提高对比度，默认的灰色在深底上太糊 */
.md-body :deep(.md-editor-code .md-editor-code-lang),
.md-body :deep(.md-editor-code .md-editor-copy-button) {
    color: rgba(226, 232, 244, 0.72);
}

/* 代码正文：加大内边距与行高，并补一道自上而下的极淡高光，
 * 让整块代码看起来是一块“有厚度的面板”而不是一摊纯黑
 */
.md-body :deep(.md-editor-code pre code) {
    padding: 18px 20px;
    line-height: 1.75;
    background-image: linear-gradient(180deg, rgba(255, 255, 255, 0.045), rgba(255, 255, 255, 0) 140px);
}

/* 行内代码：统一等宽字体，避免中英文混排时字重跳变 */
.md-body :deep(.md-editor-preview code) {
    font-family: 'JetBrains Mono', Menlo, Monaco, Consolas, 'Courier New', monospace;
}

/* 让目录跳转时标题不被顶部 sticky 导航栏（68px）遮挡 */
.md-body :deep(h1),
.md-body :deep(h2),
.md-body :deep(h3),
.md-body :deep(h4),
.md-body :deep(h5),
.md-body :deep(h6) {
    scroll-margin-top: 84px;
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
