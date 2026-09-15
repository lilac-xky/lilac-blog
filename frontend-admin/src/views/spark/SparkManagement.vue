<template>
  <TableCRUD title="灵感列表" total-unit="条灵感" add-button-text="新增灵感" :columns="columns" :table-data="tableData"
    :total="total" :loading="loading" :query-form="queryForm" :search-schema="searchSchema" :scroll="{ x: 1180 }"
    @search="handleSearch" @reset="handleReset" @add="openAdd" @edit="(record: any) => openEdit(record)"
    @delete="(record: any) => confirmDelete(record)" @table-change="handleTableChange" @page-change="fetchData">
    <!-- 内容列：单元格内单行省略，悬停查看完整灵感 -->
    <template #column-content="{ record }">
      <a-tooltip v-if="record.content" :title="record.content" placement="topLeft"
        :overlay-style="{ maxWidth: '420px' }">
        <span class="spark-content">{{ record.content }}</span>
      </a-tooltip>
      <span v-else class="text-muted">—</span>
    </template>
  </TableCRUD>

  <FormModal v-model:open="modalVisible" :title="mode === 'add' ? '新增灵感' : '编辑灵感'" :model="formData"
    :fields="formFields" :rules="rules" :loading="modalLoading" :width="520" @ok="handleOk" @cancel="handleCancel" />
</template>

<!--
  SparkManagement：后台「灵感管理」页
  仿照标签管理实现，复用同一套 CRUD composable；额外提供状态、可见性筛选与标签化展示。
  列表走 spark:list 权限的后台分页接口（公开 + 私有全部可见）；
  新增/编辑走弹窗表单，新增时创建人由后端按当前登录管理员写入。
-->
<script setup lang="ts">
import { computed, onMounted } from 'vue';
import TableCRUD from '@/components/crud/TableCRUD.vue';
import FormModal from '@/components/crud/FormModal.vue';
import type { CrudColumn, SearchField, FormField } from '@/components/crud/types';
import { usePaginationQuery } from '@/composables/usePaginationQuery';
import { useModalForm } from '@/composables/useModalForm';
import { useConfirmDelete } from '@/composables/useConfirmDelete';
import { listSparkByPage, addSpark, updateSpark, deleteSpark } from '@/api/sparkController';

// 灵感状态选项：取值与后端 SparkStatusEnum 一一对应
const STATUS_OPTIONS = [
  { label: '一闪', value: 'spark' },
  { label: '酝酿中', value: 'brewing' },
  { label: '进行中', value: 'doing' },
  { label: '已完成', value: 'done' },
  { label: '搁置', value: 'paused' },
  { label: '废弃', value: 'dropped' },
];

// 状态列标签映射：statusTag 渲染器按值查表，未命中显示「-」
const STATUS_TAG_MAP: Record<string, { label: string; color?: string }> = {
  spark: { label: '一闪', color: 'purple' },
  brewing: { label: '酝酿中', color: 'blue' },
  doing: { label: '进行中', color: 'geekblue' },
  done: { label: '已完成', color: 'green' },
  paused: { label: '搁置', color: 'orange' },
  dropped: { label: '废弃', color: 'default' },
};

// 可见性选项：public 所有人可查，private 仅创建者本人可查
const VISIBILITY_OPTIONS = [
  { label: '公开', value: 'public' },
  { label: '私有', value: 'private' },
];

// 可见性列标签映射：私有灵感用橙色提示，避免管理员误判为公开内容
const VISIBILITY_TAG_MAP: Record<string, { label: string; color?: string }> = {
  public: { label: '公开', color: 'green' },
  private: { label: '私有', color: 'orange' },
};

const { loading, total, tableData, queryForm, sortOrder, fetchData, handleSearch, handleReset, handleTableChange } =
  usePaginationQuery<API.SparkQueryRequest, API.Spark>({
    initialQuery: {
      current: 1,
      pageSize: 10,
      content: undefined,
      status: undefined,
      visibility: undefined,
      sortOrder: 'descend',
    },
    // 分页拉取：走 spark:list 后台接口，把 records/total 适配成 TableCRUD 需要的结构
    async fetcher(q) {
      const res = await listSparkByPage(q);
      return {
        records: res.data?.data?.records ?? [],
        total: (res.data?.data?.total as unknown as number) ?? 0,
      };
    },
  });

