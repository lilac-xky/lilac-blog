package com.lilac.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.lilac.common.DeleteRequest;
import com.lilac.domain.dto.permission.PermissionAddRequest;
import com.lilac.domain.dto.permission.PermissionQueryRequest;
import com.lilac.domain.dto.permission.PermissionUpdateRequest;
import com.lilac.domain.entity.Permission;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* 权限服务
*/
public interface PermissionService extends IService<Permission> {

    /**
     * 添加权限
     *
     * @param permissionAddRequest 添加权限请求
     * @return 添加的权限ID
     */
    Long addPermission(PermissionAddRequest permissionAddRequest);

    /**
     * 获取查询条件
     *
     * @param permissionQueryRequest 查询条件
     * @return 查询条件
     */
    Wrapper<Permission> getQueryWrapper(PermissionQueryRequest permissionQueryRequest);

    /**
     * 删除权限
     *
     * @param deleteRequest 删除权限请求
     * @return 是否删除成功
     */
    boolean deletePermission(DeleteRequest deleteRequest);

    /**
     * 更新权限
     *
     * @param permissionUpdateRequest 更新权限请求
     * @return 是否更新成功
     */
    boolean update(PermissionUpdateRequest permissionUpdateRequest);
}
