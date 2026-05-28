<template>
  <div class="empty glass-card list-empty">
    <slot name="icon">
      <InboxOutlined />
    </slot>
    <p>
      <slot>{{ text }}</slot>
    </p>
    <slot name="extra" />
  </div>
</template>

<!-- ListEmpty：列表/消息空态卡片。icon 默认收件箱图标，extra 插槽可放 CTA 按钮 -->
<script setup lang="ts">
import { InboxOutlined } from '@ant-design/icons-vue';

withDefaults(
  defineProps<{
    text?: string;
  }>(),
  {
    text: '暂无数据',
  },
);
</script>
