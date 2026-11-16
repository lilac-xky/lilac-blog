<template>
    <!-- 公告弹窗：由 useAnnouncementCenter 的 visible 统一控制开关，内容为空时展示空态 -->
    <a-modal v-if="visible" v-model:open="visible" :footer="null" :closable="true" centered :width="640"
        :mask-closable="false" wrap-class-name="announcement-modal" @cancel="handleClose">
        <!-- 头部：只放公告标识与模式提示，切换器挪到页脚，避免和右上角关闭按钮重叠 -->
        <template #title>
            <div class="announcement-head">
                <span class="announcement-badge">
                    <SoundOutlined />
                    系统公告
                </span>
                <span v-if="mode === 'all'" class="announcement-mode">全部公告</span>
            </div>
        </template>

        <!-- 空态：公告中心在没有已发布公告时展示，未读模式不会走到这里 -->
        <div v-if="!current" class="announcement-empty">
            <InboxOutlined />
            <p>暂无公告</p>
        </div>

        <!-- 正文：Markdown 渲染，超出高度内部滚动 -->
        <template v-else>
            <div class="announcement-body">
                <h3 class="announcement-title">{{ current.title }}</h3>
                <div class="announcement-time">
                    <ClockCircleOutlined />
                    {{ formatDate(current.publishTime || current.createTime) }}
                    <span v-if="currentRead" class="read-flag">已读</span>
                </div>
                <div class="announcement-md">
                    <MdPreview :editor-id="previewId" :model-value="current.content || ''" theme="dark"
                        preview-theme="vuepress" code-theme="atom" :show-code-row-number="false" />
                </div>
            </div>

            <!-- 页脚：左侧进度提示，右侧「上一条 / 页码 / 下一条」+ 已读按钮 -->
            <div class="announcement-footer">
                <span class="footer-hint">{{ footerHint }}</span>
                <div class="footer-actions">
                    <div v-if="queue.length > 1" class="announcement-switch">
                        <button class="switch-btn" :disabled="index <= 0" title="上一条" @click="goPrev">
                            <LeftOutlined />
                        </button>
                        <span class="switch-progress">{{ index + 1 }} / {{ queue.length }}</span>
                        <button class="switch-btn" :disabled="index >= queue.length - 1" title="下一条"
                            @click="goNext">
                            <RightOutlined />
                        </button>
                    </div>
                    <a-button type="primary" class="footer-btn" @click="handleRead">
                        <CheckOutlined />
                        已读
                    </a-button>
                </div>
            </div>
        </template>
    </a-modal>
</template>

<!--
  AnnouncementPopup：前台全局系统公告弹窗

  职责：
  - 两种模式共用同一个弹窗：
    * pending：首次进入自动弹出的未读模式，只展示未读公告，读完最后一条自动关闭
    * all：用户从顶部导航「公告」手动打开，展示全部已发布公告，可反复回看
  - 游客的已读状态记在 localStorage，登录用户的已读同时写服务端
  - 一次展示一条（最新优先）；多条时用页脚左右箭头切换，不与右上角关闭按钮重叠

  生命周期：
  - 挂在 BasicLayout 上，随主站布局挂载一次
  - 未读模式延迟 600ms 拉取，避免和首屏动画抢资源；公告中心模式在打开时按需拉取
-->
<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import {
    SoundOutlined,
    LeftOutlined,
    RightOutlined,
    ClockCircleOutlined,
    CheckOutlined,
    InboxOutlined,
} from '@ant-design/icons-vue';
import { MdPreview } from 'md-editor-v3';
import 'md-editor-v3/lib/preview.css';
import {
    listPendingAnnouncements,
    listPublishedAnnouncements,
    markRead,
} from '@/api/announcementController';
import { useDateFormat } from '@/composables/useDateFormat';
import { useAnnouncementRead } from '@/composables/useAnnouncementRead';
import { useAnnouncementCenter } from '@/composables/useAnnouncementCenter';
import { useUserStore } from '@/stores/user';

// MdPreview 的 editor-id 需要全局唯一，这里固定一个即可（页面内只会有一个公告弹窗实例）
const previewId = 'announcement-md';

const { formatDate } = useDateFormat();
const { readIds, isRead, markLocalRead, switchScope } = useAnnouncementRead();
const { visible, mode, autoShown } = useAnnouncementCenter();
const userStore = useUserStore();

// 已经成功同步到服务端的本地已读 id，避免每次打开页面都重复推送同一批
// 换账号或登出时要清空，否则会把上一个账号的已读记到新账号名下
let syncedReadIds = new Set<string>();

