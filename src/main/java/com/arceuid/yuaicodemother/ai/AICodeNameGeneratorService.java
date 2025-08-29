package com.arceuid.yuaicodemother.ai;

import com.arceuid.yuaicodemother.ai.model.AppNameResult;
import dev.langchain4j.service.SystemMessage;

public interface AICodeNameGeneratorService {
    /**
     * 生成应用名称
     *
     * @param userMessage 用户提示词
     * @return 应用名称
     */
    @SystemMessage(fromResource = "prompt/codegen-app-name-prompt.txt")
    AppNameResult generateAppName(String userMessage);
}
