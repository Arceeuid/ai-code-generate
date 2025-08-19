package com.arceuid.yuaicodemother.ai;

import com.arceuid.yuaicodemother.service.ChatHistoryService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * AI服务创建工厂
 */
@Component
@Slf4j
public class AICodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;


    /**
     * AI 服务实例缓存
     * 缓存策略：
     * - 最大缓存 1000 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<Long, AICodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除，appId: {}, 原因: {}", key, cause);
            })
            .build();

    /**
     * 根据appId获取AI服务
     *
     * @param appId 应用ID
     * @return AI服务
     */
    public AICodeGeneratorService getAiCodeGeneratorService(long appId) {

        //判断Redis中Key为appId的数据是否过期
        List<ChatMessage> chatList = redisChatMemoryStore.getMessages(appId);

        //当Redis中数据不存在时，清理缓存
        if (chatList == null || chatList.isEmpty()) {
            serviceCache.invalidate(appId);
        }

        return serviceCache.get(appId, this::createAiCodeGeneratorService);
    }

    /**
     * 创建新的AI服务
     *
     * @param appId 应用ID
     * @return AI服务
     */
    private AICodeGeneratorService createAiCodeGeneratorService(Long appId) {
        //构建对话记忆
        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(10)
                .build();

        //加载对话历史
        int loadCount = chatHistoryService.loadChatHistoryToMemory(appId, messageWindowChatMemory, 10);

        //构建AI服务
        return AiServices.builder(AICodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(messageWindowChatMemory)
                .build();
    }


    @Bean
    public AICodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0);
    }

    /**
     * 上下文无关Ai服务
     *
     * @return 上下文无关Ai服务
     */
    @Bean
    public AICodeGeneratorService noContextAiCodeGeneratorService() {
        return AiServices.builder(AICodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .build();
    }
}
