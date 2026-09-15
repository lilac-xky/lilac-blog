<template>
    <div class="spark-page" ref="sparkPageEl">
        <!-- 页头：大标题「拾灵集」+ 一句话副标题，中间不放任何头像 -->
        <header class="spark-hero">
            <h1 class="hero-title">拾灵集</h1>
            <p class="hero-subtitle">宇宙中寻觅灵感</p>
        </header>

        <!-- 搜索框：按灵感内容模糊匹配，回车或停顿后自动检索 -->
        <SearchBar v-model="keyword" class="spark-search" placeholder="搜寻散落的灵感…" @search="onSearch" />

        <!-- 排序切换：最新 / 最早，放大成页面主操作 -->
        <div class="sort-bar">
            <div class="sort-toggle">
                <button type="button" class="sort-btn" :class="{ active: sortOrder === 'descend' }"
                    @click="changeSort('descend')">
                    <RiseOutlined />
                    <span>最新</span>
                </button>
                <button type="button" class="sort-btn" :class="{ active: sortOrder === 'ascend' }"
                    @click="changeSort('ascend')">
                    <FallOutlined />
                    <span>最早</span>
                </button>
            </div>
        </div>

        <!-- 首屏加载：还没有任何数据时才占位 -->
        <div v-if="loading && !records.length" class="loading list-loading">
            <a-spin />
        </div>

        <!-- 双列瀑布流：两列各自堆叠，卡片按实际高度落进当前更矮的一列 -->
        <div v-else-if="records.length" class="spark-flow" ref="flowEl">
            <div class="flow-col">
                <SparkCard v-for="s in leftColumn" :key="s.id" :spark="s" />
            </div>
            <div class="flow-col">
                <SparkCard v-for="s in rightColumn" :key="s.id" :spark="s" />
            </div>
        </div>

        <ListEmpty v-else :text="keyword ? '没有匹配的灵感' : '还没有拾到灵感'">
            <template #icon>
                <BulbOutlined />
            </template>
        </ListEmpty>

        <!-- 底部状态：加载中 / 到底了 / 加载失败可重试 -->
        <div v-if="records.length" class="flow-foot">
            <template v-if="loading">
                <a-spin size="small" />
                <span>正在拾取更多…</span>
            </template>
            <template v-else-if="errorText">
                <span class="foot-error">{{ errorText }}</span>
                <button type="button" class="foot-retry" @click="loadAll">重新加载</button>
            </template>
            <template v-else>
                <span class="foot-line"></span>
                <span>已收录 {{ records.length }} 条，没有更多了</span>
                <span class="foot-line"></span>
            </template>
        </div>

        <!-- 隐藏测量层：卡片在单独列里量出真实高度，用于把两列摆得更均衡 -->
        <div class="measure-layer" ref="measureEl" aria-hidden="true">
            <div v-for="s in records" :key="`m-${s.id}`" class="measure-item">
                <SparkCard :spark="s" />
            </div>
        </div>
    </div>
</template>

<!--
  SparkCollection：前台「拾灵集」页（灵感 = spark）
  - 页头只有大标题「拾灵集」与副标题「宇宙中寻觅灵感」，中间不出现头像
  - 搜索框按灵感内容（后端 content 模糊查询）检索，输入停顿或回车后重新拉取
  - 排序只有「最新 / 最早」两种，切换后整条时间线重新拉取
  - 刻意不分页：沿用后端分页接口按 20 条一页连续取完，页面上只呈现一条连续时间线
  - 双列瀑布流：左列 / 右列各自堆叠，卡片按真实渲染高度落进当前更矮的一列
  - 卡片只保留 头像(链主页) / Lilac / 日期 / 正文，不含定位与评论
-->
<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import { BulbOutlined, FallOutlined, RiseOutlined } from '@ant-design/icons-vue';
import { listSparkVoByPage } from '@/api/sparkController';
import SearchBar from '@/components/SearchBar.vue';
import ListEmpty from '@/components/ListEmpty.vue';
import SparkCard from '@/components/SparkCard.vue';

/* 后端 /spark/list/page/vo 单次最多返回 20 条，取满即视为还有下一页 */
const PAGE_SIZE = 20;

/* 连续拉取的安全上限：最多 100 页（2000 条），避免异常数据把页面拖死 */
const MAX_PAGE = 100;

/* 每字预估高度（px）：只用于首次渲染前的粗排，渲染后会按真实高度重排 */
const CONTENT_LINE_HEIGHT = 26;

/* 卡片固定部分高度（px）：内边距 + 头像行 + 正文外边距 */
const CARD_BASE_HEIGHT = 96;

