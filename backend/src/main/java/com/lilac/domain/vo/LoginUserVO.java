package com.lilac.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 登录用户视图
 */
@Data
public class LoginUserVO {
    private Long id;

    /**
     * 用户名
     */
    private String userAccount;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 昵称
     */
    private String username;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 角色(admin管理,user普通用户)
     */
    private String role;

    /**
     * 角色id
     */
    private Long roleId;

    /**
     * 1正常，0异常
     */
    private Integer status;

    /**
     * token
     */
    private String token;

    /**
     * 权限列表
     */
    private List<String> permissions;

    /**
     * 角色列表
     */
    private List<String> roles;
}
