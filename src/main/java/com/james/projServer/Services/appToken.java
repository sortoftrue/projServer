package com.james.projServer.Services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class appToken {

    @Autowired
    RedisTemplate<String, String> redis;

    public String createAndStoreToken(Integer userId) {
        String authToken;
        authToken = UUID.randomUUID().toString();
        redis.opsForValue().set(authToken, Integer.toString(userId));

        return authToken;
    }

}
