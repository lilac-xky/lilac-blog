package com.lilac.domain.dto.user;

import lombok.Data;

/**
 * 登录请求
 */
@Data
public class UserRegisterRequest {

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String password;

    /**
     * 确认密码
     */
    private String checkPassword;
}
