<template>
    <aside v-if="headings.length" class="toc">
        <div class="toc-title">
            <UnorderedListOutlined />
            <span>目录</span>
        </div>
        <ul class="toc-list">
            <li v-for="(h, i) in headings" :key="`${i}-${h.text}`" class="toc-item"
                :class="{ active: idOf(h, i) === activeId }" :style="{ paddingLeft: `${(h.level - 1) * 10 + 8}px` }"
                :title="h.text" @click="jumpTo(h, i)">
                <span class="toc-text">{{ h.text }}</span>
            </li>
        </ul>
    </aside>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, ref, watch } from 'vue';
import { UnorderedListOutlined } from '@ant-design/icons-vue';
import type { HeadList, MdHeadingId } from 'md-editor-v3';

interface Props {
    headings: HeadList[];
    makeId: MdHeadingId;
}

const props = defineProps<Props>();

const activeId = ref('');
let io: IntersectionObserver | null = null;
const visibleIds = new Set<string>();
let orderedIds: string[] = [];

let jumpLockTimer: number | null = null;
let jumping = false;

function idOf(h: HeadList, index: number) {
    return props.makeId({
        text: h.text,
        level: h.level,
        index,
        currentToken: h.currentToken,
        nextToken: h.nextToken,
    });
}

function setupObserver() {
    io?.disconnect();
    visibleIds.clear();
    orderedIds = props.headings.map((h, i) => idOf(h, i));
    const els = orderedIds
        .map((id) => document.getElementById(id))
        .filter((el): el is HTMLElement => !!el);

    if (els.length === 0) {
        activeId.value = '';
        return;
    }

    activeId.value = orderedIds[0] ?? '';

    io = new IntersectionObserver(
        (entries) => {
            for (const entry of entries) {
                if (entry.isIntersecting) visibleIds.add(entry.target.id);
                else visibleIds.delete(entry.target.id);
            }
            if (jumping) return;
            const firstVisible = orderedIds.find((id) => visibleIds.has(id));
            if (firstVisible) {
                activeId.value = firstVisible;
            }
        },
        { rootMargin: '-84px 0px -70% 0px', threshold: 0 },
    );

    els.forEach((el) => io!.observe(el));
}

function jumpTo(h: HeadList, index: number) {
    const id = idOf(h, index);
    const el = document.getElementById(id);
    if (!el) return;
    activeId.value = id;
    jumping = true;
    if (jumpLockTimer !== null) window.clearTimeout(jumpLockTimer);
    el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    // 平滑滚动期间冻结 observer 的高亮更新，结束后再交还控制权
    jumpLockTimer = window.setTimeout(() => {
        jumping = false;
        jumpLockTimer = null;
    }, 600);
}

watch(
    () => props.headings,
    () => nextTick(setupObserver),
    { immediate: true },
);

onBeforeUnmount(() => {
    io?.disconnect();
    io = null;
    if (jumpLockTimer !== null) {
        window.clearTimeout(jumpLockTimer);
        jumpLockTimer = null;
    }
});
</script>

<style scoped>
.toc {
    position: fixed;
    top: 50%;
    /* 紧靠文章右侧：文章右边缘 = (100vw + 1100px) / 2，与 TOC 之间留 8px 间隙 */
    right: max(12px, calc((100vw - 1100px) / 2 - 200px - 8px));
    transform: translateY(-50%);
    width: 200px;
    max-height: calc(100vh - 240px);
    overflow-y: auto;
    padding: 10px 8px 12px;
    background: var(--bg-card);
    backdrop-filter: blur(var(--blur));
    -webkit-backdrop-filter: blur(var(--blur));
    border: 1px solid var(--border-soft);
    border-radius: var(--radius-card);
    box-shadow: var(--shadow-card);
    z-index: 30;
}

.toc-title {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 0 4px 8px;
    margin-bottom: 4px;
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.04em;
    color: var(--text-primary);
    border-bottom: 1px solid var(--border-soft);
}

.toc-title :deep(.anticon) {
    color: var(--accent);
    font-size: 13px;
}

.toc-list {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    gap: 1px;
}

.toc-item {
    position: relative;
    padding: 4px 6px 4px 8px;
    font-size: 12px;
    line-height: 1.45;
    color: var(--text-secondary);
    border-left: 2px solid transparent;
    border-radius: 5px;
    cursor: pointer;
    transition: color 0.18s ease, background 0.18s ease, border-color 0.18s ease;
}

.toc-item:hover {
    color: var(--text-primary);
    background: rgba(255, 255, 255, 0.04);
}

.toc-item.active {
    color: var(--accent);
    background: rgba(56, 189, 248, 0.08);
    border-left-color: var(--accent);
    font-weight: 600;
}

.toc-text {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.toc::-webkit-scrollbar {
    width: 6px;
}

.toc::-webkit-scrollbar-track {
    background: transparent;
}

.toc::-webkit-scrollbar-thumb {
    background: rgba(56, 189, 248, 0.25);
    border-radius: 3px;
}

.toc::-webkit-scrollbar-thumb:hover {
    background: rgba(56, 189, 248, 0.45);
}

@media (max-width: 1280px) {
    .toc {
        display: none;
    }
}
</style>
