package com.arceuid.yuaicodemother.ai;

import com.arceuid.yuaicodemother.utils.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 代码生成类型路由服务工厂
 */
@Component
public class AICodeGenTypeRouterFactory {

    /**
     * 代码生成类型路由服务
     *
     * @return 代码生成类型路由服务
     */

    public AICodeGenTypeRouterService createAiCodeGenTypeRouterService() {
        //使用多例模式的ChatModel解决并发问题
        ChatModel chatModel = SpringContextUtil.getBean("routingChatModelPrototype", ChatModel.class);
        return AiServices.builder(AICodeGenTypeRouterService.class)
                .chatModel(chatModel)
                .build();
    }

    /**
     * 提供原有的Bean
     *
     * @return 代码生成类型路由服务
     */
    @Bean
    AICodeGenTypeRouterService aiCodeGenTypeRouterService() {
        return createAiCodeGenTypeRouterService();
    }
}
