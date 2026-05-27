<template>
  <div id="app">
    <a-config-provider :locale="zhCN" :theme="themeConfig">
      <router-view />
    </a-config-provider>
  </div>
</template>

<script setup lang="ts">
import { provide } from 'vue';
import { theme } from 'ant-design-vue';
import zhCN from 'ant-design-vue/lib/locale/zh_CN';
import { FakePlayerKey, useFakePlayer } from '@/composables/useFakePlayer';

provide(FakePlayerKey, useFakePlayer());

// 全局主题配置（暗色调 · 青色主色）
const themeConfig = {
  algorithm: theme.darkAlgorithm,
  token: {
    colorPrimary: '#38bdf8',
    colorInfo: '#38bdf8',
    colorBgBase: '#08070f',
    borderRadius: 12,
    fontFamily:
      "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif",
  },
};
</script>
