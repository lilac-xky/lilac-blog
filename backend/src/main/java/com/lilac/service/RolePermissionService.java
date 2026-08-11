package com.lilac.service;

import com.lilac.domain.dto.role.RoleAddPermissionsRequest;
import com.lilac.domain.dto.role.RoleBatchAddPermissionsRequest;
import com.lilac.domain.dto.role.RoleBatchRemovePermissionsRequest;
import com.lilac.domain.dto.role.RoleRemovePermissionRequest;
import com.lilac.domain.entity.Permission;
import com.lilac.domain.entity.RolePermission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* 角色权限表
*/
public interface RolePermissionService extends IService<RolePermission> {

    /**
     * 角色添加权限
     *
     * @param roleAddPermissionsRequest 角色添加权限参数
      * @return 添加结果
     */
    boolean addPermission(RoleAddPermissionsRequest roleAddPermissionsRequest);

    /**
     * 角色批量添加权限
     *
     * @param request 批量添加权限参数
     * @return 添加结果
     */
    boolean batchAddPermissions(RoleBatchAddPermissionsRequest request);

    /**
     * 角色移除权限
     *
     * @param request 移除权限参数
     * @return 移除结果
     */
    boolean removePermission(RoleRemovePermissionRequest request);

    /**
     * 角色批量移除权限
     *
     * @param request 批量移除权限参数
     * @return 移除结果
     */
    boolean batchRemovePermissions(RoleBatchRemovePermissionsRequest request);

    /**
     * 查询角色的所有权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> getPermissionsByRoleId(Long roleId);
}
