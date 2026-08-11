import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

const STORAGE_KEY = 'lilac-blog-admin';

// 读取本地存储的用户信息
function readStorage(): API.LoginUserVO | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as API.LoginUserVO) : null;
  } catch {
    return null;
  }
}

// 用户信息存储
export const useUserStore = defineStore('user', () => {
  const loginUser = ref<API.LoginUserVO | null>(readStorage());

  /**
   * 设置登录用户信息并持久化到本地存储
   * @param user 登录用户信息
   */
  function setLoginUser(user: API.LoginUserVO) {
    loginUser.value = user;
    localStorage.setItem(STORAGE_KEY, JSON.stringify(user));
  }

  /**
   * 清除登录用户信息
   */
  function clearLoginUser() {
    loginUser.value = null;
    localStorage.removeItem(STORAGE_KEY);
  }

  /**
   * 更新用户权限列表
   * @param permissions 权限标识数组
   */
  function setPermissions(permissions: string[]) {
    if (loginUser.value) {
      loginUser.value.permissions = permissions;
      localStorage.setItem(STORAGE_KEY, JSON.stringify(loginUser.value));
    }
  }

  /**
   * 更新用户角色列表
   * @param roles 角色标识数组
   */
  function setRoles(roles: string[]) {
    if (loginUser.value) {
      loginUser.value.roles = roles;
      localStorage.setItem(STORAGE_KEY, JSON.stringify(loginUser.value));
    }
  }

  // 当前用户的权限列表（响应式）
  const permissions = computed(() => loginUser.value?.permissions || []);

  // 当前用户的角色列表（响应式）
  const roles = computed(() => loginUser.value?.roles || []);

  return {
    loginUser,
    permissions,
    roles,
    setLoginUser,
    clearLoginUser,
    setPermissions,
    setRoles,
  };
});
