package com.lilac.manager.auth;

import cn.dev33.satoken.stp.StpInterface;
import com.lilac.constant.UserConstant;
import com.lilac.domain.entity.User;
import com.lilac.service.impl.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义权限验证接口扩展
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private UserService userService;

    /**
     * 获取权限列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 获取权限列表
        List<String> permissions = new ArrayList<>();
        // 管理端体系
        if ("admin".equals(loginType)) {
            User user = userService.getById(Long.valueOf(loginId.toString()));
            if (UserConstant.ADMIN_ROLE.equals(user.getRole())) {
                permissions.add("*");
            } else {
                // 如果是其他类型的管理员（如审核员），增加权限
                permissions.add("article:review");
            }
        }

        // 用户端体系
        else if ("user".equals(loginType)) {
            permissions.add("article:submit");
            permissions.add("comment:add");
        }
        return permissions;
    }

    /**
     * 获取角色列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 根据 loginType (admin/user) 获取对应的角色列表
        User user = userService.getById(Long.valueOf(loginId.toString()));
        List<String> roles = new ArrayList<>();
        if (user != null) {
            roles.add(user.getRole());
        }
        return roles;
    }
}