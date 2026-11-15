<template>
    <div class="compose-page">
        <PageHeader title="整理成文章" subtitle="机器负责分组排序，过渡句和结论由你补" />

        <!-- 顶部工具栏：返回、标题输入、生成按钮 -->
        <div class="compose-toolbar glass-card">
            <a-button type="text" class="back-btn" @click="goBack">
                <ArrowLeftOutlined />
                返回
            </a-button>
            <a-input v-model:value="title" class="title-input" placeholder="文章标题（留空则由系统生成）" :maxlength="255"
                allow-clear />
            <a-button type="primary" :loading="composing" :disabled="noteCount === 0" @click="submitCompose">
                <FileTextOutlined />
                生成草稿
            </a-button>
        </div>

        <!-- 统计条：参与条数、主题分布、未挂主题提示 -->
        <div v-if="preview" class="compose-stats glass-card">
            <span class="stat-item">
                <span class="stat-num">{{ preview.noteCount ?? 0 }}</span>
                <span class="stat-label">条碎片</span>
            </span>
            <span class="stat-divider"></span>
            <span class="stat-item">
                <span class="stat-num">{{ preview.topics?.length ?? 0 }}</span>
                <span class="stat-label">个主题</span>
            </span>
            <!-- 未挂主题提示：明确告诉用户哪些内容会被归入杂项 -->
            <a-alert v-if="(preview.untaggedCount ?? 0) > 0" type="warning" show-icon class="untagged-alert"
                :message="`有 ${preview.untaggedCount} 条碎片没挂主题，会被归入「杂项」`"
                description="可以返回列表补上主题后重新整理，也可以直接在编辑器里删掉这一节。" />
        </div>

        <!-- 主体：左侧主题分组，右侧 Markdown 预览 -->
        <div class="compose-body">
            <div class="compose-left glass-card">
                <div class="panel-title">
                    <AppstoreOutlined />
                    主题分组
                </div>
                <div v-if="loading" class="panel-loading">
                    <a-spin />
                </div>
                <div v-else-if="topics.length > 0" class="topic-list">
                    <div v-for="topic in topics" :key="String(topic.tagId)" class="topic-item">
                        <span class="topic-name" :class="{ untagged: topic.tagId === null }">
                            {{ topic.tagName }}
                        </span>
                        <span class="topic-count">{{ topic.count }} 条</span>
                    </div>
                </div>
                <p v-else class="panel-empty">没有可整理的碎片</p>
                <!-- 规则说明：让用户明白骨架是怎么来的，减少"AI 乱来"的疑虑 -->
                <div class="rule-hint">
                    <div class="hint-title">生成规则</div>
                    <ul>
                        <li>按碎片关联的<strong>第一个标签</strong>分组</li>
                        <li>组间按碎片数降序，「杂项」恒排最后</li>
                        <li>组内按创建时间升序，形成学习时间线</li>
                        <li>相邻小节之间会插入过渡提示注释</li>
                    </ul>
                </div>
            </div>

            <div class="compose-right glass-card">
                <div class="panel-title">
                    <CodeOutlined />
                    Markdown 预览
                </div>
                <div v-if="loading" class="panel-loading">
                    <a-spin />
                </div>
                <MdPreview v-else-if="markdown" v-model="markdown" preview-theme="vuepress" language="zh-CN"
                    class="md-preview" />
                <p v-else class="panel-empty">暂无内容</p>
            </div>
        </div>
    </div>
</template>

<!--
  NoteCompose：前台「碎知」整理预览页
  - 从列表页带着选中 id 进来，调 aggregate 拿 Markdown 骨架但不落库
  - 左栏展示主题分组与生成规则，右栏实时预览 Markdown
  - 点「生成草稿」调 compose，拿到 articleId 后跳转现有编辑器润色
  - 刻意不做「一键发布」：骨架只是起点，人工润色才是这一步的价值所在
-->
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { AppstoreOutlined, ArrowLeftOutlined, CodeOutlined, FileTextOutlined } from '@ant-design/icons-vue';
import { MdPreview } from 'md-editor-v3';
import 'md-editor-v3/lib/preview.css';
import { aggregate, compose } from '@/api/noteController';
import PageHeader from '@/components/PageHeader.vue';

