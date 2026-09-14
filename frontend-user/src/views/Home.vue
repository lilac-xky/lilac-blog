<template>
    <div class="home">
        <!-- 顶部：搜索条（无按钮，回车提交） -->
        <section class="search-bar glass-card">
            <SearchOutlined class="search-icon" />
            <input v-model="keyword" class="search-input" type="text" placeholder="搜索文章标题…" @keyup.enter="onSearch" />
        </section>

        <!-- 顶部双栏：个人卡 + 音乐播放器 -->
        <section class="top-grid">
            <!-- 个人卡（加高 + 内容上对齐，统计行集成联系方式） -->
            <div class="profile-card glass-card">
                <div class="profile-main">
                    <div class="avatar-wrap">
                        <img src="https://lilacs.oss-cn-beijing.aliyuncs.com/lilac-blog/avatar/2026/05/15/2026-05-15AokvgcjLCxWBMW9v.png?x-oss-process=image/resize,w_200"
                            alt="lilac-blog" class="site-avatar" />
                        <span class="avatar-glow"></span>
                    </div>
                    <div class="profile-info">
                        <div class="hello-tag">Hi there</div>
                        <h2 class="profile-name">lilac</h2>
                        <p class="profile-bio">
                            一个用来记录代码、踩坑与日常思考的小角落。<br />
                            愿在文字与星光之间，与你不期而遇。
                        </p>
                    </div>
                </div>

                <div class="stats-line">
                    <div class="stats-numbers">
                        <div class="stat">
                            <div class="num num-violet">{{ stats.articles }}</div>
                            <div class="label">文章</div>
                        </div>
                        <div class="stat-divider"></div>
                        <div class="stat">
                            <div class="num num-pink">{{ stats.totalViews.toLocaleString() }}</div>
                            <div class="label">总浏览</div>
                        </div>
                    </div>
                    <div class="stats-contacts">
                        <a class="contact-btn" :href="contacts.github" target="_blank" rel="noopener"
                            aria-label="GitHub">
                            <GithubOutlined />
                        </a>
                        <button type="button" class="contact-btn" @click="copyQQ" aria-label="QQ">
                            <QqOutlined />
                        </button>
                        <a class="contact-btn" :href="`mailto:${contacts.email}`" aria-label="Email">
                            <MailOutlined />
                        </a>
                    </div>
                </div>
            </div>

            <!-- 音乐播放器（仅 UI） -->
            <MusicPlayer />
        </section>

        <!-- 中间：跑马灯歌词条 -->
        <LyricsMarquee />

        <!-- 时间轴 -->
        <section class="timeline-section">
            <div class="section-head">
                <h3>时间轴</h3>
                <router-link to="/archive" class="more-link">查看全部 →</router-link>
            </div>

            <div v-if="loadingList" class="list-loading">
                <a-spin />
            </div>

            <div v-else-if="latest.length" class="timeline glass-card">
                <div class="timeline-line"></div>
                <div class="tl-col tl-col-left">
                    <article v-for="a in latestLeft" :key="a.id" class="tl-card tl-card-left">
                        <router-link :to="`/article/${a.id}`" class="tl-card-link">
                            <div v-if="a.coverUrl" class="tl-cover">
                                <img :src="a.coverUrl" :alt="a.title" loading="lazy" />
                            </div>
                            <div class="tl-meta">
                                <div class="tl-date">{{ formatDate(a.createTime) }}</div>
                                <h4 class="tl-title">{{ a.title }}</h4>
                                <p v-if="a.summary" class="tl-summary">{{ a.summary }}</p>
                                <div class="tl-foot">
                                    <EyeOutlined /> {{ a.viewCount ?? 0 }}
                                </div>
                            </div>
                        </router-link>
                        <span class="tl-dot"></span>
                    </article>
                </div>
                <div class="tl-col tl-col-right">
                    <article v-for="a in latestRight" :key="a.id" class="tl-card tl-card-right">
                        <router-link :to="`/article/${a.id}`" class="tl-card-link">
                            <div v-if="a.coverUrl" class="tl-cover">
                                <img :src="a.coverUrl" :alt="a.title" loading="lazy" />
                            </div>
                            <div class="tl-meta">
                                <div class="tl-date">{{ formatDate(a.createTime) }}</div>
                                <h4 class="tl-title">{{ a.title }}</h4>
                                <p v-if="a.summary" class="tl-summary">{{ a.summary }}</p>
                                <div class="tl-foot">
                                    <EyeOutlined /> {{ a.viewCount ?? 0 }}
                                </div>
                            </div>
                        </router-link>
                        <span class="tl-dot"></span>
                    </article>
                </div>
            </div>

            <div v-else class="empty-block glass-card">
                <FileSearchOutlined />
                <p>还没有文章哦</p>
            </div>
        </section>

        <!-- 标签云 -->
        <section v-if="tags.length" class="tagcloud-section">
            <div class="section-head">
                <h3>标签云</h3>
                <span class="cloud-hint">点击进入对应标签下的文章</span>
            </div>
            <div class="tagcloud glass-card">
                <router-link v-for="t in tags" :key="t.id" :to="`/archive?tagId=${t.id}`" class="cloud-tag"
                    :style="cloudStyle(t)">
                    #{{ t.tagName }}
                </router-link>
            </div>
        </section>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import {
    FileSearchOutlined,
    SearchOutlined,
    GithubOutlined,
    QqOutlined,
    MailOutlined,
    EyeOutlined,
} from '@ant-design/icons-vue';
import { getTotalViewCount, listArticleVoByPage } from '@/api/articleController';
import { listCategoryByPageVo } from '@/api/categoryController';
import { listTagByPageVo } from '@/api/tagController';
import MusicPlayer from '@/components/MusicPlayer.vue';
import LyricsMarquee from '@/components/LyricsMarquee.vue';

