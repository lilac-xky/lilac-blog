package com.lilac.manager.auth.anotation;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import java.lang.annotation.*;

/**
 * 管理员权限注解
 */
@SaCheckPermission(type = "admin")
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE})
public @interface SaAdminPermission {
    /**
     * 权限值
     */
    String[] value() default {};

    /**
     * 验证模式
     */
    SaMode mode() default SaMode.OR;
}