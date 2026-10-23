package com.lilac.service.impl;

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
import com.lilac.domain.dto.user.UpdateEmailMethodRequest;
import com.lilac.domain.dto.user.UserQueryRequest;
import com.lilac.domain.dto.user.UserStatusRequest;
import com.lilac.domain.dto.user.UserUpdateRequest;
import com.lilac.domain.dto.user.VerifyEmailIdentityRequest;
import com.lilac.domain.entity.Permission;
import com.lilac.domain.entity.Role;
import com.lilac.domain.entity.User;
import com.lilac.domain.vo.LoginUserVO;
import com.lilac.domain.vo.UserVO;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.enums.SendCodeSceneEnum;
import com.lilac.exception.BusinessException;
import com.lilac.manager.auth.StpKit;
import com.lilac.service.RolePermissionService;
import com.lilac.service.RoleService;
import com.lilac.service.UserService;
import com.lilac.mapper.UserMapper;
import com.lilac.service.VerificationCodeService;
import com.lilac.utils.ThrowUtils;
import cn.dev33.satoken.session.SaSession;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private RoleService roleService;
    @Resource
    private RolePermissionService rolePermissionService;
    @Resource
    private VerificationCodeService verificationCodeService;
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
        // 先做数据库重复性检查，避免用户名或邮箱占用时白白消耗验证码
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount)
                .or()
                .eq("email", email);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(HttpsCodeEnum.USER_EXIST, "用户名或邮箱已存在");
        }
        // 校验通过后验证码立即被消费，注册失败需重新获取
        verificationCodeService.verify(SendCodeSceneEnum.REGISTER, email, code);
        // 密码加密
        String encodedPassword = getEncodedPassword(password);
        User user = new User();
        user.setUserAccount(userAccount);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setUsername(UserConstant.USER_DEFAULT_NAME);
        user.setStatus(1);
        // 角色id
        Role role = roleService.getRoleByName(UserConstant.USER_DEFAULT_ROLE);
        user.setRoleId(role.getId());
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
        Role role = roleService.getById(user.getRoleId());
        if (role == null) {
            throw new BusinessException(HttpsCodeEnum.NOT_FOUND_ERROR, "角色不存在");
        }
        if (!UserConstant.ADMIN_ROLE.equals(role.getLoginType())) {
            throw new BusinessException(HttpsCodeEnum.UNAUTHORIZED, "非管理员账号，请使用用户端登录");
        }
        if(user.getStatus() != 1){
            throw new BusinessException(HttpsCodeEnum.UNAUTHORIZED, "用户状态异常");
        }
        // 登录
        StpKit.ADMIN.login(user.getId());
        LoginUserVO loginUserVO = this.getLoginUserVO(user, role);
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
        Role role = roleService.getById(user.getRoleId());
        // 登录
        StpKit.USER.login(user.getId());
        LoginUserVO loginUserVO = this.getLoginUserVO(user, role);
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
     * 已登录用户修改密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param checkPassword 确认密码
     * @return 更新结果
     */
    @Override
    public boolean updatePassword(String oldPassword, String newPassword, String checkPassword) {
        // 参数校验
        if (StrUtil.hasBlank(oldPassword, newPassword, checkPassword)) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "参数不能为空");
        }
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "密码长度错误");
        }
        if (!newPassword.equals(checkPassword)) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "密码不一致");
        }
        if (newPassword.equals(oldPassword)) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "新密码不能与旧密码相同");
        }
        // 获取登录用户
        User loginUser = getLoginUser();
        if (loginUser == null) {
            throw new BusinessException(HttpsCodeEnum.NEED_LOGIN);
        }
        User user = this.getById(loginUser.getId());
        if (user == null) {
            throw new BusinessException(HttpsCodeEnum.NOT_FOUND_ERROR);
        }
        if (!user.getPassword().equals(getEncodedPassword(oldPassword))) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "原密码错误");
        }
        // 更新密码
        User update = new User();
        update.setId(user.getId());
        update.setPassword(getEncodedPassword(newPassword));
        boolean result = this.updateById(update);
        ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR);
        return true;
    }

    /**
     * 通过邮箱验证码重置密码
     *
     * @param email 邮箱
     * @param code 验证码
     * @param newPassword 新密码
     * @param checkPassword 确认密码
     * @return 重置结果
     */
    @Override
    public boolean resetPassword(String email, String code, String newPassword, String checkPassword) {
        // 参数验证
        if (StrUtil.hasBlank(email, code, newPassword, checkPassword)) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "参数不能为空");
        }
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "密码长度错误");
        }
        if (!newPassword.equals(checkPassword)) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "密码不一致");
        }
        // 验证码验证：校验通过即被一次性消费，后续失败需重新获取
        verificationCodeService.verify(SendCodeSceneEnum.RESET_PASSWORD, email, code);
        User user = this.baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) {
            throw new BusinessException(HttpsCodeEnum.NOT_FOUND_ERROR, "该邮箱未注册");
        }
        // 更新密码
        User update = new User();
        update.setId(user.getId());
        update.setPassword(getEncodedPassword(newPassword));
        boolean result = this.updateById(update);
        ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR);
        return true;
    }

    /**
     * 管理员重置用户密码
     *
     * @param userId 用户 ID
     * @param newPassword 新密码
     * @return 重置结果
     */
    @Override
    public boolean adminResetPassword(Long userId, String newPassword) {
        // 参数验证
        if (userId == null || StrUtil.isBlank(newPassword)) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "参数不能为空");
        }
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "密码长度错误");
        }
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(HttpsCodeEnum.NOT_FOUND_ERROR, "用户不存在");
        }
        // 更新密码
        User update = new User();
        update.setId(userId);
        update.setPassword(getEncodedPassword(newPassword));
        boolean result = this.updateById(update);
        ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR);
        return true;
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
        // 执行分页查询
        Page<User> userPage = this.page(new Page<>(current, size), getQueryWrapper(userQueryRequest));
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
        Role role = roleService.getById(user.getRoleId());
        if (role == null) return;
        LoginUserVO freshVO = getLoginUserVO(user, role);
        if(role.getLoginType().equals(UserConstant.ADMIN_ROLE)){
            refreshSession(StpKit.ADMIN, UserConstant.ADMIN_LOGIN_STATE, userId, freshVO);
        }
        if(role.getLoginType().equals(UserConstant.USER_DEFAULT_ROLE)){
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
        Long newRoleId = userUpdateRequest.getRoleId();
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
        if (newRoleId != null) {
            // 不能更改自己的角色
            if (id.equals(getLoginUser().getId())) {
                throw new BusinessException(HttpsCodeEnum.OPERATION_ERROR, "不能更改自己的角色");
            }
        }
        // 不能更改自己状态
        if(ObjUtil.isNotEmpty(status)){
            if(id.equals(getLoginUser().getId()) && !status.equals(getLoginUser().getStatus())){
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
     * 判断是否为管理员
     *
     * @return 是否为管理员
     */
    @Override
    public boolean isAdmin() {
        User loginUser = getLoginUser();
        if (loginUser == null) return false;
        Role role = roleService.getById(loginUser.getRoleId());
        return role != null && UserConstant.ADMIN_ROLE.equals(role.getLoginType());
    }

    /**
     * 检查用户是否为当前用户
     *
     * @param id 用户 ID
     * @return 用户 ID
     */
    @Override
    public Long checkSelf(Long id) {
        // 登录态可能因会话过期而缺失，统一走 getLoginUser 判空，避免直接空指针
        LoginUserVO vo = null;
        if (StpKit.USER.isLogin()) {
            vo = (LoginUserVO) StpKit.USER.getSession().get(UserConstant.USER_LOGIN_STATE);
        }
        ThrowUtils.throwIf(vo == null || vo.getId() == null, HttpsCodeEnum.NEED_LOGIN);
        ThrowUtils.throwIf(id == null || !vo.getId().equals(id), HttpsCodeEnum.FORBIDDEN, "只能操作自己");
        return vo.getId();
    }

    /**
     * 发送验证码
     *
     * @param sendCodeSceneEnum 发送验证码场景
     * @param email 邮箱
     */
    @Override
    public void sendCode(SendCodeSceneEnum sendCodeSceneEnum, String email) {
        verificationCodeService.send(sendCodeSceneEnum, email);
    }

    /**
     * 发送原邮箱验证码
     *
     * @param id 当前登录用户 id
     */
    @Override
    public void sendOriginalEmailCode(Long id) {
        // 先确认是本人操作，避免被用来给任意账号的邮箱发骚扰信
        checkSelf(id);
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException(HttpsCodeEnum.NOT_FOUND_ERROR, "用户不存在");
        }
        if (StrUtil.isBlank(user.getEmail())) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "当前账号未绑定邮箱，请使用密码验证");
        }
        // 目标邮箱以库中记录为准，不接受前端传入，防止被改发到其他地址
        verificationCodeService.send(SendCodeSceneEnum.UPDATE_EMAIL_OLD, user.getEmail());
    }

    /**
     * 校验邮箱身份并签发一次性凭证
     *
     * @param request 身份验证请求
     * @return 一次性凭证
     */
    @Override
    public String verifyEmailIdentity(VerifyEmailIdentityRequest request) {
        ThrowUtils.throwIf(request == null, HttpsCodeEnum.PARAMS_ERROR);
        Long id = request.getId();
        ThrowUtils.throwIf(id == null || id <= 0, HttpsCodeEnum.PARAMS_ERROR);
        // 验证方式必须显式指定，避免"两个都不传"时静默放行
        String method = StrUtil.trimToEmpty(request.getMethod()).toLowerCase();
        if (!"email".equals(method) && !"password".equals(method)) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "验证方式不正确");
        }
        // 确认是本人操作
        checkSelf(id);
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException(HttpsCodeEnum.NOT_FOUND_ERROR, "用户不存在");
        }
        if ("email".equals(method)) {
            if (StrUtil.isBlank(request.getOriginalCode())) {
                throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "请输入原邮箱验证码");
            }
            if (StrUtil.isBlank(user.getEmail())) {
                throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "当前账号未绑定邮箱，请使用密码验证");
            }
            // 原邮箱以库中记录为准，防止用他人的码来通过验证
            verificationCodeService.verify(SendCodeSceneEnum.UPDATE_EMAIL_OLD, user.getEmail(), request.getOriginalCode());
        } else {
            if (StrUtil.isBlank(request.getPassword())) {
                throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "请输入当前密码");
            }
            // 密码错误统一提示，不暴露具体校验细节
            if (!user.getPassword().equals(getEncodedPassword(request.getPassword()))) {
                throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "密码错误或验证码错误");
            }
        }
        // 签发一次性凭证：随机串绑定用户 id，第二步凭它换绑邮箱
        String ticket = RandomUtil.randomString(32);
        stringRedisTemplate.opsForValue().set(UserConstant.USER_CHANGE_EMAIL_TICKET_KEY + ticket,
                String.valueOf(id), UserConstant.USER_CHANGE_EMAIL_TICKET_TTL, TimeUnit.MINUTES);
        log.info("用户 {} 修改邮箱身份验证通过，已签发凭证", id);
        return ticket;
    }

    /**
     * 更新邮箱
     *
     * @param request 更新邮箱请求参数
     * @return 更新结果
     */
    @Override
    public boolean updateEmailMethod(UpdateEmailMethodRequest request) {
        ThrowUtils.throwIf(request == null, HttpsCodeEnum.PARAMS_ERROR);
        Long id = request.getId();
        ThrowUtils.throwIf(id == null || id <= 0, HttpsCodeEnum.PARAMS_ERROR);
        // 新邮箱与新邮箱验证码是第二步的必填项
        if (StrUtil.isBlank(request.getEmail()) || StrUtil.isBlank(request.getCode())) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "新邮箱和验证码不能为空");
        }
        if (StrUtil.isBlank(request.getTicket())) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "身份验证已失效，请重新验证");
        }
        // 确认是本人操作
        checkSelf(id);
        // 凭证必须存在且归属于当前用户，否则说明未完成第一步验证
        String ticketKey = UserConstant.USER_CHANGE_EMAIL_TICKET_KEY + request.getTicket().trim();
        String ticketOwner = stringRedisTemplate.opsForValue().get(ticketKey);
        if (ticketOwner == null || !ticketOwner.equals(String.valueOf(id))) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "身份验证已失效，请重新验证");
        }
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException(HttpsCodeEnum.NOT_FOUND_ERROR, "用户不存在");
        }
        // 邮箱统一转小写去空格，保证查重与验证码 key 和发码时一致
        String email = request.getEmail().trim().toLowerCase();
        if (email.equals(user.getEmail())) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "新邮箱不能与当前邮箱相同");
        }
        if (!Validator.isEmail(email)) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "邮箱格式不正确");
        }
        if (email.length() > 100) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "邮箱长度超过限制");
        }
        long count = this.baseMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
                .ne(User::getId, id));
        if (count > 0) {
            throw new BusinessException(HttpsCodeEnum.USER_EXIST, "邮箱已存在");
        }
        // 校验新邮箱验证码，通过即一次性消费
        verificationCodeService.verify(SendCodeSceneEnum.UPDATE_EMAIL, email, request.getCode());
        // 更新邮箱并刷新会话，让顶栏等已缓存的用户信息同步
        User update = new User();
        update.setId(id);
        update.setEmail(email);
        boolean result = this.updateById(update);
        ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR);
        refreshUserSession(id);
        // 换绑成功后凭证立即失效，防止同一凭证重复使用
        stringRedisTemplate.delete(ticketKey);
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
        Long roleId = userQueryRequest.getRoleId();
        Integer status = userQueryRequest.getStatus();
        String sortOrder = userQueryRequest.getSortOrder();
        // 拼装条件
        queryWrapper.eq(ObjUtil.isNotEmpty(id), User::getId, id);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), User::getUserAccount, userAccount);
        queryWrapper.like(StrUtil.isNotBlank(email), User::getEmail, email);
        queryWrapper.like(StrUtil.isNotBlank(username), User::getUsername, username);
        queryWrapper.eq(ObjUtil.isNotEmpty(roleId), User::getRoleId, roleId);
        queryWrapper.eq(ObjUtil.isNotEmpty(status), User::getStatus, status);
        boolean isAsc = "ascend".equalsIgnoreCase(sortOrder);
        queryWrapper.orderBy(true, isAsc, User::getCreateTime);
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
    private LoginUserVO getLoginUserVO(User user, Role role) {
        if (user == null) return null;
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        if(role != null){
            loginUserVO.setRole(role.getRoleKey());
            // 获取用户权限列表
            List<Permission> permissionList = rolePermissionService.getPermissionsByRoleId(role.getId());
            if(permissionList != null && !permissionList.isEmpty()){
                List<String> permissions = permissionList.stream()
                        .map(Permission::getPermissionKey)
                        .collect(Collectors.toList());
                loginUserVO.setPermissions(permissions);
            }
            // 设置角色列表（目前一个用户只有一个角色）
            loginUserVO.setRoles(Collections.singletonList(role.getRoleKey()));
        }
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
