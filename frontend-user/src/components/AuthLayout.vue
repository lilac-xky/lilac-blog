<template>
  <div class="auth-page">
    <StarrySky />
    <!-- 左侧品牌区 -->
    <div class="auth-hero">
      <div class="hero-content">
        <div class="hero-logo">
          <img :src="logoUrl" alt="logo" />
          <span>{{ brandName }}</span>
        </div>
        <h1 class="hero-title">{{ heroTitle }}</h1>
        <p class="hero-desc">
          <slot name="hero-desc">{{ heroDesc }}</slot>
        </p>
        <slot name="hero-extras" />
      </div>
      <div v-if="showDeco1" class="hero-deco hero-deco-1"></div>
      <div class="hero-deco hero-deco-2"></div>
    </div>

    <!-- 右侧表单区 -->
    <div class="auth-form-wrap">
      <div class="auth-form" :style="formStyle">
        <div class="form-header">
          <h2>{{ formTitle }}</h2>
          <p>{{ formSubtitle }}</p>
        </div>
        <slot />
      </div>
    </div>
  </div>
</template>

<!--
  AuthLayout：登录/注册等鉴权页共享布局
  左侧品牌区（hero）+ 右侧表单区两栏结构，星空背景全屏铺底。
  hero / form 各开放标题/副标题/扩展插槽，便于登录与注册复用同一外壳。
  ≤900px 自动收起左侧 hero 区，只保留表单。
-->
<script setup lang="ts">
import { computed } from 'vue';
import StarrySky from './StarrySky.vue';

const props = withDefaults(
  defineProps<{
    heroTitle: string;
    heroDesc?: string;
    formTitle: string;
    formSubtitle?: string;
    brandName?: string;
    logoUrl?: string;
    showDeco1?: boolean;
    formPadding?: string;
  }>(),
  {
    brandName: 'lilac-blog',
    logoUrl:
      'https://lilacs.oss-cn-beijing.aliyuncs.com/lilac-blog/avatar/2026/05/15/2026-05-15AokvgcjLCxWBMW9v.png?x-oss-process=image/resize,w_200',
    showDeco1: true,
    formPadding: '36px 32px',
  },
);

const formStyle = computed(() => ({ padding: props.formPadding }));
</script>

<style scoped>
.auth-page {
  position: relative;
  height: 100vh;
  width: 100vw;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  overflow: hidden;
  background: rgba(var(--bg-page-rgb), 0.35);
  color: var(--text-primary);
}

.auth-page>.auth-hero,
.auth-page>.auth-form-wrap {
  position: relative;
  z-index: 1;
}

.auth-hero {
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  border-right: 1px solid var(--border-soft);
}

.hero-content {
  position: relative;
  z-index: 2;
  color: var(--text-primary);
  max-width: 440px;
}

.hero-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 40px;
}

.hero-logo img {
  width: 40px;
  height: 40px;
  border-radius: 10px;
}

.hero-logo span {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.hero-title {
  font-size: 44px;
  font-weight: 800;
  line-height: 1.2;
  margin-bottom: 16px;
  background: var(--gradient-hero);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-desc {
  font-size: 15px;
  color: var(--text-secondary);
  line-height: 1.8;
  margin-bottom: 36px;
}

.hero-deco {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
}

.hero-deco-1 {
  width: 360px;
  height: 360px;
  background: #0ea5e9;
  top: -100px;
  right: -80px;
}

.hero-deco-2 {
  width: 280px;
  height: 280px;
  background: #ec4899;
  bottom: -80px;
  left: -60px;
  opacity: 0.35;
}

.auth-form-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
  overflow-y: auto;
}

.auth-form {
  width: 100%;
  max-width: 400px;
  background: var(--bg-card);
  backdrop-filter: blur(var(--blur));
  border: 1px solid var(--border-soft);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
}

.form-header {
  margin-bottom: 28px;
}

.form-header h2 {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.form-header p {
  font-size: 14px;
  color: var(--text-secondary);
}

:deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: var(--text-secondary);
}

/* 表单提交按钮（登录/注册公用） */
:deep(.auth-submit-btn) {
  height: 46px !important;
  border-radius: var(--radius-pill) !important;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  background: var(--gradient-accent) !important;
  border: none !important;
  box-shadow: 0 8px 24px rgba(var(--accent-strong-rgb), 0.4);
}

:deep(.auth-submit-btn):hover {
  box-shadow: 0 12px 30px rgba(var(--accent-strong-rgb), 0.55) !important;
  transform: translateY(-1px);
}

/* 表单底部链接区 */
:deep(.auth-form-footer) {
  text-align: center;
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 8px;
}

:deep(.auth-link-primary) {
  color: var(--accent);
  font-weight: 600;
  margin: 0 2px;
}

:deep(.auth-link-primary):hover {
  color: var(--accent-pink);
  text-decoration: underline;
}

@media (max-width: 900px) {
  .auth-page {
    grid-template-columns: 1fr;
  }

  .auth-hero {
    display: none;
  }
}
</style>
