<template>
    <div class="articles-page">
        <header class="page-head">
            <h1>文章</h1>
            <p v-if="keyword">
                搜索 “<span class="kw">{{ keyword }}</span>” 共 {{ total }} 篇
                <button type="button" class="clear-kw" @click="clearKeyword">清除</button>
            </p>
            <p v-else>共 {{ total }} 篇 · 慢慢翻阅，好奇心是最好的索引。</p>
        </header>

        <div v-if="loading && !records.length" class="loading">
            <a-spin />
        </div>
        <div v-else-if="records.length" class="article-list">
            <ArticleCard v-for="a in records" :key="a.id" :article="a" />
        </div>
        <div v-else class="empty glass-card">
            <FileSearchOutlined />
            <p v-if="keyword">没有匹配 “{{ keyword }}” 的文章</p>
            <p v-else>还没有文章</p>
        </div>

        <div v-if="total > pageSize" class="pager">
            <a-pagination v-model:current="current" :total="total" :page-size="pageSize" :show-size-changer="false"
                @change="loadList" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { FileSearchOutlined } from '@ant-design/icons-vue';
import { listArticleVoByPage } from '@/api/articleController';
import ArticleCard from '@/components/ArticleCard.vue';

const route = useRoute();
const router = useRouter();

// 分页状态：仅展示已发布文章（status=2）
const records = ref<API.ArticleVO[]>([]);
const total = ref(0);
const current = ref(1);
const pageSize = 10;
const loading = ref(false);

const keyword = computed(() => {
    const t = route.query.title;
    return typeof t === 'string' ? t : '';
});

const tagId = computed(() => {
    const v = route.query.tagId;
    if (typeof v !== 'string') return undefined;
    const n = Number(v);
    return Number.isFinite(n) ? n : undefined;
});

const categoryId = computed(() => {
    const v = route.query.categoryId;
    if (typeof v !== 'string') return undefined;
    const n = Number(v);
    return Number.isFinite(n) ? n : undefined;
});

async function loadList() {
    loading.value = true;
    try {
        const res = await listArticleVoByPage(
            {
                current: current.value,
                pageSize,
                status: 2,
                ...(keyword.value ? { title: keyword.value } : {}),
                ...(tagId.value !== undefined ? { tagIds: [tagId.value] } : {}),
                ...(categoryId.value !== undefined ? { categoryId: categoryId.value } : {}),
            },
            { silentError: true }
        );
        records.value = res.data?.data?.records ?? [];
        total.value = res.data?.data?.total ?? 0;
    } catch {
        records.value = [];
        total.value = 0;
    } finally {
        loading.value = false;
    }
}

function clearKeyword() {
    router.replace({ path: '/articles' });
}

watch(
    () => [keyword.value, tagId.value, categoryId.value] as const,
    () => {
        current.value = 1;
        loadList();
    }
);

onMounted(loadList);
</script>

<style scoped>
.articles-page {
    display: flex;
    flex-direction: column;
    gap: 26px;
}

.page-head h1 {
    font-size: 30px;
    font-weight: 800;
    background: linear-gradient(135deg, #f5f3ff, #7dd3fc);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    margin-bottom: 6px;
}

.page-head p {
    color: var(--text-secondary);
    font-size: 13px;
}

.kw {
    color: var(--accent);
    font-weight: 600;
}

.clear-kw {
    margin-left: 10px;
    padding: 2px 10px;
    border-radius: var(--radius-pill);
    border: 1px solid var(--border-soft);
    background: rgba(255, 255, 255, 0.04);
    color: var(--text-secondary);
    font-size: 12px;
    cursor: pointer;
    transition: all 0.2s;
}

.clear-kw:hover {
    color: var(--accent);
    border-color: rgba(56, 189, 248, 0.45);
}

.article-list {
    display: grid;
    gap: 14px;
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
</style>
