package com.lilac.service.impl;

import com.lilac.domain.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lilac.domain.vo.LoginUserVO;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     *
     * @param userAccount 用户名
     * @param password 密码
     * @return 登录结果
     */
    LoginUserVO userLogin(String userAccount, String password);

    /**
     * 用户注册
     *
     * @param userAccount 用户名
     * @param password 密码
     * @param chekPassword 确认密码
     * @return 注册结果
     */
    long userRegister(String userAccount, String password, String chekPassword);
}
