/**
 * 角色辅助工具
 * 提供角色 ID 和角色信息的映射，支持缓存提升性能
 */
import { getRoleList } from '@/api/roleController';

// 角色缓存：roleId -> Role
const roleCache = new Map<string, API.Role>();
let cachePromise: Promise<void> | null = null;

/**
 * 加载所有角色到缓存
 */
async function loadRoleCache(): Promise<void> {
  if (cachePromise) return cachePromise;

  cachePromise = getRoleList({ current: 1, pageSize: 100, sortOrder: 'descend' })
    .then(res => {
      const roles = res.data?.data?.records || [];
      roleCache.clear();
      roles.forEach(role => {
        if (role.id) roleCache.set(String(role.id), role);
      });
    })
    .catch(() => {
      cachePromise = null; // 失败后允许重试
    });

  return cachePromise;
}

/**
 * 根据角色 ID 获取角色信息
 * @param roleId 角色 ID（支持 string 或 number）
 * @returns 角色信息，未找到返回 undefined
 */
export async function getRoleById(roleId: string | number | undefined): Promise<API.Role | undefined> {
  if (!roleId) return undefined;
  await loadRoleCache();
  return roleCache.get(String(roleId));
}

/**
 * 根据角色 ID 获取角色名
 * @param roleId 角色 ID（支持 string 或 number）
 * @returns 角色名，未找到返回 '未知角色'
 */
export async function getRoleName(roleId: string | number | undefined): Promise<string> {
  const role = await getRoleById(roleId);
  return role?.name || '未知角色';
}

/**
 * 获取所有角色列表（用于下拉选择）
 * @returns 角色列表
 */
export async function getAllRoles(): Promise<API.Role[]> {
  await loadRoleCache();
  return Array.from(roleCache.values());
}

/**
 * 清除角色缓存（在角色管理页修改后调用）
 */
export function clearRoleCache(): void {
  roleCache.clear();
  cachePromise = null;
}
