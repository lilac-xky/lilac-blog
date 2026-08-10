package com.lilac.domain.dto.role;

import lombok.Data;

import java.util.List;

/**
 * 角色批量添加权限请求参数
 */
@Data
public class RoleBatchAddPermissionsRequest {
    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 权限ID列表
     */
    private List<Long> permissionIds;
}
