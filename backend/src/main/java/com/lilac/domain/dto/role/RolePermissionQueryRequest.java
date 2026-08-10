package com.lilac.domain.dto.role;

import lombok.Data;

/**
 * 角色权限查询请求参数
 */

@Data
public class RolePermissionQueryRequest {
    /**
     * 角色ID
     */
    private Long roleId;
}
