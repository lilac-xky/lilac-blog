<template>
    <canvas ref="canvasEl" class="click-ripple" aria-hidden="true"></canvas>
</template>

<!--
  ClickRipple：主站全局点击水波反馈层
  - 仅在用户按下指针时生成圆形扩散水波，不跟随光标移动
  - 使用单一 requestAnimationFrame 循环绘制，并限制同时存在的水波数量
  - 系统开启减少动态效果时自动停用，不拦截页面点击与输入
-->
<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue';

interface Ripple {
    x: number;
    y: number;
    radius: number;
    opacity: number;
    lineWidth: number;
}

const MAX_RIPPLES = 8;
const canvasEl = ref<HTMLCanvasElement | null>(null);
const ripples: Ripple[] = [];
let context: CanvasRenderingContext2D | null = null;
let animationId = 0;
let reduceMotion: MediaQueryList;

// 根据视口与设备像素比同步画布尺寸，保证水波边缘清晰且控制高分屏开销。
function resizeCanvas() {
    const canvas = canvasEl.value;
    if (!canvas) return;

    const pixelRatio = Math.min(window.devicePixelRatio || 1, 2);
    canvas.width = Math.round(window.innerWidth * pixelRatio);
    canvas.height = Math.round(window.innerHeight * pixelRatio);
    canvas.style.width = `${window.innerWidth}px`;
    canvas.style.height = `${window.innerHeight}px`;
    context = canvas.getContext('2d');
    context?.setTransform(pixelRatio, 0, 0, pixelRatio, 0, 0);
}

// 在点击坐标创建一圈水波，并限制数量以避免连续点击造成资源堆积。
function addRipple(x: number, y: number) {
    ripples.push({
        x,
        y,
        radius: 4,
        opacity: 0.52,
        lineWidth: 2,
    });

    if (ripples.length > MAX_RIPPLES) {
        ripples.splice(0, ripples.length - MAX_RIPPLES);
    }
}

// 绘制并推进所有点击水波，在透明度耗尽后移除对应状态。
function animate() {
    if (!context) return;
    const ctx = context;

    context.clearRect(0, 0, window.innerWidth, window.innerHeight);
    for (let index = ripples.length - 1; index >= 0; index -= 1) {
        const ripple = ripples[index]!;
        ripple.radius += 0.85;
        ripple.opacity -= 0.009;
        ripple.lineWidth = Math.max(0.7, ripple.lineWidth * 0.985);

        // 绘制错开半径的双层细波纹，模拟水面荡漾并控制在较小范围内。
        const rings = [
            { radius: ripple.radius, alpha: ripple.opacity },
            { radius: Math.max(2, ripple.radius - 15), alpha: ripple.opacity * 0.55 },
        ];
        rings.forEach((ring) => {
            ctx.beginPath();
            ctx.arc(ripple.x, ripple.y, ring.radius, 0, Math.PI * 2);
            ctx.strokeStyle = `rgba(56, 189, 248, ${Math.max(0, ring.alpha)})`;
            ctx.lineWidth = ripple.lineWidth;
            ctx.shadowColor = `rgba(125, 211, 252, ${Math.max(0, ring.alpha * 0.35)})`;
            ctx.shadowBlur = 4;
            ctx.stroke();
        });

        if (ripple.opacity <= 0) {
            ripples.splice(index, 1);
        }
    }

    animationId = requestAnimationFrame(animate);
}

// 响应鼠标、触控笔和触摸点击，在按下位置生成一次水波反馈。
function onPointerDown(event: PointerEvent) {
    if (!reduceMotion.matches) {
        addRipple(event.clientX, event.clientY);
    }
}

// 组件挂载后初始化画布、全局点击监听和水波动画循环。
onMounted(() => {
    reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)');
    resizeCanvas();
    window.addEventListener('resize', resizeCanvas, { passive: true });
    window.addEventListener('pointerdown', onPointerDown, { passive: true });
    animationId = requestAnimationFrame(animate);
});

// 组件卸载时移除全局监听并释放动画资源，避免主布局退出后残留副作用。
onBeforeUnmount(() => {
    window.removeEventListener('resize', resizeCanvas);
    window.removeEventListener('pointerdown', onPointerDown);
    cancelAnimationFrame(animationId);
    ripples.splice(0);
});
</script>

<style scoped>
/* 覆盖主站视口以绘制点击水波，并确保该层不参与任何指针命中。 */
.click-ripple {
    position: fixed;
    inset: 0;
    z-index: 60;
    display: block;
    pointer-events: none;
}

@media (prefers-reduced-motion: reduce) {

    /* 系统要求减少动态效果时隐藏全局点击水波层。 */
    .click-ripple {
        display: none;
    }
}
</style>
