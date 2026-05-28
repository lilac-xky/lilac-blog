<template>
  <a-tag v-if="meta" :color="meta.color" class="status-tag">{{ meta.label }}</a-tag>
  <span v-else>-</span>
</template>

<!-- StatusTag：根据 value 在 map 中查表展示状态标签。无匹配显示「-」 -->
<script setup lang="ts">
import { computed } from 'vue';

interface StatusMeta {
  label: string;
  color?: string;
}

const props = defineProps<{
  value: string | number | undefined | null;
  map: Record<string | number, StatusMeta>;
}>();

const meta = computed<StatusMeta | undefined>(() => {
  if (props.value === undefined || props.value === null) return undefined;
  return props.map[props.value as keyof typeof props.map];
});
</script>
