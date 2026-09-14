<template>
    <!-- 全局背景：图库轮播（清楚 → 渐糊 → 停顿 → 换图）+ 影调蒙层 + 萤火粒子 + 漂浮文字 -->
    <div class="bg-scene" :class="{ 'is-soft': blurred }" :style="sceneStyle" aria-hidden="true">
        <!-- 轮播轨道：承担整体缓慢平移 -->
        <div class="bg-track">
            <div v-for="(url, i) in galleryImages" :key="url" class="bg-image"
                :style="{ backgroundImage: `url(${url})` }" :class="{ active: i === activeIdx }"></div>
        </div>

        <!-- 影调蒙层：中性偏暖，只统一明暗不染色 -->
        <div class="bg-mask"></div>

        <!-- 萤火粒子：外层飘荡 + 内层呼吸明灭，见下方 star-drift-* / star-breathe -->
        <div class="bg-stars">
            <span v-for="(s, i) in stars" :key="i" class="star" :class="`drift-${i % 3}`" :style="s.wrap">
                <span class="star-dot" :style="s.dot"></span>
            </span>
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

/* ==================================================================
 * 【调参区一】背景轮播节奏：想改快慢只动这三个数（单位毫秒）
 * ------------------------------------------------------------------
 * CLEAR_MS     —— 新图保持「清楚」的时长。觉得糊得太早 → 调大
 * BLUR_FADE_MS —— 从清楚过渡到全糊的时长。觉得糊得太快 → 调大
 * BLUR_HOLD_MS —— 糊透之后停顿多久才换下一张
 *
 * 例：想让它清楚久一点、糊得慢一点 → CLEAR_MS = 9000，BLUR_FADE_MS = 3500
 * ================================================================== */
const CLEAR_MS = 2000;
const BLUR_FADE_MS = 1500;
const BLUR_HOLD_MS = 4000;

// 把过渡时长交给 CSS 变量，避免同一个数值在 JS 和 CSS 里各写一份导致对不上
const sceneStyle = { '--bg-blur-fade': `${BLUR_FADE_MS}ms` };

// 当前是否处于虚化阶段：为 true 时整幅背景渐渐糊掉
const blurred = ref(false);
// 当前显示的背景图下标
const activeIdx = ref(0);
let phaseTimer: number | null = null;

// 单轮节奏：先安排「变糊」，糊着停一会后换图并恢复清楚，然后递归进入下一轮
function runCycle() {
    phaseTimer = window.setTimeout(() => {
        blurred.value = true;
        phaseTimer = window.setTimeout(() => {
            blurred.value = false;
            activeIdx.value = (activeIdx.value + 1) % galleryImages.length;
            runCycle();
        }, BLUR_HOLD_MS);
    }, CLEAR_MS);
}

/*
 * 萤火粒子：外层负责「随意飘荡」，内层负责「呼吸明灭」。
 * 拆成两层是因为 transform 只能被一个动画驱动，位移和缩放写在同一个元素上会互相覆盖。
 * 位置/大小/快慢全部写死成确定性数值，避免每次渲染随机导致画面抖动。
 */
const STAR_TINTS = ['#a5f3fc', '#bbf7d0', '#e9d5ff', '#fbcfe8', '#ffffff'];

// 粒子数量：想更密/更疏改这里
const STAR_COUNT = 40;

function buildStars(count: number) {
    const list: { wrap: Record<string, string>; dot: Record<string, string> }[] = [];
    for (let i = 0; i < count; i += 1) {
        // 用固定步长取余代替 Math.random，保证每次渲染位置一致（也便于截图比对）
        const left = ((i * 37.3) % 100).toFixed(2);
        const top = ((i * 61.7) % 100).toFixed(2);
        const size = 3 + ((i * 7) % 4);
        const tint = STAR_TINTS[i % STAR_TINTS.length]!;
        // 飘荡：16~36s 一轮，快慢差两倍多；负延迟把相位错开，看起来才像各自乱飘
        const driftDuration = (16 + ((i * 5) % 9) * 2.5).toFixed(1);
        const driftDelay = (-((i * 1.7) % 20)).toFixed(1);
        // 呼吸：3.2~8.6s 一轮，负延迟错峰，避免整屏同时亮灭
        const breatheDuration = (3.2 + ((i * 3) % 7) * 0.9).toFixed(1);
        const breatheDelay = (-((i * 0.8) % 8)).toFixed(1);
        list.push({
            wrap: {
                left: `${left}%`,
                top: `${top}%`,
                animationDuration: `${driftDuration}s`,
                animationDelay: `${driftDelay}s`,
            },
            dot: {
                width: `${size}px`,
                height: `${size}px`,
                background: tint,
                // 三层光晕：内层实心 + 紧贴的高光 + 外扩的柔光，缩放时会整体一起变大变小
                boxShadow: `0 0 ${size * 2}px ${size}px ${tint}`,
                animationDuration: `${breatheDuration}s`,
                animationDelay: `${breatheDelay}s`,
            },
        });
    }
    return list;
}

// 粒子配置列表：一次性生成，供模板直接绑定
const stars = buildStars(STAR_COUNT);

onMounted(() => {
    if (galleryImages.length <= 1) return;
    runCycle();
});