/* 判定「新排布明显更均衡」的最小改善幅度（px）：不够就不换列，避免布局来回跳 */
const REBALANCE_THRESHOLD = 24;

/* 页面容器与瀑布流容器：前者监听整页宽度，后者监听变成单列后的高度变化 */
const sparkPageEl = ref<HTMLElement | null>(null);
const flowEl = ref<HTMLElement | null>(null);

/* 隐藏测量层：卡片以全宽渲染一次，量出每张卡片的真实高度 */
const measureEl = ref<HTMLElement | null>(null);

/* 全部灵感：一条线到底，不再分页 */
const records = ref<API.SparkVO[]>([]);

/* 左右两列内容，由 rebalanceColumns 按卡片高度分配 */
const leftColumn = ref<API.SparkVO[]>([]);
const rightColumn = ref<API.SparkVO[]>([]);

const loading = ref(false);
const errorText = ref('');
const sortOrder = ref<'descend' | 'ascend'>('descend');

/* 搜索关键词：非空时作为 content 条件传给后端做模糊匹配 */
const keyword = ref('');

/* 请求序号：排序切换、搜索或重新加载会自增，旧请求回来时据此丢弃结果 */
const requestId = ref(0);

/* 重排节流标记：一次重排未跑完前不再重复排队 */
let rebalancePending = false;
let rebalanceFrame = 0;
let resizeTimer: number | null = null;
let resizeObserver: ResizeObserver | null = null;

// 读取一条灵感的时间：缺失时返回 0，保证排序不会抛错
function timeOf(s: API.SparkVO): number {
    const t = s.createTime ? new Date(s.createTime).getTime() : 0;
    return Number.isNaN(t) ? 0 : t;
}

// 按选定顺序排列：最新在前或最早在前，时间相同的保持原有相对次序
function applySortOrder(list: API.SparkVO[]): API.SparkVO[] {
    const direction = sortOrder.value === 'descend' ? -1 : 1;
    return [...list].sort((a, b) => (timeOf(a) - timeOf(b)) * direction);
}

// 按正文长度粗估卡片高度：只在真实渲染前给两列一个大致均衡的起点
function estimateHeight(s: API.SparkVO): number {
    const length = (s.content ?? '').length;
    const lines = Math.max(1, Math.ceil(length / 34));
    return CARD_BASE_HEIGHT + lines * CONTENT_LINE_HEIGHT;
}

// 按估算高度贪心分列：每次把下一条放进当前累计高度更小的列
function splitByEstimate(list: API.SparkVO[]): { left: API.SparkVO[]; right: API.SparkVO[] } {
    const left: API.SparkVO[] = [];
    const right: API.SparkVO[] = [];
    let leftHeight = 0;
    let rightHeight = 0;
    list.forEach((s) => {
        const h = estimateHeight(s);
        if (leftHeight <= rightHeight) {
            left.push(s);
            leftHeight += h;
        } else {
            right.push(s);
            rightHeight += h;
        }
    });
    return { left, right };
}

// 把整批灵感重排进左右两列：先按估算分列，真实高度稍后由 rebalanceColumns 校正
function resetColumns() {
    const { left, right } = splitByEstimate(applySortOrder(records.value));
    leftColumn.value = left;
    rightColumn.value = right;
}

// 拉取单页灵感，返回这一页数据与服务端给出的总数；关键词非空时按内容模糊匹配
async function fetchSparkPage(current: number): Promise<{ list: API.SparkVO[]; total: number }> {
    const body: API.SparkQueryRequest = {
        current,
        pageSize: PAGE_SIZE,
        sortOrder: sortOrder.value,
    };
    const trimmed = keyword.value.trim();
    if (trimmed) body.content = trimmed;
    const res = await listSparkVoByPage(body);
    const data = res.data?.data;
    return {
        list: data?.records ?? [],
        total: Number(data?.total ?? 0),
    };
}

// 连续拉完所有灵感：从第 1 页开始逐页取，直到不足一页或达到安全上限
async function loadAll() {
    const myRequest = ++requestId.value;
    loading.value = true;
    errorText.value = '';
    records.value = [];
    leftColumn.value = [];
    rightColumn.value = [];
    try {
        let page = 1;
        let total = 0;
        let more = true;
        while (more && page <= MAX_PAGE) {
            const res = await fetchSparkPage(page);
            // 期间用户切了排序、改了关键词或点了重试：丢弃这次过期结果
            if (myRequest !== requestId.value) return;
            records.value = records.value.concat(res.list);
            total = res.total;
            more = res.list.length >= PAGE_SIZE && records.value.length < total;
            page += 1;
        }
        records.value = applySortOrder(records.value);
        resetColumns();
        // 等服务端数据落进 DOM 后再校正两列高度
        await nextTick();
        if (myRequest === requestId.value) rebalanceColumns();
    } catch {
        if (myRequest === requestId.value) {
            errorText.value = records.value.length ? '后续灵感加载失败' : '灵感加载失败，请稍后再试';
        }
    } finally {
        if (myRequest === requestId.value) loading.value = false;
    }
}

