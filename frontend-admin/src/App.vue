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

<style>
:root {
  /* 背景层 */
  --bg-page: #faf8f3;
  --bg-card: #ffffff;
  --bg-card-soft: #fbf9f4;
  --bg-sider: #1a2332;
  --bg-sider-light: #2a3a52;
  --bg-sider-active: rgba(196, 160, 98, 0.14);
  --sider-active-bar: #c4a062;
  --sider-border: rgba(255, 255, 255, 0.06);

  /* 文字 */
  --text-primary: #1a2332;
  --text-secondary: #4a5564;
  --text-muted: #94a0b1;

  /* 边框 */
  --border-soft: #efe9d5;
  --border: #e5ddc8;

  /* 主色（琥珀金） */
  --primary: #c4a062;
  --primary-hover: #a6874d;
  --primary-soft: #d4b679;
  --primary-wash: #f5ecd8;

  /* 阴影 */
  --shadow-card: 0 2px 12px rgba(26, 35, 50, 0.06);
  --shadow-hover: 0 8px 24px rgba(26, 35, 50, 0.10);

  /* 圆角 */
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
  background: var(--border);
  border-radius: 999px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

/* Ant Design 组件统一圆角 */
.ant-card {
  border-radius: var(--radius-card) !important;
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border) !important;
}

.ant-btn {
  border-radius: var(--radius-sm) !important;
  font-weight: 500;
}

.ant-btn-primary {
  background: var(--primary) !important;
  color: #fff !important;
  box-shadow: 0 4px 12px rgba(196, 160, 98, 0.28);
}

.ant-btn-primary:hover {
  background: var(--primary-hover) !important;
  box-shadow: 0 6px 18px rgba(196, 160, 98, 0.36);
}

.ant-input:not(.ant-input-affix-wrapper .ant-input),
.ant-input-affix-wrapper {
  border-radius: var(--radius-sm) !important;
}

.ant-input-affix-wrapper .ant-input {
  border-radius: 0 !important;
}

.ant-input-affix-wrapper:focus-within,
.ant-input-affix-wrapper-focused {
  border-color: var(--primary) !important;
  box-shadow: 0 0 0 3px var(--primary-wash) !important;
}

.ant-input-affix-wrapper {
  display: inline-flex !important;
  align-items: center !important;
  gap: 6px !important;
}

.ant-input-affix-wrapper .ant-input-prefix,
.ant-input-affix-wrapper .ant-input-suffix {
  margin: 0 !important;
  flex: 0 0 auto;
  color: var(--text-muted);
}

.ant-input-affix-wrapper input {
  flex: 1 1 auto !important;
  align-self: stretch !important;
  min-width: 0 !important;
  padding: 0 !important;
  margin: 0 !important;
  background: transparent !important;
}

.ant-menu-light.ant-menu-inline {
  border-inline-end: none !important;
}
</style>
