<template>
  <a-space>
    <slot name="prepend" />
    <a-button v-if="showEdit" type="link" size="small" class="action-edit" @click="$emit('edit')">
      <template #icon>
        <EditOutlined />
      </template>
      {{ editText }}
    </a-button>
    <a-divider v-if="showEdit && showDelete" type="vertical" />
    <a-button v-if="showDelete" type="link" size="small" danger @click="$emit('delete')">
      <template #icon>
        <DeleteOutlined />
      </template>
      {{ deleteText }}
    </a-button>
    <slot name="append" />
  </a-space>
</template>

<!-- OperationCell：表格行操作列，统一「编辑 | 删除」按钮样式。通过 prepend / append 插槽插入自定义按钮 -->
<script setup lang="ts">
import { EditOutlined, DeleteOutlined } from '@ant-design/icons-vue';

withDefaults(
  defineProps<{
    showEdit?: boolean;
    showDelete?: boolean;
    editText?: string;
    deleteText?: string;
  }>(),
  {
    showEdit: true,
    showDelete: true,
    editText: '编辑',
    deleteText: '删除',
  },
);

defineEmits<{
  edit: [];
  delete: [];
}>();
</script>
