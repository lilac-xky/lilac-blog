<template>
  <div class="home">
    <!-- 顶部：统计卡片 -->
    <div class="stat-row">
      <div v-for="item in statCards" :key="item.title" class="stat-card" @click="item.onClick">
        <div class="stat-icon" :style="{ background: item.bg, color: item.color }">
          <component :is="item.icon" />
        </div>
        <div class="stat-main">
          <div class="stat-value" :style="{ color: item.color }">
            {{ item.value }}
            <span class="stat-suffix">{{ item.suffix }}</span>
          </div>
          <div class="stat-title">{{ item.title }}</div>
        </div>
        <div class="stat-trend" :class="item.trend > 0 ? 'up' : 'down'">
          <component :is="item.trend > 0 ? RiseOutlined : FallOutlined" />
          {{ Math.abs(item.trend) }}%
        </div>
      </div>
    </div>

    <!-- 中部：左快捷 + 右最近文章 -->
    <div class="middle-row">
      <!-- 快捷操作 -->
      <div class="panel quick-panel">
        <div class="panel-header">
          <div>
            <div class="panel-title">快捷操作</div>
            <div class="panel-desc">一键进入常用功能</div>
          </div>
        </div>
        <div class="quick-grid">
          <div v-for="(q, i) in quickActions" :key="i" class="quick-item" :style="{ '--c': q.color } as any"
            @click="q.onClick">
            <div class="quick-icon">
              <component :is="q.icon" />
            </div>
            <div class="quick-label">{{ q.label }}</div>
          </div>
        </div>
      </div>

      <!-- 最近文章 -->
      <div class="panel article-panel">
        <div class="panel-header">
          <div>
            <div class="panel-title">最近文章</div>
            <div class="panel-desc">最新创建或更新的内容</div>
          </div>
          <a-button type="primary" size="small" @click="router.push('/write-blog')">
            <template #icon>
              <PlusOutlined />
            </template>
            新文章
          </a-button>
        </div>
        <div class="article-list">
          <template v-if="recentArticles.length">
            <div v-for="a in recentArticles" :key="a.id" class="article-item">
              <div class="article-cover" :style="{ background: a.cover }">
                <FileTextOutlined />
              </div>
              <div class="article-body">
                <div class="article-title">{{ a.title }}</div>
                <div class="article-meta">
                  <span>
                    <ClockCircleOutlined /> {{ a.time }}
                  </span>
                  <span>
                    <EyeOutlined /> {{ a.views }}
                  </span>
                </div>
              </div>
              <a-tag :color="a.statusColor" class="article-tag">{{ a.status }}</a-tag>
            </div>
          </template>
          <div v-else class="article-empty">
            <a-empty description="暂无文章，快去写一篇吧！" :image="simpleImage" />
          </div>
        </div>
      </div>
    </div>

    <!-- 底部：问候 + 待办 -->
    <div class="bottom-row">
      <div class="panel greet-panel">
        <div class="greet-text">
          <div class="greet-hello">{{ greeting }}, Admin 👋</div>
          <div class="greet-sub">{{ today }}，今天有 <b>{{ todos.length }}</b> 项待办等待处理</div>
        </div>
        <a-avatar :size="56" class="greet-avatar">
          <template #icon>
            <UserOutlined />
          </template>
        </a-avatar>
      </div>
      <div class="panel todo-panel">
        <div class="panel-header tight">
          <div class="panel-title small">今日待办</div>
          <span class="panel-count">{{ todos.length }}</span>
        </div>
        <div class="todo-list">
          <div v-for="t in todos" :key="t.id" class="todo-item">
            <span class="todo-dot" :style="{ background: t.color }"></span>
            <span class="todo-text">{{ t.text }}</span>
            <span class="todo-time">{{ t.time }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, h } from 'vue';
import { useRouter } from 'vue-router';
import { Empty } from 'ant-design-vue';
import {
  UserOutlined,
  EditOutlined,
  FolderOutlined,
  TagsOutlined,
  MessageOutlined,
  FileTextOutlined,
  ReadOutlined,
  AppstoreOutlined,
  TagOutlined,
  EyeOutlined,
  RiseOutlined,
  FallOutlined,
  PlusOutlined,
  ClockCircleOutlined,
  SettingOutlined,
} from '@ant-design/icons-vue';

const router = useRouter();
const simpleImage = Empty.PRESENTED_IMAGE_SIMPLE;

// 问候语
const greeting = computed(() => {
  const hour = new Date().getHours();
  if (hour < 6) return '夜深了';
  if (hour < 9) return '早上好';
  if (hour < 12) return '上午好';
  if (hour < 14) return '中午好';
  if (hour < 18) return '下午好';
  return '晚上好';
});

// 今天日期
const today = computed(() => {
  const d = new Date();
  const weekDays = ['日', '一', '二', '三', '四', '五', '六'];
  return `${d.getMonth() + 1}月${d.getDate()}日 星期${weekDays[d.getDay()]}`;
});

// 统计卡片数据（占位数据，后续接入真实 API）
const statCards = [
  {
    title: '文章总数',
    value: 0,
    suffix: '篇',
    icon: h(ReadOutlined),
    color: '#1890ff',
    bg: 'rgba(24,144,255,0.1)',
    trend: 12,
    onClick: () => router.push('/write-blog'),
  },
  {
    title: '分类',
    value: 0,
    suffix: '个',
    icon: h(AppstoreOutlined),
    color: '#52c41a',
    bg: 'rgba(82,196,26,0.1)',
    trend: 5,
    onClick: () => router.push('/blog/category'),
  },
  {
    title: '标签',
    value: 0,
    suffix: '个',
    icon: h(TagOutlined),
    color: '#faad14',
    bg: 'rgba(250,173,20,0.1)',
    trend: -2,
    onClick: () => router.push('/blog/tag'),
  },
  {
    title: '总浏览量',
    value: 0,
    suffix: '次',
    icon: h(EyeOutlined),
    color: '#722ed1',
    bg: 'rgba(114,46,209,0.1)',
    trend: 18,
    onClick: () => { },
  },
];

