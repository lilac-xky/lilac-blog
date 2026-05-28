<template>
  <TableCRUD title="标签列表" total-unit="个标签" add-button-text="新增标签" :columns="columns" :table-data="tableData"
    :total="total" :loading="loading" :query-form="queryForm" :search-schema="searchSchema" :scroll="{ x: 700 }"
    @search="handleSearch" @reset="handleReset" @add="openAdd" @edit="(record: any) => openEdit(record)"
    @delete="(record: any) => confirmDelete(record)" @table-change="handleTableChange" @page-change="fetchData" />

  <FormModal v-model:open="modalVisible" :title="mode === 'add' ? '新增标签' : '编辑标签'" :model="formData"
    :fields="formFields" :rules="rules" :loading="modalLoading" @ok="handleOk" @cancel="handleCancel" />
</template>

<!--
  TagManagement：后台「标签管理」页
  与分类管理结构一致，复用同一套 CRUD composable。
-->
<script setup lang="ts">
import { computed, onMounted } from 'vue';
import TableCRUD from '@/components/crud/TableCRUD.vue';
import FormModal from '@/components/crud/FormModal.vue';
import type { CrudColumn, SearchField, FormField } from '@/components/crud/types';
import { usePaginationQuery } from '@/composables/usePaginationQuery';
import { useModalForm } from '@/composables/useModalForm';
import { useConfirmDelete } from '@/composables/useConfirmDelete';
import { listTagByPage, addTag, updateTag, deleteTag } from '@/api/tagController';

const { loading, total, tableData, queryForm, sortOrder, fetchData, handleSearch, handleReset, handleTableChange } =
  usePaginationQuery<API.TagQueryRequest, API.Tag>({
    initialQuery: {
      current: 1,
      pageSize: 10,
      tagName: undefined,
      sortOrder: 'descend',
    },
    async fetcher(q) {
      const res = await listTagByPage(q);
      return {
        records: res.data?.data?.records ?? [],
        total: (res.data?.data?.total as unknown as number) ?? 0,
      };
    },
  });

interface TagForm {
  id?: string;
  tagName: string;
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
} = useModalForm<TagForm, API.Tag>({
  defaultForm: () => ({ id: undefined, tagName: '' }),
  rules: {
    tagName: [{ required: true, message: '请输入标签名', trigger: 'blur' }],
  },
  addApi: (form) => addTag({ tagName: form.tagName }),
  updateApi: (form) => updateTag({ id: form.id as any, tagName: form.tagName }),
  pickEditForm: (record) => ({ id: record.id as any, tagName: record.tagName ?? '' }),
  onSuccess: fetchData,
});

const { confirmDelete } = useConfirmDelete<API.Tag>({
  titleOf: () => '确认删除该标签吗？',
  contentOf: (r) => `「${r.tagName}」删除后不可恢复`,
  deleteApi: (params) => deleteTag(params),
  onSuccess: fetchData,
});

const searchSchema: SearchField[] = [
  { name: 'tagName', label: '标签名', type: 'input', placeholder: '标签名关键词', width: 200 },
];

const formFields: FormField[] = [
  { name: 'tagName', label: '标签名', type: 'input', placeholder: '请输入标签名' },
];

const columns = computed<CrudColumn[]>(() => [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 240 },
  { title: '标签名', dataIndex: 'tagName', key: 'tagName', width: 200 },
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

onMounted(fetchData);
</script>
