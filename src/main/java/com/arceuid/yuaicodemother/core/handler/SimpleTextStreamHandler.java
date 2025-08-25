package com.arceuid.yuaicodemother.core.handler;

import com.arceuid.yuaicodemother.model.entity.User;
import com.arceuid.yuaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.arceuid.yuaicodemother.service.ChatHistoryService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 处理HTML和MUlTI_FILE类型的Flux流
 */
@Component
public class SimpleTextStreamHandler {
    /**
     * 处理HTML和MUlTI_FILE类型的Flux流
     *
     * @param textStream         文本流
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @param chatHistoryService 聊天历史服务
     * @return 处理后的文本流
     */
    public Flux<String> handle(Flux<String> textStream, ChatHistoryService chatHistoryService, Long appId, User loginUser) {
        StringBuilder stringBuilder = new StringBuilder();
        return textStream.map((chunk) -> {
            stringBuilder.append(chunk);
            return chunk;
        }).doOnComplete(() -> {
            chatHistoryService.addChatMessage(appId, loginUser.getId(), stringBuilder.toString(), ChatHistoryMessageTypeEnum.AI.getValue());
        }).doOnError((error) -> {
            String errorMsg = "AI回复失败:" + error.getMessage();
            chatHistoryService.addChatMessage(appId, loginUser.getId(), errorMsg, ChatHistoryMessageTypeEnum.AI.getValue());
        });
    }

}
