package com.example.snipr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Spring Data Redis needs a "RedisTemplate" bean to actually talk to Redis --
 * think of it as the JdbcTemplate/JPA equivalent, but for a key-value store
 * instead of a relational DB. We have to define this bean ourselves (unlike
 * JPA repositories, which Spring generates automatically) because Redis is
 * schema-less -- Spring has no entity classes to infer structure from, so we
 * tell it explicitly: keys and values are both plain strings.
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}