import { createRouter, createWebHistory } from 'vue-router'
import BasicLayout from '../layouts/BasicLayout.vue'
import Home from '../views/Home.vue'
import Login from '../views/login/Login.vue'
import UserManagement from '../views/user/UserManagement.vue'
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
        {
          path: 'user/manage',
          name: 'userManage',
          component: UserManagement,
        },
      ],
    },
  ],
})

// 全局前置守卫：检查访问权限
router.beforeEach((to, _from) => {
  if (to.meta?.public) return true;
  const { loginUser } = useUserStore();
  if (!loginUser?.token) {
    return { path: '/login', query: { redirect: to.fullPath } };
  }
  return true;
});

export default router
