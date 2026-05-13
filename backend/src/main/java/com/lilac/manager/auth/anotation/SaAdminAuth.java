package com.lilac.manager.auth.anotation;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 管理员权限注解
 */
@SaCheckRole(type = "admin")
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE})
public @interface SaAdminAuth {
    // 允许传入多个角色，默认只要是 admin 角色就行
    String[] value() default {"admin"};

    // 默认为 OR 逻辑，即只要是 admin 角色就行
    SaMode mode() default SaMode.OR;
}