<template>
    <transition name="back-to-top-fade">
        <button v-show="visible" class="back-to-top" type="button" aria-label="回到顶部" @click="toTop">
            <ArrowUpOutlined />
        </button>
    </transition>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { ArrowUpOutlined } from '@ant-design/icons-vue';

const visible = ref(false);
let rafId = 0;

function onScroll() {
    if (rafId) return;
    rafId = requestAnimationFrame(() => {
        visible.value = window.scrollY > 300;
        rafId = 0;
    });
}

function toTop() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

onMounted(() => {
    window.addEventListener('scroll', onScroll, { passive: true });
    onScroll();
});

onBeforeUnmount(() => {
    window.removeEventListener('scroll', onScroll);
    if (rafId) cancelAnimationFrame(rafId);
});
</script>

<style scoped>
.back-to-top {
    position: fixed;
    right: 28px;
    bottom: 32px;
    width: 44px;
    height: 44px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--bg-card);
    backdrop-filter: blur(var(--blur));
    -webkit-backdrop-filter: blur(var(--blur));
    border: 1px solid var(--border-soft);
    color: var(--text-secondary);
    box-shadow: var(--shadow-card);
    cursor: pointer;
    font-size: 18px;
    transition: transform 0.2s ease, color 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
    z-index: 40;
}

.back-to-top:hover {
    color: var(--accent);
    border-color: rgba(56, 189, 248, 0.45);
    transform: translateY(-2px);
    box-shadow: 0 12px 28px rgba(14, 165, 233, 0.25);
}

.back-to-top:active {
    transform: translateY(0);
}

.back-to-top-fade-enter-active,
.back-to-top-fade-leave-active {
    transition: opacity 0.25s ease, transform 0.25s ease;
}

.back-to-top-fade-enter-from,
.back-to-top-fade-leave-to {
    opacity: 0;
    transform: translateY(8px);
}

@media (max-width: 720px) {
    .back-to-top {
        right: 18px;
        bottom: 22px;
        width: 40px;
        height: 40px;
        font-size: 16px;
    }
}
</style>