const router = useRouter();

// 联系方式
const contacts = {
    github: 'https://github.com/lilac-xky',
    qq: '1773484308',
    email: 'lilac-thc@qq.com',
};

const stats = reactive({ articles: 0, totalViews: 0 });
const latest = ref<API.ArticleVO[]>([]);
const latestLeft = computed(() => latest.value.filter((_, i) => i % 2 === 0));
const latestRight = computed(() => latest.value.filter((_, i) => i % 2 === 1));
const tags = ref<API.TagVO[]>([]);
const loadingList = ref(true);
const keyword = ref('');

function onSearch() {
    const q = keyword.value.trim();
    router.push({ path: '/archive', query: q ? { title: q } : {} });
}

async function copyQQ() {
    try {
        await navigator.clipboard.writeText(contacts.qq);
        message.success(`已复制 QQ 号 ${contacts.qq}`);
    } catch {
        message.info(`QQ：${contacts.qq}`);
    }
}

async function loadStats() {
    try {
        const [a, views] = await Promise.all([
            listArticleVoByPage(
                { current: 1, pageSize: 1, status: 2 },
                { silentError: true }
            ),
            getTotalViewCount({ silentError: true }),
        ]);
        stats.articles = a.data?.data?.total ?? 0;
        stats.totalViews = views.data?.data ?? 0;
    } catch {
        /* ignore */
    }
}

async function loadLatest() {
    loadingList.value = true;
    try {
        const res = await listArticleVoByPage(
            { current: 1, pageSize: 6, status: 2 },
            { silentError: true }
        );
        latest.value = res.data?.data?.records ?? [];
    } catch {
        latest.value = [];
    } finally {
        loadingList.value = false;
    }
}

async function loadTags() {
    try {
        const t = await listTagByPageVo(
            { current: 1, pageSize: 24 },
            { silentError: true }
        );
        tags.value = t.data?.data?.records ?? [];
    } catch {
        /* ignore */
    }
}

// 顺手拉取分类总数兼容旧统计接口（保留接口调用以便后续接入），但页面不再展示
async function warmupOthers() {
    try {
        await listCategoryByPageVo({ current: 1, pageSize: 1 }, { silentError: true });
    } catch {
        /* ignore */
    }
}

function formatDate(s?: string): string {
    if (!s) return '';
    return s.slice(0, 10);
}

const SIZE_BUCKETS = [13, 15, 17, 20, 24];
const COLOR_BUCKETS = ['var(--accent)', 'var(--accent-pink)', '#a78bfa', '#5eead4', '#fbbf24'];

function cloudStyle(t: API.TagVO) {
    const id = t.id ?? 0;
    const sizeIdx = Math.abs(id) % SIZE_BUCKETS.length;
    const colorIdx = Math.abs(Math.floor(id / 3)) % COLOR_BUCKETS.length;
    return {
        fontSize: `${SIZE_BUCKETS[sizeIdx]}px`,
        color: COLOR_BUCKETS[colorIdx],
        borderColor: 'rgba(255,255,255,0.12)',
    };
}

