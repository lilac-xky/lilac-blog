import { createRouter, createWebHistory } from 'vue-router'
import BasicLayout from '@/layouts/BasicLayout.vue'
import Home from '@/views/Home.vue'
import Archive from '@/views/Archive.vue'
import ArticleDetail from '@/views/ArticleDetail.vue'
import Login from '@/views/login/Login.vue'
import Register from '@/views/login/Register.vue'

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
      ],
    },
  ],
})

export default router
