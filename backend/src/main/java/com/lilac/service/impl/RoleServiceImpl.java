package com.lilac.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilac.constant.UserConstant;
import com.lilac.domain.dto.role.RoleAddRequest;
import com.lilac.domain.dto.role.RoleQueryRequest;
import com.lilac.domain.dto.role.RoleUpdateRequest;
import com.lilac.domain.entity.Role;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.exception.BusinessException;
import com.lilac.service.RoleService;
import com.lilac.mapper.RoleMapper;
import com.lilac.utils.ThrowUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
* 角色服务实现类
*/
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService{

    /**
     * 添加角色
     *
     * @param roleAddRequest 角色添加参数
     * @return 角色ID
     */
    @Override
    public Long addRole(RoleAddRequest roleAddRequest) {
        ThrowUtils.throwIf(StrUtil.isBlank(roleAddRequest.getRoleKey()), HttpsCodeEnum.PARAMS_ERROR, "角色键不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(roleAddRequest.getLoginType()), HttpsCodeEnum.PARAMS_ERROR, "登录类型不能为空");
        String roleKey = roleAddRequest.getRoleKey();
        // 角色是否存在
        Long count = this.baseMapper.selectCount(new LambdaQueryWrapper<Role>().eq(Role::getRoleKey, roleKey));
        ThrowUtils.throwIf(count > 0, HttpsCodeEnum.PARAMS_ERROR, "角色已存在");
        // 保存角色
        Role role = new Role();
        BeanUtils.copyProperties(roleAddRequest, role);
        boolean result = this.save(role);
        ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR, "添加失败");
        return role.getId();
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 删除结果
     */
    @Override
    public boolean deleteRole(Long id) {
        // 超级管理员不能删除
        Role role = this.getById(id);
        ThrowUtils.throwIf(role.getRoleKey().equals("super-admin"), HttpsCodeEnum.PARAMS_ERROR, "超级管理员不能被删除");
        // 改角色关联用户不能被删除
        Long userCount = this.baseMapper.selectCount(new LambdaQueryWrapper<Role>().eq(Role::getId, id));
        ThrowUtils.throwIf(userCount > 0, HttpsCodeEnum.PARAMS_ERROR, "该角色关联用户不能被删除");
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR, "删除失败");
        return true;
    }

    /**
     * 根据角色名获取角色
     *
     * @param userDefaultRole 角色名
     * @return 角色
     */
    @Override
    public Role getRoleByName(String userDefaultRole) {
        LambdaQueryWrapper<Role> roleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roleLambdaQueryWrapper.eq(Role::getRoleKey, userDefaultRole);
        return this.baseMapper.selectOne(roleLambdaQueryWrapper);
    }

    /**
     * 修改角色
     *
     * @param roleUpdateRequest 角色修改参数
     * @return 修改结果
     */
    @Override
    public boolean update(RoleUpdateRequest roleUpdateRequest) {
        // 超级管理员不允许被修改
        Role role = this.getById(roleUpdateRequest.getId());
        if(role.getRoleKey().equals(UserConstant.SUPER_ROLE) && !roleUpdateRequest.getRoleKey().equals(UserConstant.SUPER_ROLE)){
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "超级管理员不允许被修改");
        }
        // key不能重复
        String roleKey = roleUpdateRequest.getRoleKey();
        // 排除自己
        Long count = this.baseMapper.selectCount(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleKey, roleKey)
                .ne(Role::getId, roleUpdateRequest.getId()));
        ThrowUtils.throwIf(count > 0, HttpsCodeEnum.PARAMS_ERROR, "角色已存在");
        BeanUtils.copyProperties(roleUpdateRequest, role);
        boolean result = this.updateById(role);
        ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR, "修改失败");
        return true;
    }

    /**
     * 获取角色列表
     *
     * @param roleQueryRequest 角色查询参数
     * @return 角色列表
     */
    @Override
    public Wrapper<Role> getQueryWrapper(RoleQueryRequest roleQueryRequest) {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        Long id = roleQueryRequest.getId();
        String roleKey = roleQueryRequest.getRoleKey();
        String name = roleQueryRequest.getName();
        String loginType = roleQueryRequest.getLoginType();
        // 查询
        queryWrapper.eq(ObjUtil.isNotEmpty(id), Role::getId, id);
        queryWrapper.eq(StrUtil.isNotBlank(roleKey), Role::getRoleKey, roleKey);
        queryWrapper.eq(StrUtil.isNotBlank(name), Role::getName, name);
        queryWrapper.eq(StrUtil.isNotBlank(loginType), Role::getLoginType, loginType);
        boolean isAsc = "ascend".equalsIgnoreCase(roleQueryRequest.getSortOrder());
        queryWrapper.orderBy(true, isAsc, Role::getCreateTime);
        return queryWrapper;
    }
}




