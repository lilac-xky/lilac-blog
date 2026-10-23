package com.lilac.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilac.domain.dto.user.UpdateEmailMethodRequest;
import com.lilac.domain.dto.user.UserQueryRequest;
import com.lilac.domain.dto.user.UserStatusRequest;
import com.lilac.domain.dto.user.UserUpdateRequest;
import com.lilac.domain.dto.user.VerifyEmailIdentityRequest;
import com.lilac.domain.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lilac.domain.vo.LoginUserVO;
import com.lilac.domain.vo.UserVO;
import com.lilac.enums.SendCodeSceneEnum;

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
     * 已登录用户修改密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param checkPassword 确认密码
     * @return 修改结果
     */
    boolean updatePassword(String oldPassword, String newPassword, String checkPassword);

    /**
     * 通过邮箱验证码重置密码
     *
     * @param email 邮箱
     * @param code 验证码
     * @param newPassword 新密码
     * @param checkPassword 确认密码
     * @return 重置结果
     */
    boolean resetPassword(String email, String code, String newPassword, String checkPassword);

    /**
     * 管理员重置用户密码
     *
     * @param userId 用户 ID
     * @param newPassword 新密码
     * @return 重置结果
     */
    boolean adminResetPassword(Long userId, String newPassword);

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

    /**
     * 更新用户状态
     *
     * @param request 用户状态请求
     * @return 更新结果
     */
    boolean updateUserStatus(UserStatusRequest request);

    /**
     * 判断是否为管理员
     *
     * @return 是否为管理员
     */
    boolean isAdmin();

    /**
     * 校验用户是否为当前登录用户
     *
     * @param id 待校验的用户 id
     * @return 当前登录用户 id
     */
    Long checkSelf(Long id);

    /**
     * 发送验证码
     *
     * @param sendCodeSceneEnum 发送验证码场景
     * @param email 邮箱
     */
    void sendCode(SendCodeSceneEnum sendCodeSceneEnum, String email);

    /**
     * 发送原邮箱验证码
     * 用于修改邮箱时的身份验证，验证码发往当前账号已绑定的邮箱
     *
     * @param id 当前登录用户 id
     */
    void sendOriginalEmailCode(Long id);

    /**
     * 验证邮箱身份
     *
     * @param verifyEmailIdentityRequest 身份验证请求
     * @return 一次性凭证
     */
    String verifyEmailIdentity(VerifyEmailIdentityRequest verifyEmailIdentityRequest);

    /**
     * 更新邮箱方法
     *
     * @param updateEmailMethodRequest 更新邮箱请求参数
     * @return 更新结果
     */
    boolean updateEmailMethod(UpdateEmailMethodRequest updateEmailMethodRequest);
}
