<template>
  <div class="user-manage">
    <!-- 搜索面板 -->
    <div class="panel search-panel">
      <div class="panel-header tight">
        <div class="panel-title small">条件筛选</div>
      </div>
      <a-form layout="inline" :model="queryForm" class="search-form">
        <a-form-item label="账号">
          <a-input v-model:value="queryForm.userAccount" placeholder="账号" allow-clear style="width: 140px" />
        </a-form-item>
        <a-form-item label="昵称">
          <a-input v-model:value="queryForm.username" placeholder="昵称" allow-clear style="width: 140px" />
        </a-form-item>
        <a-form-item label="邮箱">
          <a-input v-model:value="queryForm.email" placeholder="邮箱" allow-clear style="width: 180px" />
        </a-form-item>
        <a-form-item label="角色">
          <a-select v-model:value="queryForm.role" placeholder="全部" allow-clear style="width: 120px">
            <a-select-option value="admin">管理员</a-select-option>
            <a-select-option value="user">普通用户</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="queryForm.status" placeholder="全部" allow-clear style="width: 100px">
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="0">异常</a-select-option>
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
          用户列表
          <span class="total-badge">共 {{ total }} 名用户</span>
        </div>
      </div>

      <a-table :columns="columns" :data-source="tableData" :loading="loading" :pagination="false" row-key="id"
        size="middle" class="user-table">
        <!-- 头像列 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'avatar'">
            <a-avatar :src="record.avatar" :size="36" class="user-avatar">
              <template #icon>
                <UserOutlined />
              </template>
            </a-avatar>
          </template>

          <!-- 角色列 -->
          <template v-else-if="column.key === 'role'">
            <a-tag :color="record.role === 'admin' ? 'gold' : 'blue'" class="role-tag">
              {{ record.role === 'admin' ? '管理员' : '普通用户' }}
            </a-tag>
          </template>

          <!-- 状态列 -->
          <template v-else-if="column.key === 'status'">
            <a-badge :status="record.status === 1 ? 'success' : 'error'" :text="record.status === 1 ? '正常' : '异常'" />
          </template>

          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" class="action-edit" @click="openEditModal(record)">
                <template #icon>
                  <EditOutlined />
                </template>
                编辑
              </a-button>
              <a-divider type="vertical" />
              <a-popconfirm title="确认删除该用户吗？" ok-text="确认" cancel-text="取消" ok-type="danger"
                @confirm="handleDelete(record)">
                <a-button type="link" size="small" danger>
                  <template #icon>
                    <DeleteOutlined />
                  </template>
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <a-pagination v-model:current="queryForm.current" v-model:page-size="queryForm.pageSize" :total="total"
          :page-size-options="['10', '20', '50']" show-quick-jumper show-size-changer
          :show-total="(total) => `共 ${total} 条`" @change="fetchUsers" @show-size-change="fetchUsers" />
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <a-modal v-model:open="editVisible" title="编辑用户" :confirm-loading="editLoading" ok-text="保存" cancel-text="取消"
      :width="520" @ok="handleEditOk" @cancel="handleEditCancel">
      <a-form ref="editFormRef" :model="editForm" :rules="editRules" layout="vertical" class="edit-form">
        <div class="edit-avatar-row">
          <a-avatar :src="editForm.avatar" :size="56" class="edit-preview-avatar">
            <template #icon>
              <UserOutlined />
            </template>
          </a-avatar>
          <a-form-item label="头像链接" name="avatar" style="flex: 1; margin-bottom: 0">
            <a-input v-model:value="editForm.avatar" placeholder="请输入头像 URL" allow-clear />
          </a-form-item>
        </div>

        <div class="form-row-2">
          <a-form-item label="账号" name="userAccount">
            <a-input v-model:value="editForm.userAccount" placeholder="账号" allow-clear />
          </a-form-item>
          <a-form-item label="昵称" name="username">
            <a-input v-model:value="editForm.username" placeholder="昵称" allow-clear />
          </a-form-item>
        </div>

        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="editForm.email" placeholder="邮箱" allow-clear />
        </a-form-item>

        <div class="form-row-2">
          <a-form-item label="角色" name="role">
            <a-select v-model:value="editForm.role" style="width: 100%">
              <a-select-option value="admin">管理员</a-select-option>
              <a-select-option value="user">普通用户</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="状态" name="status">
            <a-select v-model:value="editForm.status" style="width: 100%">
              <a-select-option :value="1">正常</a-select-option>
              <a-select-option :value="0">异常</a-select-option>
            </a-select>
          </a-form-item>
        </div>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import type { FormInstance } from 'ant-design-vue';
