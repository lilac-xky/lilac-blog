package com.lilac.domain.dto.role;

import com.lilac.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色添加请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleQueryRequest extends PageRequest {
    /**
     * id
     */
    private Long id;

    /**
     * 角色key
     */
    private String roleKey;

    /**
     * 角色名
     */
    private String name;
}
