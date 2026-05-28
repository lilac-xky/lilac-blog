<template>
    <div class="my-articles">
        <PageHeader title="我的文章">
            共 {{ total }} 篇，{{ activeTabLabel }}
        </PageHeader>

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

        <div v-if="loading && !records.length" class="loading list-loading">
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

        <ListEmpty v-else text="暂无文章">
            <template #icon>
                <FileSearchOutlined />
            </template>
            <template #extra>
                <router-link to="/user/write" class="empty-cta">去写一篇</router-link>
            </template>
        </ListEmpty>

        <div v-if="total > pageSize" class="pagination-wrap">
            <a-pagination v-model:current="current" :total="total" :page-size="pageSize" show-quick-jumper
                :show-total="(t: number) => `共 ${t} 篇`" @change="fetchList" />
        </div>
    </div>
</template>

<!--
  MyArticles：前台「我的文章」
  - 按状态 Tab 切换（全部/草稿/待审核/已发布）
  - 被驳回的草稿（status=0 且有 rejectReason）会在卡片内嵌红色提示条，提醒用户改完重提
  - 已发布文章不可在前台编辑，需联系管理员（点击查看跳详情页）
-->
<script setup lang="ts">
import { computed } from 'vue';
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
import { listMyArticles, deleteMyArticle } from '@/api/articleController';
import PageHeader from '@/components/PageHeader.vue';
import ListEmpty from '@/components/ListEmpty.vue';
import { useListPagination } from '@/composables/useListPagination';
import { useDateFormat } from '@/composables/useDateFormat';

const router = useRouter();
const { formatDate } = useDateFormat();

const { records, total, current, pageSize, loading, activeTab, fetchList, onTabChange } =
    useListPagination<API.ArticleVO, API.ArticleQueryRequest>({
        pageSize: 10,
        buildQuery: ({ tab, current, pageSize }) => ({
            current,
            pageSize,
            status: tab === 'all' ? undefined : Number(tab),
            sortOrder: 'descend',
        }),
        fetcher: async (query) => {
            const res = await listMyArticles(query);
            const data = res.data?.data;
            return { records: data?.records ?? [], total: Number(data?.total ?? 0) };
        },
    });

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

// 被驳回回到草稿态时，rejectReason 仍保留，用作页面提示
function hasReject(a: API.ArticleVO) {
    return a.status === 0 && !!a.rejectReason?.trim();
}

function canEdit(a: API.ArticleVO) {
    // 仅草稿/待审核可编辑（已发布需联系管理员）
    return a.status === 0 || a.status === 1;
}

// 已发布文章看详情页；草稿/待审核「查看」实际等同于编辑
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

fetchList();
</script>

<style scoped>
.my-articles {
    display: flex;
    flex-direction: column;
    gap: 18px;
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
