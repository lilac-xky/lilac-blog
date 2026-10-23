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

    String USER_ROLE = "user";

    // 超级管理员角色标识
    String SUPER_ROLE = "super-admin";

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

    /**
     * 用户重置密码验证码缓存key
     */
    String USER_RESET_CODE_KEY = "user:reset:code:";

    /**
     * 用户重置密码限制缓存key
     */
    String USER_RESET_LIMIT_KEY = "user:reset:limit:";

    /**
     * 用户重置密码验证码缓存时间
     */
    long USER_RESET_CODE_TTL = 5L;

    /**
     * 用户重置密码错误次数缓存key
     */
    String USER_RESET_CODE_ERROR_KEY = "user:reset:error:";

    /**
     * 用户修改邮箱限制缓存key
     */
    String USER_CHANGE_EMAIL_LIMIT_KEY = "user:change:email:limit:";

    /**
     * 用户修改邮箱验证码缓存key
     */
    String USER_CHANGE_EMAIL_CODE_KEY = "user:change:email:code:";

    /**
     * 用户修改邮箱验证码缓存时间
     */
    long USER_CHANGE_EMAIL_CODE_TTL = 5L;

    /**
     * 用户修改邮箱错误次数缓存key
     */
    String USER_CHANGE_EMAIL_CODE_ERROR_KEY = "user:change:email:error:";

    /**
     * 用户修改邮箱错误次数缓存时间
     */
    long USER_CHANGE_EMAIL_CODE_ERROR_TTL = 5L;

    /**
     * 用户修改邮箱-原邮箱验证码缓存key
     */
    String USER_CHANGE_EMAIL_OLD_CODE_KEY = "user:change:email:old:code:";

    /**
     * 用户修改邮箱-原邮箱验证码错误次数缓存key
     */
    String USER_CHANGE_EMAIL_OLD_CODE_ERROR_KEY = "user:change:email:old:error:";

    /**
     * 用户修改邮箱-原邮箱验证码发送限流缓存key
     */
    String USER_CHANGE_EMAIL_OLD_LIMIT_KEY = "user:change:email:old:limit:";

    /**
     * 用户修改邮箱-原邮箱验证码缓存时间
     */
    long USER_CHANGE_EMAIL_OLD_CODE_TTL = 5L;

    /**
     * 用户修改邮箱-身份验证通过凭证缓存key
     */
    String USER_CHANGE_EMAIL_TICKET_KEY = "user:change:email:ticket:";

    /**
     * 用户修改邮箱-身份验证通过凭证有效期（分钟）
     */
    long USER_CHANGE_EMAIL_TICKET_TTL = 5L;
}
