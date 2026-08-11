package com.lilac.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilac.common.DeleteRequest;
import com.lilac.domain.dto.permission.PermissionAddRequest;
import com.lilac.domain.dto.permission.PermissionQueryRequest;
import com.lilac.domain.dto.permission.PermissionUpdateRequest;
import com.lilac.domain.entity.Permission;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.exception.BusinessException;
import com.lilac.service.PermissionService;
import com.lilac.mapper.PermissionMapper;
import com.lilac.utils.ThrowUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
* 权限服务实现
*/
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService{

    /**
     * 添加权限
     *
     * @param permissionAddRequest 添加权限请求
     * @return 添加的权限ID
     */
    @Override
    public Long addPermission(PermissionAddRequest permissionAddRequest) {
        // 权限唯一
        String permissionKey = permissionAddRequest.getPermissionKey();
        Long count = this.baseMapper.selectCount(new LambdaQueryWrapper<Permission>().eq(Permission::getPermissionKey, permissionKey));
        if (count > 0) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "权限已存在");
        }
        Permission permission = new Permission();
        BeanUtils.copyProperties(permissionAddRequest, permission);
        boolean result = this.save(permission);
        ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR, "添加失败");
        return permission.getId();
    }

    /**
     * 删除权限
     *
     * @param deleteRequest 删除权限请求
     * @return 是否删除成功
     */
    @Override
    public boolean deletePermission(DeleteRequest deleteRequest) {
        boolean result = this.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR, "删除失败");
        return true;
    }

    /**
     * 获取查询条件
     *
     * @param permissionQueryRequest 查询条件
     * @return 查询条件
     */
    @Override
    public Wrapper<Permission> getQueryWrapper(PermissionQueryRequest permissionQueryRequest) {
        LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
        Long id = permissionQueryRequest.getId();
        String permissionKey = permissionQueryRequest.getPermissionKey();
        String name = permissionQueryRequest.getName();
        // 构建查询
        queryWrapper.eq(ObjUtil.isNotEmpty(id), Permission::getId, id)
                .eq(StrUtil.isNotEmpty(permissionKey), Permission::getPermissionKey, permissionKey)
                .eq(StrUtil.isNotEmpty(name), Permission::getName, name);
        return queryWrapper;
    }

    /**
     * 更新权限
     *
     * @param permissionUpdateRequest 更新权限请求
     * @return 是否更新成功
     */
    @Override
    public boolean update(PermissionUpdateRequest permissionUpdateRequest) {
        // key唯一
        String permissionKey = permissionUpdateRequest.getPermissionKey();
        Long count = this.baseMapper.selectCount(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getPermissionKey, permissionKey)
                .ne(Permission::getId, permissionUpdateRequest.getId()));
        if (count > 0) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "权限已存在");
        }
        Permission permission = new Permission();
        BeanUtils.copyProperties(permissionUpdateRequest, permission);
        return this.updateById(permission);
    }
}




