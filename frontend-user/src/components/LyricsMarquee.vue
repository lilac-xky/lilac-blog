<template>
    <div class="lyrics-marquee glass-card" v-if="player">
        <span class="ly-icon">
            <CustomerServiceOutlined />
        </span>
        <div class="ly-viewport" ref="viewport">
            <div class="ly-track" ref="track" :style="{ transform: `translateX(${-offset}px)` }">
                {{ player.track.lyric }}
            </div>
        </div>
        <span class="ly-progress">
            {{ percentLabel }}
        </span>
    </div>
</template>

<script setup lang="ts">
import { computed, inject, onMounted, onUnmounted, ref, watch } from 'vue';
import { CustomerServiceOutlined } from '@ant-design/icons-vue';
import { FakePlayerKey, type FakePlayerState } from '@/composables/useFakePlayer';

const player = inject<FakePlayerState>(FakePlayerKey);

const viewport = ref<HTMLElement | null>(null);
const track = ref<HTMLElement | null>(null);
const viewW = ref(0);
const trackW = ref(0);

const maxShift = computed(() => Math.max(0, trackW.value - viewW.value));
const offset = computed(() => {
    if (!player) return 0;
    return maxShift.value * Math.min(1, Math.max(0, player.progress));
});
const percentLabel = computed(() =>
    player ? `${Math.round(player.progress * 100)}%` : '0%'
);

let ro: ResizeObserver | null = null;

function measure() {
    if (viewport.value) viewW.value = viewport.value.clientWidth;
    if (track.value) trackW.value = track.value.scrollWidth;
}

onMounted(() => {
    measure();
    if (typeof ResizeObserver !== 'undefined') {
        ro = new ResizeObserver(measure);
        if (viewport.value) ro.observe(viewport.value);
        if (track.value) ro.observe(track.value);
    } else {
        window.addEventListener('resize', measure);
    }
});

onUnmounted(() => {
    if (ro) ro.disconnect();
    else window.removeEventListener('resize', measure);
});

watch(
    () => player?.track.lyric,
    () => {
        requestAnimationFrame(measure);
    }
);
</script>

<style scoped>
.lyrics-marquee {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 16px 24px;
    height: 72px;
    position: relative;
    overflow: hidden;
}

.lyrics-marquee::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(90deg,
            rgba(56, 189, 248, 0.06),
            transparent 25%,
            transparent 75%,
            rgba(236, 72, 153, 0.06));
    pointer-events: none;
}

.ly-icon {
    flex: 0 0 auto;
    color: var(--accent);
    font-size: 18px;
    filter: drop-shadow(0 0 8px rgba(56, 189, 248, 0.65));
}

.ly-viewport {
    flex: 1;
    overflow: hidden;
    position: relative;
    -webkit-mask-image: linear-gradient(90deg,
            transparent,
            #000 6%,
            #000 94%,
            transparent);
    mask-image: linear-gradient(90deg,
            transparent,
            #000 6%,
            #000 94%,
            transparent);
}

.ly-track {
    display: inline-block;
    white-space: nowrap;
    font-size: 18px;
    font-weight: 500;
    letter-spacing: 0.04em;
    line-height: 1.4;
    background: linear-gradient(90deg,
            #f5f3ff,
            var(--accent) 45%,
            var(--accent-pink));
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    will-change: transform;
    transition: transform 0.6s linear;
}

.ly-progress {
    flex: 0 0 auto;
    font-size: 11px;
    font-weight: 600;
    color: var(--text-muted);
    font-variant-numeric: tabular-nums;
    letter-spacing: 0.08em;
    padding: 3px 10px;
    border-radius: var(--radius-pill);
    border: 1px solid var(--border-soft);
    background: rgba(255, 255, 255, 0.04);
}

@media (max-width: 640px) {
    .ly-track {
        font-size: 15px;
    }

    .lyrics-marquee {
        padding: 12px 14px;
        height: 60px;
        gap: 10px;
    }
}
</style>
