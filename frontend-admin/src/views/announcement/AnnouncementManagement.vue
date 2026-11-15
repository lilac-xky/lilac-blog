<template>
  <TableCRUD title="公告列表" total-unit="条公告" add-button-text="新增公告" :columns="columns"
    :table-data="tableData" :total="total" :loading="loading" :query-form="queryForm" :search-schema="searchSchema"
    :scroll="{ x: 1100 }" @search="handleSearch" @reset="handleReset" @add="openAdd"
    @edit="(record: any) => openEdit(record)" @delete="(record: any) => confirmDelete(record)"
    @table-change="handleTableChange" @page-change="fetchData">
    <!-- 行操作追加「发布/下线」按钮，状态切换后刷新列表 -->
    <template #extra-actions="{ record }">
      <a-divider type="vertical" />
      <a-button type="link" size="small" @click="confirmPublish(record)">
        {{ record.status === 1 ? '下线' : '发布' }}
      </a-button>
    </template>
  </TableCRUD>

  <FormModal v-model:open="modalVisible" :title="mode === 'add' ? '新增公告' : '编辑公告'" :model="formData"
    :fields="formFields" :rules="rules" :loading="modalLoading" :width="980" @ok="handleOk"
    @cancel="handleCancel">
    <!-- 公告正文使用 Markdown 编辑器，和文章编辑保持一致 -->
    <template #field-content="{ model }">
      <MdEditor v-model="model.content" :theme="'light'" language="zh-CN" :scroll-auto="false"
        class="announcement-editor" />
    </template>
  </FormModal>
</template>

<!--
  AnnouncementManagement：后台「公告管理」页
  - 列表走 announcement:list 后台分页接口，支持标题、状态筛选
  - 新增/编辑使用 Markdown 编辑器；新增后默认草稿，需要再点「发布」
  - 行操作支持发布、下线、编辑、删除，发布/下线前二次确认
-->
<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { Modal, message } from 'ant-design-vue';
import { MdEditor } from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';
import TableCRUD from '@/components/crud/TableCRUD.vue';
import FormModal from '@/components/crud/FormModal.vue';
import type { CrudColumn, FormField, SearchField } from '@/components/crud/types';
import { usePaginationQuery } from '@/composables/usePaginationQuery';
import { useModalForm } from '@/composables/useModalForm';
import { useConfirmDelete } from '@/composables/useConfirmDelete';
import {
  addAnnouncement,
  deleteAnnouncement,
  listAnnouncementByPage,
  publishAnnouncement,
  updateAnnouncement,
} from '@/api/announcementController';

// 公告状态选项：与后端 0草稿 / 1已发布 / 2已下线保持一致
const STATUS_OPTIONS = [
  { label: '草稿', value: 0 },
  { label: '已发布', value: 1 },
  { label: '已下线', value: 2 },
];

// 状态列标签映射：statusTag 渲染器按值查表
const STATUS_TAG_MAP: Record<number, { label: string; color?: string }> = {
  0: { label: '草稿', color: 'default' },
  1: { label: '已发布', color: 'green' },
  2: { label: '已下线', color: 'orange' },
};

const { loading, total, tableData, queryForm, sortOrder, fetchData, handleSearch, handleReset, handleTableChange } =
  usePaginationQuery<API.AnnouncementQueryRequest, API.Announcement>({
    initialQuery: {
      current: 1,
      pageSize: 10,
      title: undefined,
      status: undefined,
      sortOrder: 'descend',
    },
    // 分页拉取：把后端 Page 结构适配成 TableCRUD 需要的 records/total
    async fetcher(q) {
      const res = await listAnnouncementByPage(q);
      return {
        records: res.data?.data?.records ?? [],
        total: Number(res.data?.data?.total ?? 0),
      };
    },
  });

