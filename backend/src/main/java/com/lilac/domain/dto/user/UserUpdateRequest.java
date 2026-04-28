package com.lilac.domain.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 修改用户请求
 */
@Data
public class UserUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
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
     * 1正常，0异常
     */
    private Integer status;
}
