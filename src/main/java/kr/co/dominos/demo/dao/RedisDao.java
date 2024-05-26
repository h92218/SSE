package kr.co.dominos.demo.dao;


import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisDao {
    private final RedisTemplate<String, Object> redisTemplate;

    public void saveSseEmitter(String key, Object data) {
        redisTemplate.opsForValue().set(key,data,30,TimeUnit.SECONDS);
    }

    public Object getSseEmitter(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteSseEmitter(String key) {
        redisTemplate.delete(key);
    }
}
