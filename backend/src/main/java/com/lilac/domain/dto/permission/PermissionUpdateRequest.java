package com.lilac.domain.dto.permission;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 添加权限请求
 */
@Data
public class PermissionUpdateRequest implements Serializable {
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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}