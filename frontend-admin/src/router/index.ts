import { createRouter, createWebHistory } from 'vue-router'
import BasicLayout from '../layouts/BasicLayout.vue'
import Home from '../views/Home.vue'
import Login from '../views/login/Login.vue'
import UserManagement from '../views/user/UserManagement.vue'
import { useUserStore } from '@/stores/user'
import { currentUser } from '@/api/adminController'

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

let tokenVerified = false;

// 全局前置守卫：检查访问权限，首次导航时向后端验证 token 有效性
router.beforeEach(async (to, _from) => {
  if (to.meta?.public) return true;
  const { loginUser, clearLoginUser } = useUserStore();
  if (!loginUser?.token) {
    return { path: '/login', query: { redirect: to.fullPath } };
  }
  if (!tokenVerified) {
    tokenVerified = true;
    try {
      await currentUser({ silentError: true });
    } catch {
      clearLoginUser();
      return { path: '/login', query: { redirect: to.fullPath } };
    }
  }
  return true;
});

export default router