// 切换排序：重置时间线并重新拉取全部灵感
function changeSort(order: 'descend' | 'ascend') {
    if (sortOrder.value === order) return;
    sortOrder.value = order;
    loadAll();
}

// 搜索回调：关键词变化时同步到查询条件并重新拉取；重复触发同一个词直接忽略
function onSearch(value: string) {
    const next = value.trim();
    if (next === keyword.value.trim()) return;
    keyword.value = next;
    loadAll();
}

// 在 DOM 更新后按卡片真实高度重排两列，形成高度接近的瀑布流
function rebalanceColumns() {
    const cards = measureEl.value?.querySelectorAll<HTMLElement>('.measure-item');
    if (!cards?.length) return;
    // 隐藏层宽度与真实列宽一致，量到的卡片高度可直接用于分列
    const rawHeights = Array.from(cards, (el) => el.getBoundingClientRect().height);
    // 高度按 ±12px 分档：档位相同的卡片会被视作等高，避免一两像素的差异让卡片来回换列
    const heights = rawHeights.map((h) => Math.round(h / 24) * 24);
    if (rawHeights.length !== records.value.length) return;
    // 先累计每张卡片的落位高度，最后一次成对写入，避免中间态触发多余渲染
    const nextLeft: API.SparkVO[] = [];
    const nextRight: API.SparkVO[] = [];
    let leftHeight = 0;
    let rightHeight = 0;
    records.value.forEach((s, index) => {
        const h = heights[index] || estimateHeight(s);
        if (leftHeight <= rightHeight) {
            nextLeft.push(s);
            leftHeight += h;
        } else {
            nextRight.push(s);
            rightHeight += h;
        }
    });
    const nextDiff = Math.abs(leftHeight - rightHeight);
    // 当前排布的「精确」高度差：用同一批实测高度按当前分列顺序累加
    const rowsById = new Map(records.value.map((s, index) => [s.id, heights[index] || estimateHeight(s)]));
    let currentLeftHeight = 0;
    let currentRightHeight = 0;
    leftColumn.value.forEach((s) => (currentLeftHeight += rowsById.get(s.id) ?? 0));
    rightColumn.value.forEach((s) => (currentRightHeight += rowsById.get(s.id) ?? 0));
    const currentDiff = Math.abs(currentLeftHeight - currentRightHeight);
    // 当前排布缺失（首轮渲染）或新排布明显更均衡时才换；否则保持现状，杜绝来回跳动
    const isEmpty = !leftColumn.value.length && !rightColumn.value.length;
    const shouldSwitch = isEmpty || nextDiff < currentDiff - REBALANCE_THRESHOLD;
    if (!shouldSwitch) return;
    leftColumn.value = nextLeft;
    rightColumn.value = nextRight;
}

// 合并同一帧内的多次重排请求，避免一次性搬动大量卡片造成卡顿
function scheduleRebalance() {
    if (rebalancePending) return;
    rebalancePending = true;
    rebalanceFrame = requestAnimationFrame(() => {
        rebalancePending = false;
        rebalanceColumns();
    });
}

// 监听容器尺寸：窗口缩放或瀑布流变成单列后重新校正两列高度
function observeResize() {
    if (typeof ResizeObserver === 'undefined') return;
    resizeObserver = new ResizeObserver(() => {
        if (resizeTimer) window.clearTimeout(resizeTimer);
        resizeTimer = window.setTimeout(() => scheduleRebalance(), 200);
    });
    if (flowEl.value) resizeObserver.observe(flowEl.value);
    if (sparkPageEl.value) resizeObserver.observe(sparkPageEl.value);
}

onMounted(() => {
    loadAll();
    observeResize();
});

onBeforeUnmount(() => {
    // 自增请求序号，让在途请求回来后直接作废
    requestId.value += 1;
    if (resizeObserver) resizeObserver.disconnect();
    if (resizeTimer) window.clearTimeout(resizeTimer);
    if (rebalanceFrame) cancelAnimationFrame(rebalanceFrame);
});
</script>

<style scoped>
/* 页面主容器：页头、搜索、排序、统计、瀑布流之间的统一留白 */
.spark-page {
    display: flex;
    flex-direction: column;
    gap: 22px;
}

/* 页头：整块居中，只放主标题与副标题；上方留白加大，让大字与导航栏离远一点 */
.spark-hero {
    display: flex;
    flex-direction: column;
    align-items: center;
    /* 标题与副标题贴紧一些 */
    gap: 8px;
    padding: 48px 0 4px;
}

