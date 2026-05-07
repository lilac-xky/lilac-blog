<template>
  <div class="write-blog">
    <!-- 标题行 -->
    <div class="panel title-panel">
      <a-input v-model:value="form.title" placeholder="请输入文章标题..." class="title-input" :bordered="false" allow-clear />
    </div>

    <!-- 主体区域 -->
    <div class="editor-layout">
      <!-- Markdown 编辑器 -->
      <div class="editor-wrap panel">
        <MdEditor v-model="form.content" :theme="'light'" language="zh-CN" :on-upload-img="onUploadImg"
          class="md-editor" />
      </div>

      <!-- 右侧设置面板 -->
      <div class="settings-panel">
        <!-- 封面图 -->
        <div class="panel setting-card">
          <div class="setting-label">封面图</div>
          <div class="cover-preview-wrap" @click="triggerCoverUpload">
            <img v-if="form.coverUrl" :src="form.coverUrl" class="cover-preview" alt="封面预览" />
            <div v-else class="cover-placeholder">
              <LoadingOutlined v-if="coverUploading" />
              <PictureOutlined v-else />
              <span>点击上传封面</span>
            </div>
            <div v-if="form.coverUrl" class="cover-mask">
              <LoadingOutlined v-if="coverUploading" />
              <CameraOutlined v-else />
              <span>更换封面</span>
            </div>
          </div>
          <input ref="coverInputRef" type="file" accept="image/png,image/jpeg,image/jpg,image/webp"
            style="display: none" @change="handleCoverFileChange" />
          <a-input v-model:value="form.coverUrl" placeholder="或输入封面图链接" size="small" allow-clear
            style="margin-top: 8px" />
        </div>

        <!-- 摘要 -->
        <div class="panel setting-card">
          <div class="setting-label">摘要</div>
          <a-textarea v-model:value="form.summary" placeholder="填写文章摘要（可选）" :auto-size="{ minRows: 3, maxRows: 5 }"
            allow-clear />
        </div>

        <!-- 状态 & 置顶 -->
        <div class="panel setting-card">
          <div class="setting-label">发布设置</div>
          <div class="setting-row">
            <span class="setting-row-label">状态</span>
            <a-radio-group v-model:value="form.status" size="small">
              <a-radio-button :value="0">草稿</a-radio-button>
              <a-radio-button :value="2">发布</a-radio-button>
            </a-radio-group>
          </div>
          <div class="setting-row">
            <span class="setting-row-label">置顶</span>
            <a-switch :checked="form.isTop === 1" checked-children="是" un-checked-children="否"
              @change="(val: boolean) => (form.isTop = val ? 1 : 0)" />
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="panel setting-card action-card">
          <a-button block type="primary" :loading="saving" @click="save">
            {{ form.status === 2 ? '发布文章' : '保存草稿' }}
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { MdEditor } from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';
import {
  PictureOutlined,
  CameraOutlined,
  LoadingOutlined,
} from '@ant-design/icons-vue';
import { addArticle, updateArticle, getArticle } from '@/api/articleController';
import { uploadFile } from '@/api/fileController';

const route = useRoute();
const router = useRouter();

const articleId = ref<string | undefined>(
    typeof route.query.id === 'string' ? route.query.id : undefined,
);

// ---------- 表单 ----------
const form = reactive<API.ArticleAddRequest & { id?: string }>({
  title: '',
  content: '',
  summary: '',
  coverUrl: '',
  status: 0,
  isTop: 0,
});

// ---------- 加载已有文章 ----------
onMounted(async () => {
  if (!articleId.value) return;
  try {
    const res = await getArticle({id: articleId.value as unknown as number});
    const vo = res.data?.data;
    if (vo) {
      form.id = vo.id as unknown as string;
      form.title = vo.title ?? '';
      form.content = vo.content ?? '';
      form.summary = vo.summary ?? '';
      form.coverUrl = vo.coverUrl ?? '';
      form.status = vo.status ?? 0;
      form.isTop = vo.isTop ?? 0;
    }
  } catch {
    message.error('加载文章失败');
  }
});

// ---------- 封面上传 ----------
const coverInputRef = ref<HTMLInputElement | null>(null);
const coverUploading = ref(false);

function triggerCoverUpload() {
  coverInputRef.value?.click();
}

async function handleCoverFileChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0];
  if (!file) return;

  const allowed = ['image/png', 'image/jpeg', 'image/jpg', 'image/webp'];
  if (!allowed.includes(file.type)) {
    message.error('仅支持 PNG / JPG / WEBP 格式');
    return;
  }
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

// ---------- 编辑器内图片上传 ----------
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

// ---------- 保存 / 发布 ----------
const saving = ref(false);

async function save() {
  if (!form.title?.trim()) {
    message.warning('请输入文章标题');
    return;
  }
  if (!form.content?.trim()) {
    message.warning('请输入文章内容');
    return;
  }

  const isPublish = form.status === 2;
  saving.value = true;
  try {
    if (articleId.value) {
      const res = await updateArticle({...form, id: articleId.value as unknown as number});
      if (res.data?.data) {
        message.success(isPublish ? '发布成功' : '草稿已保存');
        router.push('/article/manage');
      }
    } else {
      const res = await addArticle({ ...form });
      if (res.data?.data) {
        message.success(isPublish ? '发布成功' : '草稿已保存');
        router.push('/article/manage');
      }
    }
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped>
.write-blog {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
}

.panel {
  background: var(--bg-card);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
}

.title-panel {
  padding: 8px 16px;
  flex-shrink: 0;
}

.title-input {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}

:deep(.title-input input) {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary) !important;
}

:deep(.title-input input::placeholder) {
  color: var(--text-muted);
  font-weight: 400;
}

/* 编辑器 + 设置侧边栏 */
.editor-layout {
  display: flex;
  gap: 12px;
  flex: 1;
  min-height: 0;
  align-items: flex-start;
}

.editor-wrap {
  flex: 1;
  min-width: 0;
  padding: 0;
  overflow: hidden;
}

.md-editor {
  height: calc(100vh - 240px);
  border-radius: var(--radius-card);
}

/* 右侧设置面板 */
.settings-panel {
  width: 260px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.setting-card {
  padding: 14px 16px;
}

.setting-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  margin-bottom: 10px;
}

/* 封面图 */
.cover-preview-wrap {
  position: relative;
  width: 100%;
  height: 120px;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  background: var(--bg-page);
  border: 1.5px dashed var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: var(--text-muted);
  font-size: 24px;
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

/* 发布设置 */
.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.setting-row:last-child {
  margin-bottom: 0;
}

.setting-row-label {
  font-size: 13px;
  color: var(--text-primary);
}

/* 操作按钮卡片 */
.action-card {
  padding: 14px 16px;
}

/* md-editor 样式覆盖 */
:deep(.md-editor) {
  border: none !important;
  border-radius: var(--radius-card) !important;
}

:deep(.md-editor-toolbar-wrapper) {
  border-radius: var(--radius-card) var(--radius-card) 0 0 !important;
}
</style>
