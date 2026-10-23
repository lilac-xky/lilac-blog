package com.lilac.domain.dto.user;

import lombok.Data;

/**
 * 更新邮箱请求参数
 */
@Data
public class UpdateEmailMethodRequest {

    /**
     * 用户 id
     */
    private Long id;

    /**
     * 身份验证通过后发放的一次性凭证
     */
    private String ticket;

    /**
     * 新邮箱
     */
    private String email;

    /**
     * 新邮箱验证码
     */
    private String code;
}