onUnmounted(() => {
    if (phaseTimer !== null) {
        clearTimeout(phaseTimer);
        phaseTimer = null;
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

    /* ==============================================================
     * 【调参区二】背景虚化程度：改这两个值就能定「清楚」和「模糊」的档位
     * --------------------------------------------------------------
     * --bg-blur-clear : 清楚阶段。0px = 完全清晰；觉得太清晰 → 调大
     *                   推荐 3px（默认）/ 6px / 10px
     * --bg-blur-soft  : 模糊阶段。数值越大糊得越狠
     * --bg-blur-fade  : 过渡时长，由 JS 的 BLUR_FADE_MS 注入，不用在这里改
     * ============================================================== */
    --bg-blur-clear: 4px;
    --bg-blur-soft: 20px;
}

/* 轮播轨道：整幅略微放大并缓慢平移 */
.bg-track {
    position: absolute;
    inset: -5%;
    animation: bg-pan 40s ease-in-out infinite alternate;
    will-change: transform;
}

@keyframes bg-pan {
    0% {
        transform: scale(1.02) translate3d(0, 0, 0);
    }

    100% {
        transform: scale(1.05) translate3d(-2%, -1%, 0);
    }
}

/* 单张背景图：
 * 默认（未激活）就保持全糊，这样换图瞬间旧图不会「边淡出边变清楚」，
 * 新图则在淡入的同时由糊到清楚，形成对焦进来的观感。
 * filter 的过渡时长就是「由清楚变糊」那一下的速度。
 */
.bg-image {
    position: absolute;
    inset: 0;
    background-position: center;
    background-size: cover;
    background-repeat: no-repeat;
    filter: blur(var(--bg-blur-soft)) saturate(1.12);
    opacity: 0;
    transition:
        opacity 1.6s ease-in-out,
        filter var(--bg-blur-fade, 2.2s) ease-in-out;
    will-change: opacity, filter;
}

.bg-image.active {
    opacity: 1;
}

/* 清楚阶段：当前图只保留很轻的柔化 */
.bg-scene:not(.is-soft) .bg-image.active {
    filter: blur(var(--bg-blur-clear)) saturate(1.08);
}

/* 虚化阶段：当前图整幅糊掉，停顿一会再换下一张 */
.bg-scene.is-soft .bg-image.active {
    filter: blur(var(--bg-blur-soft)) saturate(1.12);
}

/* 影调蒙层（背景列表里越靠前越在上层）：
 * 1. 两处暖色补光，用来中和偏蓝的背景图，代替旧版的紫/青染色
 * 2. 中性暗纱，统一压暗以保证前景文字可读
 * 3. 白色提亮层只抬暗部、几乎不抬高光，所以亮的背景图不会被冲爆
 */
.bg-mask {
    position: absolute;
    inset: 0;
    background:
        radial-gradient(120% 78% at 80% 6%, rgba(255, 208, 164, 0.09), transparent 62%),
        radial-gradient(110% 78% at 14% 98%, rgba(255, 178, 196, 0.07), transparent 60%),
        linear-gradient(180deg, rgba(8, 11, 20, 0.3), rgba(8, 11, 20, 0.5)),
        linear-gradient(180deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.05));
}

/* 粒子层：铺满视口的定位容器，本身不拦截事件 */
.bg-stars {
    position: absolute;
    inset: 0;
    overflow: hidden;
}

/* 粒子外层：定在视口的百分比位置上，只负责缓慢地飘来飘去
 * 具体时长/延迟由内联 style 注入，三条路径交替使用，避免所有点朝同方向移动
 */
.star {
    position: absolute;
    display: block;
    animation-timing-function: ease-in-out;
    animation-iteration-count: infinite;
    animation-direction: alternate;
    will-change: transform;
}

/* 飘荡路径 A：向右上飘 */
.drift-0 {
    animation-name: star-drift-a;
}

/* 飘荡路径 B：向左上飘 */
.drift-1 {
    animation-name: star-drift-b;
}

/* 飘荡路径 C：向右下飘 */
.drift-2 {
    animation-name: star-drift-c;
}

@keyframes star-drift-a {
    from {
        transform: translate3d(0, 0, 0);
    }

    to {
        transform: translate3d(28px, -22px, 0);
    }
}

@keyframes star-drift-b {
    from {
        transform: translate3d(0, 0, 0);
    }

    to {
        transform: translate3d(-32px, -15px, 0);
    }
}

@keyframes star-drift-c {
    from {
        transform: translate3d(0, 0, 0);
    }

    to {
        transform: translate3d(18px, 26px, 0);
    }
}

/* 粒子内层：萤火虫呼吸灯 —— 亮点涨到最大最亮，再连同外圈光晕一起缩到消失，
 * 暗一小会儿后重新涨起来；因为缩放作用在带 box-shadow 的元素上，光晕会同步变大变小
 */
.star-dot {
    display: block;
    border-radius: 50%;
    animation-name: star-breathe;
    animation-timing-function: ease-in-out;
    animation-iteration-count: infinite;
    will-change: opacity, transform;
}

@keyframes star-breathe {
    0% {
        opacity: 0;
        transform: scale(0.15);
    }

    22% {
        opacity: 1;
        transform: scale(1);
    }

    50% {
        opacity: 0.85;
        transform: scale(1.12);
    }

    78% {
        opacity: 0;
        transform: scale(0.15);
    }

    100% {
        opacity: 0;
        transform: scale(0.15);
    }
}

/* 弹幕层：整体降到 0.5 透明度，让文字呈现为纹理而不是脏污 */
.bg-danmaku {
    position: absolute;
    inset: 0;
    overflow: hidden;
    opacity: 0.5;
}

/* 单条弹幕：纯白半透明文字，从右向左匀速漂移 */
.dm {
    position: absolute;
    white-space: nowrap;
    color: rgba(255, 255, 255, 0.13);
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
}
</style>
