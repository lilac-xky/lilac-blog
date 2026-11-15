<template>
    <div class="note-page">
        <PageHeader title="碎知" subtitle="记下零散的知识点，定期整理成一篇博客" />

        <!-- 顶部操作栏：左侧统计与筛选，右侧记一笔与整理入口 -->
        <div class="note-toolbar glass-card">
            <div class="toolbar-stats">
                <span class="stat-chip">
                    <span class="stat-num">{{ rawTotal }}</span>
                    <span class="stat-label">待整理</span>
                </span>
                <span class="stat-chip">
                    <span class="stat-num">{{ archivedTotal }}</span>
                    <span class="stat-label">已归档</span>
                </span>
            </div>
            <div class="toolbar-actions">
                <a-button @click="quickNoteOpen = true">
                    <EditOutlined />
                    记一笔
                </a-button>
                <a-button type="primary" :disabled="selectedIds.length === 0" @click="goCompose">
                    <ThunderboltOutlined />
                    整理成文章{{ selectedIds.length > 0 ? ` (${selectedIds.length})` : '' }}
                </a-button>
            </div>
        </div>

        <!-- 筛选栏：状态 Tab + 主题下拉 + 关键词搜索 -->
        <div class="note-filter">
            <div class="filter-tabs">
                <button v-for="tab in STATUS_TABS" :key="tab.value" type="button" class="filter-tab"
                    :class="{ active: query.status === tab.value }" @click="changeStatus(tab.value)">
                    {{ tab.label }}
                </button>
            </div>
            <!-- 主题筛选包一层，用于监听「鼠标离开这块区域」，实现移出即收起 -->
            <div class="filter-tag-wrap" @mouseenter="filterTagSelect.cancelClose"
                @mouseleave="filterTagSelect.scheduleClose">
                <a-select v-model:value="query.tagIds" mode="multiple" class="filter-tag" placeholder="按主题筛选"
                    :options="tagOptions" allow-clear :max-tag-count="2" :open="filterTagSelect.open"
                    popup-class-name="note-tag-dropdown" @dropdown-visible-change="filterTagSelect.onVisibleChange"
                    @change="reload" />
            </div>
            <a-input v-model:value="query.content" class="filter-search" placeholder="搜索内容…" allow-clear
                @press-enter="reload" />
        </div>

        <!-- 全选行：只在有数据时出现，方便一键全选待整理的碎片 -->
        <div v-if="records.length > 0" class="select-all-row">
            <a-checkbox :checked="allSelected" :indeterminate="partiallySelected" @change="toggleSelectAll">
                全选本页
            </a-checkbox>
            <span class="select-hint">已选 {{ selectedIds.length }} 条</span>
        </div>

        <!-- 碎片列表：每条为一张卡片，勾选后进入整理 -->
        <div v-if="loading && records.length === 0" class="note-loading">
            <a-spin />
        </div>

        <div v-else-if="records.length > 0" class="note-list">
            <article v-for="note in records" :key="note.id" class="note-card glass-card"
                :class="{ selected: isSelected(toId(note.id)) }">
                <a-checkbox class="note-check" :checked="isSelected(toId(note.id))"
                    @change="toggleSelect(toId(note.id))" />
                <div class="note-main">
                    <p class="note-content">{{ note.content }}</p>
                    <!-- 一句话理解：有才渲染，用引用块与原文拉开层级 -->
                    <blockquote v-if="note.insight" class="note-insight">
                        {{ note.insight }}
                    </blockquote>
                    <!-- 元信息行：主题标签 / 日期 / 来源 / 归档状态 -->
                    <div class="note-meta">
                        <a-tag v-for="tag in note.tags" :key="tag.id" class="note-tag">
                            {{ tag.tagName }}
                        </a-tag>
                        <span class="meta-item">
                            <ClockCircleOutlined />
                            {{ formatDateOnly(note.createTime) }}
                        </span>
                        <span v-if="note.source" class="meta-item meta-source" :title="note.source">
                            <LinkOutlined />
                            {{ note.source }}
                        </span>
                        <a-tag v-if="note.status === 1" color="green" class="meta-status">已归档</a-tag>
                    </div>
                </div>
                <div class="note-actions">
                    <a-button type="text" size="small" @click="openEdit(note)">
                        <EditOutlined />
                    </a-button>
                    <a-button type="text" size="small" danger @click="confirmDelete(note)">
                        <DeleteOutlined />
                    </a-button>
                </div>
            </article>
        </div>

        <ListEmpty v-else :text="emptyText">
            <template #icon>
                <BulbOutlined />
            </template>
        </ListEmpty>

        <!-- 分页：数据量少时隐藏，避免空页脚干扰 -->
        <div v-if="total > pageSize" class="note-pager">
            <a-pagination v-model:current="current" :page-size="pageSize" :total="total" :show-size-changer="false"
                @change="reload" />
        </div>

        <!-- 随手记弹层：保存后刷新当前列表与统计 -->
        <QuickNote v-model:open="quickNoteOpen" @saved="onNoteSaved" />

        <!-- 编辑弹窗：复用同一个表单结构，但走 updateNote -->
        <a-modal v-model:open="editOpen" title="编辑碎片" :confirm-loading="editSaving" ok-text="保存" cancel-text="取消"
            wrap-class-name="note-edit-modal" @ok="submitEdit">
            <div class="edit-body">
                <a-textarea v-model:value="editForm.content" :rows="4" placeholder="知识点正文" :maxlength="10000" show-count />
                <div class="edit-tag-wrap" @mouseenter="editTagSelect.cancelClose"
                    @mouseleave="editTagSelect.scheduleClose">
                    <a-select v-model:value="editForm.tagIds" mode="multiple" placeholder="主题（可留空）"
                        :options="tagOptions" allow-clear :max-tag-count="3" :open="editTagSelect.open"
                        popup-class-name="note-tag-dropdown" @dropdown-visible-change="editTagSelect.onVisibleChange" />
                </div>
                <a-input v-model:value="editForm.source" placeholder="来源（书名 / 链接 / 课程）" :maxlength="255" allow-clear />
                <a-textarea v-model:value="editForm.insight" :rows="2" placeholder="我的一句话理解（可选）" :maxlength="500"
                    show-count />
            </div>
        </a-modal>
    </div>
