package com.lilac.manager.auth.anotation;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.strategy.SaAnnotationStrategy;
import cn.dev33.satoken.strategy.SaStrategy;
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
            // 过滤 OPTIONS
            if (SaHolder.getRequest().getMethod().equals("OPTIONS")) return;
            // 对于 /admin/** 路径，强制校验 admin 登录
            SaRouter.match("/admin/**")
                    .notMatch("/admin/login", "/admin/logout")
                    .check(r -> StpKit.ADMIN.checkLogin());
            // 对于 /user/** 路径，强制校验 user 登录
            SaRouter.match("/user/**")
                    .notMatch("/user/login", "/user/register", "/user/logout", "/user/sendCode")
                    .notMatch("file/upload")
                    .check(r -> StpKit.USER.checkLogin());
        })).addPathPatterns("/**");
    }

    @PostConstruct
    public void rewriteSaStrategy() {
        // 重写Sa-Token的注解处理器，增加注解合并功能 
        SaAnnotationStrategy.instance.getAnnotation = (element, annotationClass) -> {
            return AnnotatedElementUtils.getMergedAnnotation(element, annotationClass);
        };
        // 重写权限匹配算法，支持管理员的 "*" 通配符
        SaStrategy.instance.hasElement = (list, element) -> {
            // 如果权限列表中包含 *，则代表拥有所有权限
            if (list.contains("*")) {
                return true;
            }
            // 否则执行标准匹配
            return list.contains(element);
        };
    }
}