// 监听登录态：切换本地已读存储桶，并清空同步标记
// 先切桶再同步，保证推给服务端的是「这个账号自己」的本地记录
watch(
    () => userStore.loginUser?.id,
    (userId) => {
        switchScope(userId);
        syncedReadIds = new Set<string>();
    },
    { immediate: true },
);

// 本次展示的公告队列，按发布时间倒序（后端已排序）
const queue = ref<API.AnnouncementVO[]>([]);
// 当前展示的公告下标
const index = ref(0);

// 当前展示的公告；queue 为空时为 null，模板据此不渲染弹窗
const current = computed<API.AnnouncementVO | null>(() => queue.value[index.value] ?? null);

// 当前这条是否已读：登录用户优先用后端字段，游客用本地记录
const currentRead = computed(() => {
    const announcement = current.value;
    if (!announcement) return false;
    if (announcement.isRead) return true;
    return isRead(announcement.id);
});

// 页脚左侧提示：按剩余未读条数区分文案
const footerHint = computed(() => {
    if (mode.value === 'all') {
        return queue.value.length ? `共 ${queue.value.length} 条公告` : '暂无公告';
    }
    const remaining = Math.max(queue.value.length - index.value - 1, 0);
    return remaining > 0 ? `还有 ${remaining} 条公告未读` : '这是最后一条公告';
});

/**
 * 把本地已读记录同步到服务端
 * 背景：游客的已读只存在 localStorage，登录用户的已读记在服务端，这是两套独立的账本。
 * 游客关掉公告后再登录，服务端并不知道他读过，于是又会弹一次。
 * 所以在登录态下、拉取未读列表之前，先把本地已读 id 推给服务端，让两套账本合流。
 * 副作用：可能发起一次 /announcement/read 请求；已同步过的 id 会被标记，避免重复推送
 */
async function syncLocalReadToServer() {
    if (!userStore.loginUser?.token) return;
    // 只推还没同步过的 id，避免每次打开页面都重复请求
    const pending = readIds.value.filter((id) => !syncedReadIds.has(id));
    if (pending.length === 0) return;
    try {
        await markRead({ announcementIds: pending as unknown as number[] }, { silentError: true });
        // 成功后才记账，失败时留待下次重试
        pending.forEach((id) => syncedReadIds.add(id));
    } catch {
        // 同步失败不阻塞公告展示，下次进入再试
    }
}

/**
 * 拉取公告数据
 * - pending 模式只取未读，并按本地已读兜底过滤
 * - all 模式取全部已发布公告，用于回看
 * 副作用：写入 queue 并重置下标
 *
 * @returns 拉取到的公告条数，0 表示没有可展示内容
 */
async function loadAnnouncements(): Promise<number> {
    try {
        if (mode.value === 'all') {
            const res = await listPublishedAnnouncements({ silentError: true });
            queue.value = res.data?.data ?? [];
        } else {
            // 先把游客期间攒下的本地已读推给服务端，避免登录后又弹一遍
            await syncLocalReadToServer();
            const res = await listPendingAnnouncements({ silentError: true });
            const list = res.data?.data ?? [];
            // 游客只有 localStorage 一份已读记录，登录用户服务端已过滤，这里再兜一层避免重复弹
            queue.value = list.filter((item) => !isRead(item.id));
        }
        index.value = 0;
        return queue.value.length;
    } catch {
        // 公告拉取失败不打扰用户，静默跳过
        queue.value = [];
        return 0;
    }
}

/**
 * 把当前公告标记为已读：本地必写，登录用户额外写服务端
 * 副作用：写入 localStorage、可能发起一次 /announcement/read 请求
 */
async function markCurrentRead() {
    const announcement = current.value;
    if (!announcement?.id) return;
    markLocalRead([announcement.id]);
    // 游客没有 token，调用会返回 400001，这里直接跳过
    if (!userStore.loginUser?.token) return;
    try {
        await markRead({ announcementIds: [announcement.id] }, { silentError: true });
    } catch {
        // 服务端写入失败不影响弹窗交互，本地已读已记录
    }
}

/**
 * 点击「已读」：标记当前公告已读
 * - 还有下一条时自动翻页
 * - 已是最后一条则关闭弹窗（未读模式与回看模式一致）
 */
async function handleRead() {
    if (!current.value) {
        visible.value = false;
        return;
    }
    await markCurrentRead();
    if (index.value < queue.value.length - 1) {
        index.value += 1;
        return;
    }
    visible.value = false;
}

/**
 * 手动切到上一条公告（不标记已读）
 */
function goPrev() {
    if (index.value > 0) index.value -= 1;
}

/**
 * 手动切到下一条公告（不标记已读）
 */
function goNext() {
    if (index.value < queue.value.length - 1) index.value += 1;
}

/**
 * 关闭弹窗：关闭当前这条也算已读，避免下次进来又弹同一则
 */