// 快捷操作
const quickActions = [
  { label: '写文章', icon: h(EditOutlined), color: '#1890ff', onClick: () => router.push('/write-blog') },
  { label: '分类', icon: h(FolderOutlined), color: '#52c41a', onClick: () => router.push('/blog/category') },
  { label: '标签', icon: h(TagsOutlined), color: '#faad14', onClick: () => router.push('/blog/tag') },
  { label: '留言', icon: h(MessageOutlined), color: '#eb2f96', onClick: () => router.push('/message') },
  { label: '关于', icon: h(UserOutlined), color: '#722ed1', onClick: () => router.push('/about') },
  { label: '设置', icon: h(SettingOutlined), color: '#13c2c2', onClick: () => { } },
];

// 最近文章占位
const recentArticles: {
  id: number;
  title: string;
  time: string;
  views: number;
  status: string;
  statusColor: string;
  cover: string;
}[] = [];

// 今日待办占位
const todos = [
  { id: 1, text: '完成博客首页样式统一', time: '10:00', color: '#1890ff' },
  { id: 2, text: '审核 3 条新留言', time: '14:00', color: '#faad14' },
  { id: 3, text: '整理本周文章标签', time: '16:30', color: '#52c41a' },
];
</script>

<style scoped>
.home {
  height: 100%;
  display: grid;
  grid-template-rows: auto 1fr auto;
  gap: 16px;
  min-height: 0;
}

/* ========= 统计卡片 ========= */
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  background: var(--bg-card);
  border-radius: var(--radius-card);
  padding: 16px 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: var(--shadow-card);
  cursor: pointer;
  transition: all 0.25s ease;
  position: relative;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-hover);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}

.stat-main {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.1;
}

.stat-suffix {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 500;
  margin-left: 2px;
}

.stat-title {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.stat-trend {
  font-size: 12px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: var(--radius-pill);
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

.stat-trend.up {
  color: #52c41a;
  background: rgba(82, 196, 26, 0.1);
}

.stat-trend.down {
  color: #ff4d4f;
  background: rgba(255, 77, 79, 0.1);
}

/* ========= 中部 ========= */
.middle-row {
  display: grid;
  grid-template-columns: 340px 1fr;
  gap: 16px;
  min-height: 0;
}

.panel {
  background: var(--bg-card);
  border-radius: var(--radius-card);
  padding: 18px 20px;
  box-shadow: var(--shadow-card);
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 14px;
}

.panel-header.tight {
  margin-bottom: 10px;
  align-items: center;
}

.panel-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.panel-title.small {
  font-size: 14px;
}

.panel-desc {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.panel-count {
  font-size: 12px;
  color: var(--text-secondary);
  background: var(--bg-page);
  padding: 2px 10px;
  border-radius: var(--radius-pill);
}

/* 快捷操作 */
.quick-panel {
  overflow: hidden;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  flex: 1;
  align-content: start;
}

.quick-item {
  background: var(--bg-page);
  border-radius: 12px;
  padding: 14px 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.25s ease;
  border: 1px solid transparent;
}

.quick-item:hover {
  transform: translateY(-2px);
  background: #fff;
  border-color: var(--c);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.quick-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--c);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  opacity: 0.9;
  transition: all 0.25s ease;
}

.quick-item:hover .quick-icon {
  transform: scale(1.08);
  opacity: 1;
}

.quick-label {
  font-size: 12px;
  color: var(--text-primary);
  font-weight: 500;
}

/* 最近文章 */
.article-panel {
  overflow: hidden;
}

.article-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.article-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 12px;
  background: var(--bg-page);
  transition: all 0.2s ease;
  cursor: pointer;
}

.article-item:hover {
  background: #fff;
  box-shadow: var(--shadow-card);
  transform: translateX(4px);
}

.article-cover {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  flex-shrink: 0;
}

.article-body {
  flex: 1;
  min-width: 0;
}

.article-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.article-meta {
  font-size: 11px;
  color: var(--text-secondary);
  display: flex;
  gap: 10px;
  margin-top: 2px;
}

.article-tag {
  border-radius: var(--radius-pill) !important;
  font-size: 11px;
  border: none !important;
}

.article-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ========= 底部 ========= */
.bottom-row {
  display: grid;
  grid-template-columns: 340px 1fr;
  gap: 16px;
}

.greet-panel {
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
  color: #fff;
  padding: 16px 20px;
}

.greet-text {
  min-width: 0;
}

.greet-hello {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
}

.greet-sub {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 4px;
}

.greet-sub b {
  color: #ffd666;
  font-weight: 700;
}

.greet-avatar {
  background: rgba(255, 255, 255, 0.15) !important;
}

.todo-panel {
  padding: 14px 18px;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 10px;
  border-radius: 10px;
  transition: background 0.2s ease;
}

.todo-item:hover {
  background: var(--bg-page);
}

.todo-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.todo-text {
  flex: 1;
  font-size: 13px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.todo-time {
  font-size: 11px;
  color: var(--text-secondary);
}

/* 响应式：小屏堆叠 */
@media (max-width: 1200px) {
  .stat-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .middle-row,
  .bottom-row {
    grid-template-columns: 1fr;
  }
}
</style>
