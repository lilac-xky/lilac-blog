package com.lilac.domain.dto.role;

import lombok.Data;

/**
 * 角色添加权限请求
 */
@Data
public class RoleAddPermissionsRequest {
    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 权限ID
     */
    private Long permissionId;
}
