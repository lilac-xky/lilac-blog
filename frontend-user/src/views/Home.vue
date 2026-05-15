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
                            <div class="num num-pink">{{ totalViews.toLocaleString() }}</div>
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
import { listArticleVoByPage } from '@/api/articleController';
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

// === MOCK START === // TODO: 接入真实总浏览数后删除
const totalViews = 12345;
// === MOCK END ===

const stats = reactive({ articles: 0 });
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
        const a = await listArticleVoByPage(
            { current: 1, pageSize: 1, status: 2 },
            { silentError: true }
        );
        stats.articles = a.data?.data?.total ?? 0;
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

/* ========== 透卡覆写：个人卡 / 时间轴 / 标签云（中部歌词条不变） ========== */
.profile-card.glass-card,
.timeline.glass-card,
.tagcloud.glass-card {
    background: rgba(20, 18, 40, 0.28) !important;
    border-color: rgba(255, 255, 255, 0.06) !important;
}

/* 音乐播放器同样透一点（其 scoped 样式由本组件控制，这里通过 :deep 覆写） */
:deep(.music-player.glass-card) {
    background: rgba(20, 18, 40, 0.28) !important;
    border-color: rgba(255, 255, 255, 0.06) !important;
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
    border-color: rgba(56, 189, 248, 0.55);
    box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.15);
}

/* ========== top-grid ========== */
.top-grid {
    display: grid;
    grid-template-columns: 1.5fr 1fr;
    gap: 16px;
    align-items: stretch;
}

/* ========== 个人卡 ========== */
.profile-card {
    padding: 22px 24px 20px;
    display: flex;
    flex-direction: column;
    min-height: 264px;
    position: relative;
    overflow: hidden;
}

.profile-card::before {
    content: '';
    position: absolute;
    inset: auto auto -40% -10%;
    width: 60%;
    height: 60%;
    background: radial-gradient(circle, rgba(236, 72, 153, 0.18), transparent 70%);
    pointer-events: none;
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

.site-avatar {
    width: 88px;
    height: 88px;
    border-radius: 50%;
    object-fit: cover;
    display: block;
    border: 2px solid rgba(56, 189, 248, 0.5);
    box-shadow: 0 0 24px rgba(56, 189, 248, 0.32);
    background: linear-gradient(135deg, #1e3a5f, #2a4a7a);
}

.avatar-glow {
    position: absolute;
    inset: -8px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(56, 189, 248, 0.45), transparent 70%);
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

.hello-tag {
    display: inline-block;
    padding: 3px 11px;
    border-radius: var(--radius-pill);
    background: rgba(56, 189, 248, 0.12);
    border: 1px solid rgba(56, 189, 248, 0.25);
    color: var(--accent);
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.06em;
    margin-bottom: 8px;
}

.profile-name {
    font-size: 26px;
    font-weight: 800;
    background: linear-gradient(135deg, #f5f3ff, #7dd3fc 60%, #ec4899);
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

/* 统计行（含联系方式） */
.stats-line {
    margin-top: auto;
    padding-top: 14px;
    border-top: 1px dashed var(--border-soft);
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

.contact-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.04);
    border: 1px solid var(--border-soft);
    color: var(--text-secondary);
    cursor: pointer;
    transition: all 0.25s;
    font-size: 14px;
}

.contact-btn:hover {
    color: var(--accent);
    border-color: rgba(56, 189, 248, 0.55);
    background: rgba(56, 189, 248, 0.12);
    box-shadow: 0 0 18px rgba(56, 189, 248, 0.35);
    transform: translateY(-2px);
}

.stat {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
}

.num {
    font-size: 24px;
    font-weight: 800;
    line-height: 1;
    font-variant-numeric: tabular-nums;
}

.num-violet {
    color: var(--accent);
}

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
            rgba(56, 189, 248, 0.7),
            rgba(236, 72, 153, 0.6));
    border-radius: 2px;
}

.tl-col {
    display: flex;
    flex-direction: column;
    gap: 22px;
    position: relative;
}

.tl-col-right {
    margin-top: 56px;
    /* 右列整体下移，左右不严格对齐 */
}

.tl-card {
    position: relative;
    border-radius: var(--radius-sm);
    background: rgba(255, 255, 255, 0.04);
    border: 1px solid var(--border-soft);
    transition: all 0.25s;
}

.tl-card:hover {
    border-color: rgba(56, 189, 248, 0.5);
    background: rgba(56, 189, 248, 0.08);
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(56, 189, 248, 0.2);
}

.tl-card-link {
    display: block;
    text-decoration: none;
    color: var(--text-primary);
}

.tl-cover {
    height: 110px;
    overflow: hidden;
    border-radius: calc(var(--radius-sm) - 1px) calc(var(--radius-sm) - 1px) 0 0;
    position: relative;
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
    box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.18);
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
    box-shadow: 0 0 14px rgba(56, 189, 248, 0.7);
}

.tl-date {
    font-size: 11.5px;
    color: var(--accent);
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
    color: var(--accent);
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

.cloud-tag {
    display: inline-flex;
    align-items: center;
    padding: 4px 12px;
    border-radius: var(--radius-pill);
    border: 1px solid;
    background: rgba(255, 255, 255, 0.03);
    line-height: 1.4;
    font-weight: 600;
    letter-spacing: 0.02em;
    transition: all 0.25s;
    text-decoration: none;
}

.cloud-tag:hover {
    transform: translateY(-2px) scale(1.05);
    background: rgba(56, 189, 248, 0.12);
    border-color: rgba(56, 189, 248, 0.55) !important;
    box-shadow: 0 6px 20px rgba(56, 189, 248, 0.25);
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
    .search-btn {
        padding: 0 14px;
        font-size: 13px;
    }

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
