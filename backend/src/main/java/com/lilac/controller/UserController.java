package com.lilac.controller;

import cn.hutool.core.util.StrUtil;
import com.lilac.common.DeleteRequest;
import com.lilac.domain.dto.user.*;
import com.lilac.domain.entity.User;
import com.lilac.domain.result.Result;
import com.lilac.domain.vo.LoginUserVO;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.enums.SendCodeSceneEnum;
import com.lilac.manager.auth.anotation.SaUserCheckLogin;
import com.lilac.service.UserService;
import com.lilac.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 用户
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<Long> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest.getUserAccount() == null, HttpsCodeEnum.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String email = userRegisterRequest.getEmail();
        String password = userRegisterRequest.getPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String code = userRegisterRequest.getCode();
        long result = userService.userRegister(userAccount, email, password, checkPassword, code);
        return Result.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @return 登录用户
     */
    @PostMapping("/login")
    public Result<LoginUserVO> login(@RequestBody UserLoginRequest userLoginRequest) {
        ThrowUtils.throwIf(userLoginRequest.getAccount() == null, HttpsCodeEnum.PARAMS_ERROR);
        String account = userLoginRequest.getAccount();
        String password = userLoginRequest.getPassword();
        LoginUserVO loginUserVO = userService.userLogin(account, password);
        return Result.success(loginUserVO);
    }

    /**
     * 用户注销
     *
     * @return 注销结果
     */
    @GetMapping("/logout")
    public Result<Boolean> logout() {
        boolean result = userService.logout();
        return Result.success(result);
    }

    /**
     * 发送注册验证码
     *
     * @param email 邮箱
     * @return 发送结果
     */
    @GetMapping("/sendCode")
    public Result<Boolean> sendRegisterCode(@RequestParam String email) {
        ThrowUtils.throwIf(StrUtil.isBlank(email), HttpsCodeEnum.PARAMS_ERROR);
        userService.sendCode(SendCodeSceneEnum.REGISTER,email);
        return Result.success(true);
    }

    /**
     * 发送重置密码验证码
     *
     * @param email 邮箱
     * @return 发送结果
     */
    @GetMapping("/sendResetCode")
    public Result<Boolean> sendResetCode(@RequestParam String email) {
        ThrowUtils.throwIf(StrUtil.isBlank(email), HttpsCodeEnum.PARAMS_ERROR);
        userService.sendCode(SendCodeSceneEnum.RESET_PASSWORD,email);
        return Result.success(true);
    }

    /**
     * 通过邮箱验证码重置密码
     *
     * @param forgotPasswordRequest 重置密码请求
     * @return 重置结果
     */
    @PostMapping("/resetPassword")
    public Result<Boolean> resetPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        ThrowUtils.throwIf(forgotPasswordRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        boolean result = userService.resetPassword(forgotPasswordRequest.getEmail(),
                forgotPasswordRequest.getCode(),
                forgotPasswordRequest.getNewPassword(),
                forgotPasswordRequest.getCheckPassword());
        return Result.success(result);
    }

    /**
     * 已登录用户修改密码
     *
     * @param updatePasswordRequest 修改密码请求
     * @return 修改结果
     */
    @SaUserCheckLogin
    @PostMapping("/updatePassword")
    public Result<Boolean> updatePassword(@RequestBody UpdatePasswordRequest updatePasswordRequest) {
        ThrowUtils.throwIf(updatePasswordRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        boolean result = userService.updatePassword(updatePasswordRequest.getOldPassword(),
                updatePasswordRequest.getNewPassword(),
                updatePasswordRequest.getCheckPassword());
        return Result.success(result);
    }

    /**
     * 更新用户信息
     *
     * @param userEditRequest 用户更新请求
     * @return 更新结果
     */
    @PostMapping("/update")
    public Result<Boolean> updateUser(@RequestBody UserEditRequest userEditRequest) {
        ThrowUtils.throwIf(userEditRequest == null || userEditRequest.getId() == null
                || userEditRequest.getId() <= 0, HttpsCodeEnum.PARAMS_ERROR);
        // 只有本人才能修改，同时取回服务端认定的用户 id
        Long loginUserId = userService.checkSelf(userEditRequest.getId());
        User user = new User();
        BeanUtils.copyProperties(userEditRequest, user);
        user.setId(loginUserId);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR);
        userService.refreshUserSession(loginUserId);
        return Result.success(true);
    }

    /**
     * 发送新邮箱验证码
     * 仅登录用户可调用，验证码发往新邮箱，用于证明对新邮箱的所有权
     *
     * @param email 新邮箱
     * @return 发送结果
     */
    @PostMapping("/updateEmailCode")
    public Result<Boolean> updateEmailCode(@RequestParam String email) {
        ThrowUtils.throwIf(StrUtil.isBlank(email), HttpsCodeEnum.PARAMS_ERROR);
        userService.sendCode(SendCodeSceneEnum.UPDATE_EMAIL, email.trim().toLowerCase());
        return Result.success(true);
    }

    /**
     * 发送原邮箱验证码
     * 用于修改邮箱时的身份验证；原邮箱已不可用时，前端改为让用户填写当前密码
     *
     * @param id 当前登录用户 id
     * @return 发送结果
     */
    @PostMapping("/sendOriginalEmailCode")
    public Result<Boolean> sendOriginalEmailCode(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, HttpsCodeEnum.PARAMS_ERROR);
        userService.sendOriginalEmailCode(id);
        return Result.success(true);
    }

    /**
     * 验证邮箱所有权
     *
     * @param verifyEmailIdentityRequest 身份验证请求
     * @return 一次性凭证
     */
    @PostMapping("/verifyEmailIdentity")
    public Result<String> verifyEmailIdentity(@RequestBody VerifyEmailIdentityRequest verifyEmailIdentityRequest) {
        ThrowUtils.throwIf(verifyEmailIdentityRequest == null || verifyEmailIdentityRequest.getId() == null
                || verifyEmailIdentityRequest.getId() <= 0, HttpsCodeEnum.PARAMS_ERROR);
        String ticket = userService.verifyEmailIdentity(verifyEmailIdentityRequest);
        return Result.success(ticket);
    }

    /**
     * 更新用户邮箱
     *
     * @param updateEmailMethodRequest 更新邮箱请求
     * @return 更新结果
     */
    @PostMapping("/updateEmailMethod")
    public Result<Boolean> updateEmailMethod(@RequestBody UpdateEmailMethodRequest updateEmailMethodRequest) {
        ThrowUtils.throwIf(updateEmailMethodRequest == null || updateEmailMethodRequest.getId() == null
                || updateEmailMethodRequest.getId() <= 0, HttpsCodeEnum.PARAMS_ERROR);
        boolean result = userService.updateEmailMethod(updateEmailMethodRequest);
        ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR);
        return Result.success(true);
    }

    /**
     * 删除用户
     *
     * @param deleteRequest 删除请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    public Result<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, HttpsCodeEnum.PARAMS_ERROR);
        // todo 注销功能待完善
        Long loginUserId = userService.checkSelf(deleteRequest.getId());
        boolean result = userService.removeById(loginUserId);
        ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR);
        return Result.success(true);
    }

}
