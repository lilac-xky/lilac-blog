/**
 * 权限工具函数
 * 提供统一的权限检查能力，支持权限标识和角色标识的验证
 */
import { useUserStore } from '@/stores/user';

/**
 * 检查当前用户是否拥有指定权限
 * @param permissionKey 权限标识，如 'article:add'
 * @returns 是否拥有该权限
 */
export function hasPermission(permissionKey: string): boolean {
  const userStore = useUserStore();
  const permissions = userStore.permissions;

  // 超级管理员权限标识为 '*'，拥有所有权限
  if (permissions.includes('*')) {
    return true;
  }

  return permissions.includes(permissionKey);
}

/**
 * 检查当前用户是否拥有指定角色
 * @param roleKey 角色标识，如 'admin'
 * @returns 是否拥有该角色
 */
export function hasRole(roleKey: string): boolean {
  const userStore = useUserStore();
  const roles = userStore.roles;
  return roles.includes(roleKey);
}

/**
 * 检查当前用户是否拥有任意一个权限（OR 逻辑）
 * @param permissionKeys 权限标识数组
 * @returns 是否拥有任意权限
 */
export function hasAnyPermission(permissionKeys: string[]): boolean {
  const userStore = useUserStore();
  const permissions = userStore.permissions;

  // 超级管理员权限标识为 '*'，拥有所有权限
  if (permissions.includes('*')) {
    return true;
  }

  return permissionKeys.some(key => permissions.includes(key));
}

/**
 * 检查当前用户是否拥有所有权限（AND 逻辑）
 * @param permissionKeys 权限标识数组
 * @returns 是否拥有所有权限
 */
export function hasAllPermissions(permissionKeys: string[]): boolean {
  const userStore = useUserStore();
  const permissions = userStore.permissions;

  // 超级管理员权限标识为 '*'，拥有所有权限
  if (permissions.includes('*')) {
    return true;
  }

  return permissionKeys.every(key => permissions.includes(key));
}
