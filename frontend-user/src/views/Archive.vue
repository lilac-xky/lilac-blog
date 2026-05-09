<template>
    <div class="archive-page">
        <header class="page-head">
            <h1>归档与探索</h1>
            <p class="subtitle">
                <span class="dot-glow"></span>
                总计 {{ totalAll }} 篇研究记录
            </p>
        </header>

        <!-- 搜索框：按标题模糊搜索 -->
        <div class="search-wrap">
            <a-input v-model:value="keyword" size="large" allow-clear placeholder="搜寻被封存的知识..." class="archive-search"
                @press-enter="onSearch" @change="onClearSearch">
                <template #prefix>
                    <SearchOutlined />
                </template>
            </a-input>
        </div>

        <!-- 分类 / 标签 筛选条 -->
        <div class="filter-card glass-card">
            <div class="filter-section filter-section-with-toggle" v-if="categories.length">
                <span class="filter-label">分类</span>
                <div class="pills">
                    <button class="pill" :class="{ active: filterType === 'all' }" @click="setAll">
                        全部档案 <span class="count">{{ totalAll }}</span>
                    </button>
                    <button v-for="c in categories" :key="`c${c.id}`" class="pill"
                        :class="{ active: filterType === 'category' && filterId === c.id }" @click="pickCategory(c)">
                        {{ c.categoryName }}
                    </button>
                </div>
                <div class="view-toggle">
                    <button class="toggle-btn" :class="{ active: viewMode === 'timeline' }"
                        @click="viewMode = 'timeline'">
                        <MenuOutlined />
                        <span>中枢链路</span>
                    </button>
                    <button class="toggle-btn" :class="{ active: viewMode === 'grid' }"
                        @click="viewMode = 'grid'">
                        <AppstoreOutlined />
                        <span>矩阵网格</span>
                    </button>
                </div>
            </div>
            <div class="filter-section" v-if="tags.length">
                <span class="filter-label">标签</span>
                <div class="pills">
                    <button v-for="t in tags" :key="`t${t.id}`" class="pill"
                        :class="{ active: filterType === 'tag' && filterId === t.id }" @click="pickTag(t)">
                        # {{ t.tagName }}
                    </button>
                </div>
            </div>
        </div>

        <!-- 加载态 -->
        <div v-if="loading && !records.length" class="loading">
            <a-spin />
        </div>

        <!-- 中枢链路：竖线居中，文章左右锯齿排列 -->
        <div v-else-if="records.length && viewMode === 'timeline'" class="archive-timeline">
            <div class="timeline-line"></div>
            <article v-for="a in records" :key="a.id" class="tl-item">
                <span class="tl-dot"></span>
                <ArticleCard :article="a" />
            </article>
        </div>

        <!-- 矩阵网格：自适应多列网格 -->
        <div v-else-if="records.length && viewMode === 'grid'" class="archive-grid">
            <ArticleCard v-for="a in records" :key="a.id" :article="a" />
        </div>

        <!-- 空态 -->
        <div v-else class="empty glass-card">
            <FileSearchOutlined />
            <p>没有匹配的文章</p>
        </div>

        <div v-if="total > pageSize" class="pager">
            <a-pagination v-model:current="current" :total="total" :page-size="pageSize" :show-size-changer="false"
                @change="loadList" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import {
    AppstoreOutlined,
    FileSearchOutlined,
    MenuOutlined,
    SearchOutlined,
} from '@ant-design/icons-vue';
import { listArticleVoByPage } from '@/api/articleController';
import { listCategoryByPageVo } from '@/api/categoryController';
import { listTagByPageVo } from '@/api/tagController';
import ArticleCard from '@/components/ArticleCard.vue';

const route = useRoute();

// 列表与分页状态
const records = ref<API.ArticleVO[]>([]);
const total = ref(0);
const totalAll = ref(0);
const current = ref(1);
const pageSize = 10;
const loading = ref(false);

// 筛选项数据：分类 / 标签
const categories = ref<API.CategoryVO[]>([]);
const tags = ref<API.TagVO[]>([]);

// 当前筛选状态：关键词 + 分类/标签筛选类型与 id
const keyword = ref('');
const filterType = ref<'all' | 'category' | 'tag'>('all');
const filterId = ref<number | null>(null);

// 视图模式：中枢链路（锯齿时间轴）/ 矩阵网格
const viewMode = ref<'timeline' | 'grid'>('timeline');

// 拉取分类与标签列表（用于筛选条）
async function loadFilters() {
    try {
        const [c, t] = await Promise.all([
            listCategoryByPageVo({ current: 1, pageSize: 50 }),
            listTagByPageVo({ current: 1, pageSize: 50 }),
        ]);
        categories.value = c.data?.data?.records ?? [];
        tags.value = t.data?.data?.records ?? [];
    } catch {
        categories.value = [];
        tags.value = [];
    }
}

