package com.lilac.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilac.common.DeleteRequest;
import com.lilac.domain.dto.user.UserLoginRequest;
import com.lilac.domain.dto.user.UserQueryRequest;
import com.lilac.domain.dto.user.UserStatusRequest;
import com.lilac.domain.dto.user.UserUpdateRequest;
import com.lilac.domain.entity.User;
import com.lilac.domain.result.Result;
import com.lilac.domain.vo.LoginUserVO;
import com.lilac.domain.vo.UserVO;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.exception.BusinessException;
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

    /**
     * 更新用户信息
     *
     * @param userUpdateRequest 用户更新请求
     * @return 更新结果
     */
    @PostMapping("/update")
    public Result<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() == null, HttpsCodeEnum.PARAMS_ERROR);
        boolean result = userService.updateUser(userUpdateRequest);
        ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR);
        return Result.success(true);
    }

    /**
     * 更新用户状态
     *
     * @param userStatusRequest 用户状态请求
     * @return 更新结果
     */
    @PostMapping("/updateStatus")
    public Result<Boolean> updateUserStatus(@RequestBody UserStatusRequest userStatusRequest) {
        ThrowUtils.throwIf(userStatusRequest == null || userStatusRequest.getId() == null, HttpsCodeEnum.PARAMS_ERROR);
        boolean result = userService.updateUserStatus(userStatusRequest);
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
        User loginUser = userService.getLoginUser();
        if(deleteRequest.getId().equals(loginUser.getId())){
            throw new BusinessException(HttpsCodeEnum.OPERATION_ERROR, "不能删除自己");
        }
        boolean result = userService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR);
        return Result.success(true);
    }

    /**
     * 分页获取用户列表
     *
     * @param userQueryRequest 用户查询请求
     * @return 用户列表
     */
    @PostMapping("/list/page/vo")
    public Result<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest){
        ThrowUtils.throwIf(userQueryRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        Page<UserVO> userVOList = userService.listUserVOByPage(userQueryRequest);
        return Result.success(userVOList);
    }

    /**
     * 验证当前 token 有效性
     *
     * @return 验证结果
     */
    @GetMapping("/currentUser")
    public Result<Boolean> currentUser() {
        return Result.success(true);
    }

}
