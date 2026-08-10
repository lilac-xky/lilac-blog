<template>
  <TableCRUD title="角色列表" total-unit="个角色" add-button-text="新增角色" :columns="columns" :table-data="tableData"
    :total="total" :loading="loading" :query-form="queryForm" :search-schema="searchSchema" :scroll="{ x: 700 }"
    @search="handleSearch" @reset="handleReset" @add="openAdd" @edit="(record: any) => openEdit(record)"
    @delete="(record: any) => confirmDelete(record)" @table-change="handleTableChange" @page-change="fetchData">
    <template #action="{ record }">
      <a-space>
        <a-button type="link" size="small" @click="openEdit(record)">编辑</a-button>
        <a-button type="link" size="small" @click="openPermissionConfig(record)">配置权限</a-button>
        <a-button type="link" danger size="small" @click="confirmDelete(record)">删除</a-button>
      </a-space>
    </template>
  </TableCRUD>

  <FormModal v-model:open="modalVisible" :title="mode === 'add' ? '新增角色' : '编辑角色'" :model="formData"
    :fields="formFields" :rules="rules" :loading="modalLoading" @ok="handleOk" @cancel="handleCancel" />

  <PermissionConfigModal v-model:open="permissionModalVisible" :role-id="currentRoleId"
    @success="handlePermissionSuccess" />
</template>

<!--
  RoleManagement：后台「角色管理」页
  标准 CRUD：TableCRUD + FormModal + 三个 composable 串起来的最小用例。
-->
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import TableCRUD from '@/components/crud/TableCRUD.vue';
import FormModal from '@/components/crud/FormModal.vue';
import PermissionConfigModal from './PermissionConfigModal.vue';
import type { CrudColumn, SearchField, FormField } from '@/components/crud/types';
import { usePaginationQuery } from '@/composables/usePaginationQuery';
import { useModalForm } from '@/composables/useModalForm';
import { useConfirmDelete } from '@/composables/useConfirmDelete';
import { getRoleList, addRole, updateRole, deleteRole, } from '@/api/roleController';

const { loading, total, tableData, queryForm, sortOrder, fetchData, handleSearch, handleReset, handleTableChange } =
  usePaginationQuery<API.RoleQueryRequest, API.Role>({
    initialQuery: {
      current: 1,
      pageSize: 10,
      roleKey: '',
      name: '',
      sortOrder: 'descend',
    },
    async fetcher(q) {
      const res = await getRoleList(q);
      return {
        records: res.data?.data?.records ?? [],
        total: (res.data?.data?.total as unknown as number) ?? 0,
      };
    },
  });

interface RoleForm {
  id?: string;
  roleKey: string;
  name: string;
  description?: string;
}

const { visible: modalVisible, loading: modalLoading, mode, formData, rules, openAdd, openEdit, handleOk, handleCancel, } = useModalForm<RoleForm, API.Role>({
  defaultForm: () => ({ id: undefined, roleKey: '', name: '' }),
  rules: {
    roleKey: [{ required: true, message: '请输入角色标识', trigger: 'blur' }],
    name: [{ required: true, message: '请输入角色名', trigger: 'blur' }],
    description: [{ required: false, message: '请输入描述', trigger: 'blur' }],
  },
  addApi: (form) => addRole({ roleKey: form.roleKey, name: form.name }),
  updateApi: (form) => updateRole({ id: form.id as any, roleKey: form.roleKey, name: form.name, description: form.description } as any),
  pickEditForm: (record) => ({ id: record.id as any, roleKey: record.roleKey ?? '', name: record.name ?? '' }),
  onSuccess: fetchData,
});

const { confirmDelete } = useConfirmDelete<API.Role>({
  titleOf: () => '确认删除该角色吗？',
  contentOf: (r) => `「${r.name}」删除后不可恢复`,
  deleteApi: (params) => deleteRole(params),
  onSuccess: fetchData,
});

const permissionModalVisible = ref(false);
const currentRoleId = ref<string | undefined>(undefined);

const openPermissionConfig = (record: API.Role) => {
  currentRoleId.value = String(record.id);
  permissionModalVisible.value = true;
};

const handlePermissionSuccess = () => {
  fetchData();
};

const searchSchema: SearchField[] = [
  { name: 'roleKey', label: '角色标识', type: 'input', placeholder: '角色标识关键词', width: 200 },
  { name: 'name', label: '角色名', type: 'input', placeholder: '角色名关键词', width: 200 },
];

const formFields: FormField[] = [
  { name: 'roleKey', label: '角色标识', type: 'input', placeholder: '请输入角色标识' },
  { name: 'name', label: '角色名', type: 'input', placeholder: '请输入角色名' },
  { name: 'description', label: '描述', type: 'input', placeholder: '请输入描述' },
];

const columns = computed<CrudColumn[]>(() => [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 220 },
  { title: '角色名', dataIndex: 'name', key: 'name', width: 150 },
  { title: '角色标识', dataIndex: 'roleKey', key: 'roleKey', width: 150 },
  { title: '描述', dataIndex: 'description', key: 'description', width: 200 },
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
  { title: '操作', key: 'action', width: 200, align: 'center' as const, fixed: 'right' as const },
]);

onMounted(fetchData);
</script>