function handleClose() {
    if (current.value) {
        markCurrentRead();
    }
    visible.value = false;
}

// 自动弹窗流程已经预加载过数据，标记一下让 watch 跳过重复请求
let preloaded = false;

// 弹窗每次打开、或模式切换时都重新拉一次数据，保证刚发布的公告能立刻看到
// 用数组监听 visible 与 mode：openAll 先改 mode 再开 visible，只会触发一次有效加载
watch([visible, mode], async () => {
    if (!visible.value) return;
    // 自动弹窗已在 onMounted 里预加载并判断过条数，这里直接用结果，避免重复请求
    if (preloaded) {
        preloaded = false;
        return;
    }
    const count = await loadAnnouncements();
    // 未读模式下没有可展示内容就立刻关闭，避免闪出一个空弹窗
    if (!count && mode.value === 'pending') {
        visible.value = false;
    }
});

onMounted(async () => {
    // 同一次布局生命周期内只自动弹一次；autoShown 只做内存标记，
    // 不写持久化存储，保证「刷新页面」仍会重新检查未读公告
    if (autoShown.value) return;

    // 延迟请求，等首屏渲染和背景动画先跑起来
    window.setTimeout(async () => {
        // 定时器期间用户可能已经手动打开公告中心，此时不再自动弹
        if (autoShown.value || visible.value) return;

        // 关键：先把数据拉回来，确认确实有未读公告再打开弹窗，
        // 否则 visible 置 true 的瞬间 current 仍为 null，会先渲染一个空壳再填充，造成闪烁
        mode.value = 'pending';
        const count = await loadAnnouncements();
        autoShown.value = true;
        if (count > 0) {
            preloaded = true;
            visible.value = true;
        }
    }, 600);
});
</script>

<style scoped>
/* 头部容器：左侧标识 + 右侧模式提示两端对齐 */
.announcement-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    /* 右侧给关闭按钮留出空间，避免「全部公告」标签被右上角 × 覆盖 */
    padding-right: 32px;
}

/* 空态容器：公告中心没有已发布公告时居中展示 */
.announcement-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 10px;
    padding: 48px 0;
    color: var(--text-muted);
    font-size: 13px;
}

/* 空态图标：放大并降低透明度，避免抢视觉 */
.announcement-empty :deep(svg) {
    font-size: 32px;
    opacity: 0.5;
}

/* 公告标识：小图标 + 文案，弱化存在感，避免抢正文视觉 */
.announcement-badge {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 600;
    color: var(--text-primary);
    letter-spacing: 0.02em;
}

/* 模式提示：回看模式下显示，弱化为辅助信息 */
.announcement-mode {
    font-size: 12px;
    color: var(--text-muted);
    padding: 2px 8px;
    border-radius: var(--radius-pill);
    background: rgba(var(--accent-rgb), 0.12);
}

/* 正文容器：限制最大高度并内部滚动，长公告不会撑破视口 */
.announcement-body {
    max-height: 56vh;
    overflow-y: auto;
    padding-right: 4px;
}

/* 公告标题：作为弹窗正文的视觉主体 */
.announcement-title {
    margin: 0 0 6px;
    font-size: 18px;
    font-weight: 700;
    line-height: 1.45;
    color: var(--text-primary);
}

/* 发布时间：弱化的辅助信息，与标题拉开层级 */
.announcement-time {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 14px;
    font-size: 12px;
    color: var(--text-muted);
}

/* 已读标记：回看模式下提示该条已读，颜色比正文更淡 */
.read-flag {
    margin-left: 6px;
    padding: 0 6px;
    border-radius: var(--radius-pill);
    background: rgba(255, 255, 255, 0.08);
    color: var(--text-muted);
    font-size: 11px;
    line-height: 18px;
}

/* Markdown 容器：继承站点字体与行高 */
.announcement-md {
    font-size: 14px;
    line-height: 1.85;
}

/* 页脚：左侧提示，右侧切换器与按钮同一行，关闭按钮在头部不再冲突 */
.announcement-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-top: 18px;
    padding-top: 14px;
    border-top: 1px solid var(--border-soft);
}

/* 页脚提示：小字灰色，不干扰按钮 */
.footer-hint {
    font-size: 12px;
    color: var(--text-muted);
}

/* 右侧操作区：切换器与已读按钮横向排列 */
.footer-actions {
    display: inline-flex;
    align-items: center;
    gap: 14px;
}

/* 切换器：多条公告时出现，紧挨已读按钮左侧 */
.announcement-switch {
    display: inline-flex;
    align-items: center;
    gap: 6px;
}

