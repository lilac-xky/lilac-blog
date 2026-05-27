<template>
  <div class="article-review">
    <!-- 搜索面板 -->
    <div class="panel search-panel">
      <div class="panel-header tight">
        <div class="panel-title small">待审核文章</div>
      </div>
      <a-form layout="inline" :model="queryForm" class="search-form">
        <a-form-item label="标题">
          <a-input v-model:value="queryForm.title" placeholder="标题关键词" allow-clear style="width: 200px"
            @press-enter="handleSearch" />
        </a-form-item>
        <a-form-item label="分类">
          <a-select v-model:value="queryForm.categoryId" placeholder="全部" allow-clear style="width: 140px"
            :options="categoryOptions.map(c => ({ value: c.id, label: c.categoryName }))" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" :loading="loading" @click="handleSearch">
            <template #icon>
              <SearchOutlined />
            </template>
            搜索
          </a-button>
          <a-button style="margin-left: 8px" @click="handleReset">重置</a-button>
        </a-form-item>
      </a-form>
    </div>

    <!-- 表格 -->
    <div class="panel table-panel">
      <div class="panel-header tight">
        <div class="panel-title small">
          审核队列
          <span class="total-badge">共 {{ total }} 篇待处理</span>
        </div>
      </div>

      <a-table :columns="columns" :data-source="tableData" :loading="loading" :pagination="false" row-key="id"
        size="middle" class="article-table" :scroll="{ x: 1280 }" :show-sorter-tooltip="false"
        @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <!-- 封面 -->
          <template v-if="column.key === 'cover'">
            <div class="cover-wrap">
              <img v-if="record.coverUrl" :src="record.coverUrl" class="cover-thumb" alt="封面" />
              <div v-else class="cover-placeholder">
                <PictureOutlined />
              </div>
            </div>
          </template>

          <!-- 标题 -->
          <template v-else-if="column.key === 'title'">
            <span class="article-title" :title="record.title">{{ record.title || '无标题' }}</span>
          </template>

          <!-- 作者 -->
          <template v-else-if="column.key === 'author'">
            <div class="author-cell">
              <a-avatar :size="24" :src="record.authorAvatar">
                <template #icon>
                  <UserOutlined />
                </template>
              </a-avatar>
              <span class="author-name">{{ record.authorName || '匿名' }}</span>
            </div>
          </template>

          <!-- 摘要 -->
          <template v-else-if="column.key === 'summary'">
            <a-tooltip v-if="record.summary" :title="record.summary" placement="topLeft"
              :overlay-style="{ maxWidth: '420px' }">
              <span class="article-summary">{{ record.summary }}</span>
            </a-tooltip>
            <span v-else class="text-muted">—</span>
          </template>

          <!-- 分类 -->
          <template v-else-if="column.key === 'categoryName'">
            <a-tag v-if="record.categoryName" color="blue" class="status-tag">{{ record.categoryName }}</a-tag>
            <span v-else class="text-muted">—</span>
          </template>

          <!-- 标签 -->
          <template v-else-if="column.key === 'tags'">
            <template v-if="(record.tags ?? []).filter(Boolean).length">
              <a-tag v-for="tag in (record.tags ?? []).filter((t: API.TagVO | null) => !!t && t.id != null)"
                :key="tag.id" class="status-tag" style="margin-bottom: 2px">
                {{ tag.tagName }}
              </a-tag>
            </template>
            <span v-else class="text-muted">—</span>
          </template>

          <!-- 提交时间 -->
          <template v-else-if="column.key === 'createTime'">
            {{ formatDateTime(record.createTime) }}
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openPreview(record)">
                <template #icon>
                  <EyeOutlined />
                </template>
                预览
              </a-button>
              <a-divider type="vertical" />
              <a-button type="link" size="small" class="pass-btn" @click="confirmPass(record)">
                <template #icon>
                  <CheckCircleOutlined />
                </template>
                通过
              </a-button>
              <a-divider type="vertical" />
              <a-button type="link" size="small" danger @click="openRejectModal(record)">
                <template #icon>
                  <CloseCircleOutlined />
                </template>
                驳回
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>

      <div class="pagination-wrap">
        <a-pagination v-model:current="queryForm.current" v-model:page-size="queryForm.pageSize" :total="total"
          :page-size-options="['10', '20', '50']" show-quick-jumper show-size-changer
          :show-total="(t: number) => `共 ${t} 条`" @change="fetchArticles" @show-size-change="fetchArticles" />
      </div>
    </div>

    <!-- 预览 Modal -->
    <a-modal v-model:open="previewOpen" :title="previewArticle?.title || '文章预览'" width="900px" :footer="null"
      class="preview-modal">
      <div class="preview-meta">
        <a-space size="middle">
          <span><strong>作者：</strong>{{ previewArticle?.authorName || '匿名' }}</span>
          <span><strong>分类：</strong>{{ previewArticle?.categoryName || '—' }}</span>
          <span><strong>提交时间：</strong>{{ formatDateTime(previewArticle?.createTime) }}</span>
        </a-space>
      </div>
      <div v-if="previewArticle?.summary" class="preview-summary">
        <strong>摘要：</strong>{{ previewArticle.summary }}
      </div>
      <MdPreview v-if="previewArticle?.content" :model-value="previewArticle.content" class="preview-md" />
      <div v-else class="text-muted" style="text-align:center;padding:24px">（无正文内容）</div>
    </a-modal>

    <!-- 驳回原因 Modal -->
    <a-modal v-model:open="rejectOpen" title="驳回文章" :confirm-loading="rejectLoading" ok-text="确认驳回" cancel-text="取消"
      ok-type="danger" :width="480" @ok="handleReject" @cancel="closeRejectModal">
      <a-form ref="rejectFormRef" :model="rejectForm" :rules="rejectRules" layout="vertical">
        <a-form-item label="文章">
          <span class="text-muted">{{ rejectingArticle?.title || '—' }}</span>
        </a-form-item>
        <a-form-item label="驳回原因" name="rejectReason">
          <a-textarea v-model:value="rejectForm.rejectReason" :rows="4" placeholder="请填写驳回原因（2-200 字），用户可在「我的文章」看到"
            :maxlength="200" show-count allow-clear />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import type { Rule } from 'ant-design-vue/es/form';