onMounted(() => {
    loadStats();
    loadLatest();
    loadTags();
    warmupOthers();
});
</script>

<style scoped>
.home {
    display: flex;
    flex-direction: column;
    gap: 22px;
}

/* ========== 卡片质感 ==========
 * 首页统一使用 utilities.css 里的 .glass-card（半透冷灰底 + 顶部高光 + 投影），
 * 不再对单张卡片做局部渐变覆写，避免出现“个人卡偏灰、播放器偏透”的不一致。
 * 这里只补一层中性白的左上高光，不使用带色相的染色，避免卡片整体发紫。
 */
.profile-card.glass-card,
.timeline.glass-card,
.tagcloud.glass-card {
    background-image: radial-gradient(120% 90% at 0% 0%, rgba(255, 255, 255, 0.07), transparent 58%);
}

/* ========== 搜索条 ========== */
.search-bar {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 14px 10px 20px;
    border-radius: var(--radius-pill);
    max-width: 640px;
    width: 100%;
    margin: 4px auto 6px;
    /* 比普通玻璃卡更亮一点，让顶部搜索条成为明确的入口而不是一片虚影 */
    background: rgba(255, 255, 255, 0.07);
    border-color: rgba(255, 255, 255, 0.16);
    box-shadow: 0 6px 24px -10px rgba(0, 0, 0, 0.5), inset 0 1px 0 rgba(255, 255, 255, 0.16);
}

.search-icon {
    color: var(--text-muted);
    font-size: 18px;
    flex: 0 0 auto;
}

.search-input {
    flex: 1;
    min-width: 0;
    height: 30px;
    background: transparent;
    border: none;
    outline: none;
    color: var(--text-primary);
    font-size: 14px;
    letter-spacing: 0.02em;
}

.search-input::placeholder {
    color: var(--text-muted);
}

.search-bar:focus-within {
    border-color: rgba(var(--accent-rgb), 0.55);
    box-shadow: 0 0 0 3px rgba(var(--accent-rgb), 0.15);
}

/* ========== top-grid ========== */
.top-grid {
    display: grid;
    grid-template-columns: 1.5fr 1fr;
    gap: 16px;
    align-items: stretch;
}

/* 个人卡容器：内容纵向分布，统计行用 margin-top:auto 钉底 */
.profile-card {
    padding: 22px 24px 20px;
    display: flex;
    flex-direction: column;
    min-height: 264px;
    position: relative;
    overflow: hidden;
}

.profile-main {
    display: flex;
    align-items: center;
    gap: 18px;
    /* 内容贴顶，statsLine 用 margin-top:auto 钉底 */
}

.avatar-wrap {
    position: relative;
    flex: 0 0 auto;
}

/* 头像：圆角方图 + 紫罗兰描边，比纯圆形更有“卡片头像”的轮廓感 */
.site-avatar {
    width: 92px;
    height: 92px;
    border-radius: 20px;
    object-fit: cover;
    display: block;
    border: 2px solid rgba(var(--accent-rgb), 0.6);
    box-shadow: 0 0 26px rgba(var(--accent-rgb), 0.45), 0 8px 22px -10px rgba(0, 0, 0, 0.8);
    background: var(--gradient-card-default);
}

/* 头像外发光：跟随头像形状的紫色光晕，做呼吸动画 */
.avatar-glow {
    position: absolute;
    inset: -10px;
    border-radius: 26px;
    background: radial-gradient(circle, rgba(var(--accent-rgb), 0.5), transparent 70%);
    filter: blur(16px);
    z-index: -1;
    animation: avatar-pulse 4s ease-in-out infinite;
}

@keyframes avatar-pulse {

    0%,
    100% {
        opacity: 0.6;
        transform: scale(1);
    }

    50% {
        opacity: 1;
        transform: scale(1.08);
    }
}

.profile-info {
    flex: 1;
    min-width: 0;
}

/* “Hi there”胶囊标签：亮紫文字 + 淡紫底，作为卡片开场的视觉引导 */
.hello-tag {
    display: inline-block;
    padding: 3px 11px;
    border-radius: var(--radius-pill);
    background: rgba(var(--accent-rgb), 0.16);
    border: 1px solid rgba(var(--accent-rgb), 0.32);
    color: var(--accent-light);
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.06em;
    margin-bottom: 8px;
}

