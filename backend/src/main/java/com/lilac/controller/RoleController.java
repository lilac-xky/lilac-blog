package com.lilac.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilac.common.DeleteRequest;
import com.lilac.domain.dto.role.*;
import com.lilac.domain.entity.Permission;
import com.lilac.domain.entity.Role;
import com.lilac.domain.result.Result;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.service.RolePermissionService;
import com.lilac.service.RoleService;
import com.lilac.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色控制器
 */
@RestController
@RequestMapping("/role")
public class RoleController {

    @Resource
    private RoleService roleService;
    @Resource
    private RolePermissionService rolePermissionService;

    /**
     * 添加角色
     */
    @PostMapping("/add")
    public Result<Long> addRole(@RequestBody RoleAddRequest roleAddRequest) {
        ThrowUtils.throwIf(roleAddRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        Long role = roleService.addRole(roleAddRequest);
        return Result.success(role);
    }

    /**
     * 获取角色列表
     *
     * @param roleQueryRequest 角色查询参数
     * @return 角色列表
     */
    @PostMapping("/list")
    public Result<Page<Role>> getRoleList(@RequestBody RoleQueryRequest roleQueryRequest) {
        ThrowUtils.throwIf(roleQueryRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        int current = roleQueryRequest.getCurrent();
        int pageSize = roleQueryRequest.getPageSize();
        Page<Role> rolePage = roleService.page(new Page<>(current, pageSize), roleService.getQueryWrapper(roleQueryRequest));
        return Result.success(rolePage);
    }

    /**
     * 删除角色
     *
     * @param deleteRequest 删除角色参数
     * @return 删除结果
     */
    @PostMapping("/delete")
    public Result<Boolean> deleteRole(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        boolean delete = roleService.deleteRole(deleteRequest.getId());
        ThrowUtils.throwIf(!delete, HttpsCodeEnum.OPERATION_ERROR);
        return Result.success(true);
    }

    /**
     * 修改角色
     *
     * @param roleUpdateRequest 修改角色参数
     * @return 修改结果
     */
    @PostMapping("/update")
    public Result<Boolean> updateRole(@RequestBody RoleUpdateRequest roleUpdateRequest) {
        ThrowUtils.throwIf(roleUpdateRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        boolean update = roleService.update(roleUpdateRequest);
        ThrowUtils.throwIf(!update, HttpsCodeEnum.OPERATION_ERROR);
        return Result.success(true);
    }

    /**
     * 角色权限连接
     */
    @PostMapping("/addPermission")
    public Result<Boolean> addPermission(@RequestBody RoleAddPermissionsRequest roleAddPermissionsRequest) {
        ThrowUtils.throwIf(roleAddPermissionsRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        boolean addPermission = rolePermissionService.addPermission(roleAddPermissionsRequest);
        ThrowUtils.throwIf(!addPermission, HttpsCodeEnum.OPERATION_ERROR);
        return Result.success(true);
    }

    /**
     * 角色批量添加权限
     */
    @PostMapping("/batchAddPermissions")
    public Result<Boolean> batchAddPermissions(@RequestBody RoleBatchAddPermissionsRequest request) {
        ThrowUtils.throwIf(request == null, HttpsCodeEnum.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getRoleId() == null, HttpsCodeEnum.PARAMS_ERROR, "角色ID不能为空");
        ThrowUtils.throwIf(request.getPermissionIds() == null || request.getPermissionIds().isEmpty(),
                HttpsCodeEnum.PARAMS_ERROR, "权限ID列表不能为空");
        boolean result = rolePermissionService.batchAddPermissions(request);
        return Result.success(result);
    }

    /**
     * 角色移除权限
     */
    @PostMapping("/removePermission")
    public Result<Boolean> removePermission(@RequestBody RoleRemovePermissionRequest request) {
        ThrowUtils.throwIf(request == null, HttpsCodeEnum.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getRoleId() == null || request.getPermissionId() == null,
                HttpsCodeEnum.PARAMS_ERROR, "角色ID和权限ID不能为空");
        boolean result = rolePermissionService.removePermission(request);
        return Result.success(result);
    }

    /**
     * 角色批量移除权限
     */
    @PostMapping("/batchRemovePermissions")
    public Result<Boolean> batchRemovePermissions(@RequestBody RoleBatchRemovePermissionsRequest request) {
        ThrowUtils.throwIf(request == null, HttpsCodeEnum.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getRoleId() == null, HttpsCodeEnum.PARAMS_ERROR, "角色ID不能为空");
        ThrowUtils.throwIf(request.getPermissionIds() == null || request.getPermissionIds().isEmpty(),
                HttpsCodeEnum.PARAMS_ERROR, "权限ID列表不能为空");
        boolean result = rolePermissionService.batchRemovePermissions(request);
        return Result.success(result);
    }

    /**
     * 查询角色的权限列表
     */
    @PostMapping("/permissions")
    public Result<List<Permission>> getRolePermissions(@RequestBody RolePermissionQueryRequest request) {
        ThrowUtils.throwIf(request == null, HttpsCodeEnum.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getRoleId() == null, HttpsCodeEnum.PARAMS_ERROR, "角色ID不能为空");
        List<Permission> permissions = rolePermissionService.getPermissionsByRoleId(request.getRoleId());
        return Result.success(permissions);
    }
}
