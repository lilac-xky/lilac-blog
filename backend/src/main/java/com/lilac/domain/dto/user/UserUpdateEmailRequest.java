package com.lilac.domain.dto.user;

import lombok.Data;

/**
 * 修改用户邮箱请求
 */
@Data
public class UserUpdateEmailRequest {

    /**
     * 用d
     */
    private Long id;

    /**
     * 邮箱
     */
    private String email;
}
