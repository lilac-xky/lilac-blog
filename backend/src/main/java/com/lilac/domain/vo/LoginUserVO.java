package com.lilac.domain.vo;

import lombok.Data;


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
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String username;

    /**
     * 头像
     */
    private String avater;

    /**
     * 角色(admin管理,user普通用户)
     */
    private String role;

    /**
     * 1正常，0异常
     */
    private Integer status;

    /**
     * token
     */
    private String token;
}
