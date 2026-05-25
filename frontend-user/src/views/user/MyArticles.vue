<template>
    <div class="my-articles">
        <header class="page-head">
            <h1>我的文章</h1>
            <p class="subtitle">
                <span class="dot-glow"></span>
                共 {{ total }} 篇，{{ activeTabLabel }}
            </p>
        </header>

        <div class="action-bar glass-card">
            <a-tabs v-model:active-key="activeTab" @change="onTabChange" class="status-tabs">
                <a-tab-pane key="all" tab="全部" />
                <a-tab-pane key="0" tab="草稿" />
                <a-tab-pane key="1" tab="待审核" />
                <a-tab-pane key="2" tab="已发布" />
            </a-tabs>
            <router-link to="/user/write" class="write-btn">
                <EditOutlined />
                写新文章
            </router-link>
        </div>

        <div v-if="loading && !records.length" class="loading">
            <a-spin />
        </div>

        <div v-else-if="records.length" class="article-list">
            <article v-for="a in records" :key="a.id" class="article-row glass-card"
                :class="{ rejected: hasReject(a) }">
                <div class="row-left">
                    <div class="row-title-line">
                        <h3 class="row-title" @click="goDetail(a)">{{ a.title || '无标题' }}</h3>
                        <a-tag :color="statusColor(a.status)" class="row-status">
                            {{ statusLabel(a.status) }}
                        </a-tag>
                    </div>
                    <p v-if="a.summary" class="row-summary">{{ a.summary }}</p>
                    <div class="row-meta">
                        <span v-if="a.categoryName" class="meta-tag">
                            <FolderOpenOutlined />{{ a.categoryName }}
                        </span>
                        <span v-for="t in (a.tags ?? []).filter((x): x is API.TagVO => !!x?.id)" :key="t.id"
                            class="meta-tag">
                            #{{ t.tagName }}
                        </span>
                        <span class="meta-tag">
                            <ClockCircleOutlined />{{ formatDate(a.createTime) }}
                        </span>
                        <span class="meta-tag">
                            <EyeOutlined />{{ a.viewCount ?? 0 }}
                        </span>
                    </div>
                    <div v-if="hasReject(a)" class="reject-banner">
                        <ExclamationCircleOutlined />
                        <span><strong>驳回原因：</strong>{{ a.rejectReason }}</span>
                    </div>
                </div>
                <div class="row-right">
                    <a-button v-if="canEdit(a)" type="link" @click="goEdit(a)">
                        <EditOutlined />编辑
                    </a-button>
                    <a-button v-if="a.status === 2" type="link" @click="goDetail(a)">
                        <EyeOutlined />查看
                    </a-button>
                    <a-button type="link" danger @click="confirmDelete(a)">
                        <DeleteOutlined />删除
                    </a-button>
                </div>
            </article>
        </div>

        <div v-else class="empty glass-card">
            <FileSearchOutlined />
            <p>暂无文章</p>
            <router-link to="/user/write" class="empty-cta">去写一篇</router-link>
        </div>

        <div v-if="total > pageSize" class="pagination-wrap">
            <a-pagination v-model:current="current" :total="total" :page-size="pageSize" show-quick-jumper
                :show-total="(t: number) => `共 ${t} 篇`" @change="fetchList" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { message, Modal } from 'ant-design-vue';
import {
    EditOutlined,
    DeleteOutlined,
    EyeOutlined,
    ClockCircleOutlined,
    FolderOpenOutlined,
    FileSearchOutlined,
    ExclamationCircleOutlined,
} from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import { listMyArticles, deleteMyArticle } from '@/api/articleController';

const router = useRouter();

const records = ref<API.ArticleVO[]>([]);
const total = ref(0);
const current = ref(1);
const pageSize = 10;
const loading = ref(false);

// '0' '1' '2' 'all'
const activeTab = ref<string>('all');

const activeTabLabel = computed(() => {
    if (activeTab.value === '0') return '草稿';
    if (activeTab.value === '1') return '待审核';
    if (activeTab.value === '2') return '已发布';
    return '全部';
});

function statusLabel(s?: number) {
    if (s === 0) return '草稿';
    if (s === 1) return '待审核';
    if (s === 2) return '已发布';
    return '未知';
}

function statusColor(s?: number) {
    if (s === 0) return 'default';
    if (s === 1) return 'orange';
    if (s === 2) return 'green';
    return 'default';
}

function hasReject(a: API.ArticleVO) {
    return a.status === 0 && !!a.rejectReason?.trim();
}

function canEdit(a: API.ArticleVO) {
    // 仅草稿/待审核可编辑（已发布需联系管理员）
    return a.status === 0 || a.status === 1;
}

function formatDate(d?: string) {
    return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '';
}

