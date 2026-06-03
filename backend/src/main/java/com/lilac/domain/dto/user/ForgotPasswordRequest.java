package com.lilac.domain.dto.user;

import lombok.Data;

/**
 * 重置密码请求
 */
@Data
public class ForgotPasswordRequest {

    /**
     * 邮箱
     */
    private String email;

    /**
     * 验证码
     */
    private String code;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认密码
     */
    private String checkPassword;
}
