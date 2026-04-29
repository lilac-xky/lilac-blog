package com.lilac.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilac.domain.dto.user.UserQueryRequest;
import com.lilac.domain.dto.user.UserUpdateRequest;
import com.lilac.domain.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lilac.domain.vo.LoginUserVO;
import com.lilac.domain.vo.UserVO;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {

    /**
     * 管理员登录
     *
     * @param account 用户名
     * @param password 密码
     * @return 登录结果
     */
    LoginUserVO adminLogin(String account, String password);

    /**
     * 用户登录
     *
     * @param account 用户名
     * @param password 密码
     * @return 登录结果
     */
    LoginUserVO userLogin(String account, String password);

    /**
     * 用户注册
     *
     * @param userAccount 用户名
     * @param email 邮箱
     * @param password 密码
     * @param chekPassword 确认密码
     * @param code 验证码
     * @return 注册结果
     */
    long userRegister(String userAccount, String email, String password, String chekPassword, String code);

    /**
     * 用户注销
     *
     * @return 注销结果
     */
    boolean logout();

    /**
     * 发送注册验证码
     *
     * @param email 邮箱
     */
    void sendRegisterCode(String email);

    /**
     * 获取当前登录用户
     *
     * @return 用户
     */
    Page<UserVO> listUserVOByPage(UserQueryRequest userQueryRequest);

    /**
     * 获取当前登录用户
     *
     * @return 用户
     */
    User getLoginUser();

    /**
     * 刷新指定用户的 Sa-Token session 缓存
     *
     * @param userId 用户 ID
     */
    void refreshUserSession(Long userId);

    /**
     * 更新用户信息
     *
     * @param userUpdateRequest 用户更新请求
     * @return 更新结果
     */
    boolean updateUser(UserUpdateRequest userUpdateRequest);
}
