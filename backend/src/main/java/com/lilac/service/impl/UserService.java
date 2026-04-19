package com.lilac.service.impl;

import com.lilac.domain.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lilac.domain.vo.LoginUserVO;

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
}
