import { createRouter, createWebHistory } from 'vue-router'
import BasicLayout from '@/layouts/BasicLayout.vue'
import Home from '@/views/Home.vue'
import Archive from '@/views/Archive.vue'
import ArticleDetail from '@/views/ArticleDetail.vue'
import Login from '@/views/login/Login.vue'
import Register from '@/views/login/Register.vue'
import { useUserStore } from '@/stores/user'
import { message } from 'ant-design-vue'

// 前台路由：登录/注册独立页 + BasicLayout 包裹的内容页
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: Login,
    },
    {
      path: '/register',
      name: 'register',
      component: Register,
    },
    {
      path: '/',
      component: BasicLayout,
      children: [
        { path: '', name: 'home', component: Home },
        { path: 'archive', name: 'archive', component: Archive },
        { path: 'article/:id', name: 'article-detail', component: ArticleDetail },
        // 用户中心（需登录）
        {
          path: 'user',
          name: 'user-center',
          component: () => import('@/views/user/UserCenter.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'user/articles',
          name: 'my-articles',
          component: () => import('@/views/user/MyArticles.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'user/write',
          name: 'write-article',
          component: () => import('@/views/user/WriteArticle.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'user/messages',
          name: 'message-center',
          component: () => import('@/views/user/MessageCenter.vue'),
          meta: { requiresAuth: true },
        },
      ],
    },
  ],
})

// 全局前置守卫：需要登录的页面强制跳转登录页
router.beforeEach((to) => {
  if (to.meta?.requiresAuth) {
    const { loginUser } = useUserStore();
    if (!loginUser?.token) {
      message.warning('请先登录');
      return { path: '/login', query: { redirect: to.fullPath } };
    }
  }
  return true;
});

export default router
