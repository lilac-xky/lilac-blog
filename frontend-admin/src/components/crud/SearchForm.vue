<template>
  <a-form layout="inline" :model="model" class="search-form">
    <a-form-item v-for="field in fields" :key="field.name" :label="field.label">
      <slot :name="`field-${field.name}`" :field="field" :model="model">
        <a-input v-if="field.type === 'input'" v-model:value="model[field.name]"
          :placeholder="field.placeholder ?? `${field.label}关键词`" :allow-clear="field.allowClear ?? true"
          :style="{ width: toWidth(field.width, 200) }" />
        <a-select v-else-if="field.type === 'select'" v-model:value="model[field.name]"
          :placeholder="field.placeholder ?? '全部'" :allow-clear="field.allowClear ?? true"
          :style="{ width: toWidth(field.width, 120) }">
          <a-select-option v-for="opt in field.options" :key="String(opt.value)" :value="opt.value">
            {{ opt.label }}
          </a-select-option>
        </a-select>
      </slot>
    </a-form-item>
    <a-form-item>
      <a-button type="primary" :loading="loading" @click="$emit('search')">
        <template #icon>
          <SearchOutlined />
        </template>
        搜索
      </a-button>
      <a-button style="margin-left: 8px" @click="$emit('reset')">重置</a-button>
    </a-form-item>
  </a-form>
</template>

<!-- SearchForm：列表页搜索栏。根据 fields schema 渲染搜索条件，配合 TableCRUD 使用 -->
<script setup lang="ts">
import { SearchOutlined } from '@ant-design/icons-vue';
import type { SearchField } from './types';

defineProps<{
  model: Record<string, any>;
  fields: SearchField[];
  loading?: boolean;
}>();

defineEmits<{
  search: [];
  reset: [];
}>();

function toWidth(w: number | string | undefined, fallback: number) {
  if (w === undefined) return `${fallback}px`;
  return typeof w === 'number' ? `${w}px` : w;
}
</script>
