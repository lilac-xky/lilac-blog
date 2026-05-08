<template>
    <div class="music-player glass-card" v-if="player">
        <!-- 顶部：唱片 + 歌名/作者  -->
        <div class="player-top">
            <div class="cover-wrap" :class="{ spinning: player.playing }">
                <div class="cover-disc" :style="{ background: player.track.accent }">
                    <img :src="player.track.cover" alt="cover" class="cover-img" />
                    <span class="cover-hole"></span>
                </div>
                <span class="cover-glow" :style="{ background: player.track.accent }"></span>
            </div>
            <div class="track-info">
                <div class="track-title" :title="player.track.title">{{ player.track.title }}</div>
                <div class="track-artist">{{ player.track.artist }}</div>
            </div>
        </div>

        <!-- 进度条 -->
        <div class="progress-row">
            <span class="time-label">{{ currentLabel }}</span>
            <div class="progress" @click="onSeek">
                <div class="progress-track"></div>
                <div class="progress-fill" :style="{ width: progressPct + '%' }"></div>
                <div class="progress-thumb" :style="{ left: progressPct + '%' }"></div>
            </div>
            <span class="time-label">{{ durationLabel }}</span>
        </div>

        <!-- 控制按钮 -->
        <div class="controls">
            <button class="ctrl-btn" type="button" @click="player.prev" aria-label="上一曲">
                <StepBackwardOutlined />
            </button>
            <button class="ctrl-btn play-btn" type="button" @click="player.toggle"
                :aria-label="player.playing ? '暂停' : '播放'">
                <PauseCircleFilled v-if="player.playing" />
                <PlayCircleFilled v-else />
            </button>
            <button class="ctrl-btn" type="button" @click="player.next" aria-label="下一曲">
                <StepForwardOutlined />
            </button>
            <span class="vol-icon">
                <SoundOutlined />
            </span>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, inject } from 'vue';
import {
    PlayCircleFilled,
    PauseCircleFilled,
    StepBackwardOutlined,
    StepForwardOutlined,
    SoundOutlined,
} from '@ant-design/icons-vue';
import { FakePlayerKey, formatTime, type FakePlayerState } from '@/composables/useFakePlayer';

const player = inject<FakePlayerState>(FakePlayerKey);

const progressPct = computed(() =>
    player ? Math.min(100, Math.max(0, player.progress * 100)) : 0
);
const currentLabel = computed(() => (player ? formatTime(player.currentTime) : '00:00'));
const durationLabel = computed(() => (player ? formatTime(player.duration) : '00:00'));

function onSeek(e: MouseEvent) {
    if (!player) return;
    const target = e.currentTarget as HTMLElement;
    const rect = target.getBoundingClientRect();
    player.seek((e.clientX - rect.left) / rect.width);
}
</script>

<style scoped>
.music-player {
    padding: 40px 20px 18px;
    display: flex;
    flex-direction: column;
    gap: 10px;
    position: relative;
    overflow: hidden;
}

.music-player::before {
    content: '';
    position: absolute;
    inset: -40% -10% auto auto;
    width: 60%;
    height: 60%;
    background: radial-gradient(circle, rgba(56, 189, 248, 0.18), transparent 70%);
    pointer-events: none;
}

/* 顶部：唱片 + 歌名作者 */
.player-top {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 18px;
}

.cover-wrap {
    position: relative;
    width: 100px;
    height: 84px;
    flex: 0 0 84px;
}

.cover-disc {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    overflow: hidden;
    position: relative;
    box-shadow:
        0 0 0 4px rgba(255, 255, 255, 0.04),
        0 12px 40px rgba(0, 0, 0, 0.45),
        inset 0 0 24px rgba(0, 0, 0, 0.4);
    animation: spin 22s linear infinite;
    animation-play-state: paused;
}

.cover-wrap.spinning .cover-disc {
    animation-play-state: running;
}

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}

.cover-img {
    position: absolute;
    inset: 18%;
    width: 64%;
    height: 64%;
    border-radius: 50%;
    object-fit: cover;
    mix-blend-mode: screen;
    opacity: 0.85;
    border: 2px solid rgba(255, 255, 255, 0.18);
}

.cover-hole {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: #0f0e1a;
    border: 2px solid rgba(255, 255, 255, 0.2);
    transform: translate(-50%, -50%);
    z-index: 2;
}

.cover-glow {
    position: absolute;
    inset: -10px;
    border-radius: 50%;
    filter: blur(24px);
    opacity: 0.45;
    z-index: -1;
}

.track-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.track-title {
    font-size: 20px;
    font-weight: 700;
    color: var(--text-primary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.track-artist {
    font-size: 15px;
    color: var(--text-muted);
    letter-spacing: 0.05em;
}

/* 进度条 */
.progress-row {
    margin-top: auto;
    display: flex;
    align-items: center;
    gap: 12px;
}

.time-label {
    font-size: 12.5px;
    color: var(--text-muted);
    font-variant-numeric: tabular-nums;
    flex: 0 0 auto;
}

.progress {
    position: relative;
    flex: 1;
    height: 22px;
    cursor: pointer;
    display: flex;
    align-items: center;
}

.progress-track {
    position: absolute;
    left: 0;
    right: 0;
    height: 4px;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.1);
}

.progress-fill {
    position: absolute;
    left: 0;
    height: 4px;
    border-radius: 999px;
    background: linear-gradient(90deg, var(--accent), var(--accent-pink));
    transition: width 0.25s linear;
}

.progress-thumb {
    position: absolute;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: #fff;
    transform: translate(-50%, 0);
    box-shadow: 0 0 12px rgba(56, 189, 248, 0.7);
    transition: left 0.25s linear;
}

.controls {
    margin-top: -6px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 18px;
    position: relative;
}

.ctrl-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    border: 1px solid var(--border-soft);
    background: rgba(255, 255, 255, 0.04);
    color: var(--text-secondary);
    cursor: pointer;
    transition: all 0.2s;
    font-size: 17px;
}

.ctrl-btn:hover {
    color: var(--accent);
    border-color: rgba(56, 189, 248, 0.45);
    background: rgba(56, 189, 248, 0.1);
    transform: translateY(-1px);
}

.ctrl-btn.play-btn {
    width: 52px;
    height: 52px;
    font-size: 38px;
    border: none;
    color: var(--accent);
    background: transparent;
    box-shadow: 0 0 22px rgba(56, 189, 248, 0.35);
}

.ctrl-btn.play-btn:hover {
    color: var(--accent-pink);
    box-shadow: 0 0 28px rgba(236, 72, 153, 0.45);
    transform: scale(1.06);
}

.vol-icon {
    position: absolute;
    right: 0;
    color: var(--text-muted);
    font-size: 16px;
}

@media (max-width: 640px) {
    .player-top {
        gap: 14px;
    }

    .cover-wrap {
        width: 74px;
        height: 74px;
        flex-basis: 74px;
    }

    .track-title {
        font-size: 18px;
    }
}
</style>