async function fetchList() {
    loading.value = true;
    try {
        const status = activeTab.value === 'all' ? undefined : Number(activeTab.value);
        const res = await listMyArticles({
            current: current.value,
            pageSize,
            status,
            sortOrder: 'descend',
        });
        const data = res.data?.data;
        records.value = data?.records ?? [];
        total.value = Number(data?.total ?? 0);
    } finally {
        loading.value = false;
    }
}

function onTabChange() {
    current.value = 1;
    fetchList();
}

function goDetail(a: API.ArticleVO) {
    if (a.status === 2) {
        router.push(`/article/${a.id}`);
    } else {
        goEdit(a);
    }
}

function goEdit(a: API.ArticleVO) {
    router.push({ path: '/user/write', query: { id: a.id } });
}

function confirmDelete(a: API.ArticleVO) {
    Modal.confirm({
        title: '确认删除该文章？',
        content: `「${a.title || '无标题'}」删除后不可恢复`,
        okText: '确认',
        okType: 'danger',
        cancelText: '取消',
        async onOk() {
            try {
                const res = await deleteMyArticle({ id: a.id });
                if (res.data?.data) {
                    message.success('已删除');
                    fetchList();
                }
            } catch {
                // ignore
            }
        },
    });
}

onMounted(fetchList);
</script>

<style scoped>
.my-articles {
    display: flex;
    flex-direction: column;
    gap: 18px;
}

.page-head h1 {
    font-size: 28px;
    font-weight: 700;
    margin-bottom: 8px;
}

.subtitle {
    color: var(--text-secondary);
    font-size: 13px;
    display: flex;
    align-items: center;
    gap: 8px;
}

.dot-glow {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--accent);
    box-shadow: 0 0 10px var(--accent);
}

.action-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 4px 20px;
}

.status-tabs {
    flex: 1;
}

:deep(.status-tabs .ant-tabs-nav) {
    margin-bottom: 0;
}

:deep(.status-tabs .ant-tabs-tab) {
    color: var(--text-secondary);
    font-size: 14px;
}

:deep(.status-tabs .ant-tabs-tab-active .ant-tabs-tab-btn) {
    color: var(--accent) !important;
}

:deep(.status-tabs .ant-tabs-ink-bar) {
    background: var(--accent);
}

.write-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 6px 16px;
    border-radius: var(--radius-pill);
    background: var(--accent);
    color: #fff !important;
    font-size: 13px;
}

.write-btn:hover {
    background: var(--accent-strong);
}

.loading {
    text-align: center;
    padding: 60px 0;
}

.article-list {
    display: flex;
    flex-direction: column;
    gap: 14px;
}

.article-row {
    display: flex;
    gap: 16px;
    padding: 20px 24px;
    transition: all 0.2s;
}

.article-row:hover {
    box-shadow: var(--shadow-hover);
}

.article-row.rejected {
    border-color: rgba(239, 68, 68, 0.32);
}

.row-left {
    flex: 1;
    min-width: 0;
}

.row-title-line {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 8px;
}

.row-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    cursor: pointer;
    transition: color 0.2s;
}

.row-title:hover {
    color: var(--accent);
}

.row-status {
    border-radius: var(--radius-pill) !important;
    border: none !important;
    font-size: 12px;
    padding: 0 10px;
}

.row-summary {
    color: var(--text-secondary);
    font-size: 13px;
    line-height: 1.6;
    margin-bottom: 8px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
}

.row-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    color: var(--text-muted);
    font-size: 12px;
}

.meta-tag {
    display: inline-flex;
    align-items: center;
    gap: 4px;
}

.reject-banner {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 10px;
    padding: 8px 12px;
    border-radius: var(--radius-sm);
    background: rgba(239, 68, 68, 0.12);
    border: 1px solid rgba(239, 68, 68, 0.28);
    color: #fca5a5;
    font-size: 12px;
}

.row-right {
    display: flex;
    flex-direction: column;
    gap: 4px;
    align-items: stretch;
    flex-shrink: 0;
}

.pagination-wrap {
    display: flex;
    justify-content: center;
    margin-top: 10px;
}

.empty {
    text-align: center;
    padding: 60px 20px;
    color: var(--text-secondary);
}

.empty :deep(.anticon) {
    font-size: 48px;
    margin-bottom: 12px;
    color: var(--text-muted);
}

.empty-cta {
    display: inline-block;
    margin-top: 10px;
    padding: 6px 18px;
    border-radius: var(--radius-pill);
    background: var(--accent);
    color: #fff !important;
    font-size: 13px;
}

@media (max-width: 720px) {
    .article-row {
        flex-direction: column;
        padding: 16px;
    }

    .row-right {
        flex-direction: row;
        justify-content: flex-end;
    }
}
</style>
