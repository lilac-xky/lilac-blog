package com.lilac.controller;

import com.lilac.domain.dto.user.UserLoginRequest;
import com.lilac.domain.result.Result;
import com.lilac.domain.vo.LoginUserVO;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.service.impl.UserService;
import com.lilac.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private UserService userService;

    /**
     * 管理员登录
     *
     * @param userLoginRequest 用户登录请求
     * @return 登录用户
     */
    @PostMapping("/login")
    public Result<LoginUserVO> login(@RequestBody UserLoginRequest userLoginRequest) {
        ThrowUtils.throwIf(userLoginRequest.getAccount() == null, HttpsCodeEnum.PARAMS_ERROR);
        String account = userLoginRequest.getAccount();
        String password = userLoginRequest.getPassword();
        LoginUserVO loginUserVO = userService.adminLogin(account, password);
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

}
