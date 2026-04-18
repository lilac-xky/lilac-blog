package com.lilac.service.impl.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilac.constant.UserConstant;
import com.lilac.domain.entity.User;
import com.lilac.domain.vo.LoginUserVO;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.exception.BusinessException;
import com.lilac.manager.auth.StpKit;
import com.lilac.service.impl.UserService;
import com.lilac.mapper.UserMapper;
import com.lilac.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 用户服务实现类
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 用户注册
     *
     * @param userAccount  用户名
     * @param password     密码
     * @param chekPassword 确认密码
     * @return 注册结果
     */
    @Override
    public long userRegister(String userAccount, String password, String chekPassword) {
        // 参数校验
        if (StrUtil.hasBlank(userAccount, password, chekPassword)) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "参数不能为空");
        }
        if (userAccount.length() > 50) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "用户名过长");
        }
        if (password.length() < 6 || password.length() > 20) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "密码长度错误");
        }
        if (!password.equals(chekPassword)) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "密码不一致");
        }
        // 检查数据库是否有重复的用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(HttpsCodeEnum.USER_EXIST);
        }
        // 密码加密
        String encodedPassword = getEncodedPassword(password);
        User user = new User();
        user.setUserAccount(userAccount);
        user.setPassword(encodedPassword);
        user.setUsername(UserConstant.USER_DEFAULT_NAME);
        user.setRole(UserConstant.USER_DEFAULT_ROLE);
        user.setStatus(1);
        try {
            boolean save = this.save(user);
            ThrowUtils.throwIf(!save, HttpsCodeEnum.SYSTEM_ERROR, "注册失败");
        } catch (Exception e) {
            log.error("用户注册失败：{}", e.getMessage());
            throw new BusinessException(HttpsCodeEnum.SYSTEM_ERROR, "注册失败");
        }
        return user.getId();
    }

    /**
     * 管理员登录
     *
     * @param userAccount 用户名
     * @param password    密码
     * @return 登录结果
     */
    @Override
    public LoginUserVO adminLogin(String userAccount, String password) {
        // 参数校验
        if (StrUtil.hasBlank(userAccount, password)) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR);
        }
        if (userAccount.length() > 50) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "用户名过长");
        }
        if (password.length() < 6 || password.length() > 20) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "密码长度错误");
        }
        // 查询数据库是否存在该用户
        User user = this.baseMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserAccount, userAccount)
                .eq(User::getPassword, getEncodedPassword(password)));
        if (user == null) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "用户名或密码错误");
        }
        if(!UserConstant.ADMIN_ROLE.equals(user.getRole())){
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "用户权限不足");
        }
        // 登录
        StpKit.ADMIN.login(user.getId());
        LoginUserVO loginUserVO = this.getLoginUserVO(user);
        loginUserVO.setToken(StpKit.ADMIN.getTokenValue());
        StpKit.ADMIN.getSession().set(UserConstant.ADMIN_LOGIN_STATE, loginUserVO);
        return loginUserVO;
    }

    /**
     * 用户登录
     *
     * @param userAccount 用户名
     * @param password    密码
     * @return 登录结果
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String password) {
        // 参数校验
        if (StrUtil.hasBlank(userAccount, password)) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR);
        }
        if (userAccount.length() > 50) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "用户名过长");
        }
        if (password.length() < 6 || password.length() > 20) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "密码长度错误");
        }
        // 查询数据库是否存在该用户
        User user = this.baseMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserAccount, userAccount)
                .eq(User::getPassword, getEncodedPassword(password)));
        if (user == null) {
            log.info("数据库不存在该用户");
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "用户名或密码错误");
        }
        // 登录
        StpKit.USER.login(user.getId());
        LoginUserVO loginUserVO = this.getLoginUserVO(user);
        loginUserVO.setToken(StpKit.USER.getTokenValue());
        StpKit.USER.getSession().set(UserConstant.USER_LOGIN_STATE, loginUserVO);
        return loginUserVO;
    }

    /**
     * 用户登出
     *
     * @return 登出结果
     */
    @Override
    public boolean logout() {
        if (StpKit.ADMIN.isLogin()) {
            StpKit.ADMIN.logout();
            return true;
        }
        if (StpKit.USER.isLogin()) {
            StpKit.USER.logout();
            return true;
        }
        return false;
    }

    /**
     * 封装用户VO
     *
     * @param user 用户
     * @return userVO
     */
    private LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 获取密码加密
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    public String getEncodedPassword(String userPassword) {
        final String salt = "lilac";
        return DigestUtils.md5DigestAsHex((salt + userPassword).getBytes());
    }
}




