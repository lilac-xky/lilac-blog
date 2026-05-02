package com.lilac.service.impl.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilac.constant.UserConstant;
import com.lilac.domain.dto.user.UserQueryRequest;
import com.lilac.domain.dto.user.UserStatusRequest;
import com.lilac.domain.dto.user.UserUpdateRequest;
import com.lilac.domain.entity.User;
import com.lilac.domain.vo.LoginUserVO;
import com.lilac.domain.vo.UserVO;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.exception.BusinessException;
import com.lilac.manager.auth.StpKit;
import com.lilac.manager.email.MailService;
import com.lilac.service.impl.UserService;
import com.lilac.mapper.UserMapper;
import com.lilac.utils.ThrowUtils;
import cn.dev33.satoken.session.SaSession;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
        validateAccountAndPassword(account, password);
        // 查询数据库是否存在该用户
        User user = getAuthenticatedUser(account, password);
        if (user == null) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "账号或密码错误");
        }
        if(!UserConstant.ADMIN_ROLE.equals(user.getRole())){
            throw new BusinessException(HttpsCodeEnum.UNAUTHORIZED, "非管理员无法登录");
        }
        if(user.getStatus() != 1){
            throw new BusinessException(HttpsCodeEnum.UNAUTHORIZED, "用户状态异常");
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
        validateAccountAndPassword(account, password);
        // 查询数据库是否存在该用户
        User user = getAuthenticatedUser(account, password);
        if (user == null) {
            log.info("数据库不存在该用户");
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "账号或密码错误");
        }
        if(user.getStatus() != 1){
            throw new BusinessException(HttpsCodeEnum.UNAUTHORIZED, "用户状态异常");
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
        if (stringRedisTemplate.hasKey(limitKey)) {
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
     * 获取用户列表
     *
     * @param userQueryRequest 用户查询请求
     * @return 用户列表
     */
    @Override
    public Page<UserVO> listUserVOByPage(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = getQueryWrapper(userQueryRequest);
        // 执行分页查询
        Page<User> userPage = this.page(new Page<>(current, size), queryWrapper);
        // 转换为 VO
        Page<UserVO> userVOPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        userVOPage.setRecords(userPage.getRecords().stream().map(UserVO::objToVo).toList());
        return userVOPage;
    }

    /**
     * 获取当前登录用户（从 Sa-Token session 读取，不查 DB）
     *
     * @return 当前登录用户
     */
    @Override
    public User getLoginUser() {
        LoginUserVO vo = null;
        if (StpKit.ADMIN.isLogin()) {
            vo = (LoginUserVO) StpKit.ADMIN.getSession().get(UserConstant.ADMIN_LOGIN_STATE);
        } else if (StpKit.USER.isLogin()) {
            vo = (LoginUserVO) StpKit.USER.getSession().get(UserConstant.USER_LOGIN_STATE);
        }
        if (vo == null) return null;
        User user = new User();
        BeanUtil.copyProperties(vo, user);
        return user;
    }

    /**
     * 刷新指定用户的 Sa-Token session 缓存（用户信息更新后调用）
     *
     * @param userId 用户 ID
     */
    @Override
    public void refreshUserSession(Long userId) {
        User user = this.getById(userId);
        if (user == null) return;
        LoginUserVO freshVO = getLoginUserVO(user);
        if(freshVO.getRole().equals(UserConstant.ADMIN_ROLE)){
            refreshSession(StpKit.ADMIN, UserConstant.ADMIN_LOGIN_STATE, userId, freshVO);
        }
        if(freshVO.getRole().equals(UserConstant.USER_DEFAULT_ROLE)){
            refreshSession(StpKit.USER, UserConstant.USER_LOGIN_STATE, userId, freshVO);
        }
    }

    /**
     * 更新用户信息
     *
     * @param userUpdateRequest 用户更新请求
     * @return 更新结果
     */
    @Override
    public boolean updateUser(UserUpdateRequest userUpdateRequest) {
        Long id = userUpdateRequest.getId();
        String userAccount = userUpdateRequest.getUserAccount();
        String email = userUpdateRequest.getEmail();
        String role = userUpdateRequest.getRole();
        Integer status = userUpdateRequest.getStatus();
        // 排除自身后检查用户名是否被其他用户占用
        if (StrUtil.isNotBlank(userAccount)) {
            long count = this.baseMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getUserAccount, userAccount)
                    .ne(User::getId, id));
            if (count > 0) {
                throw new BusinessException(HttpsCodeEnum.USER_EXIST, "用户名已存在");
            }
        }
        // 排除自身后检查邮箱是否被其他用户占用
        if (StrUtil.isNotBlank(email)) {
            long count = this.baseMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, email)
                    .ne(User::getId, id));
            if (count > 0) {
                throw new BusinessException(HttpsCodeEnum.USER_EXIST, "邮箱已存在");
            }
        }
        // 不能更改自己角色
        // todo 待权限校验细化修改
        if (StrUtil.isNotBlank(role)) {
            if(!role.equals(getLoginUser().getRole())){
                throw new BusinessException(HttpsCodeEnum.OPERATION_ERROR, "不能更改自己的角色");
            }
        }
        // 不能更改自己状态
        if(ObjUtil.isNotEmpty(status)){
            if(!status.equals(getLoginUser().getStatus())){
                throw new BusinessException(HttpsCodeEnum.OPERATION_ERROR, "不能更改自己的状态");
            }
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR);
        refreshUserSession(id);
        return true;
    }

    /**
     * 更新用户状态
     *
     * @param request 用户状态请求
     * @return 更新结果
     */
    @Override
    public boolean updateUserStatus(UserStatusRequest request) {
        Long id = request.getId();
        Integer status = request.getStatus();
        User loginUser = getLoginUser();
        if (id.equals(loginUser.getId())) {
            throw new BusinessException(HttpsCodeEnum.OPERATION_ERROR, "不能更改自己的状态");
        }
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR);
        refreshUserSession(id);
        return true;
    }

    /**
     * 刷新指定用户的 Sa-Token session 缓存（用户信息更新后调用）
     *
     * @param stpLogic       Sa-Token 逻辑
     * @param sessionKey     session key
     * @param userId         用户 ID
     * @param freshVO        刷新的 VO
     */
    private void refreshSession(cn.dev33.satoken.stp.StpLogic stpLogic, String sessionKey, Long userId, LoginUserVO freshVO) {
        try {
            SaSession session = stpLogic.getSessionByLoginId(userId, false);
            if (session == null) return;
            LoginUserVO existing = (LoginUserVO) session.get(sessionKey);
            if (existing == null) return;
            // 保留原 token，仅刷新用户信息字段
            freshVO.setToken(existing.getToken());
            session.set(sessionKey, freshVO);
        } catch (Exception ignored) {
        }
    }

    /**
     * 获取查询条件
     *
     * @param userQueryRequest 用户查询请求
     * @return 查询条件
     */
    private LambdaQueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (userQueryRequest == null) {
            return queryWrapper;
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String email = userQueryRequest.getEmail();
        String username = userQueryRequest.getUsername();
        String role = userQueryRequest.getRole();
        Integer status = userQueryRequest.getStatus();
        String sortOrder = userQueryRequest.getSortOrder();
        // 拼装条件
        queryWrapper.eq(ObjUtil.isNotEmpty(id), User::getId, id);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), User::getUserAccount, userAccount);
        queryWrapper.like(StrUtil.isNotBlank(email), User::getEmail, email);
        queryWrapper.like(StrUtil.isNotBlank(username), User::getUsername, username);
        queryWrapper.eq(StrUtil.isNotBlank(role), User::getRole, role);
        queryWrapper.eq(ObjUtil.isNotEmpty(status), User::getStatus, status);
        // 排序处理
        boolean isDesc = "descend".equalsIgnoreCase(sortOrder);
        queryWrapper.orderBy(true, isDesc, User::getCreatTime);
        return queryWrapper;
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

    /**
     * 验证用户名和密码
     *
     * @param account 用户名
     * @param password 密码
     */
    void validateAccountAndPassword(String account, String password){
        if (StrUtil.hasBlank(account, password)) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR);
        }
        if (account.length() > 50) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "用户名过长");
        }
        if (password.length() < 6 || password.length() > 20) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "密码长度错误");
        }
    }
}
