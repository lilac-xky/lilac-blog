package com.lilac.manager.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import java.util.concurrent.TimeUnit;

/**
 * Redis 服务
 */
@Service
public class RedisService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 增加指定 key 的数值，并返回增加后的值
     */
    public Long increment(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

    /**
     * 获取 key 对应的值（Long）
     */
    public Long getLong(String key) {
        String val = stringRedisTemplate.opsForValue().get(key);
        return val == null ? null : Long.parseLong(val);
    }

    /**
     * 设置 key 的值，带过期时间
     */
    public void setEx(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 判断 key 是否存在
     */
    public Boolean exists(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    /**
     * 删除 key
     */
    public Boolean delete(String key) {
        return stringRedisTemplate.delete(key);
    }

    /**
     * 设置键值，仅当 key 不存在时（NX）并设置过期时间（EX）
     * @return true 表示设置成功（即之前不存在），false 表示 key 已存在
     */
    public Boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        return stringRedisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
    }
}