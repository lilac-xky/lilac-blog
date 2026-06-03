package com.lilac.domain.dto.user;

import lombok.Data;

/**
 * 管理员重置密码请求
 */
@Data
public class AdminResetPasswordRequest {

    /**
     * id
     */
    private Long id;

    /**
     * 新密码
     */
    private String newPassword;
}
