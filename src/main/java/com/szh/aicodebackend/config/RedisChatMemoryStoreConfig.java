package com.szh.aicodebackend.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisChatMemoryStoreConfig {

    private String host;

    private int port;

    private String password;

    private long ttl;

    public RedisChatMemoryStore redisChatMemoryStore() {
        return RedisChatMemoryStore.builder()
                .port(port)
                .host(host)
                .password(password)
                .ttl(ttl)
                .build();
    }

}