</template>

<!--
  NoteList：前台「碎知」碎片列表页
  - 全部私有，列表接口按当前登录用户隔离，页面不提供任何可见性开关
  - 勾选若干条碎片后跳转整理预览页，把选中 id 通过 query 传过去
  - 状态 Tab（全部 / 待整理 / 已归档）+ 主题多选 + 内容关键词三类筛选
  - 编辑与删除只作用于自己的碎片，后端会再校验一次归属
-->
<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import {
    BulbOutlined,
    ClockCircleOutlined,
    DeleteOutlined,
    EditOutlined,
    LinkOutlined,
    ThunderboltOutlined,
} from '@ant-design/icons-vue';
import { deleteNote, getNote, listMyNoteByPage, topicStat, updateNote } from '@/api/noteController';
import { listTagByPageVo } from '@/api/tagController';
import PageHeader from '@/components/PageHeader.vue';
import ListEmpty from '@/components/ListEmpty.vue';
import QuickNote from '@/components/QuickNote.vue';
import { useDateFormat } from '@/composables/useDateFormat';
import { useHoverCollapseSelect } from '@/composables/useHoverCollapseSelect';

const router = useRouter();
const { formatDateOnly } = useDateFormat();

/* 状态筛选选项：undefined 表示不限状态 */
const STATUS_TABS: Array<{ label: string; value: number | undefined }> = [
    { label: '全部', value: undefined },
    { label: '待整理', value: 0 },
    { label: '已归档', value: 1 },
];

/* 列表数据与分页状态 */
const records = ref<API.NoteVO[]>([]);
const total = ref(0);
const current = ref(1);
const pageSize = ref(15);
const loading = ref(false);

/* 待整理 / 已归档统计，来自 topic/stat 接口 */
const rawTotal = ref(0);
const archivedTotal = ref(0);

/* 查询条件：与 NoteQueryRequest 对应，userId 由后端强制写入。
 * tagIds 为雪花 ID，按字符串承载避免精度丢失 */
const query = ref<{ status: number | undefined; tagIds: string[]; content: string }>({
    status: undefined,
    tagIds: [],
    content: '',
});

/* 主题下拉选项：value 统一为字符串，与 tagIds 语义一致 */
const tagOptions = ref<Array<{ label: string; value: string }>>([]);

/* 主题筛选下拉的「移出即收起」控制 */
const filterTagSelect = useHoverCollapseSelect({ closeDelay: 180 });

/* 编辑弹窗内主题下拉的「移出即收起」控制，与筛选栏同一套逻辑 */
const editTagSelect = useHoverCollapseSelect({ closeDelay: 180 });

