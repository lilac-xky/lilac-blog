<template>
    <!-- 随手记弹层：目标 3 秒记完，主题/来源/理解默认收起 -->
    <a-modal :open="open" :footer="null" :width="560" :mask-closable="true" centered wrap-class-name="quick-note-modal"
        title="记一笔" @cancel="handleClose">
        <div class="quick-note-body">
            <!-- 正文输入：自动聚焦，Ctrl+Enter 直接提交 -->
            <a-textarea ref="contentRef" v-model:value="form.content" class="note-input" :rows="4"
                placeholder="记下这个知识点…（Ctrl + Enter 保存，Esc 关闭）" :maxlength="10000" show-count
                @keydown.ctrl.enter.prevent="submit" @keydown.esc.prevent="handleClose" />

            <!-- 第二行常驻：主题必选性交给用户判断，留空即为「杂项」 -->
            <div class="quick-row">
                <!-- 主题下拉包一层，用于监听「鼠标离开这块区域」，实现移出即收起 -->
                <div class="tag-select-wrap" @mouseenter="tagSelect.cancelClose" @mouseleave="tagSelect.scheduleClose">
                    <a-select v-model:value="form.tagIds" mode="multiple" class="tag-select" placeholder="选个主题（可留空）"
                        :options="tagOptions" :loading="tagLoading" allow-clear :max-tag-count="2" :open="tagSelect.open"
                        popup-class-name="quick-note-tag-dropdown" @dropdown-visible-change="tagSelect.onVisibleChange" />
                </div>
                <a-input v-model:value="form.source" class="source-input" placeholder="来源（书名 / 链接 / 课程）"
                    :maxlength="255" allow-clear />
            </div>

            <!-- 第三行折叠：一句话理解，收起时不占视觉重量 -->
            <div class="insight-row">
                <button type="button" class="insight-toggle" @click="insightOpen = !insightOpen">
                    <EditOutlined />
                    <span>{{ insightOpen ? '收起理解' : '补一句我的理解' }}</span>
                </button>
                <a-textarea v-if="insightOpen" ref="insightRef" v-model:value="form.insight" class="insight-input"
                    :rows="2" placeholder="用自己的话写一句，这条才算真的沉淀下来" :maxlength="500" show-count />
            </div>

            <!-- 底部操作：左侧提示，右侧主按钮 -->
            <div class="quick-foot">
                <span class="quick-hint">保存后弹层不关，可连续记</span>
                <a-button type="primary" :loading="submitting" @click="submit">
                    <CheckOutlined />
                    记下来
                </a-button>
            </div>
        </div>
    </a-modal>
</template>

<!--
  QuickNote：前台「碎知」随手记弹层
  - 正文自动聚焦，Ctrl+Enter 提交、Esc 关闭，记录成本压到 3 秒
  - 主题 / 来源 / 理解都是可选项：记得下来比记得好重要，补理解留到整理时
  - 提交成功后弹层不关闭、清空输入并重新聚焦，便于连续记录多条
  - 主题下拉直接复用文章标签接口，与「整理」时的分组依据完全一致

  样式说明：a-modal 会被 teleport 到 body，scoped 样式命中不了内部结构，
  因此弹窗主题样式写在文件末尾的非 scoped 块里，用 quick-note-modal 类名隔离。
-->
<script setup lang="ts">
import { nextTick, onBeforeUnmount, ref, watch } from 'vue';
import { message } from 'ant-design-vue';
import { CheckOutlined, EditOutlined } from '@ant-design/icons-vue';
import { addNote } from '@/api/noteController';
import { listTagByPageVo } from '@/api/tagController';
import { useHoverCollapseSelect } from '@/composables/useHoverCollapseSelect';

/* 弹层开关：由父组件通过 v-model:open 双向绑定 */
const open = defineModel<boolean>('open', { default: false });

/* 提交成功事件：父组件可据此刷新列表或统计 */
const emit = defineEmits<{ (e: 'saved'): void }>();

/* 正文输入框与理解输入框的引用，用于提交后重新聚焦 */
const contentRef = ref<{ focus?: () => void } | null>(null);
const insightRef = ref<{ focus?: () => void } | null>(null);

/* 理解行是否展开 */
const insightOpen = ref(false);

/* 提交中状态，防止连点重复提交 */
const submitting = ref(false);

/* 主题下拉选项与加载态：value 统一为字符串，与 tagIds 的字符串语义保持一致 */
const tagOptions = ref<Array<{ label: string; value: string }>>([]);
const tagLoading = ref(false);

/* 主题下拉的「移出即收起」控制：受控 open + 鼠标进出事件 */
const tagSelect = useHoverCollapseSelect({ closeDelay: 180 });

/* 下拉弹出面板的 DOM 引用：面板被 teleport 到 body，需要手动找出来绑鼠标事件 */
let popupEl: HTMLElement | null = null;

