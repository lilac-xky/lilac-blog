<template>
  <TableCRUD :columns="columns" :table-data="tableData" :total="total" :loading="loading" :query-form="queryForm"
    :search-schema="searchSchema" :scroll="{ x: 1380 }" title="文章列表" total-unit="篇文章" add-button-text="写文章"
    @search="handleSearch" @reset="onReset" @add="goWrite" @table-change="handleTableChange" @page-change="fetchData">
    <!-- 标签搜索字段：UI 单选 -->
    <template #search-tagId="{ model }">
      <a-select v-model:value="model.tagId" placeholder="全部" allow-clear style="width: 120px"
        :options="tagOptions.map(t => ({ value: t.id, label: t.tagName }))" />
    </template>

    <!-- 封面 -->
    <template #column-cover="{ record }">
      <div class="cover-wrap">
        <img v-if="record.coverUrl" :src="record.coverUrl" class="cover-thumb" alt="封面" />
        <div v-else class="cover-placeholder">
          <PictureOutlined />
        </div>
      </div>
    </template>

    <!-- 标题 -->
    <template #column-title="{ record }">
      <span class="article-title" :title="record.title">{{ record.title || '无标题' }}</span>
    </template>

    <!-- 摘要 -->
    <template #column-summary="{ record }">
      <a-tooltip v-if="record.summary" :title="record.summary" placement="topLeft"
        :overlay-style="{ maxWidth: '420px' }">
        <span class="article-summary">{{ record.summary }}</span>
      </a-tooltip>
      <span v-else class="text-muted">—</span>
    </template>

    <!-- 分类 -->
    <template #column-categoryName="{ record }">
      <a-tag v-if="record.categoryName" color="blue" class="status-tag">{{ record.categoryName }}</a-tag>
      <span v-else class="text-muted">—</span>
    </template>

    <!-- 标签 -->
    <template #column-tags="{ record }">
      <template v-if="(record.tags ?? []).filter(Boolean).length">
        <a-tag v-for="tag in (record.tags ?? []).filter((t: API.TagVO | null) => !!t && t.id != null)" :key="tag.id"
          class="status-tag" style="margin-bottom: 2px">
          {{ tag.tagName }}
        </a-tag>
      </template>
      <span v-else class="text-muted">—</span>
    </template>

    <!-- 置顶 -->
    <template #column-isTop="{ record }">
      <a-tag v-if="record.isTop === 1" color="gold" class="status-tag">置顶</a-tag>
      <span v-else class="text-muted">—</span>
    </template>

    <!-- 浏览量 -->
    <template #column-viewCount="{ record }">
      <span class="view-count">{{ record.viewCount ?? 0 }}</span>
    </template>

    <!-- 操作列（编辑跳转，不开弹窗） -->
    <template #action="{ record }">
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
  </TableCRUD>
</template>

<!--
  ArticleManagement：后台「文章管理」页
  列出所有文章（含草稿/待审核/已发布），支持按标题/状态/分类/标签/置顶筛选。
  编辑走「写文章」整页路由，不走弹窗；删除走二次确认。
-->
<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { EditOutlined, DeleteOutlined, PictureOutlined } from '@ant-design/icons-vue';
import TableCRUD from '@/components/crud/TableCRUD.vue';
import type { CrudColumn, SearchField } from '@/components/crud/types';
import { usePaginationQuery } from '@/composables/usePaginationQuery';
import { useConfirmDelete } from '@/composables/useConfirmDelete';
import { listArticleByPage, deleteArticle } from '@/api/articleController';
import { listCategoryByPage } from '@/api/categoryController';
import { listTagByPage } from '@/api/tagController';

const router = useRouter();

const categoryOptions = ref<Array<{ id: number; categoryName: string }>>([]);
const tagOptions = ref<Array<{ id: number; tagName: string }>>([]);

// UI 上只允许选一个标签，但后端按 tagIds 数组接收
// transformQuery 里会把 tagId 转成 tagIds: [tagId]，所以这里扩出 UI 专用字段
type ArticleQueryUI = API.ArticleQueryRequest & { tagId?: number };

