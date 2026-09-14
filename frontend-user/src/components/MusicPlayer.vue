<template>
    <div class="music-player glass-card" v-if="player">
        <!-- 顶部：唱片 + 歌名/作者  -->
        <div class="player-top">
            <div class="cover-wrap" :class="{ spinning: player.playing }">
                <div class="cover-disc" :style="{ background: player.track.accent }">
                    <img :src="player.track.cover" alt="cover" class="cover-img" @error="onCoverError" />
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
                <PauseOutlined v-if="player.playing" />
                <CaretRightFilled v-else />
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
    PauseOutlined,
    CaretRightFilled,
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

// 封面请求失败时隐藏破损图标，保留唱片渐变作为可用的视觉兜底。
function onCoverError(event: Event) {
    const image = event.currentTarget as HTMLImageElement;
    image.style.display = 'none';
}
</script>

<style scoped>
/* 播放器容器：纵向三段（封面信息 / 进度 / 控制）均匀撑满整张卡，避免中部大片留白 */
.music-player {
    padding: 24px 28px 22px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    gap: 14px;
    position: relative;
    overflow: hidden;
}

/* 顶部：唱片 + 歌名作者 */
.player-top {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 10px;
}

/* 唱片外框：尺寸略放大以撑满播放器顶部，与右侧歌名区块高度对齐 */
.cover-wrap {
    position: relative;
    width: 104px;
    height: 104px;
    flex: 0 0 104px;
}

/* 唱片本体：黑胶圆盘 + 缓慢自转，未播放时暂停动画 */
.cover-disc {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    overflow: hidden;
    position: relative;
    box-shadow:
        0 0 0 3px rgba(var(--accent-rgb), 0.45),
        0 12px 40px rgba(0, 0, 0, 0.5),
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
    mix-blend-mode: normal;
    opacity: 0.92;
    border: 2px solid rgba(255, 255, 255, 0.18);
}

.cover-hole {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: var(--bg-page-2);
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

/* 进度行：由容器 justify-content 分配纵向位置，不再依赖 margin-top:auto */
.progress-row {
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

/* 已播放部分：宽度逐帧更新，过渡只留极短一段兜底，避免拖在真实进度后面 */
.progress-fill {
    position: absolute;
    left: 0;
    height: 4px;
    border-radius: 999px;
    background: linear-gradient(90deg, var(--accent), var(--accent-pink));
    transition: width 0.1s linear;
}

/* 进度圆点：与填充同步逐帧移动 */
.progress-thumb {
    position: absolute;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: #fff;
    transform: translate(-50%, 0);
    box-shadow: 0 0 12px rgba(var(--accent-rgb), 0.7);
    transition: left 0.1s linear;
}

/* 控制条：按钮居中排列，音量图标绝对定位在右端 */
.controls {
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
    border-color: var(--border-accent-strong);
    background: var(--accent-soft);
    transform: translateY(-1px);
}

/* 播放键：实心紫罗兰圆形按钮 + 白色图标，与参考站一致，比描边图标更聚焦 */
.ctrl-btn.play-btn {
    width: 48px;
    height: 48px;
    font-size: 22px;
    border: none;
    color: #fff;
    background: linear-gradient(135deg, var(--accent), var(--accent-strong));
    box-shadow: 0 8px 22px -6px rgba(var(--accent-rgb), 0.8);
}

.ctrl-btn.play-btn:hover {
    color: #fff;
    background: linear-gradient(135deg, var(--accent-light), var(--accent));
    box-shadow: 0 10px 26px -6px rgba(var(--accent-rgb), 0.95);
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
