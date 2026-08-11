package com.lilac.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.lilac.domain.dto.role.RoleAddRequest;
import com.lilac.domain.dto.role.RoleQueryRequest;
import com.lilac.domain.dto.role.RoleUpdateRequest;
import com.lilac.domain.entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* 角色服务接口
*/
public interface RoleService extends IService<Role> {

    /**
     * 添加角色
     *
     * @param roleAddRequest 添加角色参数
     * @return 角色ID
     */
    Long addRole(RoleAddRequest roleAddRequest);

    /**
     * 获取查询条件
     *
     * @param roleQueryRequest 查询条件
     * @return 查询条件
     */
    Wrapper<Role>getQueryWrapper(RoleQueryRequest roleQueryRequest);

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 删除结果
     */
    boolean deleteRole(Long id);

    /**
     * 根据角色名获取角色
     *
     * @param userDefaultRole 角色名
     * @return 角色
     */
    Role getRoleByName(String userDefaultRole);

    /**
     * 修改角色
     *
     * @param roleUpdateRequest 修改角色参数
     * @return 修改结果
     */
    boolean update(RoleUpdateRequest roleUpdateRequest);
}
