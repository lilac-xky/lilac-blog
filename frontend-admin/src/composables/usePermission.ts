/**
 * 权限检查 Composable
 * 提供响应式的权限检查能力
 */
import { computed } from 'vue';
import { hasPermission, hasRole, hasAnyPermission, hasAllPermissions } from '@/utils/permission';

/**
 * 使用权限检查的 Composable
 * @returns 包含各种权限检查方法的对象
 */
export function usePermission() {
  // 检查单个权限
  const checkPermission = (permissionKey: string) => {
    return computed(() => hasPermission(permissionKey));
  };

  // 检查角色
  const checkRole = (roleKey: string) => {
    return computed(() => hasRole(roleKey));
  };

  // 检查任意权限
  const checkAnyPermission = (permissionKeys: string[]) => {
    return computed(() => hasAnyPermission(permissionKeys));
  };

  // 检查所有权限
  const checkAllPermissions = (permissionKeys: string[]) => {
    return computed(() => hasAllPermissions(permissionKeys));
  };

  return {
    checkPermission,
    checkRole,
    checkAnyPermission,
    checkAllPermissions,
  };
}