interface AnnouncementForm {
  id?: string;
  title: string;
  content: string;
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
} = useModalForm<AnnouncementForm, API.Announcement>({
  defaultForm: () => ({ id: undefined, title: '', content: '' }),
  rules: {
    title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
    content: [{ required: true, message: '请输入公告正文', trigger: 'change' }],
  },
  // 新增公告：只提交标题和 Markdown 正文，状态由后端默认置为草稿
  addApi: (form) =>
    addAnnouncement({
      title: form.title,
      content: form.content,
    }),
  // 编辑公告：带上 id，已读记录不会因编辑而重置
  updateApi: (form) =>
    updateAnnouncement({
      id: form.id as any,
      title: form.title,
      content: form.content,
    }),
  // 编辑回填：只取表单字段，避免表格中的时间等无关字段进入表单
  pickEditForm: (record) => ({
    id: record.id as any,
    title: record.title ?? '',
    content: record.content ?? '',
  }),
  onSuccess: fetchData,
});

/**
 * 发布或下线公告
 *
 * @param record 当前行公告
 * @returns 无返回值，确认后调用接口并刷新列表
 */
function confirmPublish(record: API.Announcement) {
  const publish = record.status !== 1;
  const modal = Modal.confirm({
    title: publish ? '确认发布该公告吗？' : '确认下线该公告吗？',
    content: publish
      ? `发布后用户进入前台会看到「${record.title ?? '该公告'}」`
      : `下线后「${record.title ?? '该公告'}」将不再弹窗`,
    okText: publish ? '发布' : '下线',
    cancelText: '取消',
    async onOk() {
      try {
        const res = await publishAnnouncement({
          id: record.id as any,
          publish,
        });
        if (res.data?.data) {
          message.success(publish ? '公告已发布' : '公告已下线');
          fetchData();
        }
      } catch {
        // 请求拦截器已统一提示，关闭确认框即可
        modal.destroy();
      }
    },
  });
}

const { confirmDelete } = useConfirmDelete<API.Announcement>({
  // 删除确认标题
  titleOf: () => '确认删除该公告吗？',
  // 删除确认正文：删除后不可恢复
  contentOf: (r) => `「${r.title ?? '该公告'}」删除后不可恢复`,
  // 执行删除请求
  deleteApi: (params) => deleteAnnouncement(params),
  // 删除成功后刷新当前页
  onSuccess: fetchData,
});

// 搜索条件：标题模糊匹配 + 状态筛选
const searchSchema: SearchField[] = [
  { name: 'title', label: '标题', type: 'input', placeholder: '公告标题关键词', width: 220 },
  { name: 'status', label: '状态', type: 'select', width: 120, options: STATUS_OPTIONS },
];

// 弹窗表单字段：正文使用 slot，由 MdEditor 自定义渲染
const formFields: FormField[] = [
  { name: 'title', label: '公告标题', type: 'input', placeholder: '请输入公告标题' },
  { name: 'content', label: '公告正文', type: 'slot' },
];

// 表格列：ID / 标题 / 状态 / 发布时间 / 创建时间 / 操作
const columns = computed<CrudColumn[]>(() => [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 220 },
  { title: '标题', dataIndex: 'title', key: 'title', ellipsis: true, width: 260 },
  {
    title: '状态',
    key: 'status',
    dataIndex: 'status',
    width: 100,
    align: 'center' as const,
    renderer: { type: 'statusTag', map: STATUS_TAG_MAP },
  },
  {
    title: '发布时间',
    key: 'publishTime',
    dataIndex: 'publishTime',
    width: 180,
    renderer: { type: 'datetime' },
  },
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
  { title: '操作', key: 'action', width: 230, align: 'center' as const, fixed: 'right' as const },
]);

onMounted(fetchData);
</script>

<style scoped>
/* 公告 Markdown 编辑器：固定高度，避免弹窗随正文无限增高 */
.announcement-editor {
  height: 380px;
  border-radius: 10px;
  overflow: hidden;
}

/* 公告表格单元格垂直居中，避免标题和状态标签上下错位 */
:deep(.ant-table-tbody > tr > td) {
  vertical-align: middle;
}
</style>
