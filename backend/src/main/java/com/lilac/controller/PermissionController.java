package com.lilac.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilac.common.DeleteRequest;
import com.lilac.domain.dto.permission.PermissionAddRequest;
import com.lilac.domain.dto.permission.PermissionQueryRequest;
import com.lilac.domain.dto.permission.PermissionUpdateRequest;
import com.lilac.domain.entity.Permission;
import com.lilac.domain.result.Result;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.service.PermissionService;
import com.lilac.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 权限控制
 */
@RestController
@RequestMapping("/permission")
public class PermissionController {

    @Resource
    private PermissionService permissionService;

    /**
     * 添加权限
     */
    @PostMapping("/add")
    public Result<Long> addPermission(@RequestBody PermissionAddRequest permissionAddRequest) {
        ThrowUtils.throwIf(permissionAddRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        Long permissionId = permissionService.addPermission(permissionAddRequest);
        return Result.success(permissionId);
    }

    /**
     * 获取权限列表
     */
    @PostMapping("/list")
    public Result<Page<Permission>> getPermissionList(@RequestBody PermissionQueryRequest permissionQueryRequest) {
        ThrowUtils.throwIf(permissionQueryRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        int current = permissionQueryRequest.getCurrent();
        int pageSize = permissionQueryRequest.getPageSize();
        Page<Permission> permissionPage = permissionService.page(new Page<>(current, pageSize),
                permissionService.getQueryWrapper(permissionQueryRequest));
        return Result.success(permissionPage);
    }

    /**
     * 删除权限
     */
    @PostMapping("/delete")
    public Result<Boolean> deletePermission(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        boolean delete = permissionService.deletePermission(deleteRequest);
        ThrowUtils.throwIf(!delete, HttpsCodeEnum.OPERATION_ERROR);
        return Result.success(true);
    }

    /**
     * 更新权限
     */
    @PostMapping("/update")
    public Result<Boolean> updatePermission(@RequestBody PermissionUpdateRequest permissionUpdateRequest) {
        ThrowUtils.throwIf(permissionUpdateRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        boolean update = permissionService.update(permissionUpdateRequest);
        ThrowUtils.throwIf(!update, HttpsCodeEnum.OPERATION_ERROR);
        return Result.success(true);
    }
}
