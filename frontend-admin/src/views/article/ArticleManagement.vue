<template>
  <div class="article-manage">
    <!-- 搜索面板 -->
    <div class="panel search-panel">
      <div class="panel-header tight">
        <div class="panel-title small">条件筛选</div>
        <a-button type="primary" @click="goWrite">
          <template #icon>
            <PlusOutlined />
          </template>
          写文章
        </a-button>
      </div>
      <a-form layout="inline" :model="queryForm" class="search-form">
        <a-form-item label="标题">
          <a-input v-model:value="queryForm.title" placeholder="标题关键词" allow-clear style="width: 180px" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="queryForm.status" placeholder="全部" allow-clear style="width: 120px">
            <a-select-option :value="0">草稿</a-select-option>
            <a-select-option :value="1">待审核</a-select-option>
            <a-select-option :value="2">已发布</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="分类">
          <a-select v-model:value="queryForm.categoryId" placeholder="全部" allow-clear style="width: 120px"
            :options="categoryOptions.map(c => ({ value: c.id, label: c.categoryName }))" />
        </a-form-item>
        <a-form-item label="标签">
          <a-select v-model:value="filterTagId" placeholder="全部" allow-clear style="width: 120px"
            :options="tagOptions.map(t => ({ value: t.id, label: t.tagName }))" />
        </a-form-item>
        <a-form-item label="置顶">
          <a-select v-model:value="queryForm.isTop" placeholder="全部" allow-clear style="width: 100px">
            <a-select-option :value="1">是</a-select-option>
            <a-select-option :value="0">否</a-select-option>
          </a-select>
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

    <!-- 表格面板 -->
    <div class="panel table-panel">
      <div class="panel-header tight">
        <div class="panel-title small">
          文章列表
          <span class="total-badge">共 {{ total }} 篇文章</span>
        </div>
      </div>

      <a-table :columns="columns" :data-source="tableData" :loading="loading" :pagination="false" row-key="id"
        size="middle" class="article-table" :scroll="{ x: 1380 }" :show-sorter-tooltip="false"
        @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <!-- 封面列 -->
          <template v-if="column.key === 'cover'">
            <div class="cover-wrap">
              <img v-if="record.coverUrl" :src="record.coverUrl" class="cover-thumb" alt="封面" />
              <div v-else class="cover-placeholder">
                <PictureOutlined />
              </div>
            </div>
          </template>

          <!-- 标题列 -->
          <template v-else-if="column.key === 'title'">
            <span class="article-title" :title="record.title">{{ record.title || '无标题' }}</span>
          </template>

          <!-- 摘要列 -->
          <template v-else-if="column.key === 'summary'">
            <a-tooltip v-if="record.summary" :title="record.summary" placement="topLeft"
              :overlay-style="{ maxWidth: '420px' }">
              <span class="article-summary">{{ record.summary }}</span>
            </a-tooltip>
            <span v-else class="text-muted">—</span>
          </template>

          <!-- 分类列 -->
          <template v-else-if="column.key === 'categoryName'">
            <a-tag v-if="record.categoryName" color="blue" class="status-tag">{{ record.categoryName }}</a-tag>
            <span v-else class="text-muted">—</span>
          </template>

          <!-- 标签列 -->
          <template v-else-if="column.key === 'tags'">
            <template v-if="(record.tags ?? []).filter(Boolean).length">
              <a-tag v-for="tag in (record.tags ?? []).filter((t: API.TagVO | null) => !!t && t.id != null)"
                :key="tag.id" class="status-tag" style="margin-bottom: 2px">
                {{ tag.tagName }}
              </a-tag>
            </template>
            <span v-else class="text-muted">—</span>
          </template>

          <!-- 状态列 -->
          <template v-else-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)" class="status-tag">
              {{ statusLabel(record.status) }}
            </a-tag>
          </template>

          <!-- 置顶列 -->
          <template v-else-if="column.key === 'isTop'">
            <a-tag v-if="record.isTop === 1" color="gold" class="status-tag">置顶</a-tag>
            <span v-else class="text-muted">—</span>
          </template>

          <!-- 浏览量列 -->
          <template v-else-if="column.key === 'viewCount'">
            <span class="view-count">{{ record.viewCount ?? 0 }}</span>
          </template>

          <!-- 创建时间列 -->
          <template v-else-if="column.key === 'createTime'">
            {{ record.createTime ? record.createTime.replace('T', ' ').split('.')[0] : '-' }}
          </template>

          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" class="action-edit" @click="goEdit(record)">
                <template #icon>
                  <EditOutlined />
                </template>
                编辑
              </a-button>
              <a-divider type="vertical" />
              <a-button type="link" size="small" danger @click="confirmDelete(record)">
                <template #icon>
                  <DeleteOutlined />
                </template>
                删除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <a-pagination v-model:current="queryForm.current" v-model:page-size="queryForm.pageSize" :total="total"
          :page-size-options="['10', '20', '50']" show-quick-jumper show-size-changer
          :show-total="(t: number) => `共 ${t} 条`" @change="fetchArticles" @show-size-change="fetchArticles" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message, Modal } from 'ant-design-vue';
