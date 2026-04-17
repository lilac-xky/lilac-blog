<template>
  <div class="home">
    <!-- 欢迎区域 -->
    <a-card class="welcome-card" :bordered="false">
      <a-row align="middle" :gutter="16">
        <a-col>
          <a-avatar :size="64" style="background-color: #7265e6">
            <template #icon>
              <UserOutlined />
            </template>
          </a-avatar>
        </a-col>
        <a-col>
          <h2 class="welcome-title">{{ greeting }}，欢迎回来！</h2>
          <p class="welcome-desc">今天是 {{ today }}，祝你拥有愉快的一天。</p>
        </a-col>
      </a-row>
    </a-card>

    <!-- 统计卡片 -->
    <a-row :gutter="16" class="stat-row">
      <a-col :xs="24" :sm="12" :lg="6" v-for="item in statCards" :key="item.title">
        <a-card hoverable :bordered="false" class="stat-card">
          <a-statistic :title="item.title" :value="item.value" :value-style="{ color: item.color }">
            <template #prefix>
              <component :is="item.icon" />
            </template>
            <template #suffix v-if="item.suffix">
              <span class="stat-suffix">{{ item.suffix }}</span>
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" class="content-row">
      <!-- 快捷操作 -->
      <a-col :xs="24" :lg="8">
        <a-card title="快捷操作" :bordered="false" class="section-card">
          <a-space direction="vertical" :size="12" style="width: 100%">
            <a-button type="primary" block @click="$router.push('/write-blog')">
              <template #icon>
                <EditOutlined />
              </template>
              写新文章
            </a-button>
            <a-button block @click="$router.push('/blog/category')">
              <template #icon>
                <FolderOutlined />
              </template>
              管理分类
            </a-button>
            <a-button block @click="$router.push('/blog/tag')">
              <template #icon>
                <TagsOutlined />
              </template>
              管理标签
            </a-button>
            <a-button block @click="$router.push('/message')">
              <template #icon>
                <MessageOutlined />
              </template>
              查看留言
            </a-button>
          </a-space>
        </a-card>
      </a-col>

      <!-- 最近文章 -->
      <a-col :xs="24" :lg="16">
        <a-card title="最近文章" :bordered="false" class="section-card">
          <a-list item-layout="horizontal" :data-source="recentArticles">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta :description="item.description">
                  <template #title>
                    <a>{{ item.title }}</a>
                  </template>
                  <template #avatar>
                    <a-avatar style="background-color: #1890ff">
                      <template #icon>
                        <FileTextOutlined />
                      </template>
                    </a-avatar>
                  </template>
                </a-list-item-meta>
                <template #actions>
                  <a-tag :color="item.statusColor">{{ item.status }}</a-tag>
                </template>
              </a-list-item>
            </template>
            <template #header v-if="recentArticles.length === 0">
              <a-empty description="暂无文章，快去写一篇吧！" />
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>

    <!-- 系统信息 -->
    <a-card title="系统信息" :bordered="false" class="section-card">
      <a-descriptions :column="{ xs: 1, sm: 2, lg: 4 }" bordered size="small">
        <a-descriptions-item label="系统名称">lilac-blog 后台管理</a-descriptions-item>
        <a-descriptions-item label="前端框架">Vue 3 + Ant Design Vue</a-descriptions-item>
        <a-descriptions-item label="后端框架">Spring Boot 3</a-descriptions-item>
        <a-descriptions-item label="数据库">MySQL</a-descriptions-item>
      </a-descriptions>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, h } from 'vue';
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
} from '@ant-design/icons-vue';

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
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${weekDays[d.getDay()]}`;
});

// 统计卡片数据（占位数据，后续接入真实 API）
const statCards = [
  { title: '文章总数', value: 0, icon: h(ReadOutlined), color: '#1890ff', suffix: '篇' },
  { title: '分类数', value: 0, icon: h(AppstoreOutlined), color: '#52c41a', suffix: '个' },
  { title: '标签数', value: 0, icon: h(TagOutlined), color: '#faad14', suffix: '个' },
  { title: '总浏览量', value: 0, icon: h(EyeOutlined), color: '#722ed1', suffix: '次' },
];

// 最近文章（占位数据，后续接入真实 API）
const recentArticles: { title: string; description: string; status: string; statusColor: string }[] = [];
</script>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
}

.welcome-title {
  color: #fff;
  margin: 0 0 4px 0;
  font-size: 22px;
}

.welcome-desc {
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
  font-size: 14px;
}

.stat-row {
  margin: 0;
}

.stat-card {
  border-radius: 8px;
  margin-bottom: 0;
}

.stat-suffix {
  font-size: 14px;
  color: #999;
}

.section-card {
  border-radius: 8px;
}

.content-row {
  margin: 0;
}
</style>