/* 选中的碎片 id：仅在同一次筛选结果内有效。
 * 雪花 ID 超过 JS 安全整数范围，全程用字符串保存，避免转成 number 后末位被改写 */
const selectedIds = ref<string[]>([]);

/* 随手记与编辑弹层的开关及状态。
 * id 与 tagIds 均为雪花 ID，必须用字符串承载，转 number 会丢失末位精度 */
const quickNoteOpen = ref(false);
const editOpen = ref(false);
const editSaving = ref(false);
const editForm = ref<{ id?: string; content: string; tagIds: string[]; source: string; insight: string }>({
    id: undefined,
    content: '',
    tagIds: [],
    source: '',
    insight: '',
});

/* 空态文案：按当前筛选条件给出不同引导 */
const emptyText = computed(() => {
    if (query.value.content.trim()) return '没有匹配的碎片';
    if (query.value.status === 1) return '还没有归档过碎片';
    if (query.value.status === 0) return '待整理里空空如也';
    return '还没有记下任何碎片，点右上角「记一笔」开始';
});

/* 本页是否全选 */
const allSelected = computed(
    () => records.value.length > 0 && records.value.every((n) => isSelected(toId(n.id))),
);

/* 是否部分选中：用于全选框的半选态 */
const partiallySelected = computed(
    () => selectedIds.value.length > 0 && !allSelected.value,
);

/**
 * 把可能为 number 类型的 ID 统一转成字符串
 * 后端 Long 以字符串收发，但 Apifox 生成的类型声明为 number；
 * 这里用 String() 取值，保证比较与传递都基于原始字符串，不丢精度。
 *
 * @param id 原始 id 值
 * @returns 字符串 id，缺失时返回空串
 */
function toId(id?: number | string): string {
    return id === undefined || id === null ? '' : String(id);
}

// 判断某条碎片是否被选中：id 统一按字符串比较，规避雪花 ID 精度问题
function isSelected(id: string) {
    return id !== '' && selectedIds.value.includes(id);
}

// 切换单条选中态：id 以字符串存进 selectedIds，不做数值转换
function toggleSelect(id: string) {
    if (id === '') return;
    const index = selectedIds.value.indexOf(id);
    if (index >= 0) {
        selectedIds.value.splice(index, 1);
    } else {
        selectedIds.value.push(id);
    }
}

// 全选 / 取消全选本页：id 全部按字符串收集
function toggleSelectAll() {
    if (allSelected.value) {
        selectedIds.value = [];
    } else {
        selectedIds.value = records.value
            .map((n) => toId(n.id))
            .filter((id) => id !== '');
    }
}

/**
 * 拉取碎片列表
 * 副作用：写入 records / total；清空选中态，避免跨页残留无效 id
 */
async function fetchList() {
    loading.value = true;
    try {
        const res = await listMyNoteByPage({
            current: current.value,
            pageSize: pageSize.value,
            sortOrder: 'descend',
            status: query.value.status,
            content: query.value.content.trim() || undefined,
            // tagIds 运行时为字符串数组，仅断言绕过 Apifox 生成类型，不做数值转换
            tagIds: query.value.tagIds.length > 0
                ? (query.value.tagIds as unknown as number[])
                : undefined,
        });
        const data = res.data?.data;
        records.value = data?.records ?? [];
        total.value = Number(data?.total ?? 0);
        // 列表刷新后旧的选中项可能已不在当前页，统一清空更安全
        selectedIds.value = [];
    } finally {
        loading.value = false;
    }
}

/**
 * 拉取主题统计
 * 副作用：写入 rawTotal / archivedTotal，用于顶部统计条
 */
async function fetchStat() {
    try {
        const res = await topicStat({ silentError: true });
        const data = res.data?.data;
        rawTotal.value = Number(data?.rawTotal ?? 0);
        archivedTotal.value = Number(data?.archivedTotal ?? 0);
    } catch {
        // 统计失败不影响主流程
    }
}

/**
 * 拉取主题下拉选项
 * 副作用：写入 tagOptions；失败时保持空列表。
 * 注意：后端 Long 实际以字符串返回，这里统一 String() 收窄，
 * 避免后端返回字符串、前端按 number 比较导致选不中。
 */
async function loadTags() {
    try {
        const res = await listTagByPageVo({ current: 1, pageSize: 200 }, { silentError: true });
        const list = res.data?.data?.records ?? [];
        tagOptions.value = list.map((t) => ({
            label: String(t.tagName ?? ''),
            value: String(t.id ?? ''),
        }));
    } catch {
        // 标签拉取失败时筛选下拉为空，不影响列表
    }
}

