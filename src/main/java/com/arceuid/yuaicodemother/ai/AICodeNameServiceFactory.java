package com.arceuid.yuaicodemother.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class AICodeNameServiceFactory {
    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    @Bean
    public AICodeNameGeneratorService aiCodeNameGeneratorService() {
        return AiServices.builder(AICodeNameGeneratorService.class)
                .chatModel(chatModel)
                .build();
    }
}
