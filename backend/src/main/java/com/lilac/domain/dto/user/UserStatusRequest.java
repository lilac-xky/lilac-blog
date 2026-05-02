package com.lilac.domain.dto.user;

import lombok.Data;

/**
 * 用户状态请求
 */
@Data
public class UserStatusRequest {

    private Long id;
    // 1正常，0异常
    private Integer status;
}
