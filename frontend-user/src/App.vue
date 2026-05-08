<template>
  <div id="app">
    <a-config-provider :locale="zhCN" :theme="themeConfig">
      <router-view />
    </a-config-provider>
  </div>
</template>

<script setup lang="ts">
import { theme } from 'ant-design-vue';
import zhCN from 'ant-design-vue/lib/locale/zh_CN';

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

<style>
:root {
  /* 背景层 */
  --bg-page: #08070f;
  --bg-page-2: #0f0e1a;
  --bg-card: rgba(22, 18, 42, 0.72);
  --bg-card-solid: #15122b;
  --bg-elev: rgba(38, 32, 64, 0.78);

  /* 文字 */
  --text-primary: #f5f3ff;
  --text-secondary: rgba(245, 243, 255, 0.65);
  --text-muted: rgba(245, 243, 255, 0.4);

  /* 边框 */
  --border-soft: rgba(255, 255, 255, 0.14);
  --border-strong: rgba(255, 255, 255, 0.22);

  /* 主色（星空青） */
  --accent: #38bdf8;
  --accent-strong: #0ea5e9;
  --accent-rgb: 56, 189, 248;
  --accent-pink: #ec4899;

  /* 阴影 */
  --shadow-card: 0 8px 32px rgba(0, 0, 0, 0.25);
  --shadow-hover: 0 14px 40px rgba(14, 165, 233, 0.25);

  /* 圆角 & 模糊 */
  --radius-card: 20px;
  --radius-sm: 12px;
  --radius-pill: 999px;
  --blur: 22px;
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

a {
  color: var(--text-primary);
  text-decoration: none;
}

/* 全局滚动条 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-thumb {
  background: rgba(56, 189, 248, 0.35);
  border-radius: 999px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(56, 189, 248, 0.55);
}

::-webkit-scrollbar-track {
  background: transparent;
}

::selection {
  background: rgba(56, 189, 248, 0.4);
  color: #fff;
}

/* ----- Ant Design 全局覆写 ----- */

.ant-card {
  border-radius: var(--radius-card) !important;
  background: var(--bg-card) !important;
  backdrop-filter: blur(var(--blur)) !important;
  -webkit-backdrop-filter: blur(var(--blur)) !important;
  border: 1px solid var(--border-soft) !important;
  box-shadow: var(--shadow-card) !important;
}

.ant-btn {
  border-radius: var(--radius-pill) !important;
  font-weight: 500;
}

.ant-btn-primary {
  background: var(--accent) !important;
  border-color: var(--accent) !important;
  color: #fff !important;
}

.ant-btn-primary:hover {
  background: var(--accent-strong) !important;
  border-color: var(--accent-strong) !important;
}

.ant-input,
.ant-input-affix-wrapper {
  border-radius: var(--radius-pill) !important;
  background: rgba(255, 255, 255, 0.04) !important;
  border-color: var(--border-soft) !important;
  color: var(--text-primary) !important;
}

.ant-input-affix-wrapper:focus,
.ant-input-affix-wrapper-focused,
.ant-input:focus {
  border-color: var(--accent) !important;
  box-shadow: 0 0 0 2px rgba(56, 189, 248, 0.18) !important;
}

.ant-input::placeholder,
.ant-input-affix-wrapper input::placeholder {
  color: var(--text-muted) !important;
}

/* prefix/clear 与光标的间距修复（沿用旧规则） */
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
  color: var(--text-muted);
}

.ant-input-affix-wrapper input {
  flex: 1 1 auto !important;
  min-width: 0 !important;
  padding: 0 4px !important;
  margin: 0 !important;
  background: transparent !important;
  color: var(--text-primary) !important;
}

.ant-form-item-label > label {
  color: var(--text-secondary) !important;
}

.ant-checkbox-wrapper {
  color: var(--text-secondary) !important;
}

.ant-pagination .ant-pagination-item {
  background: rgba(255, 255, 255, 0.04);
  border-color: var(--border-soft);
}

.ant-pagination .ant-pagination-item a {
  color: var(--text-secondary);
}

.ant-pagination .ant-pagination-item-active {
  background: var(--accent);
  border-color: var(--accent);
}

.ant-pagination .ant-pagination-item-active a {
  color: #fff;
}

.ant-dropdown-menu {
  background: var(--bg-card-solid) !important;
  border: 1px solid var(--border-soft) !important;
  backdrop-filter: blur(var(--blur));
}

.ant-dropdown-menu-item {
  color: var(--text-secondary) !important;
}

.ant-dropdown-menu-item:hover {
  background: rgba(56, 189, 248, 0.12) !important;
  color: var(--text-primary) !important;
}

/* 通用玻璃卡 utility */
.glass-card {
  background: var(--bg-card);
  backdrop-filter: blur(var(--blur));
  -webkit-backdrop-filter: blur(var(--blur));
  border: 1px solid var(--border-soft);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
}
</style>
