package com.lilac.manager.auth.anotation;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.strategy.SaAnnotationStrategy;
import com.lilac.manager.auth.StpKit;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer{

    /**
     * 注册 Sa-Token 拦截器，打开注解式鉴权功能
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 管理端拦截器：只校验 /admin/** 的路由
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 匹配所有 admin 路径，排除登录注册
            SaRouter.match("/admin/**")
                    .notMatch("/admin/login", "/admin/logout")
                    .check(r -> StpKit.ADMIN.checkLogin());
        })).addPathPatterns("/admin/**");
        // 2. 用户端拦截器：只校验 /user/**
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 匹配所有 user 路径，排除登录注册
            SaRouter.match("/user/**")
                    .notMatch("/user/login", "/user/register", "/user/logout")
                    .check(r -> StpKit.USER.checkLogin());
        })).addPathPatterns("/user/**");
    }

    @PostConstruct
    public void rewriteSaStrategy() {
        // 重写Sa-Token的注解处理器，增加注解合并功能 
        SaAnnotationStrategy.instance.getAnnotation = (element, annotationClass) -> {
            return AnnotatedElementUtils.getMergedAnnotation(element, annotationClass);
        };
    }
}