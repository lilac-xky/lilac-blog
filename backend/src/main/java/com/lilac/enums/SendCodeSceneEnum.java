package com.lilac.enums;

import com.lilac.constant.UserConstant;
import lombok.Getter;

/**
 * 发送验证码场景枚举
 * 每个常量承载该场景的 Redis key 前缀、有效期、邮件文案与邮箱准入规则，
 * 验证码的发送与校验机制本身只有一份，差异全部收敛到这里。
 */
@Getter
public enum SendCodeSceneEnum {

    /**
     * 注册：邮箱必须尚未注册
     */
    REGISTER(
            "REGISTER",
            "注册",
            UserConstant.USER_REGISTER_CODE_KEY,
            UserConstant.USER_REGISTER_CODE_ERROR_KEY,
            UserConstant.USER_REGISTER_LIMIT_KEY,
            UserConstant.USER_REGISTER_CODE_TTL,
            "【Lilac】注册验证码",
            "您的注册验证码为：%s，有效期5分钟。",
            EmailPolicy.MUST_NOT_EXIST
    ),

    /**
     * 重置密码：邮箱必须已注册
     */
    RESET_PASSWORD(
            "RESET_PASSWORD",
            "重置密码",
            UserConstant.USER_RESET_CODE_KEY,
            UserConstant.USER_RESET_CODE_ERROR_KEY,
            UserConstant.USER_RESET_LIMIT_KEY,
            UserConstant.USER_RESET_CODE_TTL,
            "【Lilac】重置密码验证码",
            "您正在重置密码，验证码为：%s，有效期5分钟。如非本人操作请忽略。",
            EmailPolicy.MUST_EXIST
    ),

    /**
     * 修改邮箱：新邮箱必须尚未被占用
     */
    UPDATE_EMAIL(
            "UPDATE_EMAIL",
            "修改邮箱",
            UserConstant.USER_CHANGE_EMAIL_CODE_KEY,
            UserConstant.USER_CHANGE_EMAIL_CODE_ERROR_KEY,
            UserConstant.USER_CHANGE_EMAIL_LIMIT_KEY,
            UserConstant.USER_CHANGE_EMAIL_CODE_TTL,
            "【Lilac】修改邮箱验证码",
            "您正在修改邮箱，验证码为：%s，有效期5分钟。如非本人操作请忽略。",
            EmailPolicy.MUST_NOT_EXIST
    ),

    /**
     * 修改邮箱-验证原邮箱：原邮箱必须已注册
     */
    UPDATE_EMAIL_OLD(
            "UPDATE_EMAIL_OLD",
            "验证原邮箱",
            UserConstant.USER_CHANGE_EMAIL_OLD_CODE_KEY,
            UserConstant.USER_CHANGE_EMAIL_OLD_CODE_ERROR_KEY,
            UserConstant.USER_CHANGE_EMAIL_OLD_LIMIT_KEY,
            UserConstant.USER_CHANGE_EMAIL_OLD_CODE_TTL,
            "【Lilac】身份验证码",
            "您正在修改绑定邮箱，身份验证码为：%s，有效期5分钟。如非本人操作请立即修改密码。",
            EmailPolicy.MUST_EXIST
    );

    /**
     * 邮箱准入规则：声明发送验证码前邮箱在库中的存在性要求
     */
    public enum EmailPolicy {
        /**
         * 不校验邮箱是否存在
         */

        NONE,

        /**
         * 邮箱必须已存在于 user 表
         */
        MUST_EXIST,

        /**
         * 邮箱必须不存在于 user 表
         */
        MUST_NOT_EXIST
    }

    /**
     * 验证码允许的最大错误次数，超过后直接作废验证码
     */
    public static final int MAX_ERROR_COUNT = 5;

    /**
     * 发送限流窗口，单位秒
     */
    public static final long LIMIT_SECONDS = 60L;

    /**
     * 编码，用于日志与后续接口透传
     */
    private final String code;

    /**
     * 场景描述，用于日志
     */
    private final String desc;

    /**
     * 验证码缓存 key 前缀
     */
    private final String codeKeyPrefix;

    /**
     * 错误次数缓存 key 前缀
     */
    private final String errorKeyPrefix;

    /**
     * 发送限流缓存 key 前缀
     */
    private final String limitKeyPrefix;

    /**
     * 验证码有效期，单位分钟
     */
    private final long ttlMinutes;

    /**
     * 邮件标题
     */
    private final String subject;

    /**
     * 邮件正文模板，用 String.format 填充验证码
     */
    private final String contentTemplate;

    /**
     * 邮箱准入规则
     */
    private final EmailPolicy emailPolicy;

    /**
     * 构造场景常量
     *
     * @param code            编码
     * @param desc            场景描述
     * @param codeKeyPrefix   验证码 key 前缀
     * @param errorKeyPrefix  错误次数 key 前缀
     * @param limitKeyPrefix  限流 key 前缀
     * @param ttlMinutes      验证码有效期（分钟）
     * @param subject         邮件标题
     * @param contentTemplate 邮件正文模板
     * @param emailPolicy     邮箱准入规则
     */
    SendCodeSceneEnum(String code,
                      String desc,
                      String codeKeyPrefix,
                      String errorKeyPrefix,
                      String limitKeyPrefix,
                      long ttlMinutes,
                      String subject,
                      String contentTemplate,
                      EmailPolicy emailPolicy) {
        this.code = code;
        this.desc = desc;
        this.codeKeyPrefix = codeKeyPrefix;
        this.errorKeyPrefix = errorKeyPrefix;
        this.limitKeyPrefix = limitKeyPrefix;
        this.ttlMinutes = ttlMinutes;
        this.subject = subject;
        this.contentTemplate = contentTemplate;
        this.emailPolicy = emailPolicy;
    }
}
