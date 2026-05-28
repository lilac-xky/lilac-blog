<template>
  <TableCRUD title="用户列表" total-unit="名用户" :columns="columns" :table-data="tableData" :total="total" :loading="loading"
    :query-form="queryForm" :search-schema="searchSchema" :scroll="{ x: 900 }" :show-add="false"
    :switch-loading-id="statusLoadingId" @search="handleSearch" @reset="handleReset"
    @edit="(record: any) => openEdit(record)" @delete="(record: any) => confirmDelete(record)"
    @table-change="handleTableChange" @page-change="fetchData">
    <template #column-role="{ record }">
      <a-tag :color="record.role === 'admin' ? 'gold' : 'blue'" class="role-tag">
        {{ record.role === 'admin' ? '管理员' : '普通用户' }}
      </a-tag>
    </template>
  </TableCRUD>

  <!-- 编辑弹窗（结构较定制，保留原 DOM） -->
  <a-modal v-model:open="modalVisible" title="编辑用户" :confirm-loading="modalLoading" ok-text="保存" cancel-text="取消"
    :width="520" @ok="onModalOk" @cancel="onModalCancel">
    <a-form ref="editFormRef" :model="formData" :rules="editRules" layout="vertical" class="edit-form">
      <!-- 头像区域 -->
      <div class="edit-avatar-section">
        <a-upload :show-upload-list="false" accept="image/png,image/jpeg,image/jpg,image/webp"
          :custom-request="handleAvatarUpload" :before-upload="beforeAvatarUpload">
          <div class="avatar-uploader-wrap">
            <a-avatar :src="getAvatarSrc(formData.avatar)" :size="80" class="edit-preview-avatar">
              <template #icon>
                <UserOutlined />
              </template>
            </a-avatar>
            <div class="avatar-upload-mask">
              <LoadingOutlined v-if="avatarUploading" />
              <CameraOutlined v-else />
              <span class="mask-text">更换头像</span>
            </div>
          </div>
        </a-upload>
        <a-form-item name="avatar" class="avatar-input-item">
          <a-input v-model:value="formData.avatar" placeholder="或输入图片链接" allow-clear size="small" />
        </a-form-item>
      </div>

      <a-divider class="edit-divider" />

      <!-- 基本信息 -->
      <div class="form-section">
        <div class="form-section-label">基本信息</div>
        <div class="form-row-2">
          <a-form-item label="账号" name="userAccount">
            <a-input v-model:value="formData.userAccount" placeholder="账号" allow-clear />
          </a-form-item>
          <a-form-item label="昵称" name="username">
            <a-input v-model:value="formData.username" placeholder="昵称" allow-clear />
          </a-form-item>
        </div>
        <a-form-item label="邮箱" name="email" style="margin-bottom: 0">
          <a-input v-model:value="formData.email" placeholder="邮箱" allow-clear />
        </a-form-item>
      </div>

      <a-divider class="edit-divider" />

      <!-- 权限设置 -->
      <div class="form-section">
        <div class="form-section-label">权限设置</div>
        <div class="form-row-2" style="margin-bottom: 0">
          <a-form-item label="角色" name="role" style="margin-bottom: 0">
            <a-select v-model:value="formData.role" style="width: 100%">
              <a-select-option value="admin">管理员</a-select-option>
              <a-select-option value="user">普通用户</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="状态" name="status" style="margin-bottom: 0">
            <a-select v-model:value="formData.status" style="width: 100%">
              <a-select-option :value="1">正常</a-select-option>
              <a-select-option :value="0">异常</a-select-option>
            </a-select>
          </a-form-item>
        </div>
      </div>
    </a-form>
  </a-modal>
</template>

<!--
  UserManagement：后台「用户管理」页
  不开放「新增」（注册由用户自己完成）；编辑弹窗结构较定制（含头像上传 + 分组表单），保留原 DOM 没走 FormModal。
  状态切换走 switch 列内置渲染器，禁用当前登录管理员对自己的状态切换。
-->
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { message } from 'ant-design-vue';
import type { FormInstance } from 'ant-design-vue';
import type { Rule } from 'ant-design-vue/es/form';
import {
  UserOutlined,
  CameraOutlined,
  LoadingOutlined,
} from '@ant-design/icons-vue';
import TableCRUD from '@/components/crud/TableCRUD.vue';
import type { CrudColumn, SearchField } from '@/components/crud/types';
import { usePaginationQuery } from '@/composables/usePaginationQuery';
import { useModalForm } from '@/composables/useModalForm';
import { useConfirmDelete } from '@/composables/useConfirmDelete';
import { listUserVoByPage, updateUser, deleteUser, updateUserStatus } from '@/api/adminController';
import { uploadFile } from '@/api/fileController';
import { useUserStore } from '@/stores/user';

const { loginUser } = useUserStore();

const { loading, total, tableData, queryForm, sortOrder, fetchData, handleSearch, handleReset, handleTableChange } =
  usePaginationQuery<API.UserQueryRequest, API.UserVO>({
    initialQuery: {
      current: 1,
      pageSize: 10,
      userAccount: undefined,
      username: undefined,
      email: undefined,
      role: undefined,
      status: undefined,
      sortOrder: 'descend',
    },
    async fetcher(q) {
      const res = await listUserVoByPage(q);
      return {
        records: res.data?.data?.records ?? [],
        total: (res.data?.data?.total as unknown as number) ?? 0,
      };
    },
  });

// 状态切换
const statusLoadingId = ref<any>(null);
async function handleStatusChange(record: API.UserVO, val: boolean) {
  statusLoadingId.value = record.id;
  const newStatus = val ? 1 : 0;
  try {
    const res = await updateUserStatus({ id: record.id as any, status: newStatus });
    if (res.data?.data) {
      record.status = newStatus;
      message.success(val ? '已启用' : '已禁用');
    }
  } finally {
    statusLoadingId.value = null;
  }
}

