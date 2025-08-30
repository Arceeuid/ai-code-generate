package com.arceuid.yuaicodemother.ai;

import com.arceuid.yuaicodemother.utils.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class AICodeNameServiceFactory {


    public AICodeNameGeneratorService createAiCodeNameGeneratorService() {
        ChatModel chatModel = SpringContextUtil.getBean("routingChatModelPrototype", ChatModel.class);
        return AiServices.builder(AICodeNameGeneratorService.class)
                .chatModel(chatModel)
                .build();
    }

    @Bean
    AICodeNameGeneratorService aiCodeNameGeneratorService() {
        return createAiCodeNameGeneratorService();
    }
}
