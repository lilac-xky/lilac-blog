package com.lilac.controller;

import com.lilac.domain.dto.user.UserLoginRequest;
import com.lilac.domain.dto.user.UserRegisterRequest;
import com.lilac.domain.result.Result;
import com.lilac.domain.vo.LoginUserVO;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.service.impl.UserService;
import com.lilac.utils.ThrowUtils;
import jakarta.annotation.Resource;
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
        String password = userRegisterRequest.getPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount, password, checkPassword);
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
        ThrowUtils.throwIf(userLoginRequest.getUserAccount() == null, HttpsCodeEnum.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String password = userLoginRequest.getPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, password);
        return Result.success(loginUserVO);
    }


}
