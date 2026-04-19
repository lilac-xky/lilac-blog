package com.lilac.service.impl.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
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
import com.lilac.manager.email.MailService;
import com.lilac.service.impl.UserService;
import com.lilac.mapper.UserMapper;
import com.lilac.utils.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private MailService mailService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 用户注册
     *
     * @param userAccount 用户名
     * @param email 邮箱
     * @param password 密码
     * @param chekPassword 确认密码
     * @return 注册结果
     */
    @Override
    public long userRegister(String userAccount, String email, String password, String chekPassword, String code) {
        // 参数校验
        if (StrUtil.hasBlank(userAccount, email, password, chekPassword)) {
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
        // 尝试获取验证码
        String redisKey = UserConstant.USER_REGISTER_CODE_KEY + email;
        String errorCountKey = UserConstant.USER_REGISTER_CODE_ERROR_KEY + email;
        String captcha = stringRedisTemplate.opsForValue().get(redisKey);
        if (captcha == null) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "验证码已过期或未发送");
        }
        // 防暴力破解：检查错误次数
        String errorCountStr = stringRedisTemplate.opsForValue().get(errorCountKey);
        int errorCount = errorCountStr == null ? 0 : Integer.parseInt(errorCountStr);
        if (errorCount >= 5) {
            // 错误太多，直接作废验证码
            stringRedisTemplate.delete(redisKey);
            stringRedisTemplate.delete(errorCountKey);
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "验证码错误次数过多，请重新获取");
        }
        // 验证码校验
        if (!captcha.equals(code)) {
            stringRedisTemplate.opsForValue().increment(errorCountKey);
            // 错误计数器跟验证码有效期一致
            stringRedisTemplate.expire(errorCountKey, UserConstant.USER_REGISTER_CODE_TTL, TimeUnit.MINUTES);
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "验证码错误");
        }
        // 校验通过，清理错误计数
        stringRedisTemplate.delete(errorCountKey);
        // 检查数据库是否有重复的用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount)
                .or()
                .eq("email", email);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(HttpsCodeEnum.USER_EXIST, "用户名或邮箱已存在");
        }
        // 密码加密
        String encodedPassword = getEncodedPassword(password);
        User user = new User();
        user.setUserAccount(userAccount);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setUsername(UserConstant.USER_DEFAULT_NAME);
        user.setRole(UserConstant.USER_DEFAULT_ROLE);
        user.setStatus(1);
        try {
            boolean save = this.save(user);
            ThrowUtils.throwIf(!save, HttpsCodeEnum.SYSTEM_ERROR, "注册失败");
            // 删除验证码
            stringRedisTemplate.delete(redisKey);
        } catch (Exception e) {
            log.error("用户注册失败：{}", e.getMessage());
            throw new BusinessException(HttpsCodeEnum.SYSTEM_ERROR, "注册失败");
        }
        return user.getId();
    }

    /**
     * 管理员登录
     *
     * @param account 用户名
     * @param password    密码
     * @return 登录结果
     */
    @Override
    public LoginUserVO adminLogin(String account, String password) {
        // 参数校验
        if (StrUtil.hasBlank(account, password)) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR);
        }
        if (account.length() > 50) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "用户名过长");
        }
        if (password.length() < 6 || password.length() > 20) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "密码长度错误");
        }
        // 查询数据库是否存在该用户
        User user = getAuthenticatedUser(account, password);
        if (user == null) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "账号或密码错误");
        }
        if(!UserConstant.ADMIN_ROLE.equals(user.getRole())){
            throw new BusinessException(HttpsCodeEnum.UNAUTHORIZED, "非管理员无法登录");
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
     * @param account 用户名
     * @param password    密码
     * @return 登录结果
     */
    @Override
    public LoginUserVO userLogin(String account, String password) {
        // 参数校验
        if (StrUtil.hasBlank(account, password)) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR);
        }
        if (account.length() > 50) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "用户名过长");
        }
        if (password.length() < 6 || password.length() > 20) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "密码长度错误");
        }
        // 查询数据库是否存在该用户
        User user = getAuthenticatedUser(account, password);
        if (user == null) {
            log.info("数据库不存在该用户");
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "账号或密码错误");
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
     * 发送注册验证码
     *
     * @param email 邮箱
     */
    @Override
    public void sendRegisterCode(String email) {
        // 基本校验
        if (StrUtil.isBlank(email)) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "邮箱不能为空");
        }
        // 邮箱格式校验
        if (!Validator.isEmail(email)) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "邮箱格式不正确");
        }
        // 邮箱长度校验
        if (email.length() > 100) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "邮箱长度超过限制");
        }
        // 邮箱是否注册
        long count = this.baseMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (count > 0) {
            throw new BusinessException(HttpsCodeEnum.USER_EXIST, "该邮箱已注册");
        }
        // 防刷校验：检查是否发送过于频繁（1分钟内不准重复发送）
        String limitKey = UserConstant.USER_REGISTER_LIMIT_KEY + email;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(limitKey))) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "发送过于频繁，请稍后再试");
        }
        // 生成6位随机验证码
        String code = RandomUtil.randomNumbers(6);
        // 存入 Redis 并设置过期时间 (5分钟)
        String codeKey = UserConstant.USER_REGISTER_CODE_KEY + email;
        stringRedisTemplate.opsForValue().set(codeKey, code, UserConstant.USER_REGISTER_CODE_TTL, TimeUnit.MINUTES);
        // 设置限流标识 (60秒过期)
        stringRedisTemplate.opsForValue().set(limitKey, "1", 1, TimeUnit.MINUTES);
        // 调用异步邮件服务
        String subject = "【Lilac】注册验证码";
        String content = "您的注册验证码为：" + code + "，有效期5分钟。";
        mailService.sendSimpleMail(email, subject, content);
        log.info("发送验证码请求已提交，邮箱: {}", email);
    }

    /**
     * 公共登录：支持邮箱和用户名登录
     *
     * @param account 用户名
     * @param password    密码
     * @return 用户
     */
    public User getAuthenticatedUser(String account, String password){
        if(StrUtil.hasBlank(account, password)){
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR);
        }
        return this.baseMapper.selectOne(new LambdaQueryWrapper<User>()
                .and(wrapper -> wrapper.eq(User::getUserAccount, account).or().eq(User::getEmail, account))
                .eq(User::getPassword, getEncodedPassword(password)));
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




