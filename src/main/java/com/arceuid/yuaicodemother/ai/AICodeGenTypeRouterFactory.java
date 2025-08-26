package com.arceuid.yuaicodemother.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 代码生成类型路由服务工厂
 */
@Component
public class AICodeGenTypeRouterFactory {
    @Resource
    private ChatModel chatModel;

    /**
     * 代码生成类型路由服务
     *
     * @return 代码生成类型路由服务
     */
    @Bean
    AICodeGenTypeRouterService aiCodeGenTypeRouterService() {
        return AiServices.builder(AICodeGenTypeRouterService.class)
                .chatModel(chatModel)
                .build();
    }
}
