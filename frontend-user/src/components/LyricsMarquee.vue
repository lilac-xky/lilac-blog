<template>
    <div class="lyrics-marquee glass-card" v-if="player">
        <span class="ly-icon">
            <CustomerServiceOutlined />
        </span>
        <div class="ly-viewport">
            <transition name="lyric-fade" mode="out-in">
                <div class="ly-line" :key="lineKey">
                    {{ displayText }}
                </div>
            </transition>
        </div>
        <span class="ly-progress">
            {{ percentLabel }}
        </span>
    </div>
</template>

<script setup lang="ts">
import { computed, inject } from 'vue';
import { CustomerServiceOutlined } from '@ant-design/icons-vue';
import { FakePlayerKey, type FakePlayerState } from '@/composables/useFakePlayer';

const player = inject<FakePlayerState>(FakePlayerKey);

const displayText = computed(() => {
    if (!player) return '';
    if (player.currentLyric) return player.currentLyric.text;
    if (player.lyrics.length > 0) return player.lyrics[0]!.text;
    return player.track.lyric || player.track.title;
});

const lineKey = computed(() => {
    if (!player) return 'none';
    return `${player.index}:${player.lyricIndex}`;
});

const percentLabel = computed(() =>
    player ? `${Math.round(player.progress * 100)}%` : '0%'
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
    min-width: 0;
    height: 100%;
    position: relative;
    display: flex;
    align-items: center;
    overflow: hidden;
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

.ly-line {
    display: block;
    width: 100%;
    text-align: center;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
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
    will-change: opacity, transform;
}

.lyric-fade-enter-active,
.lyric-fade-leave-active {
    transition:
        opacity 0.45s ease,
        transform 0.55s cubic-bezier(0.22, 1, 0.36, 1);
}

.lyric-fade-enter-from {
    opacity: 0;
    transform: translateY(10px);
}

.lyric-fade-leave-to {
    opacity: 0;
    transform: translateY(-10px);
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
    .ly-line {
        font-size: 15px;
    }

    .lyrics-marquee {
        padding: 12px 14px;
        height: 60px;
        gap: 10px;
    }
}
</style>
