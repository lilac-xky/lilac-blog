package com.lilac.config;

import com.lilac.manager.upload.FileStorageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 存储服务选择器：根据 storage.type 配置决定默认使用 OSS 还是本地存储。
 * 两种实现 Bean 都会被注册，业务代码注入 {@link FileStorageService} 即拿到默认实现。
 */
@Configuration
public class StorageConfig {

    @Bean
    @Primary
    public FileStorageService fileStorageService(
            @Value("${storage.type:oss}") String type,
            @Qualifier("ossStorageService") FileStorageService oss,
            @Qualifier("localStorageService") FileStorageService local) {
        return "local".equalsIgnoreCase(type) ? local : oss;
    }
}