const route = useRoute();
const router = useRouter();

/* 预览结果：标题、Markdown 正文与主题统计 */
const preview = ref<API.NoteAggregateVO | null>(null);

/* Markdown 预览内容：与 preview.markdown 同步 */
const markdown = ref('');

/* 标题输入框：预填后端生成的标题，用户可改 */
const title = ref('');

/* 加载与生成中状态 */
const loading = ref(false);
const composing = ref(false);

/* 参与整理的碎片条数 */
const noteCount = computed(() => preview.value?.noteCount ?? 0);

/* 主题分组列表 */
const topics = computed(() => preview.value?.topics ?? []);

/**
 * 从路由 query 解析选中的碎片 id
 * 雪花 ID 超过 JS 安全整数范围（2^53-1），全程按字符串处理，绝不做 Number() 转换，
 * 否则末几位会被静默改写，导致后端查不到对应碎片。
 * 这里返回字符串数组，调用 API 时再按后端要求的 number 类型做断言（运行时仍是字符串）。
 *
 * @returns 碎片 id 字符串数组，非法或缺失时返回空数组
 */
function parseNoteIds(): string[] {
    const raw = route.query.ids;
    if (typeof raw !== 'string' || !raw) return [];
    return raw
        .split(',')
        .map((item) => item.trim())
        .filter((id) => /^\d+$/.test(id));
}

/**
 * 拉取整理预览
 * 副作用：写入 preview / markdown / title；失败时给出提示并返回列表页
 */
async function loadPreview() {
    const noteIds = parseNoteIds();
    if (noteIds.length === 0) {
        message.warning('没有选中任何碎片');
        router.replace('/user/notes');
        return;
    }
    loading.value = true;
    try {
        // noteIds 运行时是字符串数组：ApiFox 生成的类型声明为 number[]，
        // 但后端 Long 以字符串收发，这里用类型断言绕过检查，绝不 Number() 转换
        const res = await aggregate({ noteIds: noteIds as unknown as number[], insertHints: true });
        const data = res.data?.data;
        if (data) {
            preview.value = data;
            markdown.value = data.markdown ?? '';
            title.value = data.title ?? '';
        }
    } catch {
        // 错误提示由请求拦截器统一处理，这里退回列表页避免停在空白页
        router.replace('/user/notes');
    } finally {
        loading.value = false;
    }
}

/**
 * 确认整理：生成草稿文章并跳转编辑器
 * 副作用：调用 compose；成功后带上 articleId 跳到现有写作页
 */
async function submitCompose() {
    const noteIds = parseNoteIds();
    if (noteIds.length === 0) {
        message.warning('没有选中任何碎片');
        return;
    }
    composing.value = true;
    try {
        const res = await compose({
            // 同 aggregate：运行时传字符串数组，仅断言绕过类型检查
            noteIds: noteIds as unknown as number[],
            title: title.value.trim() || undefined,
            insertHints: true,
        });
        const data = res.data?.data;
        if (data?.articleId) {
            message.success(`已生成草稿，共 ${data.noteCount ?? noteIds.length} 条碎片`);
            // 跳进现有编辑器继续润色，不在这里做发布
            router.push({ path: '/user/write', query: { id: String(data.articleId) } });
        }
    } finally {
        composing.value = false;
    }
}

// 返回列表页：碎片未被归档，可重新勾选
function goBack() {
    router.push('/user/notes');
}

onMounted(loadPreview);
</script>

<style scoped>
/* 页面主容器：纵向排列工具栏、统计条与主体 */
.compose-page {
    display: flex;
    flex-direction: column;
    gap: 14px;
}

/* 顶部工具栏：返回按钮、标题输入、生成按钮三段 */
.compose-toolbar {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 18px;
}

/* 返回按钮：弱化为主色文字，不抢主操作 */
.back-btn {
    color: var(--text-secondary);
    flex-shrink: 0;
}

/* 返回按钮悬停：提亮 */
.back-btn:hover {
    color: var(--accent);
}

/* 标题输入框：占满中间剩余空间 */
.title-input {
    flex: 1;
    min-width: 0;
    font-size: 15px;
    font-weight: 600;
}

/* 统计条：横向排列条数与主题数，未挂主题时下方多一条提示 */
.compose-stats {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 14px 22px;
    flex-wrap: wrap;
}

