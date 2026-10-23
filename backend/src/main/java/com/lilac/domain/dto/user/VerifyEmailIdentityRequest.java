package com.lilac.domain.dto.user;

import lombok.Data;

/**
 * 邮箱身份验证请求参数
 */
@Data
public class VerifyEmailIdentityRequest {

    /**
     * 用户 id
     */
    private Long id;

    /**
     * 身份验证方式：email 表示原邮箱验证码，password 表示当前密码
     */
    private String method;

    /**
     * 原邮箱验证码，method 为 email 时必填
     */
    private String originalCode;

    /**
     * 当前密码，method 为 password 时必填
     */
    private String password;
}
