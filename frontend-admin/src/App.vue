<template>
  <div id="app">
    <a-config-provider :locale="zhCN" :theme="themeConfig">
      <router-view />
    </a-config-provider>
  </div>
</template>

<script setup lang="ts">
import zhCN from 'ant-design-vue/lib/locale/zh_CN';

const themeConfig = {
  token: {
    colorPrimary: '#c4a062',
    colorLink: '#a6874d',
    borderRadius: 10,
    fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif",
  },
  components: {
    Input: {
      paddingBlock: 4,
      paddingInline: 8,
    },
  },
};
</script>
