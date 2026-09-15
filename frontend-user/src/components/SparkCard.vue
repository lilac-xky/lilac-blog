<template>
    <article class="spark-card glass-card hover-lift">
        <!-- 卡片头部：头像链回主页，右侧是站点名 + 记录日期 -->
        <header class="spark-head">
            <router-link to="/" class="spark-avatar" :title="`返回 ${SITE_NAME} 主页`">
                <img :src="SITE_AVATAR_URL" :alt="SITE_NAME" loading="lazy" />
            </router-link>
            <div class="spark-who">
                <router-link to="/" class="spark-name">{{ SITE_NAME }}</router-link>
                <span class="spark-time">
                    <ClockCircleOutlined />
                    {{ formatDate(spark.createTime, 'YYYY-MM-DD') }}
                </span>
            </div>
        </header>

        <!-- 正文：保留换行与空格，未填写内容时给一句占位文案 -->
        <p class="spark-content">{{ spark.content || '（这条灵感没有留下文字）' }}</p>
    </article>
</template>

<!--
  SparkCard：拾灵集（灵感）单条卡片
  - 结构：头像 + 名字/日期 + 正文，无定位、无评论等附加区
  - 头像为圆角方形并链回主页，名字同样链回主页，作为「同一个人写的」统一身份锚点
  - 日期只精确到「日」，不展示具体时分
  - 纯展示组件：不发起请求、不持有状态，数据全部由父级通过 props 注入
-->
<script setup lang="ts">
import { ClockCircleOutlined } from '@ant-design/icons-vue';
import { useDateFormat } from '@/composables/useDateFormat';

/* 卡片署名：前台统一用站点名，和首页个人卡保持同一个名字 */
const SITE_NAME = 'Lilac';

/* 卡片头像地址：与页头 Logo 使用同一张 OSS 图，不额外引入公共配置文件 */
const SITE_AVATAR_URL =
    'https://lilacs.oss-cn-beijing.aliyuncs.com/lilac-blog/avatar/2026/05/15/2026-05-15AokvgcjLCxWBMW9v.png?x-oss-process=image/resize,w_200';

// 单条灵感数据：由拾灵集页面注入，卡片自身不做任何请求
defineProps<{ spark: API.SparkVO }>();

// 时间格式化：统一走前台通用写法，避免各页面自己拼日期串
const { formatDate } = useDateFormat();
</script>

<style scoped>
/* 单条灵感卡片：中性冷灰玻璃底，整体加大一档以承载更长的正文 */
.spark-card {
    padding: 24px 26px 26px;
    background-image: radial-gradient(120% 90% at 0% 0%, rgba(255, 255, 255, 0.07), transparent 58%);
}

/* 卡片头部：头像与文字左右排布，头像不参与压缩 */
.spark-head {
    display: flex;
    align-items: center;
    gap: 14px;
    margin-bottom: 16px;
}

/* 头像链：圆角方形裁切 + 主色描边，作为可点击的主页入口 */
.spark-avatar {
    flex: 0 0 auto;
    width: 54px;
    height: 54px;
    border-radius: 14px;
    overflow: hidden;
    display: block;
    border: 1px solid var(--border-accent);
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

/* 头像图片：铺满圆角方形触发区 */
.spark-avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
}

/* 头像悬停：描边与光晕提亮，表明可以点回主页 */
.spark-avatar:hover {
    border-color: var(--border-accent-strong);
    box-shadow: 0 0 16px rgba(var(--accent-rgb), 0.45);
}

/* 名字与日期：纵向排列，日期贴名字下方一行 */
.spark-who {
    display: flex;
    flex-direction: column;
    gap: 5px;
    min-width: 0;
}

/* 名字：站点名，放大加粗后作为卡片的视觉主标识 */
.spark-name {
    font-size: 17px;
    font-weight: 700;
    color: var(--text-primary);
    letter-spacing: 0.02em;
    transition: color 0.2s ease;
}

/* 名字悬停：转到主色，强化链接感 */
.spark-name:hover {
    color: var(--accent-light);
}

/* 日期行：比名字小一档但明显大于辅助文字，只到「日」 */
.spark-time {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    color: var(--text-muted);
    font-variant-numeric: tabular-nums;
}

/* 正文：保留原始换行与连续空格，长文本按词换行 */
.spark-content {
    font-size: 15px;
    line-height: 1.9;
    color: var(--text-secondary);
    white-space: pre-wrap;
    overflow-wrap: anywhere;
}

@media (max-width: 720px) {
    /* 移动端：卡片内边距收紧，正文略降一档字号避免密集 */
    .spark-card {
        padding: 18px;
    }

    .spark-avatar {
        width: 48px;
        height: 48px;
        border-radius: 12px;
    }

    .spark-name {
        font-size: 16px;
    }

    .spark-content {
        font-size: 14px;
    }
}
</style>
