package com.lilac.manager.auth.anotation;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import java.lang.annotation.*;
import org.springframework.core.annotation.AliasFor;

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
    @AliasFor(annotation = SaCheckPermission.class, attribute = "value")
    String[] value() default {};

    /**
     * 逻辑：OR
     */
    @AliasFor(annotation = SaCheckPermission.class, attribute = "mode")
    SaMode mode() default SaMode.OR;
}
