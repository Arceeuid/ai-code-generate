package com.arceuid.yuaicodemother.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
public class ReasoningStreamChatModelConfig {

    private String baseUrl;

    private String apiKey;
    /*  生成环境使用

        private final String MODEL_NAME = "deepseek-reasoner";

        private final int MAX_TOKENS = 32768;
    */
    private final String MODEL_NAME = "deepseek-chat";

    private final int MAX_TOKENS = 8192;

    @Bean
    public StreamingChatModel reasoningStreamChatModel() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(MODEL_NAME)
                .maxTokens(MAX_TOKENS)
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
