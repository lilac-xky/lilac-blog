import { createRouter, createWebHistory } from 'vue-router'
import BasicLayout from '../layouts/BasicLayout.vue'
import Home from '../views/Home.vue'
import Login from '../views/login/Login.vue'
import UserManagement from '../views/user/UserManagement.vue'
import { useUserStore } from '@/stores/user'
import { currentUser } from '@/api/adminController'
import { hasPermission } from '@/utils/permission'
import { message } from 'ant-design-vue'

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
          meta: { permission: 'user:list' }, // 需要用户列表权限
        },
        {
          path: 'article/manage',
          name: 'articleManage',
          component: () => import('@/views/article/ArticleManagement.vue'),
          meta: { permission: 'article:list' }, // 需要文章列表权限
        },
        {
          path: 'article/review',
          name: 'articleReview',
          component: () => import('@/views/article/ArticleReview.vue'),
          meta: { permission: 'article:review' }, // 需要文章审核权限
        },
        {
          path: 'write-blog',
          name: 'writeBlog',
          component: () => import('@/views/article/WriteBlog.vue'),
          meta: { permission: 'article:add' }, // 需要文章添加权限
        },
        {
          path: 'spark/manage',
          name: 'sparkManage',
          component: () => import('@/views/spark/SparkManagement.vue'),
          meta: { permission: 'spark:list' }, // 需要灵感列表权限
        },
        {
          path: 'blog/category',
          name: 'categoryManage',
          component: () => import('@/views/category/CategoryManagement.vue'),
          meta: { permission: 'category:list' }, // 需要分类列表权限
        },
        {
          path: 'blog/tag',
          name: 'tagManage',
          component: () => import('@/views/tag/TagManagement.vue'),
          meta: { permission: 'tag:list' }, // 需要标签列表权限
        },
        {
          path: 'role',
          name: 'roleManage',
          component: () => import('@/views/auth/RoleManagement.vue'),
          meta: { permission: 'role:list' }, // 需要角色列表权限
        },
        {
          path: 'permission',
          name: 'permissionManage',
          component: () => import('@/views/auth/PermissionManagement.vue'),
          meta: { permission: 'permission:list' }, // 需要权限列表权限
        }
      ],
    },
  ],
})

let tokenVerified = false;

/**
 * 全局前置守卫：检查访问权限和路由权限
 * 1. 检查用户是否登录
 * 2. 首次导航时向后端验证 token 有效性
 * 3. 检查路由权限（基于 meta.permission）
 */
router.beforeEach(async (to, _from) => {
  // 公开路由直接放行
  if (to.meta?.public) return true;

  const { loginUser, clearLoginUser } = useUserStore();

  // 未登录则跳转登录页
  if (!loginUser?.token) {
    return { path: '/login', query: { redirect: to.fullPath } };
  }

  // 首次导航时验证 token 有效性
  if (!tokenVerified) {
    tokenVerified = true;
    try {
      await currentUser({ silentError: true });
    } catch {
      clearLoginUser();
      return { path: '/login', query: { redirect: to.fullPath } };
    }
  }

  // 检查路由权限
  const permission = to.meta?.permission as string | undefined;
  if (permission) {
    // 检查是否拥有该权限（包含超级管理员 '*' 权限的处理）
    if (!hasPermission(permission)) {
      message.warning('您没有权限访问该页面');
      // 返回首页或停留在当前页
      return _from.path !== '/' ? false : { path: '/' };
    }
  }

  return true;
});

export default router
