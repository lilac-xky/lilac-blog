<template>
    <div class="write-article">
        <header class="page-head">
            <h1>{{ isEdit ? '编辑文章' : '写文章' }}</h1>
            <p class="subtitle">
                <span class="dot-glow"></span>
                保存草稿随时改，提交后由管理员审核，通过后将在首页与归档展示
            </p>
        </header>

        <!-- 驳回提示条（仅编辑被驳回的草稿时出现） -->
        <div v-if="rejectReason" class="reject-banner glass-card">
            <ExclamationCircleOutlined />
            <div>
                <div class="reject-title">上次被驳回</div>
                <div class="reject-text">原因：{{ rejectReason }}</div>
            </div>
        </div>

        <!-- 标题 -->
        <div class="panel glass-card title-panel">
            <a-input v-model:value="form.title" placeholder="请输入文章标题..." class="title-input" :bordered="false"
                :maxlength="80" />
        </div>

        <div class="editor-layout">
            <!-- Markdown 编辑器 -->
            <div class="editor-wrap panel glass-card">
                <MdEditor v-model="form.content" :theme="'dark'" preview-theme="vuepress" language="zh-CN"
                    :on-upload-img="onUploadImg" class="md-editor" />
            </div>

            <!-- 右侧设置面板 -->
            <div class="settings-panel">
                <div class="panel glass-card setting-card">
                    <div class="setting-label">封面图</div>
                    <div class="cover-preview-wrap" @click="triggerCoverUpload">
                        <img v-if="form.coverUrl" :src="form.coverUrl" class="cover-preview" alt="封面" />
                        <div v-else class="cover-placeholder">
                            <LoadingOutlined v-if="coverUploading" />
                            <PictureOutlined v-else />
                            <span>点击上传</span>
                        </div>
                        <div v-if="form.coverUrl" class="cover-mask">
                            <CameraOutlined />
                            <span>更换</span>
                        </div>
                    </div>
                    <input ref="coverInputRef" type="file" accept="image/png,image/jpeg,image/jpg,image/webp"
                        style="display:none" @change="onCoverChange" />
                </div>

                <div class="panel glass-card setting-card">
                    <div class="setting-label">摘要</div>
                    <a-textarea v-model:value="form.summary" placeholder="一句话简介（可选）"
                        :auto-size="{ minRows: 3, maxRows: 5 }" :maxlength="200" show-count />
                </div>

                <div class="panel glass-card setting-card">
                    <div class="setting-label">分类</div>
                    <a-select v-model:value="form.categoryId" placeholder="选择分类" allow-clear style="width:100%"
                        :options="categoryOptions.map(c => ({ value: c.id, label: c.categoryName }))" />
                </div>

                <div class="panel glass-card setting-card">
                    <div class="setting-label">标签</div>
                    <a-select v-model:value="form.tagIds" mode="multiple" placeholder="选择标签" allow-clear
                        style="width:100%" :options="tagOptions.map(t => ({ value: t.id, label: t.tagName }))" />
                </div>

                <div class="panel glass-card setting-card action-card">
                    <a-button block @click="save(0)" :loading="saving">
                        <SaveOutlined />保存草稿
                    </a-button>
                    <a-button block type="primary" :loading="saving" style="margin-top:10px" @click="save(1)">
                        <SendOutlined />{{ isEdit && originalStatus === 1 ? '重新提交审核' : '提交审核' }}
                    </a-button>
                    <p class="action-hint">提交后管理员将进行审核，<br />通过后即在前台展示。</p>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { MdEditor } from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';
import {
    PictureOutlined,
    CameraOutlined,
    LoadingOutlined,
    SaveOutlined,
    SendOutlined,
    ExclamationCircleOutlined,
} from '@ant-design/icons-vue';
import {
    getArticle,
    submitMyArticle,
    updateMyArticle,
} from '@/api/articleController';
import { listCategoryByPageVo } from '@/api/categoryController';
import { listTagByPageVo } from '@/api/tagController';
import { uploadFile } from '@/api/fileController';

const route = useRoute();
const router = useRouter();

const articleId = ref<string | undefined>(
    typeof route.query.id === 'string' && route.query.id ? route.query.id : undefined,
);
const isEdit = computed(() => articleId.value !== undefined);
const originalStatus = ref<number | undefined>(undefined);
const rejectReason = ref<string>('');

interface WriteForm {
    title: string;
    content: string;
    summary: string;
    coverUrl: string;
    categoryId?: number;
    tagIds: number[];
}

const form = reactive<WriteForm>({
    title: '',
    content: '',
    summary: '',
    coverUrl: '',
    categoryId: undefined,
    tagIds: [],
});