/**
 * 重新加载列表与统计
 * 副作用：重置到第一页后拉数据，并同步刷新顶部统计
 */
function reload() {
    current.value = 1;
    fetchList();
    fetchStat();
}

// 切换状态 Tab：先重置页码再拉取
function changeStatus(status: number | undefined) {
    if (query.value.status === status) return;
    query.value.status = status;
    reload();
}

// 随手记保存成功：刷新列表与统计，保持当前筛选上下文
function onNoteSaved() {
    fetchList();
    fetchStat();
}

/**
 * 打开编辑弹窗
 * 先拉一次详情，确保拿到最新的标签顺序（第一个即主主题）
 */
async function openEdit(note: API.NoteVO) {
    // 标签 id 按字符串承载，避免雪花 ID 被转成 number 后丢失末位
    editForm.value = {
        id: toId(note.id),
        content: note.content ?? '',
        tagIds: (note.tags ?? []).map((t) => toId(t.id)).filter((id) => id !== ''),
        source: note.source ?? '',
        insight: note.insight ?? '',
    };
    // 每次打开都重置下拉展开态，避免上次遗留的展开状态带进来
    editTagSelect.close();
    editOpen.value = true;
    // 详情接口能拿到完整标签，列表里已包含则不必再请求
    if (!note.tags) {
        try {
            // id 运行时为字符串，仅断言绕过类型检查，不做数值转换
            const res = await getNote({ id: editForm.value.id as unknown as number }, { silentError: true });
            const vo = res.data?.data;
            if (vo) {
                editForm.value.tagIds = (vo.tags ?? [])
                    .map((t) => toId(t.id))
                    .filter((id) => id !== '');
            }
        } catch {
            // 详情拉取失败时沿用列表数据
        }
    }
}

/**
 * 提交编辑
 * 副作用：调用 updateNote；成功后关闭弹窗并刷新列表
 */
async function submitEdit() {
    if (!editForm.value.id) return;
    if (!editForm.value.content.trim()) {
        message.warning('碎片内容不能为空');
        return;
    }
    editSaving.value = true;
    try {
        const res = await updateNote({
            // id 与 tagIds 运行时均为字符串，仅断言绕过 Apifox 生成类型，避免雪花 ID 精度丢失
            id: editForm.value.id as unknown as number,
            content: editForm.value.content.trim(),
            insight: editForm.value.insight.trim(),
            source: editForm.value.source.trim(),
            // 传空数组表示清空标签，与后端语义一致
            tagIds: editForm.value.tagIds as unknown as number[],
        });
        if (res.data?.data) {
            message.success('已保存');
            editOpen.value = false;
            fetchList();
            fetchStat();
        }
    } finally {
        editSaving.value = false;
    }
}

// 删除确认：删除后同步刷新列表与统计
async function confirmDelete(note: API.NoteVO) {
    const { Modal } = await import('ant-design-vue');
    Modal.confirm({
        title: '确认删除这条碎片吗？',
        content: note.content ? `「${note.content.slice(0, 30)}${note.content.length > 30 ? '…' : ''}」删除后不可恢复` : undefined,
        okText: '确认删除',
        okType: 'danger',
        cancelText: '取消',
        async onOk() {
            // toId 保证拿到原始字符串，仅断言绕过 Apifox 生成的 number 类型
            const res = await deleteNote({ id: toId(note.id) as unknown as number });
            if (res.data?.data) {
                message.success('已删除');
                fetchList();
                fetchStat();
            }
        },
    });
}

/**
 * 跳转整理预览页
 * 把选中的碎片 id 以逗号分隔写进 query，预览页据此拉取骨架
 */
function goCompose() {
    if (selectedIds.value.length === 0) {
        message.warning('先勾选要整理的碎片');
        return;
    }
    router.push({ path: '/user/notes/compose', query: { ids: selectedIds.value.join(',') } });
}

onMounted(() => {
    fetchList();
    fetchStat();
    loadTags();
});

/* 下拉弹出面板被 teleport 到 body，这里统一找出面板并绑定鼠标进出事件 */
let popupEl: HTMLElement | null = null;

/* 当前绑定在面板上的控制器与处理函数，解绑时按原引用移除，避免移除不掉造成监听泄漏 */
let boundHandlers: { enter: () => void; leave: () => void } | null = null;