/* 昵称：接近纯白的浅青渐变，沿用站点原有的蓝青配色 */
.profile-name {
    font-size: 28px;
    font-weight: 800;
    letter-spacing: 0.01em;
    background: linear-gradient(135deg, #ffffff, #e0f2fe 55%, #7dd3fc);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    margin-bottom: 6px;
}

.profile-bio {
    color: var(--text-secondary);
    font-size: 14px;
    line-height: 1.75;
}

/* 统计行（含联系方式）：去掉虚线分隔，改用留白区分上下两组信息 */
.stats-line {
    margin-top: auto;
    padding-top: 18px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 14px;
    flex-wrap: wrap;
}

.stats-numbers {
    display: flex;
    align-items: center;
    gap: 22px;
}

.stats-contacts {
    display: flex;
    align-items: center;
    gap: 8px;
}

/* 联系方式圆钮：清晰的描边与浅底，避免旧版在深色卡片上“看不见按钮” */
.contact-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 34px;
    height: 34px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.07);
    border: 1px solid rgba(255, 255, 255, 0.16);
    color: rgba(232, 234, 252, 0.82);
    cursor: pointer;
    transition: all 0.25s;
    font-size: 15px;
}

.contact-btn:hover {
    color: #fff;
    border-color: rgba(var(--accent-rgb), 0.7);
    background: rgba(var(--accent-rgb), 0.28);
    box-shadow: 0 0 18px rgba(var(--accent-rgb), 0.45);
    transform: translateY(-2px);
}

.stat {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
}

.num {
    font-size: 26px;
    font-weight: 800;
    line-height: 1;
    font-variant-numeric: tabular-nums;
}

/* 文章数：亮紫，深色底上比主色更醒目 */
.num-violet {
    color: var(--accent-light);
}

/* 浏览数：品牌粉，与亮紫形成冷暖对比 */
.num-pink {
    color: var(--accent-pink);
}

.label {
    font-size: 13px;
    color: var(--text-muted);
    letter-spacing: 0.08em;
}

.stat-divider {
    width: 1px;
    height: 32px;
    background: var(--border-soft);
}

/* ========== section-head 复用 ========== */
.section-head {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    margin-bottom: 12px;
}

.section-head h3 {
    font-size: 18px;
    font-weight: 700;
    position: relative;
    padding-left: 14px;
}

.section-head h3::before {
    content: '';
    position: absolute;
    left: 0;
    top: 6px;
    bottom: 6px;
    width: 4px;
    border-radius: 4px;
    background: linear-gradient(180deg, var(--accent), var(--accent-pink));
}

.more-link,
.cloud-hint {
    color: var(--text-secondary);
    font-size: 13px;
}

.more-link:hover {
    color: var(--accent);
}

.list-loading {
    text-align: center;
    padding: 32px 0;
}

.empty-block {
    padding: 60px 12px;
    text-align: center;
    color: var(--text-muted);
}

.empty-block :deep(.anticon) {
    font-size: 36px;
    margin-bottom: 10px;
}

/* ========== 时间轴（居中竖线，左右双列独立堆叠） ========== */
.timeline {
    position: relative;
    padding: 24px 22px;
    display: grid;
    grid-template-columns: 1fr 1fr;
    column-gap: 64px;
}

.timeline-line {
    position: absolute;
    left: 50%;
    top: 24px;
    bottom: 24px;
    width: 2px;
    transform: translateX(-50%);
    background: linear-gradient(180deg,
            rgba(var(--accent-rgb), 0.7),
            rgba(var(--accent-pink-rgb), 0.6));
    border-radius: 2px;
}

.tl-col {
    display: flex;
    flex-direction: column;
    gap: 22px;
    position: relative;
}

/* 右列整体下移半张封面高度，做出错落的瀑布感（封面比例统一后不会显得散乱） */
.tl-col-right {
    margin-top: 44px;
}

/* 时间轴小卡片：比外层容器略亮一档，形成“容器 > 卡片”的两级层次
 * 注意不能加 overflow:hidden，否则会把定位在卡片外侧的时间轴圆点裁掉
 */
.tl-card {
    position: relative;
    border-radius: var(--radius-sm);
    background: rgba(255, 255, 255, 0.055);
    border: 1px solid rgba(255, 255, 255, 0.1);
    transition: all 0.25s;
}