const categoryOptions = ref<Array<{ id: number; categoryName: string }>>([]);
const tagOptions = ref<Array<{ id: number; tagName: string }>>([]);

const saving = ref(false);

onMounted(async () => {
    const [catRes, tagRes] = await Promise.all([
        listCategoryByPageVo({ current: 1, pageSize: 200 }, { silentError: true }),
        listTagByPageVo({ current: 1, pageSize: 200 }, { silentError: true }),
    ]);
    categoryOptions.value = (catRes.data?.data?.records ?? []) as Array<{ id: number; categoryName: string }>;
    tagOptions.value = (tagRes.data?.data?.records ?? []) as Array<{ id: number; tagName: string }>;

    if (articleId.value) {
        try {
            const res = await getArticle({ id: articleId.value as unknown as number });
            const vo = res.data?.data;
            if (vo) {
                if (vo.status === 2) {
                    message.warning('已发布的文章不可在前台直接编辑');
                    router.replace('/user/articles');
                    return;
                }
                form.title = vo.title ?? '';
                form.content = vo.content ?? '';
                form.summary = vo.summary ?? '';
                form.coverUrl = vo.coverUrl ?? '';
                form.categoryId = vo.categoryId ?? undefined;
                form.tagIds = ((vo.tags ?? []) as Array<{ id: number }>).map((t) => t.id);
                originalStatus.value = vo.status;
                rejectReason.value = vo.rejectReason ?? '';
            }
        } catch {
            message.error('加载文章失败');
        }
    }
});

// ---------- 封面上传 ----------
const coverInputRef = ref<HTMLInputElement | null>(null);
const coverUploading = ref(false);

function triggerCoverUpload() {
    coverInputRef.value?.click();
}

async function onCoverChange(e: Event) {
    const file = (e.target as HTMLInputElement).files?.[0];
    if (!file) return;
    if (file.size > 5 * 1024 * 1024) {
        message.error('图片不能超过 5MB');
        return;
    }
    coverUploading.value = true;
    try {
        const res = await uploadFile({ type: 'cover' }, {}, file);
        const result = res.data?.data;
        const url = result?.thumbnailUrl ?? result?.url;
        if (url) {
            form.coverUrl = url;
            message.success('封面上传成功');
        }
    } finally {
        coverUploading.value = false;
        if (coverInputRef.value) coverInputRef.value.value = '';
    }
}

async function onUploadImg(files: File[], callback: (urls: string[]) => void) {
    const urls: string[] = [];
    for (const file of files) {
        try {
            const res = await uploadFile({ type: 'content' }, {}, file);
            const url = res.data?.data?.url;
            if (url) urls.push(url);
        } catch {
            urls.push('');
        }
    }
    callback(urls);
}

// ---------- 保存 / 提交 ----------
async function save(targetStatus: 0 | 1) {
    if (targetStatus === 1) {
        if (!form.title.trim()) {
            message.warning('请输入文章标题');
            return;
        }
        if (!form.content.trim()) {
            message.warning('请输入文章内容');
            return;
        }
    }
    saving.value = true;
    try {
        if (isEdit.value && articleId.value) {
            const res = await updateMyArticle({
                id: articleId.value as unknown as number,
                title: form.title,
                summary: form.summary,
                content: form.content,
                coverUrl: form.coverUrl,
                categoryId: form.categoryId,
                tagIds: form.tagIds,
                status: targetStatus,
            });
            if (res.data?.data) {
                message.success(targetStatus === 1 ? '已提交，等待审核' : '草稿已保存');
                router.push('/user/articles');
            }
        } else {
            const res = await submitMyArticle({
                title: form.title,
                summary: form.summary,
                content: form.content,
                coverUrl: form.coverUrl,
                categoryId: form.categoryId,
                tagIds: form.tagIds,
                status: targetStatus,
            });
            if (res.data?.data) {
                message.success(targetStatus === 1 ? '已提交，等待审核' : '草稿已保存');
                router.push('/user/articles');
            }
        }
    } finally {
        saving.value = false;
    }
}
</script>

<style scoped>
.write-article {
    display: flex;
    flex-direction: column;
    gap: 14px;
}

.page-head h1 {
    font-size: 26px;
    font-weight: 700;
    margin-bottom: 6px;
}

.subtitle {
    color: var(--text-secondary);
    font-size: 13px;
    display: flex;
    align-items: center;
    gap: 8px;
}

.dot-glow {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--accent);
    box-shadow: 0 0 10px var(--accent);
}

