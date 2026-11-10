package com.pushkar.distributedlockmanager.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import lombok.Getter;
import lombok.Setter;

@Configuration 
public class RedlockConfig {
    
    @Getter 
    @Setter 
    @ConfigurationProperties(prefix = "redlock")
    public static class RedlockProperties {
        private List<NodeConfig> nodes = new ArrayList<>();
    }

    @Getter 
    @Setter 
    public static class NodeConfig {
        private String host;
        private int port;
    }

    @Bean 
    public RedlockProperties redlockProperties() {
        return new RedlockProperties();
    }

    @Bean 
    public List<StringRedisTemplate> redlockTemplates(RedlockProperties props) {
        List<StringRedisTemplate> templates = new ArrayList<>();
        for (NodeConfig node : props.getNodes()) {
            RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(node.getHost(), node.getPort());
            LettuceConnectionFactory factory = new LettuceConnectionFactory(standaloneConfig);
            factory.afterPropertiesSet();
            templates.add(new StringRedisTemplate(factory));
        }
        return templates;
    }
}
