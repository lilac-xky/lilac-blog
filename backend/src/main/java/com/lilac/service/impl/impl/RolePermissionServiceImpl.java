package com.lilac.service.impl.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilac.common.DeleteRequest;
import com.lilac.domain.dto.role.RoleAddPermissionsRequest;
import com.lilac.domain.dto.role.RoleBatchAddPermissionsRequest;
import com.lilac.domain.dto.role.RoleBatchRemovePermissionsRequest;
import com.lilac.domain.dto.role.RoleRemovePermissionRequest;
import com.lilac.domain.entity.Permission;
import com.lilac.domain.entity.RolePermission;
import com.lilac.domain.entity.Tag;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.service.impl.RolePermissionService;
import com.lilac.mapper.RolePermissionMapper;
import com.lilac.utils.ThrowUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
* 角色权限表
*/
@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService{

    /**
     * 角色添加权限
     *
     * @param roleAddPermissionsRequest 角色添加权限参数
     * @return 添加结果
     */
    @Override
    public boolean addPermission(RoleAddPermissionsRequest roleAddPermissionsRequest) {
        RolePermission rolePermission = new RolePermission();
        BeanUtils.copyProperties(roleAddPermissionsRequest, rolePermission);
        boolean save = save(rolePermission);
        ThrowUtils.throwIf(!save, HttpsCodeEnum.OPERATION_ERROR);
        return true;
    }

    /**
     * 角色批量添加权限
     *
     * @param request 批量添加权限参数
     * @return 添加结果
     */
    @Override
    @Transactional
    public boolean batchAddPermissions(RoleBatchAddPermissionsRequest request) {
        // 获取角色ID和权限ID列表
        Long roleId = request.getRoleId();
        List<Long> permissionIds = request.getPermissionIds();
        ThrowUtils.throwIf(roleId == null, HttpsCodeEnum.PARAMS_ERROR, "角色ID不能为空");
        ThrowUtils.throwIf(permissionIds == null || permissionIds.isEmpty(), HttpsCodeEnum.PARAMS_ERROR, "权限ID列表不能为空");

        // 查询已有的权限
        LambdaQueryWrapper<RolePermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePermission::getRoleId, roleId);
        queryWrapper.in(RolePermission::getPermissionId, permissionIds);
        List<RolePermission> existingList = this.list(queryWrapper);
        Set<Long> existingPermissionIds = existingList.stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toSet());

        // 构建待添加的权限列表
        List<RolePermission> toAddList = new ArrayList<>();
        for (Long permissionId : permissionIds) {
            if (!existingPermissionIds.contains(permissionId)) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                toAddList.add(rolePermission);
            }
        }

        // 批量添加权限
        if (!toAddList.isEmpty()) {
            boolean result = this.saveBatch(toAddList);
            ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR, "批量添加失败");
        }
        return true;
    }

    /**
     * 角色移除权限
     *
     * @param request 移除权限参数
     * @return 移除结果
     */
    @Override
    public boolean removePermission(RoleRemovePermissionRequest request) {
        // 获取角色ID和权限ID
        Long roleId = request.getRoleId();
        Long permissionId = request.getPermissionId();
        ThrowUtils.throwIf(roleId == null || permissionId == null, HttpsCodeEnum.PARAMS_ERROR, "角色ID和权限ID不能为空");

        LambdaQueryWrapper<RolePermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePermission::getRoleId, roleId);
        queryWrapper.eq(RolePermission::getPermissionId, permissionId);
        boolean result = this.remove(queryWrapper);
        ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR, "删除失败");
        return true;
    }

    /**
     * 角色批量移除权限
     *
     * @param request 批量移除权限参数
     * @return 移除结果
     */
    @Override
    @Transactional
    public boolean batchRemovePermissions(RoleBatchRemovePermissionsRequest request) {
        // 获取角色ID和权限ID列表
        Long roleId = request.getRoleId();
        List<Long> permissionIds = request.getPermissionIds();
        ThrowUtils.throwIf(roleId == null, HttpsCodeEnum.PARAMS_ERROR, "角色ID不能为空");
        ThrowUtils.throwIf(permissionIds == null || permissionIds.isEmpty(), HttpsCodeEnum.PARAMS_ERROR, "权限ID列表不能为空");

        // 查询已有的权限
        LambdaQueryWrapper<RolePermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePermission::getRoleId, roleId);
        queryWrapper.in(RolePermission::getPermissionId, permissionIds);
        boolean result = this.remove(queryWrapper);
        ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR, "批量删除失败");
        return true;
    }

    /**
     * 查询角色的所有权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Override
    public List<Permission> getPermissionsByRoleId(Long roleId) {
        // 获取角色ID
        ThrowUtils.throwIf(roleId == null, HttpsCodeEnum.PARAMS_ERROR, "角色ID不能为空");
        return this.baseMapper.selectPermissionsByRoleId(roleId);
    }
}