// 拉取站内文章总数（不受筛选影响，用于副标题展示）
async function loadTotalAll() {
    try {
        const res = await listArticleVoByPage(
            { current: 1, pageSize: 1, status: 2 },
            { silentError: true }
        );
        totalAll.value = res.data?.data?.total ?? 0;
    } catch {
        totalAll.value = 0;
    }
}

// 根据当前关键词与筛选条件拉取文章列表
async function loadList() {
    loading.value = true;
    try {
        const body: API.ArticleQueryRequest = {
            current: current.value,
            pageSize,
            status: 2,
        };
        if (keyword.value.trim()) body.title = keyword.value.trim();
        if (filterType.value === 'category' && filterId.value != null) {
            body.categoryId = filterId.value;
        } else if (filterType.value === 'tag' && filterId.value != null) {
            body.tagIds = [filterId.value];
        }
        const res = await listArticleVoByPage(body, { silentError: true });
        records.value = res.data?.data?.records ?? [];
        total.value = res.data?.data?.total ?? 0;
    } catch {
        records.value = [];
        total.value = 0;
    } finally {
        loading.value = false;
    }
}

// 切换为「全部档案」
function setAll() {
    filterType.value = 'all';
    filterId.value = null;
    current.value = 1;
    loadList();
}

// 选中某个分类
function pickCategory(c: API.CategoryVO) {
    filterType.value = 'category';
    filterId.value = c.id ?? null;
    current.value = 1;
    loadList();
}

// 选中某个标签
function pickTag(t: API.TagVO) {
    filterType.value = 'tag';
    filterId.value = t.id ?? null;
    current.value = 1;
    loadList();
}

// 触发搜索：回车提交关键词
function onSearch() {
    current.value = 1;
    loadList();
}

// 清空搜索框时自动恢复列表
function onClearSearch(e: any) {
    if (!e?.target?.value) {
        current.value = 1;
        loadList();
    }
}

// 根据当前 URL query 同步 keyword / 分类 / 标签 筛选状态
function syncFromQuery() {
    const q = route.query;
    keyword.value = typeof q.title === 'string' ? q.title : '';
    if (q.categoryId) {
        filterType.value = 'category';
        filterId.value = Number(q.categoryId);
    } else if (q.tagId) {
        filterType.value = 'tag';
        filterId.value = Number(q.tagId);
    } else {
        filterType.value = 'all';
        filterId.value = null;
    }
    current.value = 1;
}

// 监听 query 变化（如从首页标签云点入），重新拉取列表
watch(() => route.query, () => {
    syncFromQuery();
    loadList();
});

onMounted(() => {
    syncFromQuery();
    loadTotalAll();
    loadFilters();
    loadList();
});
</script>

<style scoped>
.archive-page {
    display: flex;
    flex-direction: column;
    gap: 26px;
}

.page-head {
    text-align: center;
    padding: 20px 0 8px;
}

.page-head h1 {
    font-size: 38px;
    font-weight: 800;
    letter-spacing: 0.04em;
    background: linear-gradient(135deg, #f5f3ff, #7dd3fc 60%, #ec4899);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    margin-bottom: 12px;
}

.subtitle {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    color: var(--text-secondary);
    font-size: 13px;
}

.dot-glow {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--accent);
    box-shadow: 0 0 10px var(--accent);
}

.search-wrap {
    max-width: 640px;
    margin: 0 auto;
    width: 100%;
}

.archive-search :deep(.ant-input) {
    background: transparent !important;
}

/* 筛选卡：覆盖 .glass-card 让背景更透 */
.filter-card.glass-card {
    padding: 20px 22px;
    display: flex;
    flex-direction: column;
    gap: 14px;
    background: rgba(20, 18, 40, 0.22) !important;
    border-color: rgba(255, 255, 255, 0.06) !important;
}

.filter-section {
    display: flex;
    align-items: flex-start;
    gap: 14px;
}

/* 第一行筛选区右侧需腾出空间放视图切换按钮 */
.filter-section-with-toggle .pills {
    padding-right: 8px;
}

.filter-label {
    flex: 0 0 auto;
    color: var(--text-muted);
    font-size: 12px;
    letter-spacing: 0.1em;
    padding-top: 6px;
}

