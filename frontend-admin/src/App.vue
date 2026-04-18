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
  --bg-page: #f4f6fa;
  --bg-card: #ffffff;
  --bg-sider: #1a1a1a;
  --bg-sider-active: #2a2a2a;
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
  height: 100%;
  overflow: hidden;
}

body {
  background: var(--bg-page);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC',
    'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  color: var(--text-primary);
  -webkit-font-smoothing: antialiased;
}

/* 全局滚动条 */
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

/* Ant Design 组件统一圆角 */
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

.ant-input-affix-wrapper {
  display: inline-flex !important;
  align-items: center !important;
  gap: 8px !important;
  padding-inline: 14px !important;
}

.ant-input-affix-wrapper.ant-input-affix-wrapper-lg {
  padding-block: 8px !important;
  padding-inline: 16px !important;
}

.ant-input-affix-wrapper .ant-input-prefix,
.ant-input-affix-wrapper .ant-input-suffix {
  margin: 0 !important;
  flex: 0 0 auto;
  color: #bfbfbf;
}

.ant-input-affix-wrapper input {
  flex: 1 1 auto !important;
  min-width: 0 !important;
  padding: 0 4px !important;
  margin: 0 !important;
  background: transparent !important;
}

.ant-menu-light.ant-menu-inline {
  border-inline-end: none !important;
}
</style>