import {
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  PlusOutlined,
  PictureOutlined,
} from '@ant-design/icons-vue';
import { listArticleByPage, deleteArticle } from '@/api/articleController';
import { listCategoryByPage } from '@/api/categoryController';
import { listTagByPage } from '@/api/tagController';

const router = useRouter();

// ---------- 分类/标签选项 ----------
const categoryOptions = ref<Array<{ id: number; categoryName: string }>>([]);
const tagOptions = ref<Array<{ id: number; tagName: string }>>([]);

// ---------- 查询 ----------
const loading = ref(false);
const total = ref(0);
const tableData = ref<API.ArticleVO[]>([]);

const queryForm = reactive<API.ArticleQueryRequest>({
  current: 1,
  pageSize: 10,
  title: undefined,
  status: undefined,
  isTop: undefined,
  categoryId: undefined,
  tagIds: undefined,
  sortOrder: 'descend',
});

// 创建时间排序方向（默认降序）
const sortCreateOrder = ref<'descend' | 'ascend'>('descend');

// tagIds 在 UI 上用单个 tagId 来驱动，转换时包装成数组
const filterTagId = ref<number | undefined>(undefined);

async function fetchArticles() {
  loading.value = true;
  try {
    const params = {
      ...queryForm,
      tagIds: filterTagId.value !== undefined ? [filterTagId.value] : undefined,
    };
    const res = await listArticleByPage(params);
    if (res.data?.data) {
      tableData.value = res.data.data.records ?? [];
      total.value = Number(res.data.data.total ?? 0);
    }
  } catch {
    // 错误由 request 拦截器统一提示
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
  queryForm.status = undefined;
  queryForm.isTop = undefined;
  queryForm.categoryId = undefined;
  filterTagId.value = undefined;
  queryForm.current = 1;
  sortCreateOrder.value = 'descend';
  queryForm.sortOrder = 'descend';
  fetchArticles();
}

// 表格变化（仅用于切换创建时间排序方向）
function handleTableChange(_pagination: unknown, _filters: unknown, sorter: { columnKey?: string; field?: string } | undefined) {
  if (!sorter) return;
  const key = sorter.columnKey ?? sorter.field;
  if (key !== 'createTime') return;
  sortCreateOrder.value = sortCreateOrder.value === 'descend' ? 'ascend' : 'descend';
  queryForm.sortOrder = sortCreateOrder.value;
  queryForm.current = 1;
  fetchArticles();
}

// ---------- 状态工具 ----------
function statusLabel(status?: number) {
  if (status === 0) return '草稿';
  if (status === 1) return '待审核';
  if (status === 2) return '已发布';
  return '未知';
}

function statusColor(status?: number) {
  if (status === 0) return 'default';
  if (status === 1) return 'orange';
  if (status === 2) return 'green';
  return 'default';
}

// ---------- 跳转 ----------
function goWrite() {
  router.push('/write-blog');
}

function goEdit(record: API.ArticleVO) {
  router.push({ path: '/write-blog', query: { id: record.id } });
}

// ---------- 删除 ----------
function confirmDelete(record: API.ArticleVO) {
  const modal = Modal.confirm({
    title: '确认删除该文章吗？',
    content: `「${record.title || '无标题'}」删除后不可恢复`,
    okText: '确认',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        const res = await deleteArticle({ id: record.id });
        if (res.data?.data) {
          message.success('删除成功');
          fetchArticles();
        }
      } catch {
        modal.destroy();
      }
    },
  });
}