/* 已绑定的处理函数引用，解绑时必须用同一个函数对象才移除得掉 */
let boundHandlers: { enter: () => void; leave: () => void } | null = null;

/**
 * 给下拉弹出面板绑定鼠标进出事件
 * 面板是 teleport 到 body 的，不在触发器 DOM 树内，
 * 因此鼠标从输入框移进面板时会先触发 wrapper 的 mouseleave，必须让面板自身也能「取消收起」。
 * 副作用：先解绑旧面板上的监听，再给新面板绑上
 */
function bindPopupHover() {
    unbindPopupHover();
    popupEl = document.querySelector<HTMLElement>('.quick-note-tag-dropdown');
    if (!popupEl) return;
    const enter = () => tagSelect.cancelClose();
    const leave = () => tagSelect.scheduleClose();
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

// 下拉展开时绑定面板事件，收起时解绑；面板每次展开都会重建，必须重新绑
watch(
    () => tagSelect.open.value,
    async (visible) => {
        if (visible) {
            // 等面板真正挂到 DOM 再找，否则 querySelector 拿不到
            await nextTick();
            bindPopupHover();
        } else {
            unbindPopupHover();
        }
    },
);

/* 表单模型：与 NoteAddRequest 字段一一对应。
 * tagIds 为雪花 ID，按字符串承载，避免转 number 后末位精度丢失 */
const form = ref<{ content: string; insight: string; source: string; tagIds: string[] }>({
    content: '',
    insight: '',
    source: '',
    tagIds: [],
});

/**
 * 重置表单到初始状态
 * 副作用：清空四个字段、收起理解行与主题下拉，不影响弹层开关
 */
function resetForm() {
    form.value = { content: '', insight: '', source: '', tagIds: [] };
    insightOpen.value = false;
    tagSelect.close();
}

/**
 * 拉取主题下拉选项
 * 副作用：写入 tagOptions；失败时保持空列表并静默，不阻塞记录。
 * 注意：后端 Long 实际以字符串返回，这里统一 String() 收窄，
 * 避免后端返回字符串、前端却按 number 直接比较导致选不中。
 */
async function loadTags() {
    if (tagOptions.value.length > 0) return;
    tagLoading.value = true;
    try {
        const res = await listTagByPageVo({ current: 1, pageSize: 200 }, { silentError: true });
        const records = res.data?.data?.records ?? [];
        tagOptions.value = records.map((t) => ({
            label: String(t.tagName ?? ''),
            value: String(t.id ?? ''),
        }));
    } catch {
        // 标签拉取失败不影响记笔记，保持空选项即可
    } finally {
        tagLoading.value = false;
    }
}

/**
 * 提交一条碎片
 * 副作用：调用 addNote；成功后清空表单、保持弹层打开并重新聚焦，便于连续记录
 */
async function submit() {
    if (submitting.value) return;
    if (!form.value.content.trim()) {
        message.warning('先写点什么再保存吧');
        return;
    }
    submitting.value = true;
    try {
        const res = await addNote({
            content: form.value.content.trim(),
            insight: form.value.insight.trim() || undefined,
            source: form.value.source.trim() || undefined,
            // tagIds 运行时为字符串数组，仅断言绕过 Apifox 生成类型，避免雪花 ID 转 number 丢精度
            tagIds: form.value.tagIds.length > 0
                ? (form.value.tagIds as unknown as number[])
                : undefined,
        });
        if (res.data?.data) {
            message.success('已记下，继续记');
            resetForm();
            // 清空后把焦点放回正文，省掉一次点击
            await nextTick();
            contentRef.value?.focus?.();
            emit('saved');
        }
    } finally {
        submitting.value = false;
    }
}

/**
 * 关闭弹层
 * 副作用：清空表单并通知父组件关闭，避免下次打开残留上一次的内容
 */
function handleClose() {
    resetForm();
    open.value = false;
}

// 打开时加载主题并聚焦正文；关闭时清空残留内容
watch(open, async (visible) => {
    if (visible) {
        loadTags();
        await nextTick();
        contentRef.value?.focus?.();
    } else {
        resetForm();
    }
});

// 展开理解行后自动聚焦，省去一次点击
watch(insightOpen, async (expanded) => {
    if (expanded) {
        await nextTick();
        insightRef.value?.focus?.();
    }
});

// 弹层销毁时解绑面板监听，避免残留引用
onBeforeUnmount(unbindPopupHover);
</script>

<style scoped>
/* 弹层主体：纵向排列输入区、元信息行、折叠区与底部操作 */
.quick-note-body {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding-top: 4px;
}

/* 第二行：主题占主要宽度，来源固定右侧 */
.quick-row {
    display: flex;
    align-items: center;
    gap: 10px;
}

/* 主题下拉容器：撑满剩余宽度，内部 select 占满整宽 */
.tag-select-wrap {
    flex: 1;
    min-width: 0;
}

/* 主题下拉：撑满容器宽度，多选时最多展示 2 个标签 */
.tag-select {
    width: 100%;
}

/* 来源输入框：固定宽度，避免挤压主题下拉 */
.source-input {
    width: 220px;
    flex-shrink: 0;
}

/* 理解折叠区：纵向排列，展开时下方多出输入框 */
.insight-row {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

/* 展开/收起按钮：弱化为文字链接风格，不抢主按钮注意力 */
.insight-toggle {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    align-self: flex-start;
    padding: 2px 0;
    border: none;
    background: transparent;
    color: var(--accent-light);
    font-size: 12.5px;
    cursor: pointer;
    transition: color 0.2s ease;
}

/* 悬停态：提亮到主色，明确可点击 */
.insight-toggle:hover {
    color: var(--accent);
}

/* 底部操作行：提示靠左、主按钮靠右 */
.quick-foot {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: 4px;
}

/* 底部提示文案：弱化层级，说明弹层不会自动关闭 */
.quick-hint {
    font-size: 12px;
    color: var(--text-muted);
}

/* 窄屏适配：第二行改为纵向堆叠，来源输入框占满整行 */
@media (max-width: 720px) {
    .quick-row {
        flex-direction: column;
        align-items: stretch;
    }

    .source-input {
        width: 100%;
    }
}
</style>

<style>
/* ===== 以下为弹窗 teleport 到 body 后的全局样式，必须非 scoped 才能命中 ===== */

/* 弹窗主体：玻璃拟态 + 主色描边，与前台卡片风格一致 */
.quick-note-modal .ant-modal-content {
    border-radius: var(--radius-card);
    border: 1px solid var(--border-accent);
    background: rgba(18, 22, 34, 0.94);
    backdrop-filter: blur(var(--blur)) saturate(1.2);
    -webkit-backdrop-filter: blur(var(--blur)) saturate(1.2);
    box-shadow: var(--shadow-card);
}

/* 弹窗头部：透明底，去掉默认分割线，避免与正文叠出双线 */
.quick-note-modal .ant-modal-header {
    background: transparent;
    margin-bottom: 12px;
}

/* 弹窗标题：与站点标题层级一致 */
.quick-note-modal .ant-modal-title {
    font-size: 16px;
    font-weight: 700;
    color: var(--text-primary);
}

/* 关闭按钮：跟随次级文字色，悬停提亮到主色 */
.quick-note-modal .ant-modal-close {
    color: var(--text-muted);
}

.quick-note-modal .ant-modal-close:hover {
    color: var(--accent);
}

/* 正文输入框：覆盖全局胶囊圆角，改回与卡片一致的方角，多行文本才不显得像椭圆 */
.quick-note-modal textarea.ant-input {
    border-radius: var(--radius-sm) !important;
    padding: 12px 14px !important;
    font-size: 14px;
    line-height: 1.75;
    resize: vertical;
    background: rgba(255, 255, 255, 0.04) !important;
}

/* 单行输入框（来源）：保留胶囊形，与站点其它输入框统一 */
.quick-note-modal input.ant-input {
    border-radius: var(--radius-pill) !important;
}

/* 字数统计：弱化到辅助文字色，避免比正文更抢眼 */
.quick-note-modal .ant-input-data-count {
    font-size: 11px;
    color: var(--text-muted);
}

/* 主题下拉选择器：与输入框同高同底色 */
.quick-note-modal .ant-select .ant-select-selector {
    border-radius: var(--radius-pill) !important;
    background: rgba(255, 255, 255, 0.04) !important;
    border-color: var(--border-soft) !important;
}

/* 下拉聚焦态：主色描边 + 柔光，与全局输入框聚焦保持一致 */
.quick-note-modal .ant-select-focused .ant-select-selector {
    border-color: var(--accent) !important;
    box-shadow: 0 0 0 2px var(--accent-soft-2) !important;
}

/* 主题下拉面板：与弹窗同一套玻璃底，避免默认暗色面板显得比弹窗更亮 */
.quick-note-tag-dropdown .ant-select-item {
    color: var(--text-secondary);
    border-radius: var(--radius-sm);
}

/* 下拉项悬停：主色淡底，与站点其它 hover 反馈统一 */
.quick-note-tag-dropdown .ant-select-item-option-active:not(.ant-select-item-option-disabled) {
    background: rgba(var(--accent-rgb), 0.1) !important;
    color: var(--text-primary);
}

/* 下拉项选中：主色淡底 + 主色文字，与右侧对勾同时提示已选 */
.quick-note-tag-dropdown .ant-select-item-option-selected:not(.ant-select-item-option-disabled) {
    background: rgba(var(--accent-rgb), 0.16) !important;
    color: var(--accent-light);
    font-weight: 600;
}
</style>
