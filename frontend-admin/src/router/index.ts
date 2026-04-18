import { createRouter, createWebHistory } from 'vue-router'
import BasicLayout from '../layouts/BasicLayout.vue'
import Home from '../views/Home.vue'
import Login from '../views/login/Login.vue'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: Login,
      meta: { public: true },
    },
    {
      path: '/',
      component: BasicLayout,
      children: [
        {
          path: '',
          name: 'home',
          component: Home,
        },
      ],
    },
  ],
})

// 全局前置守卫：检查访问权限
router.beforeEach((to, _from, next) => {
  if (to.meta?.public) return next()
  const { loginUser } = useUserStore()
  if (!loginUser?.token) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }
  next()
})

export default router
