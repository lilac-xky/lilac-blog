<template>
    <div class="message-center">
        <PageHeader title="消息中心">
            共 {{ total }} 条消息，其中 {{ unreadCount }} 条未读
        </PageHeader>

        <div class="action-bar glass-card">
            <a-tabs v-model:active-key="activeTab" @change="onTabChange" class="msg-tabs">
                <a-tab-pane key="all" tab="全部" />
                <a-tab-pane key="unread" tab="未读" />
                <a-tab-pane key="1" tab="审核通过" />
                <a-tab-pane key="2" tab="审核驳回" />
            </a-tabs>
            <a-button type="link" :disabled="unreadCount === 0" @click="markAllRead">
                <CheckOutlined />全部已读
            </a-button>
        </div>

        <div v-if="loading && !records.length" class="loading list-loading">
            <a-spin />
        </div>

        <div v-else-if="records.length" class="msg-list">
            <article v-for="m in records" :key="m.id" class="msg-row glass-card" :class="{ unread: m.isRead === 0 }"
                @click="openMessage(m)">
                <div class="msg-icon" :class="iconClass(m.type)">
                    <component :is="iconOf(m.type)" />
                </div>
                <div class="msg-body">
                    <div class="msg-head">
                        <span class="msg-title">{{ m.title }}</span>
                        <a-tag :color="tagColor(m.type)" class="msg-type">{{ typeLabel(m.type) }}</a-tag>
                        <span v-if="m.isRead === 0" class="unread-dot"></span>
                    </div>
                    <p v-if="m.content" class="msg-content">{{ m.content }}</p>
                    <div class="msg-time">{{ formatDate(m.createTime) }}</div>
                </div>
            </article>
        </div>

        <ListEmpty v-else text="暂无消息" />

        <div v-if="total > pageSize" class="pagination-wrap">
            <a-pagination v-model:current="current" :total="total" :page-size="pageSize" show-quick-jumper
                :show-total="(t: number) => `共 ${t} 条`" @change="fetchList" />
        </div>
    </div>
</template>

<!--
  MessageCenter：前台「消息中心」
  - 按 Tab 切换全部/未读/审核通过/审核驳回
  - 点击消息条目自动标记已读，审核类消息跳转至「我的文章」
  - 支持一键全部已读
-->
<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import {
    CheckOutlined,
    CheckCircleOutlined,
    CloseCircleOutlined,
    BellOutlined,
} from '@ant-design/icons-vue';
import {
    listMyMessageByPage,
    markRead,
    getUnreadCount,
} from '@/api/messageController';
import PageHeader from '@/components/PageHeader.vue';
import ListEmpty from '@/components/ListEmpty.vue';
import { useListPagination } from '@/composables/useListPagination';
import { useDateFormat } from '@/composables/useDateFormat';

const router = useRouter();
const { formatDate } = useDateFormat();
const unreadCount = ref(0);

const { records, total, current, pageSize, loading, activeTab, fetchList, onTabChange } =
    useListPagination<API.MessageVO, API.MessageQueryRequest>({
        pageSize: 15,
        buildQuery: ({ tab, current, pageSize }) => {
            const body: API.MessageQueryRequest = { current, pageSize };
            if (tab === '1' || tab === '2') {
                body.type = Number(tab);
            } else if (tab === 'unread') {
                body.isRead = 0;
            }
            return body;
        },
        fetcher: async (body) => {
            const res = await listMyMessageByPage(body);
            const data = res.data?.data;
            return { records: data?.records ?? [], total: Number(data?.total ?? 0) };
        },
        onAfterFetch: () => fetchUnread(),
    });

async function fetchUnread() {
    try {
        const res = await getUnreadCount({ silentError: true });
        unreadCount.value = Number(res.data?.data ?? 0);
    } catch {
        unreadCount.value = 0;
    }
}

async function openMessage(m: API.MessageVO) {
    if (m.isRead === 0 && m.id) {
        try {
            await markRead({ ids: [m.id] });
            m.isRead = 1;
            fetchUnread();
        } catch {
            // ignore
        }
    }
    // 审核类消息（type 1 通过 / 2 驳回）联动跳到我的文章页面查看
    if ((m.type === 1 || m.type === 2) && m.refType === 'article') {
        router.push('/user/articles');
    }
}

async function markAllRead() {
    try {
        const res = await markRead({ ids: records.value.filter((m) => m.isRead === 0).map((m) => m.id).filter((id): id is number => id != null) });
        if ((res.data?.data ?? 0) > 0) {
            message.success('已全部标记为已读');
        }
        records.value.forEach((m) => (m.isRead = 1));
        fetchUnread();
    } catch {
        // ignore
    }
}

function typeLabel(t?: number) {
    if (t === 1) return '审核通过';
    if (t === 2) return '审核驳回';
    if (t === 99) return '系统通知';
    return '消息';
}

function tagColor(t?: number) {
    if (t === 1) return 'green';
    if (t === 2) return 'red';
    return 'default';
}

function iconOf(t?: number) {
    if (t === 1) return CheckCircleOutlined;
    if (t === 2) return CloseCircleOutlined;
    return BellOutlined;
}

function iconClass(t?: number) {
    if (t === 1) return 'icon-pass';
    if (t === 2) return 'icon-reject';
    return 'icon-system';
}

fetchList();
</script>

<style scoped>
.message-center {
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

.msg-tabs {
    flex: 1;
}

:deep(.msg-tabs .ant-tabs-nav) {
    margin-bottom: 0;
}

:deep(.msg-tabs .ant-tabs-tab) {
    color: var(--text-secondary);
    font-size: 14px;
}

:deep(.msg-tabs .ant-tabs-tab-active .ant-tabs-tab-btn) {
    color: var(--accent) !important;
}

:deep(.msg-tabs .ant-tabs-ink-bar) {
    background: var(--accent);
}

.msg-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.msg-row {
    display: flex;
    align-items: flex-start;
    gap: 14px;
    padding: 18px 20px;
    cursor: pointer;
    transition: all 0.2s;
}

.msg-row:hover {
    box-shadow: var(--shadow-hover);
}

.msg-row.unread {
    border-color: rgba(var(--accent-rgb), 0.32);
}

.msg-icon {
    width: 40px;
    height: 40px;
    flex-shrink: 0;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
}

.icon-pass {
    background: rgba(52, 211, 153, 0.16);
    color: #34d399;
}

.icon-reject {
    background: rgba(248, 113, 113, 0.16);
    color: #f87171;
}

.icon-system {
    background: rgba(var(--accent-rgb), 0.16);
    color: #38bdf8;
}

.msg-body {
    flex: 1;
    min-width: 0;
}

.msg-head {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 4px;
}

.msg-title {
    font-size: 15px;
    font-weight: 600;
    color: var(--text-primary);
}

.msg-type {
    border-radius: var(--radius-pill) !important;
    border: none !important;
    font-size: 11px;
}

.unread-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: var(--accent);
    box-shadow: 0 0 8px var(--accent);
}

.msg-content {
    color: var(--text-secondary);
    font-size: 13px;
    line-height: 1.6;
    margin-bottom: 6px;
}

.msg-time {
    color: var(--text-muted);
    font-size: 12px;
}

.pagination-wrap {
    display: flex;
    justify-content: center;
    margin-top: 10px;
}
</style>
