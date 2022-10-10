package com.songyuankun.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * RedisUtils
 *
 * @author songyuankun
 */
@Component
public class RedisUtil {
    /**
     * 不设置过期时长
     */
    private static final long NOT_EXPIRE = -1;
    private final RedisTemplate<String, String> redisTemplate;
    private final ValueOperations<String, String> valueOperations;

    public RedisUtil(RedisTemplate<String, String> redisTemplate, ValueOperations<String, String> valueOperations) {
        this.redisTemplate = redisTemplate;
        this.valueOperations = valueOperations;

    }


    public void setString(String key, String value, long expire) {
        valueOperations.set(key, value);
        if (expire != NOT_EXPIRE) {
            redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        }
    }


    /**
     * 获取值，同时设置过期时间
     */
    public String get(String key) {
        String value = valueOperations.get(key);
        redisTemplate.expire(key, RedisUtil.NOT_EXPIRE, TimeUnit.SECONDS);
        return value;
    }


    /**
     * 删除
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }


}
