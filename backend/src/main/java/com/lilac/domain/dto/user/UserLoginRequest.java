package com.lilac.domain.dto.user;

import lombok.Data;

/**
 * 登录请求
 */
@Data
public class UserLoginRequest {

    /**
     * 账号
     */
    private String account;

    /**
     * 密码
     */
    private String password;
}