.tl-card:hover {
    border-color: rgba(var(--accent-rgb), 0.55);
    background: rgba(var(--accent-rgb), 0.12);
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(var(--accent-rgb), 0.25);
}

.tl-card-link {
    display: block;
    text-decoration: none;
    color: var(--text-primary);
}

/* 封面：按固定宽高比铺满卡片宽度，避免旧版固定 110px 高度在不同列宽下被压成细条 */
.tl-cover {
    aspect-ratio: 5 / 2;
    overflow: hidden;
    border-radius: calc(var(--radius-sm) - 1px) calc(var(--radius-sm) - 1px) 0 0;
    position: relative;
    background: var(--bg-page-2);
}

.tl-cover img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s ease;
}

.tl-card:hover .tl-cover img {
    transform: scale(1.06);
}

.tl-meta {
    padding: 12px 16px 14px;
}

/* 圆点：左卡片在右边、右卡片在左边，落在中央竖线上 */
.tl-dot {
    position: absolute;
    top: 28px;
    width: 14px;
    height: 14px;
    border-radius: 50%;
    background: var(--bg-page-2);
    border: 2px solid var(--accent);
    box-shadow: 0 0 0 3px rgba(var(--accent-rgb), 0.18);
    z-index: 2;
    transition: all 0.25s;
}

.tl-card-left .tl-dot {
    right: -39px;
}

.tl-card-right .tl-dot {
    left: -39px;
}

.tl-card:hover .tl-dot {
    background: var(--accent);
    box-shadow: 0 0 14px rgba(var(--accent-rgb), 0.7);
}

/* 时间轴日期：亮紫小字，作为卡片的视觉锚点 */
.tl-date {
    font-size: 11.5px;
    color: var(--accent-light);
    font-weight: 600;
    letter-spacing: 0.06em;
    margin-bottom: 6px;
    font-variant-numeric: tabular-nums;
}

.tl-title {
    font-size: 15px;
    font-weight: 600;
    color: var(--text-primary);
    transition: color 0.2s;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 1;
    line-clamp: 1;
    -webkit-box-orient: vertical;
}

.tl-card:hover .tl-title {
    color: var(--accent-light);
}

.tl-summary {
    margin-top: 6px;
    font-size: 12.5px;
    color: var(--text-secondary);
    line-height: 1.6;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
}

.tl-foot {
    margin-top: 8px;
    font-size: 11.5px;
    color: var(--text-muted);
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-variant-numeric: tabular-nums;
}

/* ========== 标签云 ========== */
.tagcloud {
    padding: 22px 24px;
    display: flex;
    flex-wrap: wrap;
    gap: 10px 14px;
    align-items: center;
    justify-content: center;
}

/* 标签云单个标签：底色由页面内联样式注入颜色，这里只负责形状与悬停反馈 */
.cloud-tag {
    display: inline-flex;
    align-items: center;
    padding: 4px 12px;
    border-radius: var(--radius-pill);
    border: 1px solid;
    background: rgba(255, 255, 255, 0.06);
    line-height: 1.4;
    font-weight: 600;
    letter-spacing: 0.02em;
    transition: all 0.25s;
    text-decoration: none;
}

.cloud-tag:hover {
    transform: translateY(-2px) scale(1.05);
    background: rgba(var(--accent-rgb), 0.12);
    border-color: rgba(var(--accent-rgb), 0.55) !important;
    box-shadow: 0 6px 20px rgba(var(--accent-rgb), 0.25);
}

/* ========== 响应式 ========== */
@media (max-width: 960px) {
    .top-grid {
        grid-template-columns: 1fr;
    }

    .profile-card {
        min-height: auto;
    }
}

@media (max-width: 640px) {
    .profile-main {
        flex-direction: column;
        align-items: flex-start;
        gap: 14px;
    }

    .stats-line {
        flex-direction: column;
        align-items: flex-start;
        gap: 14px;
    }

    .stats-contacts {
        align-self: flex-start;
    }

    .timeline {
        grid-template-columns: 1fr;
        column-gap: 0;
        padding: 16px 14px 16px 38px;
    }

    .timeline-line {
        left: 18px;
        transform: none;
    }

    .tl-col {
        gap: 16px;
    }

    .tl-col-right {
        margin-top: 16px;
    }

    .tl-card-left .tl-dot,
    .tl-card-right .tl-dot {
        left: -27px;
        right: auto;
        top: 22px;
    }
}
</style>
