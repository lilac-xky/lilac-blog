<template>
  <TableCRUD title="碎知列表" total-unit="条碎片" :show-add="false" :show-edit-action="false" :columns="columns"
    :table-data="tableData" :total="total" :loading="loading" :query-form="queryForm" :search-schema="searchSchema"
    :scroll="{ x: 1240 }" @search="handleSearch" @reset="handleReset" @delete="(record: any) => confirmDelete(record)"
    @table-change="handleTableChange" @page-change="fetchData">
    <!-- 内容列：单元格内单行省略，悬停查看完整碎片 -->
    <template #column-content="{ record }">
      <a-tooltip v-if="record.content" :title="record.content" placement="topLeft"
        :overlay-style="{ maxWidth: '460px' }">
        <span class="note-content">{{ record.content }}</span>
      </a-tooltip>
      <span v-else class="text-muted">—</span>
    </template>

    <!-- 主题列：展示前两个标签，多出的收进 +N -->
    <template #column-tags="{ record }">
      <template v-if="record.tags && record.tags.length">
        <a-tag v-for="tag in record.tags.slice(0, 2)" :key="tag.id" class="note-tag">
          {{ tag.tagName }}
        </a-tag>
        <a-tag v-if="record.tags.length > 2" class="note-tag">+{{ record.tags.length - 2 }}</a-tag>
      </template>
      <span v-else class="text-muted">杂项</span>
    </template>

    <!-- 一句话理解列：省略展示，悬停查看全文 -->
    <template #column-insight="{ record }">
      <a-tooltip v-if="record.insight" :title="record.insight" placement="topLeft"
        :overlay-style="{ maxWidth: '420px' }">
        <span class="note-insight">{{ record.insight }}</span>
      </a-tooltip>
      <span v-else class="text-muted">—</span>
    </template>
  </TableCRUD>
</template>

<!--
  NoteManagement：后台「碎知管理」页
  碎片全部私有，后台只做只读巡查与违规清理，因此不提供新增与编辑入口。
  列表走 note:list:admin 权限的跨用户分页接口，可按内容、主题、状态、归属用户筛选；
  删除只对明显违规内容使用，普通用户的碎片应由本人通过前台管理。
-->
<script setup lang="ts">
import { computed, onMounted } from 'vue';
import TableCRUD from '@/components/crud/TableCRUD.vue';
import type { CrudColumn, SearchField } from '@/components/crud/types';
import { usePaginationQuery } from '@/composables/usePaginationQuery';
import { useConfirmDelete } from '@/composables/useConfirmDelete';
import { deleteNote, listNoteAdminByPage } from '@/api/noteController';

// 碎片状态选项：取值与后端 NoteConstant 一致
const STATUS_OPTIONS = [
  { label: '未整理', value: 0 },
  { label: '已归档', value: 1 },
];

// 状态列标签映射：未整理为蓝色，已归档为绿色
const STATUS_TAG_MAP: Record<string | number, { label: string; color?: string }> = {
  0: { label: '未整理', color: 'blue' },
  1: { label: '已归档', color: 'green' },
};

const { loading, total, tableData, queryForm, sortOrder, fetchData, handleSearch, handleReset, handleTableChange } =
  usePaginationQuery<API.NoteQueryRequest, API.NoteVO>({
    initialQuery: {
      current: 1,
      pageSize: 10,
      content: undefined,
      status: undefined,
      userId: undefined,
      sortOrder: 'descend',
    },
    // 分页拉取：走 note:list:admin 后台接口，把 records/total 适配成 TableCRUD 需要的结构
    async fetcher(q) {
      const res = await listNoteAdminByPage(q);
      return {
        records: res.data?.data?.records ?? [],
        total: (res.data?.data?.total as unknown as number) ?? 0,
      };
    },
  });

/**
 * 截断过长的碎片内容，用于删除确认等短文案场景
 *
 * @param text 原始内容
 * @param max 最大保留字数，超出部分以省略号替代
 * @returns 截断后的文本
 */
function snippet(text?: string, max = 20) {
  if (!text) return '';
  return text.length > max ? `${text.slice(0, max)}…` : text;
}

const { confirmDelete } = useConfirmDelete<API.NoteVO>({
  // 删除确认弹窗标题
  titleOf: () => '确认删除该碎片吗？',
  // 删除确认弹窗正文：内容过长时截断，避免弹窗被撑高
  contentOf: (r) => `「${snippet(r.content)}」删除后不可恢复`,
  // 执行删除请求
  deleteApi: (params) => deleteNote(params),
  // 删除成功后刷新当前页列表
  onSuccess: fetchData,
});

// 搜索条件：内容关键词 + 状态 + 归属用户，均对应后端 NoteQueryRequest
const searchSchema: SearchField[] = [
  { name: 'content', label: '内容', type: 'input', placeholder: '内容关键词', width: 200 },
  { name: 'status', label: '状态', type: 'select', width: 120, options: STATUS_OPTIONS },
  { name: 'userId', label: '归属用户ID', type: 'input', placeholder: '用户ID', width: 200 },
];

// 表格列：ID / 内容 / 理解 / 主题 / 状态 / 归属用户 / 归档文章 / 创建时间 / 操作
const columns = computed<CrudColumn[]>(() => [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 240 },
  { title: '内容', dataIndex: 'content', key: 'content', ellipsis: true, width: 300 },
  { title: '理解', dataIndex: 'insight', key: 'insight', ellipsis: true, width: 200 },
  { title: '主题', key: 'tags', width: 180 },
  {
    title: '状态',
    key: 'status',
    dataIndex: 'status',
    width: 100,
    align: 'center' as const,
    renderer: { type: 'statusTag', map: STATUS_TAG_MAP },
  },
  { title: '归属用户ID', dataIndex: 'userId', key: 'userId', width: 240 },
  { title: '归档文章ID', dataIndex: 'articleId', key: 'articleId', width: 240 },
  {
    title: '创建时间',
    key: 'createTime',
    dataIndex: 'createTime',
    width: 180,
    sorter: true,
    sortOrder: sortOrder.value,
    sortDirections: ['descend', 'ascend'] as const,
    renderer: { type: 'datetime' },
  },
  { title: '操作', key: 'action', width: 80, align: 'center' as const, fixed: 'right' as const },
]);

// 进入页面即拉取第一页数据
onMounted(fetchData);
</script>

<style scoped>
/* 内容单元格：限制为一行，超出省略，完整内容由 a-tooltip 弹出展示 */
.note-content {
  display: inline-block;
  max-width: 100%;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: middle;
}

/* 理解单元格：比正文弱一档，避免两列抢注意力 */
.note-insight {
  display: inline-block;
  max-width: 100%;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: middle;
}

/* 主题标签：收窄内边距，多个标签也能在一行内排开 */
.note-tag {
  margin-inline-end: 4px;
  font-size: 12px;
}

/* 空内容占位：内容为空时展示的浅灰色破折号 */
.text-muted {
  color: var(--text-muted);
}

/* 本页表格单元格垂直居中，避免多行内容与状态标签上下错位 */
:deep(.ant-table-tbody > tr > td) {
  vertical-align: middle;
}
</style>