const initialQuery: ArticleQueryUI = reactive({
  current: 1,
  pageSize: 10,
  title: undefined,
  status: undefined,
  isTop: undefined,
  categoryId: undefined,
  tagId: undefined,
  sortOrder: 'descend',
});

const { loading, total, tableData, queryForm, sortOrder, fetchData, handleSearch, handleReset, handleTableChange } =
  usePaginationQuery<ArticleQueryUI, API.ArticleVO>({
    initialQuery: { ...initialQuery },
    transformQuery: (q) => ({
      ...q,
      tagIds: q.tagId !== undefined ? [q.tagId] : undefined,
      tagId: undefined,
    }),
    async fetcher(q) {
      const res = await listArticleByPage(q);
      return {
        records: res.data?.data?.records ?? [],
        total: (res.data?.data?.total as unknown as number) ?? 0,
      };
    },
  });

function onReset() {
  handleReset();
}

// 删除
const { confirmDelete } = useConfirmDelete<API.ArticleVO>({
  titleOf: () => '确认删除该文章吗？',
  contentOf: (r) => `「${r.title || '无标题'}」删除后不可恢复`,
  deleteApi: (params) => deleteArticle(params),
  onSuccess: fetchData,
});

// 跳转
function goWrite() {
  router.push('/write-blog');
}

function goEdit(record: API.ArticleVO) {
  router.push({ path: '/write-blog', query: { id: record.id } });
}

// 搜索 schema（tagId 字段走自定义插槽）
const searchSchema = computed<SearchField[]>(() => [
  { name: 'title', label: '标题', type: 'input', placeholder: '标题关键词', width: 180 },
  {
    name: 'status',
    label: '状态',
    type: 'select',
    width: 120,
    options: [
      { label: '草稿', value: 0 },
      { label: '待审核', value: 1 },
      { label: '已发布', value: 2 },
    ],
  },
  {
    name: 'categoryId',
    label: '分类',
    type: 'select',
    width: 120,
    options: categoryOptions.value.map((c) => ({ label: c.categoryName, value: c.id })),
  },
  { name: 'tagId', label: '标签', type: 'select', width: 120, options: [] },
  {
    name: 'isTop',
    label: '置顶',
    type: 'select',
    width: 100,
    options: [
      { label: '是', value: 1 },
      { label: '否', value: 0 },
    ],
  },
]);

// 列
const columns = computed<CrudColumn[]>(() => [
  { title: '封面', key: 'cover', width: 80, align: 'center' as const },
  { title: '标题', key: 'title', dataIndex: 'title', ellipsis: true, width: 180 },
  { title: '摘要', key: 'summary', dataIndex: 'summary', ellipsis: true, width: 200 },
  { title: '分类', key: 'categoryName', width: 100 },
  { title: '标签', key: 'tags', width: 160 },
  {
    title: '状态',
    key: 'status',
    dataIndex: 'status',
    width: 90,
    align: 'center' as const,
    renderer: {
      type: 'statusTag',
      map: {
        0: { label: '草稿', color: 'default' },
        1: { label: '待审核', color: 'orange' },
        2: { label: '已发布', color: 'green' },
      },
    },
  },
  { title: '置顶', key: 'isTop', width: 70, align: 'center' as const },
  { title: '浏览量', key: 'viewCount', width: 80, align: 'center' as const },
  {
    title: '创建时间',
    key: 'createTime',
    dataIndex: 'createTime',
    width: 160,
    sorter: true,
    sortOrder: sortOrder.value,
    sortDirections: ['descend', 'ascend'] as const,
    renderer: { type: 'datetime' },
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
  fetchData();
});
</script>

<style scoped>
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

.text-muted {
  color: var(--text-muted);
}

.view-count {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 表格行垂直居中（仅本页需要） */
:deep(.ant-table-tbody > tr > td) {
  vertical-align: middle;
}
</style>
