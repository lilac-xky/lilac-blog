package com.lilac.domain.dto.role;

import lombok.Data;

/**
 * 角色添加请求
 */
@Data
public class RoleAddRequest {

    /**
     * 角色key
     */
    private String roleKey;

    /**
     * 角色名
     */
    private String name;

    /**
     * 角色描述
     */
    private String description;
}
