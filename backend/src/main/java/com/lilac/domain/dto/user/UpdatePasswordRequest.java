package com.lilac.domain.dto.user;

import lombok.Data;

/**
 * 修改密码请求
 */
@Data
public class UpdatePasswordRequest {

    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认密码
     */
    private String checkPassword;
}
