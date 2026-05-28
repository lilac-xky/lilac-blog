<template>
  <a-modal :open="open" :title="title" :confirm-loading="loading" ok-text="保存" cancel-text="取消" :width="width"
    @update:open="(v: boolean) => $emit('update:open', v)" @ok="onOk" @cancel="onCancel">
    <a-form ref="formRef" :model="model" :rules="rules" :layout="layout" style="margin-top: 16px">
      <slot name="prepend" :model="model" />
      <template v-for="field in fields" :key="field.name">
        <a-form-item :label="field.label" :name="field.name">
          <slot :name="`field-${field.name}`" :model="model" :field="field">
            <a-input v-if="field.type === 'input'" v-model:value="model[field.name]"
              :placeholder="field.placeholder ?? `请输入${field.label ?? ''}`" :allow-clear="field.allowClear ?? true" />
            <a-textarea v-else-if="field.type === 'textarea'" v-model:value="model[field.name]" :rows="field.rows ?? 3"
              :placeholder="field.placeholder ?? `请输入${field.label ?? ''}`" :allow-clear="field.allowClear ?? true" />
            <a-select v-else-if="field.type === 'select'" v-model:value="model[field.name]"
              :placeholder="field.placeholder ?? '请选择'" :allow-clear="field.allowClear ?? true">
              <a-select-option v-for="opt in field.options" :key="String(opt.value)" :value="opt.value">
                {{ opt.label }}
              </a-select-option>
            </a-select>
            <a-switch v-else-if="field.type === 'switch'" v-model:checked="model[field.name]" />
          </slot>
        </a-form-item>
      </template>
      <slot name="append" :model="model" />
    </a-form>
  </a-modal>
</template>

<!--
  FormModal：表单弹窗组件
  根据 fields schema 自动渲染 input/textarea/select/switch 等控件，校验后通过 ok 事件通知父组件。
  关闭时清理校验态，避免下次打开沿用上次的红色错误样式。
-->
<script setup lang="ts">
import { ref, watch, nextTick } from 'vue';
import type { FormInstance } from 'ant-design-vue';
import type { Rule } from 'ant-design-vue/es/form';
import type { FormField } from './types';

const props = withDefaults(
  defineProps<{
    open: boolean;
    title: string;
    model: Record<string, any>;
    fields: FormField[];
    rules?: Record<string, Rule[]>;
    loading?: boolean;
    width?: number | string;
    layout?: 'horizontal' | 'vertical' | 'inline';
  }>(),
  {
    width: 420,
    layout: 'vertical',
  },
);

const emit = defineEmits<{
  'update:open': [v: boolean];
  ok: [];
  cancel: [];
}>();

const formRef = ref<FormInstance>();

defineExpose({ formRef });

async function onOk() {
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }
  emit('ok');
}

function onCancel() {
  formRef.value?.resetFields();
  emit('cancel');
}

// 关闭后下一帧清空校验态，避免再次打开仍残留红框
watch(
  () => props.open,
  (v) => {
    if (!v) {
      nextTick(() => formRef.value?.clearValidate());
    }
  },
);
</script>
