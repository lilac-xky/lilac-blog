package com.lilac.controller;

import cn.hutool.core.util.StrUtil;
import com.lilac.common.DeleteRequest;
import com.lilac.constant.UserConstant;
import com.lilac.domain.dto.user.UserEditRequest;
import com.lilac.domain.dto.user.UserLoginRequest;
import com.lilac.domain.dto.user.UserRegisterRequest;
import com.lilac.domain.entity.User;
import com.lilac.domain.result.Result;
import com.lilac.domain.vo.LoginUserVO;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.exception.BusinessException;
import com.lilac.manager.auth.StpKit;
import com.lilac.manager.auth.anotation.SaUserCheckLogin;
import com.lilac.service.impl.UserService;
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
        userService.sendRegisterCode(email);
        return Result.success(true);
    }

    /**
     * 更新用户信息
     *
     * @param userEditRequest 用户更新请求
     * @return 更新结果
     */
    @PostMapping("/update")
    public Result<Boolean> updateUser(@RequestBody UserEditRequest userEditRequest) {
        ThrowUtils.throwIf(userEditRequest == null || userEditRequest.getId() <= 0, HttpsCodeEnum.PARAMS_ERROR);
        // todo 带开发前台系统时增加修改用户名和邮箱次数限制，并且保证邮箱正确，需要验证码校验
        User user = new User();
        BeanUtils.copyProperties(userEditRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR);
        userService.refreshUserSession(userEditRequest.getId());
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
        LoginUserVO loginUser = (LoginUserVO) StpKit.USER.getSession().get(UserConstant.USER_LOGIN_STATE);
        // todo 注销功能待完善
        if(!loginUser.getId().equals(deleteRequest.getId())){
            throw new BusinessException(HttpsCodeEnum.OPERATION_ERROR, "只能删除自己");
        }
        boolean result = userService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR);
        return Result.success(true);
    }

}
