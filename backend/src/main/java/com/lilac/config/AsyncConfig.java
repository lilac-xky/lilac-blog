package com.lilac.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 邮件异步线程池
     *
     * @return 线程池
     */
    @Bean(name = "mailExecutor")
    public Executor mailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(5);
        // 最大线程数
        executor.setMaxPoolSize(10);
        // 队列容量
        executor.setQueueCapacity(100);
        // 线程前缀名
        executor.setThreadNamePrefix("mail-async-");
        // 设置线程池关闭时等待堆积任务完成
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 设置等待终止的最长时间（秒），避免无限期等待
        executor.setAwaitTerminationSeconds(60);
        // 拒绝策略：由调用者所在的线程执行（保证邮件至少能触发发送）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}