<template>
  <div class="tag-manage">
    <!-- 搜索面板 -->
    <div class="panel search-panel">
      <div class="panel-header tight">
        <div class="panel-title small">条件筛选</div>
        <a-button type="primary" @click="openAddModal">
          <template #icon>
            <PlusOutlined />
          </template>
          新增标签
        </a-button>
      </div>
      <a-form layout="inline" :model="queryForm" class="search-form">
        <a-form-item label="标签名">
          <a-input v-model:value="queryForm.tagName" placeholder="标签名关键词" allow-clear style="width: 200px" />
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
          标签列表
          <span class="total-badge">共 {{ total }} 个标签</span>
        </div>
      </div>

      <a-table :columns="columns" :data-source="tableData" :loading="loading" :pagination="false" row-key="id"
        size="middle" class="tag-table" :scroll="{ x: 700 }" :show-sorter-tooltip="false"
        @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <!-- 创建时间列 -->
          <template v-if="column.key === 'createTime'">
            {{ record.createTime ? record.createTime.replace('T', ' ').split('.')[0] : '-' }}
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
          :show-total="(t: number) => `共 ${t} 条`" @change="fetchTags" @show-size-change="fetchTags" />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <a-modal v-model:open="modalVisible" :title="editingId ? '编辑标签' : '新增标签'" :confirm-loading="modalLoading"
      ok-text="保存" cancel-text="取消" :width="420" @ok="handleModalOk" @cancel="handleModalCancel">
      <a-form ref="modalFormRef" :model="modalForm" :rules="modalRules" layout="vertical" style="margin-top: 16px">
        <a-form-item label="标签名" name="tagName">
          <a-input v-model:value="modalForm.tagName" placeholder="请输入标签名" allow-clear />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import type { FormInstance } from 'ant-design-vue';
import type { Rule } from 'ant-design-vue/es/form';
import {
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  PlusOutlined,
} from '@ant-design/icons-vue';
import { listTagByPage, addTag, updateTag, deleteTag } from '@/api/tagController';

// ---------- 查询 ----------
const loading = ref(false);
const total = ref(0);
const tableData = ref<API.Tag[]>([]);

const queryForm = reactive<API.TagQueryRequest>({
  current: 1,
  pageSize: 10,
  tagName: undefined,
  sortOrder: 'descend',
});

// 创建时间排序方向（默认降序）
const sortCreateOrder = ref<'descend' | 'ascend'>('descend');

async function fetchTags() {
  loading.value = true;
  try {
    const res = await listTagByPage({ ...queryForm });
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
  fetchTags();
}

function handleReset() {
  queryForm.tagName = undefined;
  queryForm.current = 1;
  sortCreateOrder.value = 'descend';
  queryForm.sortOrder = 'descend';
  fetchTags();
}

// 表格变化（仅用于切换创建时间排序方向）
function handleTableChange(_pagination: unknown, _filters: unknown, sorter: { columnKey?: string; field?: string } | undefined) {
  if (!sorter) return;
  const key = sorter.columnKey ?? sorter.field;
  if (key !== 'createTime') return;
  sortCreateOrder.value = sortCreateOrder.value === 'descend' ? 'ascend' : 'descend';
  queryForm.sortOrder = sortCreateOrder.value;
  queryForm.current = 1;
  fetchTags();
}

// ---------- 删除 ----------
function confirmDelete(record: API.Tag) {
  const modal = Modal.confirm({
    title: '确认删除该标签吗？',
    content: `「${record.tagName}」删除后不可恢复`,
    okText: '确认',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        const res = await deleteTag({ id: record.id });
        if (res.data?.data) {
          message.success('删除成功');
          fetchTags();
        }
      } catch {
        modal.destroy();
      }
    },
  });
}

// ---------- 新增/编辑弹窗 ----------
const modalVisible = ref(false);
const modalLoading = ref(false);
const editingId = ref<number | undefined>(undefined);
const modalFormRef = ref<FormInstance>();
const modalForm = reactive<{ tagName: string }>({ tagName: '' });

const modalRules: Record<string, Rule[]> = {
  tagName: [{ required: true, message: '请输入标签名', trigger: 'blur' }],
};

function openAddModal() {
  editingId.value = undefined;
  modalForm.tagName = '';
  modalVisible.value = true;
}

function openEditModal(record: API.Tag) {
  editingId.value = record.id as number;
  modalForm.tagName = record.tagName ?? '';
  modalVisible.value = true;
}

async function handleModalOk() {
  try {
    await modalFormRef.value?.validate();
  } catch {
    return;
  }
  modalLoading.value = true;
  try {
    if (editingId.value) {
      const res = await updateTag({ id: editingId.value, tagName: modalForm.tagName });
      if (res.data?.data) {
        message.success('更新成功');
        modalVisible.value = false;
        fetchTags();
      }
    } else {
      const res = await addTag({ tagName: modalForm.tagName });
      if (res.data?.data) {
        message.success('新增成功');
        modalVisible.value = false;
        fetchTags();
      }
    }
  } finally {
    modalLoading.value = false;
  }
}

function handleModalCancel() {
  modalFormRef.value?.resetFields();
}

// ---------- 表格列定义 ----------
const columns = computed(() => [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 240 },
  { title: '标签名', dataIndex: 'tagName', key: 'tagName', width: 200 },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180,
    sorter: true,
    sortOrder: sortCreateOrder.value,
    sortDirections: ['descend', 'ascend'] as const,
  },
  { title: '操作', key: 'action', width: 130, align: 'center' as const, fixed: 'right' as const },
]);

onMounted(fetchTags);
</script>

<style scoped>
.tag-manage {
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
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: var(--bg-page) !important;
}

:deep(.ant-table) {
  background: transparent !important;
}
</style>