/* 切换按钮：圆形描边小按钮，禁用时降低透明度 */
.switch-btn {
    width: 26px;
    height: 26px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    border: 1px solid var(--border-soft);
    background: transparent;
    color: var(--text-secondary);
    font-size: 11px;
    cursor: pointer;
    transition: all var(--duration-fast);
}

/* 切换按钮悬停态：主色描边提亮，明确可点击 */
.switch-btn:hover:not(:disabled) {
    border-color: var(--border-accent-strong);
    color: var(--accent);
}

/* 切换按钮禁用态：首尾边界不再响应点击 */
.switch-btn:disabled {
    opacity: 0.35;
    cursor: not-allowed;
}

/* 进度文本：等宽数字，避免左右切换时宽度跳动 */
.switch-progress {
    min-width: 44px;
    text-align: center;
    font-size: 12px;
    color: var(--text-muted);
    font-variant-numeric: tabular-nums;
}

/* 已读按钮：继承项目胶囊按钮样式，宽度自适应 */
.footer-btn {
    min-width: 92px;
}
</style>

<style>
/* ===== 以下为弹窗 teleport 到 body 后的全局样式，必须非 scoped 才能命中 ===== */

/* 弹窗主体：玻璃拟态 + 主色描边，与前台卡片风格一致 */
.announcement-modal .ant-modal-content {
    border-radius: var(--radius-card);
    border: 1px solid var(--border-accent);
    background: rgba(18, 22, 34, 0.92);
    backdrop-filter: blur(var(--blur)) saturate(1.2);
    -webkit-backdrop-filter: blur(var(--blur)) saturate(1.2);
    box-shadow: var(--shadow-card);
}

/* 弹窗头部：去掉默认底部分割线，避免和自定义头部叠出双线 */
.announcement-modal .ant-modal-header {
    background: transparent;
    margin-bottom: 12px;
}

/* 关闭按钮：跟随站点次级文字色，悬停提亮 */
.announcement-modal .ant-modal-close {
    color: var(--text-muted);
}

/* 关闭按钮悬停态：主色提示可关闭 */
.announcement-modal .ant-modal-close:hover {
    color: var(--accent);
}

/* Markdown 预览外壳：透明化，让内容直接落在玻璃卡上 */
.announcement-modal .md-editor,
.announcement-modal .md-editor-dark,
.announcement-modal .md-editor-preview-wrapper,
.announcement-modal .md-editor-content {
    background: transparent !important;
}

/* Markdown 预览内边距：由外层容器统一控制，去掉编辑器默认留白 */
.announcement-modal .md-editor-preview-wrapper {
    padding: 0 !important;
}

/* Markdown 预览排版：暗色主题变量对齐站点配色（表格、代码块、链接） */
.announcement-modal .md-editor-preview {
    background: transparent !important;
    color: var(--text-secondary);
    font-size: 14px;
    line-height: 1.85;
    --md-theme-table-stripe-color: rgba(255, 255, 255, 0.03);
    --md-theme-table-tr-bg-color: transparent;
    --md-theme-table-td-border-color: rgba(255, 255, 255, 0.1);
    --md-theme-code-block-bg-color: rgba(32, 38, 56, 0.78);
    --md-theme-code-before-bg-color: rgba(32, 38, 56, 0.78);
    --md-theme-code-block-color: #c8d3e6;
    --md-theme-code-block-radius: 12px;
    --md-theme-code-inline-color: var(--accent-light);
    --md-theme-code-inline-bg-color: rgba(var(--accent-rgb), 0.14);
    --md-theme-code-inline-radius: 5px;
    --md-theme-link-color: var(--accent-light);
    --md-theme-link-hover-color: var(--accent-pink);
    --md-theme-border-color: rgba(255, 255, 255, 0.1);
}

/* 标题层级：公告正文内的标题统一白字，避免和弹窗标题混淆 */
.announcement-modal .md-editor-preview h1,
.announcement-modal .md-editor-preview h2,
.announcement-modal .md-editor-preview h3,
.announcement-modal .md-editor-preview h4 {
    color: var(--text-primary);
}

/* 引用块：加一条主色竖线，弱化默认灰底 */
.announcement-modal .md-editor-preview blockquote {
    border-left: 3px solid var(--border-accent-strong);
    background: rgba(var(--accent-rgb), 0.06);
    color: var(--text-secondary);
}

/* 正文滚动条：细条深色，避免默认白色滚动条破坏暗色弹窗 */
.announcement-modal .announcement-body::-webkit-scrollbar {
    width: 6px;
}

/* 滚动条滑块：低透明度主色，悬停时加深 */
.announcement-modal .announcement-body::-webkit-scrollbar-thumb {
    background: rgba(var(--accent-rgb), 0.28);
    border-radius: var(--radius-pill);
}
</style>
