package com.lilac.domain.dto.permission;

import com.lilac.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 添加权限请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PermissionQueryRequest extends PageRequest {
    /**
     * id
     */
    private Long id;

    /**
     * 权限key
     */
    private String permissionKey;

    /**
     * 权限描述
     */
    private String name;
}