package com.lilac.constant;

/**
 * 用户常量
 */
public interface UserConstant {

    String USER_LOGIN_STATE = "user_login";

    String ADMIN_LOGIN_STATE = "admin_login";

    String USER_DEFAULT_NAME = "用户";

    String USER_DEFAULT_ROLE = "user";

    String ADMIN_ROLE = "admin";

    /**
     * 用户注册验证码缓存key
     */
    String USER_REGISTER_CODE_KEY = "user:register:code:";

    /**
     * 用户注册限制缓存key
     */
    String USER_REGISTER_LIMIT_KEY = "user:register:limit:";

    /**
     * 用户注册验证码缓存时间
     */
    long USER_REGISTER_CODE_TTL = 5L;

    /**
     * 用户注册错误次数缓存key
     */
    String USER_REGISTER_CODE_ERROR_KEY = "user:register:error:";
}
