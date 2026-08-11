/**
 * v-permission 权限指令
 * 用法：v-permission="'article:add'" 或 v-permission="['article:add', 'article:edit']"
 * 修饰符：v-permission.all="[...]" 要求拥有所有权限
 */
import type { Directive } from 'vue';
import { hasPermission, hasAnyPermission, hasAllPermissions } from '@/utils/permission';

export const permissionDirective: Directive = {
  mounted(el, binding) {
    const value = binding.value;
    if (!value) return;

    // 检查权限
    let hasAuth = false;
    if (typeof value === 'string') {
      // 单个权限检查
      hasAuth = hasPermission(value);
    } else if (Array.isArray(value)) {
      // 数组权限检查，支持 .all 修饰符
      hasAuth = binding.modifiers.all
        ? hasAllPermissions(value)
        : hasAnyPermission(value);
    }

    // 无权限则移除元素
    if (!hasAuth && el.parentNode) {
      el.parentNode.removeChild(el);
    }
  },
};
