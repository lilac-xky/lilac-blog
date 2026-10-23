package com.lilac.domain.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 修改用户请求
 */
@Data
public class UserEditRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 昵称
     */
    private String username;

    /**
     * 头像
     */
    private String avatar;
}
