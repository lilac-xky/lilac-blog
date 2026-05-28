<template>
  <TableCRUD title="分类列表" total-unit="个分类" add-button-text="新增分类" :columns="columns" :table-data="tableData"
    :total="total" :loading="loading" :query-form="queryForm" :search-schema="searchSchema" :scroll="{ x: 700 }"
    @search="handleSearch" @reset="handleReset" @add="openAdd" @edit="(record: any) => openEdit(record)"
    @delete="(record: any) => confirmDelete(record)" @table-change="handleTableChange" @page-change="fetchData" />

  <FormModal v-model:open="modalVisible" :title="mode === 'add' ? '新增分类' : '编辑分类'" :model="formData"
    :fields="formFields" :rules="rules" :loading="modalLoading" @ok="handleOk" @cancel="handleCancel" />
</template>

<!--
  CategoryManagement：后台「分类管理」页
  标准 CRUD：TableCRUD + FormModal + 三个 composable 串起来的最小用例。
-->
<script setup lang="ts">
import { computed, onMounted } from 'vue';
import TableCRUD from '@/components/crud/TableCRUD.vue';
import FormModal from '@/components/crud/FormModal.vue';
import type { CrudColumn, SearchField, FormField } from '@/components/crud/types';
import { usePaginationQuery } from '@/composables/usePaginationQuery';
import { useModalForm } from '@/composables/useModalForm';
import { useConfirmDelete } from '@/composables/useConfirmDelete';
import {
  listCategoryByPage,
  addCategory,
  updateCategory,
  deleteCategory,
} from '@/api/categoryController';

const { loading, total, tableData, queryForm, sortOrder, fetchData, handleSearch, handleReset, handleTableChange } =
  usePaginationQuery<API.CategoryQueryRequest, API.Category>({
    initialQuery: {
      current: 1,
      pageSize: 10,
      categoryName: undefined,
      sortOrder: 'descend',
    },
    async fetcher(q) {
      const res = await listCategoryByPage(q);
      return {
        records: res.data?.data?.records ?? [],
        total: (res.data?.data?.total as unknown as number) ?? 0,
      };
    },
  });

interface CategoryForm {
  id?: string;
  categoryName: string;
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
} = useModalForm<CategoryForm, API.Category>({
  defaultForm: () => ({ id: undefined, categoryName: '' }),
  rules: {
    categoryName: [{ required: true, message: '请输入分类名', trigger: 'blur' }],
  },
  addApi: (form) => addCategory({ categoryName: form.categoryName }),
  updateApi: (form) => updateCategory({ id: form.id as any, categoryName: form.categoryName }),
  pickEditForm: (record) => ({ id: record.id as any, categoryName: record.categoryName ?? '' }),
  onSuccess: fetchData,
});

const { confirmDelete } = useConfirmDelete<API.Category>({
  titleOf: () => '确认删除该分类吗？',
  contentOf: (r) => `「${r.categoryName}」删除后不可恢复`,
  deleteApi: (params) => deleteCategory(params),
  onSuccess: fetchData,
});

const searchSchema: SearchField[] = [
  { name: 'categoryName', label: '分类名', type: 'input', placeholder: '分类名关键词', width: 200 },
];

const formFields: FormField[] = [
  { name: 'categoryName', label: '分类名', type: 'input', placeholder: '请输入分类名' },
];

const columns = computed<CrudColumn[]>(() => [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 240 },
  { title: '分类名', dataIndex: 'categoryName', key: 'categoryName', width: 200 },
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
