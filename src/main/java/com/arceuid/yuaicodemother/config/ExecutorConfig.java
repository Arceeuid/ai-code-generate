package com.arceuid.yuaicodemother.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ExecutorService;

@Configuration
public class ExecutorConfig {

    /**
     * 配置线程池用于异步任务（如代码生成、文件保存等）
     * 可根据实际需求调整核心线程数、最大线程数、队列容量等参数
     */
    @Bean
    public ExecutorService asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数（长期保留的线程数）
        executor.setCorePoolSize(4);
        // 最大线程数（当队列满时可临时创建的线程数）
        executor.setMaxPoolSize(8);
        // 队列容量（核心线程忙时，新任务会先进入此队列）
        executor.setQueueCapacity(100);
        // 线程空闲超时时间（超过此时间的临时线程会被回收）
        executor.setKeepAliveSeconds(30);
        // 线程名前缀（方便日志排查）
        executor.setThreadNamePrefix("AsyncTask-");
        // 初始化线程池
        executor.initialize();
        return executor.getThreadPoolExecutor();
    }
}
