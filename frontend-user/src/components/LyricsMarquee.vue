<template>
    <!-- 歌词条：左侧图标 + 中间当前歌词 + 右侧音符；底部一条细进度线表示播放进度 -->
    <div class="lyrics-marquee glass-card-deep" v-if="player">
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
        <span class="ly-note">
            <SoundOutlined />
        </span>
        <!-- 进度线：用伪元素承载填充宽度，避免额外 DOM -->
        <span class="ly-bar" :style="{ '--ly-progress': progressWidth }"></span>
    </div>
</template>

<script setup lang="ts">
import { computed, inject } from 'vue';
import { CustomerServiceOutlined, SoundOutlined } from '@ant-design/icons-vue';
import { FakePlayerKey, type FakePlayerState } from '@/composables/useFakePlayer';

const player = inject<FakePlayerState>(FakePlayerKey);

// 当前应展示的歌词：优先正在唱的那句，其次第一句，最后退化为歌名
const displayText = computed(() => {
    if (!player) return '';
    if (player.currentLyric) return player.currentLyric.text;
    if (player.lyrics.length > 0) return player.lyrics[0]!.text;
    return player.track.lyric || player.track.title;
});

// 切歌/换行时的过渡 key，保证 transition 能识别到内容变化
const lineKey = computed(() => {
    if (!player) return 'none';
    return `${player.index}:${player.lyricIndex}`;
});

/*
 * 底部进度线的填充宽度，直接喂给 CSS 变量 --ly-progress。
 *
 * 这里必须保留小数精度：早期版本写的是 Math.round(progress * 100)，
 * 也就是把宽度量化成整数百分比——一首 5 分钟的歌 1% ≈ 3 秒，
 * 进度条就变成每 3 秒才跳一格，这才是「一跳一跳、不连贯」的真正原因。
 * 保留三位小数后，配合 useFakePlayer 里逐帧同步的 currentTime，才是真正的匀速推进。
 */
const progressWidth = computed(() => {
    if (!player) return '0%';
    return `${(player.progress * 100).toFixed(3)}%`;
});
</script>

<style scoped>
/* 歌词条容器：使用深色实底变体，把背景压下去，让歌词成为中部最亮的一行字 */
.lyrics-marquee {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 16px 24px;
    height: 72px;
    position: relative;
    overflow: hidden;
}

/* 左侧耳机图标：主色 + 外发光 */
.ly-icon {
    flex: 0 0 auto;
    color: var(--accent-light);
    font-size: 18px;
    filter: drop-shadow(0 0 8px rgba(var(--accent-rgb), 0.65));
}

/* 歌词视口：左右两侧做渐隐遮罩，长句从中间淡出而不是被硬切 */
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

/* 单行歌词：白→主色（蓝青）→粉的横向渐变字，居中显示 */
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

/* 右侧音符：与左侧图标呼应，低透明度不抢歌词 */
.ly-note {
    flex: 0 0 auto;
    color: rgba(232, 234, 252, 0.4);
    font-size: 16px;
}

/* 底部进度线：1px 导轨 + 主色填充，宽度由 --ly-progress 控制 */
.ly-bar {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    height: 2px;
    background: rgba(255, 255, 255, 0.08);
}

/* 底部进度线填充：宽度逐帧更新，过渡只留极短一段用于兜底，
 * 过渡时间一长反而会拖在真实播放进度后面，看起来仍然是一跳一跳
 */
.ly-bar::after {
    content: '';
    position: absolute;
    inset: 0 auto 0 0;
    width: var(--ly-progress, 0%);
    background: linear-gradient(90deg, var(--accent), var(--accent-pink));
    transition: width 0.1s linear;
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