.reject-banner {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 14px 20px;
    border: 1px solid rgba(239, 68, 68, 0.32);
    color: #fca5a5;
}

.reject-banner :deep(.anticon) {
    font-size: 22px;
}

.reject-title {
    font-weight: 600;
    margin-bottom: 2px;
}

.reject-text {
    font-size: 13px;
    color: rgba(245, 243, 255, 0.7);
}

.panel {
    padding: 14px 18px;
}

.title-panel {
    padding: 8px 18px;
}

.title-input {
    background: transparent !important;
    border: none !important;
}

:deep(.title-input input) {
    background: transparent !important;
    font-size: 22px;
    font-weight: 700;
    color: var(--text-primary) !important;
}

.editor-layout {
    display: flex;
    gap: 14px;
    align-items: flex-start;
}

.editor-wrap {
    flex: 1;
    min-width: 0;
    padding: 0;
    overflow: hidden;
}

.md-editor {
    height: calc(100vh - 320px);
    min-height: 480px;
    border-radius: var(--radius-card);
}

.settings-panel {
    width: 280px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    gap: 12px;
    max-height: calc(100vh - 320px);
    overflow-y: auto;
}

.setting-card {
    padding: 14px 18px;
}

.setting-label {
    font-size: 12px;
    font-weight: 600;
    color: var(--text-secondary);
    letter-spacing: 0.06em;
    text-transform: uppercase;
    margin-bottom: 10px;
}

.cover-preview-wrap {
    position: relative;
    width: 100%;
    height: 120px;
    border-radius: 10px;
    overflow: hidden;
    cursor: pointer;
    background: rgba(255, 255, 255, 0.04);
    border: 1.5px dashed var(--border-soft);
    display: flex;
    align-items: center;
    justify-content: center;
}

.cover-preview {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.cover-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    color: var(--text-muted);
    font-size: 22px;
}

.cover-placeholder span {
    font-size: 12px;
}

.cover-mask {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.45);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4px;
    opacity: 0;
    transition: opacity 0.2s;
    color: #fff;
    font-size: 20px;
}

.cover-mask span {
    font-size: 12px;
}

.cover-preview-wrap:hover .cover-mask {
    opacity: 1;
}

.action-card {
    text-align: center;
}

.action-hint {
    margin-top: 12px;
    font-size: 11px;
    color: var(--text-muted);
    line-height: 1.6;
}

/* 摘要等多行 textarea 局部覆写圆角（不动 App.vue 全局规则） */
.settings-panel :deep(textarea.ant-input) {
    border-radius: var(--radius-sm) !important;
    padding: 10px 12px !important;
    line-height: 1.6;
    resize: vertical;
}

.settings-panel :deep(.ant-input-textarea-show-count::after) {
    color: var(--text-muted);
    font-size: 11px;
}

/* ≤1200：右栏稍窄、间距收紧 */
@media (max-width: 1200px) {
    .settings-panel {
        width: 240px;
    }

    .panel {
        padding: 12px 14px;
    }
}

/* ≤960：编辑器与右栏改为上下堆叠，4 张设置卡 2×2 排列 */
@media (max-width: 960px) {
    .editor-layout {
        flex-direction: column;
        gap: 12px;
    }

    .settings-panel {
        width: 100%;
        max-height: none;
        overflow-y: visible;
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 10px;
    }

    .settings-panel .action-card {
        grid-column: 1 / -1;
    }

    .md-editor {
        height: 60vh;
        min-height: 420px;
    }
}

/* ≤768：标题与按钮收窄 */
@media (max-width: 768px) {
    .page-head h1 {
        font-size: 22px;
    }

    .subtitle {
        font-size: 12px;
    }

    .write-article {
        gap: 10px;
    }

    .panel {
        padding: 12px;
    }

    :deep(.title-input input) {
        font-size: 18px !important;
    }

    .md-editor {
        height: 55vh;
        min-height: 360px;
    }

    .reject-banner {
        padding: 12px 14px;
        gap: 10px;
    }
}

/* ≤480：单列布局 */
@media (max-width: 480px) {
    .settings-panel {
        grid-template-columns: 1fr;
    }

    .cover-preview-wrap {
        height: 100px;
    }

    .md-editor {
        height: 50vh;
        min-height: 320px;
    }

    .page-head h1 {
        font-size: 20px;
    }

    :deep(.title-input input) {
        font-size: 16px !important;
    }

    .action-hint {
        font-size: 10px;
    }
}

/* 兜底：md-editor 工具栏在窄屏溢出时允许横滚 */
.md-editor :deep(.md-editor-toolbar-wrapper) {
    overflow-x: auto;
}
</style>
