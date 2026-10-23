package com.lilac.service.impl;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lilac.domain.entity.User;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.enums.SendCodeSceneEnum;
import com.lilac.exception.BusinessException;
import com.lilac.manager.email.MailService;
import com.lilac.mapper.UserMapper;
import com.lilac.service.VerificationCodeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 */
@Slf4j
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private MailService mailService;

    /**
     * 发送验证码
     * 输入：场景枚举 + 目标邮箱
     * 副作用：写入 Redis 验证码与限流标识，并异步发送邮件
     *
     * @param scene 场景
     * @param email 邮箱
     */
    @Override
    public void send(SendCodeSceneEnum scene, String email) {
        if (scene == null) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "验证码场景不能为空");
        }
        // 邮箱基础校验：非空、格式、长度
        validateEmail(email);
        // 统一转小写并去空格，保证存储的验证码 key 与校验时一致
        email = email.trim().toLowerCase();
        // 按场景校验邮箱准入规则（注册要未注册、重置要已注册）
        checkEmailPolicy(scene, email);
        // 限流标识原子占位，避免并发请求同时通过检查后发送多封邮件
        String limitKey = scene.getLimitKeyPrefix() + email;
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(limitKey, "1", SendCodeSceneEnum.LIMIT_SECONDS, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(acquired)) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "发送过于频繁，请稍后再试");
        }
        try {
            // 生成 6 位数字验证码并写入缓存，有效期由场景决定
            String code = RandomUtil.randomNumbers(6);
            String codeKey = scene.getCodeKeyPrefix() + email;
            stringRedisTemplate.opsForValue().set(codeKey, code, scene.getTtlMinutes(), TimeUnit.MINUTES);
            // 重新发码意味着旧错误计数作废，避免上一次的错误次数压到新码上
            stringRedisTemplate.delete(scene.getErrorKeyPrefix() + email);
            // 邮件标题与正文由场景模板决定，正文用验证码填充
            mailService.sendSimpleMail(email, scene.getSubject(), String.format(scene.getContentTemplate(), code));
        } catch (RuntimeException e) {
            // 发码失败时释放限流占位，否则用户要空等一分钟才能重试
            stringRedisTemplate.delete(limitKey);
            throw e;
        }
        log.info("发送{}验证码请求已提交，邮箱: {}", scene.getDesc(), email);
    }

    /**
     * 校验验证码
     * 输入：场景枚举 + 邮箱 + 用户提交的验证码
     * 副作用：校验通过后立即删除验证码（一次性消费），失败时累加错误计数
     *
     * @param scene 场景
     * @param email 邮箱
     * @param code  验证码
     */
    @Override
    public void verify(SendCodeSceneEnum scene, String email, String code) {
        if (scene == null) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "验证码场景不能为空");
        }
        if (StrUtil.hasBlank(email, code)) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "邮箱或验证码不能为空");
        }
        // 与发送时保持一致：统一转小写并去空格，否则大小写不同会取不到码
        email = email.trim().toLowerCase();
        String codeKey = scene.getCodeKeyPrefix() + email;
        String errorKey = scene.getErrorKeyPrefix() + email;
        String cachedCode = stringRedisTemplate.opsForValue().get(codeKey);
        if (cachedCode == null) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "验证码已过期或未发送");
        }
        // 错误次数达到上限时直接作废验证码，防止暴力破解
        String errorCountStr = stringRedisTemplate.opsForValue().get(errorKey);
        int errorCount = errorCountStr == null ? 0 : Integer.parseInt(errorCountStr);
        if (errorCount >= SendCodeSceneEnum.MAX_ERROR_COUNT) {
            stringRedisTemplate.delete(codeKey);
            stringRedisTemplate.delete(errorKey);
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "验证码错误次数过多，请重新获取");
        }
        // 验证码不匹配：累加错误次数，有效期与验证码保持一致且不再续期
        if (!cachedCode.equals(code.trim())) {
            Long failed = stringRedisTemplate.opsForValue().increment(errorKey);
            if (failed != null && failed == 1L) {
                stringRedisTemplate.expire(errorKey, scene.getTtlMinutes(), TimeUnit.MINUTES);
            }
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "验证码错误");
        }
        // 校验通过：一次性消费验证码并清理错误计数，杜绝重放
        stringRedisTemplate.delete(codeKey);
        stringRedisTemplate.delete(errorKey);
    }

    /**
     * 校验邮箱基础格式
     *
     * @param email 邮箱
     */
    private void validateEmail(String email) {
        if (StrUtil.isBlank(email)) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "邮箱不能为空");
        }
        if (email.length() > 100) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "邮箱长度超过限制");
        }
        if (!Validator.isEmail(email)) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "邮箱格式不正确");
        }
    }

    /**
     * 按场景校验邮箱在库中的存在性
     *
     * @param scene 场景
     * @param email 邮箱
     */
    private void checkEmailPolicy(SendCodeSceneEnum scene, String email) {
        SendCodeSceneEnum.EmailPolicy policy = scene.getEmailPolicy();
        if (policy == SendCodeSceneEnum.EmailPolicy.NONE) {
            return;
        }
        long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (policy == SendCodeSceneEnum.EmailPolicy.MUST_EXIST && count == 0) {
            throw new BusinessException(HttpsCodeEnum.NOT_FOUND_ERROR, "该邮箱未注册");
        }
        if (policy == SendCodeSceneEnum.EmailPolicy.MUST_NOT_EXIST && count > 0) {
            throw new BusinessException(HttpsCodeEnum.USER_EXIST, "该邮箱已注册");
        }
    }
}
