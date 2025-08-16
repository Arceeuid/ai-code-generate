package com.arceuid.yuaicodemother.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置生产环境使用的异步任务执行器（用于处理MVC异步请求）
     */
    @Bean("mvcAsyncTaskExecutor")
    public AsyncTaskExecutor mvcAsyncTaskExecutor() {
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
        executor.setThreadNamePrefix("MvcAsync-");
        // 初始化线程池
        executor.initialize();
        return executor;
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // 设置MVC异步请求使用的执行器
        configurer.setTaskExecutor(mvcAsyncTaskExecutor());
        // 设置异步请求超时时间（可选，根据业务需求调整）
        configurer.setDefaultTimeout(30_000); // 30秒
    }
}