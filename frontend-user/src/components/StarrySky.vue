<template>
    <!-- 全局背景：图库轮播 + 遮罩 + 光晕 + 漂浮文字 -->
    <div class="bg-scene" aria-hidden="true">
        <!-- 背景图库 -->
        <div v-for="(url, i) in galleryImages" :key="url" class="bg-image" :style="{ backgroundImage: `url(${url})` }"
            :class="{ active: i === activeIdx }"></div>
        <!-- 半透明蒙层，压暗整体亮度 -->
        <div class="bg-mask"></div>
        <!-- 彩色光晕 -->
        <div class="bg-glow">
            <span class="glow glow-a"></span>
            <span class="glow glow-b"></span>
        </div>
        <!-- 横向漂浮的弹幕文字 -->
        <div class="bg-danmaku">
            <span class="dm dm-1">在干嘛呢?</span>
            <span class="dm dm-2">记录一下</span>
            <span class="dm dm-3">lilac 的小宇宙</span>
            <span class="dm dm-4">Stay curious</span>
            <span class="dm dm-5">今日份思考</span>
            <span class="dm dm-6">代码之外</span>
        </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';

// === 背景图库 ===
const galleryImages: string[] = [
    'https://img0.baidu.com/it/u=1310669715,706974892&fm=253&app=138&f=JPEG?w=1422&h=800',
    'https://img2.baidu.com/it/u=1885921255,298745559&fm=253&fmt=auto&app=138&f=JPEG?w=1422&h=800',
    'https://img0.baidu.com/it/u=4122862851,310941634&fm=253&fmt=auto&app=138&f=JPEG?w=759&h=427',
    'https://p3-pc-sign.douyinpic.com/obj/tos-cn-p-0015/oEdnBhRuEjpFPG2A5gvIoyts9pd9DAAfAfjItC?x-expires=2073405600&x-signature=A6vGWxBXsYwVyC%2BJatEn8DmbhSg%3D&from=1516005123',
    'https://img0.baidu.com/it/u=1469011681,1329290528&fm=253&fmt=auto&app=120&f=JPEG?w=1600&h=800'
];

const activeIdx = ref(0);
let timer: number | null = null;

onMounted(() => {
    if (galleryImages.length <= 1) return;
    timer = window.setInterval(() => {
        activeIdx.value = (activeIdx.value + 1) % galleryImages.length;
    }, 12000);
});

onUnmounted(() => {
    if (timer !== null) {
        clearInterval(timer);
        timer = null;
    }
});
</script>

<style scoped>
.bg-scene {
    position: fixed;
    inset: 0;
    pointer-events: none;
    overflow: hidden;
    z-index: 0;
}

.bg-image {
    position: absolute;
    inset: -5%;
    background-position: center;
    background-size: cover;
    background-repeat: no-repeat;
    filter: blur(14px) saturate(1.1) brightness(0.7);
    transform: scale(1.06);
    opacity: 0;
    transition: opacity 1.6s ease-in-out;
    will-change: opacity;
    animation: bg-pan 40s ease-in-out infinite alternate;
}

.bg-image.active {
    opacity: 1;
}

@keyframes bg-pan {
    0% {
        transform: scale(1.1) translate3d(0, 0, 0);
    }

    100% {
        transform: scale(1.12) translate3d(-2%, -1%, 0);
    }
}

.bg-mask {
    position: absolute;
    inset: 0;
    background: rgba(8, 7, 15, 0.32);
}

.bg-glow {
    position: absolute;
    inset: 0;
}

.glow {
    position: absolute;
    border-radius: 50%;
    filter: blur(120px);
    pointer-events: none;
}

.glow-a {
    width: 520px;
    height: 520px;
    background: #0ea5e9;
    opacity: 0.28;
    top: -120px;
    left: -80px;
}

.glow-b {
    width: 380px;
    height: 380px;
    background: #ec4899;
    opacity: 0.20;
    bottom: -100px;
    right: -60px;
}

.bg-danmaku {
    position: absolute;
    inset: 0;
    overflow: hidden;
}

.dm {
    position: absolute;
    white-space: nowrap;
    color: rgba(255, 255, 255, 0.09);
    font-weight: 600;
    letter-spacing: 0.08em;
    animation: dm-drift linear infinite;
    will-change: transform;
}

.dm-1 {
    top: 12%;
    font-size: 26px;
    animation-duration: 38s;
    animation-delay: -2s;
}

.dm-2 {
    top: 24%;
    font-size: 32px;
    animation-duration: 52s;
    animation-delay: -10s;
}

.dm-3 {
    top: 38%;
    font-size: 22px;
    animation-duration: 44s;
    animation-delay: -22s;
}

.dm-4 {
    top: 56%;
    font-size: 36px;
    animation-duration: 60s;
    animation-delay: -5s;
}

.dm-5 {
    top: 72%;
    font-size: 24px;
    animation-duration: 48s;
    animation-delay: -30s;
}

.dm-6 {
    top: 86%;
    font-size: 30px;
    animation-duration: 56s;
    animation-delay: -16s;
}

@keyframes dm-drift {
    from {
        transform: translateX(110vw);
    }

    to {
        transform: translateX(-100%);
    }
}

@media (max-width: 720px) {
    .bg-danmaku {
        display: none;
    }

    .glow {
        width: 320px;
        height: 320px;
    }
}
</style>