.pills {
    flex: 1;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.pill {
    padding: 6px 16px;
    border-radius: var(--radius-pill);
    background: rgba(255, 255, 255, 0.04);
    border: 1px solid var(--border-soft);
    color: var(--text-secondary);
    font-size: 13px;
    cursor: pointer;
    transition: all 0.2s;
}

.pill:hover {
    color: var(--text-primary);
    border-color: rgba(56, 189, 248, 0.4);
}

.pill.active {
    background: var(--accent);
    border-color: var(--accent);
    color: #fff;
    box-shadow: 0 4px 18px rgba(14, 165, 233, 0.4);
}

.pill .count {
    margin-left: 4px;
    color: var(--accent-pink);
    font-weight: 600;
}

.pill.active .count {
    color: #fff;
}

/* 视图切换按钮组 */
.view-toggle {
    flex: 0 0 auto;
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px;
    border-radius: var(--radius-pill);
    background: rgba(255, 255, 255, 0.04);
    border: 1px solid var(--border-soft);
}

.toggle-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 5px 14px;
    border-radius: var(--radius-pill);
    border: none;
    background: transparent;
    color: var(--text-secondary);
    font-size: 13px;
    cursor: pointer;
    transition: all 0.2s;
    white-space: nowrap;
}

.toggle-btn:hover {
    color: var(--text-primary);
}

.toggle-btn.active {
    background: var(--accent);
    color: #fff;
    box-shadow: 0 4px 14px rgba(56, 189, 248, 0.4);
}

/* 矩阵网格视图：自适应多列 */
.archive-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 22px;
    padding: 8px 0;
}

/* 归档时间轴 */
.archive-timeline {
    position: relative;
    display: flex;
    flex-direction: column;
    gap: 36px;
    padding: 24px 0;
}

.timeline-line {
    position: absolute;
    left: 50%;
    top: 0;
    bottom: 0;
    width: 2px;
    transform: translateX(-50%);
    background: linear-gradient(180deg,
            rgba(56, 189, 248, 0.7),
            rgba(56, 189, 248, 0.25) 60%,
            transparent);
    pointer-events: none;
}

/* tl-item 仍占满半侧（保持圆点相对中线的定位准确），卡片在内部限宽并贴中线一侧 */
.tl-item {
    position: relative;
    width: calc(50% - 70px);
}

.tl-item:nth-of-type(odd) {
    align-self: flex-start;
}

.tl-item:nth-of-type(even) {
    align-self: flex-end;
}

/* 时间轴卡片：横向宽幅矩形（宽 > 高） */
.tl-item :deep(.article-card) {
    max-width: 410px;
    width: 100%;
}

.tl-item:nth-of-type(odd) :deep(.article-card) {
    margin-left: auto;
}

.tl-item:nth-of-type(even) :deep(.article-card) {
    margin-right: auto;
}

/* 圆点：垂直居中对齐卡片中部，精确落在中线上（70px 间距 + 圆点半径 10px） */
.tl-dot {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: var(--accent);
    box-shadow: 0 0 18px var(--accent), 0 0 0 6px rgba(56, 189, 248, 0.18);
    border: 2px solid #0d0a1f;
    z-index: 2;
}

.tl-item:nth-of-type(odd) .tl-dot {
    right: -80px;
}

.tl-item:nth-of-type(even) .tl-dot {
    left: -80px;
}

.loading {
    text-align: center;
    padding: 60px 0;
}

.empty {
    text-align: center;
    padding: 60px 12px;
    color: var(--text-muted);
}

.empty :deep(.anticon) {
    font-size: 36px;
    margin-bottom: 10px;
}

.pager {
    display: flex;
    justify-content: center;
    margin-top: 20px;
}

@media (max-width: 720px) {
    .filter-section {
        flex-direction: column;
        gap: 8px;
    }

    .view-toggle {
        align-self: flex-start;
    }

    .toggle-btn span {
        display: none;
    }

    /* 矩阵网格：移动端单列 */
    .archive-grid {
        grid-template-columns: 1fr;
        gap: 16px;
    }

    /* 移动端：单列布局，时间线移至左侧 */
    .archive-timeline {
        gap: 24px;
        padding: 16px 0 16px 28px;
    }

    .timeline-line {
        left: 8px;
        transform: none;
    }

    .tl-item {
        width: 100%;
        max-width: none;
    }

    .tl-item:nth-of-type(odd),
    .tl-item:nth-of-type(even) {
        align-self: stretch;
    }

    /* 移动端：卡片占满 + 圆点回到顶部左侧 */
    .tl-item :deep(.article-card) {
        max-width: none;
        margin: 0 !important;
    }

    .tl-item:nth-of-type(odd) .tl-dot,
    .tl-item:nth-of-type(even) .tl-dot {
        top: 24px;
        transform: none;
        left: -28px;
        right: auto;
    }
}
</style>
