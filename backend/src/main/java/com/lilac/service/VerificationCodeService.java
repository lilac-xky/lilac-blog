package com.lilac.service;

import com.lilac.enums.SendCodeSceneEnum;

/**
 * 验证码服务
 */
public interface VerificationCodeService {
    /**
     * 发送验证码
     *
     * @param scene 场景
     * @param email 邮箱
     */
    void send(SendCodeSceneEnum scene, String email);

    /**
     * 验证验证码
     *
     * @param scene  场景
     * @param email  邮箱
     * @param code 验证码
     */
    void verify(SendCodeSceneEnum scene,String email,String code);
}
