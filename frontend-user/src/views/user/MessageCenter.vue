<template>
    <div class="message-center">
        <header class="page-head">
            <h1>消息中心</h1>
            <p class="subtitle">
                <span class="dot-glow"></span>
                共 {{ total }} 条消息，其中 {{ unreadCount }} 条未读
            </p>
        </header>

        <div class="action-bar glass-card">
            <a-tabs v-model:active-key="filterTab" @change="onTabChange" class="msg-tabs">
                <a-tab-pane key="all" tab="全部" />
                <a-tab-pane key="unread" tab="未读" />
                <a-tab-pane key="1" tab="审核通过" />
                <a-tab-pane key="2" tab="审核驳回" />
            </a-tabs>
            <a-button type="link" :disabled="unreadCount === 0" @click="markAllRead">
                <CheckOutlined />全部已读
            </a-button>
        </div>

        <div v-if="loading && !records.length" class="loading">
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

        <div v-else class="empty glass-card">
            <InboxOutlined />
            <p>暂无消息</p>
        </div>

        <div v-if="total > pageSize" class="pagination-wrap">
            <a-pagination v-model:current="current" :total="total" :page-size="pageSize" show-quick-jumper
                :show-total="(t: number) => `共 ${t} 条`" @change="fetchList" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import {
    CheckOutlined,
    CheckCircleOutlined,
    CloseCircleOutlined,
    BellOutlined,
    InboxOutlined,
} from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import {
    listMyMessageByPage,
    markRead,
    getUnreadCount,
} from '@/api/messageController';

const router = useRouter();

const records = ref<API.MessageVO[]>([]);
const total = ref(0);
const current = ref(1);
const pageSize = 15;
const loading = ref(false);
const unreadCount = ref(0);

const filterTab = ref<string>('all');

function onTabChange() {
    current.value = 1;
    fetchList();
}

async function fetchList() {
    loading.value = true;
    try {
        const body: API.MessageQueryRequest = {
            current: current.value,
            pageSize,
        };
        if (filterTab.value === '1' || filterTab.value === '2') {
            body.type = Number(filterTab.value);
        } else if (filterTab.value === 'unread') {
            body.isRead = 0;
        }
        const res = await listMyMessageByPage(body);
        const data = res.data?.data;
        records.value = data?.records ?? [];
        total.value = Number(data?.total ?? 0);
    } finally {
        loading.value = false;
    }
    fetchUnread();
}

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
    // 审核类消息跳转「我的文章」
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

function formatDate(d?: string) {
    return d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '';
}

onMounted(fetchList);
</script>

<style scoped>
.message-center {
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

.loading {
    text-align: center;
    padding: 60px 0;
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
    border-color: rgba(56, 189, 248, 0.32);
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
    background: rgba(56, 189, 248, 0.16);
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

.pagination-wrap {
    display: flex;
    justify-content: center;
    margin-top: 10px;
}
</style>