/* 主标题「拾灵集」：纯白大字，字距拉开 */
.hero-title {
    font-size: 64px;
    font-weight: 800;
    line-height: 1.1;
    color: #ffffff;
    /* 字距 + 等量缩进：保证大字在页面上依然居中 */
    letter-spacing: 0.36em;
    text-indent: 0.36em;
    text-shadow: 0 4px 24px rgba(0, 0, 0, 0.45), 0 0 32px rgba(var(--accent-rgb), 0.22);
}

/* 副标题：斜体小字，紧贴大标题下方 */
.hero-subtitle {
    font-size: 12px;
    font-style: italic;
    color: var(--text-secondary);
    letter-spacing: 0.24em;
    text-indent: 0.24em;
}

/* 搜索框：收窄居中，与上方小字拉开一段距离 */
.spark-search {
    max-width: 560px;
    margin: 34px auto 0;
}

/* 排序行：居中放置最新/最早切换 */
.sort-bar {
    display: flex;
    justify-content: center;
}

/* 排序切换容器：胶囊底 + 内嵌两个小号按钮 */
.sort-toggle {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 3px;
    border-radius: var(--radius-pill);
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid var(--border-soft);
    backdrop-filter: blur(var(--blur));
    -webkit-backdrop-filter: blur(var(--blur));
}

/* 单个排序按钮：比原始尺寸小，选中时为主色实底 */
.sort-btn {
    display: inline-flex;
    align-items: center;
    gap: 5px;
    padding: 8px 20px;
    border: none;
    border-radius: var(--radius-pill);
    background: transparent;
    color: var(--text-secondary);
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
}

.sort-btn :deep(.anticon) {
    font-size: 15px;
}

.sort-btn:hover {
    color: var(--text-primary);
}

/* 选中态：主色实底 + 光晕，与未选中形成强对比 */
.sort-btn.active {
    background: var(--accent);
    color: #fff;
    box-shadow: 0 4px 14px rgba(var(--accent-rgb), 0.38);
}

/* 双列瀑布流：两列各自独立堆叠，不再按奇偶左右交替 */
.spark-flow {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 22px;
    align-items: start;
    /* 与上方排序按钮拉开距离，卡片不至于贴着操作区 */
    margin-top: 18px;
}

/* 单列容器：纵向排列卡片 */
.flow-col {
    display: flex;
    flex-direction: column;
    gap: 22px;
    min-width: 0;
}

/* 底部状态条：加载中 / 到底了 / 失败重试 */
.flow-foot {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    padding: 6px 0 10px;
    color: var(--text-muted);
    font-size: 12.5px;
}

/* 到底了两侧的装饰线：中间渐隐，强调时间线已经收尾 */
.foot-line {
    width: 54px;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(var(--accent-rgb), 0.5), transparent);
}

/* 加载失败文案：偏红提示，与「到底了」区分开 */
.foot-error {
    color: #fca5a5;
}

/* 重新加载按钮：小胶囊，失败后给一个明确出口 */
.foot-retry {
    padding: 3px 14px;
    border-radius: var(--radius-pill);
    border: 1px solid var(--border-accent);
    background: var(--accent-soft);
    color: var(--accent-light);
    font-size: 12px;
    cursor: pointer;
    transition: all 0.2s ease;
}

.foot-retry:hover {
    background: rgba(var(--accent-rgb), 0.24);
    border-color: var(--border-accent-strong);
}

/* 隐藏测量层：卡片全宽渲染但不显示也不响应交互，仅用于量高度 */
.measure-layer {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    visibility: hidden;
    pointer-events: none;
    z-index: -1;
}

/* 测量项：与真实列宽一致的单个卡片槽位 */
.measure-item {
    width: 100%;
}

@media (max-width: 720px) {
    /* 移动端：页头上方留白收一半 */
    .spark-hero {
        padding: 28px 0 4px;
    }

    /* 移动端：瀑布流退化成单列，卡片顺序即为时间顺序 */
    .spark-flow {
        display: flex;
        flex-direction: column;
        gap: 18px;
        margin-top: 4px;
    }

    /* 移动端标题降一档，字距同步收窄，避免三个大字被挤出屏宽 */
    .hero-title {
        font-size: 40px;
        letter-spacing: 0.28em;
        text-indent: 0.28em;
    }

    .hero-subtitle {
        font-size: 13px;
        letter-spacing: 0.18em;
        text-indent: 0.18em;
    }

    /* 移动端搜索框上方留白收窄 */
    .spark-search {
        margin: 24px auto 0;
    }
}
</style>
