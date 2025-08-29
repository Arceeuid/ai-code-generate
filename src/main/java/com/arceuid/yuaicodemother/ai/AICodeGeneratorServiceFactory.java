package com.arceuid.yuaicodemother.ai;

import com.arceuid.yuaicodemother.ai.tools.ToolManager;
import com.arceuid.yuaicodemother.exception.BusinessException;
import com.arceuid.yuaicodemother.exception.ErrorCode;
import com.arceuid.yuaicodemother.model.enums.CodeGenTypeEnum;
import com.arceuid.yuaicodemother.service.ChatHistoryService;
import com.arceuid.yuaicodemother.utils.SpringContextUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
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

    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private ToolManager toolManager;


    /**
     * AI 服务实例缓存
     * 缓存策略：
     * - 最大缓存 1000 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<String, AICodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除，appId_key: {}, 原因: {}", key, cause);
            })
            .build();

    /**
     * 根据appId获取AI服务,兼容老的逻辑
     *
     * @param appId 应用ID
     * @return AI服务
     */
    public AICodeGeneratorService getAiCodeGeneratorService(long appId) {
        return getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
    }

    /**
     * 根据appId获取AI服务
     *
     * @param appId 应用ID
     * @param codeGenTypeEnum 代码生成类型
     * @return AI服务
     */
    public AICodeGeneratorService getAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenTypeEnum) {

        //判断Redis中Key为appId的数据是否过期
        List<ChatMessage> chatList = redisChatMemoryStore.getMessages(appId);

        //构造缓存Key
        String cacheKey = buildCacheKey(appId, codeGenTypeEnum);

        //当Redis中数据不存在时，清理缓存
        if (chatList == null || chatList.isEmpty()) {
            serviceCache.invalidate(cacheKey);
        }

        return serviceCache.get(cacheKey, key -> createAiCodeGeneratorService(appId, codeGenTypeEnum));
    }

    /**
     * 创建新的AI服务
     *
     * @param appId 应用ID
     * @return AI服务
     */
    private AICodeGeneratorService createAiCodeGeneratorService(Long appId, CodeGenTypeEnum codeGenTypeEnum) {
        //构建对话记忆
        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();

        //加载对话历史
        int loadCount = chatHistoryService.loadChatHistoryToMemory(appId, messageWindowChatMemory, 10);

        //根据代码生成类型创建AI服务
        return switch (codeGenTypeEnum) {
            //vue项目模式
            case VUE_PROJECT -> {
                //使用多例模式的ChatModel解决并发问题
                StreamingChatModel reasoningStreamingChatModel = SpringContextUtil.getBean("reasoningStreamingChatModelPrototype", StreamingChatModel.class);
                yield AiServices.builder(AICodeGeneratorService.class)
                        .chatModel(chatModel)
                        .streamingChatModel(reasoningStreamingChatModel)
                        .chatMemoryProvider(memoryId -> messageWindowChatMemory)
                        .tools(toolManager.getAllTools())
                        .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(toolExecutionRequest, "Error,cannot find such tool called " + toolExecutionRequest.name()))
                        .build();
            }

            //html和多文件模式
            case HTML, MULTI_FILE -> {
                //使用多例模式的ChatModel解决并发问题
                StreamingChatModel streamingChatModel = SpringContextUtil.getBean("streamingChatModelPrototype", StreamingChatModel.class);
                yield AiServices.builder(AICodeGeneratorService.class)
                        .chatModel(chatModel)
                        .streamingChatModel(streamingChatModel)
                        .chatMemory(messageWindowChatMemory)
                        .build();
            }
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码参数类型: " + codeGenTypeEnum);
        };
    }


    @Bean
    public AICodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0);
    }


    /**
     * 构建缓存Key
     *
     * @param appId           应用ID
     * @param codeGenTypeEnum 代码生成类型
     * @return 缓存Key
     */
    public String buildCacheKey(Long appId, CodeGenTypeEnum codeGenTypeEnum) {
        return appId + "_" + codeGenTypeEnum.getValue();
    }
}
