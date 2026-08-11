package com.lilac.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 角色VO
 */
@Data
public class RoleVO implements Serializable {
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

    /**
     * 登录类型
     */
    private String loginType;

    /**
     * 角色描述
     */
    private String description;


    private static final long serialVersionUID = 1L;
}