/**
 * 给下拉弹出面板绑定鼠标进出事件
 * 面板不在触发器 DOM 树内，鼠标从输入框移进面板会先触发 wrapper 的 mouseleave，
 * 因此必须让面板自身也能取消收起。
 * 副作用：先移除上一次的监听，再给当前面板重新绑定
 *
 * @param controller 当前下拉的收起控制器
 */
function bindPopupHover(controller: { cancelClose: () => void; scheduleClose: () => void }) {
    unbindPopupHover();
    popupEl = document.querySelector<HTMLElement>('.note-tag-dropdown');
    if (!popupEl) return;
    // 保存引用，解绑时必须用同一个函数对象，否则 removeEventListener 无效
    const enter = () => controller.cancelClose();
    const leave = () => controller.scheduleClose();
    boundHandlers = { enter, leave };
    popupEl.addEventListener('mouseenter', enter);
    popupEl.addEventListener('mouseleave', leave);
}

/**
 * 解绑下拉面板上的鼠标事件
 * 副作用：按保存的原引用移除监听并清空状态，避免面板重建后监听泄漏
 */
function unbindPopupHover() {
    if (popupEl && boundHandlers) {
        popupEl.removeEventListener('mouseenter', boundHandlers.enter);
        popupEl.removeEventListener('mouseleave', boundHandlers.leave);
    }
    popupEl = null;
    boundHandlers = null;
}

// 筛选栏下拉展开时绑定面板事件，收起时解绑
watch(
    () => filterTagSelect.open.value,
    async (visible) => {
        if (visible) {
            await nextTick();
            bindPopupHover(filterTagSelect);
        } else {
            unbindPopupHover();
        }
    },
);

// 编辑弹窗下拉展开时绑定面板事件，收起时解绑
watch(
    () => editTagSelect.open.value,
    async (visible) => {
        if (visible) {
            await nextTick();
            bindPopupHover(editTagSelect);
        } else {
            unbindPopupHover();
        }
    },
);

// 页面销毁时解绑，避免残留引用
onBeforeUnmount(() => {
    unbindPopupHover();
});
</script>

<style scoped>
/* 页面主容器：各区块之间的统一留白 */
.note-page {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

/* 顶部操作栏：左右两端对齐，窄屏时自动换行 */
.note-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 16px 22px;
    flex-wrap: wrap;
}

/* 统计区：两个数字胶囊并排 */
.toolbar-stats {
    display: flex;
    align-items: center;
    gap: 20px;
}

/* 单个统计胶囊：数字在上、标签在下 */
.stat-chip {
    display: flex;
    flex-direction: column;
    gap: 2px;
}

/* 统计数字：放大加粗，作为操作栏的视觉重点 */
.stat-num {
    font-size: 22px;
    font-weight: 700;
    color: var(--text-primary);
    line-height: 1.1;
    font-variant-numeric: tabular-nums;
}

/* 统计标签：小字说明 */
.stat-label {
    font-size: 12px;
    color: var(--text-muted);
}

/* 操作按钮区：两个按钮并排 */
.toolbar-actions {
    display: flex;
    align-items: center;
    gap: 10px;
}

/* 筛选栏：Tab、主题、搜索三块横向排列 */
.note-filter {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
}

/* 状态 Tab 容器：胶囊底 */
.filter-tabs {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 3px;
    border-radius: var(--radius-pill);
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid var(--border-soft);
}

/* 单个 Tab 按钮：未选中为透明底 */
.filter-tab {
    padding: 6px 16px;
    border: none;
    border-radius: var(--radius-pill);
    background: transparent;
    color: var(--text-secondary);
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
}

/* Tab 悬停：提亮文字 */
.filter-tab:hover {
    color: var(--text-primary);
}

/* Tab 选中态：主色实底 */
.filter-tab.active {
    background: var(--accent);
    color: #fff;
    box-shadow: 0 4px 14px rgba(var(--accent-rgb), 0.32);
}

/* 主题筛选容器：固定宽度，内部 select 占满整宽 */
.filter-tag-wrap {
    width: 240px;
}

/* 主题筛选下拉：占满容器宽度，避免过长挤压搜索框 */
.filter-tag {
    width: 100%;
}

/* 内容搜索框：占满剩余宽度 */
.filter-search {
    flex: 1;
    min-width: 180px;
}

/* 全选行：左全选框 + 右统计 */
.select-all-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 4px;
}

/* 已选条数提示：弱化层级 */
.select-hint {
    font-size: 12px;
    color: var(--text-muted);
}

/* 首屏加载占位：居中显示 */
.note-loading {
    display: flex;
    justify-content: center;
    padding: 60px 0;
}

