<template>
    <TableCRUD title="权限列表" total-unit="个权限" add-button-text="新增权限" :columns="columns" :table-data="tableData"
        :total="total" :loading="loading" :query-form="queryForm" :search-schema="searchSchema" :scroll="{ x: 700 }"
        @search="handleSearch" @reset="handleReset" @add="openAdd" @edit="(record: any) => openEdit(record)"
        @delete="(record: any) => confirmDelete(record)" @table-change="handleTableChange" @page-change="fetchData" />

    <FormModal v-model:open="modalVisible" :title="mode === 'add' ? '新增权限' : '编辑权限'" :model="formData"
        :fields="formFields" :rules="rules" :loading="modalLoading" @ok="handleOk" @cancel="handleCancel" />
</template>

<!--
  PermissionManagement：后台「权限管理」页
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
import { getPermissionList, addPermission, updatePermission, deletePermission, } from '@/api/permissionController';

const { loading, total, tableData, queryForm, sortOrder, fetchData, handleSearch, handleReset, handleTableChange } =
    usePaginationQuery<API.PermissionQueryRequest, API.Permission>({
        initialQuery: {
            current: 1,
            pageSize: 10,
            permissionKey: '',
            name: '',
            sortOrder: 'descend',
        },
        async fetcher(q) {
            const res = await getPermissionList(q);
            return {
                records: res.data?.data?.records ?? [],
                total: (res.data?.data?.total as unknown as number) ?? 0,
            };
        },
    });

interface PermissionForm {
    id?: string;
    permissionKey: string;
    name: string;
}

const { visible: modalVisible, loading: modalLoading, mode, formData, rules, openAdd, openEdit, handleOk, handleCancel, } = useModalForm<PermissionForm, API.Permission>({
    defaultForm: () => ({ id: undefined, permissionKey: '', name: '' }),
    rules: {
        permissionKey: [{ required: true, message: '请输入权限标识', trigger: 'blur' }],
        name: [{ required: true, message: '请输入权限名', trigger: 'blur' }],
    },
    addApi: (form) => addPermission({ permissionKey: form.permissionKey, name: form.name }),
    updateApi: (form) => updatePermission({ id: form.id as any, permissionKey: form.permissionKey, name: form.name } as any),
    pickEditForm: (record) => ({ id: record.id as any, permissionKey: record.permissionKey ?? '', name: record.name ?? '' }),
    onSuccess: fetchData,
});

const { confirmDelete } = useConfirmDelete<API.Permission>({
    titleOf: () => '确认删除该权限吗？',
    contentOf: (r) => `「${r.name}」删除后不可恢复`,
    deleteApi: (params) => deletePermission(params),
    onSuccess: fetchData,
});

const searchSchema: SearchField[] = [
    { name: 'permissionKey', label: '权限标识', type: 'input', placeholder: '权限标识关键词', width: 200 },
    { name: 'name', label: '权限名', type: 'input', placeholder: '权限名关键词', width: 200 },
];

const formFields: FormField[] = [
    { name: 'permissionKey', label: '权限标识', type: 'input', placeholder: '请输入权限标识' },
    { name: 'name', label: '权限名', type: 'input', placeholder: '请输入权限名' }
];

const columns = computed<CrudColumn[]>(() => [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 220 },
    { title: '权限名', dataIndex: 'name', key: 'name', width: 150 },
    { title: '权限标识', dataIndex: 'permissionKey', key: 'permissionKey', width: 150 },
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
