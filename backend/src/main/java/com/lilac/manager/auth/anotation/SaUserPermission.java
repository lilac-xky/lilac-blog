package com.lilac.manager.auth.anotation;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import java.lang.annotation.*;

/**
 * 用户权限注解
 */
@SaCheckPermission(type = "user")
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE})
public @interface SaUserPermission {
    /**
     * 权限码
     */
    String[] value() default {};

    /**
     * 逻辑：OR
     */
    SaMode mode() default SaMode.OR;
}