/* 单个统计项：数字与标签并排 */
.stat-item {
    display: inline-flex;
    align-items: baseline;
    gap: 6px;
}

/* 统计数字：放大加粗 */
.stat-num {
    font-size: 20px;
    font-weight: 700;
    color: var(--text-primary);
    font-variant-numeric: tabular-nums;
}

/* 统计标签：小字说明 */
.stat-label {
    font-size: 12.5px;
    color: var(--text-muted);
}

/* 统计项之间的竖线分隔 */
.stat-divider {
    width: 1px;
    height: 16px;
    background: var(--border-soft);
}

/* 未挂主题提示：占满整行，与统计项换行 */
.untagged-alert {
    width: 100%;
    margin-top: 4px;
}

/* 主体区：左侧分组窄、右侧预览宽 */
.compose-body {
    display: grid;
    grid-template-columns: 280px 1fr;
    gap: 14px;
    align-items: start;
}

/* 左右面板：统一内边距与纵向排列 */
.compose-left,
.compose-right {
    padding: 16px 18px;
    display: flex;
    flex-direction: column;
    gap: 14px;
    min-width: 0;
}

/* 面板标题：图标 + 文字，与其他页面卡片标题保持一致 */
.panel-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    font-weight: 600;
    color: var(--text-secondary);
    letter-spacing: 0.04em;
    text-transform: uppercase;
}

/* 面板加载态：居中 */
.panel-loading {
    display: flex;
    justify-content: center;
    padding: 40px 0;
}

/* 面板空态：弱化文字 */
.panel-empty {
    font-size: 13px;
    color: var(--text-muted);
    text-align: center;
    padding: 24px 0;
    margin: 0;
}

/* 主题列表：纵向排列每个主题 */
.topic-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

/* 单个主题项：名称与条数左右对齐 */
.topic-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
    padding: 8px 12px;
    border-radius: var(--radius-sm);
    background: rgba(255, 255, 255, 0.04);
    border: 1px solid var(--border-soft);
}

/* 主题名：加粗，作为分组标题 */
.topic-name {
    font-size: 13px;
    font-weight: 600;
    color: var(--text-primary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

/* 杂项主题：用橙色区分，提示这批碎片缺少主题 */
.topic-name.untagged {
    color: #fbbf24;
}

/* 主题条数：小字，右对齐 */
.topic-count {
    font-size: 12px;
    color: var(--text-muted);
    flex-shrink: 0;
    font-variant-numeric: tabular-nums;
}

/* 规则说明区：顶部一条分隔线，与上方列表拉开 */
.rule-hint {
    margin-top: 4px;
    padding-top: 12px;
    border-top: 1px dashed var(--border-soft);
    font-size: 12px;
    color: var(--text-muted);
    line-height: 1.8;
}

/* 规则标题：稍亮一档 */
.hint-title {
    font-weight: 600;
    color: var(--text-secondary);
    margin-bottom: 6px;
}

/* 规则列表：去掉默认缩进，保留项目符号 */
.rule-hint ul {
    margin: 0;
    padding-left: 16px;
}

/* 规则关键字：强调「第一个标签」这条最容易踩坑的约定 */
.rule-hint strong {
    color: var(--accent-light);
    font-weight: 600;
}

/* Markdown 预览：限制最大高度，避免页面被长文撑开 */
.md-preview {
    min-height: 420px;
    max-height: calc(100vh - 340px);
    overflow-y: auto;
    background: transparent;
    border-radius: var(--radius-sm);
}

/* 窄屏适配：主体改为上下堆叠 */
@media (max-width: 960px) {
    .compose-body {
        grid-template-columns: 1fr;
    }

    /* 预览区高度改为视口百分比，不再依赖固定减去值 */
    .md-preview {
        max-height: 60vh;
    }
}

/* 窄屏进一步收敛：工具栏换行，标题独占一行 */
@media (max-width: 720px) {
    .compose-toolbar {
        flex-wrap: wrap;
        gap: 8px;
    }

    .title-input {
        order: 3;
        width: 100%;
        flex-basis: 100%;
    }

    .compose-left,
    .compose-right {
        padding: 14px;
    }
}
</style>