interface SparkForm {
  id?: string;
  content: string;
  status: string;
  visibility: string;
}

const {
  visible: modalVisible,
  loading: modalLoading,
  mode,
  formData,
  rules,
  openAdd,
  openEdit,
  handleOk,
  handleCancel,
} = useModalForm<SparkForm, API.Spark>({
  defaultForm: () => ({ id: undefined, content: '', status: 'spark', visibility: 'private' }),
  rules: {
    content: [{ required: true, message: '请输入灵感内容', trigger: 'blur' }],
  },
  // 新增灵感：内容/状态/可见性来自表单，创建人由后端按当前登录管理员写入
  addApi: (form) => addSpark({ content: form.content, status: form.status, visibility: form.visibility }),
  // 编辑灵感：必须带上 id，其余字段与新增一致
  updateApi: (form) =>
    updateSpark({
      id: form.id as any,
      content: form.content,
      status: form.status,
      visibility: form.visibility,
    }),
  // 编辑回填：只取表单用到的字段，避免表格里的 createTime 等无关字段进入表单
  pickEditForm: (record) => ({
    id: record.id as any,
    content: record.content ?? '',
    status: record.status ?? 'spark',
    visibility: record.visibility ?? 'private',
  }),
  // 保存成功后刷新当前页列表
  onSuccess: fetchData,
});

/**
 * 截断过长的灵感内容，用于删除确认等短文案场景
 *
 * @param text 原始内容
 * @param max 最大保留字数，超出部分以省略号替代
 * @returns 截断后的文本
 */
function snippet(text?: string, max = 20) {
  if (!text) return '';
  return text.length > max ? `${text.slice(0, max)}…` : text;
}

const { confirmDelete } = useConfirmDelete<API.Spark>({
  // 删除确认弹窗标题
  titleOf: () => '确认删除该灵感吗？',
  // 删除确认弹窗正文：内容过长时截断，避免弹窗被撑高
  contentOf: (r) => `「${snippet(r.content)}」删除后不可恢复`,
  // 执行删除请求
  deleteApi: (params) => deleteSpark(params),
  // 删除成功后刷新当前页列表
  onSuccess: fetchData,
});

// 搜索条件：内容关键词 + 状态 + 可见性，均对应后端 SparkQueryRequest
const searchSchema: SearchField[] = [
  { name: 'content', label: '内容', type: 'input', placeholder: '内容关键词', width: 200 },
  { name: 'status', label: '状态', type: 'select', width: 120, options: STATUS_OPTIONS },
  { name: 'visibility', label: '可见性', type: 'select', width: 120, options: VISIBILITY_OPTIONS },
];

// 弹窗表单字段：内容为多行文本，状态与可见性为下拉，默认值见 defaultForm
const formFields: FormField[] = [
  { name: 'content', label: '灵感内容', type: 'textarea', rows: 4, placeholder: '请输入灵感内容' },
  { name: 'status', label: '状态', type: 'select', options: STATUS_OPTIONS },
  { name: 'visibility', label: '可见性', type: 'select', options: VISIBILITY_OPTIONS },
];

// 表格列：ID / 内容 / 状态 / 可见性 / 创建人 / 创建时间 / 操作，createTime 支持表头排序
const columns = computed<CrudColumn[]>(() => [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 240 },
  { title: '内容', dataIndex: 'content', key: 'content', ellipsis: true, width: 280 },
  {
    title: '状态',
    key: 'status',
    dataIndex: 'status',
    width: 100,
    align: 'center' as const,
    renderer: { type: 'statusTag', map: STATUS_TAG_MAP },
  },
  {
    title: '可见性',
    key: 'visibility',
    dataIndex: 'visibility',
    width: 100,
    align: 'center' as const,
    renderer: { type: 'statusTag', map: VISIBILITY_TAG_MAP },
  },
  { title: '创建人ID', dataIndex: 'userId', key: 'userId', width: 240 },
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
  { title: '操作', key: 'action', width: 130, align: 'center' as const, fixed: 'right' as const },
]);

// 进入页面即拉取第一页数据
onMounted(fetchData);
</script>

<style scoped>
/* 内容单元格：限制为一行，超出省略，完整内容由 a-tooltip 弹出展示 */
.spark-content {
  display: inline-block;
  max-width: 100%;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: middle;
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