// ---------- 表格列定义 ----------
const columns = computed(() => [
  { title: '封面', key: 'cover', width: 80, align: 'center' as const },
  { title: '标题', key: 'title', dataIndex: 'title', ellipsis: true, width: 180 },
  { title: '摘要', key: 'summary', dataIndex: 'summary', ellipsis: true, width: 200 },
  { title: '分类', key: 'categoryName', width: 100 },
  { title: '标签', key: 'tags', width: 160 },
  { title: '状态', key: 'status', width: 90, align: 'center' as const },
  { title: '置顶', key: 'isTop', width: 70, align: 'center' as const },
  { title: '浏览量', key: 'viewCount', width: 80, align: 'center' as const },
  {
    title: '创建时间',
    key: 'createTime',
    width: 160,
    sorter: true,
    sortOrder: sortCreateOrder.value,
    sortDirections: ['descend', 'ascend'] as const,
  },
  { title: '操作', key: 'action', width: 130, align: 'center' as const, fixed: 'right' as const },
]);

onMounted(async () => {
  const [catRes, tagRes] = await Promise.all([
    listCategoryByPage({ current: 1, pageSize: 200 }),
    listTagByPage({ current: 1, pageSize: 200 }),
  ]);
  categoryOptions.value = (catRes.data?.data?.records ?? []) as Array<{ id: number; categoryName: string }>;
  tagOptions.value = (tagRes.data?.data?.records ?? []) as Array<{ id: number; tagName: string }>;
  fetchArticles();
});
</script>

<style scoped>
.article-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel {
  background: var(--bg-card);
  border-radius: var(--radius-card);
  padding: 16px 20px;
  box-shadow: var(--shadow-card);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.panel-header.tight {
  margin-bottom: 12px;
}

.panel-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 10px;
}

.panel-title.small {
  font-size: 14px;
}

.total-badge {
  font-size: 12px;
  color: var(--text-secondary);
  background: var(--bg-page);
  padding: 2px 10px;
  border-radius: var(--radius-pill);
  font-weight: 400;
}

.search-panel {
  flex-shrink: 0;
}

.search-form {
  flex-wrap: wrap;
  gap: 8px 0;
}

:deep(.ant-form-inline .ant-form-item) {
  margin-right: 12px;
  margin-bottom: 0;
}

:deep(.ant-form-inline .ant-form-item-label) {
  padding-right: 4px;
}

:deep(.ant-form-inline .ant-form-item-label > label) {
  font-size: 13px;
}

.table-panel {
  flex-shrink: 0;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid var(--border-soft);
  margin-top: 16px;
}

/* 封面缩略图 */
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
  vertical-align: middle;
}

.status-tag {
  border-radius: var(--radius-pill) !important;
  border: none !important;
  font-size: 12px;
}

.text-muted {
  color: var(--text-muted);
}

.view-count {
  font-size: 13px;
  color: var(--text-secondary);
}

.action-edit {
  color: var(--primary) !important;
}

.action-edit:hover {
  color: var(--primary-hover) !important;
}

:deep(.ant-table-thead > tr > th) {
  background: var(--bg-page) !important;
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 13px;
  border-bottom: 1px solid var(--border-soft) !important;
}

:deep(.ant-table-tbody > tr > td) {
  border-bottom: 1px solid var(--border-soft) !important;
  font-size: 13px;
  vertical-align: middle;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: var(--bg-page) !important;
}

:deep(.ant-table) {
  background: transparent !important;
}
</style>
