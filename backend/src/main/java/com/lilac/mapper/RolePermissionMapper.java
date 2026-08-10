package com.lilac.mapper;

import com.lilac.domain.entity.Permission;
import com.lilac.domain.entity.RolePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 角色权限表
*/
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    /**
     * 查询角色的所有权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> selectPermissionsByRoleId(@Param("roleId") Long roleId);
}