/* 碎片列表：纵向排列卡片 */
.note-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

/* 单条碎片卡片：勾选框 + 主体 + 操作按钮三栏 */
.note-card {
    display: flex;
    align-items: flex-start;
    gap: 14px;
    padding: 18px 20px;
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

/* 选中态：主色描边 + 淡底，明确提示会参与整理 */
.note-card.selected {
    border-color: var(--border-accent-strong);
    box-shadow: 0 0 0 1px rgba(var(--accent-rgb), 0.24);
}

/* 勾选框：顶部对齐，不随正文高度居中 */
.note-check {
    margin-top: 3px;
    flex-shrink: 0;
}

/* 卡片主体：纵向排列正文、理解、元信息 */
.note-main {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 10px;
}

/* 正文：保留换行与连续空格，长文本按词换行 */
.note-content {
    font-size: 14.5px;
    line-height: 1.8;
    color: var(--text-primary);
    white-space: pre-wrap;
    overflow-wrap: anywhere;
    margin: 0;
}

/* 一句话理解：左侧主色竖线 + 略淡的文字，与原文形成层级 */
.note-insight {
    margin: 0;
    padding: 8px 14px;
    border-left: 2px solid var(--accent);
    border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
    background: rgba(var(--accent-rgb), 0.07);
    font-size: 13px;
    line-height: 1.7;
    color: var(--text-secondary);
    white-space: pre-wrap;
    overflow-wrap: anywhere;
}

/* 元信息行：标签、日期、来源、状态横向排列 */
.note-meta {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
}

/* 主题标签：沿用 antd 默认样式，只收窄内边距 */
.note-tag {
    margin-inline-end: 0;
    font-size: 12px;
}

/* 元信息项：图标 + 文字，弱化颜色 */
.meta-item {
    display: inline-flex;
    align-items: center;
    gap: 5px;
    font-size: 12px;
    color: var(--text-muted);
    font-variant-numeric: tabular-nums;
}

/* 来源：可能较长，限制最大宽度并省略 */
.meta-source {
    max-width: 260px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

/* 归档状态标签：与元信息保持同一行高度 */
.meta-status {
    margin-inline-end: 0;
    font-size: 12px;
}

/* 右侧操作区：纵向排列或横排的小图标按钮 */
.note-actions {
    display: flex;
    align-items: center;
    gap: 2px;
    flex-shrink: 0;
}

/* 分页容器：居中 */
.note-pager {
    display: flex;
    justify-content: center;
    padding: 8px 0 4px;
}

/* 编辑弹窗主体：四个字段纵向排列 */
.edit-body {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding-top: 8px;
}

/* 窄屏适配：统计区与操作按钮改为整行并排 */
@media (max-width: 720px) {
    .note-toolbar {
        flex-direction: column;
        align-items: stretch;
        gap: 12px;
    }

    .toolbar-actions {
        justify-content: flex-end;
    }

    /* 筛选下拉在窄屏占满整行，避免半截被截断 */
    .filter-tag-wrap {
        width: 100%;
    }

    .note-card {
        padding: 14px 16px;
        gap: 10px;
    }

    /* 窄屏下操作按钮换到正文下方，避免挤压正文宽度 */
    .note-actions {
        align-self: flex-end;
    }
}
</style>

<style>
/* ===== 以下为下拉面板与编辑弹窗 teleport 到 body 后的全局样式，必须非 scoped 才能命中 ===== */

/* 主题筛选下拉面板：与站点玻璃卡片同一套底，避免默认暗色面板偏亮 */
.note-tag-dropdown .ant-select-item {
    color: var(--text-secondary);
    border-radius: var(--radius-sm);
}

/* 下拉项悬停：主色淡底，与站点其它 hover 反馈统一 */
.note-tag-dropdown .ant-select-item-option-active:not(.ant-select-item-option-disabled) {
    background: rgba(var(--accent-rgb), 0.1) !important;
    color: var(--text-primary);
}

/* 下拉项选中：主色淡底 + 主色文字 */
.note-tag-dropdown .ant-select-item-option-selected:not(.ant-select-item-option-disabled) {
    background: rgba(var(--accent-rgb), 0.16) !important;
    color: var(--accent-light);
    font-weight: 600;
}

/* 编辑弹窗正文 textarea：覆盖全局胶囊圆角，多行文本改用方角 */
.note-edit-modal textarea.ant-input {
    border-radius: var(--radius-sm) !important;
    padding: 10px 12px !important;
    line-height: 1.7;
    resize: vertical;
}
</style>
