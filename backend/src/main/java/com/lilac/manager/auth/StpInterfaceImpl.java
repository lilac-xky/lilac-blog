package com.lilac.manager.auth;

import cn.dev33.satoken.stp.StpInterface;
import com.lilac.constant.UserConstant;
import com.lilac.domain.entity.Permission;
import com.lilac.domain.entity.Role;
import com.lilac.domain.entity.RolePermission;
import com.lilac.domain.entity.User;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.exception.BusinessException;
import com.lilac.service.PermissionService;
import com.lilac.service.RolePermissionService;
import com.lilac.service.RoleService;
import com.lilac.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义权限验证接口扩展
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private UserService userService;
    @Resource
    private RoleService roleService;
    @Resource
    private PermissionService permissionService;
    @Resource
    private RolePermissionService rolePermissionService;

    /**
     * 获取权限列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 查用户
        User user = userService.getById(Long.valueOf(loginId.toString()));
        if(user == null || user.getRoleId() == null) {
            return new ArrayList<>();
        }
        // 查角色
        Role role = roleService.getById(user.getRoleId());
        if(role == null) {
            return new ArrayList<>();
        }
        // 查角色关联权限
        List<Permission> permissionList = rolePermissionService.getPermissionsByRoleId(role.getId());
        if(permissionList == null || permissionList.isEmpty()){
            return new ArrayList<>();
        }
        // 返回权限标识列表
        return permissionList.stream().map(Permission::getPermissionKey).collect(Collectors.toList());
    }

    /**
     * 获取角色列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        User user = userService.getById(Long.valueOf(loginId.toString()));
        List<String> roles = new ArrayList<>();
        if (user != null && user.getRoleId() != null) {
            Role role = roleService.getById(user.getRoleId());
            roles.add(role.getRoleKey());
        }
        return roles;
    }
}