import type { FormInstance } from 'ant-design-vue';
import {
  SearchOutlined,
  PictureOutlined,
  EyeOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  UserOutlined,
} from '@ant-design/icons-vue';
import { MdPreview } from 'md-editor-v3';
import 'md-editor-v3/lib/preview.css';
import { listArticleByPage, reviewArticle } from '@/api/articleController';
import { listCategoryByPage } from '@/api/categoryController';
import { formatDateTime } from '@/utils/datetime';

// ---------- 分类选项 ----------
const categoryOptions = ref<Array<{ id: number; categoryName: string }>>([]);

// ---------- 查询 ----------
const loading = ref(false);
const total = ref(0);
const tableData = ref<API.ArticleVO[]>([]);

const queryForm = reactive<API.ArticleQueryRequest>({
  current: 1,
  pageSize: 10,
  title: undefined,
  // 锁定状态为「待审核」
  status: 1,
  categoryId: undefined,
  sortOrder: 'ascend',
});

const sortCreateOrder = ref<'descend' | 'ascend'>('ascend');

async function fetchArticles() {
  loading.value = true;
  try {
    const params = { ...queryForm, status: 1 };
    const res = await listArticleByPage(params);
    if (res.data?.data) {
      tableData.value = res.data.data.records ?? [];
      total.value = Number(res.data.data.total ?? 0);
    }
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  queryForm.current = 1;
  fetchArticles();
}

function handleReset() {
  queryForm.title = undefined;
  queryForm.categoryId = undefined;
  queryForm.current = 1;
  sortCreateOrder.value = 'ascend';
  queryForm.sortOrder = 'ascend';
  fetchArticles();
}

function handleTableChange(_p: unknown, _f: unknown, sorter: { columnKey?: string; field?: string } | undefined) {
  if (!sorter) return;
  const key = sorter.columnKey ?? sorter.field;
  if (key !== 'createTime') return;
  sortCreateOrder.value = sortCreateOrder.value === 'descend' ? 'ascend' : 'descend';
  queryForm.sortOrder = sortCreateOrder.value;
  queryForm.current = 1;
  fetchArticles();
}

// ---------- 预览 ----------
const previewOpen = ref(false);
const previewArticle = ref<API.ArticleVO | null>(null);

function openPreview(record: API.ArticleVO) {
  previewArticle.value = record;
  previewOpen.value = true;
}

// ---------- 通过 ----------
function confirmPass(record: API.ArticleVO) {
  Modal.confirm({
    title: '确认通过该文章吗？',
    content: `「${record.title || '无标题'}」通过后将立刻在前台展示`,
    okText: '确认通过',
    cancelText: '取消',
    async onOk() {
      try {
        const res = await reviewArticle({ id: record.id, action: 1 });
        if (res.data?.data) {
          message.success('已通过');
          fetchArticles();
        }
      } catch {
        // 错误由 request 拦截器统一提示
      }
    },
  });
}

// ---------- 驳回 ----------
const rejectOpen = ref(false);
const rejectLoading = ref(false);
const rejectingArticle = ref<API.ArticleVO | null>(null);
const rejectFormRef = ref<FormInstance>();
const rejectForm = reactive<{ rejectReason: string }>({ rejectReason: '' });

const rejectRules: Record<string, Rule[]> = {
  rejectReason: [
    { required: true, message: '请填写驳回原因', trigger: 'blur' },
    { min: 2, max: 200, message: '驳回原因 2-200 字', trigger: 'blur' },
  ],
};

function openRejectModal(record: API.ArticleVO) {
  rejectingArticle.value = record;
  rejectForm.rejectReason = '';
  rejectOpen.value = true;
}

function closeRejectModal() {
  rejectOpen.value = false;
  rejectingArticle.value = null;
  rejectForm.rejectReason = '';
}

async function handleReject() {
  try {
    await rejectFormRef.value?.validate();
  } catch {
    return;
  }
  if (!rejectingArticle.value?.id) return;
  rejectLoading.value = true;
  try {
    const res = await reviewArticle({
      id: rejectingArticle.value.id,
      action: 2,
      rejectReason: rejectForm.rejectReason.trim(),
    });
    if (res.data?.data) {
      message.success('已驳回，用户将收到通知');
      closeRejectModal();
      fetchArticles();
    }
  } finally {
    rejectLoading.value = false;
  }
}

// ---------- 表格列 ----------
const columns = computed(() => [
  { title: '封面', key: 'cover', width: 80, align: 'center' as const },
  { title: '标题', key: 'title', dataIndex: 'title', ellipsis: true, width: 200 },
  { title: '作者', key: 'author', width: 130 },
  { title: '摘要', key: 'summary', dataIndex: 'summary', ellipsis: true, width: 200 },
  { title: '分类', key: 'categoryName', width: 110 },
  { title: '标签', key: 'tags', width: 160 },
  {
    title: '提交时间',
    key: 'createTime',
    width: 160,
    sorter: true,
    sortOrder: sortCreateOrder.value,
    sortDirections: ['descend', 'ascend'] as const,
  },
  { title: '操作', key: 'action', width: 220, align: 'center' as const, fixed: 'right' as const },
]);

onMounted(async () => {
  const catRes = await listCategoryByPage({ current: 1, pageSize: 200 });
  categoryOptions.value = (catRes.data?.data?.records ?? []) as Array<{ id: number; categoryName: string }>;
  fetchArticles();
});
</script>

<style scoped>
.article-review {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cover-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-thumb {
  width: 56px;
  height: 36px;
  object-fit: cover;
  border-radius: 6px;
  display: block;
}

.cover-placeholder {
  width: 56px;
  height: 36px;
  border-radius: 6px;
  background: var(--bg-page);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: 16px;
}

.article-title {
  font-weight: 500;
  color: var(--text-primary);
}

.article-summary {
  display: inline-block;
  max-width: 100%;
  color: var(--text-secondary);
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.author-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-name {
  font-size: 13px;
  color: var(--text-primary);
}

.text-muted {
  color: var(--text-muted);
}

.pass-btn {
  color: #52c41a !important;
}

.pass-btn:hover {
  color: #389e0d !important;
}

.preview-meta {
  padding: 8px 0 12px;
  font-size: 13px;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-soft);
  margin-bottom: 12px;
}

.preview-summary {
  background: var(--bg-page);
  padding: 10px 14px;
  border-radius: 8px;
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--text-secondary);
}

.preview-md {
  max-height: 60vh;
  overflow-y: auto;
}

/* 表格行垂直居中（仅本页需要） */
:deep(.ant-table-tbody > tr > td) {
  vertical-align: middle;
}
</style>
