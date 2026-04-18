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
    colorPrimary: '#1a1a1a',
    borderRadius: 10,
    fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif",
  },
};
</script>

<style>
:root {
  --bg-page: #faf9f7;
  --bg-card: #ffffff;
  --bg-hero: #1a1a1a;
  --text-primary: #1a1a1a;
  --text-secondary: #8a8a8a;
  --text-muted: #b0b0b0;
  --border-soft: #eef0f4;
  --shadow-card: 0 2px 12px rgba(0, 0, 0, 0.04);
  --shadow-hover: 0 8px 24px rgba(0, 0, 0, 0.08);
  --radius-card: 16px;
  --radius-sm: 10px;
  --radius-pill: 999px;
}

* {
  margin: 0;
  padding: 0;
  border: 0;
  box-sizing: border-box;
  font-family: inherit;
  vertical-align: baseline;
}

html,
body,
#app {
  min-height: 100%;
}

body {
  background: var(--bg-page);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC',
    'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  color: var(--text-primary);
  -webkit-font-smoothing: antialiased;
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 999px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

.ant-card {
  border-radius: var(--radius-card) !important;
  box-shadow: var(--shadow-card);
  border: none !important;
}

.ant-btn {
  border-radius: var(--radius-sm) !important;
  font-weight: 500;
}

.ant-btn-primary {
  background: var(--text-primary) !important;
}

.ant-btn-primary:hover {
  background: #333 !important;
}

.ant-input,
.ant-input-affix-wrapper {
  border-radius: var(--radius-pill) !important;
}
</style>
