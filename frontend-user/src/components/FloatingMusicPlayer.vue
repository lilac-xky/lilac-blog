<template>
    <transition name="floating-player">
        <aside v-if="player" class="floating-music glass-card" aria-label="音乐播放器">
            <div class="floating-disc" :class="{ spinning: player.playing }">
                <img :src="player.track.cover" :alt="`${player.track.title} 封面`" />
                <span class="disc-hole"></span>
            </div>

            <div class="track-copy">
                <strong :title="player.track.title">{{ player.track.title }}</strong>
                <span>{{ player.track.artist }}</span>
            </div>

            <div class="floating-controls">
                <button type="button" :aria-label="player.playing ? '暂停' : '播放'" @click="player.toggle">
                    <PauseOutlined v-if="player.playing" />
                    <CaretRightOutlined v-else />
                </button>
                <button type="button" aria-label="下一首" @click="player.next">
                    <StepForwardOutlined />
                </button>
            </div>
        </aside>
    </transition>
</template>

<!--
  FloatingMusicPlayer：主站非首页使用的悬浮音乐控制器
  - 复用 BasicLayout 提供的播放器状态，路由切换时保持歌曲与播放进度
  - 提供播放/暂停与下一首操作，唱片旋转状态与真实音频状态同步
  - 固定在右下角，并为移动端提供紧凑布局
-->
<script setup lang="ts">
import { inject } from 'vue';
import {
    CaretRightOutlined,
    PauseOutlined,
    StepForwardOutlined,
} from '@ant-design/icons-vue';
import { FakePlayerKey } from '@/composables/useFakePlayer';

// 从主布局注入共享播放器状态，避免悬浮控件创建第二个 Audio 实例。
const player = inject(FakePlayerKey);
</script>

<style scoped>
/* 固定悬浮播放器主体尺寸，降低非首页正文区域的遮挡。 */
.floating-music {
    position: fixed;
    right: 24px;
    bottom: 24px;
    z-index: 70;
    width: min(280px, calc(100vw - 48px));
    height: 72px;
    padding: 10px 12px;
    display: grid;
    grid-template-columns: 48px minmax(0, 1fr) auto;
    align-items: center;
    gap: 10px;
    background: var(--bg-card-translucent-strong);
    backdrop-filter: blur(var(--blur));
    -webkit-backdrop-filter: blur(var(--blur));
    box-shadow: var(--shadow-card), 0 0 28px rgba(var(--accent-rgb), 0.12);
}

/* 约束唱片封面尺寸，并维持播放时的旋转反馈。 */
.floating-disc {
    position: relative;
    width: 48px;
    height: 48px;
    border-radius: 50%;
    overflow: hidden;
    border: 2px solid rgba(var(--accent-rgb), 0.45);
    box-shadow: 0 0 18px rgba(var(--accent-rgb), 0.28);
    animation: floating-disc-spin 18s linear infinite paused;
}

/* 仅在播放状态恢复唱片旋转动画。 */
.floating-disc.spinning {
    animation-play-state: running;
}

/* 让封面图片完整填充圆形唱片区域。 */
.floating-disc img {
    width: 100%;
    height: 100%;
    display: block;
    object-fit: cover;
}

/* 绘制唱片中心孔，增强封面的唱片识别度。 */
.disc-hole {
    position: absolute;
    left: 50%;
    top: 50%;
    width: 8px;
    height: 8px;
    border: 2px solid rgba(255, 255, 255, 0.35);
    border-radius: 50%;
    background: var(--bg-page-2);
    transform: translate(-50%, -50%);
}

/* 纵向排列歌曲标题和歌手，并允许文本区域收缩。 */
.track-copy {
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;
}

/* 对过长的歌曲信息统一使用单行省略。 */
.track-copy strong,
.track-copy span {
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
}

/* 设置悬浮播放器内歌曲标题的紧凑字号。 */
.track-copy strong {
    color: var(--text-primary);
    font-size: 13px;
}

/* 设置歌手信息的次要文本样式。 */
.track-copy span {
    color: var(--text-muted);
    font-size: 11px;
}

/* 横向排列播放控制按钮并压缩按钮间距。 */
.floating-controls {
    display: flex;
    gap: 4px;
}

/* 定义悬浮播放器圆形控制按钮的尺寸和交互过渡。 */
.floating-controls button {
    width: 30px;
    height: 30px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border: 1px solid var(--border-soft);
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.05);
    color: var(--text-secondary);
    cursor: pointer;
    transition: color var(--duration-fast), border-color var(--duration-fast), transform var(--duration-fast);
}

/* 在指针悬浮时突出当前可操作的播放器按钮。 */
.floating-controls button:hover {
    color: var(--accent);
    border-color: var(--border-accent-strong);
    transform: translateY(-1px);
}

/* 为悬浮播放器挂载和卸载提供淡入位移动画。 */
.floating-player-enter-active,
.floating-player-leave-active {
    transition: opacity var(--duration-normal), transform var(--duration-normal);
}

/* 设置悬浮播放器过渡动画的起止状态。 */
.floating-player-enter-from,
.floating-player-leave-to {
    opacity: 0;
    transform: translateY(12px);
}

@keyframes floating-disc-spin {
    to {
        transform: rotate(360deg);
    }
}

@media (max-width: 640px) {

    /* 在移动端进一步压缩播放器宽度和内部留白。 */
    .floating-music {
        right: 14px;
        bottom: 14px;
        width: min(252px, calc(100vw - 28px));
        height: 66px;
        padding: 8px 10px;
        grid-template-columns: 44px minmax(0, 1fr) auto;
        gap: 8px;
    }

    /* 缩小移动端唱片，避免挤占歌曲信息空间。 */
    .floating-disc {
        width: 44px;
        height: 44px;
    }

    /* 收紧移动端控制按钮之间的距离。 */
    .floating-controls {
        gap: 3px;
    }

    /* 缩小移动端控制按钮并保持稳定点击区域。 */
    .floating-controls button {
        width: 28px;
        height: 28px;
    }
}

@media (prefers-reduced-motion: reduce) {

    /* 尊重系统低动态偏好并停止唱片旋转。 */
    .floating-disc {
        animation: none;
    }

    /* 尊重系统低动态偏好并关闭播放器切换动画。 */
    .floating-player-enter-active,
    .floating-player-leave-active {
        transition: none;
    }
}
</style>
