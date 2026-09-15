<template>
    <div class="search-bar glass-card">
        <SearchOutlined class="search-icon" />
        <input :value="modelValue" class="search-input" type="text" :placeholder="placeholder" @input="onInput"
            @keyup.enter="emitSubmit" @compositionstart="onCompositionStart" @compositionend="onCompositionEnd" />
    </div>
</template>

<!--
  SearchBar：前台通用搜索条（无按钮，回车提交）
  - modelValue 单向接收关键词，输入时 update:modelValue + search 一起抛出，避免无谓的本地镜像状态
  - 输入时经 320ms 防抖自动检索，避免每敲一个字都打一次接口
  - 中文输入法组合期间不触发自动检索，等 compositionend 后再搜，避免拼音阶段打出半成品关键词
  - 回车立即提交；清空输入会以空关键词提交一次，用于恢复完整列表
-->
<script setup lang="ts">
import { ref } from 'vue';
import { SearchOutlined } from '@ant-design/icons-vue';

// 自动检索防抖时长：略长于常见手速的连续输入间隔
const DEBOUNCE_MS = 320;

withDefaults(
    defineProps<{
        modelValue?: string;
        placeholder?: string;
    }>(),
    {
        modelValue: '',
        placeholder: '搜索…',
    },
);

const emit = defineEmits<{
    (e: 'update:modelValue', value: string): void;
    (e: 'search', value: string): void;
}>();

// 输入法组合标记：组合期间只同步值，不触发检索
const composing = ref(false);
let debounceTimer: number | null = null;

// 清除排队中的自动检索，防止切换条件后仍触发旧关键词
function clearDebounce() {
    if (debounceTimer) {
        window.clearTimeout(debounceTimer);
        debounceTimer = null;
    }
}

// 输入过程：先同步给父级，再按防抖排一次检索；组合输入期间不排队
function onInput(event: Event) {
    const next = (event.target as HTMLInputElement).value;
    emit('update:modelValue', next);
    clearDebounce();
    if (composing.value) return;
    debounceTimer = window.setTimeout(() => {
        emit('search', next.trim());
    }, DEBOUNCE_MS);
}

// 中文输入结束：立刻按当前值检索一次
function onCompositionEnd(event: CompositionEvent) {
    composing.value = false;
    clearDebounce();
    emit('search', (event.target as HTMLInputElement).value.trim());
}

// 组合开始：仅打标记，交给 compositionend 兜底
function onCompositionStart() {
    composing.value = true;
    clearDebounce();
}

// 回车提交：取消防抖后立即检索
function emitSubmit(event: KeyboardEvent) {
    clearDebounce();
    emit('search', (event.target as HTMLInputElement).value.trim());
}
</script>

<style scoped>
/* 搜索条容器：胶囊玻璃底，比普通卡片略亮一档作为页面主入口 */
.search-bar {
    display: flex;
    align-items: center;
    gap: 10px;
    width: 100%;
    padding: 9px 16px 9px 18px;
    border-radius: var(--radius-pill);
    background: rgba(255, 255, 255, 0.07);
    border-color: rgba(255, 255, 255, 0.16);
    box-shadow: 0 6px 24px -10px rgba(0, 0, 0, 0.5), inset 0 1px 0 rgba(255, 255, 255, 0.16);
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

/* 聚焦态：主色描边 + 外发光，明确当前正在检索 */
.search-bar:focus-within {
    border-color: rgba(var(--accent-rgb), 0.55);
    box-shadow: 0 0 0 3px rgba(var(--accent-rgb), 0.15);
}

.search-icon {
    flex: 0 0 auto;
    color: var(--text-muted);
    font-size: 16px;
}

/* 原生输入框：透明底，去掉浏览器默认边框与轮廓 */
.search-input {
    flex: 1;
    min-width: 0;
    height: 28px;
    background: transparent;
    border: none;
    outline: none;
    color: var(--text-primary);
    font-size: 14px;
    letter-spacing: 0.02em;
}

.search-input::placeholder {
    color: var(--text-muted);
}
</style>
