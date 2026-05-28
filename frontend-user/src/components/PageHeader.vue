<template>
  <header class="page-head">
    <h1>
      <slot name="title">{{ title }}</slot>
    </h1>
    <p class="subtitle">
      <span class="dot-glow"></span>
      <slot>{{ subtitle }}</slot>
    </p>
  </header>
</template>

<!-- PageHeader：前台页面通用大标题。subtitle 走默认插槽，title 走具名插槽以支持自定义渐变文字 -->
<script setup lang="ts">
defineProps<{
  title?: string;
  subtitle?: string;
}>();
</script>