import type { Rule } from 'ant-design-vue/es/form';
import {
  UserOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
} from '@ant-design/icons-vue';
import { listUserVoByPage, updateUser, deleteUser } from '@/api/adminController';

// ---------- 查询 ----------
const loading = ref(false);
const total = ref(0);
const tableData = ref<API.UserVO[]>([]);

const queryForm = reactive<API.UserQueryRequest>({
  current: 1,
  pageSize: 10,
  userAccount: undefined,
  username: undefined,
  email: undefined,
  role: undefined,
  status: undefined,
});

async function fetchUsers() {
  loading.value = true;
  try {
    const res = await listUserVoByPage({ ...queryForm });
    if (res.data?.data) {
      tableData.value = res.data.data.records ?? [];
      total.value = res.data.data.total ?? 0;
    }
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  queryForm.current = 1;
  fetchUsers();
}

function handleReset() {
  queryForm.userAccount = undefined;
  queryForm.username = undefined;
  queryForm.email = undefined;
  queryForm.role = undefined;
  queryForm.status = undefined;
  queryForm.current = 1;
  fetchUsers();
}

// ---------- 删除 ----------
async function handleDelete(record: API.UserVO) {
  try {
    const res = await deleteUser({ id: record.id });
    if (res.data?.data) {
      message.success('删除成功');
      fetchUsers();
    }
  } catch (_) {
    // 错误由拦截器统一处理
  }
}

// ---------- 编辑 ----------
const editVisible = ref(false);
const editLoading = ref(false);
const editFormRef = ref<FormInstance>();
const editForm = reactive<API.UserUpdateRequest>({});

const editRules: Record<string, Rule[]> = {
  userAccount: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
};

function openEditModal(record: API.UserVO) {
  Object.assign(editForm, {
    id: record.id,
    userAccount: record.userAccount,
    username: record.username,
    email: record.email,
    avatar: record.avatar,
    role: record.role,
    status: record.status,
  });
  editVisible.value = true;
}

async function handleEditOk() {
  try {
    await editFormRef.value?.validate();
  } catch {
    return;
  }
  editLoading.value = true;
  try {
    const res = await updateUser({ ...editForm });
    if (res.data?.data) {
      message.success('更新成功');
      editVisible.value = false;
      fetchUsers();
    }
  } finally {
    editLoading.value = false;
  }
}

function handleEditCancel() {
  editFormRef.value?.resetFields();
}

// ---------- 表格列定义 ----------
const columns = [
  { title: '头像', key: 'avatar', width: 64, align: 'center' as const },
  { title: 'ID', dataIndex: 'id', key: 'id', width: 72 },
  { title: '账号', dataIndex: 'userAccount', key: 'userAccount', ellipsis: true },
  { title: '昵称', dataIndex: 'username', key: 'username', ellipsis: true },
  { title: '邮箱', dataIndex: 'email', key: 'email', ellipsis: true },
  { title: '角色', key: 'role', width: 100, align: 'center' as const },
  { title: '状态', key: 'status', width: 90, align: 'center' as const },
  { title: '创建时间', dataIndex: 'creatTime', key: 'creatTime', width: 170, ellipsis: true },
  { title: '操作', key: 'action', width: 150, align: 'center' as const, fixed: 'right' as const },
];

onMounted(fetchUsers);
</script>

<style scoped>
.user-manage {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
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

/* 搜索区 */
.search-panel {
  flex-shrink: 0;
}

.search-form {
  flex-wrap: wrap;
  gap: 8px 0;
}

/* 表格区 */
.table-panel {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.user-table {
  flex: 1;
  min-height: 0;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid var(--border-soft);
  margin-top: auto;
}

/* 头像 */
.user-avatar {
  background: var(--primary-wash) !important;
  color: var(--primary) !important;
}

/* 角色标签 */
.role-tag {
  border-radius: var(--radius-pill) !important;
  border: none !important;
  font-size: 12px;
}

/* 操作按钮 */
.action-edit {
  color: var(--primary) !important;
}

.action-edit:hover {
  color: var(--primary-hover) !important;
}

/* 编辑表单 */
.edit-avatar-row {
  display: flex;
  align-items: flex-end;
  gap: 14px;
  margin-bottom: 16px;
}

.edit-preview-avatar {
  background: var(--primary-wash) !important;
  color: var(--primary) !important;
  flex-shrink: 0;
  margin-bottom: 4px;
}

.form-row-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 16px;
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
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: var(--bg-page) !important;
}

:deep(.ant-table) {
  background: transparent !important;
}

:deep(.ant-table-wrapper) {
  flex: 1;
}
</style>
