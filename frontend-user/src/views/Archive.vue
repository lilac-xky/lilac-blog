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
            <div class="filter-section" v-if="categories.length">
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

        <!-- 时间轴风格的归档列表 -->
        <div v-if="loading && !records.length" class="loading">
            <a-spin />
        </div>
        <div v-else-if="records.length" class="timeline">
            <div v-for="a in records" :key="a.id" class="tl-item">
                <div class="tl-dot"></div>
                <div class="tl-date">
                    <ClockCircleOutlined />
                    {{ formatDate(a.createTime) }}
                </div>
                <div class="tl-card">
                    <ArticleCard :article="a" />
                </div>
            </div>
        </div>
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
import { onMounted, ref } from 'vue';
import { ClockCircleOutlined, FileSearchOutlined, SearchOutlined } from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import { listArticleVoByPage } from '@/api/articleController';
import { listCategoryByPageVo } from '@/api/categoryController';
import { listTagByPageVo } from '@/api/tagController';
import ArticleCard from '@/components/ArticleCard.vue';

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

// 格式化日期：YYYY-MM-DD HH:mm
function formatDate(d?: string) {
    return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '';
}

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

onMounted(() => {
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

.filter-card {
    padding: 20px 22px;
    display: flex;
    flex-direction: column;
    gap: 14px;
}

.filter-section {
    display: flex;
    align-items: flex-start;
    gap: 14px;
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

.timeline {
    position: relative;
    padding-left: 28px;
    display: flex;
    flex-direction: column;
    gap: 22px;
}

.timeline::before {
    content: '';
    position: absolute;
    left: 9px;
    top: 6px;
    bottom: 6px;
    width: 2px;
    background: linear-gradient(180deg, var(--accent), transparent);
}

.tl-item {
    position: relative;
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.tl-dot {
    position: absolute;
    left: -24px;
    top: 8px;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: var(--accent);
    box-shadow: 0 0 12px var(--accent);
    border: 2px solid #0d0a1f;
}

.tl-date {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    color: var(--accent);
    font-size: 12px;
    font-weight: 500;
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
}
</style>