// 删除
const { confirmDelete } = useConfirmDelete<API.UserVO>({
  titleOf: () => '确认删除该用户吗？',
  deleteApi: (params) => deleteUser(params),
  onSuccess: fetchData,
});

// 编辑弹窗
const editFormRef = ref<FormInstance>();
const editRules: Record<string, Rule[]> = {
  userAccount: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
};

const {
  visible: modalVisible,
  loading: modalLoading,
  formData,
  openEdit,
  handleOk,
} = useModalForm<API.UserUpdateRequest, API.UserVO>({
  defaultForm: () => ({}),
  updateApi: (form) => updateUser(form),
  pickEditForm: (record) => ({
    id: record.id,
    userAccount: record.userAccount,
    username: record.username,
    email: record.email,
    avatar: record.avatar,
    role: record.role,
    status: record.status,
  }),
  onSuccess: fetchData,
});

async function onModalOk() {
  try {
    await editFormRef.value?.validate();
  } catch {
    return;
  }
  await handleOk();
}

function onModalCancel() {
  editFormRef.value?.resetFields();
}

// 头像上传
const avatarUploading = ref(false);

// OSS 头像统一加 webp + 200 宽缩放参数，未带参数时拼一次；非 OSS 链接原样返回
function getAvatarSrc(url?: string): string | undefined {
  if (!url) return undefined;
  if (!url.includes('.aliyuncs.com')) return url;
  if (url.includes('x-oss-process')) return url;
  return url + '?x-oss-process=image/resize,w_200/format,webp';
}

function beforeAvatarUpload(file: File) {
  const allowed = ['image/png', 'image/jpeg', 'image/jpg', 'image/webp'];
  if (!allowed.includes(file.type)) {
    message.error('仅支持 PNG / JPG / WEBP 格式');
    return false;
  }
  if (file.size > 5 * 1024 * 1024) {
    message.error('图片不能超过 5MB');
    return false;
  }
  return true;
}

async function handleAvatarUpload({ file }: { file: File }) {
  avatarUploading.value = true;
  try {
    const res = await uploadFile({ type: 'avatar' }, {}, file);
    const url = res.data?.data?.url;
    if (url) {
      formData.avatar = url;
      message.success('头像上传成功');
    }
  } finally {
    avatarUploading.value = false;
  }
}

// 搜索 schema
const searchSchema: SearchField[] = [
  { name: 'userAccount', label: '账号', type: 'input', placeholder: '账号', width: 140 },
  { name: 'username', label: '昵称', type: 'input', placeholder: '昵称', width: 140 },
  { name: 'email', label: '邮箱', type: 'input', placeholder: '邮箱', width: 180 },
  {
    name: 'role',
    label: '角色',
    type: 'select',
    width: 120,
    options: [
      { label: '管理员', value: 'admin' },
      { label: '普通用户', value: 'user' },
    ],
  },
  {
    name: 'status',
    label: '状态',
    type: 'select',
    width: 100,
    options: [
      { label: '正常', value: 1 },
      { label: '异常', value: 0 },
    ],
  },
];

// 列定义
const columns = computed<CrudColumn[]>(() => [
  {
    title: '头像',
    key: 'avatar',
    dataIndex: 'avatar',
    width: 60,
    align: 'center' as const,
    renderer: { type: 'avatar', size: 36 },
  },
  { title: 'ID', dataIndex: 'id', key: 'id', width: 120, ellipsis: true },
  { title: '账号', dataIndex: 'userAccount', key: 'userAccount', width: 110, ellipsis: true },
  { title: '昵称', dataIndex: 'username', key: 'username', width: 110, ellipsis: true },
  { title: '邮箱', dataIndex: 'email', key: 'email', width: 160, ellipsis: true },
  { title: '角色', key: 'role', width: 90, align: 'center' as const },
  {
    title: '状态',
    key: 'status',
    dataIndex: 'status',
    width: 100,
    align: 'center' as const,
    renderer: {
      type: 'switch',
      getChecked: (r: API.UserVO) => r.status === 1,
      onChange: handleStatusChange,
      // 禁止管理员把自己禁用，避免锁死后台入口
      disabled: (r: API.UserVO) => r.id === loginUser?.id,
    },
  },
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

onMounted(fetchData);
</script>

<style scoped>
:deep(.search-form .ant-input-affix-wrapper) {
  padding: 4px 8px;
}

/* 头像 */
:deep(.ant-avatar) {
  background: var(--primary-wash) !important;
  color: var(--primary) !important;
}

/* 编辑表单 */
.edit-form {
  padding: 8px 0 0;
}

.edit-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 16px 0 8px;
}

.avatar-uploader-wrap {
  position: relative;
  display: inline-block;
  cursor: pointer;
  border-radius: 50%;
}

.avatar-uploader-wrap:hover .avatar-upload-mask {
  opacity: 1;
}

.avatar-upload-mask {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  opacity: 0;
  transition: opacity 0.2s;
  color: #fff;
  font-size: 16px;
}

.mask-text {
  font-size: 11px;
  line-height: 1;
}

.edit-preview-avatar {
  background: var(--primary-wash) !important;
  color: var(--primary) !important;
  border: 3px solid var(--primary-wash);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.10);
  display: block !important;
}

.avatar-input-item {
  width: 100%;
  margin-bottom: 0 !important;
}

.edit-divider {
  margin: 8px 0 16px !important;
  border-color: var(--border-soft) !important;
}

.form-section {
  margin-bottom: 4px;
}

.form-section-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-secondary);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  margin-bottom: 12px;
}

.form-row-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 16px;
}
</style>
