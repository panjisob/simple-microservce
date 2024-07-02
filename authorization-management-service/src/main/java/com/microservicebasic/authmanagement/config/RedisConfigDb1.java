package com.microservicebasic.authmanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfigDb1 {

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.password}")
    private String redisPassword;

    @Value("${redis.port}")
    private int redisPort;

    @Value("${redis.database1}")
    private int redisDatabase1;

    @Bean(name = "jedisConnectionFactory1")
    JedisConnectionFactory jedisConnectionFactory1() {
        var redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisHost, redisPort);
        redisStandaloneConfiguration.setDatabase(redisDatabase1);
        redisStandaloneConfiguration.setPassword(redisPassword);

        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean(name = "redisTemplate1")
    public RedisTemplate<String, Object> redisTemplate1() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory1());
        template.setKeySerializer(new StringRedisSerializer());
        return template;
    }

    @Bean(name = "redisOperator1")
    public HashOperations<String, Object, Object> redisOperator1() {
        RedisTemplate<String, Object> redisTemplate1 = redisTemplate1();
        return redisTemplate1.opsForHash();
    }
}
