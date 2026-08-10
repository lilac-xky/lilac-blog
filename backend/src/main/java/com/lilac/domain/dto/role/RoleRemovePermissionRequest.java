package com.lilac.domain.dto.role;

import lombok.Data;

/**
 * 角色移除权限请求参数
 */
@Data
public class RoleRemovePermissionRequest {
    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 权限ID
     */
    private Long permissionId;
}
